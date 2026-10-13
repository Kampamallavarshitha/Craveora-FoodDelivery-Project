package com.example.fooddeliveryapp;

import android.text.TextUtils;
import android.util.Log;

import com.example.fooddeliveryapp.model.Restaurant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Same parsing logic as HomeFragment, reusable by other screens (Explore). */
public final class RestaurantParser {

    private static final String TAG = "RestaurantParser";

    private RestaurantParser() {}

    public static List<Restaurant> parse(String result) {

        List<Restaurant> out = new ArrayList<>();

        if (result == null || result.trim().isEmpty()) return out;

        if (parseJson(result, out) && !out.isEmpty()) return out;

        out.clear();
        parseText(result, out);

        return out;
    }

    // ---------------- JSON ----------------

    private static boolean parseJson(String result, List<Restaurant> out) {

        try {

            String jsonText = cleanJsonText(result);
            if (jsonText.isEmpty()) return false;

            JsonReader reader = new JsonReader(new StringReader(jsonText));
            reader.setLenient(true);
            JsonElement root = JsonParser.parseReader(reader);

            List<JsonObject> objects = new ArrayList<>();
            collect(root, objects);

            Set<String> added = new HashSet<>();

            for (JsonObject r : objects) {

                String id = getRestaurantId(r);
                String name = firstString(r, "name", "restaurantName", "restaurant_name", "title");
                String image = getImageUrl(r);
                String rating = firstString(r, "avgRating", "avg_rating", "rating", "ratings");
                String time = firstString(r, "deliveryTimeMinutes", "delivery_time", "deliveryTime", "sla");

                if (!time.isEmpty() && time.matches("\\d+")) time += " min";

                String price = firstString(r, "costForTwo", "cost_for_two", "price");
                String cuisine = getCuisine(r);

                if (!name.isEmpty() && !id.isEmpty() && !added.contains(id)) {
                    out.add(new Restaurant(name, cuisine, rating, time, price, id, image));
                    added.add(id);
                }
            }

            return !out.isEmpty();

        } catch (Exception e) {
            Log.e(TAG, "JSON parsing failed", e);
            return false;
        }
    }

    private static String cleanJsonText(String result) {

        if (result == null) return "";

        String text = result.trim();

        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?", "");
            text = text.replaceFirst("```\\s*$", "");
            text = text.trim();
        }

        int o = text.indexOf("{");
        int a = text.indexOf("[");
        int start;

        if (o >= 0 && a >= 0) start = Math.min(o, a);
        else if (o >= 0) start = o;
        else start = a;

        if (start > 0) text = text.substring(start);

        return text.trim();
    }

    private static void collect(JsonElement element, List<JsonObject> output) {

        if (element == null || element.isJsonNull()) return;

        if (element.isJsonObject()) {

            JsonObject object = element.getAsJsonObject();

            String name = firstString(object, "name", "restaurantName", "restaurant_name", "title");
            String id = getRestaurantId(object);

            if (!name.isEmpty() && !id.isEmpty()) output.add(object);

            for (String key : object.keySet()) {
                JsonElement child = object.get(key);
                if (child == null || child.isJsonNull()) continue;
                if (child.isJsonObject() || child.isJsonArray()) collect(child, output);
            }

        } else if (element.isJsonArray()) {

            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) collect(child, output);
        }
    }

    private static String getRestaurantId(JsonObject r) {

        if (r == null) return "";

        String[] keys = {"restaurantId", "restaurant_id", "restaurantID", "restId", "rest_id", "restID"};

        for (String key : keys) {
            String id = firstString(r, key);
            if (valid(id)) return id;
        }

        if (r.has("info") && r.get("info").isJsonObject()) {
            String id = firstString(r.getAsJsonObject("info"), "restaurantId", "restaurant_id", "id");
            if (valid(id)) return id;
        }

        if (r.has("card") && r.get("card").isJsonObject()) {
            String id = getRestaurantId(r.getAsJsonObject("card"));
            if (valid(id)) return id;
        }

        if (r.has("restaurant") && r.get("restaurant").isJsonObject()) {
            String id = getRestaurantId(r.getAsJsonObject("restaurant"));
            if (valid(id)) return id;
        }

        String generic = firstString(r, "id");
        if (valid(generic)) return generic;

        String deep = findDeep(r);
        if (valid(deep)) return deep;

        return "";
    }

    private static boolean valid(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        String v = id.trim();
        return !v.equalsIgnoreCase("null") && !v.equalsIgnoreCase("undefined");
    }

    private static String findDeep(JsonElement element) {

        if (element == null || element.isJsonNull()) return "";

        if (element.isJsonObject()) {

            JsonObject object = element.getAsJsonObject();

            String[] keys = {"restaurantId", "restaurant_id", "restaurantID", "restId", "rest_id", "restID"};

            for (String key : keys) {
                String id = firstString(object, key);
                if (valid(id)) return id;
            }

            if (object.has("info") && object.get("info").isJsonObject()) {
                String id = firstString(object.getAsJsonObject("info"), "id", "restaurantId", "restaurant_id");
                if (valid(id)) return id;
            }

            String name = firstString(object, "name", "restaurantName", "restaurant_name", "title");
            String cuisine = firstString(object, "cuisine");

            boolean looksLikeRestaurant = !name.isEmpty()
                    || object.has("cuisines")
                    || object.has("avgRating")
                    || object.has("cloudinaryImageId")
                    || !cuisine.isEmpty();

            if (looksLikeRestaurant && object.has("id")) {
                String id = firstString(object, "id");
                if (valid(id)) return id;
            }

            for (String key : object.keySet()) {
                JsonElement child = object.get(key);
                if (child == null || child.isJsonNull()) continue;
                if (child.isJsonObject() || child.isJsonArray()) {
                    String id = findDeep(child);
                    if (valid(id)) return id;
                }
            }

        } else if (element.isJsonArray()) {

            for (JsonElement child : element.getAsJsonArray()) {
                String id = findDeep(child);
                if (valid(id)) return id;
            }
        }

        return "";
    }

    private static String getImageUrl(JsonObject r) {

        if (r == null) return "";

        String url = firstString(r, "imageUrl", "image_url", "image", "logo", "cloudinaryImageUrl");
        if (!url.isEmpty()) return url;

        String imageId = firstString(r, "cloudinaryImageId", "cloudinary_image_id", "imageId");
        if (!imageId.isEmpty()) return buildImageUrl(imageId);

        if (r.has("info") && r.get("info").isJsonObject()) {

            JsonObject info = r.getAsJsonObject("info");

            url = firstString(info, "imageUrl", "image_url", "image", "logo", "cloudinaryImageUrl");
            if (!url.isEmpty()) return url;

            imageId = firstString(info, "cloudinaryImageId", "cloudinary_image_id", "imageId");
            if (!imageId.isEmpty()) return buildImageUrl(imageId);
        }

        return "";
    }

    private static String buildImageUrl(String imageId) {

        String value = imageId.trim();

        if (value.isEmpty()) return "";

        if (value.startsWith("http://") || value.startsWith("https://")) return value;

        return "https://media-assets.swiggy.com/swiggy/image/upload/fl_lossy,f_auto,q_auto,w_660/" + value;
    }

    private static String firstString(JsonObject object, String... keys) {

        if (object == null) return "";

        for (String key : keys) {

            try {

                if (object.has(key) && !object.get(key).isJsonNull()) {

                    JsonElement el = object.get(key);

                    if (el.isJsonPrimitive()) {
                        String value = el.getAsString().trim();
                        if (!value.isEmpty()) return value;
                    }
                }

            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private static String getCuisine(JsonObject r) {

        try {

            if (r.has("cuisines")) {

                JsonElement cuisines = r.get("cuisines");

                if (cuisines.isJsonArray()) {

                    List<String> names = new ArrayList<>();

                    for (JsonElement item : cuisines.getAsJsonArray()) {

                        if (item.isJsonPrimitive()) {
                            names.add(item.getAsString());
                        } else if (item.isJsonObject()) {
                            String n = firstString(item.getAsJsonObject(), "name", "title");
                            if (!n.isEmpty()) names.add(n);
                        }
                    }

                    return TextUtils.join(", ", names);
                }

                if (cuisines.isJsonPrimitive()) return cuisines.getAsString().trim();
            }

            String cuisine = firstString(r, "cuisine");
            if (!cuisine.isEmpty()) return cuisine;

            if (r.has("info") && r.get("info").isJsonObject()) {
                return getCuisine(r.getAsJsonObject("info"));
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    // ---------------- TEXT FALLBACK ----------------
    // 1. Restaurant Name (ID: 123) — Cuisine | 4.2 | 30 min | ₹300

    private static void parseText(String result, List<Restaurant> out) {

        Set<String> added = new HashSet<>();

        for (String original : result.split("\\r?\\n")) {

            try {

                String line = original.trim();

                if (line.isEmpty()) continue;
                if (line.startsWith("{") || line.startsWith("[") || line.startsWith("\"")) continue;

                String id = idFromText(line);
                String name = nameFromText(line);

                String cuisine = "", rating = "", time = "", price = "";

                String clean = line.replaceAll("\\(ID\\s*:\\s*[^)]*\\)", "").trim();
                String[] parts = clean.split("—");

                if (parts.length >= 2) {

                    String[] d = parts[1].split("\\|");

                    if (d.length >= 1) cuisine = d[0].trim();
                    if (d.length >= 2) rating = d[1].replace("★", "").trim();
                    if (d.length >= 3) time = d[2].trim();
                    if (d.length >= 4) price = d[3].trim();
                }

                if (!name.isEmpty() && !id.isEmpty() && !added.contains(id)) {
                    out.add(new Restaurant(name, cuisine, rating, time, price, id, ""));
                    added.add(id);
                }

            } catch (Exception e) {
                Log.e(TAG, "Text parsing error", e);
            }
        }
    }

    private static String idFromText(String line) {

        String[] patterns = {
                "\\(ID\\s*:\\s*([0-9]+)\\)",
                "restaurantId\\s*[:=]\\s*[\"']?([0-9]+)",
                "restaurant_id\\s*[:=]\\s*[\"']?([0-9]+)",
                "restId\\s*[:=]\\s*[\"']?([0-9]+)",
                "rest_id\\s*[:=]\\s*[\"']?([0-9]+)",
                "\"id\"\\s*:\\s*\"?([0-9]+)\"?"
        };

        for (String p : patterns) {

            Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(line);

            if (m.find()) {
                String id = m.group(1);
                if (valid(id)) return id;
            }
        }

        return "";
    }

    private static String nameFromText(String line) {

        String name = line;

        name = name.replaceFirst("^\\d+\\.\\s*", "");
        name = name.replaceAll("\\(ID\\s*:\\s*[^)]*\\)", "");
        name = name.replace("**", "");

        if (name.contains("—")) name = name.substring(0, name.indexOf("—"));

        int idx = name.indexOf("restaurantId");
        if (idx >= 0) name = name.substring(0, idx);

        return name.trim();
    }
}