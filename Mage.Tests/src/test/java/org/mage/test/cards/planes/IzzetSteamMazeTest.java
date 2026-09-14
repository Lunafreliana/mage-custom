package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.events.GameEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class IzzetSteamMazeTest extends CardTestPlayerBase {

    private void setupPlanechase() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_IZZET_STEAM_MAZE);
        skipInitShuffling();
    }

    @Test
    public void instantAndSorcerySpellsAreCopiedForTheirCaster() {
        setupPlanechase();
        addCard(Zone.HAND, playerA, "Divination");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);
        addCard(Zone.HAND, playerB, "Divination");
        addCard(Zone.BATTLEFIELD, playerB, "Island", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Divination");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, 4);
        assertHandCount(playerB, 5); // Four cards from Divination and the turn-two draw.
    }

    @Test
    public void chaosReducesOnlyControllerInstantAndSorcerySpellsThisTurn() {
        setupPlanechase();
        addCard(Zone.HAND, playerA, "Concentrate");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);

        runCode("resolve chaos", 1, PhaseStep.UPKEEP, playerA, (info, player, game) -> {
            game.fireEvent(new GameEvent(GameEvent.EventType.CHAOS_ENSUES,
                    game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId()));
            game.checkStateAndTriggered();
            game.getStack().resolve(game);
        });
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Concentrate");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, 6); // Concentrate and its copy each draw three cards.
    }

    @Test
    public void registryExposesIzzetSteamMaze() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_IZZET_STEAM_MAZE));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Izzet Steam Maze", metadata.getEnglishName());
        Assert.assertEquals("Plane - Izzet Steam Maze", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
