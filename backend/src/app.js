import express from "express";
import cors from "cors";

// ==========================================
// ROUTES
// ==========================================

import authRoutes from "./routes/authRoutes.js";
import userRoutes from "./routes/userRoutes.js";
import categoryRoutes from "./routes/Categoryroute.js";
import restaurantRoutes from "./routes/restaurantRoutes.js";
import foodRoutes from "./routes/foodRoutes.js";
import cartRoutes from "./routes/cartRoutes.js";
import orderRoutes from "./routes/orderRoutes.js";
import addressRoutes from "./routes/addressroute.js";
import paymentRoutes from "./routes/paymentroute.js";
import adminRoutes from "./routes/adminroute.js";
import swiggyRoutes from "./routes/swiggyRoutes.js";

// ==========================================
// ERROR MIDDLEWARE
// ==========================================

import {
    notFound,
    errorHandler
} from "./middleware/error.middleware.js";


// ==========================================
// EXPRESS APP
// ==========================================

const app = express();


// ==========================================
// GLOBAL MIDDLEWARE
// ==========================================

app.use(
    cors({
        origin: "*",
        methods: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
        allowedHeaders: ["Content-Type", "Authorization"]
    })
);

app.use(express.json());

app.use(
    express.urlencoded({
        extended: true
    })
);


// ==========================================
// HEALTH CHECK
// ==========================================

app.get("/", (req, res) => {
    res.status(200).json({
        success: true,
        message: "Craveora Food Delivery API is running",
        version: "1.0.0",
        timestamp: new Date().toISOString()
    });
});


// ==========================================
// API HEALTH CHECK
// ==========================================

app.get("/api/v1/health", (req, res) => {
    res.status(200).json({
        success: true,
        message: "Craveora API is healthy",
        timestamp: new Date().toISOString()
    });
});


// ==========================================
// API ROUTES
// ==========================================

app.use(
    "/api/v1/auth",
    authRoutes
);

app.use(
    "/api/v1/users",
    userRoutes
);

app.use(
    "/api/v1/categories",
    categoryRoutes
);

app.use(
    "/api/v1/restaurants",
    restaurantRoutes
);

app.use(
    "/api/v1/foods",
    foodRoutes
);

app.use(
    "/api/v1/cart",
    cartRoutes
);

app.use(
    "/api/v1/orders",
    orderRoutes
);

app.use(
    "/api/v1/addresses",
    addressRoutes
);

app.use(
    "/api/v1/payments",
    paymentRoutes
);

app.use(
    "/api/v1/admin",
    adminRoutes
);

app.use(
    "/api/v1/swiggy",
    swiggyRoutes
);


// ==========================================
// 404 HANDLER
// ==========================================

app.use(notFound);


// ==========================================
// GLOBAL ERROR HANDLER
// ==========================================

app.use(errorHandler);


// ==========================================
// EXPORT APP
// ==========================================

export default app;