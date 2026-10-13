
import {
    MCPServerStreamableHttp
} from "@openai/agents";

import {
    getSwiggyToken
} from "./swiggy-oauth.js";

// ============================================================
// SWIGGY FOOD MCP
// ============================================================

const SWIGGY_FOOD_MCP =
    "https://mcp.swiggy.com/food";

// ============================================================
// CREATE SWIGGY FOOD MCP CONNECTION
// ============================================================

export async function createSwiggyFoodMCP(userId) {
    if (!userId || !String(userId).trim()) {
        throw new Error("Craveora user ID is required");
    }

    const token = getSwiggyToken(userId);

    if (!token) {
        throw new Error(
            "Swiggy is not connected for this user. Please authorize Swiggy again."
        );
    }

    if (!token.access_token) {
        throw new Error(
            "Swiggy access token is missing. Please reconnect Swiggy."
        );
    }

    const swiggyFood = new MCPServerStreamableHttp({
        name: "Swiggy Food",
        url: SWIGGY_FOOD_MCP,
        requestInit: {
            headers: {
                Authorization: `Bearer ${token.access_token}`
            }
        }
    });

    try {
        await swiggyFood.connect();
        console.log("Connected to Swiggy Food MCP");
        return swiggyFood;
    } catch (error) {
        try {
            await swiggyFood.close();
        } catch {
            // Preserve the original connection error.
        }

        throw error;
    }
}

// ============================================================
// GET SWIGGY SAVED ADDRESSES
// ============================================================

export async function getSwiggyAddresses(userId) {
    const mcp = await createSwiggyFoodMCP(userId);

    try {
        console.log("Getting Swiggy addresses");

        const result = await mcp.callTool(
            "get_addresses",
            {}
        );

        console.log("Swiggy addresses received");
        return result;
    } finally {
        try {
            await mcp.close();
        } catch (error) {
            console.error("Swiggy MCP close error:", error);
        }
    }
}

// ============================================================
// SEARCH RESTAURANTS
// ============================================================

export async function searchSwiggyRestaurants(
    userId,
    argumentsObject
) {
    if (
        !argumentsObject ||
        !argumentsObject.query ||
        !String(argumentsObject.query).trim()
    ) {
        throw new Error("Restaurant search query is required");
    }

    const mcp = await createSwiggyFoodMCP(userId);

    try {
        console.log("Swiggy restaurant search:", argumentsObject);

        const result = await mcp.callTool(
            "search_restaurants",
            argumentsObject
        );

        console.log("Swiggy restaurant search completed");
        return result;
    } finally {
        try {
            await mcp.close();
        } catch (error) {
            console.error("Swiggy MCP close error:", error);
        }
    }
}

// ============================================================
// GET RESTAURANT MENU
// ============================================================

export async function getSwiggyRestaurantMenu(
    userId,
    restaurantId
) {
    if (
        restaurantId === undefined ||
        restaurantId === null ||
        !String(restaurantId).trim()
    ) {
        throw new Error("Restaurant ID is required");
    }

    const mcp = await createSwiggyFoodMCP(userId);

    try {
        console.log("Getting restaurant menu:", restaurantId);

        const result = await mcp.callTool(
            "get_restaurant_menu",
            {
                restaurantId: String(restaurantId).trim()
            }
        );

        console.log("Restaurant menu received:", restaurantId);
        return result;
    } finally {
        try {
            await mcp.close();
        } catch (error) {
            console.error("Swiggy MCP close error:", error);
        }
    }
}

// ============================================================
// GET RESTAURANT DETAILS
// ============================================================

export async function getSwiggyRestaurantDetails(
    userId,
    restaurantId
) {
    if (
        restaurantId === undefined ||
        restaurantId === null ||
        !String(restaurantId).trim()
    ) {
        throw new Error("Restaurant ID is required");
    }

    const mcp = await createSwiggyFoodMCP(userId);

    try {
        console.log("Getting restaurant details:", restaurantId);

        const result = await mcp.callTool(
            "get_restaurant_details",
            {
                restaurantId: String(restaurantId).trim()
            }
        );

        console.log("Restaurant details received");
        return result;
    } finally {
        try {
            await mcp.close();
        } catch (error) {
            console.error("Swiggy MCP close error:", error);
        }
    }
}

// ============================================================
// LIST SWIGGY FOOD TOOLS
// ============================================================

export async function listSwiggyFoodTools(userId) {
    const mcp = await createSwiggyFoodMCP(userId);

    try {
        const tools = await mcp.listTools();

        console.log("SWIGGY FOOD MCP TOOLS");

        for (const tool of tools) {
            console.log("Tool:", tool.name);
            console.log("Description:", tool.description);
            console.log(
                "Input Schema:",
                JSON.stringify(tool.inputSchema, null, 2)
            );
            console.log("------------------------------------");
        }

        return tools;
    } finally {
        try {
            await mcp.close();
        } catch (error) {
            console.error("Swiggy MCP close error:", error);
        }
    }
}

// ============================================================
// CALL ANY SWIGGY FOOD MCP TOOL
// ============================================================

export async function callSwiggyFoodTool(
    userId,
    toolName,
    argumentsObject = {}
) {
    if (!toolName || !String(toolName).trim()) {
        throw new Error("Swiggy MCP tool name is required");
    }

    if (
        !argumentsObject ||
        typeof argumentsObject !== "object" ||
        Array.isArray(argumentsObject)
    ) {
        throw new Error("Tool arguments must be an object");
    }

    const mcp = await createSwiggyFoodMCP(userId);

    try {
        console.log("====================================");
        console.log("SWIGGY MCP TOOL CALL");
        console.log("====================================");
        console.log("User:", userId);
        console.log("Tool:", toolName);
        console.log("Arguments:", argumentsObject);

        const result = await mcp.callTool(
            String(toolName).trim(),
            argumentsObject
        );

        console.log("Swiggy tool executed");
        return result;
    } finally {
        try {
            await mcp.close();
        } catch (error) {
            console.error("Swiggy MCP close error:", error);
        }
    }
}

// ============================================================
// EXPORT MCP URL
// ============================================================

export {
    SWIGGY_FOOD_MCP
};

