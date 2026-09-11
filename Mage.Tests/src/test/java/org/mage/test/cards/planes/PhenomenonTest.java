package org.mage.test.cards.planes;

import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Phenomenon;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.phenomena.MutualEpiphanyPhenomenon;
import mage.game.stack.StackObject;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;
import java.util.Collections;

public class PhenomenonTest extends CardTestPlayerBase {

    @Test
    public void testRuntimeCharacteristicsAndCopy() {
        Phenomenon phenomenon = new MutualEpiphanyPhenomenon();
        phenomenon.setFaceUp(true);
        Phenomenon copy = phenomenon.copy();

        Assert.assertEquals(CardType.PHENOMENON, copy.getPlanarCardType());
        Assert.assertEquals(Collections.singletonList(CardType.PHENOMENON), copy.getCardType(currentGame));
        Assert.assertTrue(copy.getSubtype().isEmpty());
        Assert.assertTrue(copy.isFaceUp());
        copy.setFaceUp(false);
        Assert.assertEquals(1, copy.getZoneChangeCounter(currentGame));
    }

    @Test
    public void testSetupSkipsPhenomenonWithoutEncounteringIt() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarPhenomena = Collections.singletonList(Phenomena.MUTUAL_EPIPHANY);
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        Assert.assertEquals(1, currentGame.getState().getFaceUpPlanes().size());
        Assert.assertTrue(currentGame.getState().getFaceUpPhenomena().isEmpty());
        Assert.assertEquals(1, currentGame.getState().getSharedPlanarDeck().size());
        assertHandCount(playerA, 0);
        assertHandCount(playerB, 0);
    }

    @Test
    public void testMixedRegistryIdDeckPreservesOrder() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarCardIds = Arrays.asList(
                PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY),
                PlanarCardRegistry.getId(Planes.PLANE_FIELDS_OF_SUMMER),
                PlanarCardRegistry.getId(Planes.PLANE_AKOUM));

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        Assert.assertEquals("Plane - Fields of Summer",
                currentGame.getState().getFaceUpPlanes().get(0).getName());
        Assert.assertEquals(2, currentGame.getState().getSharedPlanarDeck().size());
    }

    @Test
    public void testEncounterTriggerDelaysSbaUntilResolution() {
        prepareStartedPlanechaseGame();
        runCode("encounter and resolve phenomenon", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertTrue(game.addPhenomenon(new MutualEpiphanyPhenomenon(), player.getId()));
            game.checkStateAndTriggered();
            Assert.assertEquals(info, 1, game.getStack().size());
            Assert.assertEquals(info, 1, game.getState().getFaceUpPhenomena().size());

            int handA = playerA.getHand().size();
            int handB = playerB.getHand().size();
            game.getStack().resolve(game);
            Assert.assertEquals(info, handA + 4, playerA.getHand().size());
            Assert.assertEquals(info, handB + 4, playerB.getHand().size());

            game.checkStateAndTriggered();
            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
            Assert.assertEquals(info, 1, game.getState().getFaceUpPlanes().size());
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testRemovingEncounterTriggerMakesSbaPlaneswalk() {
        prepareStartedPlanechaseGame();
        runCode("remove encounter trigger", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertTrue(game.addPhenomenon(new MutualEpiphanyPhenomenon(), player.getId()));
            game.checkStateAndTriggered();

            StackObject encounterTrigger = game.getStack().getFirstOrNull();
            Assert.assertNotNull(info, encounterTrigger);
            game.getStack().remove(encounterTrigger, game);
            game.checkStateAndTriggered();

            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
            Assert.assertEquals(info, 1, game.getState().getFaceUpPlanes().size());
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    private void prepareStartedPlanechaseGame() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);
    }
}
