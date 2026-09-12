package org.mage.test.cards.planes;

import mage.constants.CardType;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.SharedPlanarDeckValidator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanarCardRegistryTest {

    @Test
    public void testRegistryDiscoversAndConstructsBothTypes() {
        Assert.assertEquals(Planes.values().length + Phenomena.values().length,
                PlanarCardRegistry.getAvailableCards().size());
        for (PlanarCardRegistry.Metadata metadata : PlanarCardRegistry.getAvailableCards()) {
            PlanarCard card = PlanarCardRegistry.create(metadata.getId());
            Assert.assertNotNull(metadata.getId(), card);
            Assert.assertEquals(metadata.getType(), card.getPlanarCardType());
            Assert.assertTrue(metadata.getSetCode().equals("PCA") || metadata.getSetCode().equals("MOC"));
            Assert.assertEquals(card.getName(), metadata.getImageName());
            Assert.assertFalse(metadata.getEnglishName().startsWith("Plane - "));
            Assert.assertFalse(metadata.getEnglishName().startsWith("Phenomenon - "));
        }
        Assert.assertNull(PlanarCardRegistry.create("plane:not_registered"));
        Assert.assertEquals("MOC", PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_TOWASHI)).getSetCode());
    }

    @Test
    public void testMixedDeckValidation() {
        List<String> deck = new ArrayList<>();
        for (Planes plane : Planes.values()) {
            deck.add(PlanarCardRegistry.getId(plane));
        }
        Assert.assertTrue(SharedPlanarDeckValidator.validate(deck, 2).isEmpty());

        deck.add(PlanarCardRegistry.getId(Planes.PLANE_AKOUM));
        List<String> errors = SharedPlanarDeckValidator.validate(deck, 2);
        Assert.assertTrue(errors.toString(), errors.stream().anyMatch(error -> error.contains("unique")));

        List<String> phenomenonOnly = Collections.nCopies(5,
                PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY));
        errors = SharedPlanarDeckValidator.validate(phenomenonOnly, 2);
        Assert.assertTrue(errors.toString(), errors.stream().anyMatch(error -> error.contains("at most 4")));
        Assert.assertTrue(errors.toString(), errors.stream().anyMatch(error -> error.contains("must contain a plane")));
    }

    @Test
    public void testMetadataTypes() {
        Assert.assertEquals(CardType.PLANE,
                PlanarCardRegistry.getMetadata(PlanarCardRegistry.getId(Planes.PLANE_AKOUM)).getType());
        Assert.assertEquals("Eloren Wilds",
                PlanarCardRegistry.getMetadata(PlanarCardRegistry.getId(Planes.PLANE_ELOREN_WILDS)).getEnglishName());
        Assert.assertEquals(CardType.PHENOMENON,
                PlanarCardRegistry.getMetadata(PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY)).getType());
    }

    @Test
    public void testRegistryIdsAreUniqueAndStableByType() {
        List<PlanarCardRegistry.Metadata> metadata = PlanarCardRegistry.getAvailableCards();
        Set<String> ids = metadata.stream().map(PlanarCardRegistry.Metadata::getId).collect(Collectors.toSet());

        Assert.assertEquals(metadata.size(), ids.size());
        Assert.assertTrue(metadata.stream()
                .filter(entry -> entry.getType() == CardType.PLANE)
                .allMatch(entry -> entry.getId().startsWith("plane:")));
        Assert.assertTrue(metadata.stream()
                .filter(entry -> entry.getType() == CardType.PHENOMENON)
                .allMatch(entry -> entry.getId().startsWith("phenomenon:")));
    }

    @Test
    public void testFactoryReturnsIndependentRuntimeObjects() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_AKOUM);
        PlanarCard first = PlanarCardRegistry.create(id);
        PlanarCard second = PlanarCardRegistry.create(id);

        Assert.assertNotSame(first, second);
        Assert.assertNotEquals(first.getId(), second.getId());
        first.setFaceUp(true);
        Assert.assertFalse(second.isFaceUp());
    }

    @Test
    public void testRegistryCatalogCannotBeMutatedByCallers() {
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> PlanarCardRegistry.getAvailableCards().clear());
    }

    @Test
    public void testUnknownMetadataAndFactoryAreBothNull() {
        Assert.assertNull(PlanarCardRegistry.getMetadata("phenomenon:not_registered"));
        Assert.assertNull(PlanarCardRegistry.create("phenomenon:not_registered"));
    }

    @Test
    public void testSharedMinimumCapsAtFortyCards() {
        List<String> deck = new ArrayList<>();
        Arrays.stream(Planes.values())
                .map(PlanarCardRegistry::getId)
                .forEach(deck::add);

        Assert.assertTrue(SharedPlanarDeckValidator.validate(deck, 5).stream()
                .anyMatch(error -> error.contains("at least 40")));
    }

    @Test
    public void testSharedMinimumScalesBelowFourPlayers() {
        List<String> deck = new ArrayList<>();
        Arrays.stream(Planes.values()).limit(19)
                .map(PlanarCardRegistry::getId)
                .forEach(deck::add);

        Assert.assertTrue(SharedPlanarDeckValidator.validate(deck, 2).stream()
                .anyMatch(error -> error.contains("at least 20")));
        deck.add(PlanarCardRegistry.getId(Planes.values()[19]));
        Assert.assertFalse(SharedPlanarDeckValidator.validate(deck, 2).stream()
                .anyMatch(error -> error.contains("needs at least")));
    }
}
