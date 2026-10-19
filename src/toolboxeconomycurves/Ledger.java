package toolboxeconomycurves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;

/**
 * Every multiplier this mod puts on the game, with the way to take it off again.
 *
 * Each pass re-asserts the multipliers that should exist. Whatever was not re-asserted (the colony
 * shrank back to the anchor, the farm was demolished, a setting changed) comes off in
 * {@link #sweep()}, and {@link #releaseAll()} takes everything off so a save never holds a trace of
 * this mod. Same idea as Omega Core Everywhere's ledger.
 */
final class Ledger {

    private static final class Hold {
        final Runnable release;
        int stamp;

        Hold(Runnable release) {
            this.release = release;
        }
    }

    private final Map<MutableStat, Map<String, Hold>> holds = new IdentityHashMap<>();
    private int stamp = 1;

    void mult(MutableStat stat, String id, float value, String desc) {
        StatMod before = stat.getMultStatMod(id);
        // Rewriting an existing entry does not change the map's structure, which matters because
        // Theory of Toolbox's worker threads may be reading these stats.
        if (before == null || before.getValue() != value || !desc.equals(before.getDesc())) {
            stat.modifyMult(id, value, desc);
        }
        Map<String, Hold> byId = holds.get(stat);
        if (byId == null) {
            byId = new HashMap<>();
            holds.put(stat, byId);
        }
        Hold hold = byId.get(id);
        if (hold == null) {
            hold = new Hold(() -> stat.unmodifyMult(id));
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

    private void release(boolean all) {
        List<Runnable> stale = new ArrayList<>();
        Iterator<Map<String, Hold>> targets = holds.values().iterator();
        while (targets.hasNext()) {
            Map<String, Hold> byId = targets.next();
            Iterator<Hold> it = byId.values().iterator();
            while (it.hasNext()) {
                Hold hold = it.next();
                if (all || hold.stamp != stamp) {
                    stale.add(hold.release);
                    it.remove();
                }
            }
            if (byId.isEmpty()) targets.remove();
        }
        for (Runnable release : stale) release.run();
    }
}
