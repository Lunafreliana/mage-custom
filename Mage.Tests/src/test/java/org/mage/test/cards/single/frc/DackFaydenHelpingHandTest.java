package org.mage.test.cards.single.frc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestMultiPlayerBaseWithRangeAll;

import static org.junit.Assert.assertTrue;

/**
 * @author TheElk801
 */
public class DackFaydenHelpingHandTest extends CardTestMultiPlayerBaseWithRangeAll {

    private static final String dack = "Dack Fayden, Helping Hand";

    @Test
    public void testRevealGoadAndDistribute() {
        skipInitShuffling();
        addCard(Zone.HAND, playerA, dack);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 6);
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.LIBRARY, playerA, "Centaur Courser");
        addCard(Zone.LIBRARY, playerA, "Plains");
        addCard(Zone.LIBRARY, playerA, "Hill Giant");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dack);
        addTarget(playerA, playerD);
        addTarget(playerA, playerC);
        // PlayerB is the only eligible opponent left and is selected automatically.
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerD, "Hill Giant", 1);
        assertPermanentCount(playerC, "Centaur Courser", 1);
        assertPermanentCount(playerB, "Grizzly Bears", 1);
        assertLibraryCount(playerA, 3);
        assertTrue(getPermanent("Hill Giant").getGoadingPlayers().contains(playerA.getId()));
        assertTrue(getPermanent("Centaur Courser").getGoadingPlayers().contains(playerA.getId()));
        assertTrue(getPermanent("Grizzly Bears").getGoadingPlayers().contains(playerA.getId()));
    }
}
