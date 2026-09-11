package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.game.command.Plane;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommander3PlayersFFA;

/**
 * Multiplayer departure coverage for the shared planar controller.
 */
public class PlanarControllerPlayerLeftTest extends CardTestCommander3PlayersFFA {

    @Test
    public void testActivePlayerDepartureTransfersPlaneWithoutReplacingIt() {
        gameOptions.planeChase = true;

        concede(1, PhaseStep.PRECOMBAT_MAIN, playerA);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Plane plane = currentGame.getState().getCurrentPlane();
        Assert.assertNotNull("The shared plane must remain after its controller leaves", plane);
        Assert.assertEquals(Planes.PLANE_FIELDS_OF_SUMMER, plane.getPlaneType());
        // The three-player fixture's turn order after player A is player C.
        Assert.assertEquals(playerC.getId(), currentGame.getPlanarControllerId(plane.getId()));
        Assert.assertEquals(playerC.getId(), plane.getControllerId());
    }
}
