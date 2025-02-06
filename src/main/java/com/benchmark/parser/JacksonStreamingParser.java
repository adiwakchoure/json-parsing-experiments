package com.benchmark.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.JsonReadFeature;

public class JacksonStreamingParser implements JsonParserInterface {
    private final JsonFactory factory = JsonFactory.builder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .build();

    @Override
    public boolean isValidJson(String json) {
        try {
            JsonParser parser = factory.createParser(json);
            while (parser.nextToken() != null) {
                // Just iterate through tokens
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            JsonParser parser = factory.createParser(json);
            while (parser.nextToken() != null) {
                if (parser.getCurrentToken() == JsonToken.FIELD_NAME && 
                    key.equals(parser.getCurrentName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "JacksonStreaming";
    }
}
