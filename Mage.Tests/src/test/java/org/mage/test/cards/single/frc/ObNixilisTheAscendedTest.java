package org.mage.test.cards.single.frc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author JayDi85
 */
public class ObNixilisTheAscendedTest extends CardTestPlayerBase {

    private static final String obNixilis = "Ob Nixilis, the Ascended";

    @Test
    public void testEntersAndEndStepAbilities() {
        addCard(Zone.HAND, playerA, obNixilis);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 7);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears", 1, true);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears", 1, true);
        addCard(Zone.BATTLEFIELD, playerB, "Darksteel Myr", 1, true);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, obNixilis);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, obNixilis, 1);
        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerB, "Grizzly Bears", 0);
        assertPermanentCount(playerB, "Darksteel Myr", 1);
        assertPermanentCount(playerB, "Hill Giant", 1);
        assertLife(playerA, 21);
        assertPermanentCount(playerA, "Angel Token", 1);
    }

    @Test
    public void testNoLifeGainMeansNoAngel() {
        addCard(Zone.BATTLEFIELD, playerA, obNixilis);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerA, 20);
        assertPermanentCount(playerA, "Angel Token", 0);
    }
}
