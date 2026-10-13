import jwt from "jsonwebtoken";
import User from "../models/User.js";

// ==========================================
// PROTECT ROUTES
// ==========================================
export const protect = async (req, res, next) => {
    try {
        let token;

        // Check Authorization Header
        if (
            req.headers.authorization &&
            req.headers.authorization.startsWith("Bearer")
        ) {
            token = req.headers.authorization.split(" ")[1];
        }

        // Token not found
        if (!token) {
            return res.status(401).json({
                success: false,
                message: "Not authorized. Token not found."
            });
        }

        // Verify JWT Token
        const decoded = jwt.verify(
            token,
            process.env.JWT_SECRET
        );

        // Find user from token ID
        const user = await User.findById(decoded.id)
            .select("-password");

        if (!user) {
            return res.status(401).json({
                success: false,
                message: "User not found or token is invalid."
            });
        }

        // Attach user to request
        req.user = user;

        next();

    } catch (error) {

        console.error("AUTH ERROR:", error.message);

        return res.status(401).json({
            success: false,
            message: "Not authorized. Invalid or expired token."
        });

    }
};