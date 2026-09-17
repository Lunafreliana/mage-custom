package org.mage.test.cards.single.moc;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class PathOfThePyromancerTest extends CardTestPlayerBase {

    @Test
    public void discardsHandAddsManaAndDrawsOneExtraCard() {
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 5);
        addCard(Zone.HAND, playerA, "Path of the Pyromancer");
        addCard(Zone.HAND, playerA, "Grizzly Bears", 2);
        addCard(Zone.LIBRARY, playerA, "Island", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Path of the Pyromancer");
        setChoice(playerA, true);
        setChoice(playerB, false);
        checkManaPool("mana from discarded cards", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "R", 2);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 2);
        assertHandCount(playerA, "Island", 3);
    }

    @Test
    public void chaosWinsTiedVote() {
        addPlane(playerA, Planes.PLANE_STRONGHOLD_FURNACE);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 5);
        addCard(Zone.HAND, playerA, "Path of the Pyromancer");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Path of the Pyromancer");
        setChoice(playerA, true);
        setChoice(playerB, false);
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
    }
}
