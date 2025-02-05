package com.benchmark;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
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
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@Fork(1)
public class JsonParsingBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(JsonParsingBenchmark.class);

    private ObjectMapper objectMapper;
    private JsonFactory jsonFactory;
    private List<String> jsonInputs;

    @Param({"100", "1000", "10000", "100000", "500000"})
    private int rowCount;

    @Setup(Level.Trial)
    public void setup() {
        objectMapper = new ObjectMapper();
        jsonFactory = new JsonFactory();
        jsonInputs = DataLoader.loadJsonInputs(rowCount);
        
        // Run sanity checks once during setup
        ParserSanityCheck.runSanityChecks();
    }

    @Benchmark
    public int jacksonDomIsValidJson() {
        int validCount = 0;
        for (String json : jsonInputs) {
            try {
                objectMapper.readTree(json);
                validCount++;
            } catch (IOException e) {
                // Invalid JSON
            }
        }
        return validCount;
    }

    @Benchmark
    public int jacksonStreamingIsValidJson() {
        int validCount = 0;
        for (String json : jsonInputs) {
            try {
                // Use the parser to validate without manual token iteration
                JsonParser parser = jsonFactory.createParser(json);
                parser.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
                // Skip to the end - this validates the structure without manual iteration
                parser.skipChildren();
                validCount++;
                parser.close();
            } catch (IOException e) {
                // Invalid JSON
            }
        }
        return validCount;
    }

    @Benchmark
    public int jacksonDomHasJsonKey() {
        int validCount = 0;
        for (String json : jsonInputs) {
            try {
                JsonNode node = objectMapper.readTree(json);
                if (node.has("someKey")) {
                    validCount++;
                }
            } catch (IOException e) {
                // Invalid JSON
            }
        }
        return validCount;
    }

    @Benchmark
    public int jacksonStreamingHasJsonKey() {
        int validCount = 0;
        for (String json : jsonInputs) {
            try {
                JsonParser parser = jsonFactory.createParser(json);
                parser.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
                
                // More efficient key search
                JsonToken token;
                while ((token = parser.nextToken()) != null) {
                    if (token == JsonToken.FIELD_NAME && "someKey".equals(parser.getCurrentName())) {
                        validCount++;
                        break;
                    }
                    // Skip nested objects/arrays if we haven't found our key
                    if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
                        parser.skipChildren();
                    }
                }
                parser.close();
            } catch (IOException e) {
                // Invalid JSON
            }
        }
        return validCount;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JsonParsingBenchmark.class.getSimpleName())
                .build();

        Collection<RunResult> results = new Runner(opt).run();
        ResultVisualizer.createCharts(results);
    }
}
