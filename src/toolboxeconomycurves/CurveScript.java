package toolboxeconomycurves;

import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

import data.kaysaar.aotd.tot.scripts.economy.AoTDWorkerManager;

/**
 * Puts the chosen size curve on every colony in the Sector.
 *
 * Theory of Toolbox writes its doubling under its own modifier ids every time a colony is
 * reapplied, and its economy reads the amounts right after that reapply. A multiplier under this
 * mod's own id is not touched by the reapply, so it is still in place when the economy reads. Each
 * one is "wanted amount / Theory of Toolbox's amount", recomputed every frame from what Theory of
 * Toolbox actually wrote, so nothing in Theory of Toolbox is replaced.
 *
 * Astral Ascension Renewed holds Theory of Toolbox's doubling at size 10 with a multiplier of its
 * own ({@link #ASCENSION_CAP}). That one is divided out as well, so this mod's curve and its own
 * "stop growing at size" setting decide alone.
 *
 * The script is transient and everything comes off before a save ({@link #suspend()}).
 */
public final class CurveScript implements EveryFrameScript {

    /** Theory of Toolbox's ids: the colony's demand multiplier and the farm corrector. */
    static final String TOOLBOX_SCALE = "aotd_new_scale";
    static final String TOOLBOX_FARM = "aotd_toolbox_food_prod";
    /** Astral Ascension Renewed's hold at size 10. */
    static final String ASCENSION_CAP = "aar_toolbox_cap";

    static final String CURVE_ID = "tec_size_curve";
    static final String PRODUCER_ID = "tec_size_curve_producers";
    static final String CURVE_DESC = "Size curve (Toolbox Economy Curves)";

    private static final double SAME = 1e-4;

    private static CurveScript current;

    private final Ledger ledger = new Ledger();
    private boolean suspended;

    private CurveScript() {}

    /** Adds the script for this session. Called on every game load. */
    public static void install() {
        if (current != null) current.ledger.releaseAll();
        current = new CurveScript();
        Global.getSector().addTransientScript(current);
    }

    public static void suspend() {
        if (current == null) return;
        current.suspended = true;
        current.ledger.releaseAll();
    }

    public static void resume() {
        if (current != null) current.suspended = false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        // The colony screen is open while paused: keep its numbers right after a building change.
        return true;
    }

    @Override
    public void advance(float amount) {
        if (suspended) return;
        // Theory of Toolbox computes prices on worker threads that read these stats. Adding a new
        // entry while one of them iterates could break it, so wait for a frame with none running.
        if (AoTDWorkerManager.getRunningWorkers() > 0 || AoTDWorkerManager.isSaveBarrierActive()) return;

        Curve curve = Settings.curve();
        boolean goodsProducers = Settings.scaleGoodsProducers();
        boolean machineryProducers = Settings.scaleMachineryProducers();

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            int size = market.getSize();
            Industry population = market.getIndustry(Industries.POPULATION);
            if (population != null) {
                scale(population.getDemand(Commodities.FOOD).getQuantity(), TOOLBOX_SCALE, curve.foodDemand(size));
                scale(population.getDemand(Commodities.DOMESTIC_GOODS).getQuantity(), TOOLBOX_SCALE, curve.goodsDemand(size));
            }
            for (Industry ind : market.getIndustries()) {
                farm(ind, curve.farmFood(size));
                scale(ind.getDemand(Commodities.HEAVY_MACHINERY).getQuantity(), TOOLBOX_FARM, curve.farmMachinery(size));
                if (ind == population) continue;
                if (goodsProducers) producer(ind, Commodities.DOMESTIC_GOODS, curve.producer(size));
                if (machineryProducers) producer(ind, Commodities.HEAVY_MACHINERY, curve.producer(size));
            }
        }
        ledger.sweep();
    }

    /** Turns Theory of Toolbox's multiplier {@code theirs} into {@code wanted}. */
    private void scale(MutableStat stat, String theirs, double wanted) {
        StatMod mod = stat.getMultStatMod(theirs);
        if (mod == null || mod.getValue() <= 0) return;
        assertMult(stat, CURVE_ID, wanted / (mod.getValue() * ascensionCap(stat)));
    }

    /**
     * Farms. Theory of Toolbox adds a flat amount so output becomes base x 2^(size - 1), where base
     * is the farm's own output after any heavy machinery shortage. The wanted output is base x the
     * curve's factor, on top of everything else the farm has.
     */
    private void farm(Industry ind, double factor) {
        MutableStat food = ind.getSupply(Commodities.FOOD).getQuantity();
        StatMod theirs = food.getFlatStatMod(TOOLBOX_FARM);
        if (theirs == null) return;
        double others = food.getBaseValue();
        for (Map.Entry<String, StatMod> e : food.getFlatMods().entrySet()) {
            if (!TOOLBOX_FARM.equals(e.getKey())) others += e.getValue().getValue();
        }
        double now = others + theirs.getValue();
        if (now <= 0) return;
        double wanted = others + toolboxBase(ind, food) * (factor - 1);
        assertMult(food, CURVE_ID, Math.max(0, wanted) / (now * ascensionCap(food)));
    }

    /** The output Theory of Toolbox doubles: AoTDToolboxFoodProd.getSupplyFromIndustry, minus its own entry. */
    private static double toolboxBase(Industry ind, MutableStat food) {
        int total = 0;
        float penalty = 1f;
        for (Map.Entry<String, StatMod> e : food.getFlatMods().entrySet()) {
            if (TOOLBOX_FARM.equals(e.getKey())) continue;
            StatMod mod = e.getValue();
            String desc = mod.getDesc();
            if (desc == null || !desc.contains("shortage")) {
                total += (int) mod.getValue();
                continue;
            }
            if (!desc.contains("Heavy machinery")) continue;
            int demanded = ind.getDemand(Commodities.HEAVY_MACHINERY).getQuantity().getModifiedInt();
            if (demanded <= 0) continue;
            int missing = ind.getMaxDeficit(Commodities.HEAVY_MACHINERY).two;
            float ratio = (float) Math.max(0, demanded - missing) / demanded;
            if (ratio <= penalty) penalty = ratio;
        }
        if (penalty <= 0.2f) penalty = 0.2f;
        return Math.round(penalty * total);
    }

    /** Light Industry, Heavy Industry and any modded building that makes the commodity. */
    private void producer(Industry ind, String commodity, double mult) {
        MutableStat supply = ind.getSupply(commodity).getQuantity();
        if (supply.getBaseValue() + sumFlats(supply) <= 0) return;
        assertMult(supply, PRODUCER_ID, mult);
    }

    private static double sumFlats(MutableStat stat) {
        double sum = 0;
        for (StatMod mod : stat.getFlatMods().values()) sum += mod.getValue();
        return sum;
    }

    private static float ascensionCap(MutableStat stat) {
        StatMod cap = stat.getMultStatMod(ASCENSION_CAP);
        return cap == null || cap.getValue() <= 0 ? 1f : cap.getValue();
    }

    private void assertMult(MutableStat stat, String id, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return;
        if (Math.abs(value - 1) < SAME) return;
        ledger.mult(stat, id, (float) value, CURVE_DESC);
    }
}
