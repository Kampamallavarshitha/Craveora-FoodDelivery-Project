import mongoose from "mongoose";

const addressSchema = new mongoose.Schema(
    {
        label: {
            type: String,
            default: "Home"
        },

        fullName: {
            type: String,
            required: true
        },

        phone: {
            type: String,
            required: true
        },

        houseNo: {
            type: String,
            required: true
        },

        street: {
            type: String
        },

        landmark: {
            type: String
        },

        city: {
            type: String,
            required: true
        },

        state: {
            type: String,
            required: true
        },

        pincode: {
            type: String,
            required: true
        },

        latitude: {
            type: Number
        },

        longitude: {
            type: Number
        },

        isDefault: {
            type: Boolean,
            default: false
        }
    },
    {
        timestamps: true
    }
);


const userSchema = new mongoose.Schema(
    {
        name: {
            type: String,
            required: true,
            trim: true
        },

        email: {
            type: String,
            required: true,
            unique: true,
            lowercase: true,
            trim: true
        },

        phone: {
            type: String,
            required: true,
            unique: true
        },

        password: {
            type: String,
            required: true,
            minlength: 6
        },

        profileImage: {
            type: String,
            default: ""
        },

        role: {
            type: String,
            enum: ["user", "admin"],
            default: "user"
        },

        addresses: [addressSchema],

        isActive: {
            type: Boolean,
            default: true
        }
    },
    {
        timestamps: true
    }
);


const User = mongoose.model("User", userSchema);

export default User;