package com.benchmark.parser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public class FastJsonDomParser implements JsonParserInterface {

    @Override
    public boolean isValidJson(String json) {
        return JSON.isValid(json);
    }

    @Override
    public boolean hasJsonKey(String json, String key) {
        try {
            Object obj = JSON.parse(json);
            if (obj instanceof JSONObject) {
                return ((JSONObject) obj).containsKey(key);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "FastJsonDOM";
    }
}
