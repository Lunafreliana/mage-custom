package org.mage.test.cards.single.moc;

import mage.abilities.keyword.FlyingAbility;
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
    public void createsXSpiritsAndPlaneswalks() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(Planes.PLANE_AKOUM, Planes.PLANE_PANOPTICON);
        addCard(Zone.HAND, playerA, path);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        setChoice(playerA, "X=3");
        setChoice(playerA, true); // Planeswalk
        setChoice(playerB, true); // Planeswalk

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Spirit Token", 3);
        assertAbility(playerA, "Spirit Token", FlyingAbility.getInstance(), true, 3);
        Assert.assertEquals(Planes.PLANE_PANOPTICON,
                currentGame.getState().getFaceUpPlanes().get(0).getPlaneType());
        assertGraveyardCount(playerA, path, 1);
    }
}
