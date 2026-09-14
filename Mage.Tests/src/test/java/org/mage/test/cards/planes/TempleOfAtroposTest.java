package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.events.GameEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class TempleOfAtroposTest extends CardTestPlayerBase {

    @Test
    public void addsBeginningPhaseAfterPostcombatMain() {
        addPlane(playerA, Planes.PLANE_TEMPLE_OF_ATROPOS);
        addCard(Zone.BATTLEFIELD, playerA, "Agent of Masks");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerA, 22);
        assertLife(playerB, 18);
    }

    @Test
    public void chaosReversesTurnOrderAndThenPlaneswalks() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_TEMPLE_OF_ATROPOS,
                Planes.PLANE_FIELDS_OF_SUMMER
        );

        runCode("resolve chaos", 1, PhaseStep.UPKEEP, playerA,
                (info, player, game) -> {
                    game.fireEvent(new GameEvent(
                            GameEvent.EventType.CHAOS_ENSUES,
                            game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId()
                    ));
                    game.checkStateAndTriggered();
                    game.getStack().resolve(game);

                    Assert.assertTrue(info + " -- turn order must be reversed", game.isTurnOrderReversed());
                    Assert.assertEquals(info, "Plane - Fields of Summer",
                            game.getState().getFaceUpPlanes().get(0).getName());
                });

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_TEMPLE_OF_ATROPOS)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Temple of Atropos", metadata.getEnglishName());
        Assert.assertEquals("Plane - Temple of Atropos", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
