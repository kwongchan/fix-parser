package io.github.kwongchan.fixparser.utils;

public class Int2ObjectHashMap<V> {
    private static final float LOAD_FACTOR = 0.75f;
    private static final int EMPTY_KEY = 0;

    private int[] keys;
    private Object[] values;
    private int size;
    private int threshold;

    public Int2ObjectHashMap(int initialCapacity) {
        int cap = powerOfTwo(initialCapacity);
        keys = new int[cap];
        values = new Object[cap];
        // keys are initialized to 0 by JVM, which is EMPTY_KEY
        threshold = (int) (cap * LOAD_FACTOR);
    }

    private static int powerOfTwo(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : n + 1;
    }

    private static int hash(int key) {
        // Murmur-like finalizer for better distribution
        int h = key;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    private int mask() {
        return keys.length - 1;
    }

    public V put(int key, V value) {
        if (size  >= threshold) {
            rehash(keys.length * 2);
        }

        int mask = mask();
        int index = hash(key) & mask;

        while (true) {
            int k = keys[index];
            if (k == EMPTY_KEY) {
                keys[index] = key;
                @SuppressWarnings("unchecked")
                V old = (V) values[index];
                values[index] = value;
                if (old == null) {
                    size++;
                }
                return old;
            } else if (k == key) {
                @SuppressWarnings("unchecked")
                V old = (V) values[index];
                values[index] = value;
                return old;
            }
            index = (index + 1) & mask;
        }
    }

    public V get(int key) {
        int mask = mask();
        int index = hash(key) & mask;

        while (true) {
            int k = keys[index];
            if (k == EMPTY_KEY) {
                return null;
            }
            if (k == key) {
                @SuppressWarnings("unchecked")
                V v = (V) values[index];
                return v;
            }
            index = (index + 1) & mask;
        }
    }

    public boolean containsKey(int key) {
        return get(key) != null;
    }

    private void rehash(int newCapacity) {
        int[] oldKeys = keys;
        Object[] oldValues = values;

        keys = new int[newCapacity];
        values = new Object[newCapacity];
        threshold = (int) (newCapacity * LOAD_FACTOR);
        size = 0;

        int mask = newCapacity - 1;
        for (int i = 0; i < oldKeys.length; i++) {
            int k = oldKeys[i];
            if (k != EMPTY_KEY) {
                int index = hash(k) & mask;
                while (keys[index] != EMPTY_KEY) {
                    index = (index + 1) & mask;
                }
                keys[index] = k;
                values[index] = oldValues[i];
                size++;
            }
        }
    }

    public void clear() {
        if (size > 0) {
            java.util.Arrays.fill(keys, EMPTY_KEY);
            java.util.Arrays.fill(values, null);
            size = 0;
        }
    }
}
