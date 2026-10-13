package com.example.fooddeliveryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddeliveryapp.adapters.MenuAdapter;
import com.example.fooddeliveryapp.model.MenuItem;
import com.example.fooddeliveryapp.model.MenuListEntry;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantMenuActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantMenu";
    private static final String USER_ID = "test-user-001";

    // Swiggy MCP tool that returns a restaurant's menu
    private static final String MENU_TOOL = "get_restaurant_menu";

    // Safety limit when following "Use page=N for more"
    private static final int MAX_PAGES = 25;

    private static final Pattern NEXT_PAGE =
            Pattern.compile("Use\\s+page\\s*=\\s*(\\d+)\\s+for\\s+more", Pattern.CASE_INSENSITIVE);

    private static final String[] ID_KEYS = {
            "id", "itemId", "item_id", "menuItemId", "menu_item_id", "dishId", "dish_id"
    };
    private static final String[] NAME_KEYS = {
            "name", "itemName", "item_name", "dishName", "title"
    };
    private static final String[] PAISE_KEYS = {
            "priceInPaisa", "price_in_paisa", "priceInPaise"
    };
    private static final String[] PRICE_KEYS = {
            "price", "finalPrice", "final_price", "defaultPrice", "default_price",
            "priceInRupees", "mrp", "cost"
    };
    private static final String[] DESC_KEYS = {
            "description", "itemDescription", "item_description", "desc"
    };
    private static final String[] IMAGE_KEYS = {
            "imageUrl", "image_url", "image", "imageId", "image_id",
            "cloudinaryImageId", "cloudinary_image_id"
    };
    private static final String[] CATEGORY_KEYS = {
            "categoryName", "category_name", "category", "section", "sectionName", "title", "name"
    };

    private String restaurantId;
    private String restaurantName;
    private String addressId;

    private RecyclerView menuRecyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private View cartBar;
    private TextView txtCartInfo;

    private final List<MenuListEntry> menuList = new ArrayList<>();
    private MenuAdapter menuAdapter;

    private SwiggyApiService apiService;

    // parsing state (kept across pages)
    private final Set<String> seenItemIds = new HashSet<>();
    private String lastHeader = null;

    // UI: used to bounce the cart text when the count changes
    private int lastCartCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI: draw behind status bar / nav bar (insets are applied below)
        UiUtils.enableEdgeToEdge(this);

        setContentView(R.layout.activity_restaurant_menu);

        restaurantId = getIntent().getStringExtra("RESTAURANT_ID");
        restaurantName = getIntent().getStringExtra("RESTAURANT_NAME");
        addressId = getIntent().getStringExtra("ADDRESS_ID");

        if (restaurantName == null || restaurantName.equalsIgnoreCase("null")) {
            restaurantName = "";
        }

        ((TextView) findViewById(R.id.txtRestaurantName)).setText(
                restaurantName.isEmpty() ? "Restaurant Menu" : restaurantName
        );

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        txtEmpty = findViewById(R.id.txtEmpty);
        cartBar = findViewById(R.id.cartBar);
        txtCartInfo = findViewById(R.id.txtCartInfo);

        // UI: safe area + touch feedback
        UiUtils.applyTopInset(findViewById(R.id.headerContainer));
        UiUtils.applyBottomMargin(cartBar);
        UiUtils.attachPress(findViewById(R.id.btnBack));
        UiUtils.attachPress(cartBar);

        menuAdapter = new MenuAdapter(
                menuList,
                restaurantId == null ? "" : restaurantId,
                restaurantName,
                this::updateCartBar
        );

        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuRecyclerView.setAdapter(menuAdapter);

        cartBar.setOnClickListener(v -> showCartDialog());

        updateCartBar();

        if (isBlank(restaurantId)) {
            showError("Restaurant ID not found");
            return;
        }

        if (isBlank(addressId)) {
            showError("Address ID not found");
            return;
        }

        apiService = ApiClient.getClient().create(SwiggyApiService.class);

        loadMenu(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (menuAdapter != null) menuAdapter.notifyDataSetChanged();
        updateCartBar();
    }

    // UI: smooth slide-out transition when leaving this screen
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in_slow, R.anim.slide_out_right);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null");
    }

    // ============================================================
    // LOAD MENU (page by page)
    // ============================================================

    private void loadMenu(final int page) {

        if (page == 1) {
            progressBar.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.GONE);
        }

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("restaurantId", restaurantId);
        arguments.put("addressId", addressId);

        if (page > 1) {
            arguments.put("page", page);
        }

        Log.d(TAG, "CALLING " + MENU_TOOL + " | ARGS = " + arguments);

        apiService
                .callSwiggyTool(USER_ID, new SwiggyRequest(MENU_TOOL, arguments))
                .enqueue(new Callback<SwiggyResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<SwiggyResponse> call,
                            @NonNull Response<SwiggyResponse> response
                    ) {

                        progressBar.setVisibility(View.GONE);

                        if (!response.isSuccessful() || response.body() == null) {

                            Log.e(TAG, "Server error " + response.code() + " on page " + page);

                            if (page == 1) {
                                showError("Server Error: " + response.code());
                            }

                            return;
                        }

                        SwiggyResponse body = response.body();

                        if (!body.isSuccess()
                                || body.getData() == null
                                || body.getData().isEmpty()) {

                            if (page == 1) showError("No menu available");

                            return;
                        }

                        String raw = extractResultText(body);

                        Log.d(TAG, "RAW MENU (page " + page + "):\n" + raw);

                        if (raw.isEmpty()) {
                            if (page == 1) showError("Empty menu received");
                            return;
                        }

                        parseMenu(raw, page);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<SwiggyResponse> call,
                            @NonNull Throwable t
                    ) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Menu request failed", t);

                        if (page == 1) {
                            showError("Cannot connect to backend: " + t.getMessage());
                        }
                    }
                });
    }

    private void showError(String message) {
        txtEmpty.setText(message);
        txtEmpty.setVisibility(View.VISIBLE);
    }

    private String extractResultText(SwiggyResponse response) {

        StringBuilder builder = new StringBuilder();

        for (SwiggyResponse.SwiggyData data : response.getData()) {

            if (data == null) continue;

            String text = data.getText();

            if (text != null && !text.trim().isEmpty()) {
                if (builder.length() > 0) builder.append("\n");
                builder.append(text);
            }
        }

        return builder.toString().trim();
    }

    // ============================================================
    // PARSE MENU
    // ============================================================

    private void parseMenu(String raw, int page) {

        if (page == 1) {
            menuList.clear();
            seenItemIds.clear();
            lastHeader = null;
        }

        int before = menuList.size();

        // JSON first, unless the response clearly is the text format
        boolean looksLikeText = raw.contains("(ID:") || raw.contains("## ");

        if (!looksLikeText) {

            try {

                String jsonText = cleanJsonText(raw);

                if (!jsonText.isEmpty()) {

                    // Read only the first JSON value, ignore trailing text
                    JsonReader reader = new JsonReader(new StringReader(jsonText));
                    reader.setLenient(true);
                    JsonElement root = JsonParser.parseReader(reader);

                    walk(root, null, 0);
                }

            } catch (Exception e) {
                Log.e(TAG, "Menu JSON parsing failed", e);
            }
        }

        if (menuList.size() == before) {
            parseTextMenu(raw);
        }

        menuAdapter.notifyDataSetChanged();

        // UI: slide-in animation for the first page of items
        if (page == 1) {
            menuRecyclerView.scheduleLayoutAnimation();
        }

        Log.d(TAG, "Menu entries = " + menuList.size());

        if (menuList.isEmpty()) {
            showError("Unable to load menu");
        } else {
            txtEmpty.setVisibility(View.GONE);
        }

        // follow "Use page=N for more"
        Matcher next = NEXT_PAGE.matcher(raw);

        if (next.find()) {

            try {

                int nextPage = Integer.parseInt(next.group(1));

                if (nextPage > page && nextPage <= MAX_PAGES) {
                    loadMenu(nextPage);
                }

            } catch (NumberFormatException ignored) {
            }
        }
    }

    private String cleanJsonText(String result) {

        if (result == null) return "";

        String text = result.trim();

        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?", "");
            text = text.replaceFirst("```\\s*$", "");
            text = text.trim();
        }

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

        if (start > 0) text = text.substring(start);

        return text.trim();
    }

    // ============================================================
    // JSON WALKER
    // ============================================================

    private void walk(JsonElement element, String category, int depth) {

        if (element == null || element.isJsonNull()) return;

        if (element.isJsonArray()) {

            JsonArray array = element.getAsJsonArray();

            for (JsonElement child : array) {
                walk(child, category, depth);
            }

            return;
        }

        if (!element.isJsonObject()) return;

        JsonObject object = element.getAsJsonObject();

        if (looksLikeMenuItem(object)) {
            addItem(object, category);
            return;
        }

        String newCategory = category;

        if (depth > 0) {
            String title = firstString(object, CATEGORY_KEYS);
            if (!title.isEmpty()) newCategory = title;
        }

        for (String key : object.keySet()) {

            JsonElement child = object.get(key);

            if (child != null && (child.isJsonArray() || child.isJsonObject())) {
                walk(child, newCategory, depth + 1);
            }
        }
    }

    private boolean looksLikeMenuItem(JsonObject object) {

        if (firstString(object, NAME_KEYS).isEmpty()) return false;

        boolean hasPrice = !firstString(object, PRICE_KEYS).isEmpty()
                || !firstString(object, PAISE_KEYS).isEmpty();
        boolean hasDescription = !firstString(object, DESC_KEYS).isEmpty();
        boolean hasImage = !firstImage(object).isEmpty();

        return hasPrice || hasDescription || hasImage;
    }

    private void addItem(JsonObject object, String category) {

        String name = firstString(object, NAME_KEYS);
        String id = firstString(object, ID_KEYS);

        if (id.isEmpty()) id = name;

        if (!seenItemIds.add(id)) return;

        int price = readPrice(object);

        String description = firstString(object, DESC_KEYS);
        String image = firstImage(object);

        int vegType = MenuItem.VEG_UNKNOWN;

        String vegClassifier = firstString(object, "vegClassifier", "veg_classifier");

        if (vegClassifier.equalsIgnoreCase("VEG")) {
            vegType = MenuItem.VEG;
        } else if (vegClassifier.toLowerCase(Locale.ROOT).contains("non")) {
            vegType = MenuItem.NON_VEG;
        } else if (object.has("isVeg") || object.has("is_veg")) {
            vegType = flag(object, "isVeg", "is_veg") ? MenuItem.VEG : MenuItem.NON_VEG;
        }

        boolean bestseller = flag(object, "isBestseller", "is_bestseller", "bestseller");
        boolean customisable = flag(object, "hasAddons", "has_addons",
                "hasVariants", "has_variants", "customisable", "customizable");

        String header = category;
        String ownCategory = firstString(
                object, "category", "categoryName", "category_name", "section", "sectionName");
        if (!ownCategory.isEmpty()) header = ownCategory;
        if (header == null || header.isEmpty()) header = "Menu";

        addEntry(header, new MenuItem(
                id, name, price, description, image, vegType, bestseller, customisable));
    }

    private boolean flag(JsonObject object, String... keys) {

        String v = firstString(object, keys);

        return v.equalsIgnoreCase("true") || v.equals("1");
    }

    private void addEntry(String header, MenuItem item) {

        if (!header.equals(lastHeader)) {
            menuList.add(MenuListEntry.header(header));
            lastHeader = header;
        }

        menuList.add(MenuListEntry.item(item));
    }

    private String firstImage(JsonObject object) {

        String direct = firstString(object, IMAGE_KEYS);

        if (!direct.isEmpty()) return direct;

        String[] containerKeys = {
                "images", "imageUrls", "image_urls", "imageIds", "image_ids",
                "image", "imageInfo", "media"
        };

        for (String key : containerKeys) {

            if (!object.has(key) || object.get(key).isJsonNull()) continue;

            JsonElement el = object.get(key);

            if (el.isJsonArray()) {

                for (JsonElement child : el.getAsJsonArray()) {

                    if (child.isJsonPrimitive()) {
                        String v = child.getAsString().trim();
                        if (!v.isEmpty()) return v;
                    } else if (child.isJsonObject()) {
                        String v = firstString(child.getAsJsonObject(),
                                "url", "imageUrl", "src", "id", "imageId");
                        if (!v.isEmpty()) return v;
                    }
                }

            } else if (el.isJsonObject()) {

                String v = firstString(el.getAsJsonObject(),
                        "url", "imageUrl", "src", "id", "imageId");
                if (!v.isEmpty()) return v;
            }
        }

        return scanForImage(object, 0);
    }

    private String scanForImage(JsonElement el, int depth) {

        if (el == null || el.isJsonNull() || depth > 4) return "";

        if (el.isJsonPrimitive()) {

            String v = el.getAsString().trim();
            String lower = v.toLowerCase(Locale.ROOT);

            boolean looksLikeImage =
                    lower.contains("media-assets.swiggy.com")
                            || lower.contains("cloudinary")
                            || lower.matches(".*\\.(jpg|jpeg|png|webp)(\\?.*)?$")
                            || lower.startsWith("food_catalog/")
                            || lower.startsWith("swiggy/image/");

            return looksLikeImage ? v : "";
        }

        if (el.isJsonArray()) {

            for (JsonElement child : el.getAsJsonArray()) {
                String v = scanForImage(child, depth + 1);
                if (!v.isEmpty()) return v;
            }

            return "";
        }

        JsonObject obj = el.getAsJsonObject();

        for (String key : obj.keySet()) {

            String v = scanForImage(obj.get(key), depth + 1);
            if (!v.isEmpty()) return v;
        }

        return "";
    }

    private int readPrice(JsonObject object) {

        String paise = firstString(object, PAISE_KEYS);

        if (!paise.isEmpty()) {
            return parsePriceValue(paise) / 100;
        }

        int number = parsePriceValue(firstString(object, PRICE_KEYS));

        if (number > 10000) number = number / 100;

        return number;
    }

    private int parsePriceValue(String value) {

        if (value == null) return 0;

        String cleaned = value.replaceAll("[^0-9.]", "");

        if (cleaned.isEmpty()) return 0;

        try {
            return (int) Math.round(Double.parseDouble(cleaned));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String firstString(JsonObject object, String... keys) {

        if (object == null) return "";

        for (String key : keys) {

            try {

                if (object.has(key)
                        && !object.get(key).isJsonNull()
                        && object.get(key).isJsonPrimitive()) {

                    String value = object.get(key).getAsString().trim();

                    if (!value.isEmpty()) return value;
                }

            } catch (Exception ignored) {
            }
        }

        return "";
    }

    // ============================================================
    // TEXT PARSER
    // Format handled:
    //   ## Recommended
    //     - Chicken Biryani Single — ₹170 | Non-veg, Bestseller, has addons [image: https://...] (ID: 184892466)
    // ============================================================

    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(?:₹|Rs\\.?|INR)\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ID_PATTERN = Pattern.compile(
            "\\(?\\bID\\s*:\\s*([A-Za-z0-9_-]+)\\)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[^\\s)\\]\"'<>]+");

    private static final Pattern IMAGE_BRACKET =
            Pattern.compile("\\[\\s*image\\s*:[^\\]]*\\]", Pattern.CASE_INSENSITIVE);

    private static final Pattern IMAGE_FIELD = Pattern.compile(
            "(?:image(?:Id|_id|Url|_url)?|img|photo)\\s*[:=]\\s*[\"']?([^\\s\"',)\\]]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NON_VEG_TAG =
            Pattern.compile("\\bnon[- ]?veg(?:etarian)?\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern VEG_TAG =
            Pattern.compile("\\bveg(?:etarian)?\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern BESTSELLER_TAG =
            Pattern.compile("\\bbest\\s?seller\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern CUSTOM_TAG = Pattern.compile(
            "\\bhas\\s+(?:add-?ons|variants|options)\\b|\\bcustomi[sz]able\\b",
            Pattern.CASE_INSENSITIVE
    );

    private void parseTextMenu(String raw) {

        String currentCategory = "Menu";
        int lastItemIndex = -1;

        for (String originalLine : raw.split("\\r?\\n")) {

            String line = originalLine.trim();

            if (line.isEmpty()) continue;

            if (line.startsWith("{") || line.startsWith("\"")) continue;

            String image = findImage(line);

            Matcher priceMatcher = PRICE_PATTERN.matcher(line);

            if (!priceMatcher.find()) {

                // image on its own line right after an item
                if (!image.isEmpty() && lastItemIndex >= 0) {

                    MenuItem old = menuList.get(lastItemIndex).getMenuItem();

                    if (old.getFullImageUrl().isEmpty()) {
                        menuList.set(lastItemIndex, MenuListEntry.item(old.withImage(image)));
                    }

                    continue;
                }

                String header = line
                        .replace("**", "")
                        .replaceFirst("^#+", "")
                        .replaceAll(":$", "")
                        .trim();

                boolean looksLikeHeader = originalLine.contains("**")
                        || line.startsWith("#")
                        || line.endsWith(":");

                if (looksLikeHeader && !header.isEmpty()) {
                    currentCategory = header;
                }

                continue;
            }

            int price = parsePriceValue(priceMatcher.group(1));

            String id = "";
            Matcher idMatcher = ID_PATTERN.matcher(line);
            if (idMatcher.find()) id = idMatcher.group(1);

            // ---- name ----
            String name = line.substring(0, priceMatcher.start());
            name = IMAGE_BRACKET.matcher(name).replaceAll("");
            name = URL_PATTERN.matcher(name).replaceAll("");
            name = ID_PATTERN.matcher(name).replaceAll("");
            name = name.replaceFirst("^\\s*(?:[-*•]\\s*|\\d+[.)]\\s+)", "")
                    .replace("**", "")
                    .replaceAll("[\\s—\\-–:|]+$", "")
                    .trim();

            if (name.isEmpty()) continue;

            // ---- everything after the price: tags + description ----
            String rest = line.substring(priceMatcher.end());
            rest = IMAGE_BRACKET.matcher(rest).replaceAll("");
            rest = URL_PATTERN.matcher(rest).replaceAll("");
            rest = ID_PATTERN.matcher(rest).replaceAll("");
            rest = rest.replace("**", "");

            int vegType = MenuItem.VEG_UNKNOWN;

            if (NON_VEG_TAG.matcher(rest).find()) {
                vegType = MenuItem.NON_VEG;
            } else if (VEG_TAG.matcher(rest).find()) {
                vegType = MenuItem.VEG;
            }

            boolean bestseller = BESTSELLER_TAG.matcher(rest).find();
            boolean customisable = CUSTOM_TAG.matcher(rest).find();

            rest = NON_VEG_TAG.matcher(rest).replaceAll("");
            rest = VEG_TAG.matcher(rest).replaceAll("");
            rest = BESTSELLER_TAG.matcher(rest).replaceAll("");
            rest = CUSTOM_TAG.matcher(rest).replaceAll("");

            // what is left (if anything) is a real description
            String description = rest
                    .replaceAll("^[\\s—\\-–:|,;]+", "")
                    .replaceAll("[\\s—\\-–:|,;]+$", "")
                    .replaceAll("\\s*[|,;]\\s*[|,;]+\\s*", ", ")
                    .trim();

            if (description.length() < 3) description = "";

            if (id.isEmpty()) id = name;

            if (!seenItemIds.add(id)) continue;

            addEntry(currentCategory, new MenuItem(
                    id, name, price, description, image, vegType, bestseller, customisable));

            lastItemIndex = menuList.size() - 1;
        }
    }

    private String findImage(String line) {

        Matcher urls = URL_PATTERN.matcher(line);

        while (urls.find()) {

            String u = urls.group();
            String lower = u.toLowerCase(Locale.ROOT);

            if (lower.contains("swiggy") || lower.contains("cloudinary")
                    || lower.matches(".*\\.(jpg|jpeg|png|webp)(\\?.*)?$")) {
                return u;
            }
        }

        Matcher field = IMAGE_FIELD.matcher(line);

        if (field.find()) {

            String v = field.group(1);

            if (v != null && !v.equalsIgnoreCase("null") && !v.isEmpty()) return v;
        }

        return "";
    }

    // ============================================================
    // CART BAR + DIALOG
    // ============================================================

    private void updateCartBar() {

        CartManager cart = CartManager.getInstance();
        int count = cart.getTotalCount();

        if (count == 0) {
            UiUtils.slideDownHide(cartBar);
            lastCartCount = 0;
            return;
        }

        boolean wasHidden = cartBar.getVisibility() != View.VISIBLE;

        txtCartInfo.setText(String.format(
                Locale.getDefault(),
                "%d item%s  •  ₹%d",
                count,
                count == 1 ? "" : "s",
                cart.getTotalPrice()
        ));

        UiUtils.slideUpShow(cartBar);

        if (!wasHidden && count != lastCartCount) {
            UiUtils.pop(txtCartInfo);
        }

        lastCartCount = count;
    }

    private void showCartDialog() {

        CartManager cart = CartManager.getInstance();

        StringBuilder summary = new StringBuilder();

        for (CartManager.CartEntry e : cart.getEntries()) {
            summary.append(String.format(
                    Locale.getDefault(),
                    "%d x %s  -  ₹%d\n",
                    e.quantity,
                    e.item.getName(),
                    e.item.getPrice() * e.quantity
            ));
        }

        summary.append(String.format(
                Locale.getDefault(),
                "\nTotal: ₹%d",
                cart.getTotalPrice()
        ));

        new AlertDialog.Builder(this)
                .setTitle("Your cart - " + cart.getRestaurantName())
                .setMessage(summary.toString())
                .setPositiveButton("Place order", (d, w) -> {
                    // save to Orders tab (must happen BEFORE cart.clear())
                    OrderStore.place(this, cart);
                    cart.clear();
                    menuAdapter.notifyDataSetChanged();
                    updateCartBar();
                    Toast.makeText(this, "Order placed! Track it in the Orders tab 🎉",
                            Toast.LENGTH_LONG).show();
                })
                .setNeutralButton("Close", null)
                .setNegativeButton("Clear cart", (d, w) -> {
                    cart.clear();
                    menuAdapter.notifyDataSetChanged();
                    updateCartBar();
                })
                .show();
    }
}