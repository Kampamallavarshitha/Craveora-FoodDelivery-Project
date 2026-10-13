import express from "express";

import {
    getUserProfile,
    updateUserProfile
} from "../controllers/user.controller.js";

import { protect } from "../middleware/auth.middleware.js";

const router = express.Router();

// ==========================================
// USER ROUTES
// ==========================================

// Get profile
router.get("/profile", protect, getUserProfile);

// Update profile
router.put("/profile", protect, updateUserProfile);

export default router;