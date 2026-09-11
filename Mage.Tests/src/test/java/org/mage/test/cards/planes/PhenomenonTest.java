package org.mage.test.cards.planes;

import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.game.command.Phenomenon;
import mage.game.command.phenomena.MutualEpiphanyPhenomenon;
import mage.game.stack.StackObject;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

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
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarPhenomena = Collections.singletonList(Phenomena.MUTUAL_EPIPHANY);
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        Assert.assertEquals(1, currentGame.getState().getFaceUpPlanes().size());
        Assert.assertTrue(currentGame.getState().getFaceUpPhenomena().isEmpty());
        Assert.assertEquals(1, currentGame.getState().getSharedPlanarDeck().size());
        Assert.assertEquals(7, playerA.getHand().size());
        Assert.assertEquals(7, playerB.getHand().size());
    }

    @Test
    public void testEncounterTriggerDelaysSbaUntilResolution() {
        addPlane(playerA, Planes.PLANE_FIELDS_OF_SUMMER);
        Assert.assertTrue(currentGame.addPhenomenon(new MutualEpiphanyPhenomenon(), playerA.getId()));

        currentGame.checkStateAndTriggered();
        Assert.assertEquals(1, currentGame.getStack().size());
        Assert.assertEquals(1, currentGame.getState().getFaceUpPhenomena().size());

        int handA = playerA.getHand().size();
        int handB = playerB.getHand().size();
        currentGame.getStack().resolve(currentGame);
        Assert.assertEquals(handA + 4, playerA.getHand().size());
        Assert.assertEquals(handB + 4, playerB.getHand().size());

        currentGame.checkStateAndTriggered();
        Assert.assertTrue(currentGame.getState().getFaceUpPhenomena().isEmpty());
        Assert.assertEquals(1, currentGame.getState().getFaceUpPlanes().size());
    }

    @Test
    public void testRemovingEncounterTriggerMakesSbaPlaneswalk() {
        addPlane(playerA, Planes.PLANE_FIELDS_OF_SUMMER);
        Assert.assertTrue(currentGame.addPhenomenon(new MutualEpiphanyPhenomenon(), playerA.getId()));
        currentGame.checkStateAndTriggered();

        StackObject encounterTrigger = currentGame.getStack().getFirstOrNull();
        Assert.assertNotNull(encounterTrigger);
        currentGame.getStack().remove(encounterTrigger, currentGame);
        currentGame.checkStateAndTriggered();

        Assert.assertTrue(currentGame.getState().getFaceUpPhenomena().isEmpty());
        Assert.assertEquals(1, currentGame.getState().getFaceUpPlanes().size());
    }
}
