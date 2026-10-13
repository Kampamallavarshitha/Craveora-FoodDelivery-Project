
import express from "express";

import {
    startSwiggyAuth,
    swiggyCallback,
    swiggyStatus,
    swiggyLogout,
    swiggyTestMCP,
    swiggyListTools,
    callSwiggyTool,
    swiggyRestaurants
} from "../controllers/swiggy.controller.js";

const router = express.Router();


// ==========================================
// SWIGGY OAUTH
// ==========================================

router.get(
    "/auth",
    startSwiggyAuth
);

router.get(
    "/callback",
    swiggyCallback
);

router.get(
    "/status",
    swiggyStatus
);

router.post(
    "/logout",
    swiggyLogout
);


// ==========================================
// SWIGGY RESTAURANTS
// ==========================================

router.get(
    "/restaurants",
    swiggyRestaurants
);


// ==========================================
// SWIGGY MCP TEST
// ==========================================

router.get(
    "/mcp-test",
    swiggyTestMCP
);


// ==========================================
// LIST AVAILABLE MCP TOOLS
// ==========================================

router.get(
    "/tools",
    swiggyListTools
);


// ==========================================
// CALL SWIGGY MCP TOOL
// ==========================================

router.post(
    "/call",
    callSwiggyTool
);


export default router;

