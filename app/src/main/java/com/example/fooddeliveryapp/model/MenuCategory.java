package com.example.fooddeliveryapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MenuCategory {

    @SerializedName("category")
    private String category;

    @SerializedName("items")
    private List<MenuItem> items;

    public MenuCategory() {
    }

    public MenuCategory(String category, List<MenuItem> items) {
        this.category = category;
        this.items = items;
    }

    public String getCategory() {
        return category;
    }

    public List<MenuItem> getItems() {
        return items;
    }
}
