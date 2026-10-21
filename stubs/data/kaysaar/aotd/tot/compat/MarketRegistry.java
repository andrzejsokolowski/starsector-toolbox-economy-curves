package data.kaysaar.aotd.tot.compat;

/**
 * Compile-only stand-in for the Theory of Toolbox Scheduler Fork's MarketRegistry
 * (https://github.com/cyrrp/AoTD-Theory-Of-Toolbox-Scheduler-Fork, 1.0.14-spp13). Only the members
 * Toolbox Economy Curves calls are here. It is never packaged: in game the fork's own class is used.
 */
public final class MarketRegistry {
    public static final int PRIORITY_NORMAL = 0;

    private MarketRegistry() {}

    public static void markDirty(Object market, int dirtyMask, int priorityHint) {
        throw new UnsupportedOperationException("stub");
    }
}
