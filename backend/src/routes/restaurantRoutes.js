import express from "express";

import {
    getRestaurants,
    getRestaurantById,
    createRestaurant,
    updateRestaurant,
    deleteRestaurant
} from "../controllers/restaurant.controller.js";

import { protect } from "../middleware/auth.middleware.js";
import { adminOnly } from "../middleware/admin.middleware.js";

const router = express.Router();

// ==========================================
// PUBLIC
// ==========================================

// Get restaurants
router.get("/", getRestaurants);

// Get restaurant by ID
router.get("/:id", getRestaurantById);


// ==========================================
// ADMIN
// ==========================================

// Create restaurant
router.post(
    "/",
    protect,
    adminOnly,
    createRestaurant
);

// Update restaurant
router.put(
    "/:id",
    protect,
    adminOnly,
    updateRestaurant
);

// Delete restaurant
router.delete(
    "/:id",
    protect,
    adminOnly,
    deleteRestaurant
);

export default router;