package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class HotelOfFearsTest extends CardTestPlayerBase {

    @Test
    public void upkeepExilesCardLosesLifeAndAllowsPlayingIt() {
        addPlane(playerA, Planes.PLANE_HOTEL_OF_FEARS);
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 16);
        assertPermanentCount(playerA, "Hill Giant", 1);
        assertExileCount("Hill Giant", 0);
    }

    @Test
    public void chaosUsesChosenDevotionAndSacrificesAnotherCreature() {
        addPlane(playerA, Planes.PLANE_HOTEL_OF_FEARS);
        addCard(Zone.BATTLEFIELD, playerA, "Phyrexian Obliterator");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        removeAllCardsFromLibrary(playerA);
        skipInitShuffling();

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Phyrexian Obliterator");
        setChoice(playerA, "Black");
        addTarget(playerA, "Memnite");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount("Phyrexian Obliterator", CounterType.P1P1, 4);
        assertPermanentCount(playerA, "Phyrexian Obliterator", 1);
        assertGraveyardCount(playerA, "Memnite", 1);
    }

    @Test
    public void chaosCannotSacrificeTheTargetCreature() {
        addPlane(playerA, Planes.PLANE_HOTEL_OF_FEARS);
        addCard(Zone.BATTLEFIELD, playerA, "Phyrexian Obliterator");
        removeAllCardsFromLibrary(playerA);
        skipInitShuffling();

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Phyrexian Obliterator");
        setChoice(playerA, "Black");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount("Phyrexian Obliterator", CounterType.P1P1, 4);
        assertPermanentCount(playerA, "Phyrexian Obliterator", 1);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_HOTEL_OF_FEARS)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Hotel of Fears", metadata.getEnglishName());
        Assert.assertEquals("Plane - Hotel of Fears", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
