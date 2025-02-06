package com.benchmark.parser;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.StringReader;

public class GsonStreamingParser implements JsonParserInterface {

    @Override
    public boolean isValidJson(String json) {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            while (reader.peek() != JsonToken.END_DOCUMENT) {
                reader.skipValue();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                return false;
            }
            
            reader.beginObject();
            while (reader.hasNext()) {
                String fieldName = reader.nextName();
                if (key.equals(fieldName)) {
                    return true;
                }
                reader.skipValue();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getJsonValue(String json, String path) {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            // Remove the leading "$." if present
            String normalizedPath = path.startsWith("$.") ? path.substring(2) : path;
            String[] segments = normalizedPath.split("\\.");
            int currentSegment = 0;
            
            while (currentSegment < segments.length) {
                String segment = segments[currentSegment];
                
                JsonToken token = reader.peek();
                if (token == JsonToken.END_DOCUMENT) return "";
                
                // Handle array access
                if (segment.contains("[") && segment.contains("]")) {
                    String arrayName = segment.substring(0, segment.indexOf('['));
                    int targetIndex = Integer.parseInt(segment.substring(segment.indexOf('[') + 1, segment.indexOf(']')));
                    
                    // Navigate to array
                    boolean foundArray = false;
                    if (token == JsonToken.BEGIN_OBJECT) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name = reader.nextName();
                            if (arrayName.equals(name)) {
                                foundArray = true;
                                break;
                            }
                            reader.skipValue();
                        }
                        if (!foundArray) return "";
                    }
                    
                    // Navigate to array index
                    if (reader.peek() != JsonToken.BEGIN_ARRAY) return "";
                    reader.beginArray();
                    int currentIndex = 0;
                    while (currentIndex < targetIndex && reader.hasNext()) {
                        reader.skipValue();
                        currentIndex++;
                    }
                    if (currentIndex != targetIndex || !reader.hasNext()) return "";
                    
                    currentSegment++;
                    if (currentSegment == segments.length) {
                        // This is our target value
                        token = reader.peek();
                        if (token.equals(JsonToken.NULL)) return "";
                        if (token.equals(JsonToken.BEGIN_ARRAY) || token.equals(JsonToken.BEGIN_OBJECT)) return "";
                        if (token.equals(JsonToken.BOOLEAN)) return String.valueOf(reader.nextBoolean());
                        return reader.nextString();
                    }
                    
                } else {
                    // Navigate through object
                    if (token != JsonToken.BEGIN_OBJECT) return "";
                    
                    reader.beginObject();
                    boolean found = false;
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (segment.equals(name)) {
                            found = true;
                            break;
                        }
                        reader.skipValue();
                    }
                    if (!found) return "";
                    
                    currentSegment++;
                    if (currentSegment == segments.length) {
                        // This is our target value
                        token = reader.peek();
                        if (token.equals(JsonToken.NULL)) return "";
                        if (token.equals(JsonToken.BEGIN_ARRAY) || token.equals(JsonToken.BEGIN_OBJECT)) return "";
                        if (token.equals(JsonToken.BOOLEAN)) return String.valueOf(reader.nextBoolean());
                        return reader.nextString();
                    }
                }
            }
            
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getName() {
        return "GsonStreaming";
    }
}
