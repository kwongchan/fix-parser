package io.github.kwongchan.fixparser;

import io.github.kwongchan.fixparser.utils.ByteArrayCharSequence;
import io.github.kwongchan.fixparser.utils.FIXValueParser;
import io.github.kwongchan.fixparser.utils.ObjectPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class FIXMessageImplTest {

    private List<ByteArrayCharSequence> releasedCharSequences;
    private FIXMessageImpl messageUnderTest;

    @BeforeEach
    void setUp() {
        var valueParser = new FIXValueParser();
        releasedCharSequences = new ArrayList<>();
        // anonymous pool implementation used for tests; records released instances
        var testPool = new ObjectPool<>(ByteArrayCharSequence::new, 8) {
            @Override
            public ByteArrayCharSequence acquire() {
                // create fresh instance per acquire
                return new ByteArrayCharSequence();
            }

            @Override
            public void release(ByteArrayCharSequence instance) {
                releasedCharSequences.add(instance);
            }
        };
        messageUnderTest = new FIXMessageImpl(valueParser, testPool, 8);
    }

    @Test
    void when_getString_then_returnValue() {
        byte[] bytes = "55=SYM\u0001".getBytes();
        messageUnderTest.init(bytes);
        messageUnderTest.addFields(55, 3, 6);
        assertThat(messageUnderTest.hasTag(55)).isTrue();
        assertThat(messageUnderTest.getString(55)).hasToString("SYM");
    }

    @Test
    void when_getInt_then_returnValue() {
        byte[] intBytes = "38=100\u0001".getBytes();
        messageUnderTest.init(intBytes);
        messageUnderTest.addFields(38, 3, 6);
        assertThat(messageUnderTest.getInt(38)).isEqualTo(100);
    }

    @Test
    void when_getLong_then_returnValue() {
        byte[] longBytes = "9000=1234567890123\u0001".getBytes();
        messageUnderTest.init(longBytes);
        messageUnderTest.addFields(9000, 5, 18);
        assertThat(messageUnderTest.getLong(9000)).isEqualTo(1_234_567_890_123L);
    }

    @Test
    void when_getDouble_then_returnValue() {
        byte[] dblBytes = "44=99.99\u0001".getBytes();
        messageUnderTest.init(dblBytes);
        messageUnderTest.addFields(44, 3, 8);
        assertThat(messageUnderTest.getDouble(44)).isCloseTo(99.99, offset(1e-9));
    }

    @Test
    void when_getBoolean_then_returnValue() {
        byte[] boolBytesY = "150=Y\u0001".getBytes();
        messageUnderTest.init(boolBytesY);
        messageUnderTest.addFields(150, 4, 5);
        assertThat(messageUnderTest.getBoolean(150)).isTrue();

        byte[] boolBytesN = "150=N\u0001".getBytes();
        messageUnderTest.init(boolBytesN);
        messageUnderTest.addFields(150, 4, 5);
        assertThat(messageUnderTest.getBoolean(150)).isFalse();
    }

    @Test
    void when_getDateTimeEpochMillis_then_returnValue() {
        String ts = "20230101-00:00:00.123";
        byte[] bytes = ("60=" + ts + "\u0001").getBytes();
        messageUnderTest.init(bytes);

        // start after "60="
        messageUnderTest.addFields(60, 3, 3 + ts.length());
        long actual = messageUnderTest.getDateTimeEpochMillis(60);

        // compute expected using java.time with support for 1..3 fractional digits
        long expected = LocalDateTime.parse(
                ts,
                new DateTimeFormatterBuilder()
                        .appendPattern("yyyyMMdd-HH:mm:ss[.SSS]")
                        .optionalStart()
                        .optionalEnd()
                        .toFormatter()
        ).toInstant(ZoneOffset.UTC).toEpochMilli();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void when_reset_then_releasesAllAcquiredCharSequencesAndClearsFields() {
        byte[] bytes = ("55=ABC\u000138=100\u0001").getBytes();
        messageUnderTest.init(bytes);

        // add two fields
        messageUnderTest.addFields(55, 3, 6);   // "ABC"
        messageUnderTest.addFields(38, 10, 13); // "100"

        // before reset, fields present
        assertThat(messageUnderTest.hasTag(55)).isTrue();
        assertThat(messageUnderTest.hasTag(38)).isTrue();
        assertThat(releasedCharSequences).isEmpty();

        messageUnderTest.reset();

        // after reset, fields cleared and pool released both acquired char sequences
        assertThat(messageUnderTest.hasTag(55)).isFalse();
        assertThat(messageUnderTest.hasTag(38)).isFalse();
        // released should contain two items
        assertThat(releasedCharSequences).hasSize(2);
        // getString should return null for missing tag
        assertThat(messageUnderTest.getString(55)).isNull();
    }
}
