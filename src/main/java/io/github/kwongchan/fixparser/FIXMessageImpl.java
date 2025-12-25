package io.github.kwongchan.fixparser;

import io.github.kwongchan.fixparser.utils.ByteArrayCharSequence;
import io.github.kwongchan.fixparser.utils.FIXValueParser;
import io.github.kwongchan.fixparser.utils.Int2ObjectHashMap;
import io.github.kwongchan.fixparser.utils.ObjectPool;

import java.util.ArrayList;
import java.util.List;

class FIXMessageImpl implements FIXMessage {

    private final Int2ObjectHashMap<ByteArrayCharSequence> fields;
    private final List<ByteArrayCharSequence> acquiredCharSequences;
    private final FIXValueParser valueParser;
    private final ObjectPool<ByteArrayCharSequence> charSequencePool;

    private byte[] messageBytes;

    FIXMessageImpl(FIXValueParser valueParser, ObjectPool<ByteArrayCharSequence> charSequencePool, int initialCapacity) {
        fields = new Int2ObjectHashMap<>(initialCapacity);
        acquiredCharSequences = new ArrayList<>(initialCapacity);
        this.valueParser = valueParser;
        this.charSequencePool = charSequencePool;
    }

    void init(byte[] messageBytes) {
        this.messageBytes = messageBytes;
    }

    void addFields(int tag, int start, int end) {
        ByteArrayCharSequence charSequence = charSequencePool.acquire();
        charSequence.init(messageBytes, start, end);
        acquiredCharSequences.add(charSequence);
        fields.put(tag, charSequence);
    }

    @Override
    public boolean hasTag(int tag) {
        return fields.containsKey(tag);
    }

    @Override
    public CharSequence getCharSequence(int tag) {
        return fields.get(tag);
    }

    @Override
    public int getInt(int tag) {
        return valueParser.parseInt(fields.get(tag));
    }

    @Override
    public long getLong(int tag) {
        return valueParser.parseLong(fields.get(tag));
    }

    @Override
    public double getDouble(int tag) {
        return valueParser.parseDouble(fields.get(tag));
    }

    @Override
    public boolean getBoolean(int tag) {
        return valueParser.parseBoolean(fields.get(tag));
    }

    @Override
    public long getDateTimeEpochMillis(int tag) {
        return valueParser.parseDateTimeToEpochMillis(fields.get(tag));
    }

    void reset() {
        for (int i = 0; i < acquiredCharSequences.size(); i++) {
            charSequencePool.release(acquiredCharSequences.get(i));
        }
        fields.clear();
        acquiredCharSequences.clear();
        messageBytes = null;
    }
}
