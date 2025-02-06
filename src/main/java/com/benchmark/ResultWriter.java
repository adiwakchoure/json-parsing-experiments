package com.benchmark;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.benchmark.parser.JsonIteratorParser;

import java.io.File;
import java.util.Collection;

public class ResultWriter {
    private static final Logger logger = LoggerFactory.getLogger(ResultWriter.class);

    public static void runBenchmarkAndSaveResults() throws RunnerException {
        String resultFile = "data/results/benchmark_results_" + 
                          System.currentTimeMillis() + ".csv";

        // Options opt = new OptionsBuilder()
        //         .include(JsonParsingBenchmark.class.getSimpleName())
        //         .resultFormat(ResultFormatType.CSV)
        //         .result(resultFile)
        //         .build();

        Options opt = new OptionsBuilder()
            .include(".*fastJson.*") // If you need to specify the parser you want to test
            .resultFormat(ResultFormatType.CSV)
            .result(resultFile)
            .build();

        Collection<RunResult> results = new Runner(opt).run();
        logger.info("Results have been written to {}", resultFile);
    }

    public static void main(String[] args) throws RunnerException {
        // Create results directory if it doesn't exist
        new File("data/results").mkdirs();
        runBenchmarkAndSaveResults();
    }
}
