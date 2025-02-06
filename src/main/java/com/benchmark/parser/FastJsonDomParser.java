package com.benchmark.parser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;

public class FastJsonDomParser implements JsonParserInterface {

    @Override
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        // Basic JSON validation - must start with { or [ and use double quotes
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return false;
        }
        
        // Check for single quotes which are not valid in JSON
        if (json.contains("'")) {
            return false;
        }
        
        // Check for trailing commas
        if (json.contains(",}") || json.contains(",]")) {
            return false;
        }
        
        try {
            JSON.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            JSONObject obj = JSON.parseObject(json);
            return obj != null && obj.containsKey(key);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getJsonValue(String json, String path) {
        try {
            Object obj = JSON.parse(json);
            if (obj == null) {
                return "";
            }
            
            // Handle root path
            if (path.equals("$")) {
                return obj.toString();
            }
            
            // Remove leading $. if present
            String normalizedPath = path.startsWith("$.") ? path : "$." + path;
            Object value = JSONPath.extract(json, normalizedPath);
            
            if (value == null) {
                return "";
            }
            
            // Handle different value types
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                return value.toString();
            }
            
            // For complex types (arrays/objects), return empty string
            return "";
            
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getName() {
        return "FastJsonDOM";
    }
}
