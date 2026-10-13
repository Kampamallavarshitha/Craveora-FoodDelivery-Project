package com.example.fooddeliveryapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recycler;
    private View empty;

    private OrderAdapter adapter;
    private final List<OrderStore.Order> orders = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    // refresh the tracking status every 15 seconds while this tab is visible
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) adapter.notifyDataSetChanged();
            handler.postDelayed(this, 15000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        recycler = view.findViewById(R.id.ordersRecycler);
        empty = view.findViewById(R.id.ordersEmpty);

        UiUtils.applyTopInset(view.findViewById(R.id.headerContainer));

        adapter = new OrderAdapter(orders);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        UiUtils.staggerIn(view.findViewById(R.id.headerContainer));

        return view;
    }

    private void load() {

        if (recycler == null) return;

        orders.clear();
        orders.addAll(OrderStore.getAll(requireContext()));
        adapter.notifyDataSetChanged();
        recycler.scheduleLayoutAnimation();

        empty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
        recycler.setVisibility(orders.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void startTicker() {
        load();
        handler.removeCallbacks(ticker);
        handler.postDelayed(ticker, 15000);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) startTicker();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            handler.removeCallbacks(ticker);
        } else {
            startTicker();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(ticker);
        recycler = null;
        empty = null;
    }
}