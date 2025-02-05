package com.benchmark.json;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class JsonParserBenchmark {
    private ObjectMapper objectMapper;
    private JsonFactory jsonFactory;
    private String jsonInput;

    @Param({"100", "1K", "10K"})
    private String dataSize;

    @Setup
    public void setup() throws IOException {
        objectMapper = new ObjectMapper();
        jsonFactory = new JsonFactory();
        
        // Load data from Parquet based on dataSize parameter
        ParquetDataReader reader = new ParquetDataReader();
        jsonInput = reader.readData(dataSize);
    }

    @Benchmark
    public void jacksonDomParsing(Blackhole blackhole) throws IOException {
        JsonNode node = objectMapper.readTree(jsonInput);
        blackhole.consume(node);
    }

    @Benchmark
    public void jacksonStreamingParsing(Blackhole blackhole) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(jsonInput)) {
            while (parser.nextToken() != null) {
                blackhole.consume(parser.getCurrentToken());
            }
        }
    }

    @Benchmark
    public void jsoniterParsing(Blackhole blackhole) throws IOException {
        JsonIterator iter = JsonIterator.parse(jsonInput);
        while (iter.readAny() != null) {
            blackhole.consume(iter.readAny());
        }
    }

    @Benchmark
    public void fastjsonParsing(Blackhole blackhole) {
        Object obj = JSON.parse(jsonInput);
        blackhole.consume(obj);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JsonParserBenchmark.class.getSimpleName())
                .build();

        Collection<RunResult> results = new Runner(opt).run();
        BenchmarkResultVisualizer.createCharts(results);
    }
}
