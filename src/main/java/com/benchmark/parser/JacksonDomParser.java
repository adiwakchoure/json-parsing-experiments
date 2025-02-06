package com.benchmark.parser;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;

public class JacksonDomParser implements JsonParserInterface {
    private static final ObjectMapper mapper = new ObjectMapper()
        .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true)
        .configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, false);

    @Override
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = json.trim();
        // Only accept objects and arrays as valid JSON
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return false;
        }
        
        try {
            mapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            JsonNode node = mapper.readTree(json);
            return node.isObject() && node.has(key);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getJsonValue(String json, String path) {
        try {
            JsonNode root = mapper.readTree(json);
            
            // Convert path from $.store.book[0] format to /store/book/0 format
            String jsonPointerPath = path.startsWith("$.") ? path.substring(2) : path;
            jsonPointerPath = jsonPointerPath.replace(".", "/")
                                          .replaceAll("\\[(\\d+)\\]", "/$1");
            if (!jsonPointerPath.startsWith("/")) {
                jsonPointerPath = "/" + jsonPointerPath;
            }
            
            JsonPointer pointer = JsonPointer.compile(jsonPointerPath);
            JsonNode current = root.at(pointer);
            
            if (current.isMissingNode()) {
                return "";
            }
            
            // Handle different value types
            if (current.isValueNode()) {
                if (current.isNull()) {
                    return "";
                }
                return current.asText();
            }
            
            // For complex types (arrays/objects), return empty string
            return "";
            
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getName() {
        return "JacksonDOM";
    }
}
