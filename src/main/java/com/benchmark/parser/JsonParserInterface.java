package com.benchmark.parser;

public interface JsonParserInterface {
    /**
     * Check if input is valid JSON
     * @param json Input JSON string
     * @return true if valid JSON
     */
    boolean isValidJson(String json);

    /**
     * Check if JSON object has a specific key
     * @param json Input JSON string
     * @param key Key to look for
     * @return true if key exists
     */
    boolean hasJsonKey(String json, String key);

    /**
     * Get parser name for benchmarking
     * @return Parser name
     */
    String getName();
}
