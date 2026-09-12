package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Plane;
import mage.game.command.PlanarDeckMode;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;
import java.util.Collections;

public class IndividualPlanarDeckTest extends CardTestPlayerBase {

    @Test
    public void planeswalkingPlayerUsesOwnDeckAndCardsReturnToOwnersDeck() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.planarDeckMode = PlanarDeckMode.INDIVIDUAL;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);

        runCode("install individual planar decks", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Plane akoum = Plane.createPlane(Planes.PLANE_AKOUM);
                    Plane agyrem = Plane.createPlane(Planes.PLANE_AGYREM);
                    akoum.setPlanarDeckOwnerId(playerA.getId());
                    agyrem.setPlanarDeckOwnerId(playerA.getId());
                    game.getState().setPlayerPlanarDeck(playerA.getId(), Arrays.asList(akoum, agyrem), false);

                    Plane bant = Plane.createPlane(Planes.PLANE_BANT);
                    Plane pools = Plane.createPlane(Planes.PLANE_NAYA);
                    bant.setPlanarDeckOwnerId(playerB.getId());
                    pools.setPlanarDeckOwnerId(playerB.getId());
                    game.getState().setPlayerPlanarDeck(playerB.getId(), Arrays.asList(bant, pools), false);

                    Assert.assertTrue(info, game.planeswalk(playerB.getId()));
                    Assert.assertEquals(info, "Plane - Bant",
                            game.getState().getFaceUpPlanes().get(0).getName());
                    Assert.assertEquals(info, 2, game.getState().getPlayerPlanarDeck(playerA.getId()).size());
                    Assert.assertEquals(info, 1, game.getState().getPlayerPlanarDeck(playerB.getId()).size());

                    Assert.assertTrue(info, game.planeswalk(playerA.getId()));
                    Assert.assertEquals(info, "Plane - Akoum",
                            game.getState().getFaceUpPlanes().get(0).getName());
                    Assert.assertEquals(info, 1, game.getState().getPlayerPlanarDeck(playerA.getId()).size());
                    Assert.assertEquals(info, 2, game.getState().getPlayerPlanarDeck(playerB.getId()).size());
                });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }
}
