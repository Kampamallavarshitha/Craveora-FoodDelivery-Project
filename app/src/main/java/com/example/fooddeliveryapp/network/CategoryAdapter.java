package com.example.fooddeliveryapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public static class Category {
        final String emoji;
        final String name;
        final String query;
        final int color;

        public Category(String emoji, String name, String query, String color) {
            this.emoji = emoji;
            this.name = name;
            this.query = query;
            this.color = Color.parseColor(color);
        }
    }

    public interface OnCategoryClick {
        void onClick(Category category);
    }

    private final List<Category> items;
    private final OnCategoryClick listener;

    public CategoryAdapter(List<Category> items, OnCategoryClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        Category c = items.get(position);

        h.emoji.setText(c.emoji);
        h.name.setText(c.name);
        ((CardView) h.itemView).setCardBackgroundColor(c.color);

        UiUtils.attachPress(h.itemView);
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView emoji;
        final TextView name;

        VH(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.txtCategoryEmoji);
            name = itemView.findViewById(R.id.txtCategoryName);
        }
    }
}