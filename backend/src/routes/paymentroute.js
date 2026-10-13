import express from "express";

import {
    cashOnDelivery
} from "../controllers/payment.controller.js";

import { protect } from "../middleware/auth.middleware.js";

const router = express.Router();

// ==========================================
// PAYMENT ROUTES
// ==========================================

// Cash on Delivery
router.post(
    "/cod",
    protect,
    cashOnDelivery
);

export default router;