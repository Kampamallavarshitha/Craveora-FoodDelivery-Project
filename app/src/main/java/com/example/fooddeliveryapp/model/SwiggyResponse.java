package com.example.fooddeliveryapp.model;

import java.util.List;

public class SwiggyResponse {

    private boolean success;
    private String toolName;
    private List<SwiggyData> data;

    public boolean isSuccess() {
        return success;
    }

    public String getToolName() {
        return toolName;
    }

    public List<SwiggyData> getData() {
        return data;
    }

    public static class SwiggyData {

        private String type;
        private String text;

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }
}