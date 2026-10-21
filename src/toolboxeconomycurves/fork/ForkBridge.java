package toolboxeconomycurves.fork;

import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import data.kaysaar.aotd.tot.compat.MarketRegistry;

/**
 * The only class that names the Scheduler Fork. Loaded by {@link SchedulerFork} only when the fork is
 * installed.
 */
final class ForkBridge {

    /** SchedulerBridge.DIRTY_INDUSTRIES | DIRTY_DERIVED_ECONOMY in fork 1.0.14-spp13: what its own buildings send. */
    static final int INDUSTRIES_AND_ECONOMY = (1 << 1) | (1 << 3);

    private ForkBridge() {}

    static void markChanged(List<MarketAPI> markets) {
        for (MarketAPI market : markets) {
            MarketRegistry.markDirty(market, INDUSTRIES_AND_ECONOMY, MarketRegistry.PRIORITY_NORMAL);
        }
    }
}
