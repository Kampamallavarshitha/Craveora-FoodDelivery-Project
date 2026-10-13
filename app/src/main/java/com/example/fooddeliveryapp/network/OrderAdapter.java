package com.example.fooddeliveryapp;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    private final List<OrderStore.Order> orders;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public OrderAdapter(List<OrderStore.Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        OrderStore.Order o = orders.get(position);

        h.restaurant.setText(o.restaurant == null || o.restaurant.isEmpty() ? "Restaurant" : o.restaurant);

        h.status.setText(o.status());
        h.status.setBackgroundTintList(ColorStateList.valueOf(o.color()));

        h.progress.setProgress(o.progress());
        h.progress.setProgressTintList(ColorStateList.valueOf(o.color()));

        StringBuilder items = new StringBuilder();
        int max = Math.min(3, o.items.size());

        for (int i = 0; i < max; i++) {
            if (i > 0) items.append("\n");
            items.append(o.items.get(i));
        }

        if (o.items.size() > max) {
            items.append("\n+").append(o.items.size() - max).append(" more");
        }

        h.items.setText(items.toString());

        h.meta.setText("#" + o.id + "  •  " + fmt.format(new Date(o.time)));
        h.total.setText("₹" + o.total);

        UiUtils.attachPress(h.itemView);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView restaurant, status, items, meta, total;
        final ProgressBar progress;

        VH(@NonNull View v) {
            super(v);
            restaurant = v.findViewById(R.id.txtOrderRestaurant);
            status = v.findViewById(R.id.txtOrderStatus);
            items = v.findViewById(R.id.txtOrderItems);
            meta = v.findViewById(R.id.txtOrderMeta);
            total = v.findViewById(R.id.txtOrderTotal);
            progress = v.findViewById(R.id.progressOrder);
        }
    }
}