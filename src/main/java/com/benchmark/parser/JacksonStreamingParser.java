package com.benchmark.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;

public class JacksonStreamingParser implements JsonParserInterface {
    private static final JsonFactory factory = new JsonFactory();

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
            JsonParser parser = factory.createParser(json);
            while (parser.nextToken() != null) {
                // Keep parsing until the end to validate the entire content
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try (JsonParser parser = factory.createParser(json)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return false;
            }
            
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                if (key.equals(fieldName)) {
                    return true;
                }
                parser.nextToken(); // Skip value
                parser.skipChildren(); // Skip nested objects/arrays
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getJsonValue(String json, String path) {
        try (JsonParser parser = factory.createParser(json)) {
            // Remove the leading "$." if present
            String normalizedPath = path.startsWith("$.") ? path.substring(2) : path;
            String[] segments = normalizedPath.split("\\.");
            int currentSegment = 0;
            
            JsonToken token = parser.nextToken();
            while (token != null && currentSegment < segments.length) {
                String segment = segments[currentSegment];
                
                // Handle array access
                if (segment.contains("[") && segment.contains("]")) {
                    String arrayName = segment.substring(0, segment.indexOf('['));
                    int targetIndex = Integer.parseInt(segment.substring(segment.indexOf('[') + 1, segment.indexOf(']')));
                    
                    // Find the array field
                    while (token != null) {
                        if (token == JsonToken.FIELD_NAME && arrayName.equals(parser.getCurrentName())) {
                            token = parser.nextToken(); // Move to array start
                            if (token != JsonToken.START_ARRAY) return "";
                            
                            // Skip to target index
                            int currentIndex = 0;
                            while (currentIndex < targetIndex) {
                                parser.skipChildren();
                                token = parser.nextToken();
                                if (token == JsonToken.END_ARRAY) return "";
                                currentIndex++;
                            }
                            currentSegment++;
                            break;
                        }
                        token = parser.nextToken();
                        if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
                            parser.skipChildren();
                        }
                    }
                } else {
                    // Find the field
                    while (token != null) {
                        if (token == JsonToken.FIELD_NAME && segment.equals(parser.getCurrentName())) {
                            if (currentSegment == segments.length - 1) {
                                // This is the target field, get its value
                                token = parser.nextToken();
                                if (token == JsonToken.VALUE_NULL) {
                                    return "";
                                }
                                if (token.isScalarValue()) {
                                    return parser.getValueAsString();
                                }
                                return ""; // Non-scalar value
                            }
                            currentSegment++;
                            break;
                        }
                        token = parser.nextToken();
                        if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
                            parser.skipChildren();
                        }
                    }
                }
                
                if (token == null) return "";
                token = parser.nextToken();
            }
            
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getName() {
        return "JacksonStreaming";
    }
}
