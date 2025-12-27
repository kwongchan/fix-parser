package io.github.kwongchan.fixparser;

import io.github.kwongchan.fixparser.utils.ByteArrayCharSequence;
import io.github.kwongchan.fixparser.utils.FIXValueParser;
import io.github.kwongchan.fixparser.utils.ObjectPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class FIXMessageFlyWeightFactoryTest {

    private Supplier<FIXMessageImpl> factoryUnderTest;

    @BeforeEach
    void setUp() {
        factoryUnderTest = new FIXMessageFlyWeightFactory(
                new FIXValueParser(),
                new ObjectPool<>(ByteArrayCharSequence::new, 10),
                20
        );
    }

    @Test
    void when_get_then_returnResetInstance() {
        FIXMessageImpl message1 = factoryUnderTest.get();
        message1.init("8=FIX.4.2\u00019=12\u000135=D\u0001".getBytes());
        message1.addFields(8, 2, 9);
        assertThat(message1.getCharSequence(8)).hasToString("FIX.4.2");

        FIXMessageImpl message2 = factoryUnderTest.get();
        assertThat(message2).isSameAs(message1);
        assertThat(message2.hasTag(8)).isFalse();
    }
}
