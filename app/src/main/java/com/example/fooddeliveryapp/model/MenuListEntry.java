package com.example.fooddeliveryapp.model;

public class MenuListEntry {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private final int type;
    private final String headerTitle;
    private final MenuItem menuItem;

    private MenuListEntry(int type, String headerTitle, MenuItem menuItem) {
        this.type = type;
        this.headerTitle = headerTitle;
        this.menuItem = menuItem;
    }

    public static MenuListEntry header(String title) {
        return new MenuListEntry(TYPE_HEADER, title, null);
    }

    public static MenuListEntry item(MenuItem item) {
        return new MenuListEntry(TYPE_ITEM, null, item);
    }

    public int getType() {
        return type;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }
}