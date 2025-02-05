package com.benchmark.json;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openjdk.jmh.results.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class BenchmarkResultVisualizer {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkResultVisualizer.class);
    private static final String CHARTS_DIR = "data/charts/";

    public static void createCharts(Collection<RunResult> results) {
        try {
            new File(CHARTS_DIR).mkdirs();
            createBarChart(results);
        } catch (IOException e) {
            logger.error("Failed to create charts", e);
        }
    }

    private static void createBarChart(Collection<RunResult> results) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (RunResult result : results) {
            String benchmark = result.getParams().getBenchmark();
            String parser = benchmark.substring(benchmark.lastIndexOf('.') + 1);
            String dataSize = result.getParams().getParam("dataSize");
            double score = result.getPrimaryResult().getScore();
            
            dataset.addValue(score, parser, dataSize);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "JSON Parser Performance Comparison",
                "Data Size",
                "Average Time (microseconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        ChartUtils.saveChartAsPNG(
                new File(CHARTS_DIR + "performance_comparison.png"),
                chart,
                800,
                600
        );
    }
}
