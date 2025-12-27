package io.github.kwongchan.fixparser;

import io.github.kwongchan.fixparser.utils.ByteArrayCharSequence;
import io.github.kwongchan.fixparser.utils.FIXValueParser;
import io.github.kwongchan.fixparser.utils.ObjectPool;

public class FIXParserFactory {

    private static final int INITIAL_CAPACITY = 128;

    public FIXParser newParser() {
        var fixValueParser = new FIXValueParser();
        var charSequencePool = new ObjectPool<>(ByteArrayCharSequence::new, INITIAL_CAPACITY);
        var fixMessageFactory = new FIXMessageFlyWeightFactory(fixValueParser, charSequencePool, INITIAL_CAPACITY);
        return new FIXParserImpl(fixMessageFactory);
    }
}
