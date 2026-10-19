package toolboxeconomycurves;

import java.util.ArrayList;
import java.util.List;

/**
 * The size curve that replaces Theory of Toolbox's doubling.
 *
 * Theory of Toolbox multiplies four amounts by 2^(size - 1) (or a fixed fraction of it): colony food
 * demand, colony domestic goods demand (a quarter), farm food output and farm heavy machinery demand
 * (0.3 of it). {@link #factor(int)} is the number that takes the place of 2^(size - 1). Every shape
 * except Theory of Toolbox's own is pinned to Theory of Toolbox at the anchor size, so the amounts
 * stay the same there and only the growth above and below it changes.
 *
 * No game classes in here, so it can be tested on its own.
 */
public final class Curve {

    public enum Shape { TOOLBOX, LINEAR, GENTLE, GROWTH, LIST }

    public static final int MAX_SIZE = 15;

    /** Andrzej's gentle curve for sizes 1 to 15: x1.2, x1.5, x2.25, x3.5, x5, x7.5, x10, then about x1.3 a size. */
    static final double[] GENTLE = {1, 1.2, 1.5, 2.25, 3.5, 5, 7.5, 10, 13, 17, 22, 28, 36, 46, 58};

    /** Theory of Toolbox's farm machinery demand is this share of its food multiplier. */
    static final double MACHINERY_SHARE = 0.3;

    private final Shape shape;
    private final int anchor;
    private final int stopAt;
    private final double growth;
    private final double[] list;

    public Curve(Shape shape, int anchor, int stopAt, double growth, double[] list) {
        this.shape = shape;
        this.anchor = clampSize(anchor);
        this.stopAt = clampSize(stopAt);
        this.growth = Double.isNaN(growth) || growth <= 0 ? 1 : growth;
        this.list = list == null || list.length == 0 ? GENTLE : list;
    }

    public Shape getShape() {
        return shape;
    }

    public int getAnchor() {
        return anchor;
    }

    /** What Theory of Toolbox multiplies by at this size. */
    public static double toolbox(int size) {
        return Math.pow(2, Math.max(size, 1) - 1);
    }

    /** Replaces Theory of Toolbox's 2^(size - 1). */
    public double factor(int size) {
        int s = Math.min(Math.max(size, 1), stopAt);
        if (shape == Shape.TOOLBOX) return toolbox(s);
        int a = Math.min(anchor, stopAt);
        return toolbox(anchor) * shapeAt(s) / shapeAt(a);
    }

    public double foodDemand(int size) {
        return factor(size);
    }

    public double goodsDemand(int size) {
        return factor(size) / 4;
    }

    public double farmFood(int size) {
        return factor(size);
    }

    public double farmMachinery(int size) {
        return MACHINERY_SHARE * factor(size);
    }

    /** Light and Heavy Industry output: unchanged at the anchor size, following the curve elsewhere. */
    public double producer(int size) {
        return factor(size) / toolbox(anchor);
    }

    private double shapeAt(int size) {
        switch (shape) {
            case LINEAR:
                return 1;
            case GROWTH:
                return Math.pow(growth, size - 1);
            case GENTLE:
            case LIST:
                return list[Math.min(size, list.length) - 1];
            default:
                return toolbox(size);
        }
    }

    private static int clampSize(int size) {
        return Math.max(1, Math.min(MAX_SIZE, size));
    }

    /**
     * Reads "1, 1.2, x1.5, 2.25" into multipliers. Returns null if any entry is not a positive number,
     * so a typo falls back to the gentle curve instead of producing nonsense.
     */
    public static double[] parseList(String text) {
        if (text == null) return null;
        List<Double> values = new ArrayList<>();
        for (String part : text.split("[,;\\s]+")) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            if (p.charAt(0) == 'x' || p.charAt(0) == 'X') p = p.substring(1);
            double v;
            try {
                v = Double.parseDouble(p);
            } catch (NumberFormatException e) {
                return null;
            }
            if (!(v > 0) || Double.isInfinite(v)) return null;
            values.add(v);
            if (values.size() == MAX_SIZE) break;
        }
        if (values.isEmpty()) return null;
        double[] out = new double[values.size()];
        for (int i = 0; i < out.length; i++) out[i] = values.get(i);
        return out;
    }
}
