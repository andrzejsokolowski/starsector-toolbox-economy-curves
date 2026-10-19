package toolboxeconomycurves.raids;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;

import data.kaysaar.aotd.tot.raids.AoTDCommodityGroundRaidObjectivePluginImpl;

/**
 * Swaps Theory of Toolbox's commodity raid targets for {@link FairCommodityRaid}.
 *
 * The game asks every raid listener for targets at priorities 0 to 9. Theory of Toolbox builds its
 * list at 0, so at 1 the list is complete and its commodity targets can be replaced one for one.
 * Only that exact class is replaced: targets other mods add are left alone.
 */
public final class FairRaidLoot implements GroundRaidObjectivesListener {

    private static final int AFTER_TOOLBOX = 1;

    @Override
    public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives,
                                     RaidType type, int marineTokens, int priority) {
        if (priority != AFTER_TOOLBOX || market == null || type != RaidType.VALUABLE) return;
        for (int i = objectives.size() - 1; i >= 0; i--) {
            GroundRaidObjectivePlugin objective = objectives.get(i);
            if (objective.getClass() != AoTDCommodityGroundRaidObjectivePluginImpl.class) continue;
            FairCommodityRaid fair = new FairCommodityRaid(market, objective.getId());
            if (fair.getQuantity(1) > 0f) {
                objectives.set(i, fair);
            } else {
                objectives.remove(i);
            }
        }
    }

    @Override
    public void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
    }
}
