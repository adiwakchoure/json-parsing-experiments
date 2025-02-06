package com.benchmark.parser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GsonDomParser implements JsonParserInterface {
    private final Gson gson = new Gson();

    @Override
    public boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonObject()) {
                return element.getAsJsonObject().has(key);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "GsonDOM";
    }
}
