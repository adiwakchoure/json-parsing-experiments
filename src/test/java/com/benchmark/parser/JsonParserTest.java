package com.benchmark.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JsonParserTest {

    private final JsonParserInterface[] parsers = {
            new FastJsonStreamingParser(),
            new FastJsonDomParser(),
            new JsonIteratorParser(),
            new JacksonStreamingParser(),
            new JacksonDomParser(),
            new GsonDomParser(),
            new GsonStreamingParser()
    };

    @Test
    public void testValidJson() {
        String[] validJsonInputs = {
            "{}",  // Empty object
            "[]",  // Empty array
            "{\"key\": \"value\"}", // Simple key-value
            "{\"number\": 123}", // Number
            "{\"boolean\": true}", // Boolean
            "{\"null\": null}", // Null
            "[1,2,3]", // Array of numbers
            "{\"nested\": {\"key\": \"value\"}}", // Nested object
            "{\"array\": [1,{\"key\": \"value\"}]}", // Mixed array
            "{\n  \"key\": \"value\"\n}" // With whitespace
        };

        for (JsonParserInterface parser : parsers) {
            String parserName = parser.getClass().getSimpleName();
            for (String json : validJsonInputs) {
                assertTrue(parser.isValidJson(json), 
                    String.format("%s failed to validate valid JSON: %s", parserName, json));
            }
        }
    }

    @Test
    public void testInvalidJson() {
        String[] invalidJsonInputs = {
            "", // Empty string
            "not json", // Plain text
            "{", // Incomplete object
            "}", // Just closing brace
            "[", // Incomplete array
            "{\"key\": }", // Missing value
            "{key: \"value\"}", // Unquoted key
            "{\"key\": 'value'}", // Single quotes
            "{\"key\": \"value\",}", // Trailing comma
            "{\"key\": 12.3.4}", // Invalid number
            "{'key': \"value\"}", // Single quoted key
            "[1,2,]", // Trailing comma in array
            "{\"key\": undefined}", // JavaScript undefined
            "{\"key\": value}", // Unquoted string
            "\"just a string\"" // Just a string value
        };

        for (JsonParserInterface parser : parsers) {
            String parserName = parser.getClass().getSimpleName();
            for (String json : invalidJsonInputs) {
                assertFalse(parser.isValidJson(json), 
                    String.format("%s incorrectly validated invalid JSON: %s", parserName, json));
            }
        }
    }

    @Test
    public void testHasJsonKey() {
        String json = "{\"name\": \"John\", \"age\": 30, \"nested\": {\"key\": \"value\"}}";
        
        for (JsonParserInterface parser : parsers) {
            String parserName = parser.getClass().getSimpleName();
            
            assertTrue(parser.hasJsonKey(json, "name"), 
                String.format("%s failed to find existing key 'name'", parserName));
            assertTrue(parser.hasJsonKey(json, "age"), 
                String.format("%s failed to find existing key 'age'", parserName));
            assertTrue(parser.hasJsonKey(json, "nested"), 
                String.format("%s failed to find existing key 'nested'", parserName));
            assertFalse(parser.hasJsonKey(json, "nonexistent"), 
                String.format("%s found non-existent key", parserName));
        }
    }

    @Test
    public void testGetJsonValue() {
        String json = "{\"string\": \"value\", \"number\": 123, \"boolean\": true, \"null\": null, \"nested\": {\"key\": \"value\"}}";
        
        for (JsonParserInterface parser : parsers) {
            String parserName = parser.getClass().getSimpleName();
            
            assertEquals("value", parser.getJsonValue(json, "string"), 
                String.format("%s failed to get string value", parserName));
            assertEquals("123", parser.getJsonValue(json, "number"), 
                String.format("%s failed to get number value", parserName));
            assertEquals("true", parser.getJsonValue(json, "boolean"), 
                String.format("%s failed to get boolean value", parserName));
            assertEquals("", parser.getJsonValue(json, "null"), 
                String.format("%s failed to handle null value", parserName));
            assertEquals("", parser.getJsonValue(json, "nonexistent"), 
                String.format("%s failed to handle non-existent key", parserName));
        }
    }
}
