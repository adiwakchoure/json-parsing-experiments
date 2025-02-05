package com.benchmark;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ParserSanityCheck {
    private static final Logger logger = LoggerFactory.getLogger(ParserSanityCheck.class);
    private static final Set<String> checkedParsers = new HashSet<>();
    
    private static final String[] VALID_JSONS = {
        "{\"message\": \"test\"}",
        "{\"id\": 1, \"message\": \"hello\", \"nested\": {\"key\": \"value\"}}",
        "{}",
        "{\"array\": [1,2,3], \"message\": \"with array\"}"
    };
    
    private static final String[] INVALID_JSONS = {
        "{message: \"missing quotes\"}",
        "{\"unclosed\": \"string}",
        "not json at all",
        "{\"missing\": }",
        "{\"trailing\": \"comma\",}"
    };

    public static synchronized void checkDomParser() {
        if (checkedParsers.contains("jackson-dom")) {
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("Testing Jackson DOM parser...");
        
        for (String json : VALID_JSONS) {
            try {
                JsonNode node = objectMapper.readTree(json);
                if (node == null) {
                    throw new RuntimeException("DOM parser failed to parse valid JSON: " + json);
                }
            } catch (Exception e) {
                throw new RuntimeException("DOM parser incorrectly failed on valid JSON: " + json, e);
            }
        }
        
        for (String json : INVALID_JSONS) {
            try {
                JsonNode node = objectMapper.readTree(json);
                throw new RuntimeException("DOM parser incorrectly accepted invalid JSON: " + json);
            } catch (Exception e) {
                // Expected failure
            }
        }
        
        logger.info("Jackson DOM parser validation complete");
        checkedParsers.add("jackson-dom");
    }

    public static synchronized void checkStreamingParser() {
        if (checkedParsers.contains("jackson-streaming")) {
            return;
        }

        JsonFactory jsonFactory = new JsonFactory();
        logger.info("Testing Jackson Streaming parser...");
        
        for (String json : VALID_JSONS) {
            try (JsonParser parser = jsonFactory.createParser(json)) {
                while (parser.nextToken() != null) {
                    // Just iterate through tokens
                }
            } catch (Exception e) {
                throw new RuntimeException("Streaming parser incorrectly failed on valid JSON: " + json, e);
            }
        }
        
        for (String json : INVALID_JSONS) {
            try (JsonParser parser = jsonFactory.createParser(json)) {
                while (parser.nextToken() != null) {
                    // Just iterate through tokens
                }
                throw new RuntimeException("Streaming parser incorrectly accepted invalid JSON: " + json);
            } catch (Exception e) {
                // Expected failure
            }
        }
        
        logger.info("Jackson Streaming parser validation complete");
        checkedParsers.add("jackson-streaming");
    }

    public static void runSanityChecks() {
        if (checkedParsers.isEmpty()) {
            logger.info("Running initial parser sanity checks...");
            checkDomParser();
            checkStreamingParser();
            logger.info("All parser sanity checks passed!");
        }
    }
}
