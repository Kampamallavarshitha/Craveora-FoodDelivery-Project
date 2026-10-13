package com.example.fooddeliveryapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "home";
    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_EXPLORE = "explore";
    private static final String TAG_ORDERS = "orders";
    private static final String TAG_PROFILE = "profile";

    private static final String[] ALL_TAGS = {
            TAG_HOME, TAG_CATEGORIES, TAG_EXPLORE, TAG_ORDERS, TAG_PROFILE
    };

    private BottomNavigationView bottomNav;
    private String currentTag = TAG_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UiUtils.enableEdgeToEdge(this);

        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState != null) {
            currentTag = savedInstanceState.getString("tag", TAG_HOME);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            String tag = tagFor(item.getItemId());
            if (tag == null) return false;
            showTab(tag);
            return true;
        });

        showTab(currentTag);
        bottomNav.setSelectedItemId(idFor(currentTag));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tag", currentTag);
    }

    // ============================================================
    // TAB SWITCHING (fragments are kept alive: show / hide)
    // ============================================================

    private void showTab(String tag) {

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction t = fm.beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        for (String other : ALL_TAGS) {
            Fragment f = fm.findFragmentByTag(other);
            if (f != null && !other.equals(tag) && !f.isHidden()) {
                t.hide(f);
            }
        }

        Fragment target = fm.findFragmentByTag(tag);

        if (target == null) {
            t.add(R.id.frameLayout, createFragment(tag), tag);
        } else {
            t.show(target);
        }

        t.commitNow();

        currentTag = tag;
    }

    private Fragment createFragment(String tag) {
        switch (tag) {
            case TAG_CATEGORIES: return new CategoriesFragment();
            case TAG_EXPLORE:    return new ExploreFragment();
            case TAG_ORDERS:     return new OrdersFragment();
            case TAG_PROFILE:    return new ProfileFragment();
            default:             return new HomeFragment();
        }
    }

    private String tagFor(int itemId) {
        if (itemId == R.id.nav_home) return TAG_HOME;
        if (itemId == R.id.nav_categories) return TAG_CATEGORIES;
        if (itemId == R.id.nav_explore) return TAG_EXPLORE;
        if (itemId == R.id.nav_orders) return TAG_ORDERS;
        if (itemId == R.id.nav_profile) return TAG_PROFILE;
        return null;
    }

    private int idFor(String tag) {
        switch (tag) {
            case TAG_CATEGORIES: return R.id.nav_categories;
            case TAG_EXPLORE:    return R.id.nav_explore;
            case TAG_ORDERS:     return R.id.nav_orders;
            case TAG_PROFILE:    return R.id.nav_profile;
            default:             return R.id.nav_home;
        }
    }

    /** Called by Categories: jump to the Explore tab and search. */
    public void openExplore(String query) {

        bottomNav.setSelectedItemId(R.id.nav_explore);

        Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_EXPLORE);

        if (f instanceof ExploreFragment) {
            ((ExploreFragment) f).search(query);
        }
    }
}