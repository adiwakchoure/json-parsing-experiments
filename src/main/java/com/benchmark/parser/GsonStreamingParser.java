package com.benchmark.parser;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.StringReader;

public class GsonStreamingParser implements JsonParserInterface {
    @Override
    public boolean isValidJson(String json) {
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
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
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                return false;
            }
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (key.equals(name)) {
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
    public String getName() {
        return "GsonStreaming";
    }
}
