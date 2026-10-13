package com.example.fooddeliveryapp.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.CartManager;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.model.MenuItem;
import com.example.fooddeliveryapp.model.MenuListEntry;

import java.util.List;
import java.util.Locale;

public class MenuAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface CartListener {
        void onCartChanged();
    }

    private static final int GREEN = Color.parseColor("#0F8A3C");
    private static final int RED = Color.parseColor("#D32F2F");

    private final List<MenuListEntry> menuList;
    private final String restaurantId;
    private final String restaurantName;
    private final CartListener cartListener;

    public MenuAdapter(
            List<MenuListEntry> menuList,
            String restaurantId,
            String restaurantName,
            CartListener cartListener
    ) {
        this.menuList = menuList;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.cartListener = cartListener;
    }

    @Override
    public int getItemViewType(int position) {
        return menuList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == MenuListEntry.TYPE_HEADER) {
            return new HeaderViewHolder(
                    inflater.inflate(R.layout.item_menu_category, parent, false)
            );
        }

        return new ItemViewHolder(
                inflater.inflate(R.layout.item_menu_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {

        MenuListEntry entry = menuList.get(position);

        if (entry.getType() == MenuListEntry.TYPE_HEADER) {

            ((HeaderViewHolder) holder).txtCategory.setText(entry.getHeaderTitle());
            return;
        }

        ItemViewHolder h = (ItemViewHolder) holder;
        MenuItem item = entry.getMenuItem();

        h.txtItemName.setText(item.getName());

        if (item.getPrice() > 0) {
            h.txtItemPrice.setText(
                    String.format(Locale.getDefault(), "₹%d", item.getPrice())
            );
        } else {
            h.txtItemPrice.setText("Price unavailable");
        }

        // veg / non-veg marker
        if (item.getVegType() == MenuItem.VEG_UNKNOWN) {
            h.txtVeg.setVisibility(View.GONE);
        } else {
            int color = item.getVegType() == MenuItem.VEG ? GREEN : RED;
            h.txtVeg.setVisibility(View.VISIBLE);
            h.txtVeg.setTextColor(color);
            h.txtVeg.setBackground(vegBackground(h.itemView, color));
        }

        h.txtBestseller.setVisibility(item.isBestseller() ? View.VISIBLE : View.GONE);
        h.txtCustomisable.setVisibility(item.isCustomisable() ? View.VISIBLE : View.GONE);

        if (!item.getDescription().trim().isEmpty()) {
            h.txtItemDescription.setText(item.getDescription());
            h.txtItemDescription.setVisibility(View.VISIBLE);
        } else {
            h.txtItemDescription.setVisibility(View.GONE);
        }

        String imageUrl = item.getFullImageUrl();

        if (!imageUrl.isEmpty()) {
            Glide.with(h.imgItem.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.res)
                    .error(R.drawable.res)
                    .centerCrop()
                    .into(h.imgItem);
        } else {
            Glide.with(h.imgItem.getContext()).clear(h.imgItem);
            h.imgItem.setImageResource(R.drawable.res);
        }

        bindCartControls(h, item);
    }

    private GradientDrawable vegBackground(View view, int color) {

        float density = view.getResources().getDisplayMetrics().density;

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(3 * density);
        bg.setStroke(Math.round(1.5f * density), color);
        bg.setColor(Color.WHITE);

        return bg;
    }

    private void bindCartControls(ItemViewHolder h, MenuItem item) {

        CartManager cart = CartManager.getInstance();
        int qty = cart.getQuantity(item.getId());

        if (qty > 0) {
            h.btnAdd.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);
            h.txtQty.setText(String.valueOf(qty));
        } else {
            h.btnAdd.setVisibility(View.VISIBLE);
            h.layoutQty.setVisibility(View.GONE);
        }

        h.btnAdd.setEnabled(item.getPrice() > 0);

        h.btnAdd.setOnClickListener(v -> {
            cart.add(restaurantId, restaurantName, item);
            bindCartControls(h, item);
            cartListener.onCartChanged();
        });

        h.btnPlus.setOnClickListener(v -> {
            cart.add(restaurantId, restaurantName, item);
            bindCartControls(h, item);
            cartListener.onCartChanged();
        });

        h.btnMinus.setOnClickListener(v -> {
            cart.remove(item);
            bindCartControls(h, item);
            cartListener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView txtCategory;

        HeaderViewHolder(View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView txtItemName, txtItemPrice, txtItemDescription;
        TextView txtVeg, txtBestseller, txtCustomisable;
        TextView btnAdd, btnMinus, btnPlus, txtQty;
        LinearLayout layoutQty;
        ImageView imgItem;

        ItemViewHolder(View itemView) {
            super(itemView);
            txtItemName = itemView.findViewById(R.id.txtItemName);
            txtItemPrice = itemView.findViewById(R.id.txtItemPrice);
            txtItemDescription = itemView.findViewById(R.id.txtItemDescription);
            txtVeg = itemView.findViewById(R.id.txtVeg);
            txtBestseller = itemView.findViewById(R.id.txtBestseller);
            txtCustomisable = itemView.findViewById(R.id.txtCustomisable);
            imgItem = itemView.findViewById(R.id.imgItem);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            txtQty = itemView.findViewById(R.id.txtQty);
            layoutQty = itemView.findViewById(R.id.layoutQty);
        }
    }
}