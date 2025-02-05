# JSON Parsing Benchmark

This project benchmarks different JSON parsing approaches using Jackson, focusing on comparing DOM vs Streaming parsing performance.

## Project Structure
```
project/
├── src/        
│   └── main/java/  # Source code
│       └── com/benchmark/
│           ├── JsonParsingBenchmark.java  # Main benchmark class
│           ├── DataLoader.java            # DuckDB data loading
│           └── ResultVisualizer.java      # Performance visualization
└── data/          
    ├── input.parquet  # JSON strings in 'Body' column
    └── results/       # Benchmark outputs
```

## Prerequisites
- Java 17 or higher
- Maven
- Input Parquet file with JSON data in 'Body' column

## Building and Running

1. Build the project:
```bash
mvn clean package
```

2. Run the benchmarks:
```bash
java -jar target/benchmarks.jar
```

## Benchmark Details

The project compares two JSON parsing approaches:
1. DOM Parsing (using JsonNode)
2. Streaming Parsing (using JsonParser)

Operations benchmarked:
- `isValidJSON`: Validates if input is valid JSON
- `hasKey`: Searches for a specific key in the JSON

## Results
Performance results are saved as PNG charts in the `data/results` directory.
