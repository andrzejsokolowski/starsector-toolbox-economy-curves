package toolboxeconomycurves;

/** When a multiplier has moved enough to be worth recalculating a colony for. No game classes, so it can be tested. */
final class Changes {

    /** Smaller moves than this (relative) are rounding noise from one frame to the next. */
    static final float THRESHOLD = 0.001f;

    private Changes() {}

    static boolean differs(float before, float after) {
        if (before == after) return false;
        float scale = Math.max(Math.abs(before), Math.abs(after));
        if (scale == 0f) return false;
        return Math.abs(after - before) / scale > THRESHOLD;
    }
}
