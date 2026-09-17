package org.mage.test.cards.single.frc;

import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class OmnathLocusOfTheVoidTest extends CardTestPlayerBase {

    private static final String omnath = "Omnath, Locus of the Void";

    @Test
    public void testBoostsForAllUnspentMana() {
        addCard(Zone.BATTLEFIELD, playerA, omnath);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}");
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, omnath, 8, 8);
        assertManaPool(playerA, ManaType.COLORLESS, 2);
        assertManaPool(playerA, ManaType.GREEN, 0);
        assertManaPool(playerA, ManaType.RED, 0);
    }

    @Test
    public void testLandfallAddsTwoColorlessMana() {
        addCard(Zone.BATTLEFIELD, playerA, omnath);
        addCard(Zone.HAND, playerA, "Wastes");

        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Wastes");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertManaPool(playerA, ManaType.COLORLESS, 2);
        assertPowerToughness(playerA, omnath, 8, 8);
    }

    @Test
    public void testManaIsLostWithoutOmnath() {
        addCard(Zone.BATTLEFIELD, playerA, omnath);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.HAND, playerA, "Murder");
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 3);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Murder", omnath);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertManaPool(playerA, ManaType.COLORLESS, 0);
        assertManaPool(playerA, ManaType.GREEN, 0);
        assertGraveyardCount(playerA, omnath, 1);
    }
}
