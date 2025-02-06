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

    public static List<String> loadJsonInputs(String parquetFile, int rowCount) {
        List<String> jsonInputs = new ArrayList<>();
        try (DuckDBConnection conn = (DuckDBConnection) new DuckDBDriver().connect("jdbc:duckdb:", null)) {
            String sql = String.format("SELECT Body FROM read_parquet('%s') LIMIT %d", parquetFile, rowCount);
            ResultSet rs = conn.createStatement().executeQuery(sql);
            
            while (rs.next()) {
                String input = rs.getString("Body");
                if (input != null && !input.trim().isEmpty()) {
                    jsonInputs.add(input);
                }
            }
            
            logger.info("Loaded {} inputs from {}", jsonInputs.size(), parquetFile);
        } catch (SQLException e) {
            logger.error("Error loading data from {}: {}", parquetFile, e.getMessage());
            throw new RuntimeException("Failed to load data", e);
        }
        
        return jsonInputs;
    }

    public static List<String> loadValidJsonInputs(int count) {
        return loadJsonInputs(VALID_JSON_FILE, count);
    }

    public static List<String> loadInvalidJsonInputs(int count) {
        return loadJsonInputs(INVALID_JSON_FILE, count);
    }
}
