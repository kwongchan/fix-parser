package io.github.kwongchan.fixparser;

public interface FIXParser {

    default FIXMessage parse(byte[] data) {
        return parse(data, 0, data.length);
    }

    FIXMessage parse(byte[] data, int start, int end);
}
