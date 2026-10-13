import express from "express";

import {
    registerUser,
    loginUser,
    getMe
} from "../controllers/auth.controller.js";

import { protect } from "../middleware/auth.middleware.js";

const router = express.Router();

// ==========================================
// AUTH ROUTES
// ==========================================

// Register
router.post("/register", registerUser);

// Login
router.post("/login", loginUser);

// Current logged-in user
router.get("/me", protect, getMe);

export default router;