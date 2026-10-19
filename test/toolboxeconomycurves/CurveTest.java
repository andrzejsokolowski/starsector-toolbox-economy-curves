package toolboxeconomycurves;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CurveTest {

    private static final double EPS = 1e-9;

    @Test
    void toolboxShapeIsTheDoubling() {
        Curve c = new Curve(Curve.Shape.TOOLBOX, 6, 15, 1.5, null);
        for (int s = 1; s <= 15; s++) assertEquals(Math.pow(2, s - 1), c.factor(s), EPS);
        assertEquals(Math.pow(2, 5) / 4, c.goodsDemand(6), EPS);
        assertEquals(0.3 * 32, c.farmMachinery(6), EPS);
    }

    @Test
    void toolboxShapeStopsAtTheCap() {
        Curve c = new Curve(Curve.Shape.TOOLBOX, 6, 10, 1.5, null);
        assertEquals(512, c.factor(10), EPS);
        assertEquals(512, c.factor(15), EPS);
    }

    @Test
    void everyShapeMatchesToolboxAtTheAnchor() {
        for (Curve.Shape shape : Curve.Shape.values()) {
            for (int anchor = 1; anchor <= 15; anchor++) {
                Curve c = new Curve(shape, anchor, 15, 1.5, Curve.GENTLE);
                assertEquals(Curve.toolbox(anchor), c.factor(anchor), EPS, shape + " at anchor " + anchor);
                assertEquals(1, c.producer(anchor), EPS);
            }
        }
    }

    @Test
    void linearIsFlat() {
        Curve c = new Curve(Curve.Shape.LINEAR, 6, 15, 1.5, null);
        for (int s = 1; s <= 15; s++) {
            assertEquals(32, c.factor(s), EPS);
            assertEquals(1, c.producer(s), EPS);
        }
    }

    @Test
    void growthMultipliesPerSize() {
        Curve c = new Curve(Curve.Shape.GROWTH, 6, 15, 1.5, null);
        assertEquals(32 * 1.5, c.factor(7), EPS);
        assertEquals(32 / 1.5, c.factor(5), EPS);
        assertEquals(32 * Math.pow(1.5, 9), c.factor(15), EPS);
    }

    @Test
    void gentleFollowsTheList() {
        Curve c = new Curve(Curve.Shape.GENTLE, 6, 15, 1.5, null);
        // Gentle is 5 at size 6 and 10 at size 8: twice the anchor amount.
        assertEquals(64, c.factor(8), EPS);
        assertEquals(32 * 58 / 5.0, c.factor(15), EPS);
    }

    @Test
    void shortListKeepsItsLastValue() {
        Curve c = new Curve(Curve.Shape.LIST, 2, 15, 1.5, new double[] {1, 2, 3});
        assertEquals(2 * 3 / 2.0, c.factor(3), EPS);
        assertEquals(c.factor(3), c.factor(12), EPS);
    }

    @Test
    void anchorAboveTheCapStillMatchesAtTheCap() {
        Curve c = new Curve(Curve.Shape.GROWTH, 12, 8, 1.5, null);
        assertEquals(Curve.toolbox(12), c.factor(8), EPS);
        assertEquals(Curve.toolbox(12), c.factor(15), EPS);
    }

    @Test
    void parsesListsAndRejectsTypos() {
        assertArrayEquals(new double[] {1, 1.2, 1.5, 2.25}, Curve.parseList("1, x1.2,X1.5 ; 2.25"), EPS);
        assertNull(Curve.parseList("1, 2, three"));
        assertNull(Curve.parseList("1, 0, 2"));
        assertNull(Curve.parseList(" , "));
        assertEquals(15, Curve.parseList("1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1").length);
    }
}
