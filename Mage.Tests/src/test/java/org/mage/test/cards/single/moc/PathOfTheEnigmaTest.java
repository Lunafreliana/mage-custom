package org.mage.test.cards.single.moc;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarDeckMode;
import mage.game.command.Plane;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;
import java.util.Collections;

public class PathOfTheEnigmaTest extends CardTestPlayerBase {

    @Test
    public void targetPlayerDrawsFourCards() {
        addCard(Zone.HAND, playerA, "Path of the Enigma");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);
        addCard(Zone.LIBRARY, playerB, "Mountain", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Path of the Enigma", playerB);
        setChoice(playerA, "Yes");
        setChoice(playerB, "Yes");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerB, 4);
        assertGraveyardCount(playerA, "Path of the Enigma", 1);
    }

    @Test
    public void planeswalkVoteMovesToNextPlane() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_FIELDS_OF_SUMMER, Planes.PLANE_SOKENZAN);
        addCard(Zone.HAND, playerA, "Path of the Enigma");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);
        addCard(Zone.LIBRARY, playerA, "Mountain", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Path of the Enigma", playerA);
        setChoice(playerA, false); // Decline Fields of Summer's cast trigger.
        setChoice(playerA, "Yes");
        setChoice(playerB, "Yes");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Assert.assertEquals(Planes.PLANE_SOKENZAN,
                currentGame.getState().getFaceUpPlanes().get(0).getPlaneType());
    }

    @Test
    public void tiedVoteCausesChaos() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);
        addCard(Zone.HAND, playerA, "Path of the Enigma");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);
        addCard(Zone.LIBRARY, playerA, "Mountain", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Path of the Enigma", playerA);
        setChoice(playerA, false); // Decline Fields of Summer's cast trigger.
        setChoice(playerA, "Yes");
        setChoice(playerB, "No");
        setChoice(playerA, true); // Gain 10 life from Fields of Summer's chaos ability.

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerA, 30);
        assertLife(playerB, 20);
    }

    @Test
    public void planarControllerPlaneswalksWhenCastOnAnotherPlayersTurn() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);
        addCard(Zone.BATTLEFIELD, playerA, "Vedalken Orrery");
        addCard(Zone.HAND, playerA, "Path of the Enigma");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);

        runCode("install individual planar decks", 2, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Plane akoum = Plane.createPlane(Planes.PLANE_AKOUM);
                    akoum.setPlanarDeckOwnerId(playerA.getId());
                    game.getState().setPlayerPlanarDeck(
                            playerA.getId(), Collections.singletonList(akoum), false);

                    Plane bant = Plane.createPlane(Planes.PLANE_BANT);
                    bant.setPlanarDeckOwnerId(playerB.getId());
                    game.getState().setPlayerPlanarDeck(
                            playerB.getId(), Collections.singletonList(bant), false);

                    game.getState().getFaceUpPlanarCards().forEach(card ->
                            card.setPlanarDeckOwnerId(playerA.getId()));
                    game.getState().setPlanarDeckMode(PlanarDeckMode.INDIVIDUAL);
                });
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, "Path of the Enigma", playerA);
        setChoice(playerA, false); // Decline Fields of Summer's cast trigger.
        setChoice(playerA, "Yes");
        setChoice(playerB, "Yes");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.BEGIN_COMBAT);
        execute();

        Assert.assertEquals(Planes.PLANE_BANT,
                currentGame.getState().getFaceUpPlanes().get(0).getPlaneType());
    }
}
