package org.mage.test.cards.planes;

import mage.abilities.effects.common.PlanechasePlanarDieResultResolver;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Phenomena;
import mage.constants.PlanarDieRollResult;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.phenomena.FixedPointInTimePhenomenon;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class FixedPointInTimeTest extends CardTestPlayerBase {

    @Test
    public void planarResultRolledByAnyPlayerCausesChaosInstead() {
        prepareStartedPlanechaseGame();
        setChoice(playerA, "Yes"); // Fields of Summer's chaos ability

        runCode("encounter Fixed Point in Time", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
            Assert.assertTrue(info, game.addPhenomenon(new FixedPointInTimePhenomenon(), player.getId()));
            game.checkStateAndTriggered();
            game.getStack().resolve(game);
            game.checkStateAndTriggered();

            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
            Assert.assertEquals(info, "Plane - Fields of Summer",
                    game.getState().getFaceUpPlanes().get(0).getName());

            Assert.assertTrue(info, PlanechasePlanarDieResultResolver.resolve(
                    PlanarDieRollResult.PLANAR_ROLL, playerB.getId(), null, game));
            game.checkStateAndTriggered();
            Assert.assertEquals(info, 1, game.getStack().size());
            game.getStack().resolve(game);
        });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 30);
        Assert.assertEquals("Plane - Fields of Summer",
                currentGame.getState().getFaceUpPlanes().get(0).getName());
    }

    @Test
    public void registryExposesDoctorWhoMetadata() {
        String id = PlanarCardRegistry.getId(Phenomena.FIXED_POINT_IN_TIME);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(CardType.PHENOMENON, metadata.getType());
        Assert.assertEquals("Fixed Point in Time", metadata.getEnglishName());
        Assert.assertEquals("Phenomenon - Fixed Point in Time", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertTrue(PlanarCardRegistry.create(id) instanceof FixedPointInTimePhenomenon);
    }

    private void prepareStartedPlanechaseGame() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);
    }
}
