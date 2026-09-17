package org.mage.test.cards.single.mom;

import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class PathOfTheSchemerTest extends CardTestPlayerBase {

    private static final String path = "Path of the Schemer";

    private void prepareSpell() {
        addCard(Zone.HAND, playerA, path);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 5);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Silvercoat Lion");
        addCard(Zone.LIBRARY, playerB, "Hill Giant");
        addCard(Zone.LIBRARY, playerB, "Memnite");
        skipInitShuffling();
        setStrictChooseMode(true);
    }

    @Test
    public void millsReanimatesAndMakesCreatureAnArtifact() {
        prepareSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        setChoice(playerA, "Hill Giant");
        setChoice(playerA, false);
        setChoice(playerB, false);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, 3);
        assertGraveyardCount(playerB, 1);
        assertPermanentCount(playerA, "Hill Giant", 1);
        assertType("Hill Giant", CardType.CREATURE, true);
        assertType("Hill Giant", CardType.ARTIFACT, true);
    }

    @Test
    public void planeswalkVoteUsesPlanechaseOperation() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(Planes.PLANE_AKOUM, Planes.PLANE_INYS_HAEN);
        prepareSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        setChoice(playerA, "Hill Giant");
        setChoice(playerA, true);
        setChoice(playerB, true);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Assert.assertEquals("Plane - Inys Haen", currentGame.getState().getFaceUpPlanes().get(0).getName());
    }

    @Test
    public void tiedVoteCausesChaos() {
        addPlane(playerA, Planes.PLANE_GOLDMEADOW);
        prepareSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, path);
        setChoice(playerA, "Hill Giant");
        setChoice(playerA, true);
        setChoice(playerB, false);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Goat Token", 1);
    }
}
