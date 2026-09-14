package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.game.command.PlanarDeckMode;
import mage.game.command.Plane;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommander3PlayersFFA;

import java.util.Arrays;
import java.util.Collections;

/**
 * Multiplayer departure coverage for the planar controller.
 */
public class PlanarControllerPlayerLeftTest extends CardTestCommander3PlayersFFA {

    @Test
    public void testActivePlayerDepartureTransfersPlaneWithoutReplacingIt() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);

        concede(1, PhaseStep.PRECOMBAT_MAIN, playerA);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Assert.assertEquals("The shared plane must remain after its controller leaves", 1,
                currentGame.getState().getFaceUpPlanes().size());
        Plane plane = currentGame.getState().getFaceUpPlanes().get(0);
        Assert.assertEquals(Planes.PLANE_FIELDS_OF_SUMMER, plane.getPlaneType());
        // The three-player fixture's turn order after player A is player C.
        Assert.assertEquals(playerC.getId(), currentGame.getPlanarControllerId(plane.getId()));
        Assert.assertEquals(playerC.getId(), plane.getControllerId());
    }

    @Test
    public void testIndividualPlaneLeavesWithOwnerAndSuccessorRevealsOwnPlane() {
        gameOptions.planeChase = true;
        // Bootstrap with a deterministic shared Plane, then install individual decks
        // in-game so the test controls their exact order.
        gameOptions.planarDeckMode = PlanarDeckMode.SHARED;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);

        runCode("install individual planar decks and put A's Plane face up",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
                    Plane akoum = Plane.createPlane(Planes.PLANE_AKOUM);
                    Plane agyrem = Plane.createPlane(Planes.PLANE_AGYREM);
                    akoum.setPlanarDeckOwnerId(playerA.getId());
                    agyrem.setPlanarDeckOwnerId(playerA.getId());
                    game.getState().setPlayerPlanarDeck(
                            playerA.getId(), Arrays.asList(akoum, agyrem), false);

                    Plane naya = Plane.createPlane(Planes.PLANE_NAYA);
                    naya.setPlanarDeckOwnerId(playerB.getId());
                    game.getState().setPlayerPlanarDeck(
                            playerB.getId(), Collections.singletonList(naya), false);

                    Plane bant = Plane.createPlane(Planes.PLANE_BANT);
                    bant.setPlanarDeckOwnerId(playerC.getId());
                    game.getState().setPlayerPlanarDeck(
                            playerC.getId(), Collections.singletonList(bant), false);

                    // The bootstrap Plane belongs to A once individual mode starts.
                    game.getState().getFaceUpPlanarCards()
                            .forEach(card -> card.setPlanarDeckOwnerId(playerA.getId()));
                    game.getState().setPlanarDeckMode(PlanarDeckMode.INDIVIDUAL);

                    Assert.assertTrue(info, game.planeswalk(playerA.getId()));
                    Assert.assertEquals(info, 1, game.getState().getFaceUpPlanes().size());
                    Plane faceUp = game.getState().getFaceUpPlanes().get(0);
                    Assert.assertEquals(info, Planes.PLANE_AKOUM, faceUp.getPlaneType());
                    Assert.assertEquals(info, playerA.getId(), faceUp.getPlanarDeckOwnerId());
                });

        // A leaves during A's own turn. In this fixture C is the next player in
        // turn order, so A's owned Plane must leave the game and C immediately
        // reveals the top card of C's own planar deck.
        concede(1, PhaseStep.PRECOMBAT_MAIN, playerA);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Assert.assertEquals("Exactly the successor's replacement Plane should be face up", 1,
                currentGame.getState().getFaceUpPlanes().size());
        Plane replacement = currentGame.getState().getFaceUpPlanes().get(0);
        Assert.assertEquals("A's Akoum must disappear with A; C should reveal Bant",
                Planes.PLANE_BANT, replacement.getPlaneType());
        Assert.assertEquals(playerC.getId(), replacement.getPlanarDeckOwnerId());
        Assert.assertEquals(playerC.getId(), currentGame.getPlanarControllerId(replacement.getId()));
        Assert.assertEquals(playerC.getId(), replacement.getControllerId());
    }
}
