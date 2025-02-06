package com.benchmark;

import com.benchmark.parser.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class JsonParsingBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(JsonParsingBenchmark.class);
    private static final int SAMPLE_SIZE = 100000;
    
    private static List<String> validJsonInputs;
    private static List<String> invalidJsonInputs;
    private static final String jsonKey = "id";
    
    static {
        validJsonInputs = DataLoader.loadValidJsonInputs(SAMPLE_SIZE);
        invalidJsonInputs = DataLoader.loadInvalidJsonInputs(SAMPLE_SIZE);
        logger.info("Loaded {} valid and {} invalid JSON inputs", validJsonInputs.size(), invalidJsonInputs.size());
    }

    private final JsonParserInterface jacksonDomParser = new JacksonDomParser();
    private final JsonParserInterface jacksonStreamingParser = new JacksonStreamingParser();
    private final JsonParserInterface gsonDomParser = new GsonDomParser();
    private final JsonParserInterface gsonStreamingParser = new GsonStreamingParser();
    private final JsonParserInterface fastJsonDomParser = new FastJsonDomParser();
    private final JsonParserInterface fastJsonStreamingParser = new FastJsonStreamingParser();
    private final JsonParserInterface jsonIteratorParser = new JsonIteratorParser();

    @Benchmark
    public void jacksonDomParser_ValidInputs(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(jacksonDomParser.isValidJson(json));
        }
    }

    @Benchmark
    public void jacksonDomParser_InvalidInputs(Blackhole blackhole) {
        for (String json : invalidJsonInputs) {
            blackhole.consume(jacksonDomParser.isValidJson(json));
        }
    }

    @Benchmark
    public void jacksonDomParser_HasKey(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(jacksonDomParser.hasJsonKey(json, jsonKey));
        }
    }

    @Benchmark
    public void jacksonStreamingParser_ValidInputs(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(jacksonStreamingParser.isValidJson(json));
        }
    }

    @Benchmark
    public void jacksonStreamingParser_InvalidInputs(Blackhole blackhole) {
        for (String json : invalidJsonInputs) {
            blackhole.consume(jacksonStreamingParser.isValidJson(json));
        }
    }

    @Benchmark
    public void jacksonStreamingParser_HasKey(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(jacksonStreamingParser.hasJsonKey(json, jsonKey));
        }
    }

    @Benchmark
    public void gsonDomParser_ValidInputs(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(gsonDomParser.isValidJson(json));
        }
    }

    @Benchmark
    public void gsonDomParser_InvalidInputs(Blackhole blackhole) {
        for (String json : invalidJsonInputs) {
            blackhole.consume(gsonDomParser.isValidJson(json));
        }
    }

    @Benchmark
    public void gsonDomParser_HasKey(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(gsonDomParser.hasJsonKey(json, jsonKey));
        }
    }

    @Benchmark
    public void gsonStreamingParser_ValidInputs(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(gsonStreamingParser.isValidJson(json));
        }
    }

    @Benchmark
    public void gsonStreamingParser_InvalidInputs(Blackhole blackhole) {
        for (String json : invalidJsonInputs) {
            blackhole.consume(gsonStreamingParser.isValidJson(json));
        }
    }

    @Benchmark
    public void gsonStreamingParser_HasKey(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(gsonStreamingParser.hasJsonKey(json, jsonKey));
        }
    }

    @Benchmark
    public void fastJsonDomParser_ValidInputs(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(fastJsonDomParser.isValidJson(json));
        }
    }

    @Benchmark
    public void fastJsonDomParser_InvalidInputs(Blackhole blackhole) {
        for (String json : invalidJsonInputs) {
            blackhole.consume(fastJsonDomParser.isValidJson(json));
        }
    }

    @Benchmark
    public void fastJsonDomParser_HasKey(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(fastJsonDomParser.hasJsonKey(json, jsonKey));
        }
    }

    @Benchmark
    public void fastJsonStreamingParser_ValidInputs(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(fastJsonStreamingParser.isValidJson(json));
        }
    }

    @Benchmark
    public void fastJsonStreamingParser_InvalidInputs(Blackhole blackhole) {
        for (String json : invalidJsonInputs) {
            blackhole.consume(fastJsonStreamingParser.isValidJson(json));
        }
    }

    @Benchmark
    public void fastJsonStreamingParser_HasKey(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(fastJsonStreamingParser.hasJsonKey(json, jsonKey));
        }
    }

    @Benchmark
    public void jsonIteratorParser_ValidInputs(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(jsonIteratorParser.isValidJson(json));
        }
    }

    @Benchmark
    public void jsonIteratorParser_InvalidInputs(Blackhole blackhole) {
        for (String json : invalidJsonInputs) {
            blackhole.consume(jsonIteratorParser.isValidJson(json));
        }
    }

    @Benchmark
    public void jsonIteratorParser_HasKey(Blackhole blackhole) {
        for (String json : validJsonInputs) {
            blackhole.consume(jsonIteratorParser.hasJsonKey(json, jsonKey));
        }
    }

    public static void main(String[] args) throws Exception {
        ResultWriter.runBenchmarkAndSaveResults();
    }
}
