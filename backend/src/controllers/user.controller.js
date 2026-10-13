import User from "../models/User.js";

// ================================
// GET USER PROFILE
// ================================
export const getUserProfile = async (req, res) => {

    try {

        const user = await User.findById(req.user.id)
            .select("-password");

        res.status(200).json({
            success: true,
            user
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// ================================
// UPDATE USER PROFILE
// ================================
export const updateUserProfile = async (req, res) => {

    try {

        const { name, phone } = req.body;

        const user = await User.findByIdAndUpdate(
            req.user.id,
            {
                name,
                phone
            },
            {
                new: true
            }
        ).select("-password");

        res.status(200).json({
            success: true,
            message: "Profile updated successfully",
            user
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};