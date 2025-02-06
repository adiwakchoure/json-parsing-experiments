package com.benchmark;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;  // Updated import
import org.openjdk.jmh.results.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResultVisualizer {
    private static final Logger logger = LoggerFactory.getLogger(ResultVisualizer.class);
    private static final int CHART_WIDTH = 1600;
    private static final int CHART_HEIGHT = 900;

    // Parser colors - using a more distinct color palette
    private static final Color[] PARSER_COLORS = {
        new Color(41, 128, 185),  // Blue (Jackson)
        new Color(39, 174, 96),   // Green (GSON)
        new Color(192, 57, 43),   // Red (FastJSON)
        new Color(142, 68, 173)  // Purple (JsonIterator)
    };

    // Task type patterns
    private static final float[][] TASK_PATTERNS = {
        {10.0f, 0.0f},           // Solid (Valid JSON)
        {6.0f, 6.0f},            // Hatched (Invalid JSON)
        {2.0f, 2.0f, 5.0f, 2.0f} // Dash-dot (Key Check)
    };

    private static final Map<String, String> METHOD_ALIASES = new HashMap<String, String>() {{
        put("domIsValidJson_ValidInputs", "Jackson DOM (Valid)");
        put("domIsValidJson_InvalidInputs", "Jackson DOM (Invalid)");
        put("domHasJsonKey_ValidInputs", "Jackson DOM (Key Check)");
        put("streamingIsValidJson_ValidInputs", "Jackson Streaming (Valid)");
        put("streamingIsValidJson_InvalidInputs", "Jackson Streaming (Invalid)");
        put("streamingHasJsonKey_ValidInputs", "Jackson Streaming (Key Check)");
        put("gsonDomIsValidJson_ValidInputs", "GSON DOM (Valid)");
        put("gsonDomIsValidJson_InvalidInputs", "GSON DOM (Invalid)");
        put("gsonDomHasJsonKey_ValidInputs", "GSON DOM (Key Check)");
        put("gsonStreamingIsValidJson_ValidInputs", "GSON Streaming (Valid)");
        put("gsonStreamingIsValidJson_InvalidInputs", "GSON Streaming (Invalid)");
        put("gsonStreamingHasJsonKey_ValidInputs", "GSON Streaming (Key Check)");
        put("fastjsonDomIsValidJson_ValidInputs", "FastJSON DOM (Valid)");
        put("fastjsonDomIsValidJson_InvalidInputs", "FastJSON DOM (Invalid)");
        put("fastjsonDomHasJsonKey_ValidInputs", "FastJSON DOM (Key Check)");
        put("fastjsonStreamingIsValidJson_ValidInputs", "FastJSON Streaming (Valid)");
        put("fastjsonStreamingIsValidJson_InvalidInputs", "FastJSON Streaming (Invalid)");
        put("fastjsonStreamingHasJsonKey_ValidInputs", "FastJSON Streaming (Key Check)");
        put("jsoniterIsValidJson_ValidInputs", "JsonIterator (Valid)");
        put("jsoniterIsValidJson_InvalidInputs", "JsonIterator (Invalid)");
        put("jsoniterHasJsonKey_ValidInputs", "JsonIterator (Key Check)");
    }};

    private static String getMethodAlias(String methodName) {
        return METHOD_ALIASES.getOrDefault(methodName, methodName);
    }

    private static int getParserColorIndex(String methodName) {
        if (methodName.toLowerCase().contains("jackson")) return 0;
        if (methodName.toLowerCase().contains("gson")) return 1;
        if (methodName.toLowerCase().contains("fastjson")) return 2;
        if (methodName.toLowerCase().contains("jsoniter")) return 3;
        return 0; // Default color
    }

    private static float[] getTaskPattern(String methodName) {
        if (methodName.toLowerCase().contains("_valid")) return TASK_PATTERNS[0];
        if (methodName.toLowerCase().contains("_invalid")) return TASK_PATTERNS[1];
        if (methodName.toLowerCase().contains("hasjsonkey")) return TASK_PATTERNS[2];
        return TASK_PATTERNS[0]; // Default to solid line
    }

    public static void createCharts(Collection<RunResult> results) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path resultsDir = Paths.get("data", "results", timestamp);
        try {
            Files.createDirectories(resultsDir);
            logger.info("Created results directory: {}", resultsDir);
        } catch (IOException e) {
            logger.error("Failed to create results directory: {}", e.getMessage());
            return;
        }

        Map<String, DefaultCategoryDataset> datasets = new HashMap<>();
        datasets.put("validation_time", new DefaultCategoryDataset());
        datasets.put("key_check_time", new DefaultCategoryDataset());

        // Populate datasets
        for (RunResult result : results) {
            String benchmark = result.getParams().getBenchmark();
            String method = benchmark.substring(benchmark.lastIndexOf('.') + 1);
            int rowCount = Integer.parseInt(result.getParams().getParam("rowCount"));
            double score = result.getPrimaryResult().getScore();

            logger.info("Processing benchmark result: method={}, rowCount={}, score={}", method, rowCount, score);

            if (method.toLowerCase().contains("isvalidjson")) {
                datasets.get("validation_time").addValue(score, getMethodAlias(method), String.valueOf(rowCount));
                logger.info("Added to validation_time dataset: {}", method);
            } else if (method.toLowerCase().contains("hasjsonkey")) {
                datasets.get("key_check_time").addValue(score, getMethodAlias(method), String.valueOf(rowCount));
                logger.info("Added to key_check_time dataset: {}", method);
            } else {
                logger.warn("Benchmark method {} did not match any known patterns", method);
            }
        }

        // Create and save charts for each dataset
        for (Map.Entry<String, DefaultCategoryDataset> entry : datasets.entrySet()) {
            String datasetName = entry.getKey();
            DefaultCategoryDataset dataset = entry.getValue();
            String title = datasetName.equals("validation_time") ? 
                "JSON Validation Time by Parser and Input Size" :
                "JSON Key Check Time by Parser and Input Size";

            // Bar charts (normal and log scale)
            createBarChart(dataset, title, 
                Paths.get(resultsDir.toString(), datasetName + "_bar.png").toString(), false);
            createBarChart(dataset, title + " (Log Scale)", 
                Paths.get(resultsDir.toString(), datasetName + "_bar_log.png").toString(), true);

            // Line charts (normal and log scale)
            createLineChart(dataset, title,
                Paths.get(resultsDir.toString(), datasetName + "_line.png").toString(), false);
            createLineChart(dataset, title + " (Log Scale)",
                Paths.get(resultsDir.toString(), datasetName + "_line_log.png").toString(), true);
        }
    }

    private static void createBarChart(DefaultCategoryDataset dataset, String title, String outputPath, boolean useLogScale) {
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            "Input Size (number of JSON objects)",
            "Time per Operation (ms)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        customizePlot(plot);

        if (useLogScale) {
            LogAxis logAxis = new LogAxis("Time per Operation (ms)");
            logAxis.setBase(10);
            plot.setRangeAxis(logAxis);
        }

        // Customize renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        
        int seriesCount = plot.getDataset().getRowCount();
        for (int i = 0; i < seriesCount; i++) {
            String methodName = plot.getDataset().getRowKey(i).toString();
            int colorIndex = getParserColorIndex(methodName);
            float[] pattern = getTaskPattern(methodName);
            
            renderer.setSeriesPaint(i, PARSER_COLORS[colorIndex]);
            if (pattern == TASK_PATTERNS[1]) {
                renderer.setSeriesOutlinePaint(i, PARSER_COLORS[colorIndex]);
                renderer.setSeriesOutlineStroke(i, new BasicStroke(2.0f));
                renderer.setSeriesShape(i, new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0));
                renderer.setSeriesStroke(i, new BasicStroke(
                    2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[] {6.0f, 6.0f}, 0.0f
                ));
            }
        }

        saveChart(chart, outputPath);
    }

    private static void createLineChart(DefaultCategoryDataset dataset, String title, String outputPath, boolean useLogScale) {
        JFreeChart chart = ChartFactory.createLineChart(
            title,
            "Input Size (number of JSON objects)",
            "Time per Operation (ms)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        customizePlot(plot);

        if (useLogScale) {
            LogAxis logAxis = new LogAxis("Time per Operation (ms)");
            logAxis.setBase(10);
            plot.setRangeAxis(logAxis);
        }

        // Make lines thicker and add markers
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultStroke(new BasicStroke(2.0f));
        renderer.setDefaultShapesVisible(true);
        renderer.setAutoPopulateSeriesShape(false);
        renderer.setDefaultShape(new Ellipse2D.Double(-3, -3, 6, 6));

        saveChart(chart, outputPath);
    }

    private static void customizePlot(CategoryPlot plot) {
        // Set background to white (no background color)
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));

        // Customize domain axis (x-axis)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));

        // Customize range axis (y-axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));

        // Customize legend
        LegendTitle legend = plot.getChart().getLegend();
        legend.setItemFont(new Font("SansSerif", Font.PLAIN, 12));
        legend.setBackgroundPaint(Color.WHITE);
        legend.setBorder(1.0, 1.0, 1.0, 1.0);
        legend.setPadding(10, 10, 10, 10);
        legend.setPosition(RectangleEdge.BOTTOM); // Set legend position to bottom
    }

    private static void saveChart(JFreeChart chart, String outputPath) {
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));

        try {
            ChartUtils.saveChartAsPNG(
                new File(outputPath),
                chart,
                CHART_WIDTH,
                CHART_HEIGHT
            );
            logger.info("Chart saved to: {}", outputPath);
        } catch (IOException e) {
            logger.error("Error saving chart: {}", e.getMessage());
        }
    }
}
