import Category from "../models/Category.js";

// ==========================================
// CREATE CATEGORY
// ==========================================
export const createCategory = async (req, res, next) => {
    try {
        const { name, image, description } = req.body;

        if (!name) {
            return res.status(400).json({
                success: false,
                message: "Category name is required"
            });
        }

        const existingCategory = await Category.findOne({
            name: name.trim()
        });

        if (existingCategory) {
            return res.status(409).json({
                success: false,
                message: "Category already exists"
            });
        }

        const category = await Category.create({
            name: name.trim(),
            image,
            description
        });

        res.status(201).json({
            success: true,
            message: "Category created successfully",
            data: category
        });

    } catch (error) {
        next(error);
    }
};


// ==========================================
// GET ALL CATEGORIES
// ==========================================
export const getCategories = async (req, res, next) => {
    try {
        const categories = await Category.find()
            .sort({ createdAt: -1 });

        res.status(200).json({
            success: true,
            count: categories.length,
            data: categories
        });

    } catch (error) {
        next(error);
    }
};


// ==========================================
// GET CATEGORY BY ID
// ==========================================
export const getCategoryById = async (req, res, next) => {
    try {
        const category = await Category.findById(req.params.id);

        if (!category) {
            return res.status(404).json({
                success: false,
                message: "Category not found"
            });
        }

        res.status(200).json({
            success: true,
            data: category
        });

    } catch (error) {
        next(error);
    }
};


// ==========================================
// UPDATE CATEGORY
// ==========================================
export const updateCategory = async (req, res, next) => {
    try {
        const { name, image, description } = req.body;

        const category = await Category.findByIdAndUpdate(
            req.params.id,
            {
                ...(name && { name: name.trim() }),
                ...(image !== undefined && { image }),
                ...(description !== undefined && { description })
            },
            {
                new: true,
                runValidators: true
            }
        );

        if (!category) {
            return res.status(404).json({
                success: false,
                message: "Category not found"
            });
        }

        res.status(200).json({
            success: true,
            message: "Category updated successfully",
            data: category
        });

    } catch (error) {
        next(error);
    }
};


// ==========================================
// DELETE CATEGORY
// ==========================================
export const deleteCategory = async (req, res, next) => {
    try {
        const category = await Category.findByIdAndDelete(req.params.id);

        if (!category) {
            return res.status(404).json({
                success: false,
                message: "Category not found"
            });
        }

        res.status(200).json({
            success: true,
            message: "Category deleted successfully"
        });

    } catch (error) {
        next(error);
    }
};