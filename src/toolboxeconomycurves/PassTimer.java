package toolboxeconomycurves;

/**
 * Decides when {@link CurveScript} runs its next pass. It counts real time, so it also runs while the
 * game is paused (the colony screen is open then), and it is not sped up by fast-forward.
 *
 * Why a pass every second is enough: this mod's multipliers sit under its own ids, so Theory
 * of Toolbox's reapply leaves them in place. They only go stale when a colony really changes (it
 * grows, a building is added, a shortage starts), and Theory of Toolbox only reads the amounts on its
 * economy step, every 3 game days (30 seconds of real time at normal speed, 15 at double speed). A
 * second is a small part of that. The colony screen shows a change at most a second late.
 *
 * No game classes, so it is tested in {@code test/}.
 */
final class PassTimer {

    /** Seconds of real time between passes. */
    static final double INTERVAL_SECONDS = 1.0;

    private final long intervalNanos;
    private long last;
    private boolean force = true;

    PassTimer() {
        this(INTERVAL_SECONDS);
    }

    PassTimer(double intervalSeconds) {
        intervalNanos = (long) (intervalSeconds * 1e9);
    }

    /** Whether a pass should run at {@code nowNanos} (from {@link System#nanoTime()}). */
    boolean due(long nowNanos) {
        return force || nowNanos - last >= intervalNanos;
    }

    /** A pass ran at {@code nowNanos}: the next one is due a full interval later. */
    void ran(long nowNanos) {
        last = nowNanos;
        force = false;
    }

    /** The next frame runs a pass whatever the time: after a save, when every multiplier is off. */
    void forceNext() {
        force = true;
    }
}
