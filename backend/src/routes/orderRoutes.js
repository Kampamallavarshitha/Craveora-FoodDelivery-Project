import express from "express";

import {
    createOrder,
    getMyOrders,
    getAllOrders,
    updateOrderStatus
} from "../controllers/order.controller.js";

import { protect } from "../middleware/auth.middleware.js";
import { adminOnly } from "../middleware/admin.middleware.js";

const router = express.Router();

// ==========================================
// USER ORDERS
// ==========================================

// Create order
router.post(
    "/",
    protect,
    createOrder
);

// My orders
router.get(
    "/my-orders",
    protect,
    getMyOrders
);


// ==========================================
// ADMIN ORDERS
// ==========================================

// All orders
router.get(
    "/all",
    protect,
    adminOnly,
    getAllOrders
);

// Update order status
router.put(
    "/:id/status",
    protect,
    adminOnly,
    updateOrderStatus
);

export default router;