package com.example.fooddeliveryapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        UiUtils.applyTopInset(view.findViewById(R.id.headerContainer));

        List<CategoryAdapter.Category> list = new ArrayList<>();
        list.add(new CategoryAdapter.Category("🍗", "Biryani", "biryani", "#FFE8D6"));
        list.add(new CategoryAdapter.Category("🍕", "Pizza", "pizza", "#FFF1C9"));
        list.add(new CategoryAdapter.Category("🍔", "Burger", "burger", "#FFE0E0"));
        list.add(new CategoryAdapter.Category("🥞", "Dosa", "dosa", "#E3F4D9"));
        list.add(new CategoryAdapter.Category("🥡", "Chinese", "chinese", "#E0ECFF"));
        list.add(new CategoryAdapter.Category("🍰", "Desserts", "desserts", "#F3E1FF"));
        list.add(new CategoryAdapter.Category("🌯", "Rolls", "rolls", "#FFE8D6"));
        list.add(new CategoryAdapter.Category("🥗", "Healthy", "healthy", "#E3F4D9"));
        list.add(new CategoryAdapter.Category("🍦", "Ice Cream", "ice cream", "#E0ECFF"));
        list.add(new CategoryAdapter.Category("☕", "Coffee", "coffee", "#FFF1C9"));
        list.add(new CategoryAdapter.Category("🍜", "Noodles", "noodles", "#FFE0E0"));
        list.add(new CategoryAdapter.Category("🥪", "Sandwich", "sandwich", "#F3E1FF"));

        RecyclerView recycler = view.findViewById(R.id.categoriesRecycler);
        recycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recycler.setAdapter(new CategoryAdapter(list, category -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openExplore(category.query);
            }
        }));

        UiUtils.staggerIn(view.findViewById(R.id.headerContainer));

        return view;
    }
}