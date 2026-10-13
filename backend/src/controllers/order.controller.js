import Order from "../models/Order.js";
import Cart from "../models/Cart.js";


// CREATE ORDER
export const createOrder = async (req, res) => {

    try {

        const {
            address,
            paymentMethod,
            totalAmount
        } = req.body;

        const cart = await Cart.findOne({
            user: req.user.id
        }).populate("items.food");

        if (!cart || cart.items.length === 0) {

            return res.status(400).json({
                success: false,
                message: "Cart is empty"
            });

        }

        const order = await Order.create({

            user: req.user.id,

            items: cart.items.map(item => ({
                food: item.food._id,
                quantity: item.quantity,
                price: item.food.price
            })),

            address,

            paymentMethod,

            totalAmount,

            orderStatus: "Pending"
        });

        cart.items = [];

        await cart.save();

        res.status(201).json({
            success: true,
            message: "Order placed successfully",
            order
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// GET MY ORDERS
export const getMyOrders = async (req, res) => {

    try {

        const orders = await Order.find({
            user: req.user.id
        })
            .populate("items.food")
            .sort({ createdAt: -1 });

        res.status(200).json({
            success: true,
            orders
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// GET ALL ORDERS - ADMIN
export const getAllOrders = async (req, res) => {

    try {

        const orders =
            await Order.find()
                .populate("user", "name email phone")
                .populate("items.food")
                .sort({ createdAt: -1 });

        res.status(200).json({
            success: true,
            orders
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// UPDATE ORDER STATUS
export const updateOrderStatus = async (req, res) => {

    try {

        const { status } = req.body;

        const order =
            await Order.findByIdAndUpdate(
                req.params.id,
                {
                    orderStatus: status
                },
                {
                    new: true
                }
            );

        res.status(200).json({
            success: true,
            message: "Order status updated",
            order
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};