package com.example.fooddeliveryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreFragment extends Fragment {

    private static final String TAG = "ExploreFragment";

    private EditText exploreSearch;
    private RecyclerView recycler;
    private View progress;
    private TextView empty;

    private RestaurantAdapter adapter;
    private final List<Restaurant> list = new ArrayList<>();

    private SwiggyApiService apiService;
    private String pendingQuery = null;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        exploreSearch = view.findViewById(R.id.exploreSearch);
        recycler = view.findViewById(R.id.exploreRecycler);
        progress = view.findViewById(R.id.exploreProgress);
        empty = view.findViewById(R.id.exploreEmpty);

        UiUtils.applyTopInset(view.findViewById(R.id.headerContainer));

        apiService = ApiClient.getClient().create(SwiggyApiService.class);

        // ---------------- LIST ----------------

        adapter = new RestaurantAdapter(list, restaurant -> {

            String id = restaurant.getRestaurantId();
            String name = restaurant.getName();

            if (id == null || id.trim().isEmpty() || id.equalsIgnoreCase("null")) {
                Toast.makeText(requireContext(), "Restaurant ID not found", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(requireContext(), RestaurantMenuActivity.class);
            intent.putExtra("RESTAURANT_ID", id);
            intent.putExtra("RESTAURANT_NAME", name);
            intent.putExtra("ADDRESS_ID", AppData.ADDRESS_ID);
            startActivity(intent);

            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slow);
        });

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        // The restaurant card is a fixed-width horizontal card (Home).
        // Here we stretch it to full width so it works as a vertical list.
        recycler.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View v) {
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (lp != null) {
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    if (lp instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) lp).bottomMargin = (int) UiUtils.dp(v, 16);
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View v) {
            }
        });

        // ---------------- TRENDING CHIPS ----------------

        LinearLayout row = view.findViewById(R.id.trendingRow);
        String[] trending = {"Biryani", "Pizza", "Burger", "Dosa", "Chinese", "Rolls", "Ice Cream", "Coffee"};

        for (String t : trending) {

            TextView chip = (TextView) inflater.inflate(R.layout.item_chip, row, false);
            chip.setText(t);
            UiUtils.attachPress(chip);

            chip.setOnClickListener(c -> {
                exploreSearch.setText(t);
                exploreSearch.setSelection(t.length());
                runSearch(t.toLowerCase());
            });

            row.addView(chip);
        }

        // ---------------- SEARCH BOX ----------------

        exploreSearch.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                String q = exploreSearch.getText().toString().trim();

                if (q.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter food or restaurant name", Toast.LENGTH_SHORT).show();
                } else {
                    runSearch(q);
                }

                return true;
            }

            return false;
        });

        UiUtils.staggerIn(view.findViewById(R.id.headerContainer));

        applyPending();

        return view;
    }

    /** Called from MainActivity / Categories. */
    public void search(String query) {
        pendingQuery = query;
        if (getView() != null) applyPending();
    }

    private void applyPending() {

        if (pendingQuery == null || exploreSearch == null) return;

        String q = pendingQuery;
        pendingQuery = null;

        exploreSearch.setText(q);
        exploreSearch.setSelection(q.length());

        runSearch(q);
    }

    private void runSearch(String query) {

        hideKeyboard();

        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);

        list.clear();
        adapter.notifyDataSetChanged();

        Map<String, Object> args = new HashMap<>();
        args.put("query", query);
        args.put("addressId", AppData.ADDRESS_ID);

        apiService
                .callSwiggyTool(AppData.USER_ID, new SwiggyRequest("search_restaurants", args))
                .enqueue(new Callback<SwiggyResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<SwiggyResponse> call,
                            @NonNull Response<SwiggyResponse> response
                    ) {

                        if (!isAdded() || getView() == null) return;

                        progress.setVisibility(View.GONE);

                        if (!response.isSuccessful() || response.body() == null) {
                            showEmpty("Server error: " + response.code());
                            return;
                        }

                        SwiggyResponse body = response.body();

                        if (!body.isSuccess() || body.getData() == null || body.getData().isEmpty()) {
                            showEmpty("No restaurants found for \"" + query + "\" 😕");
                            return;
                        }

                        StringBuilder sb = new StringBuilder();

                        for (SwiggyResponse.SwiggyData d : body.getData()) {
                            if (d == null) continue;
                            String text = d.getText();
                            if (text != null && !text.trim().isEmpty()) {
                                if (sb.length() > 0) sb.append("\n");
                                sb.append(text);
                            }
                        }

                        List<Restaurant> parsed = RestaurantParser.parse(sb.toString().trim());

                        Log.d(TAG, "Explore results = " + parsed.size());

                        list.clear();
                        list.addAll(parsed);
                        adapter.notifyDataSetChanged();
                        recycler.scheduleLayoutAnimation();

                        if (list.isEmpty()) {
                            showEmpty("No restaurants found for \"" + query + "\" 😕");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SwiggyResponse> call, @NonNull Throwable t) {

                        if (!isAdded() || getView() == null) return;

                        progress.setVisibility(View.GONE);
                        showEmpty("Cannot connect: " + t.getMessage());
                    }
                });
    }

    private void showEmpty(String message) {
        empty.setText(message);
        empty.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && exploreSearch != null) {
                imm.hideSoftInputFromWindow(exploreSearch.getWindowToken(), 0);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        exploreSearch = null;
        recycler = null;
        progress = null;
        empty = null;
    }
}