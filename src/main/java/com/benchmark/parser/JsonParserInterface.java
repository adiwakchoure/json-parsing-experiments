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
     * Extract a value from JSON using a path
     * Similar to ClickHouse's JSON_VALUE function
     * @param json Input JSON string
     * @param path Path to the value (e.g. "$.store.book[0].title" or "$.hello")
     * @return The extracted value as a string, or empty string if not found
     */
    String getJsonValue(String json, String path);

    /**
     * Get parser name for benchmarking
     * @return Parser name
     */
    String getName();
}
