package io.github.kwongchan.fixparser.utils;

import java.util.function.Supplier;

public class ObjectPool<T extends Poolable> {
    private final Supplier<T> factory;
    private Object[] pool;
    private int size;

    public ObjectPool(Supplier<T> factory, int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be >= 0");
        }
        this.factory = factory;
        this.pool = new Object[initialCapacity];
        this.size = 0;

        for (int i = 0; i < initialCapacity; i++) {
             pool[i] = factory.get();
             size++;
        }
    }

    public T acquire() {
        if (size > 0) {
            @SuppressWarnings("unchecked")
            T obj = (T) pool[--size];
            pool[size] = null;
            return obj;
        }
        return factory.get();
    }

    public void release(T obj) {
        if (obj == null) {
            return;
        }

        obj.reset();

        if (size == pool.length) {
            Object[] newPool = new Object[pool.length * 2];
            System.arraycopy(pool, 0, newPool, 0, pool.length);
            pool = newPool;
        }

        pool[size++] = obj;
    }
}
