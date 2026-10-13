package com.example.fooddeliveryapp;

import com.example.fooddeliveryapp.model.MenuItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple in-memory cart. One restaurant at a time:
 * adding an item from a different restaurant clears the old cart.
 */
public class CartManager {

    public static class CartEntry {
        public final MenuItem item;
        public int quantity;

        CartEntry(MenuItem item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }

    private static final CartManager INSTANCE = new CartManager();

    public static CartManager getInstance() {
        return INSTANCE;
    }

    private final Map<String, CartEntry> entries = new LinkedHashMap<>();
    private String restaurantId = "";
    private String restaurantName = "";

    private CartManager() {
    }

    public void add(String restId, String restName, MenuItem item) {

        if (!restId.equals(restaurantId)) {
            entries.clear();
            restaurantId = restId;
            restaurantName = restName;
        }

        CartEntry entry = entries.get(item.getId());

        if (entry == null) {
            entries.put(item.getId(), new CartEntry(item, 1));
        } else {
            entry.quantity++;
        }
    }

    public void remove(MenuItem item) {

        CartEntry entry = entries.get(item.getId());

        if (entry == null) return;

        entry.quantity--;

        if (entry.quantity <= 0) {
            entries.remove(item.getId());
        }
    }

    public int getQuantity(String itemId) {
        CartEntry entry = entries.get(itemId);
        return entry == null ? 0 : entry.quantity;
    }

    public int getTotalCount() {
        int count = 0;
        for (CartEntry e : entries.values()) count += e.quantity;
        return count;
    }

    public int getTotalPrice() {
        int total = 0;
        for (CartEntry e : entries.values()) {
            total += e.item.getPrice() * e.quantity;
        }
        return total;
    }

    public List<CartEntry> getEntries() {
        return new ArrayList<>(entries.values());
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void clear() {
        entries.clear();
        restaurantId = "";
        restaurantName = "";
    }
}