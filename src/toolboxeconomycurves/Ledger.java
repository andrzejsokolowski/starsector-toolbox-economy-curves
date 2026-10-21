package toolboxeconomycurves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;

/**
 * Every multiplier this mod puts on the game, with the way to take it off again.
 *
 * Each pass re-asserts the multipliers that should exist. Whatever was not re-asserted (the colony
 * shrank back to the anchor, the farm was demolished, a setting changed) comes off in
 * {@link #sweep()}, and {@link #releaseAll()} takes everything off so a save never holds a trace of
 * this mod. Same idea as Omega Core Everywhere's ledger.
 *
 * Colonies whose numbers moved noticeably are collected in {@link #changed()}, so an economy that
 * only recalculates colonies it knows have changed (the Theory of Toolbox Scheduler Fork) can be told.
 */
final class Ledger {

    private static final class Hold {
        final Runnable release;
        final MarketAPI market;
        int stamp;

        Hold(Runnable release, MarketAPI market) {
            this.release = release;
            this.market = market;
        }
    }

    private final Map<MutableStat, Map<String, Hold>> holds = new IdentityHashMap<>();
    private final Map<MarketAPI, Boolean> changed = new IdentityHashMap<>();
    private int stamp = 1;

    void mult(MutableStat stat, String id, float value, String desc, MarketAPI market) {
        StatMod before = stat.getMultStatMod(id);
        // Rewriting an existing entry does not change the map's structure, which matters because
        // Theory of Toolbox's worker threads may be reading these stats.
        if (before == null || before.getValue() != value || !desc.equals(before.getDesc())) {
            if (Changes.differs(before == null ? 1f : before.getValue(), value)) touch(market);
            stat.modifyMult(id, value, desc);
        }
        Map<String, Hold> byId = holds.get(stat);
        if (byId == null) {
            byId = new HashMap<>();
            holds.put(stat, byId);
        }
        Hold hold = byId.get(id);
        if (hold == null) {
            hold = new Hold(() -> stat.unmodifyMult(id), market);
            byId.put(id, hold);
        }
        hold.stamp = stamp;
    }

    /** Takes off whatever was not re-asserted since the last sweep. */
    void sweep() {
        release(false);
        stamp++;
    }

    void releaseAll() {
        release(true);
        stamp++;
    }

    /** Colonies whose numbers moved since the last call. The set is emptied by the call. */
    List<MarketAPI> changed() {
        List<MarketAPI> out = new ArrayList<>(changed.keySet());
        changed.clear();
        return out;
    }

    private void touch(MarketAPI market) {
        if (market != null) changed.put(market, Boolean.TRUE);
    }

    private void release(boolean all) {
        List<Hold> stale = new ArrayList<>();
        Iterator<Map<String, Hold>> targets = holds.values().iterator();
        while (targets.hasNext()) {
            Map<String, Hold> byId = targets.next();
            Iterator<Hold> it = byId.values().iterator();
            while (it.hasNext()) {
                Hold hold = it.next();
                if (all || hold.stamp != stamp) {
                    stale.add(hold);
                    it.remove();
                }
            }
            if (byId.isEmpty()) targets.remove();
        }
        for (Hold hold : stale) {
            hold.release.run();
            touch(hold.market);
        }
    }
}
