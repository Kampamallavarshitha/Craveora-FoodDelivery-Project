import User from "../models/User.js";
import Order from "../models/Order.js";
import Food from "../models/Food.js";
import Restaurant from "../models/Restaurant.js";


// ADMIN DASHBOARD
export const getDashboardStats = async (req, res) => {

    try {

        const totalUsers =
            await User.countDocuments();

        const totalOrders =
            await Order.countDocuments();

        const totalFoods =
            await Food.countDocuments();

        const totalRestaurants =
            await Restaurant.countDocuments();

        const orders =
            await Order.find();

        const totalRevenue =
            orders.reduce(
                (sum, order) =>
                    sum + order.totalAmount,
                0
            );

        res.status(200).json({

            success: true,

            data: {

                totalUsers,

                totalOrders,

                totalFoods,

                totalRestaurants,

                totalRevenue

            }

        });

    } catch (error) {

        res.status(500).json({

            success: false,

            message: error.message

        });

    }
};