import express from "express";

import {
    getAddresses,
    addAddress,
    deleteAddress
} from "../controllers/address.controller.js";

import { protect } from "../middleware/auth.middleware.js";

const router = express.Router();

// ==========================================
// ADDRESS ROUTES
// ==========================================

// Get addresses
router.get(
    "/",
    protect,
    getAddresses
);

// Add address
router.post(
    "/",
    protect,
    addAddress
);

// Delete address
router.delete(
    "/:id",
    protect,
    deleteAddress
);

export default router;