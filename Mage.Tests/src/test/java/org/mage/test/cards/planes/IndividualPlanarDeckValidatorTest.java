package org.mage.test.cards.planes;

import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.game.command.IndividualPlanarDeckValidator;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndividualPlanarDeckValidatorTest {

    @Test
    public void validatesEachIndividualDeckAtTenCards() {
        List<String> cards = new ArrayList<>();
        Arrays.stream(Planes.values()).limit(10)
                .map(PlanarCardRegistry::getId)
                .forEach(cards::add);

        Assert.assertTrue(IndividualPlanarDeckValidator.validate(cards).isEmpty());
        cards.remove(cards.size() - 1);
        Assert.assertTrue(IndividualPlanarDeckValidator.validate(cards).stream()
                .anyMatch(error -> error.contains("at least 10")));
    }

    @Test
    public void rejectsDuplicatesAndTooManyPhenomena() {
        List<String> cards = new ArrayList<>();
        Arrays.stream(Planes.values()).limit(7)
                .map(PlanarCardRegistry::getId)
                .forEach(cards::add);
        String duplicate = PlanarCardRegistry.getId(Planes.values()[0]);
        cards.add(duplicate);
        cards.add(duplicate);
        String phenomenon = PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY);
        cards.add(phenomenon);
        cards.add(phenomenon);
        cards.add(phenomenon);

        List<String> errors = IndividualPlanarDeckValidator.validate(cards);
        Assert.assertTrue(errors.stream().anyMatch(error -> error.contains("unique")));
        Assert.assertTrue(errors.stream().anyMatch(error -> error.contains("at most 2")));
    }
}
