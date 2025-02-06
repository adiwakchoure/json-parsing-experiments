package com.benchmark.parser;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;

public class FastJsonStreamingParser implements JsonParserInterface {
    
    private static final JSONReader.Feature[] STRICT_FEATURES = new JSONReader.Feature[] {
        JSONReader.Feature.IgnoreNoneSerializable,
        JSONReader.Feature.ErrorOnNoneSerializable
    };

    @Override
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = json.trim();
        // Quick validation - must start with { or [
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return false;
        }
        
        // Quick validation - no single quotes allowed
        if (trimmed.contains("'")) {
            return false;
        }
        
        // Quick validation - no trailing commas
        if (trimmed.matches(".*,\\s*[}\\]]")) {
            return false;
        }
        
        try {
            JSONReader reader = JSONReader.of(json);
            reader.getContext().config(STRICT_FEATURES);
            
            // For objects, verify each key-value pair
            if (trimmed.startsWith("{")) {
                if (!reader.nextIfMatch('{')) {
                    return false;
                }
                
                // Handle empty object
                if (reader.nextIfMatch('}')) {
                    return true;
                }
                
                while (true) {
                    // Must have a field name
                    String fieldName = reader.readFieldName();
                    if (fieldName == null) {
                        return false;
                    }
                    
                    // Must have a value (null is allowed)
                    try {
                        reader.readAny();
                    } catch (Exception e) {
                        return false;
                    }
                    
                    // Check for end of object or next field
                    if (reader.nextIfMatch('}')) {
                        return reader.isEnd();
                    }
                    if (!reader.nextIfMatch(',')) {
                        return false;
                    }
                }
            }
            
            // For arrays, verify each value
            if (trimmed.startsWith("[")) {
                // Read the array and validate it
                try {
                    reader.readArray();
                    return reader.isEnd();
                } catch (Exception e) {
                    return false;
                }
            }
            
            return false;
        } catch (JSONException e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            JSONReader reader = JSONReader.of(json);
            reader.getContext().config(STRICT_FEATURES);
            
            if (reader.nextIfMatch('{')) {
                while (reader.nextIfMatch(',') || !reader.nextIfMatch('}')) {
                    String fieldName = reader.readFieldName();
                    if (key.equals(fieldName)) {
                        return true;
                    }
                    reader.skipValue();
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getJsonValue(String json, String path) {
        try {
            JSONReader reader = JSONReader.of(json);
            reader.getContext().config(STRICT_FEATURES);
            
            if (reader.nextIfMatch('{')) {
                while (reader.nextIfMatch(',') || !reader.nextIfMatch('}')) {
                    String fieldName = reader.readFieldName();
                    if (path.equals(fieldName)) {
                        Object value = reader.readAny();
                        return value == null ? "" : value.toString();
                    }
                    reader.skipValue();
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getName() {
        return "FastJsonStreaming";
    }
}
