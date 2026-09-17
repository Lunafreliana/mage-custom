package org.mage.test.cards.single.moc;

import java.util.Arrays;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class PathOfTheAnimistTest extends CardTestPlayerBase {

    private static final String path = "Path of the Animist";

    private void setupPlanechase(Planes... planes) {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(planes);
        addCard(Zone.HAND, playerA, path);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 4);
        setStrictChooseMode(true);
    }

    @Test
    public void planeswalkVoteFindsLandsAndPlaneswalks() {
        setupPlanechase(Planes.PLANE_AKOUM, Planes.PLANE_PANOPTICON);
        addCard(Zone.LIBRARY, playerA, "Plains");
        addCard(Zone.LIBRARY, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        addTarget(playerA, "Plains^Island");
        setChoice(playerA, true); // Planeswalk
        setChoice(playerB, true); // Planeswalk

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Plains", 1);
        assertPermanentCount(playerA, "Island", 1);
        assertTappedCount("Plains", true, 1);
        assertTappedCount("Island", true, 1);
        Assert.assertEquals("Plane - Panopticon", currentGame.getState().getFaceUpPlanes().get(0).getName());
    }

    @Test
    public void tiedVoteCausesChaos() {
        setupPlanechase(Planes.PLANE_TOWASHI);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        addTarget(playerA, TestPlayer.TARGET_SKIP); // Search for zero lands.
        setChoice(playerA, true); // Planeswalk
        setChoice(playerB, false); // Chaos; the vote is tied.
        addTarget(playerA, "Grizzly Bears^X=3");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount("Grizzly Bears", CounterType.P1P1, 3);
        Assert.assertEquals("Plane - Towashi", currentGame.getState().getFaceUpPlanes().get(0).getName());
    }
}
