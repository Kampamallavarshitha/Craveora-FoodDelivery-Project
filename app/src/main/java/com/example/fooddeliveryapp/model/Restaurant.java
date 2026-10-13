
package com.example.fooddeliveryapp.model;

import com.google.gson.annotations.SerializedName;

public class Restaurant {

    @SerializedName(value = "restaurantId", alternate = {"id", "restaurant_id"})
    private String restaurantId;

    @SerializedName("name")
    private String name;

    @SerializedName(value = "cuisine", alternate = {"cuisines"})
    private String cuisine;

    @SerializedName(value = "rating", alternate = {"avgRating", "avg_rating"})
    private String rating;

    @SerializedName(value = "deliveryTime", alternate = {
            "deliveryTimeMinutes", "delivery_time"
    })
    private String deliveryTime;

    @SerializedName(value = "priceForTwo", alternate = {
            "costForTwo", "cost_for_two"
    })
    private String priceForTwo;

    @SerializedName(value = "imageUrl", alternate = {
            "image", "cloudinaryImageId", "cloudinary_image_id"
    })
    private String imageUrl;

    public Restaurant() {
    }

    public Restaurant(
            String name,
            String cuisine,
            String rating,
            String deliveryTime,
            String priceForTwo,
            String restaurantId,
            String imageUrl
    ) {
        this.name = name;
        this.cuisine = cuisine;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
        this.priceForTwo = priceForTwo;
        this.restaurantId = restaurantId;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public String getCuisine() {
        if (cuisine == null) return "";

        return cuisine;
    }

    public String getRating() {
        return rating == null ? "N/A" : rating;
    }

    public String getDeliveryTime() {
        if (deliveryTime == null || deliveryTime.isEmpty()) {
            return "Delivery time unavailable";
        }

        return deliveryTime.matches("\\d+")
                ? deliveryTime + " min"
                : deliveryTime;
    }

    public String getPriceForTwo() {
        return priceForTwo == null ? "" : priceForTwo;
    }

    public String getRestaurantId() {
        return restaurantId == null ? "" : restaurantId;
    }

    public String getImageUrl() {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        String image = imageUrl.trim();

        if (image.startsWith("https://")
                || image.startsWith("http://")) {
            return image;
        }

        return "https://media-assets.swiggy.com/swiggy/image/upload/"
                + image;
    }
}