package cz.cuni.mff.benchmark.lookup;

import com.google.common.io.Files;
import org.apache.lucene.search.suggest.BufferedInputIterator;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LookupUtils {

    private static final int SEED = 0;

    private static final int RESULT_COUNT = 10;

    private static final List<String> alphabet = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");

    private LookupUtils() {

    }

    static void build(final Lookup lookup) throws IOException {
        InputStreamReader words = new InputStreamReader(LookupUtils.class.getResourceAsStream("/words_alpha.txt"));
        Random random = new Random(SEED);

        try (BufferedReader br = new BufferedReader(words)) {

            lookup.build(new BufferedInputIterator(new InputIterator() {

                @Override
                public long weight() {
                    return random.nextInt() & Integer.MAX_VALUE;
                }

                @Override
                public BytesRef payload() {
                    return null;
                }

                @Override
                public boolean hasPayloads() {
                    return false;
                }

                @Override
                public Set<BytesRef> contexts() {
                    return null;
                }

                @Override
                public boolean hasContexts() {
                    return false;
                }

                @Override
                public BytesRef next() throws IOException {
                    String line = br.readLine();
                    if (line != null) {
                        return new BytesRef(line);
                    } else {
                        return null;
                    }
                }
            }));
        }
    }

    static Directory getTempDir() throws IOException {
        return FSDirectory.open(Files.createTempDir().toPath());
    }

    static void oneLetterPrefixLookup(final Lookup lookup) throws IOException {
        for (String letter : alphabet) {
            lookup.lookup(letter, false, RESULT_COUNT);
        }
    }

    static void twoLetterPrefixLookup(final Lookup lookup) throws IOException {
        for (String letter1 : alphabet) {
            for (String letter2 : alphabet) {
                lookup.lookup(letter1 + letter2, false, RESULT_COUNT);
            }
        }
    }

    static void nonPrefixLookup(final Lookup lookup) throws IOException {
        lookup.lookup("non", false, RESULT_COUNT);
    }

}
