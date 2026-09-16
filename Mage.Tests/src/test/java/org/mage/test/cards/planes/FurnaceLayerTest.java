package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.events.GameEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class FurnaceLayerTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndUpkeepSelectRandomPlayersToDiscard() {
        addPlane(playerA, Planes.PLANE_FURNACE_LAYER);
        // A single card makes each discard automatic regardless of which player is selected.
        // If both triggers select the same player, the second resolves with that player's hand empty.
        addCard(Zone.HAND, playerA, "Mountain");
        addCard(Zone.HAND, playerB, "Mountain");
        removeAllCardsFromLibrary(playerA);
        removeAllCardsFromLibrary(playerB);
        skipInitShuffling();

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        runCode("two random targets discarded lands", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    int handCards = game.getPlayer(playerA.getId()).getHand().size()
                            + game.getPlayer(playerB.getId()).getHand().size();
                    int totalLife = game.getPlayer(playerA.getId()).getLife()
                            + game.getPlayer(playerB.getId()).getLife();
                    int discardedCards = 2 - handCards;
                    Assert.assertTrue(info + " -- at least one selected player discards", discardedCards >= 1);
                    Assert.assertTrue(info + " -- no more than one card per trigger is discarded", discardedCards <= 2);
                    Assert.assertEquals(info + " -- each discarded land causes 3 life loss",
                            40 - 3 * discardedCards, totalLife);
                });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void chaosMayDestroyTargetNonlandPermanent() {
        addPlane(playerA, Planes.PLANE_FURNACE_LAYER);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Forest");

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        runCode("chaos ensues", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            game.fireEvent(new GameEvent(
                    GameEvent.EventType.CHAOS_ENSUES,
                    game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId()
            ));
            game.checkStateAndTriggered();
        });
        addTarget(playerA, "Grizzly Bears");
        setChoice(playerA, true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerB, "Grizzly Bears", 1);
        assertPermanentCount(playerB, "Forest", 1);
    }

    @Test
    public void registryExposesFurnaceLayerMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_FURNACE_LAYER);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Furnace Layer", metadata.getEnglishName());
        Assert.assertEquals("Plane - Furnace Layer", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
