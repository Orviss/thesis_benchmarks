package cz.cuni.mff.benchmark.lookup;

import org.apache.lucene.search.suggest.tst.TSTLookup;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;

public class TSTLookupBenchmark {

    @State(Scope.Benchmark)
    public static class MyState {

        private TSTLookup lookup;

        @Setup(Level.Trial)
        public void setup() throws IOException {
            lookup = new TSTLookup(LookupUtils.getTempDir(), "suffix");

            LookupUtils.build(lookup);
        }
    }

    @State(Scope.Benchmark)
    public static class MyStateLinux {

        private TSTLookup lookup;

        @Setup(Level.Trial)
        public void setup() throws IOException {
            lookup = new TSTLookup(LookupUtils.getTempDir(), "suffix");

            LookupUtils.buildLinux(lookup);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void oneLetterPrefixLookup(MyState state, Blackhole blackhole) throws IOException {
        LookupUtils.oneLetterPrefixLookup(state.lookup, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void twoLetterPrefixLookup(MyState state, Blackhole blackhole) throws IOException {
        LookupUtils.twoLetterPrefixLookup(state.lookup, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void nonPrefixLookup(MyState state, Blackhole blackhole) throws IOException {
        blackhole.consume(LookupUtils.nonPrefixLookup(state.lookup));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void buildTime(Blackhole blackhole) throws IOException {
        TSTLookup lookup = new TSTLookup(LookupUtils.getTempDir(), "suffix");
        LookupUtils.build(lookup);
        blackhole.consume(lookup);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void oneLetterPrefixLookupLinux(MyStateLinux state, Blackhole blackhole) throws IOException {
        LookupUtils.oneLetterPrefixLookup(state.lookup, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void twoLetterPrefixLookupLinux(MyStateLinux state, Blackhole blackhole) throws IOException {
        LookupUtils.twoLetterPrefixLookup(state.lookup, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void nonPrefixLookupLinux(MyStateLinux state, Blackhole blackhole) throws IOException {
        blackhole.consume(LookupUtils.nonPrefixLookup(state.lookup));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void buildTimeLinux(Blackhole blackhole) throws IOException {
        TSTLookup lookup = new TSTLookup(LookupUtils.getTempDir(), "suffix");
        LookupUtils.buildLinux(lookup);
        blackhole.consume(lookup);
    }

}
