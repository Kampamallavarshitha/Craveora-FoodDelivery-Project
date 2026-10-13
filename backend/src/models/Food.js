import mongoose from "mongoose";

const foodSchema = new mongoose.Schema(
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

        price: {
            type: Number,
            required: true,
            min: 0
        },

        originalPrice: {
            type: Number,
            default: 0
        },

        discount: {
            type: Number,
            default: 0
        },

        restaurant: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "Restaurant",
            required: true
        },

        category: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "Category",
            required: true
        },

        foodType: {
            type: String,
            enum: ["veg", "non-veg", "egg"],
            default: "veg"
        },

        rating: {
            type: Number,
            default: 0,
            min: 0,
            max: 5
        },

        isAvailable: {
            type: Boolean,
            default: true
        },

        isRecommended: {
            type: Boolean,
            default: false
        }
    },
    {
        timestamps: true
    }
);


const Food = mongoose.model(
    "Food",
    foodSchema
);

export default Food;