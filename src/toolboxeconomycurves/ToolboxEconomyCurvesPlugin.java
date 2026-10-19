package toolboxeconomycurves;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;

import toolboxeconomycurves.raids.FairRaidLoot;

public class ToolboxEconomyCurvesPlugin extends BaseModPlugin {

    public static final String MOD_ID = "toolbox_economy_curves";

    @Override
    public void onGameLoad(boolean newGame) {
        CurveScript.install();
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(FairRaidLoot.class)) listeners.addListener(new FairRaidLoot(), true);
    }

    /** Every multiplier comes off before a save and goes back on after, so saves stay clean. */
    @Override
    public void beforeGameSave() {
        CurveScript.suspend();
    }

    @Override
    public void afterGameSave() {
        CurveScript.resume();
    }

    @Override
    public void onGameSaveFailed() {
        CurveScript.resume();
    }
}
