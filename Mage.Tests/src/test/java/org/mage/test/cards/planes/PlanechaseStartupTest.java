package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.watchers.common.PlaneswalkedWatcher;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class PlanechaseStartupTest extends CardTestPlayerBase {

    @Test
    public void startingPlaneIsNotAPlaneswalkButLaterPlaneswalkIs() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_FIELDS_OF_SUMMER,
                Planes.PLANE_PANOPTICON
        );

        runCode("check startup and perform a real planeswalk", 1, PhaseStep.UPKEEP, playerA,
                (info, player, game) -> {
                    PlaneswalkedWatcher watcher = game.getState().getWatcher(PlaneswalkedWatcher.class);
                    Assert.assertEquals(info, "Plane - Fields of Summer",
                            game.getState().getFaceUpPlanes().get(0).getName());
                    Assert.assertTrue(info + " -- setup must not create a stack object", game.getStack().isEmpty());
                    Assert.assertEquals(info + " -- setup must not emit PLANESWALKED", 0, watcher.getCount());

                    int handSize = player.getHand().size();
                    Assert.assertTrue(info, game.planeswalk(player.getId()));
                    game.checkStateAndTriggered();

                    Assert.assertEquals(info, "Plane - Panopticon",
                            game.getState().getFaceUpPlanes().get(0).getName());
                    Assert.assertEquals(info + " -- a real planeswalk must emit PLANESWALKED", 1, watcher.getCount());
                    Assert.assertEquals(info + " -- Panopticon's planeswalk trigger must use the stack",
                            1, game.getStack().size());
                    game.getStack().resolve(game);
                    Assert.assertEquals(info, handSize + 1, player.getHand().size());
                });

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}
