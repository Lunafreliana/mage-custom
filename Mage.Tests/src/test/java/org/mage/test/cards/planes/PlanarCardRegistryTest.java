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
import java.util.Collections;
import java.util.List;

public class PlanarCardRegistryTest {

    @Test
    public void testRegistryDiscoversAndConstructsBothTypes() {
        Assert.assertEquals(Planes.values().length + Phenomena.values().length,
                PlanarCardRegistry.getAvailableCards().size());
        for (PlanarCardRegistry.Metadata metadata : PlanarCardRegistry.getAvailableCards()) {
            PlanarCard card = PlanarCardRegistry.create(metadata.getId());
            Assert.assertNotNull(metadata.getId(), card);
            Assert.assertEquals(metadata.getType(), card.getPlanarCardType());
            Assert.assertFalse(metadata.getSetCode().isEmpty());
            Assert.assertEquals(card.getName(), metadata.getImageName());
            Assert.assertFalse(metadata.getEnglishName().startsWith("Plane - "));
            Assert.assertFalse(metadata.getEnglishName().startsWith("Phenomenon - "));
        }
        Assert.assertNull(PlanarCardRegistry.create("plane:not_registered"));
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
        Assert.assertEquals(CardType.PHENOMENON,
                PlanarCardRegistry.getMetadata(PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY)).getType());
    }
}
