package com.benchmark;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openjdk.jmh.results.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
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
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color VALID_COLOR = new Color(46, 139, 87);  // SeaGreen
    private static final Color INVALID_COLOR = new Color(220, 20, 60);  // Crimson
    private static final Color[] SERIES_COLORS = {
        VALID_COLOR,  // Blue
        INVALID_COLOR,   // Red
        new Color(39, 174, 96),   // Green
        new Color(142, 68, 173)   // Purple
    };

    public static void createCharts(Collection<RunResult> results) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path resultsDir = Paths.get("data", "results", timestamp);
        try {
            Files.createDirectories(resultsDir);
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

            if (method.contains("IsValidJson")) {
                datasets.get("validation_time").addValue(score, method, String.valueOf(rowCount));
            } else if (method.contains("HasJsonKey")) {
                datasets.get("key_check_time").addValue(score, method, String.valueOf(rowCount));
            }
        }

        // Extract validation counts from the first result
        int validCount = -1;
        int keyCount = -1;
        for (RunResult result : results) {
            String benchmark = result.getParams().getBenchmark();
            if (benchmark.contains("jacksonDomIsValidJson")) {
                validCount = (int) result.getPrimaryResult().getScore();
            } else if (benchmark.contains("jacksonDomHasJsonKey")) {
                keyCount = (int) result.getPrimaryResult().getScore();
            }
        }

        // Create and save charts
        String fullValidationTitle = "JSON Validation Time by Method";
        String fullKeyCheckTitle = "JSON Key Check Time by Method";
        if (validCount >= 0 && keyCount >= 0) {
            fullValidationTitle += String.format(" (Valid: %d, With Key: %d)", validCount, keyCount);
            fullKeyCheckTitle += String.format(" (Valid: %d, With Key: %d)", validCount, keyCount);
        }
        createBarChart(datasets.get("validation_time"), 
            fullValidationTitle, 
            resultsDir.resolve("validation_time_bar.png").toString());
        createLineChart(datasets.get("validation_time"),
            "JSON Validation Time Trend",
            resultsDir.resolve("validation_time_line.png").toString());
            
        createBarChart(datasets.get("key_check_time"),
            fullKeyCheckTitle,
            resultsDir.resolve("key_check_time_bar.png").toString());
        createLineChart(datasets.get("key_check_time"),
            "JSON Key Check Time Trend",
            resultsDir.resolve("key_check_time_line.png").toString());
    }

    private static void createBarChart(DefaultCategoryDataset dataset, String title, String outputPath) {
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            "Input Size (rows)",
            "Time (ms)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        customizePlot(plot);

        // Customize bars
        BarRenderer renderer = new BarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        for (int i = 0; i < SERIES_COLORS.length; i++) {
            renderer.setSeriesPaint(i, SERIES_COLORS[i]);
        }
        plot.setRenderer(renderer);

        saveChart(chart, outputPath);
    }

    private static void createLineChart(DefaultCategoryDataset dataset, String title, String outputPath) {
        JFreeChart chart = ChartFactory.createLineChart(
            title,
            "Input Size (rows)",
            "Time (ms)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        customizePlot(plot);

        // Customize lines
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultStroke(new BasicStroke(2.0f));
        for (int i = 0; i < SERIES_COLORS.length; i++) {
            renderer.setSeriesPaint(i, SERIES_COLORS[i]);
            if (i == 0) {
                renderer.setSeriesStroke(i, new BasicStroke(2.0f));
            } else if (i == 1) {
                renderer.setSeriesStroke(i, new BasicStroke(
                    2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                    10.0f, new float[]{10.0f, 5.0f}, 0.0f
                ));
            } else {
                renderer.setSeriesStroke(i, new BasicStroke(2.0f));
            }
            renderer.setSeriesShape(i, new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));
        }
        plot.setRenderer(renderer);

        saveChart(chart, outputPath);
    }

    private static void customizePlot(CategoryPlot plot) {
        // Set background
        plot.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setDomainGridlinesVisible(false);

        // Customize axes
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        // Customize legend
        LegendTitle legend = plot.getChart().getLegend();
        legend.setBackgroundPaint(BACKGROUND_COLOR);
        legend.setItemFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private static void saveChart(JFreeChart chart, String outputPath) {
        chart.setBackgroundPaint(BACKGROUND_COLOR);
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
