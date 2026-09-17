package org.mage.test.cards.single.frc;

import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class OmnathLocusOfTheVoidTest extends CardTestPlayerBase {

    private static final String omnath = "Omnath, Locus of the Void";

    @Test
    public void testBoostAndManaRetention() {
        addCard(Zone.BATTLEFIELD, playerA, omnath);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}");
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertManaPool(playerA, ManaType.GREEN, 0);
        assertManaPool(playerA, ManaType.COLORLESS, 2);
        assertPowerToughness(playerA, omnath, 8, 8);
    }

    @Test
    public void testLandfall() {
        addCard(Zone.BATTLEFIELD, playerA, omnath);
        addCard(Zone.HAND, playerA, "Wastes");

        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Wastes");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertManaPool(playerA, ManaType.COLORLESS, 2);
        assertPowerToughness(playerA, omnath, 8, 8);
    }
}
