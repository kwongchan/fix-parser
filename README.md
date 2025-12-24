# Simple FIX Parser
## Overview

## Limitation
- No FIX message validation
- Repeating Group is not supported

## Improvement

## Benchmark
```
Benchmark                                                   Mode  Cnt  Score   Error  Units
FixParserBenchmark.parseOneMillionMessagesWithFIXParser    thrpt   10  1.663 ± 0.044  ops/s
FixParserBenchmark.parseOneMillionMessagesWithQuickFixJ    thrpt   10  0.129 ± 0.017  ops/s
FixParserBenchmark.parseOneMillionMessagesWithStringSplit  thrpt   10  0.678 ± 0.132  ops/s
```