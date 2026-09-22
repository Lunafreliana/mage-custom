package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class LoreholdTheHistorianTest extends CardTestPlayerBase {

    private static final String lorehold = "Lorehold, the Historian";

    @Test
    public void testGrantsMiracleToInstantOrSorcery() {
        skipInitShuffling();
        addCard(Zone.BATTLEFIELD, playerA, lorehold);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.LIBRARY, playerA, "Lava Axe");

        setChoice(playerA, true); // Reveal Lava Axe for miracle
        setChoice(playerA, true); // Cast Lava Axe for its miracle cost
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerB, 15);
        assertGraveyardCount(playerA, "Lava Axe", 1);
    }

    @Test
    public void testLootsDuringOpponentsUpkeep() {
        skipInitShuffling();
        addCard(Zone.BATTLEFIELD, playerA, lorehold);
        addCard(Zone.HAND, playerA, "Squire");
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Mountain");

        setChoice(playerA, "Squire");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.DRAW);
        execute();

        assertGraveyardCount(playerA, "Squire", 1);
        assertHandCount(playerA, "Forest", 1);
    }
}
