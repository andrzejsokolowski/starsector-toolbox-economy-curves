package toolboxeconomycurves;

import lunalib.lunaSettings.LunaSettings;

/** LunaLib values, read live. A missing value falls back to the default in LunaSettings.csv. */
public final class Settings {

    static final String SHAPE_TOOLBOX = "Theory of Toolbox (doubling)";
    static final String SHAPE_LINEAR = "Linear";
    static final String SHAPE_GENTLE = "Gentle";
    static final String SHAPE_GROWTH = "Custom growth";
    static final String SHAPE_LIST = "Custom list";

    static final String PRODUCERS_OFF = "Off";
    static final String PRODUCERS_GOODS = "Domestic goods";
    static final String PRODUCERS_ALL = "Domestic goods and heavy machinery";

    static final int DEFAULT_ANCHOR = 6;
    static final int DEFAULT_STOP_AT = 15;
    static final double DEFAULT_GROWTH = 1.5;
    static final double DEFAULT_RAID_SHARE = 1.0;

    private Settings() {}

    public static Curve curve() {
        String shapeName = string("tec_shape", SHAPE_GENTLE);
        Curve.Shape shape;
        double[] list = null;
        switch (shapeName) {
            case SHAPE_TOOLBOX:
                shape = Curve.Shape.TOOLBOX;
                break;
            case SHAPE_LINEAR:
                shape = Curve.Shape.LINEAR;
                break;
            case SHAPE_GROWTH:
                shape = Curve.Shape.GROWTH;
                break;
            case SHAPE_LIST:
                list = Curve.parseList(string("tec_list", null));
                shape = list == null ? Curve.Shape.GENTLE : Curve.Shape.LIST;
                break;
            default:
                shape = Curve.Shape.GENTLE;
        }
        return new Curve(shape, integer("tec_anchor", DEFAULT_ANCHOR), integer("tec_stop_at", DEFAULT_STOP_AT),
                decimal("tec_growth", DEFAULT_GROWTH), list);
    }

    /** Light Industry and every other domestic goods producer follows the curve. */
    public static boolean scaleGoodsProducers() {
        return !PRODUCERS_OFF.equals(string("tec_producers", PRODUCERS_GOODS));
    }

    /** Heavy Industry and every other heavy machinery producer follows the curve too. */
    public static boolean scaleMachineryProducers() {
        return PRODUCERS_ALL.equals(string("tec_producers", PRODUCERS_GOODS));
    }

    /** Share of the colony's stock one marine group takes in a raid, as a fraction. 0 = Theory of Toolbox's loot. */
    public static float raidShare() {
        double percent = decimal("tec_raid_share", DEFAULT_RAID_SHARE);
        return (float) Math.max(0d, Math.min(10d, percent)) / 100f;
    }

    private static String string(String key, String fallback) {
        String value = LunaSettings.getString(ToolboxEconomyCurvesPlugin.MOD_ID, key);
        return value == null ? fallback : value.trim();
    }

    private static int integer(String key, int fallback) {
        Integer value = LunaSettings.getInt(ToolboxEconomyCurvesPlugin.MOD_ID, key);
        return value == null ? fallback : value;
    }

    private static double decimal(String key, double fallback) {
        Double value = LunaSettings.getDouble(ToolboxEconomyCurvesPlugin.MOD_ID, key);
        return value == null || value.isNaN() ? fallback : value;
    }
}
