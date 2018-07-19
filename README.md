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

**Note:** a path to indexed Linux project must be changed in `LookupUtils#PATH_TO_LINUX_INDEX`.

### Run specific benchmarks
```bash
# an example, change BENCHMARK_NAMES variable to include the tests you want to run
BENCHMARK_NAMES="cz.cuni.mff.benchmark.lookup.WFSTLookupBenchmark.nonPrefixLookup"
java -jar target/benchmarks.jar ${BENCHMARK_NAMES}
```

### Java 9+ 
Add following arguments to `java` invocation:

```bash
--add-exports java.base/jdk.internal.ref=ALL-UNNAMED
--add-exports java.base/jdk.internal.misc=ALL-UNNAMED
--add-exports java.base/sun.nio.ch=ALL-UNNAMED
```