package com.benchmark.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;

public class GsonDomParser implements JsonParserInterface {

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
        
        // Check for trailing commas in arrays
        if (trimmed.startsWith("[")) {
            boolean inQuotes = false;
            boolean expectingValue = true;
            
            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                
                if (c == '"' && (i == 0 || trimmed.charAt(i-1) != '\\')) {
                    inQuotes = !inQuotes;
                } else if (!inQuotes) {
                    if (c == ',') {
                        if (!expectingValue) {
                            expectingValue = true;
                        } else {
                            // Found a comma when we were already expecting a value (double comma or trailing comma)
                            return false;
                        }
                    } else if (c == ']') {
                        if (expectingValue && i > 1 && trimmed.charAt(i-1) == ',') {
                            // Found a trailing comma before the closing bracket
                            return false;
                        }
                    } else if (!Character.isWhitespace(c)) {
                        expectingValue = false;
                    }
                }
            }
        }
        
        // Check for unquoted keys, single quotes, invalid number formats, and undefined values
        if (trimmed.startsWith("{")) {
            int braceCount = 0;
            boolean inQuotes = false;
            boolean expectingKey = true;
            StringBuilder currentToken = new StringBuilder();
            
            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                
                if (c == '"' && (i == 0 || trimmed.charAt(i-1) != '\\')) {
                    inQuotes = !inQuotes;
                    currentToken.setLength(0);
                } else if (!inQuotes) {
                    if (c == '{') {
                        braceCount++;
                        expectingKey = true;
                        currentToken.setLength(0);
                    } else if (c == '}') {
                        braceCount--;
                        expectingKey = false;
                        currentToken.setLength(0);
                    } else if (c == ':' && braceCount == 1) {
                        // When we find a colon at the top level, check if the previous token was a quoted key
                        int j = i - 1;
                        while (j >= 0 && Character.isWhitespace(trimmed.charAt(j))) j--;
                        if (j >= 0 && trimmed.charAt(j) != '"') {
                            return false;
                        }
                        currentToken.setLength(0);
                    } else if (c == '\'') {
                        // Single quotes are not allowed in JSON
                        return false;
                    } else if (!Character.isWhitespace(c)) {
                        currentToken.append(c);
                        
                        // Check for invalid values like undefined
                        String token = currentToken.toString();
                        if (token.equals("undefined") || token.equals("NaN") || token.equals("Infinity") || token.equals("-Infinity")) {
                            return false;
                        }
                        
                        // Check for invalid number formats like multiple dots
                        if (Character.isDigit(c) || c == '.' || c == '-' || c == 'e' || c == 'E' || c == '+') {
                            if (c == '.') {
                                int dotCount = 0;
                                for (int k = 0; k < currentToken.length(); k++) {
                                    if (currentToken.charAt(k) == '.') {
                                        dotCount++;
                                    }
                                }
                                if (dotCount > 1) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        try {
            JsonElement element = JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            JsonElement element = JsonParser.parseReader(reader);
            return element.isJsonObject() && element.getAsJsonObject().has(key);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getJsonValue(String json, String path) {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            JsonElement root = JsonParser.parseReader(reader);
            
            // Remove the leading "$." if present
            String normalizedPath = path.startsWith("$.") ? path.substring(2) : path;
            
            // Split path into segments
            String[] segments = normalizedPath.split("\\.");
            JsonElement current = root;
            
            for (String segment : segments) {
                if (current == null || !current.isJsonObject()) return "";
                
                // Handle array access e.g. books[0]
                if (segment.contains("[") && segment.contains("]")) {
                    String arrayName = segment.substring(0, segment.indexOf('['));
                    String indexStr = segment.substring(segment.indexOf('[') + 1, segment.indexOf(']'));
                    int index = Integer.parseInt(indexStr);
                    
                    JsonElement array = current.getAsJsonObject().get(arrayName);
                    if (array == null || !array.isJsonArray() || index >= array.getAsJsonArray().size()) {
                        return "";
                    }
                    current = array.getAsJsonArray().get(index);
                } else {
                    current = current.getAsJsonObject().get(segment);
                }
                
                if (current == null) return "";
            }
            
            // Handle different value types
            if (current.isJsonPrimitive()) {
                if (current.isJsonNull()) return "";
                return current.getAsString();
            }
            
            // For complex types (arrays/objects), return empty string
            return "";
            
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getName() {
        return "GsonDOM";
    }
}
