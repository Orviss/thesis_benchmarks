# Diploma thesis benchmarks

## Topic: Suggester implementation for the OpenGrok search engine

## Usage

### How to build

```bash
mvn clean package
```

### Run all benchmarks
```bash
java -jar target/benchmarks.jar
```

### Run specific benchmarks
```bash
# an example, change BENCHMARK_NAMES variable to include the tests you want to run
BENCHMARK_NAMES="cz.cuni.mff.benchmark.lookup.WFSTLookupBenchmark.nonPrefixLookup"
java -jar target/benchmarks.jar ${BENCHMARK_NAMES}
```