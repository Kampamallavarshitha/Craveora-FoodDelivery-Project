import http from "http";
import dotenv from "dotenv";

import app from "./app.js";
import connectDB from "./config/db.js";

dotenv.config();

// Environment
const PORT = process.env.PORT || 5001;

// Database
connectDB();

// Create HTTP server
const server = http.createServer(app);

// Start server on all network interfaces
server.listen(PORT, "0.0.0.0", () => {
    console.log("====================================");
    console.log("🚀 CRAVEORA BACKEND SERVER");
    console.log("====================================");
    console.log(`🚀 Server running on port ${PORT}`);
    console.log(`🌐 Local: http://localhost:${PORT}`);
    console.log(`🌐 Network: http://YOUR_PC_IP:${PORT}`);
    console.log("====================================");
});

// Handle server errors
server.on("error", (error) => {
    console.error("❌ Server Error:", error.message);
});


// ==========================================
// SERVER ERROR
// ==========================================

server.on("error", (error) => {
    console.error("❌ Server Error:", error.message);
});