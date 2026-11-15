package toolboxeconomycurves;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

/**
 * Keeps NaN and endless numbers out of a colony's income.
 *
 * Theory of Toolbox does not multiply trade income by the colony's income multiplier. At stability
 * 0 that multiplier is x0 while the colony still earns a lot from trade. Corrupt.Officials then
 * divides its penalty by the x0 multiplier and writes an endless penalty on Population &
 * Infrastructure's income; endless x 0 is NaN, and the colony's income becomes NaN. Industrial
 * Evolution's own "Corrupt Colony Officials" option reads that NaN income and writes a x NaN
 * "Corruption" income multiplier on top.
 *
 * The game applies a colony's conditions in list order, so this condition is kept last
 * ({@link #keepLast}) and drops every non-finite modifier the others left on the colony's income
 * and upkeep. At x0 no penalty on building income changes anything, so dropping it loses nothing.
 * The condition never reaches a save: {@link #removeAll} takes it off before every save, and the
 * next curve pass puts it back. Adding and removing it touches no other condition.
 */
public final class IncomeGuard extends BaseMarketConditionPlugin {

    public static final String ID = "tec_income_guard";

    private static final Logger log = Global.getLogger(IncomeGuard.class);
    private static boolean loggedError, loggedDrop;

    @Override
    public void apply(String id) {
        try {
            clean(market);
        } catch (RuntimeException e) {
            if (!loggedError) {
                loggedError = true;
                log.error("Toolbox Economy Curves could not check a colony's income", e);
            }
        }
    }

    @Override
    public void unapply(String id) {
    }

    @Override
    public boolean showIcon() {
        return false;
    }

    @Override
    public boolean isPlanetary() {
        return false;
    }

    /** Puts the guard at the end of the colony's conditions, where it runs after every other one. */
    static void keepLast(MarketAPI market) {
        List<MarketConditionAPI> conditions = market.getConditions();
        int size = conditions.size();
        if (size > 0 && ID.equals(conditions.get(size - 1).getId())) {
            // Already last; still check, in case a building changed the numbers since.
            clean(market);
            return;
        }
        if (market.hasCondition(ID)) market.removeCondition(ID);
        // Adding a condition applies it, so the colony is checked here as well.
        market.addCondition(ID);
    }

    /** Takes the guard off every colony, so no save holds it. */
    static void removeAll() {
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.hasCondition(ID)) market.removeCondition(ID);
        }
    }

    static void clean(MarketAPI market) {
        if (market == null) return;
        int dropped = drop(market.getIncomeMult()) + drop(market.getUpkeepMult());
        for (Industry ind : market.getIndustries()) {
            dropped += drop(ind.getIncome()) + drop(ind.getUpkeep());
        }
        if (dropped > 0 && !loggedDrop) {
            loggedDrop = true;
            log.info("Toolbox Economy Curves dropped " + dropped + " NaN or endless income modifier(s) on "
                    + market.getName() + " (logged once)");
        }
    }

    /** Removes every flat, percent and multiplier entry whose value is not a real number. */
    static int drop(MutableStat stat) {
        if (stat == null) return 0;
        int dropped = 0;
        for (String key : bad(stat.getFlatMods())) { stat.unmodifyFlat(key); dropped++; }
        for (String key : bad(stat.getPercentMods())) { stat.unmodifyPercent(key); dropped++; }
        for (String key : bad(stat.getMultMods())) { stat.unmodifyMult(key); dropped++; }
        return dropped;
    }

    private static List<String> bad(Map<String, StatMod> mods) {
        List<String> keys = null;
        for (Map.Entry<String, StatMod> e : mods.entrySet()) {
            float v = e.getValue().getValue();
            if (Float.isNaN(v) || Float.isInfinite(v)) {
                if (keys == null) keys = new ArrayList<>();
                keys.add(e.getKey());
            }
        }
        return keys == null ? Collections.<String>emptyList() : keys;
    }
}
