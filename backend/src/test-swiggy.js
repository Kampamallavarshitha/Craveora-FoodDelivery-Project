import {
    MCPServerStreamableHttp
} from "@openai/agents";

const swiggyFood = new MCPServerStreamableHttp({
    url: "https://mcp.swiggy.com/food"
});

try {
    console.log("Connecting to Swiggy MCP...");

    await swiggyFood.connect();

    console.log("✅ Connected to Swiggy MCP");

    const tools = await swiggyFood.listTools();

    console.log("Available Swiggy tools:");

    for (const tool of tools) {
        console.log("-", tool.name);
    }

    await swiggyFood.close();

} catch (error) {

    console.error("❌ Swiggy MCP connection failed:");

    console.error(error);

}