package cz.cuni.mff.benchmark.lookup;

import com.google.common.io.Files;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.BufferedInputIterator;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.openjdk.jmh.infra.Blackhole;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LookupUtils {

    private static final String PATH_TO_LINUX_INDEX = "/Users/adamhornacek/OpengrokDataLarge/index/linux";

    private static final int SEED = 0;

    private static final int RESULT_COUNT = 10;

    private static final int MAXIMUM_TERM_SIZE = Short.MAX_VALUE - 3;

    private static final int DEFAULT_WEIGHT = 0;

    private static final int NORMALIZED_DOCUMENT_FREQUENCY_MULTIPLIER = 1000;

    private static final List<String> alphabet = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");

    private LookupUtils() {

    }

    public static void build(final Lookup lookup) throws IOException {
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

    public static void buildLinux(final Lookup lookup) throws IOException {
        try (IndexReader indexReader = DirectoryReader.open(FSDirectory.open(
                new File(PATH_TO_LINUX_INDEX).toPath()))) {

            InputIterator iterator = new MyInputIterator(
                    new LuceneDictionary(indexReader, "full").getEntryIterator(), indexReader, "full");

            lookup.build(iterator);
        }
    }

    public static Directory getTempDir() throws IOException {
        return FSDirectory.open(Files.createTempDir().toPath());
    }

    static void oneLetterPrefixLookup(final Lookup lookup, final Blackhole blackhole) throws IOException {
        for (String letter : alphabet) {
            List<Lookup.LookupResult> l = lookup.lookup(letter, false, RESULT_COUNT);
            blackhole.consume(l);
        }
    }

    static void twoLetterPrefixLookup(final Lookup lookup, final Blackhole blackhole) throws IOException {
        for (String letter1 : alphabet) {
            for (String letter2 : alphabet) {
                List<Lookup.LookupResult> l = lookup.lookup(letter1 + letter2, false, RESULT_COUNT);
                blackhole.consume(l);
            }
        }
    }

    static List<Lookup.LookupResult> nonPrefixLookup(final Lookup lookup) throws IOException {
        return lookup.lookup("non", false, RESULT_COUNT);
    }

    private static class MyInputIterator implements InputIterator {

        private final InputIterator wrapped;

        private final IndexReader indexReader;

        private final String field;

        MyInputIterator(final InputIterator wrapped, final IndexReader indexReader, final String field) {
            this.wrapped = wrapped;
            this.indexReader = indexReader;
            this.field = field;
        }

        private BytesRef last;

        @Override
        public long weight() {
            try {
                if (last != null) {
                    return computeWeight(indexReader, field, last);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            return DEFAULT_WEIGHT;
        }

        static long computeWeight(final IndexReader indexReader, final String field, final BytesRef bytesRef)
                throws IOException {
            Term term = new Term(field, bytesRef);
            double normalizedDocumentFrequency = computeNormalizedDocumentFrequency(indexReader, term);

            return (long) (normalizedDocumentFrequency * NORMALIZED_DOCUMENT_FREQUENCY_MULTIPLIER);
        }

        private static double computeNormalizedDocumentFrequency(final IndexReader indexReader, final Term term)
                throws IOException {
            int documentFrequency = indexReader.docFreq(term);

            return ((double) documentFrequency) / indexReader.numDocs();
        }

        @Override
        public BytesRef payload() {
            return wrapped.payload();
        }

        @Override
        public boolean hasPayloads() {
            return wrapped.hasPayloads();
        }

        @Override
        public Set<BytesRef> contexts() {
            return wrapped.contexts();
        }

        @Override
        public boolean hasContexts() {
            return wrapped.hasContexts();
        }

        @Override
        public BytesRef next() throws IOException {
            last = wrapped.next();

            // skip very large terms because of the buffer exception
            while (last != null && last.length > MAXIMUM_TERM_SIZE) {
                last = wrapped.next();
            }

            return last;
        }
    }

}
