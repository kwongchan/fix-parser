package io.github.kwongchan.fixparser;

import io.github.kwongchan.fixparser.utils.ByteArrayCharSequence;
import io.github.kwongchan.fixparser.utils.FIXValueParser;
import io.github.kwongchan.fixparser.utils.ObjectPool;

import java.util.function.Supplier;

class FIXMessageFlyWeightFactory implements Supplier<FIXMessageImpl> {

    private final FIXMessageImpl fixMessage;

    FIXMessageFlyWeightFactory(FIXValueParser valueParser, ObjectPool<ByteArrayCharSequence> charSequencePool, int initialCapacity) {
        this.fixMessage = new FIXMessageImpl(valueParser, charSequencePool, initialCapacity);
    }

    @Override
    public FIXMessageImpl get() {
        fixMessage.reset();
        return fixMessage;
    }
}
