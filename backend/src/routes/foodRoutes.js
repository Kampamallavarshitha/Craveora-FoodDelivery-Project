import express from "express";

import {
    getFoods,
    getFoodById,
    createFood,
    updateFood,
    deleteFood
} from "../controllers/food.controller.js";

import { protect } from "../middleware/auth.middleware.js";
import { adminOnly } from "../middleware/admin.middleware.js";

const router = express.Router();

// ==========================================
// PUBLIC
// ==========================================

// Get all foods
router.get("/", getFoods);

// Get food by ID
router.get("/:id", getFoodById);


// ==========================================
// ADMIN
// ==========================================

// Add food
router.post(
    "/",
    protect,
    adminOnly,
    createFood
);

// Update food
router.put(
    "/:id",
    protect,
    adminOnly,
    updateFood
);

// Delete food
router.delete(
    "/:id",
    protect,
    adminOnly,
    deleteFood
);

export default router;