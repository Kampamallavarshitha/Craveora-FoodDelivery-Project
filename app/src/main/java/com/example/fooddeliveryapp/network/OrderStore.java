package com.example.fooddeliveryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Saves placed orders on the phone (SharedPreferences + Gson). */
public final class OrderStore {

    private OrderStore() {}

    private static final String KEY = "orders";
    private static final Gson GSON = new Gson();

    public static class Order {
        public String id;
        public String restaurant;
        public List<String> items = new ArrayList<>();
        public int count;
        public int total;
        public long time;

        public long minutesAgo() {
            return (System.currentTimeMillis() - time) / 60000L;
        }

        // Demo tracking: status advances with time since the order was placed.
        public String status() {
            long m = minutesAgo();
            if (m < 1) return "Order placed";
            if (m < 3) return "Preparing";
            if (m < 6) return "Out for delivery";
            return "Delivered";
        }

        public int progress() {
            long m = minutesAgo();
            if (m < 1) return 25;
            if (m < 3) return 50;
            if (m < 6) return 75;
            return 100;
        }

        public int color() {
            long m = minutesAgo();
            if (m < 1) return Color.parseColor("#F59E0B");
            if (m < 3) return Color.parseColor("#FC6A1E");
            if (m < 6) return Color.parseColor("#2563EB");
            return Color.parseColor("#16A34A");
        }
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences("fd_orders", Context.MODE_PRIVATE);
    }

    public static List<Order> getAll(Context c) {
        try {
            Type type = new TypeToken<List<Order>>() {}.getType();
            List<Order> list = GSON.fromJson(prefs(c).getString(KEY, "[]"), type);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /** Builds an order from the current cart. Call BEFORE cart.clear(). */
    public static Order place(Context c, CartManager cart) {

        Order order = new Order();
        order.id = "FD" + (10000 + new Random().nextInt(90000));
        order.restaurant = cart.getRestaurantName();
        order.count = cart.getTotalCount();
        order.total = cart.getTotalPrice();
        order.time = System.currentTimeMillis();

        for (CartManager.CartEntry e : cart.getEntries()) {
            order.items.add(e.quantity + " x " + e.item.getName());
        }

        List<Order> all = getAll(c);
        all.add(0, order);
        prefs(c).edit().putString(KEY, GSON.toJson(all)).apply();

        return order;
    }

    public static void clear(Context c) {
        prefs(c).edit().remove(KEY).apply();
    }
}