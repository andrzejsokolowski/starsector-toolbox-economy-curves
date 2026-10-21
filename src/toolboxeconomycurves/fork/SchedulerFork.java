package toolboxeconomycurves.fork;

import java.util.List;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

/**
 * Support for the Theory of Toolbox Scheduler Fork (StarsectorPrepatcher).
 *
 * The fork only recalculates a colony's supply and demand when its registry says the colony
 * changed. This mod changes building numbers from outside, so it has to say so itself. Nothing in
 * here touches the fork's classes: they live in {@link ForkBridge}, which is only loaded once the
 * fork has been found, so the mod runs the same with the original Theory of Toolbox.
 */
public final class SchedulerFork {

    private static final String TOOLBOX_ID = "aotd_theory_of_toolbox";
    private static final Logger log = Global.getLogger(SchedulerFork.class);

    private static Boolean present;
    private static boolean failed;

    private SchedulerFork() {}

    /** The fork marks its version "1.0.14-spp13" and its name "... Scheduler Fork". */
    public static boolean isPresent() {
        if (present == null) {
            ModSpecAPI spec = Global.getSettings().getModManager().getModSpec(TOOLBOX_ID);
            String version = spec == null || spec.getVersion() == null ? "" : spec.getVersion().toLowerCase();
            String name = spec == null || spec.getName() == null ? "" : spec.getName().toLowerCase();
            present = version.contains("spp") || name.contains("scheduler fork");
            if (present) log.info("Theory of Toolbox Scheduler Fork found: colonies are reported to it when their curve numbers change");
        }
        return present;
    }

    /** Tells the fork these colonies changed. Does nothing with the original Theory of Toolbox. */
    public static void markChanged(List<MarketAPI> markets) {
        if (markets.isEmpty() || failed || !isPresent()) return;
        try {
            ForkBridge.markChanged(markets);
        } catch (Throwable t) {
            // A future fork that renamed its registry must not take the game down with it.
            failed = true;
            log.error("Could not report changed colonies to the Theory of Toolbox Scheduler Fork; stopped trying", t);
        }
    }
}
