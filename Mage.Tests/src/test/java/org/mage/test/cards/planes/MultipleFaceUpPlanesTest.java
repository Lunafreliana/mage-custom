package org.mage.test.cards.planes;

import mage.abilities.effects.ContinuousEffect;
import mage.constants.Planes;
import mage.game.command.Plane;
import mage.game.command.planes.AgyremPlane;
import mage.game.command.planes.AstralArenaPlane;
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

    @Test
    public void testPlaneswalkDoesNotDiscardReusablePrintedEffects() {
        addPlane(playerA, Planes.PLANE_FIELDS_OF_SUMMER);
        AstralArenaPlane astralArena = new AstralArenaPlane();
        Assert.assertTrue(currentGame.addPlane(astralArena, playerA.getId()));
        Plane faceUpAstralArena = currentGame.getState().getFaceUpPlanes().stream()
                .filter(plane -> plane.getPlaneType() == Planes.PLANE_ASTRAL_ARENA)
                .findFirst()
                .orElseThrow(AssertionError::new);
        ContinuousEffect printedEffect = (ContinuousEffect) faceUpAstralArena
                .getAbilities().get(0).getEffects().get(0);

        Assert.assertTrue(currentGame.planeswalk(playerA.getId()));

        Assert.assertFalse("Bottoming a planar card must not mutate its printed ability", printedEffect.isDiscarded());
    }
}
