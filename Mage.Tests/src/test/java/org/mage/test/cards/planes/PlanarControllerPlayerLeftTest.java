package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.game.command.Plane;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommander3PlayersFFA;

import java.util.Collections;

/**
 * Multiplayer departure coverage for the shared planar controller.
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
}
