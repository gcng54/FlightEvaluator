package io.github.gcng54.flyeval.lib.units;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrapperTest {

    @Test
    void testwrap() {
        double min = -90.0;
        double max = 90.0;
        double value = 100.0;

        // NONE: 100.0
        double noneWrapped = Wrapper.wrap(value, min, max, Wrapper.EWrapMode.NONE);
        assertEquals(100.0, noneWrapped, 0.001);

        // BOUND: 90.0
        double boundWrapped = Wrapper.wrap(value, min, max, Wrapper.EWrapMode.BOUND);
        assertEquals(90.0, boundWrapped, 0.001);

        // CYCLE: -80.0
        double cycleWrapped = Wrapper.wrap(value, min, max, Wrapper.EWrapMode.CYCLE);
        assertEquals(-80.0, cycleWrapped, 0.001);

        // BOUNCE: 80.0
        double bounceWrapped = Wrapper.wrap(value, min, max, Wrapper.EWrapMode.BOUNCE);
        assertEquals(80.0, bounceWrapped, 0.001);
    }

    @Test
    void testValidateMinToMax() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Wrapper(100, 0);
        });
    }

    @Test
    void testwrapNegative() {
        double min = -90.0;
        double max = 90.0;
        double value = -100.0;

        // NONE: -100.0
        double noneWrapped = Wrapper.wrap(value, min, max, Wrapper.EWrapMode.NONE);
        assertEquals(-100.0, noneWrapped, 0.001);

        // BOUND: -90.0
        double boundWrapped = Wrapper.wrap(value, min, max, Wrapper.EWrapMode.BOUND);
        assertEquals(-90.0, boundWrapped, 0.001);

        // CYCLE: 80.0
        double cycleWrapped = Wrapper.wrap(value, min, max, Wrapper.EWrapMode.CYCLE);
        assertEquals(80.0, cycleWrapped, 0.001);

        // BOUNCE: -80.0
        double bounceWrapped = Wrapper.wrap(value, min, max, Wrapper.EWrapMode.BOUNCE);
        assertEquals(-80.0, bounceWrapped, 0.001);
    }
}
