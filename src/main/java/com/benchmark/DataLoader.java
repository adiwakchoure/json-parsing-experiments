package com.benchmark;

import org.duckdb.DuckDBConnection;
import org.duckdb.DuckDBDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final String VALID_JSON_FILE = "data/100k_valid_json.parquet";
    private static final String INVALID_JSON_FILE = "data/100k_invalid_json.parquet";

    public static List<String> loadValidJsonInputs(int count) {
        return loadFromParquet(VALID_JSON_FILE, count);
    }

    public static List<String> loadInvalidJsonInputs(int count) {
        return loadFromParquet(INVALID_JSON_FILE, count);
    }

    private static List<String> loadFromParquet(String file, int count) {
        List<String> inputs = new ArrayList<>();
        
        try (DuckDBConnection conn = (DuckDBConnection) new DuckDBDriver().connect("jdbc:duckdb:", null)) {
            String query = String.format(
                "SELECT Body FROM read_parquet('%s') LIMIT %d",
                file, count
            );
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                String input = rs.getString("Body");
                if (input != null && !input.trim().isEmpty()) {
                    inputs.add(input);
                }
            }
            
            logger.info("Loaded {} inputs from {}", inputs.size(), file);
            
        } catch (SQLException e) {
            logger.error("Error loading data from {}: {}", file, e.getMessage());
            throw new RuntimeException("Failed to load data", e);
        }
        
        return inputs;
    }
}
