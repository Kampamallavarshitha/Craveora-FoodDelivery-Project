import {
    MCPServerStreamableHttp
} from "@openai/agents";

import {
    getSwiggyToken
} from "./swiggy-oauth.js";


const SWIGGY_FOOD_URL =
    "https://mcp.swiggy.com/food";


/**
 * Create authenticated Swiggy MCP connection
 */
async function createSwiggyMCP(userId) {

    const tokenData =
        getSwiggyToken(userId);

    if (!tokenData) {

        const error = new Error(
            "Swiggy authentication required"
        );

        error.status = 401;

        throw error;
    }


    const swiggyFood =
        new MCPServerStreamableHttp({

            url: SWIGGY_FOOD_URL,

            name: "Swiggy Food",

            requestInit: {

                headers: {

                    Authorization:
                        `Bearer ${tokenData.accessToken}`

                }

            },

            cacheToolsList: true

        });


    await swiggyFood.connect();


    return swiggyFood;
}


/**
 * Get user's saved Swiggy addresses
 */
export async function getAddresses(userId) {

    const swiggyFood =
        await createSwiggyMCP(userId);

    try {

        const result =
            await swiggyFood.callTool(
                "get_addresses",
                {}
            );

        return result;

    } finally {

        await swiggyFood.close();

    }
}


/**
 * Search restaurants
 *
 * NOTE:
 * Parameters must match the current
 * Swiggy Food MCP tool schema.
 */
export async function searchRestaurants(
    userId,
    argumentsObject
) {

    const swiggyFood =
        await createSwiggyMCP(userId);

    try {

        const result =
            await swiggyFood.callTool(
                "search_restaurants",
                argumentsObject
            );

        return result;

    } finally {

        await swiggyFood.close();

    }
}


/**
 * Generic Swiggy tool caller
 *
 * Useful while developing and testing.
 */
export async function callSwiggyTool(
    userId,
    toolName,
    argumentsObject = {}
) {

    const swiggyFood =
        await createSwiggyMCP(userId);

    try {

        const result =
            await swiggyFood.callTool(
                toolName,
                argumentsObject
            );

        return result;

    } finally {

        await swiggyFood.close();

    }
}