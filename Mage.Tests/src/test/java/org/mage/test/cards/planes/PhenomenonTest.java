package org.mage.test.cards.planes;

import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Phenomenon;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.phenomena.InterplanarTunnelPhenomenon;
import mage.game.command.phenomena.MutualEpiphanyPhenomenon;
import mage.game.command.phenomena.SpatialMergingPhenomenon;
import mage.game.stack.StackObject;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
    public void testMixedRegistryIdDeckStartsWithAConfiguredPlane() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarCardIds = Arrays.asList(
                PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY),
                PlanarCardRegistry.getId(Planes.PLANE_FIELDS_OF_SUMMER),
                PlanarCardRegistry.getId(Planes.PLANE_AKOUM));

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        String startingPlane = currentGame.getState().getFaceUpPlanes().get(0).getName();
        Assert.assertTrue(startingPlane.equals("Plane - Fields of Summer")
                || startingPlane.equals("Plane - Akoum"));
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

    @Test
    public void testSpatialMergingPlaneswalksToTwoPlanesSimultaneously() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_FIELDS_OF_SUMMER,
                Planes.PLANE_AKOUM,
                Planes.PLANE_AGYREM);

        runCode("encounter Spatial Merging", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            // Preserve the initialized, deterministic next two cards while the
            // addPhenomenon test seam turns Spatial Merging face up.
            PlanarCard first = game.getState().getSharedPlanarDeck().draw();
            PlanarCard second = game.getState().getSharedPlanarDeck().draw();
            Assert.assertTrue(info, game.addPhenomenon(new SpatialMergingPhenomenon(), player.getId()));
            game.getState().getSharedPlanarDeck().putOnBottom(first);
            game.getState().getSharedPlanarDeck().putOnBottom(second);

            game.checkStateAndTriggered();
            Assert.assertEquals(info, 1, game.getStack().size());
            game.getStack().resolve(game);

            Assert.assertEquals(info, 2, game.getState().getFaceUpPlanes().size());
            Assert.assertEquals(info, "Plane - Akoum", game.getState().getFaceUpPlanes().get(0).getName());
            Assert.assertEquals(info, "Plane - Agyrem", game.getState().getFaceUpPlanes().get(1).getName());
            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testInterplanarTunnelChoosesTheNextPlane() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarCardIds = Arrays.asList(
                PlanarCardRegistry.getId(Planes.PLANE_FIELDS_OF_SUMMER),
                PlanarCardRegistry.getId(Planes.PLANE_AGYREM),
                PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY),
                PlanarCardRegistry.getId(Planes.PLANE_AKOUM),
                PlanarCardRegistry.getId(Planes.PLANE_ASTRAL_ARENA),
                PlanarCardRegistry.getId(Planes.PLANE_BANT),
                PlanarCardRegistry.getId(Planes.PLANE_EDGE_OF_MALACOL));

        setChoice(playerA, "Plane - Bant");
        runCode("encounter Interplanar Tunnel", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            List<PlanarCard> planarDeck = new ArrayList<>();
            PlanarCard card;
            while ((card = game.getState().getSharedPlanarDeck().draw()) != null) {
                planarDeck.add(card);
            }
            Assert.assertTrue(info, game.addPhenomenon(new InterplanarTunnelPhenomenon(), player.getId()));
            planarDeck.forEach(game.getState().getSharedPlanarDeck()::putOnBottom);
            game.checkStateAndTriggered();
            Assert.assertEquals(info, 1, game.getStack().size());
            game.getStack().resolve(game);
            game.checkStateAndTriggered();

            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
            Assert.assertEquals(info, 1, game.getState().getFaceUpPlanes().size());
            Assert.assertEquals(info, "Plane - Bant", game.getState().getFaceUpPlanes().get(0).getName());
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testInterplanarTunnelPreservesDeckWhenItCannotRevealFivePlanes() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_FIELDS_OF_SUMMER,
                Planes.PLANE_AGYREM,
                Planes.PLANE_AKOUM,
                Planes.PLANE_ASTRAL_ARENA,
                Planes.PLANE_BANT);

        runCode("encounter Interplanar Tunnel with only four planes left", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, (info, player, game) -> {
                    List<PlanarCard> planarDeck = new ArrayList<>();
                    PlanarCard card;
                    while ((card = game.getState().getSharedPlanarDeck().draw()) != null) {
                        planarDeck.add(card);
                    }
                    Assert.assertTrue(info,
                            game.addPhenomenon(new InterplanarTunnelPhenomenon(), player.getId()));
                    planarDeck.forEach(game.getState().getSharedPlanarDeck()::putOnBottom);
                    List<UUID> originalOrder = game.getState().getSharedPlanarDeck().getOrder();

                    game.checkStateAndTriggered();
                    Assert.assertEquals(info, 1, game.getStack().size());
                    game.getStack().resolve(game);

                    Assert.assertEquals(info, originalOrder,
                            game.getState().getSharedPlanarDeck().getOrder());
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
