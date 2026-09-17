package org.mage.test.cards.single.moc;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class PathOfTheGhosthunterTest extends CardTestPlayerBase {

    private static final String path = "Path of the Ghosthunter";

    @Test
    public void createsSpiritTokensAndChaosEnsuesOnATiedVote() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_FIELDS_OF_SUMMER,
                Planes.PLANE_TARDIS_BAY
        );
        addCard(Zone.HAND, playerA, path);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        setChoice(playerA, "X=2");
        setChoice(playerA, false); // Chaos
        setChoice(playerB, true); // Planeswalk, producing a tied vote
        setChoice(playerA, true); // Gain 10 life from Fields of Summer's chaos ability

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Spirit Token", 2);
        assertLife(playerA, 30);
        Assert.assertEquals("Plane - Fields of Summer",
                currentGame.getState().getFaceUpPlanes().get(0).getName());
    }

    @Test
    public void planarControllerPlaneswalksWhenPlaneswalkWinsVote() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_FIELDS_OF_SUMMER,
                Planes.PLANE_TARDIS_BAY
        );
        addCard(Zone.HAND, playerA, path);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        setChoice(playerA, "X=0");
        setChoice(playerA, true); // Planeswalk
        setChoice(playerB, true); // Planeswalk

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Spirit Token", 0);
        Assert.assertEquals("Plane - TARDIS Bay",
                currentGame.getState().getFaceUpPlanes().get(0).getName());
    }
}
