package org.mage.test.cards.planes;

import mage.constants.Planes;
import mage.game.command.Plane;
import mage.game.command.planes.AgyremPlane;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MultipleFaceUpPlanesTest extends CardTestPlayerBase {

    @Test
    public void testPlaneswalkBottomsEveryFaceUpPlane() {
        addPlane(playerA, Planes.PLANE_FIELDS_OF_SUMMER);
        Assert.assertTrue(currentGame.addPlane(new AgyremPlane(), playerA.getId()));
        List<UUID> walkedAwayIds = currentGame.getState().getFaceUpPlanes().stream()
                .map(Plane::getId)
                .collect(Collectors.toList());
        Assert.assertEquals(2, walkedAwayIds.size());

        Assert.assertTrue(currentGame.planeswalk(playerA.getId()));

        Assert.assertEquals(1, currentGame.getState().getFaceUpPlanes().size());
        Assert.assertEquals(1, currentGame.getState().getSharedPlanarDeck().size());
        UUID destinationId = currentGame.getState().getFaceUpPlanes().get(0).getId();
        Assert.assertTrue(walkedAwayIds.contains(destinationId));
        Assert.assertFalse(currentGame.getState().getSharedPlanarDeck().getOrder().contains(destinationId));
    }
}
