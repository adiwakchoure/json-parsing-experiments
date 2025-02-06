package com.benchmark.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.spi.JsonException;

public class JsonIteratorParser implements JsonParserInterface {

    @Override
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        // Basic JSON validation
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return false;
        }
        
        // Check for common invalid JSON patterns
        if (json.contains(": }") || json.contains(": ]") ||  // Missing values
            json.contains(",}") || json.contains(",]") ||    // Trailing commas
            json.contains("'") ||                           // Single quotes
            json.contains("undefined") ||                   // JavaScript undefined
            json.contains(": value")) {                     // Unquoted values
            return false;
        }
        
        // Check for invalid numbers (multiple dots)
        if (json.matches(".*[0-9]+\\.[0-9]+\\.[0-9]+.*")) {
            return false;
        }
        
        // Check for unquoted keys
        if (trimmed.startsWith("{")) {
            int braceCount = 0;
            boolean inQuotes = false;
            boolean expectingKey = true;
            
            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                
                if (c == '"' && (i == 0 || trimmed.charAt(i-1) != '\\')) {
                    inQuotes = !inQuotes;
                } else if (!inQuotes) {
                    if (c == '{') {
                        braceCount++;
                        expectingKey = true;
                    } else if (c == '}') {
                        braceCount--;
                        expectingKey = false;
                    } else if (c == ':' && braceCount == 1) {
                        // When we find a colon at the top level, check if the previous token was a quoted key
                        int j = i - 1;
                        while (j >= 0 && Character.isWhitespace(trimmed.charAt(j))) j--;
                        if (j >= 0 && trimmed.charAt(j) != '"') {
                            return false;
                        }
                    }
                }
            }
        }
        
        try {
            // Let JsonIterator do the heavy lifting for validation
            Any parsed = JsonIterator.deserialize(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            Any any = JsonIterator.deserialize(json);
            if (!any.toString().startsWith("{")) {
                return false;
            }
            
            // Check if the key exists and has a non-null value
            String jsonStr = any.toString();
            return jsonStr.contains("\"" + key + "\":");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getJsonValue(String json, String path) {
        try {
            Any any = JsonIterator.deserialize(json);
            Any value = any.get(path);
            return value == null || value.valueType().name().equals("NULL") ? "" : value.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getName() {
        return "JsonIterator";
    }
}
