package com.example.fooddeliveryapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MenuResponse {

    @SerializedName("restaurant")
    private RestaurantInfo restaurant;

    @SerializedName("menu")
    private List<MenuCategory> menu;

    public RestaurantInfo getRestaurant() {
        return restaurant;
    }

    public List<MenuCategory> getMenu() {
        return menu;
    }

    public static class RestaurantInfo {

        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}