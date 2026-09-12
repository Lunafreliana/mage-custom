package org.mage.test.cards.planes;

import mage.constants.CardType;
import mage.game.GameState;
import mage.game.command.Plane;
import mage.game.command.SharedPlanarDeck;
import mage.game.command.planes.AgyremPlane;
import mage.game.command.planes.FieldsOfSummerPlane;
import mage.game.command.planes.PanopticonPlane;
import mage.util.RandomUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SharedPlanarDeckTest {

    @Test
    public void testKnownOrderAndBottomCycling() {
        SharedPlanarDeck deck = knownDeck();

        Plane first = (Plane) deck.draw();
        Assert.assertEquals("Plane - Fields of Summer", first.getName());
        deck.putOnBottom(first);

        Assert.assertEquals("Plane - Agyrem", deck.draw().getName());
        Assert.assertEquals("Plane - Panopticon", deck.draw().getName());
        Assert.assertEquals("Plane - Fields of Summer", deck.draw().getName());
    }

    @Test
    public void testCopyPreservesOrderAndIsIndependent() {
        SharedPlanarDeck original = knownDeck();
        SharedPlanarDeck copy = original.copy();

        Assert.assertEquals(original.getOrder(), copy.getOrder());
        copy.draw();

        Assert.assertEquals(3, original.size());
        Assert.assertEquals(2, copy.size());
    }

    @Test
    public void testShuffleUsesRandomUtilAndDoesNotPreserveConfiguredOrder() {
        List<Plane> planes = Arrays.asList(
                new FieldsOfSummerPlane(),
                new AgyremPlane(),
                new PanopticonPlane());
        List<UUID> configuredOrder = Arrays.asList(
                planes.get(0).getId(),
                planes.get(1).getId(),
                planes.get(2).getId());

        RandomUtil.setSeed(123L);
        SharedPlanarDeck first = new SharedPlanarDeck();
        first.setPlanes(planes, true);
        RandomUtil.setSeed(123L);
        SharedPlanarDeck second = new SharedPlanarDeck();
        second.setPlanes(planes, true);

        Assert.assertEquals(first.getOrder(), second.getOrder());
        Assert.assertNotEquals(configuredOrder, first.getOrder());
        RandomUtil.setSeed(System.nanoTime());
    }

    @Test
    public void testOrderViewIsImmutableAndContainsOnlyIds() {
        SharedPlanarDeck deck = knownDeck();
        List<UUID> order = deck.getOrder();

        Assert.assertEquals(3, order.size());
        Assert.assertThrows(UnsupportedOperationException.class, () -> order.remove(0));
    }

    @Test
    public void testRuntimeMetadataAndFaceStateSurviveCopy() {
        SharedPlanarDeck deck = knownDeck();
        Plane plane = (Plane) deck.draw();

        Assert.assertEquals(CardType.PLANE, plane.getPlanarCardType());
        Assert.assertEquals(deck.getId(), plane.getPlanarDeckId());
        Assert.assertFalse(plane.isFaceUp());

        plane.setFaceUp(true);
        Plane copy = plane.copy();
        Assert.assertEquals(plane.getId(), copy.getId());
        Assert.assertEquals(deck.getId(), copy.getPlanarDeckId());
        Assert.assertTrue(copy.isFaceUp());

        deck.putOnBottom(plane);
        Assert.assertFalse(plane.isFaceUp());
        Assert.assertEquals(1, plane.getZoneChangeCounter(null));
    }

    @Test
    public void testFaceUpCollectionSupportsZeroOneAndMultiplePlanes() {
        GameState state = new GameState();
        Plane fields = new FieldsOfSummerPlane();
        Plane agyrem = new AgyremPlane();

        Assert.assertTrue(state.getFaceUpPlanarCards().isEmpty());
        fields.setFaceUp(true);
        state.addCommandObject(fields);
        Assert.assertEquals(1, state.getFaceUpPlanes().size());
        agyrem.setFaceUp(true);
        state.addCommandObject(agyrem);
        Assert.assertEquals(2, state.getFaceUpPlanes().size());
        Assert.assertTrue(state.hasFaceUpPlane(fields.getPlaneType()));
        Assert.assertTrue(state.isFaceUpPlanarCard(agyrem.getId()));
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> state.getFaceUpPlanes().remove(0));
    }

    @Test
    public void testEmptyDeckDrawIsSafe() {
        SharedPlanarDeck deck = new SharedPlanarDeck();

        Assert.assertTrue(deck.isEmpty());
        Assert.assertNull(deck.draw());
        Assert.assertEquals(0, deck.size());
    }

    @Test
    public void testSetPlanesDefensivelyCopiesInputCards() {
        Plane original = new FieldsOfSummerPlane();
        SharedPlanarDeck deck = new SharedPlanarDeck();
        deck.setPlanes(Arrays.asList(original), false);

        Plane stored = (Plane) deck.draw();

        Assert.assertNotSame(original, stored);
        Assert.assertEquals(original.getId(), stored.getId());
        Assert.assertNull(original.getPlanarDeckId());
        Assert.assertEquals(deck.getId(), stored.getPlanarDeckId());
    }

    @Test
    public void testReplacingDeckContentsClearsOldOrder() {
        SharedPlanarDeck deck = knownDeck();
        Plane replacement = new AstralArenaPlane();

        deck.setPlanes(Arrays.asList(replacement), false);

        Assert.assertEquals(1, deck.size());
        Assert.assertEquals(replacement.getId(), deck.getOrder().get(0));
    }

    @Test
    public void testBottomingFaceUpCardChangesItsPlanarIncarnationOnce() {
        SharedPlanarDeck deck = knownDeck();
        Plane plane = (Plane) deck.draw();
        plane.setFaceUp(true);

        deck.putOnBottom(plane);

        Assert.assertFalse(plane.isFaceUp());
        Assert.assertEquals(1, plane.getZoneChangeCounter(null));
        Assert.assertEquals(plane.getId(), deck.getOrder().get(deck.size() - 1));
    }

    @Test
    public void testBottomingAlreadyFaceDownCardDoesNotChangeIncarnation() {
        SharedPlanarDeck deck = new SharedPlanarDeck();
        Plane plane = new FieldsOfSummerPlane();

        deck.putOnBottom(plane);

        Assert.assertEquals(0, plane.getZoneChangeCounter(null));
        Assert.assertEquals(deck.getId(), plane.getPlanarDeckId());
    }

    @Test
    public void testCopiedDeckRetainsDeckIdentityButNotCardInstances() {
        SharedPlanarDeck original = knownDeck();
        SharedPlanarDeck copy = original.copy();

        Plane originalTop = (Plane) original.draw();
        Plane copiedTop = (Plane) copy.draw();

        Assert.assertEquals(original.getId(), copy.getId());
        Assert.assertEquals(originalTop.getId(), copiedTop.getId());
        Assert.assertNotSame(originalTop, copiedTop);
        copiedTop.setFaceUp(true);
        Assert.assertFalse(originalTop.isFaceUp());
    }

    @Test
    public void testClearRemovesEveryCardButPreservesDeckIdentity() {
        SharedPlanarDeck deck = knownDeck();
        UUID deckId = deck.getId();

        deck.clear();

        Assert.assertTrue(deck.isEmpty());
        Assert.assertEquals(deckId, deck.getId());
    }

    private static SharedPlanarDeck knownDeck() {
        SharedPlanarDeck deck = new SharedPlanarDeck();
        deck.setPlanes(Arrays.asList(
                new FieldsOfSummerPlane(),
                new AgyremPlane(),
                new PanopticonPlane()
        ), false);
        return deck;
    }
}
