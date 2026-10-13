package com.example.fooddeliveryapp;

import android.content.Context;
import android.content.SharedPreferences;

/** App-wide constants + small local profile storage. */
public final class AppData {

    private AppData() {}

    public static final String USER_ID = "test-user-001";
    public static final String ADDRESS_ID = "da7d8kc1d96midrdfum0";

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences("fd_prefs", Context.MODE_PRIVATE);
    }

    public static String getName(Context c) { return prefs(c).getString("name", "Guest User"); }
    public static String getPhone(Context c) { return prefs(c).getString("phone", ""); }
    public static String getAddress(Context c) { return prefs(c).getString("address", "Home"); }
    public static boolean isNotifOn(Context c) { return prefs(c).getBoolean("notif", true); }

    public static void setName(Context c, String v) { prefs(c).edit().putString("name", v).apply(); }
    public static void setPhone(Context c, String v) { prefs(c).edit().putString("phone", v).apply(); }
    public static void setAddress(Context c, String v) { prefs(c).edit().putString("address", v).apply(); }
    public static void setNotif(Context c, boolean v) { prefs(c).edit().putBoolean("notif", v).apply(); }

    public static void clearProfile(Context c) {
        prefs(c).edit().remove("name").remove("phone").remove("address").remove("notif").apply();
    }
}