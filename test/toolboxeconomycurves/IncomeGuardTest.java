package toolboxeconomycurves;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fs.starfarer.api.combat.MutableStat;

class IncomeGuardTest {

    /** The colony from the report: stability 0, so the income multiplier is x0. */
    @Test
    void corruptionAtZeroMultiplierIsDropped() {
        MutableStat incomeMult = new MutableStat(1f);
        incomeMult.modifyMult("stability", 0f, "Stability");

        // Corrupt.Officials divides its penalty by the x0 multiplier: an endless penalty.
        float penalty = 50_000f / incomeMult.getModifiedValue();
        MutableStat population = new MutableStat(0f);
        population.modifyFlat("population", 20_000f, "Population");
        population.modifyFlatAlways("corruptofficials_cond", -penalty, "Government Corruption");
        float industryIncome = population.getModifiedValue() * incomeMult.getModifiedValue();
        assertTrue(Float.isNaN(industryIncome), "endless x 0 should be NaN, as in the game");

        // Industrial Evolution's corruption then writes x NaN from the NaN income.
        incomeMult.modifyMult("IndEvo_corruption", Float.NaN, "Corruption");

        assertEquals(1, IncomeGuard.drop(population));
        assertEquals(1, IncomeGuard.drop(incomeMult));
        assertFalse(Float.isNaN(population.getModifiedValue() * incomeMult.getModifiedValue()));
        assertEquals(0f, incomeMult.getModifiedValue());
        assertEquals(20_000f, population.getModifiedValue());
    }

    @Test
    void realNumbersStay() {
        MutableStat stat = new MutableStat(100f);
        stat.modifyFlat("a", -40f);
        stat.modifyPercent("b", 25f);
        stat.modifyMult("c", 0.5f);
        assertEquals(0, IncomeGuard.drop(stat));
        stat.modifyPercent("d", Float.POSITIVE_INFINITY);
        assertEquals(1, IncomeGuard.drop(stat));
        // The game: (base x (1 + percent) + flat) x mult.
        assertEquals((100f * 1.25f - 40f) * 0.5f, stat.getModifiedValue(), 1e-3);
    }
}
