package io.github.kwongchan.fixparser.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FIXValueParserTest {

    private FIXValueParser parserUnderTest;

    @BeforeEach
    void setUp() {
        parserUnderTest = new FIXValueParser();
    }

    @ParameterizedTest
    @CsvSource({
            "123, 123",
            "-456, -456",
            "+789, 789"
    })
    void when_parseInt_then_returnValue(String input, int expected) {
        assertThat(parserUnderTest.parseInt(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"-", "+", "12a3", "abc"})
    void given_invalidInput_when_parseInt_then_throwsException(String input) {
        assertThatThrownBy(() -> parserUnderTest.parseInt(input)).isInstanceOf(NumberFormatException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "42, 42",
            "-9223372036854775807, -9223372036854775807",
            "+7, 7"
    })
    void when_parseLong_then_returnValue(String input, long expected) {
        assertThat(parserUnderTest.parseLong(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1x", "-"})
    void given_invalidInput_when_parseLong_then_throwsException(String input) {
        assertThatThrownBy(() -> parserUnderTest.parseLong(input)).isInstanceOf(NumberFormatException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "123, 123.0",
            "-123.45, -123.45",
            "+0.001, 0.001",
            ".1, 0.1",
            "1., 1.0"
    })
    void when_parseDouble_then_returnValue(String input, double expected) {
        assertThat(parserUnderTest.parseDouble(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1.2.3", "12a"})
    void given_invalidInput_when_parseDouble_then_throwsException(String input) {
        assertThatThrownBy(() -> parserUnderTest.parseDouble(input)).isInstanceOf(NumberFormatException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "Y, true",
            "N, false"
    })
    void when_parseBoolean_then_returnValue(String input, boolean expected) {
        assertThat(parserUnderTest.parseBoolean(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"y", "yes"})
    void given_invalidInput_when_parseBoolean_then_throwsException(String input) {
        assertThatThrownBy(() -> parserUnderTest.parseBoolean(input)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "19700101-00:00:00",
            "20250101-00:00:00",
            "20251224-15:30:12.123"
    })
    void when_parseDateTimeToEpochMillis_then_returnValue(String input) {
        long expected = expectedEpochMillis(input);
        long actual = parserUnderTest.parseDateTimeToEpochMillis(input);
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "20251224-15:30:12.1234", // too many fractional digits
            "20251224-15:30:12.",     // missing digits after dot
            "20251224 15:30:12",      // wrong separator
            "20251224-15-30:12",      // wrong separator
            "20251224-15:30-12",      // wrong separator
            "2025-12-24",             // too short / wrong format
            "20251224-25:00:00",      // invalid hour
            "20251224-15:61:00",      // invalid minute
            "20251224-15:30:61",      // invalid second
            "20251424-15:30:12",      // invalid month
            "20251239-15:30:12"       // invalid day
    })
    void given_invalidInput_when_parseDateTimeToEpochMillis_then_throwsException(String input) {
        assertThatThrownBy(() -> parserUnderTest.parseDateTimeToEpochMillis(input))
                .isInstanceOf(NumberFormatException.class);
    }

    private static long expectedEpochMillis(String input) {
        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss[.SSS]");
        var ldt = LocalDateTime.parse(input, formatter);
        return ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
