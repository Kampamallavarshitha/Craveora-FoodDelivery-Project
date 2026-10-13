import mongoose from "mongoose";


const orderItemSchema = new mongoose.Schema(
    {
        food: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "Food",
            required: true
        },

        quantity: {
            type: Number,
            required: true,
            min: 1
        },

        price: {
            type: Number,
            required: true,
            min: 0
        }
    },
    {
        _id: false
    }
);


const orderSchema = new mongoose.Schema(
    {
        user: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User",
            required: true
        },

        items: [orderItemSchema],

        address: {
            fullName: String,
            phone: String,
            houseNo: String,
            street: String,
            landmark: String,
            city: String,
            state: String,
            pincode: String,
            latitude: Number,
            longitude: Number
        },

        paymentMethod: {
            type: String,
            enum: [
                "COD",
                "ONLINE",
                "RAZORPAY"
            ],
            default: "COD"
        },

        paymentStatus: {
            type: String,
            enum: [
                "Pending",
                "Paid",
                "Failed"
            ],
            default: "Pending"
        },

        totalAmount: {
            type: Number,
            required: true,
            min: 0
        },

        orderStatus: {
            type: String,
            enum: [
                "Pending",
                "Confirmed",
                "Preparing",
                "Out for Delivery",
                "Delivered",
                "Cancelled"
            ],
            default: "Pending"
        }
    },
    {
        timestamps: true
    }
);


const Order = mongoose.model(
    "Order",
    orderSchema
);

export default Order;