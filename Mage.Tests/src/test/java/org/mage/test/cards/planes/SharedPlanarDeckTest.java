package org.mage.test.cards.planes;

import mage.game.command.Plane;
import mage.game.command.SharedPlanarDeck;
import mage.game.command.planes.AgyremPlane;
import mage.game.command.planes.FieldsOfSummerPlane;
import mage.game.command.planes.PanopticonPlane;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SharedPlanarDeckTest {

    @Test
    public void testKnownOrderAndBottomCycling() {
        SharedPlanarDeck deck = knownDeck();

        Plane first = deck.draw();
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
    public void testOrderViewIsImmutableAndContainsOnlyIds() {
        SharedPlanarDeck deck = knownDeck();
        List<UUID> order = deck.getOrder();

        Assert.assertEquals(3, order.size());
        Assert.assertThrows(UnsupportedOperationException.class, () -> order.remove(0));
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
