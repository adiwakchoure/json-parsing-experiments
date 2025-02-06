# JSON Parser Performance Benchmark Experiment

## Overview
Goal is to compare the performance of various JSON parsing libraries in Java, focusing on the three key operations we need for last9 clickhouse queries:
1. JSON Validation
2. Key Existence Check
3. Value Extraction by Path

## Parsers Under Test
Chose both DOM and Streaming implementations of the fastest/most popular JSON libraries from https://github.com/fabienrenaud/java-json-benchmark 

1. **Jackson**
   - DOM (ObjectMapper)
   - Streaming (JsonParser)

2. **GSON**
   - DOM
   - Streaming

3. **FastJSON 2.x** (https://www.iteye.com/blog/wenshao-1142031)
   - DOM
   - Streaming

4. **JsonIter** (https://jsoniter.com/)
   - DOM-based implementation

## Benchmark Configuration

### JMH Settings
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 2)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Timeout(time = 10, timeUnit = TimeUnit.MINUTES)
```

### Test Data
- **Sample Size**: 100,000 JSON rows per test
- **Data Sources**:
  - Valid JSON inputs from last9 Parquet files (game logs)
  - Invalid JSON inputs from last9 Parquet files
- **Data Pattern**:
  ```json
  {
    "correlationId": "gameplayed__X54ww__1734897498952",  // present in ~59% of records
    "tm": "01:28:19.560",
    "logger": "com.games24x7.offerservice.execution.step.RTSendToConsumer",
    // ... other fields
  }
  ```
- **Test Keys/Paths**:
  - Key Check: "correlationId" (tests both presence and absence)
  - Path Extraction: "$.logger" (tests value extraction and error handling)
- **Data Distribution**:
  - ~59% of records have correlationId (tests positive case)
  - ~41% of records missing correlationId (tests negative case)
  - This distribution helps evaluate both successful parsing and error handling

## Test Cases

For each parser, we run the following benchmarks:

1. **JSON Validation Tests**
   ```java
   [parser]_ValidInputs()   // Tests isValidJson() with valid game logs
   [parser]_InvalidInputs() // Tests isValidJson() with invalid JSON
   ```

2. **Key Check Test**
   ```java
   [parser]_HasKey()        // Tests hasJsonKey() with key "correlationId"
   ```

3. **Value Extraction Test**
   ```java
   [parser]_GetValue()      // Tests getJsonValue() with path "$.logger"
   ```

## Performance Optimizations
1. **Early Exit Strategies**
   - Fast-fail validation for invalid JSON
   - Early return in key checking operations

## Expected Outcomes
The benchmark will help evaluate:
1. Performance differences between DOM and streaming approaches
2. Library-specific optimizations effectiveness
3. Trade-offs between memory usage and parsing speed
4. Impact of different JSON operations on performance

> It might make sense to have different libraries for specific JSON operators.

## JMH Setup and config
```java
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 2)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Timeout(time = 10, timeUnit = TimeUnit.MINUTES)
public class JsonParsingBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(JsonParsingBenchmark.class);
    private static final int SAMPLE_SIZE = 100000;
    
    private static List<String> validJsonInputs;
    private static List<String> invalidJsonInputs;
    private static final String jsonKey = "correlationId";
    private static final String jsonPath = "$.logger";

```