package toolboxeconomycurves.raids;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.IconGroupAPI;
import com.fs.starfarer.api.ui.IconRenderMode;

import data.kaysaar.aotd.tot.plugins.AoTDBaseDemSupCalc;
import data.kaysaar.aotd.tot.plugins.AoTDCommodityEconSpecManager;
import data.kaysaar.aotd.tot.raids.AoTDCommodityGroundRaidObjectivePluginImpl;
import data.kaysaar.aotd.tot.scripts.commoditydata.AoTDCommodityOnMarket;
import toolboxeconomycurves.Curve;
import toolboxeconomycurves.Settings;

/**
 * Theory of Toolbox's commodity raid target with fair loot.
 *
 * Theory of Toolbox gives each marine group 1% of the colony's demand plus 10% of its whole unsold
 * surplus, with no cap: a full raid of 10 groups carries off the entire surplus and more. Here each
 * group takes a set share (LunaLib, default 1%) of everything the colony holds, demand and surplus
 * alike, so a full raid takes a tenth of it.
 *
 * The deficit icons divide by the size curve actually in effect instead of Theory of Toolbox's
 * doubling, so they stay right when the curve is changed.
 */
public class FairCommodityRaid extends AoTDCommodityGroundRaidObjectivePluginImpl {

    public FairCommodityRaid(MarketAPI market, String commodityId) {
        super(market, commodityId);
    }

    @Override
    public float getBaseRaidQuantity(boolean forDeficit) {
        float share = Settings.raidShare();
        if (forDeficit || share <= 0 || !(com instanceof AoTDCommodityOnMarket)) {
            return super.getBaseRaidQuantity(forDeficit);
        }
        AoTDCommodityOnMarket commodity = (AoTDCommodityOnMarket) com;
        int demand = commodity.getSupplyDemandData().getTotalRawUnitsFromDemand();
        if (demand <= 0) return 0f;
        float stock = demand + commodity.getExcessQuantity() - commodity.getDeficitQuantity();
        return Math.max(0f, stock) * share;
    }

    @Override
    public int getDeficitCaused() {
        if (!(com instanceof AoTDCommodityOnMarket)) return super.getDeficitCaused();
        return toIcons(units(getQuantity(getMarinesAssigned(), true)));
    }

    @Override
    public void addIcons(IconGroupAPI iconGroup) {
        if (!(com instanceof AoTDCommodityOnMarket)) {
            super.addIcons(iconGroup);
            return;
        }
        AoTDCommodityOnMarket commodity = (AoTDCommodityOnMarket) com;
        int units = units(commodity.getSupplyDemandData().getTotalRawUnitsFromDemand());
        iconGroup.addIconGroup(getId(), IconRenderMode.NORMAL, toIcons(units), null);
    }

    private int units(float raw) {
        AoTDBaseDemSupCalc calc = AoTDCommodityEconSpecManager.getEconSpec(com.getId()).getCalculationScript();
        return calc.convertRawUnitsToDemand(raw, market, com.getId());
    }

    /** Food and domestic goods carry the size curve: take it out again so the icon count reads like the base game's. */
    private int toIcons(int units) {
        String id = com.getId();
        if (!Commodities.FOOD.equals(id) && !Commodities.DOMESTIC_GOODS.equals(id)) return units;
        Curve curve = Settings.curve();
        int size = com.getMarket().getSize();
        double divisor = Commodities.FOOD.equals(id) ? curve.foodDemand(size) : curve.goodsDemand(size);
        if (divisor <= 0) return units;
        return (int) Math.round(units / divisor);
    }
}
