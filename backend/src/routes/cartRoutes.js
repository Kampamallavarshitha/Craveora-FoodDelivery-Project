import express from "express";

import {
    getCart,
    addToCart,
    removeFromCart,
    clearCart
} from "../controllers/cart.controller.js";

import { protect } from "../middleware/auth.middleware.js";

const router = express.Router();

// ==========================================
// CART ROUTES
// ==========================================

// Get cart
router.get("/", protect, getCart);

// Add food to cart
router.post("/add", protect, addToCart);

// Remove food from cart
router.delete(
    "/remove/:foodId",
    protect,
    removeFromCart
);

// Clear cart
router.delete(
    "/clear",
    protect,
    clearCart
);

export default router;