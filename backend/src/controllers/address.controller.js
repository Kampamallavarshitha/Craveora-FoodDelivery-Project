import User from "../models/User.js";


// GET ADDRESSES
export const getAddresses = async (req, res) => {

    try {

        const user =
            await User.findById(req.user.id);

        res.status(200).json({
            success: true,
            addresses: user.addresses || []
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// ADD ADDRESS
export const addAddress = async (req, res) => {

    try {

        const user =
            await User.findById(req.user.id);

        user.addresses.push(req.body);

        await user.save();

        res.status(201).json({
            success: true,
            message: "Address added successfully",
            addresses: user.addresses
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// DELETE ADDRESS
export const deleteAddress = async (req, res) => {

    try {

        const user =
            await User.findById(req.user.id);

        user.addresses =
            user.addresses.filter(
                address =>
                    address._id.toString() !== req.params.id
            );

        await user.save();

        res.status(200).json({
            success: true,
            message: "Address deleted successfully"
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};