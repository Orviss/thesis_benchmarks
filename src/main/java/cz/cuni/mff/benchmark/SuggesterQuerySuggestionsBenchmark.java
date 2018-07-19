package cz.cuni.mff.benchmark;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.opengrok.suggest.SuggesterSearcher;
import org.opengrok.suggest.query.SuggesterPhraseQuery;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class SuggesterQuerySuggestionsBenchmark {

    public static Path sourcePath;

    @State(Scope.Benchmark)
    public static class MyState {

        public SuggesterSearcher searcher;

        private Path dataRoot;

        private IndexReader reader;

        @Setup(Level.Trial)
        public void setup() throws IOException {
            dataRoot = Files.createTempDirectory("data");
            sourcePath = Files.createTempDirectory("source");

            ZipUtil.unpack(getClass().getClassLoader().getResourceAsStream("data.zip"), dataRoot.toFile());
            ZipUtil.unpack(getClass().getClassLoader().getResourceAsStream("source.zip"), sourcePath.toFile());

            reader = DirectoryReader.open(FSDirectory.open(Paths.get(dataRoot.toString(), "index", "fruitonserver")));

            searcher = new SuggesterSearcher(reader, 10);
        }

        @TearDown
        public void tearDown() throws IOException {
            reader.close();

            FileUtils.deleteDirectory(dataRoot.toFile());
            FileUtils.deleteDirectory(sourcePath.toFile());
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void suggesterPhraseQuerySmall(final MyState state, final Blackhole blackhole) {
        SuggesterPhraseQuery q = new SuggesterPhraseQuery("full", "kbcf", Arrays.asList("notnull", "kbcf"), 0);
        blackhole.consume(state.searcher.suggest(q.getPhraseQuery(), "fruitonserver", q.getSuggesterQuery(), k -> 0));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void suggesterPhraseQuerySmallDisk(final MyState state, final Blackhole blackhole) {
        SuggesterPhraseQuery q = new SuggesterPhraseQuery("full", "kbcf", Arrays.asList("notnull", "kbcf"), 0);
        blackhole.consume(state.searcher.suggestDisk(q.getPhraseQuery(), "fruitonserver", q.getSuggesterQuery(), k -> 0));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void suggesterPhraseQueryLarge(final MyState state, final Blackhole blackhole) {
        SuggesterPhraseQuery q = new SuggesterPhraseQuery("full", "kbcf", Arrays.asList(".", "kbcf"), 0);
        blackhole.consume(state.searcher.suggest(q.getPhraseQuery(), "fruitonserver", q.getSuggesterQuery(), k -> 0));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void suggesterPhraseQueryLargeDisk(final MyState state, final Blackhole blackhole) {
        SuggesterPhraseQuery q = new SuggesterPhraseQuery("full", "kbcf", Arrays.asList(".", "kbcf"), 0);
        blackhole.consume(state.searcher.suggestDisk(q.getPhraseQuery(), "fruitonserver", q.getSuggesterQuery(), k -> 0));
    }

}
