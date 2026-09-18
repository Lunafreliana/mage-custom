package org.mage.test.cards.single.moc;

import java.util.Arrays;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class PathOfThePyromancerTest extends CardTestPlayerBase {

    private static final String path = "Path of the Pyromancer";

    @Test
    public void discardsHandAddsManaAndDrawsCards() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(Planes.PLANE_AKOUM, Planes.PLANE_PANOPTICON);
        addCard(Zone.HAND, playerA, path);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 5);
        addCard(Zone.LIBRARY, playerA, "Memnite", 3);
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        setChoice(playerA, true); // Planeswalk
        setChoice(playerB, true); // Planeswalk
        checkManaPool("mana from two discarded cards", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "R", 2);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertHandCount(playerA, 3);
        assertGraveyardCount(playerA, 3);
        Assert.assertEquals("Plane - Panopticon", currentGame.getState().getFaceUpPlanes().get(0).getName());
    }

    @Test
    public void emptyHandStillDrawsOneCard() {
        addCard(Zone.HAND, playerA, path);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 5);
        addCard(Zone.LIBRARY, playerA, "Memnite");
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        setChoice(playerA, false); // Chaos
        setChoice(playerB, false); // Chaos
        checkManaPool("no mana from no discarded cards", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "R", 0);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertHandCount(playerA, 1);
        assertGraveyardCount(playerA, path, 1);
    }
}
