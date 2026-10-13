import express from "express";

import {
    getDashboardStats
} from "../controllers/admin.controller.js";

import { protect } from "../middleware/auth.middleware.js";
import { adminOnly } from "../middleware/admin.middleware.js";

const router = express.Router();

// ==========================================
// ADMIN DASHBOARD
// ==========================================

router.get(
    "/dashboard",
    protect,
    adminOnly,
    getDashboardStats
);

export default router;