package cz.cuni.mff.benchmark.db;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapDBBenchmark {

    @State(Scope.Benchmark)
    public static class MyState {

        private List<String> words = new ArrayList<>();

        private int wordCount;

        private HTreeMap<String, Integer> map;

        private final Random random = new Random(MapUtils.SEED);

        @Setup(Level.Trial)
        public void setup() throws IOException {

            Path temp = Files.createTempFile("mapdb", "suffix");

            temp.toFile().delete();

            DB db = DBMaker.fileDB(temp.toFile())
                    .fileMmapEnable()
                    .transactionEnable()
                    .make();

            map = db.hashMap("test", Serializer.STRING, Serializer.INTEGER).valueLoader(s -> 0).createOrOpen();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/words_alpha.txt")))) {

                String line;
                while ((line = br.readLine()) != null) {
                    if (wordCount % 20 == 0) {
                        words.add(line);
                    }

                    map.put(line, random.nextInt() & Integer.MAX_VALUE);

                    wordCount++;
                }
            }

            db.commit();
        }
    }

    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    public void test(final MyState state, final Blackhole blackhole) {
        for (String word : state.words) {
            int val = state.map.get(word);
            blackhole.consume(val);
        }
    }

}
