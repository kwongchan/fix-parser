package io.github.kwongchan.fixparser.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ByteArrayCharSequenceTest {

    private ByteArrayCharSequence sequenceUnderTest;

    @BeforeEach
    void setUp() {
        sequenceUnderTest = new ByteArrayCharSequence();
    }

    @Test
    void when_checkLengthAndCharAt_then_returnExpectedValue() {
        byte[] data = "hello".getBytes(StandardCharsets.US_ASCII);
        sequenceUnderTest.init(data, 0, data.length);

        assertThat(sequenceUnderTest.length()).isEqualTo(5);
        assertThat(sequenceUnderTest.charAt(0)).isEqualTo('h');
        assertThat(sequenceUnderTest.charAt(1)).isEqualTo('e');
        assertThat(sequenceUnderTest.charAt(4)).isEqualTo('o');
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3})
    void when_charAtOutOfBounds_then_throwsException(int index) {
        byte[] data = "ab".getBytes(StandardCharsets.US_ASCII);
        sequenceUnderTest.init(data, 0, data.length);

        assertThatThrownBy(() -> sequenceUnderTest.charAt(index)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> sequenceUnderTest.charAt(sequenceUnderTest.length())).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void when_subSequence_then_returnNewCharSequence() {
        byte[] data = "substring".getBytes(StandardCharsets.US_ASCII);
        sequenceUnderTest.init(data, 0, data.length);

        var subSequence = sequenceUnderTest.subSequence(3, 6);
        assertThat(subSequence.length()).isEqualTo(3);
        assertThat(subSequence.charAt(0)).isEqualTo('s');
        assertThat(subSequence).hasToString("str");
    }

    @Test
    void when_subSequenceWithZeroLength_then_returnEmptyCharSequence() {
        byte[] data = "substring".getBytes(StandardCharsets.US_ASCII);
        sequenceUnderTest.init(data, 0, data.length);

        var subSequence = sequenceUnderTest.subSequence(2, 2);
        assertThat(subSequence.length()).isZero();
        assertThat(subSequence).hasToString("");
        assertThatThrownBy(() -> subSequence.charAt(0)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void when_toString_then_returnStringValue() {
        byte[] data = new byte[]{'x', 'h', 'e', 'l', 'l', 'o', 'y'};
        sequenceUnderTest.init(data, 1, 6);

        assertThat(sequenceUnderTest.length()).isEqualTo(5);
        assertThat(sequenceUnderTest).hasToString("hello");
    }

    @Test
    void when_reset_then_clearState() {
        byte[] data = "abc".getBytes(StandardCharsets.ISO_8859_1);
        sequenceUnderTest.init(data, 0, data.length);

        assertThat(sequenceUnderTest.length()).isEqualTo(3);
        sequenceUnderTest.reset();
        assertThat(sequenceUnderTest.length()).isZero();
        assertThat(sequenceUnderTest).hasToString("");
        assertThatThrownBy(() -> sequenceUnderTest.charAt(0)).isInstanceOf(IndexOutOfBoundsException.class);
    }
}
