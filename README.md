# JSON Parser Benchmarking

A comprehensive benchmarking framework comparing popular Java JSON parsers.

## Parsers Tested

### DOM-based Parsers
- **Jackson ObjectMapper**: Full DOM parsing with tree traversal
  - Pros: Easy to use, mature API
  - Cons: Higher memory usage for large JSON
- **GSON**: Google's JSON parser with object mapping
  - Pros: Clean API, good for Java objects
  - Cons: No streaming support
- **FastJSON**: Alibaba's high-performance JSON library
  - Pros: Very fast for small JSON
  - Cons: Less mature than Jackson/GSON
- **SimdJSON**: SIMD-accelerated parser (currently disabled)
  - Pros: Hardware acceleration
  - Cons: Requires JDK Vector API support

### Streaming Parsers
- **Jackson JsonParser**: Event-based streaming parser
  - Pros: Memory efficient, early exit
  - Cons: More complex API
- **FastJSON JSONReader**: Streaming implementation
  - Pros: Fast, supports early exit
  - Cons: Less documented API
- **JsonIterator**: High-performance streaming parser
  - Pros: Very fast for large JSON
  - Cons: Limited documentation

## Test Data
- 100k valid JSON records
- 100k invalid JSON records (log lines, malformed JSON)
- Data stored in Parquet format
- Loaded via DuckDB for efficiency

## Benchmark Types

Each parser is tested with three scenarios:
1. `isValidJson` with valid inputs (best case)
2. `isValidJson` with invalid inputs (error handling)
3. `hasJsonKey` with valid inputs (key lookup)

## Implementation Details

- **JMH Settings**:
  - Warmup: 2 iterations, 2s each
  - Measurement: 3 iterations, 3s each
  - Fork: 1
  - Mode: Average Time (ms)

- **Validation Modes**:
  - Normal mode: Accepts any valid JSON value (string, number, boolean, null, object, array)
  - Strict mode: Only accepts objects and arrays as valid JSON (`-p strictMode=true`)

- **Optimizations**:
  - Early exit on invalid JSON
  - Proper resource cleanup
  - Blackhole consumption
  - Static parser instances

## Running Benchmarks

```bash
# Run in normal mode (accepts all JSON values)
mvn clean package
java -jar target/benchmarks.jar

# Run in strict mode (only objects/arrays)
java -jar target/benchmarks.jar -p strictMode=true
```

Results show average processing time in milliseconds. Lower is better.
