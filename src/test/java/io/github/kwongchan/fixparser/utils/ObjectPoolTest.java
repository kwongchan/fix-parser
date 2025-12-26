package io.github.kwongchan.fixparser.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectPoolTest {

    @Test
    void given_negativeCapacity_when_newObjectPool_thenThrowException() {
        assertThatThrownBy(() -> new ObjectPool<>(newFactory(), -1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void given_emptyPool_when_acquire_then_retrieveFromFactory() {
        var nextId = new AtomicInteger(0);
        Supplier<TestPoolable> factory = () -> new TestPoolable(nextId.incrementAndGet());

        // initialCapacity = 3 -> constructor will create ids 1,2,3 (in that order)
        ObjectPool<TestPoolable> pool = new ObjectPool<>(factory, 3);

        // Acquires should return last-in (LIFO): 3,2,1
        TestPoolable a = pool.acquire();
        TestPoolable b = pool.acquire();
        TestPoolable c = pool.acquire();

        assertThat(a.getId()).isEqualTo(3);
        assertThat(b.getId()).isEqualTo(2);
        assertThat(c.getId()).isEqualTo(1);

        // Next acquire uses factory and produces id 4
        TestPoolable d = pool.acquire();
        assertThat(d.getId()).isEqualTo(4);
    }

    @Test
    void given_fullPool_when_release_then_resetsObjectsAndExpandsPool() {
        AtomicInteger nextId = new AtomicInteger(0);
        Supplier<TestPoolable> factory = () -> new TestPoolable(nextId.incrementAndGet());

        // start with capacity 1 to force expansion when adding second object
        ObjectPool<TestPoolable> pool = new ObjectPool<>(factory, 1);

        // Acquire the single prefilled object (id 1)
        TestPoolable fromPool = pool.acquire();
        assertThat(fromPool.getId()).isEqualTo(1);

        // Create a second object outside the pool (id 2)
        TestPoolable external = factory.get();
        assertThat(external.getId()).isEqualTo(2);

        // release(null) should be no-op (no exception and no reset)
        pool.release(null);

        // release objects; release calls reset() on each
        pool.release(fromPool);
        pool.release(external); // second release should trigger expansion

        assertThat(fromPool.getResetCount()).as("fromPool should have been reset once").isEqualTo(1);
        assertThat(external.getResetCount()).as("external should have been reset once").isEqualTo(1);

        // Acquire twice: should return last-released first (LIFO)
        TestPoolable r1 = pool.acquire();
        TestPoolable r2 = pool.acquire();

        assertThat(r1).as("first acquire should be the last released object").isSameAs(external);
        assertThat(r2).as("second acquire should be the earlier released object").isSameAs(fromPool);
    }

    private static class TestPoolable implements Poolable {
        final int id;
        int resetCount = 0;

        TestPoolable(int id) { this.id = id; }

        @Override
        public void reset() { resetCount++; }

        int getResetCount() { return resetCount; }
        int getId() { return id; }
    }

    private static Supplier<TestPoolable> newFactory() {
        AtomicInteger nextId = new AtomicInteger(0);
        return () -> new TestPoolable(nextId.incrementAndGet());
    }
}
