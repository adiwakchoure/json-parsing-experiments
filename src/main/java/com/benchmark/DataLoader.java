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
    private static final String PARQUET_FILE = "data/input.parquet";

    public static List<String> loadJsonInputs(int count) {
        List<String> jsonInputs = new ArrayList<>();
        
        try (DuckDBConnection conn = (DuckDBConnection) new DuckDBDriver().connect("jdbc:duckdb:", null)) {
            // Read only the Body column and ensure it's not null
            String query = String.format(
                "SELECT Body FROM read_parquet('%s') WHERE Body IS NOT NULL LIMIT %d",
                PARQUET_FILE, count
            );
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                String json = rs.getString("Body");
                if (json != null && !json.trim().isEmpty()) {
                    jsonInputs.add(json);
                }
            }
            
            logger.info("Successfully loaded {} valid JSON records", jsonInputs.size());
        } catch (SQLException e) {
            logger.error("Error loading data from Parquet: {}", e.getMessage());
            throw new RuntimeException("Failed to load data", e);
        }
        
        return jsonInputs;
    }
}
