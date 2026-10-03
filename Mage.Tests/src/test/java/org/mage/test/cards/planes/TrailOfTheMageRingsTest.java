package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class TrailOfTheMageRingsTest extends CardTestPlayerBase {

    @Test
    public void grantsReboundToEveryPlayersInstantsAndSorceries() {
        addPlane(playerA, Planes.PLANE_TRAIL_OF_THE_MAGE_RINGS);
        addCard(Zone.HAND, playerA, "Volcanic Hammer");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.HAND, playerB, "Lightning Bolt");
        addCard(Zone.BATTLEFIELD, playerB, "Mountain");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Volcanic Hammer", playerB);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Lightning Bolt", playerA);
        setChoice(playerB, true); // Cast Lightning Bolt via rebound during player B's upkeep.
        addTarget(playerB, playerA);
        setChoice(playerA, true); // Cast Volcanic Hammer via rebound during player A's upkeep.
        addTarget(playerA, playerB);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerA, 14);
        assertLife(playerB, 16);
        assertGraveyardCount(playerA, "Volcanic Hammer", 1);
        assertGraveyardCount(playerB, "Lightning Bolt", 1);
        assertExileCount(playerA, 0);
        assertExileCount(playerB, 0);
    }
}
