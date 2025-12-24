package io.github.kwongchan.fixparser;

import io.github.kwongchan.fixparser.utils.ByteArrayCharSequence;
import io.github.kwongchan.fixparser.utils.FIXValueParser;
import io.github.kwongchan.fixparser.utils.ObjectPool;

public class FIXParserFactory {

    private static final int DEFAULT_CAPACITY = 128;

    public FIXParser newParser() {
        var fixValueParser = new FIXValueParser();
        var charSequencePool = new ObjectPool<>(ByteArrayCharSequence::new, DEFAULT_CAPACITY);
        var fixMessage = new FIXMessageImpl(fixValueParser, charSequencePool, DEFAULT_CAPACITY);
        return new FIXParserImpl(fixMessage);
    }
}
