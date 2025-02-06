package com.benchmark.parser;

import com.alibaba.fastjson2.JSONReader;

public class FastJsonStreamingParser implements JsonParserInterface {

    @Override
    public boolean isValidJson(String json) {
        try {
            try (JSONReader reader = JSONReader.of(json)) {
                // Read the entire structure
                reader.readAny();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            try (JSONReader reader = JSONReader.of(json)) {
                if (reader.nextIfObjectStart()) {
                    while (!reader.nextIfObjectEnd()) {
                        String fieldName = reader.readFieldName();
                        if (key.equals(fieldName)) {
                            return true;
                        }
                        reader.skipValue();
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "FastJsonStreaming";
    }
}
