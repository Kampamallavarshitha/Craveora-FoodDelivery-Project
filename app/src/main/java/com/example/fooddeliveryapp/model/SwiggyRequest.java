package com.example.fooddeliveryapp.model;

import java.util.Map;

public class SwiggyRequest {

    private String toolName;
    private Map<String, Object> arguments;

    public SwiggyRequest(
            String toolName,
            Map<String, Object> arguments
    ) {
        this.toolName = toolName;
        this.arguments = arguments;
    }

    public String getToolName() {
        return toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }
}