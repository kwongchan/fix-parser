package io.github.kwongchan.fixparser.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Int2ObjectHashMapTest {

    private Int2ObjectHashMap<String> mapUnderTest;

    @BeforeEach
    void setUp() {
        mapUnderTest = new Int2ObjectHashMap<>(4);
    }

    @Test
    void given_valuePutToMap_when_getByKey_then_returnValue() {
        // put returns null when inserting new key
        assertThat(mapUnderTest.put(1, "one")).isNull();
        assertThat(mapUnderTest.get(1)).isEqualTo("one");
        assertThat(mapUnderTest.containsKey(1)).isTrue();

        // updating returns previous value
        assertThat(mapUnderTest.put(1, "uno")).isEqualTo("one");
        assertThat(mapUnderTest.get(1)).isEqualTo("uno");

        // missing key
        assertThat(mapUnderTest.get(2)).isNull();
        assertThat(mapUnderTest.containsKey(2)).isFalse();
    }

    @Test
    void when_mapIsResized_then_allInsertedValueAreRemained() {
        // small initial capacity to force rehash/resizing during inserts
        Int2ObjectHashMap<String> map = new Int2ObjectHashMap<>(2);

        int count = 50;
        for (int i = 1; i <= count; i++) {
            String prev = map.put(i, "v" + i);
            assertThat(prev).as("expected null previous for new key %s", i).isNull();
        }

        // verify all inserted values after potential rehashes
        for (int i = 1; i <= count; i++) {
            assertThat(map.get(i)).as("value mismatch for key %s", i).isEqualTo("v" + i);
            assertThat(map.containsKey(i)).as("containsKey false for key %s", i).isTrue();
        }
    }

    @Test
    void when_clear_then_removeAllValues() {
        mapUnderTest.put(10, "ten");
        mapUnderTest.put(20, "twenty");

        assertThat(mapUnderTest.get(10)).isEqualTo("ten");
        assertThat(mapUnderTest.get(20)).isEqualTo("twenty");

        mapUnderTest.clear();

        assertThat(mapUnderTest.get(10)).isNull();
        assertThat(mapUnderTest.get(20)).isNull();
        assertThat(mapUnderTest.containsKey(10)).isFalse();
        assertThat(mapUnderTest.containsKey(20)).isFalse();
    }

    @Test
    void given_nullValue_when_put_the_noValueIsInserted() {
        Int2ObjectHashMap<String> map = new Int2ObjectHashMap<>(4);
        assertThat(map.put(100, null)).isNull();
        assertThat(map.get(100)).isNull();
        assertThat(map.containsKey(100)).isFalse();
    }
}
