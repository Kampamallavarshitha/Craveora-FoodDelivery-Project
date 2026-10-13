import Cart from "../models/Cart.js";


// GET CART
export const getCart = async (req, res) => {

    try {

        let cart = await Cart.findOne({
            user: req.user.id
        }).populate("items.food");

        if (!cart) {

            cart = await Cart.create({
                user: req.user.id,
                items: []
            });

        }

        res.status(200).json({
            success: true,
            cart
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// ADD TO CART
export const addToCart = async (req, res) => {

    try {

        const { foodId, quantity } = req.body;

        let cart = await Cart.findOne({
            user: req.user.id
        });

        if (!cart) {

            cart = await Cart.create({
                user: req.user.id,
                items: []
            });

        }

        const existingItem =
            cart.items.find(
                item =>
                    item.food.toString() === foodId
            );

        if (existingItem) {

            existingItem.quantity += quantity || 1;

        } else {

            cart.items.push({
                food: foodId,
                quantity: quantity || 1
            });

        }

        await cart.save();

        res.status(200).json({
            success: true,
            message: "Item added to cart",
            cart
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// REMOVE FROM CART
export const removeFromCart = async (req, res) => {

    try {

        const { foodId } = req.params;

        const cart = await Cart.findOne({
            user: req.user.id
        });

        cart.items =
            cart.items.filter(
                item =>
                    item.food.toString() !== foodId
            );

        await cart.save();

        res.status(200).json({
            success: true,
            message: "Item removed from cart",
            cart
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// CLEAR CART
export const clearCart = async (req, res) => {

    try {

        const cart = await Cart.findOne({
            user: req.user.id
        });

        cart.items = [];

        await cart.save();

        res.status(200).json({
            success: true,
            message: "Cart cleared"
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};