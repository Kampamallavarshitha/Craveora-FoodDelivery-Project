// ==========================================
// 404 - ROUTE NOT FOUND MIDDLEWARE
// ==========================================

export const notFound = (req, res, next) => {
    const error = new Error(
        `Route not found: ${req.method} ${req.originalUrl}`
    );

    error.statusCode = 404;

    next(error);
};


// ==========================================
// GLOBAL ERROR HANDLER
// ==========================================

export const errorHandler = (err, req, res, next) => {

    console.error("==========================================");
    console.error("ERROR");
    console.error("==========================================");
    console.error("Method:", req.method);
    console.error("URL:", req.originalUrl);
    console.error("Message:", err.message);
    console.error("Stack:", err.stack);
    console.error("==========================================");

    const statusCode = err.statusCode || 500;

    res.status(statusCode).json({
        success: false,
        statusCode,
        message: err.message || "Internal Server Error",

        ...(process.env.NODE_ENV === "development" && {
            stack: err.stack
        })
    });
};