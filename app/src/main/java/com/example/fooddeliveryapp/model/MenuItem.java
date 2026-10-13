package com.example.fooddeliveryapp.model;

public class MenuItem {

    public static final int VEG_UNKNOWN = 0;
    public static final int VEG = 1;
    public static final int NON_VEG = 2;

    private final String id;
    private final String name;
    private final int price;          // in rupees
    private final String description;
    private final String imageUrl;
    private final int vegType;
    private final boolean bestseller;
    private final boolean customisable;

    public MenuItem(
            String id,
            String name,
            int price,
            String description,
            String imageUrl
    ) {
        this(id, name, price, description, imageUrl, VEG_UNKNOWN, false, false);
    }

    public MenuItem(
            String id,
            String name,
            int price,
            String description,
            String imageUrl,
            int vegType,
            boolean bestseller,
            boolean customisable
    ) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
        this.price = price;
        this.description = description == null ? "" : description;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.vegType = vegType;
        this.bestseller = bestseller;
        this.customisable = customisable;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getVegType() {
        return vegType;
    }

    public boolean isBestseller() {
        return bestseller;
    }

    public boolean isCustomisable() {
        return customisable;
    }

    /** Same item with a different image (used when the image comes on a later line). */
    public MenuItem withImage(String newImageUrl) {
        return new MenuItem(id, name, price, description, newImageUrl,
                vegType, bestseller, customisable);
    }

    public String getFullImageUrl() {
        String image = imageUrl.trim();

        if (image.isEmpty()) {
            return "";
        }

        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }

        return "https://media-assets.swiggy.com/swiggy/image/upload/"
                + "fl_lossy,f_auto,q_auto,w_300/" + image;
    }
}