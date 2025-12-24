package io.github.kwongchan.fixparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FIXParserImplTest {

    private FIXParser parserUnderTest;

    @BeforeEach
    void setUp() {
        FIXParserFactory factory = new FIXParserFactory();
        parserUnderTest = factory.newParser();
    }

    @Test
    void when_parseWithoutRange_then_returnMessage() {
        String msg = "8=FIX.4.4\u00019=12\u000135=D\u000144=120.5\u000138=5000\u000110=123\u0001";
        byte[] data = msg.getBytes(StandardCharsets.US_ASCII);
        var result = parserUnderTest.parse(data);
        assertFIXMessage(result, "FIX.4.4", 120.5, 5000);
    }

    @Test
    void when_parseWithRange_then_returnMessage() {
        String msg = "8=FIX.4.2\u00019=12\u000135=D\u000144=38.5\u000138=1000\u000110=123\u00018=FIX.4.4\u00019=12\u000135=D\u000144=120.5\u000138=5000\u000110=123\u0001";
        byte[] data = msg.getBytes(StandardCharsets.US_ASCII);
        var result = parserUnderTest.parse(data, 0, 43);
        assertFIXMessage(result, "FIX.4.2", 38.5, 1000);
    }

    private void assertFIXMessage(FIXMessage message, String fixVersion, double price, int quantity) {
        assertThat(message.getString(8)).hasToString(fixVersion);
        assertThat(message.getDouble(44)).isEqualTo(price);
        assertThat(message.getInt(38)).isEqualTo(quantity);
    }

    @Test
    void given_messageWithMissingEquals_when_parse_then_throwsException() {
        String msg = "8\u0001"; // no '=' after tag
        byte[] data = msg.getBytes(StandardCharsets.US_ASCII);

        assertThatThrownBy(() -> parserUnderTest.parse(data, 0, data.length))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing '='");
    }

    @Test
    void given_messageWithMissingSOHAfterValue_when_parse_then_throwsException() {
        String msg = "8=FIX.4.2"; // value without trailing SOH
        byte[] data = msg.getBytes(StandardCharsets.US_ASCII);

        assertThatThrownBy(() -> parserUnderTest.parse(data, 0, data.length))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing SOH");
    }

    @Test
    void given_nonNumericTag_when_parse_then_throwsException() {
        String msg = "A=1\u0001"; // tag 'A' is not numeric
        byte[] data = msg.getBytes(StandardCharsets.US_ASCII);

        assertThatThrownBy(() -> parserUnderTest.parse(data, 0, data.length))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("Invalid digit");
    }

    @Test
    void given_missingTagValue_when_parse_then_throwsException() {
        String msg = "=1\u0001";
        byte[] data = msg.getBytes(StandardCharsets.US_ASCII);

        assertThatThrownBy(() -> parserUnderTest.parse(data, 0, data.length))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("Empty integer");
    }
}
