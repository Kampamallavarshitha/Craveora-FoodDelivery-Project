package com.example.fooddeliveryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddeliveryapp.adapters.RestaurantAdapter;
import com.example.fooddeliveryapp.model.Restaurant;
import com.example.fooddeliveryapp.model.SwiggyRequest;
import com.example.fooddeliveryapp.model.SwiggyResponse;
import com.example.fooddeliveryapp.network.ApiClient;
import com.example.fooddeliveryapp.network.SwiggyApiService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // ============================================================
    // CONSTANTS
    // ============================================================

    private static final String USER_ID = "test-user-001";
    private static final String ADDRESS_ID = "da7d8kc1d96midrdfum0";

    // ============================================================
    // VIEWS / DATA / API
    // ============================================================

    private EditText searchFood;
    private RecyclerView restaurantRecyclerView;

    private RestaurantAdapter restaurantAdapter;
    private final List<Restaurant> restaurantList = new ArrayList<>();

    private SwiggyApiService apiService;

    public HomeFragment() {
    }

    // ============================================================
    // CREATE VIEW
    // ============================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchFood = view.findViewById(R.id.searchFood);
        restaurantRecyclerView = view.findViewById(R.id.restaurantRecyclerView);

        // ---------------- UI: SAFE AREA (status bar / notch) ----------------

        UiUtils.applyTopInset(view.findViewById(R.id.headerContainer));

        // ---------------- ADAPTER ----------------

        restaurantAdapter = new RestaurantAdapter(
                restaurantList,
                restaurant -> {

                    String restaurantId = restaurant.getRestaurantId();
                    String restaurantName = restaurant.getName();

                    Log.d(TAG, "RESTAURANT CLICKED: " + restaurantName
                            + " | ID = " + restaurantId
                            + " | ADDRESS = " + ADDRESS_ID);

                    if (restaurantId == null
                            || restaurantId.trim().isEmpty()
                            || restaurantId.equalsIgnoreCase("null")) {

                        Toast.makeText(
                                requireContext(),
                                "Restaurant ID not found",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.e(TAG, "Restaurant ID EMPTY for: " + restaurantName);
                        return;
                    }

                    Intent intent = new Intent(
                            requireContext(),
                            RestaurantMenuActivity.class
                    );

                    intent.putExtra("RESTAURANT_ID", restaurantId);
                    intent.putExtra("RESTAURANT_NAME", restaurantName);
                    intent.putExtra("ADDRESS_ID", ADDRESS_ID);

                    startActivity(intent);

                    // UI: smooth slide transition
                    requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.fade_out_slow);
                }
        );

        // ---------------- RECYCLER VIEW ----------------

        restaurantRecyclerView.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        restaurantRecyclerView.setAdapter(restaurantAdapter);

        // ---------------- RETROFIT ----------------

        apiService = ApiClient.getClient().create(SwiggyApiService.class);

        // ---------------- DEFAULT SEARCH ----------------

        searchRestaurants("biryani");

        // ---------------- SEARCH BOX ----------------

        searchFood.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                String query = searchFood.getText().toString().trim();

                if (!query.isEmpty()) {
                    searchRestaurants(query);
                } else {
                    Toast.makeText(
                            requireContext(),
                            "Enter food name",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                return true;
            }

            return false;
        });

        // ---------------- UI: CATEGORY CHIPS ----------------

        final int[] chipIds = {
                R.id.chipBiryani, R.id.chipPizza, R.id.chipBurger,
                R.id.chipDosa, R.id.chipChinese, R.id.chipDesserts
        };
        final String[] chipQueries = {
                "biryani", "pizza", "burger", "dosa", "chinese", "desserts"
        };

        for (int i = 0; i < chipIds.length; i++) {

            final String q = chipQueries[i];
            View chip = view.findViewById(chipIds[i]);

            UiUtils.attachPress(chip);

            chip.setOnClickListener(c -> {
                for (int id : chipIds) {
                    view.findViewById(id).setSelected(id == c.getId());
                }
                searchFood.setText("");
                searchRestaurants(q);
            });
        }

        view.findViewById(R.id.chipBiryani).setSelected(true);

        // ---------------- UI: ENTRANCE ANIMATION ----------------

        UiUtils.staggerIn(
                view.findViewById(R.id.headerContainer),
                view.findViewById(R.id.chipScroll),
                view.findViewById(R.id.rowTitle),
                view.findViewById(R.id.promoCard)
        );

        return view;
    }

    // ============================================================
    // SEARCH RESTAURANTS
    // ============================================================

    private void searchRestaurants(String query) {

        Log.d(TAG, "SEARCHING SWIGGY | Query = " + query
                + " | Address ID = " + ADDRESS_ID);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("query", query);
        arguments.put("addressId", ADDRESS_ID);

        SwiggyRequest request = new SwiggyRequest("search_restaurants", arguments);

        apiService
                .callSwiggyTool(USER_ID, request)
                .enqueue(new Callback<SwiggyResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<SwiggyResponse> call,
                            @NonNull Response<SwiggyResponse> response
                    ) {

                        if (!isAdded()) return;

                        Log.d(TAG, "HTTP CODE = " + response.code());

                        if (response.isSuccessful() && response.body() != null) {

                            SwiggyResponse swiggyResponse = response.body();

                            if (swiggyResponse.isSuccess()
                                    && swiggyResponse.getData() != null
                                    && !swiggyResponse.getData().isEmpty()) {

                                String result = extractResultText(swiggyResponse);

                                Log.d("SWIGGY_RESULT", "RAW RESULT:\n" + result);

                                parseRestaurants(result);

                            } else {

                                restaurantList.clear();
                                restaurantAdapter.notifyDataSetChanged();

                                Toast.makeText(
                                        requireContext(),
                                        "No restaurants found",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {

                            Log.e(TAG, "Server Error = " + response.code());

                            if (response.errorBody() != null) {
                                try {
                                    Log.e(TAG, "ERROR BODY = "
                                            + response.errorBody().string());
                                } catch (Exception ignored) {
                                }
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Server Error: " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<SwiggyResponse> call,
                            @NonNull Throwable t
                    ) {

                        Log.e(TAG, "BACKEND CONNECTION ERROR", t);

                        if (!isAdded()) return;

                        Toast.makeText(
                                requireContext(),
                                "Backend Error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // ============================================================
    // EXTRACT TEXT FROM MCP RESPONSE
    // ============================================================

    private String extractResultText(SwiggyResponse response) {

        if (response == null || response.getData() == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (SwiggyResponse.SwiggyData data : response.getData()) {

            if (data == null) continue;

            String text = data.getText();

            if (text != null && !text.trim().isEmpty()) {

                if (builder.length() > 0) {
                    builder.append("\n");
                }

                builder.append(text);
            }
        }

        return builder.toString().trim();
    }

    // ============================================================
    // PARSE RESTAURANTS
    // ============================================================

    private void parseRestaurants(String result) {

        restaurantList.clear();

        if (result == null || result.trim().isEmpty()) {
            restaurantAdapter.notifyDataSetChanged();
            return;
        }

        boolean jsonSuccess = parseJsonRestaurants(result);

        if (jsonSuccess && !restaurantList.isEmpty()) {

            restaurantAdapter.notifyDataSetChanged();

            // UI: replay card slide-in animation
            restaurantRecyclerView.scheduleLayoutAnimation();

            Log.d(TAG, "JSON restaurants = " + restaurantList.size());
            return;
        }

        // ---------------- FALLBACK TEXT PARSER ----------------

        Log.w(TAG, "JSON parsing produced nothing, using text fallback");

        restaurantList.clear();

        parseTextRestaurants(result);

        restaurantAdapter.notifyDataSetChanged();

        // UI: replay card slide-in animation
        restaurantRecyclerView.scheduleLayoutAnimation();

        Log.d(TAG, "TEXT restaurants = " + restaurantList.size());

        if (restaurantList.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "No restaurants could be parsed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ============================================================
    // JSON PARSER
    // ============================================================

    private boolean parseJsonRestaurants(String result) {

        try {

            String jsonText = cleanJsonText(result);

            if (jsonText.isEmpty()) {
                return false;
            }

            // FIX: read only the FIRST JSON value and ignore anything after it
            // (trailing notes, extra text blocks, etc.)
            JsonReader reader = new JsonReader(new StringReader(jsonText));
            reader.setLenient(true);
            JsonElement root = JsonParser.parseReader(reader);

            List<JsonObject> restaurantObjects = new ArrayList<>();

            collectRestaurantObjects(root, restaurantObjects);

            Log.d(TAG, "Restaurant JSON objects found = "
                    + restaurantObjects.size());

            Set<String> addedIds = new HashSet<>();

            for (JsonObject restaurant : restaurantObjects) {

                String restaurantId = getRestaurantId(restaurant);

                String name = getFirstString(
                        restaurant,
                        "name", "restaurantName", "restaurant_name", "title"
                );

                String imageUrl = getImageUrl(restaurant);

                String rating = getFirstString(
                        restaurant,
                        "avgRating", "avg_rating", "rating", "ratings"
                );

                String deliveryTime = getFirstString(
                        restaurant,
                        "deliveryTimeMinutes", "delivery_time",
                        "deliveryTime", "sla"
                );

                if (!deliveryTime.isEmpty() && deliveryTime.matches("\\d+")) {
                    deliveryTime += " min";
                }

                String price = getFirstString(
                        restaurant,
                        "costForTwo", "cost_for_two", "price"
                );

                String cuisine = getCuisine(restaurant);

                if (!name.isEmpty()
                        && !restaurantId.isEmpty()
                        && !addedIds.contains(restaurantId)) {

                    restaurantList.add(
                            new Restaurant(
                                    name,
                                    cuisine,
                                    rating,
                                    deliveryTime,
                                    price,
                                    restaurantId,
                                    imageUrl
                            )
                    );

                    addedIds.add(restaurantId);

                    Log.d(TAG, "ADDED: " + name + " | ID = " + restaurantId
                            + " | IMAGE = " + imageUrl);
                }
            }

            return !restaurantList.isEmpty();

        } catch (Exception e) {

            Log.e(TAG, "JSON parsing failed", e);

            return false;
        }
    }

    // ============================================================
    // CLEAN JSON
    // ============================================================

    private String cleanJsonText(String result) {

        if (result == null) {
            return "";
        }

        String text = result.trim();

        // Remove markdown fence

        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?", "");
            text = text.replaceFirst("```\\s*$", "");
            text = text.trim();
        }

        // Sometimes MCP returns text before JSON.
        // Locate the first JSON object/array.

        int objectStart = text.indexOf("{");
        int arrayStart = text.indexOf("[");

        int start;

        if (objectStart >= 0 && arrayStart >= 0) {
            start = Math.min(objectStart, arrayStart);
        } else if (objectStart >= 0) {
            start = objectStart;
        } else {
            start = arrayStart;
        }

        if (start > 0) {
            text = text.substring(start);
        }

        return text.trim();
    }

    // ============================================================
    // COLLECT RESTAURANT OBJECTS
    // ============================================================

    private void collectRestaurantObjects(
            JsonElement element,
            List<JsonObject> output
    ) {

        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonObject()) {

            JsonObject object = element.getAsJsonObject();

            String name = getFirstString(
                    object,
                    "name", "restaurantName", "restaurant_name", "title"
            );

            String id = getRestaurantId(object);

            if (!name.isEmpty() && !id.isEmpty()) {
                output.add(object);
            }

            for (String key : object.keySet()) {

                JsonElement child = object.get(key);

                if (child == null || child.isJsonNull()) continue;

                if (child.isJsonObject() || child.isJsonArray()) {
                    collectRestaurantObjects(child, output);
                }
            }

        } else if (element.isJsonArray()) {

            JsonArray array = element.getAsJsonArray();

            for (JsonElement child : array) {
                collectRestaurantObjects(child, output);
            }
        }
    }

    // ============================================================
    // GET RESTAURANT ID
    // ============================================================

    private String getRestaurantId(JsonObject restaurant) {

        if (restaurant == null) {
            return "";
        }

        // 1. Direct restaurant id keys

        String[] directIdKeys = {
                "restaurantId", "restaurant_id", "restaurantID",
                "restId", "rest_id", "restID"
        };

        for (String key : directIdKeys) {

            String id = getFirstString(restaurant, key);

            if (isValidRestaurantId(id)) {
                return id;
            }
        }

        // 2. info object

        if (restaurant.has("info") && restaurant.get("info").isJsonObject()) {

            JsonObject info = restaurant.getAsJsonObject("info");

            String id = getFirstString(info, "restaurantId", "restaurant_id", "id");

            if (isValidRestaurantId(id)) {
                return id;
            }
        }

        // 3. card object

        if (restaurant.has("card") && restaurant.get("card").isJsonObject()) {

            String id = getRestaurantId(restaurant.getAsJsonObject("card"));

            if (isValidRestaurantId(id)) {
                return id;
            }
        }

        // 4. nested restaurant object

        if (restaurant.has("restaurant")
                && restaurant.get("restaurant").isJsonObject()) {

            String id = getRestaurantId(restaurant.getAsJsonObject("restaurant"));

            if (isValidRestaurantId(id)) {
                return id;
            }
        }

        // 5. generic id

        String genericId = getFirstString(restaurant, "id");

        if (isValidRestaurantId(genericId)) {
            return genericId;
        }

        // 6. deep search

        String deepId = findRestaurantIdDeep(restaurant);

        if (isValidRestaurantId(deepId)) {
            return deepId;
        }

        return "";
    }

    private boolean isValidRestaurantId(String id) {

        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String value = id.trim();

        return !value.equalsIgnoreCase("null")
                && !value.equalsIgnoreCase("undefined");
    }

    // ============================================================
    // DEEP RESTAURANT ID SEARCH
    // ============================================================

    private String findRestaurantIdDeep(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return "";
        }

        if (element.isJsonObject()) {

            JsonObject object = element.getAsJsonObject();

            String[] idKeys = {
                    "restaurantId", "restaurant_id", "restaurantID",
                    "restId", "rest_id", "restID"
            };

            for (String key : idKeys) {

                String id = getFirstString(object, key);

                if (isValidRestaurantId(id)) {
                    return id;
                }
            }

            if (object.has("info") && object.get("info").isJsonObject()) {

                String infoId = getFirstString(
                        object.getAsJsonObject("info"),
                        "id", "restaurantId", "restaurant_id"
                );

                if (isValidRestaurantId(infoId)) {
                    return infoId;
                }
            }

            String name = getFirstString(
                    object,
                    "name", "restaurantName", "restaurant_name", "title"
            );

            String cuisine = getFirstString(object, "cuisine");

            boolean looksLikeRestaurant =
                    !name.isEmpty()
                            || object.has("cuisines")
                            || object.has("avgRating")
                            || object.has("cloudinaryImageId")
                            || !cuisine.isEmpty();

            if (looksLikeRestaurant && object.has("id")) {

                String id = getFirstString(object, "id");

                if (isValidRestaurantId(id)) {
                    return id;
                }
            }

            for (String key : object.keySet()) {

                JsonElement child = object.get(key);

                if (child == null || child.isJsonNull()) continue;

                if (child.isJsonObject() || child.isJsonArray()) {

                    String id = findRestaurantIdDeep(child);

                    if (isValidRestaurantId(id)) {
                        return id;
                    }
                }
            }

        } else if (element.isJsonArray()) {

            for (JsonElement child : element.getAsJsonArray()) {

                String id = findRestaurantIdDeep(child);

                if (isValidRestaurantId(id)) {
                    return id;
                }
            }
        }

        return "";
    }

    // ============================================================
    // IMAGE URL
    // ============================================================

    private String getImageUrl(JsonObject restaurant) {

        if (restaurant == null) {
            return "";
        }

        String imageUrl = getFirstString(
                restaurant,
                "imageUrl", "image_url", "image", "logo", "cloudinaryImageUrl"
        );

        if (!imageUrl.isEmpty()) {
            return imageUrl;
        }

        String imageId = getFirstString(
                restaurant,
                "cloudinaryImageId", "cloudinary_image_id", "imageId"
        );

        if (!imageId.isEmpty()) {
            return buildSwiggyImageUrl(imageId);
        }

        if (restaurant.has("info") && restaurant.get("info").isJsonObject()) {

            JsonObject info = restaurant.getAsJsonObject("info");

            imageUrl = getFirstString(
                    info,
                    "imageUrl", "image_url", "image", "logo", "cloudinaryImageUrl"
            );

            if (!imageUrl.isEmpty()) {
                return imageUrl;
            }

            imageId = getFirstString(
                    info,
                    "cloudinaryImageId", "cloudinary_image_id", "imageId"
            );

            if (!imageId.isEmpty()) {
                return buildSwiggyImageUrl(imageId);
            }
        }

        return "";
    }

    private String buildSwiggyImageUrl(String imageId) {

        if (imageId == null || imageId.trim().isEmpty()) {
            return "";
        }

        String value = imageId.trim();

        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }

        return "https://media-assets.swiggy.com/swiggy/image/upload/"
                + "fl_lossy,f_auto,q_auto,w_660/"
                + value;
    }

    // ============================================================
    // GET STRING
    // ============================================================

    private String getFirstString(JsonObject object, String... keys) {

        if (object == null) {
            return "";
        }

        for (String key : keys) {

            try {

                if (object.has(key) && !object.get(key).isJsonNull()) {

                    JsonElement element = object.get(key);

                    if (element.isJsonPrimitive()) {

                        String value = element.getAsString().trim();

                        if (!value.isEmpty()) {
                            return value;
                        }
                    }
                }

            } catch (Exception ignored) {
            }
        }

        return "";
    }

    // ============================================================
    // CUISINE
    // ============================================================

    private String getCuisine(JsonObject restaurant) {

        try {

            if (restaurant.has("cuisines")) {

                JsonElement cuisines = restaurant.get("cuisines");

                if (cuisines.isJsonArray()) {

                    List<String> names = new ArrayList<>();

                    for (JsonElement item : cuisines.getAsJsonArray()) {

                        if (item.isJsonPrimitive()) {

                            names.add(item.getAsString());

                        } else if (item.isJsonObject()) {

                            String name = getFirstString(
                                    item.getAsJsonObject(),
                                    "name", "title"
                            );

                            if (!name.isEmpty()) {
                                names.add(name);
                            }
                        }
                    }

                    return TextUtils.join(", ", names);
                }

                if (cuisines.isJsonPrimitive()) {
                    return cuisines.getAsString().trim();
                }
            }

            String cuisine = getFirstString(restaurant, "cuisine");

            if (!cuisine.isEmpty()) {
                return cuisine;
            }

            if (restaurant.has("info") && restaurant.get("info").isJsonObject()) {
                return getCuisine(restaurant.getAsJsonObject("info"));
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    // ============================================================
    // TEXT PARSER (FALLBACK)
    // Expected line format:
    //   1. Restaurant Name (ID: 123) — Cuisine | 4.2 | 30 min | ₹300
    // ============================================================

    private void parseTextRestaurants(String result) {

        if (result == null || result.trim().isEmpty()) {
            return;
        }

        String[] lines = result.split("\\r?\\n");

        Set<String> addedIds = new HashSet<>();

        for (String originalLine : lines) {

            try {

                String line = originalLine.trim();

                if (line.isEmpty()) {
                    continue;
                }

                // FIX: skip raw JSON lines. They are not
                // "Name — details" lines and would become one junk card.
                if (line.startsWith("{")
                        || line.startsWith("[")
                        || line.startsWith("\"")) {
                    continue;
                }

                String restaurantId = extractRestaurantIdFromText(line);
                String restaurantName = extractRestaurantNameFromText(line);

                String cuisine = "";
                String rating = "";
                String deliveryTime = "";
                String price = "";

                String cleanLine = line
                        .replaceAll("\\(ID\\s*:\\s*[^)]*\\)", "")
                        .trim();

                String[] parts = cleanLine.split("—");

                if (parts.length >= 2) {

                    String[] details = parts[1].split("\\|");

                    if (details.length >= 1) {
                        cuisine = details[0].trim();
                    }

                    if (details.length >= 2) {
                        rating = details[1].replace("★", "").trim();
                    }

                    if (details.length >= 3) {
                        deliveryTime = details[2].trim();
                    }

                    if (details.length >= 4) {
                        price = details[3].trim();
                    }
                }

                if (!restaurantName.isEmpty()
                        && !restaurantId.isEmpty()
                        && !addedIds.contains(restaurantId)) {

                    restaurantList.add(
                            new Restaurant(
                                    restaurantName,
                                    cuisine,
                                    rating,
                                    deliveryTime,
                                    price,
                                    restaurantId,
                                    ""
                            )
                    );

                    addedIds.add(restaurantId);

                    Log.d(TAG, "TEXT RESTAURANT ADDED: " + restaurantName
                            + " | ID = " + restaurantId);
                }

            } catch (Exception e) {

                Log.e(TAG, "Text parsing error", e);
            }
        }
    }

    private String extractRestaurantIdFromText(String line) {

        if (line == null) {
            return "";
        }

        String[] patterns = {
                "\\(ID\\s*:\\s*([0-9]+)\\)",
                "restaurantId\\s*[:=]\\s*[\"']?([0-9]+)",
                "restaurant_id\\s*[:=]\\s*[\"']?([0-9]+)",
                "restId\\s*[:=]\\s*[\"']?([0-9]+)",
                "rest_id\\s*[:=]\\s*[\"']?([0-9]+)",
                "\"id\"\\s*:\\s*\"?([0-9]+)\"?"
        };

        for (String pattern : patterns) {

            Matcher matcher = Pattern
                    .compile(pattern, Pattern.CASE_INSENSITIVE)
                    .matcher(line);

            if (matcher.find()) {

                String id = matcher.group(1);

                if (isValidRestaurantId(id)) {
                    return id;
                }
            }
        }

        return "";
    }

    private String extractRestaurantNameFromText(String line) {

        if (line == null) {
            return "";
        }

        String name = line;

        // Remove list number
        name = name.replaceFirst("^\\d+\\.\\s*", "");

        // Remove (ID: ...)
        name = name.replaceAll("\\(ID\\s*:\\s*[^)]*\\)", "");

        // Remove markdown bold
        name = name.replace("**", "");

        // Usually: Restaurant Name — Cuisine | Rating
        if (name.contains("—")) {
            name = name.substring(0, name.indexOf("—"));
        }

        int restaurantIdIndex = name.indexOf("restaurantId");

        if (restaurantIdIndex >= 0) {
            name = name.substring(0, restaurantIdIndex);
        }

        return name.trim();
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        searchFood = null;
        restaurantRecyclerView = null;
    }
}