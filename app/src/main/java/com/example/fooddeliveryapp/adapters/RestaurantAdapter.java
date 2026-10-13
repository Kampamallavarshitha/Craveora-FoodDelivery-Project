package com.example.fooddeliveryapp.adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.model.Restaurant;

import java.util.List;

public class RestaurantAdapter
        extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private static final String TAG = "RestaurantAdapter";

    private final List<Restaurant> restaurantList;
    private final OnRestaurantClickListener listener;

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    public RestaurantAdapter(
            List<Restaurant> restaurantList,
            OnRestaurantClickListener listener
    ) {
        this.restaurantList = restaurantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Restaurant restaurant = restaurantList.get(position);

        holder.txtRestaurantName.setText(restaurant.getName());
        holder.txtCuisine.setText(restaurant.getCuisine());

        holder.txtRestaurantInfo.setText(
                "⭐ " + restaurant.getRating()
                        + " • "
                        + restaurant.getDeliveryTime()
        );

        String imageUrl = lighten(restaurant.getImageUrl());

        Log.d(TAG, restaurant.getName() + " | IMAGE URL = " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {

            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.res)
                    .error(R.drawable.res)
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {

                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                Target<Drawable> target,
                                boolean isFirstResource
                        ) {
                            Log.e(TAG, "IMAGE LOAD FAILED: " + model, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                Drawable resource,
                                Object model,
                                Target<Drawable> target,
                                DataSource dataSource,
                                boolean isFirstResource
                        ) {
                            return false;
                        }
                    })
                    .into(holder.imgRestaurant);

        } else {

            Glide.with(holder.itemView.getContext()).clear(holder.imgRestaurant);
            holder.imgRestaurant.setImageResource(R.drawable.res);
        }

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onRestaurantClick(restaurant);
            }
        });
    }

    /**
     * Swiggy's raw upload URLs can be several MB. Adding a transformation
     * after /upload/ makes them small and fast to load.
     */
    private String lighten(String url) {

        if (url == null || url.isEmpty()) return url;

        String marker = "/image/upload/";

        if (url.contains("media-assets.swiggy.com")
                && url.contains(marker)
                && !url.contains("w_")) {

            return url.replace(marker, marker + "fl_lossy,f_auto,q_auto,w_660/");
        }

        return url;
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgRestaurant;

        TextView txtRestaurantName;
        TextView txtCuisine;
        TextView txtRestaurantInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgRestaurant = itemView.findViewById(R.id.imgRestaurant);
            txtRestaurantName = itemView.findViewById(R.id.txtRestaurantName);
            txtCuisine = itemView.findViewById(R.id.txtCuisine);
            txtRestaurantInfo = itemView.findViewById(R.id.txtRestaurantInfo);
        }
    }
}