
import {
    startSwiggyOAuth,
    handleSwiggyCallback,
    getSwiggyToken,
    logoutSwiggy
} from "../services/swiggy-oauth.js";

import {
    createSwiggyFoodMCP,
    searchSwiggyRestaurants
} from "../services/swiggy-mcp.js";

// ==========================================
// GET /api/v1/swiggy/auth
// ==========================================

export async function startSwiggyAuth(req, res) {
    try {
        const userId =
            req.user?.id ||
            req.headers["x-user-id"];

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "Craveora user authentication required"
            });
        }

        const result = await startSwiggyOAuth(userId);

        return res.status(200).json({
            success: true,
            message: "Swiggy authorization started",
            authorizationUrl: result.authorizationUrl
        });
    } catch (error) {
        console.error("Swiggy OAuth start error:", error);

        return res.status(500).json({
            success: false,
            message: error.message || "Unable to start Swiggy authorization"
        });
    }
}

// ==========================================
// SWIGGY MCP TEST
// ==========================================

export async function swiggyTestMCP(req, res) {
    let swiggyFood;

    try {
        const userId =
            req.user?.id ||
            req.headers["x-user-id"];

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "User authentication required"
            });
        }

        swiggyFood = await createSwiggyFoodMCP(userId);

        const result = await swiggyFood.callTool(
            "get_addresses",
            {}
        );

        console.log("SWIGGY ADDRESSES:", result);

        return res.status(200).json({
            success: true,
            message: "Swiggy MCP connected successfully",
            data: result
        });
    } catch (error) {
        console.error("Swiggy MCP error:", error);

        const statusCode =
            error.message?.includes("not connected") ||
            error.message?.includes("access token is missing")
                ? 401
                : 500;

        return res.status(statusCode).json({
            success: false,
            message: error.message || "Swiggy MCP test failed"
        });
    } finally {
        if (swiggyFood) {
            try {
                await swiggyFood.close();
            } catch (error) {
                console.error("Swiggy MCP close error:", error);
            }
        }
    }
}

// ==========================================
// GET /api/v1/swiggy/restaurants
// ==========================================

export async function swiggyRestaurants(req, res) {
    try {
        const userId =
            req.user?.id ||
            req.headers["x-user-id"];

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "User authentication required"
            });
        }

        const {
            query,
            addressId,
            latitude,
            longitude
        } = req.query;

        if (!query || !String(query).trim()) {
            return res.status(400).json({
                success: false,
                message: "Restaurant search query is required"
            });
        }

        const argumentsObject = {
            query: String(query).trim()
        };

        if (addressId) {
            argumentsObject.addressId = addressId;
        }

        if (latitude !== undefined && latitude !== "") {
            const parsedLatitude = Number(latitude);

            if (!Number.isFinite(parsedLatitude)) {
                return res.status(400).json({
                    success: false,
                    message: "Invalid latitude"
                });
            }

            argumentsObject.latitude = parsedLatitude;
        }

        if (longitude !== undefined && longitude !== "") {
            const parsedLongitude = Number(longitude);

            if (!Number.isFinite(parsedLongitude)) {
                return res.status(400).json({
                    success: false,
                    message: "Invalid longitude"
                });
            }

            argumentsObject.longitude = parsedLongitude;
        }

        console.log("Swiggy restaurant search:", {
            userId,
            argumentsObject
        });

        const result = await searchSwiggyRestaurants(
            userId,
            argumentsObject
        );

        return res.status(200).json({
            success: true,
            message: "Restaurants fetched from Swiggy",
            data: result
        });
    } catch (error) {
        console.error("Swiggy restaurant search error:", error);

        const statusCode =
            error.message?.includes("not connected") ||
            error.message?.includes("access token is missing")
                ? 401
                : 500;

        return res.status(statusCode).json({
            success: false,
            message: error.message || "Restaurant search failed"
        });
    }
}

// ==========================================
// GET SWIGGY MCP TOOLS
// ==========================================

export async function swiggyListTools(req, res) {
    let swiggyFood;

    try {
        const userId =
            req.user?.id ||
            req.headers["x-user-id"];

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "User authentication required"
            });
        }

        swiggyFood = await createSwiggyFoodMCP(userId);

        const tools = await swiggyFood.listTools();

        console.log("SWIGGY MCP AVAILABLE TOOLS");

        for (const tool of tools) {
            console.log(`Tool: ${tool.name}`);
            console.log("Description:", tool.description);
            console.log(
                "Input Schema:",
                JSON.stringify(tool.inputSchema, null, 2)
            );
        }

        return res.status(200).json({
            success: true,
            totalTools: tools.length,
            tools
        });
    } catch (error) {
        console.error("Swiggy MCP tools error:", error);

        return res.status(500).json({
            success: false,
            message: error.message || "Unable to list Swiggy tools"
        });
    } finally {
        if (swiggyFood) {
            try {
                await swiggyFood.close();
            } catch (error) {
                console.error("Swiggy MCP close error:", error);
            }
        }
    }
}

// ==========================================
// CALL SWIGGY MCP TOOL
// ==========================================

export const callSwiggyTool = async (req, res) => {
    try {
        const userId =
            req.user?.id ||
            req.headers["x-user-id"];

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "User authentication required"
            });
        }

        const {
            toolName,
            arguments: toolArguments = {}
        } = req.body || {};

        if (!toolName || !String(toolName).trim()) {
            return res.status(400).json({
                success: false,
                message: "toolName is required"
            });
        }

        if (
            !toolArguments ||
            typeof toolArguments !== "object" ||
            Array.isArray(toolArguments)
        ) {
            return res.status(400).json({
                success: false,
                message: "Tool arguments must be an object"
            });
        }

        console.log("====================================");
        console.log("SWIGGY MCP TOOL CALL");
        console.log("====================================");
        console.log("User:", userId);
        console.log("Tool:", toolName);
        console.log("Arguments:", toolArguments);

        const swiggy = await createSwiggyFoodMCP(userId);

        try {
            const result = await swiggy.callTool(
                String(toolName).trim(),
                toolArguments
            );

            console.log("Swiggy tool executed successfully");

            return res.status(200).json({
                success: true,
                toolName,
                data: result
            });
        } finally {
            try {
                await swiggy.close();
            } catch (error) {
                console.error("Swiggy MCP close error:", error);
            }
        }
    } catch (error) {
        console.error("Swiggy MCP tool error:", error);

        const statusCode =
            error.message?.includes("not connected") ||
            error.message?.includes("access token is missing")
                ? 401
                : 500;

        return res.status(statusCode).json({
            success: false,
            message: error.message || "Swiggy MCP tool failed"
        });
    }
};

// ==========================================
// SWIGGY OAUTH CALLBACK
// ==========================================

export async function swiggyCallback(req, res) {
    try {
        const {
            code,
            state,
            error,
            error_description
        } = req.query;

        if (error) {
            return res.status(400).json({
                success: false,
                message: error_description || error
            });
        }

        if (!code || !state) {
            return res.status(400).json({
                success: false,
                message: "Missing OAuth code or state"
            });
        }

        const result = await handleSwiggyCallback(code, state);

        return res.status(200).json({
            success: true,
            message: "Swiggy connected successfully",
            userId: result.userId,
            expiresIn: result.expiresIn,
            scope: result.scope
        });
    } catch (error) {
        console.error("Swiggy callback error:", error);

        return res.status(500).json({
            success: false,
            message: error.message || "Swiggy authorization failed"
        });
    }
}

// ==========================================
// SWIGGY CONNECTION STATUS
// ==========================================

export async function swiggyStatus(req, res) {
    try {
        const userId =
            req.user?.id ||
            req.headers["x-user-id"];

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "User authentication required"
            });
        }

        const token = getSwiggyToken(userId);

        return res.status(200).json({
            success: true,
            connected: Boolean(token?.access_token)
        });
    } catch (error) {
        console.error("Swiggy status error:", error);

        return res.status(500).json({
            success: false,
            message: error.message || "Unable to check Swiggy status"
        });
    }
}

// ==========================================
// SWIGGY LOGOUT
// ==========================================

export async function swiggyLogout(req, res) {
    try {
        const userId =
            req.user?.id ||
            req.headers["x-user-id"];

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "User authentication required"
            });
        }

        logoutSwiggy(userId);

        return res.status(200).json({
            success: true,
            message: "Swiggy connection removed"
        });
    } catch (error) {
        console.error("Swiggy logout error:", error);

        return res.status(500).json({
            success: false,
            message: error.message || "Swiggy logout failed"
        });
    }
}

