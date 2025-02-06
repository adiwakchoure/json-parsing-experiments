Now the benchmark is clearly structured with 6 tests:

isValidJson tests (both DOM and streaming):
Test with valid JSON inputs (best case)
Test with invalid JSON inputs (worst case)
Uses both Parquet files as source of truth
Success = parsing all valid JSON and failing all invalid JSON
hasJsonKey tests (both DOM and streaming):
Only test with valid JSON inputs
Checks for common fields in our log data (level and message/msg)
Reports count of how many entries have these fields
