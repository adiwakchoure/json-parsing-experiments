package com.benchmark;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 3, time = 3)
public class JsonParsingBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(JsonParsingBenchmark.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonFactory factory = mapper.getFactory();

    @Param({"100", "1000", "10000", "100000"})
    private int rowCount;

    private List<String> validJsonInputs;
    private List<String> invalidJsonInputs;
    
    // Store results for logging in TearDown
    private int domLevelCount;
    private int streamingLevelCount;

    @Setup
    public void setup() {
        validJsonInputs = DataLoader.loadValidJsonInputs(rowCount);
        invalidJsonInputs = DataLoader.loadInvalidJsonInputs(rowCount);
        logger.info("Loaded {} valid and {} invalid JSON inputs", validJsonInputs.size(), invalidJsonInputs.size());
        
        // Reset counts
        domLevelCount = 0;
        streamingLevelCount = 0;
    }
    
    @TearDown
    public void tearDown() {
        // Log key counts from both parsers
        logger.info("DOM parsing found level field in {} inputs", domLevelCount);
        logger.info("Streaming parsing found level field in {} inputs", streamingLevelCount);
    }

    // Test 1: isValidJson with DOM parser on valid inputs (best case)
    @Benchmark
    public void domIsValidJson_ValidInputs(Blackhole blackhole) {
        int validCount = 0;
        for (String json : validJsonInputs) {
            try {
                JsonNode node = mapper.readTree(json);
                validCount++;
                blackhole.consume(node);
            } catch (IOException e) {
                // Should not happen for valid JSON - our parser is wrong
            }
        }
        if (validCount != validJsonInputs.size()) {
            logger.error("DOM parser failed to parse {} valid JSON inputs!", validJsonInputs.size() - validCount);
        }
    }

    // Test 2: isValidJson with DOM parser on invalid inputs (worst case)
    @Benchmark
    public void domIsValidJson_InvalidInputs(Blackhole blackhole) {
        int validCount = 0;
        for (String json : invalidJsonInputs) {
            try {
                JsonNode node = mapper.readTree(json);
                validCount++;
                if (rowCount == 100000) {  // Only log for the largest test case to avoid spam
                    logger.error("JSON that should be invalid but was parsed: {}", json);
                }
                blackhole.consume(node);
            } catch (IOException e) {
                // Expected for invalid JSON
            }
        }
        if (validCount > 0) {
            logger.error("DOM parser incorrectly parsed {} invalid JSON inputs!", validCount);
        }
    }

    // Test 3: isValidJson with streaming parser on valid inputs (best case)
    @Benchmark
    public void streamingIsValidJson_ValidInputs(Blackhole blackhole) {
        int validCount = 0;
        for (String json : validJsonInputs) {
            try (JsonParser parser = factory.createParser(json)) {
                while (parser.nextToken() != null) {
                    blackhole.consume(parser.getCurrentToken());
                }
                validCount++;
            } catch (IOException e) {
                // Should not happen for valid JSON - our parser is wrong
            }
        }
        if (validCount != validJsonInputs.size()) {
            logger.error("Streaming parser failed to parse {} valid JSON inputs!", validJsonInputs.size() - validCount);
        }
    }

    // Test 4: isValidJson with streaming parser on invalid inputs (worst case)
    @Benchmark
    public void streamingIsValidJson_InvalidInputs(Blackhole blackhole) {
        int validCount = 0;
        for (String json : invalidJsonInputs) {
            try (JsonParser parser = factory.createParser(json)) {
                while (parser.nextToken() != null) {
                    blackhole.consume(parser.getCurrentToken());
                }
                validCount++;
            } catch (IOException e) {
                // Expected for invalid JSON
            }
        }
        if (validCount > 0) {
            logger.error("Streaming parser incorrectly parsed {} invalid JSON inputs!", validCount);
        }
    }

    // Test 5: hasJsonKey with DOM parser (only valid inputs)
    @Benchmark
    public void domHasJsonKey_ValidInputs(Blackhole blackhole) {
        int levelCount = 0;
        
        for (String json : validJsonInputs) {
            try {
                JsonNode node = mapper.readTree(json);
                if (node.has("level")) levelCount++;
                blackhole.consume(node);
            } catch (IOException e) {
                // Should not happen for valid JSON - our parser is wrong
            }
        }
        
        // Store count for logging in TearDown
        this.domLevelCount = levelCount;
    }

    // Test 6: hasJsonKey with streaming parser (only valid inputs)
    @Benchmark
    public void streamingHasJsonKey_ValidInputs(Blackhole blackhole) {
        int levelCount = 0;
        
        for (String json : validJsonInputs) {
            try (JsonParser parser = factory.createParser(json)) {
                boolean hasLevel = false;
                
                while (parser.nextToken() != null) {
                    if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
                        String fieldName = parser.getCurrentName();
                        if ("level".equals(fieldName)) {
                            hasLevel = true;
                            break; // No need to continue parsing once we find the field
                        }
                    }
                    blackhole.consume(parser.getCurrentToken());
                }
                if (hasLevel) levelCount++;
            } catch (IOException e) {
                // Should not happen for valid JSON - our parser is wrong
            }
        }
        
        // Store count for logging in TearDown
        this.streamingLevelCount = levelCount;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JsonParsingBenchmark.class.getSimpleName())
                .build();

        Collection<RunResult> results = new Runner(opt).run();
        ResultVisualizer.createCharts(results);
    }
}
