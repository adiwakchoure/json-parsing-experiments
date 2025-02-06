package com.benchmark.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonDomParser implements JsonParserInterface {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isValidJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            JsonNode node = mapper.readTree(json);
            return node.has(key);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "JacksonDOM";
    }
}
