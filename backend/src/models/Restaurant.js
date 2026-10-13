import mongoose from "mongoose";

const restaurantSchema = new mongoose.Schema(
    {
        name: {
            type: String,
            required: true,
            trim: true
        },

        description: {
            type: String,
            default: ""
        },

        image: {
            type: String,
            default: ""
        },

        coverImage: {
            type: String,
            default: ""
        },

        phone: {
            type: String,
            required: true
        },

        email: {
            type: String,
            default: ""
        },

        address: {
            type: String,
            required: true
        },

        city: {
            type: String,
            required: true
        },

        latitude: {
            type: Number
        },

        longitude: {
            type: Number
        },

        rating: {
            type: Number,
            default: 0,
            min: 0,
            max: 5
        },

        deliveryTime: {
            type: String,
            default: "30-40 mins"
        },

        minimumOrder: {
            type: Number,
            default: 0
        },

        deliveryFee: {
            type: Number,
            default: 0
        },

        cuisine: [
            {
                type: String
            }
        ],

        isOpen: {
            type: Boolean,
            default: true
        },

        isActive: {
            type: Boolean,
            default: true
        }
    },
    {
        timestamps: true
    }
);


const Restaurant = mongoose.model(
    "Restaurant",
    restaurantSchema
);

export default Restaurant;