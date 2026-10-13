import mongoose from "mongoose";

const connectDB = async () => {
    try {
        if (!process.env.MONGO_URI) {
            throw new Error("MONGO_URI is missing in .env");
        }

        await mongoose.connect(process.env.MONGO_URI);

        console.log("==========================================");
        console.log("✅ MongoDB Connected Successfully");
        console.log(`📦 Database: ${mongoose.connection.name}`);
        console.log("==========================================");

    } catch (error) {
        console.error("❌ MongoDB Connection Failed:");
        console.error(error.message);

        process.exit(1);
    }
};

export default connectDB;