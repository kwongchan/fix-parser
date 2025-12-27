package io.github.kwongchan.fixparser.utils;

public class FIXValueParser {

    public int parseInt(CharSequence value) {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("Empty or null CharSequence");
        }
        return parseInt(value, 0, value.length());
    }

    private int parseInt(CharSequence value, int start, int end) {
        int index = start;

        // Handle sign
        boolean negative = false;
        char firstChar = value.charAt(index);
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        // After sign, we must have at least one digit
        if (index >= end) {
            throw new NumberFormatException("No digits after sign");
        }

        int result = 0;
        for (; index < end; index++) {
            char c = value.charAt(index);
            if (c < '0' || c > '9') {
                throw new NumberFormatException("Invalid character '" + c + "' at position " + index);
            }
            result = result * 10 + (c - '0');
        }

        return negative ? -result : result;
    }

    public long parseLong(CharSequence value) {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("Empty or null CharSequence");
        }

        int index = 0;
        int end = value.length();

        // Handle sign
        boolean negative = false;
        char firstChar = value.charAt(index);
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        // Must have at least one digit after sign
        if (index >= end) {
            throw new NumberFormatException("No digits after sign");
        }

        long result = 0;
        for (; index < end; index++) {
            char c = value.charAt(index);
            if (c < '0' || c > '9') {
                throw new NumberFormatException("Invalid character '" + c + "' at position " + index);
            }

            int digit = c - '0';
            result = result * 10 + digit;
        }

        return negative ? -result : result;
    }

    public double parseDouble(CharSequence value) {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("Empty or null CharSequence");
        }
        long significand = 0;
        int exponent = 0;
        boolean negative = false;
        int index = 0;

        // Sign
        if (value.charAt(index) == '-') {
            negative = true;
            index++;
        } else if (value.charAt(index) == '+') index++;

        // Integer part
        while (index < value.length()) {
            char c = value.charAt(index);
            if (c == '.') {
                index++;
                break;
            }
            if (c < '0' || c > '9') throw new NumberFormatException();
            significand = significand * 10 + (c - '0');
            index++;
        }

        // Fractional part
        while (index < value.length()) {
            char c = value.charAt(index);
            if (c < '0' || c > '9') throw new NumberFormatException();
            significand = significand * 10 + (c - '0');
            exponent--;
            index++;
        }

        double d = significand * Math.pow(10, exponent);
        return negative ? -d : d;
    }

    public boolean parseBoolean(CharSequence value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Empty or null CharSequence");
        }
        if (value.length() == 1) {
            char c = value.charAt(0);
            if (c == 'Y') {
                return true;
            } else if (c == 'N') {
                return false;
            }
        }
        throw new IllegalArgumentException("Invalid boolean representation: " + value);
    }

    public long parseDateTimeToEpochMillis(CharSequence value) {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("Empty or null CharSequence");
        }

        final int len = value.length();
        // minimal length yyyyMMdd-HH:mm:ss -> 17
        if (len < 17) {
            throw new NumberFormatException("Invalid FIX timestamp: " + value);
        }

        // Parse fixed-width fields without allocations
        int year = parseInt(value, 0, 4);
        int month = parseInt(value, 4, 6);
        int day = parseInt(value, 6, 8);

        if (value.charAt(8) != '-') {
            throw new NumberFormatException("Expected '-' at position 8");
        }

        int hour = parseInt(value, 9, 11);
        if (value.charAt(11) != ':') {
            throw new NumberFormatException("Expected ':' at position 11");
        }

        int minute = parseInt(value, 12, 14);
        if (value.charAt(14) != ':') {
            throw new NumberFormatException("Expected ':' at position 14");
        }

        int second = parseInt(value, 15, 17);
        int millis = getMillis(value, len);

        // basic range checks
        if (month < 1 || month > 12) throw new NumberFormatException("Invalid month: " + month);
        if (day < 1 || day > 31) throw new NumberFormatException("Invalid day: " + day);
        if (hour < 0 || hour > 23) throw new NumberFormatException("Invalid hour: " + hour);
        if (minute < 0 || minute > 59) throw new NumberFormatException("Invalid minute: " + minute);
        if (second < 0 || second > 59) throw new NumberFormatException("Invalid second: " + second);

        if (month <= 2) {
            year--;
            month += 12;
        }
        long era = year / 400;
        long yoe = year - era * 400;                   // [0, 399]
        long doy = (153 * (month - 3) + 2) / 5 + (long) day - 1; // [0, 365]
        long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy; // [0, 146096]
        long epochDay = era * 146097 + doe - 719468;

        long epochSecond = epochDay * 86400L + hour * 3600L + minute * 60L + second;

        return epochSecond * 1000L + (long) millis;
    }

    private static int getMillis(CharSequence value, int len) {
        int index = 17;
        int millis = 0; // 3 digits
        if (index < len && value.charAt(index) == '.') {
            index++;
            int digits = 0;
            while (index < len) {
                char c = value.charAt(index);
                if (c < '0' || c > '9') {
                    throw new NumberFormatException("Invalid fractional digit '" + c + "' at position " + index);
                }
                if (digits < 3) {
                    millis = millis * 10 + (c - '0');
                }
                digits++;
                index++;
            }
            if (digits != 3) {
                throw new NumberFormatException("Only 3 fractional digits (milliseconds) supported");
            }
        }
        return millis;
    }
}
