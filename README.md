# JSON Parsing Benchmark

This project benchmarks various JSON parsing libraries in Java using JMH (Java Microbenchmark Harness).

## Libraries Compared

- Jackson (DOM-based)
- Jackson (Streaming)
- simdjson-java
- jsoniter
- fastjson

## Project Structure

```
project/
├── src/         # Java source code
└── data/        # Parquet files and benchmark results
    └── charts/  # Generated performance comparison charts
```

## Prerequisites

- Java 11 or higher
- Maven
- Parquet files with JSON data in the `data/` directory

## Running the Benchmarks

1. Place your Parquet files in the `data/` directory with the following naming convention:
   - data_1k.parquet
   - data_10k.parquet
   - data_100k.parquet
   - data_1m.parquet

2. Build the project:
```bash
mvn clean package
```

3. Run the benchmarks:
```bash
java -jar target/benchmarks.jar
```

The benchmark results will be displayed in the console, and performance comparison charts will be generated in the `data/charts/` directory.

## Benchmark Configuration

- Warmup: 3 iterations, 1 second each
- Measurement: 5 iterations, 1 second each
- Fork: 1
- Mode: Average Time
- Output: Microseconds

## Adding New Parsers

To add a new JSON parser:

1. Add the dependency to `pom.xml`
2. Create a new benchmark method in `JsonParserBenchmark.java`
3. Run the benchmarks to include the new parser in the comparison
