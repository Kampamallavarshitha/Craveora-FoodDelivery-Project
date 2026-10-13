import Food from "../models/Food.js";


// GET ALL FOODS
export const getFoods = async (req, res) => {

    try {

        const foods = await Food.find({
            isAvailable: true
        })
            .populate("restaurant")
            .populate("category");

        res.status(200).json({
            success: true,
            count: foods.length,
            foods
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// GET FOOD BY ID
export const getFoodById = async (req, res) => {

    try {

        const food =
            await Food.findById(req.params.id)
                .populate("restaurant")
                .populate("category");

        if (!food) {

            return res.status(404).json({
                success: false,
                message: "Food not found"
            });

        }

        res.status(200).json({
            success: true,
            food
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// CREATE FOOD
export const createFood = async (req, res) => {

    try {

        const food =
            await Food.create(req.body);

        res.status(201).json({
            success: true,
            message: "Food added successfully",
            food
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// UPDATE FOOD
export const updateFood = async (req, res) => {

    try {

        const food =
            await Food.findByIdAndUpdate(
                req.params.id,
                req.body,
                { new: true }
            );

        res.status(200).json({
            success: true,
            message: "Food updated successfully",
            food
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};


// DELETE FOOD
export const deleteFood = async (req, res) => {

    try {

        await Food.findByIdAndDelete(
            req.params.id
        );

        res.status(200).json({
            success: true,
            message: "Food deleted successfully"
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};