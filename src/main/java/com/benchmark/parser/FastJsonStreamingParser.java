package com.benchmark.parser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;

public class FastJsonStreamingParser implements JsonParserInterface {
    
    @Override
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        // Check for common invalid JSON patterns
        String trimmed = json.trim();
        if (trimmed.contains("'") || // Single quotes
            trimmed.contains("undefined") || // JavaScript undefined
            trimmed.endsWith(",}") || // Trailing comma in object
            trimmed.endsWith(",]") || // Trailing comma in array
            trimmed.matches(".*[^\\\\]'.*") || // Unescaped single quotes
            (trimmed.startsWith("\"") && trimmed.endsWith("\"")) || // Just a string
            (!trimmed.startsWith("{") && !trimmed.startsWith("[")) || // Must be object or array
            trimmed.contains(":.") || // Missing value after colon
            trimmed.contains(",,")) { // Double comma
            return false;
        }
        
        try {
            JSON.parse(json);
            return true;
        } catch (JSONException e) {
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
            Object value = JSONPath.extract(json, path);
            return value == null ? "" : value.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getName() {
        return "FastJsonStreaming";
    }
}
