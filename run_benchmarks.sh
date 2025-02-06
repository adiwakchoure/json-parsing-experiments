#!/bin/bash

echo "=== Running benchmarks in normal mode (all JSON values) ==="
java -jar target/benchmarks.jar

echo -e "\n=== Running benchmarks in strict mode (only objects/arrays) ==="
java -jar target/benchmarks.jar -p strictMode=true
