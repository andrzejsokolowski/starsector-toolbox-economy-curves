package toolboxeconomycurves;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChangesTest {

    @Test
    void roundingNoiseIsNotAChange() {
        assertFalse(Changes.differs(1.5f, 1.5f));
        assertFalse(Changes.differs(1.5f, 1.5004f));
        assertFalse(Changes.differs(0f, 0f));
    }

    @Test
    void realMovesAreChanges() {
        assertTrue(Changes.differs(1f, 1.4f));
        assertTrue(Changes.differs(2.74f, 1f));
        assertTrue(Changes.differs(0.25f, 0.2f));
    }
}
