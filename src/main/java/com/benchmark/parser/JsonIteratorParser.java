package com.benchmark.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import java.io.IOException;

public class JsonIteratorParser implements JsonParserInterface {

    @Override
    public boolean isValidJson(String json) {
        try {
            JsonIterator iter = JsonIterator.parse(json);
            // Skip through the entire document to validate
            iter.skip();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            JsonIterator iter = JsonIterator.parse(json);
            if (iter.whatIsNext() != ValueType.OBJECT) {
                return false;
            }
            
            // Stream through object keys until we find the one we want
            for (String field = iter.readObject(); field != null; field = iter.readObject()) {
                if (field.equals(key)) {
                    return true;
                }
                iter.skip(); // Skip the value if it's not the key we want
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "JsonIterator";
    }
}
