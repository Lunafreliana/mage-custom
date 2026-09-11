package org.mage.test.cards.single.msc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class CosmicCrucibleTest extends CardTestPlayerBase {

    @Test
    public void testManaAndCopyOnlyOnceEachTurn() {
        addCard(Zone.BATTLEFIELD, playerA, "Cosmic Crucible");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.HAND, playerA, "Shock", 2);

        setChoiceAmount(playerA, 0, 4, 0, 0, 0);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkManaPool("first main phase mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "U", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Shock", playerB);
        setChoice(playerA, true); // copy the first Shock
        setChoice(playerA, false); // keep the copy's target
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Shock", playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 14);
    }

    @Test
    public void testDecliningFirstCopyLeavesLaterCopyAvailable() {
        addCard(Zone.BATTLEFIELD, playerA, "Cosmic Crucible");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, "Bear Cub");
        addCard(Zone.HAND, playerA, "Shock", 2);

        setChoiceAmount(playerA, 4, 0, 0, 0, 0);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Bear Cub");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Shock", playerB);
        setChoice(playerA, false); // decline the first opportunity
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Shock", playerB);
        setChoice(playerA, true); // copy the second Shock
        setChoice(playerA, false); // keep the copy's target

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 14);
        assertPermanentCount(playerA, "Bear Cub", 1);
    }
}
