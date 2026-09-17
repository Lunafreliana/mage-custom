package org.mage.test.cards.single.fra;

import mage.constants.CounterType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class OverwriteTheMultiverseTest extends CardTestPlayerBase {

    @Test
    public void exilesCreaturesAndEmpowersNewJace() {
        addCard(Zone.HAND, playerA, "Overwrite the Multiverse");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 6);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Darksteel Myr", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Overwrite the Multiverse");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Jace Token", 1);
        assertCounterCount(playerA, "Jace Token", CounterType.LOYALTY, 3);
        assertExileCount("Grizzly Bears", 1);
        assertExileCount("Darksteel Myr", 2);
    }
}
