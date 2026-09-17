package org.mage.test.cards.single.frc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommander4Players;

import java.util.Collections;

public class DackFaydenHelpingHandTest extends CardTestCommander4Players {

    private static final String dack = "Dack Fayden, Helping Hand";

    @Test
    public void revealsOneCreatureForEachOpponentAndGivesEachOneAway() {
        setStrictChooseMode(true);
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);

        addCard(Zone.HAND, playerA, dack);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 6);

        // Cards are added to the top, so Fugitive Wizard is revealed first.
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Hill Giant");
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Plains");
        addCard(Zone.LIBRARY, playerA, "Fugitive Wizard");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dack);
        addTarget(playerA, playerB);
        addTarget(playerA, playerC);
        addTarget(playerA, playerD);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, dack, 1);
        assertPermanentCount(playerB, "Fugitive Wizard", 1);
        assertPermanentCount(playerC, "Grizzly Bears", 1);
        assertPermanentCount(playerD, "Hill Giant", 1);
        assertLibraryCount(playerA, 3);

        Assert.assertEquals(Collections.singleton(playerA.getId()),
                getPermanent("Fugitive Wizard").getGoadingPlayers());
        Assert.assertEquals(Collections.singleton(playerA.getId()),
                getPermanent("Grizzly Bears").getGoadingPlayers());
        Assert.assertEquals(Collections.singleton(playerA.getId()),
                getPermanent("Hill Giant").getGoadingPlayers());
    }

    @Test
    public void putsAllCreaturesOntoBattlefieldWhenLibraryContainsFewerThanX() {
        setStrictChooseMode(true);
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);

        addCard(Zone.HAND, playerA, dack);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 6);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Fugitive Wizard");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dack);
        addTarget(playerA, playerB);
        addTarget(playerA, playerC);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Fugitive Wizard", 1);
        assertPermanentCount(playerC, "Grizzly Bears", 1);
        assertPermanentCount(playerD, "Fugitive Wizard", 0);
        assertPermanentCount(playerD, "Grizzly Bears", 0);
        assertLibraryCount(playerA, 0);
    }
}
