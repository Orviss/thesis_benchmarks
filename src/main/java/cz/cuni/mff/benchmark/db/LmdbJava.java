package cz.cuni.mff.benchmark.db;

import org.lmdbjava.Cursor;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.GetOp;
import org.lmdbjava.Txn;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LmdbJava {

    @State(Scope.Benchmark)
    public static class MyState {

        private List<String> words = new ArrayList<>();

        int wordCount;

        private Dbi<ByteBuffer> db;

        private Env<ByteBuffer> env;

        private final Random random = new Random(MapUtils.SEED);

        @Setup(Level.Trial)
        public void setup() throws IOException {

            Path temp = Files.createTempDirectory("lmdb");

            env = Env.<ByteBuffer>create()
                    .setMapSize(400_000L * 100)
                    .setMaxDbs(1)
                    .open(temp.toFile());

            db = env.openDbi("db", DbiFlags.MDB_CREATE);

            ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
            ByteBuffer value = ByteBuffer.allocateDirect(4);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass()
                    .getResourceAsStream("/words_alpha.txt")));
                 Txn<ByteBuffer> txn = env.txnWrite()
            ) {

                Cursor<ByteBuffer> c = db.openCursor(txn);
                String line;
                while ((line = br.readLine()) != null) {

                    if (wordCount % 20 == 0) {
                        words.add(line);
                    }

                    key.clear();
                    key.put(line.getBytes(StandardCharsets.UTF_8)).flip();

                    value.clear();
                    value.putInt(random.nextInt() & Integer.MAX_VALUE).flip();

                    c.put(key, value);

                    wordCount++;
                }
                c.close();
                txn.commit();
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void test(final MyState state, final Blackhole blackhole) {
        try (Txn<ByteBuffer> txn = state.env.txnRead()) {
            ByteBuffer key = ByteBuffer.allocateDirect(state.env.getMaxKeySize());

            Cursor<ByteBuffer> c = state.db.openCursor(txn);

            for (String s : state.words) {
                key.clear();
                key.put(s.getBytes(StandardCharsets.UTF_8)).flip();
                c.get(key, GetOp.MDB_SET);

                int i = c.val().getInt();
                blackhole.consume(i);
            }

            c.close();
        }
    }

}
