# Simple FIX Parser
## Overview
A small, high-performance, single-threaded FIX message parser focused on low-latency and minimal allocations. The project provides a lightweight, allocation-conscious API to parse FIX messages from raw byte arrays and read common field types (string, int, long, double, boolean, datetime) with minimal overhead.

- Minimal allocations and object reuse for hot paths to suit low-latency environments.
- Simple, imperative API for extracting tag values directly from a parsed message.
- Parse raw FIX bytes into a `FIXMessage` instance and read fields via typed getters.
- Good performance for benchmarking.

## Sample Usage
```java
byte[] rawFIX = "8=FIX.4.2\u00019=12\u000135=D\u000144=38.5\u000138=1000\u000110=123\u0001".getBytes(StandardCharsets.US_ASCII);
FIXParser fixParser = new FIXParserFactory().newParser();
FIXMessage msg = fixParser.parse(rawFIX);

CharSequence beginString = msg.getCharSequence(8);
int bodyLength = msg.getInt(9);
CharSequence msgType = msg.getCharSequence(35);
double price = msg.getDouble(44);
int quantity = msg.getInt(38);
int checksum = msg.getInt(10);
```

## Limitations
- No message validation: does not verify checksum, bodyLength, required header/trailer fields, or correct field ordering.
- No repeating-group support: repeating groups (and nested groups) are not parsed.
- Limited field types: only basic parsing (string/int/long/double/boolean/date-time); special/complex types (RawData/binary, encoded fields) are not handled.
- No semantic validation: tag semantics, enum/field value validation, or FIX version-specific rules are not enforced.
- Not designed for concurrent mutation: internal data structures are not thread-safe for concurrent use.

## Benchmark
Below is the JMH Benchmark results for parsing one million FIX messages. It also includes the benchmark results of QuickFix/J and a simple `String.split()` implementation for comparison.
```
Benchmark                                                  Mode  Cnt  Score   Error  Units
FixParserBenchmark.parseOneMillionMessagesWithFIXParser    avgt   10  0.593 ± 0.013   s/op
FixParserBenchmark.parseOneMillionMessagesWithQuickFixJ    avgt   10  2.342 ± 0.356   s/op
FixParserBenchmark.parseOneMillionMessagesWithStringSplit  avgt   10  1.430 ± 0.147   s/op
```
The benchmark results show that the Simple FIX Parser significantly outperforms QuickFix/J and the `String.split()` implementation in terms of average time when parsing one million FIX messages.

The benchmark is implemented in [FixParserBenchmark](https://github.com/kwongchan/fix-parser/blob/main/src/jmh/java/io/github/kwongchan/fixparser/benchmark/FixParserBenchmark.java). To run the benchmark, you can use the following command:
```
./gradlew jmh
```
