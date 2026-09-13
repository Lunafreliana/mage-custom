package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class TheGoldenCityOfOrazcaTest extends CardTestPlayerBase {

    @Test
    public void combatDamageCreatesOneTreasureWithoutCitysBlessing() {
        removeAllCardsFromHand(playerA);
        addPlane(playerA, Planes.PLANE_THE_GOLDEN_CITY_OF_ORAZCA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");

        attack(1, playerA, "Grizzly Bears");
        attack(1, playerA, "Memnite");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Treasure Token", 1);
        assertHandCount(playerA, 0);
    }

    @Test
    public void combatDamageDrawsWithCitysBlessing() {
        removeAllCardsFromHand(playerA);
        addPlane(playerA, Planes.PLANE_THE_GOLDEN_CITY_OF_ORAZCA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 9);
        addCard(Zone.LIBRARY, playerA, "Island");
        skipInitShuffling();

        attack(1, playerA, "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Treasure Token", 1);
        assertHandCount(playerA, "Island", 1);
    }

    @Test
    public void chaosMayPutPermanentOntoBattlefieldTapped() {
        addPlane(playerA, Planes.PLANE_THE_GOLDEN_CITY_OF_ORAZCA);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, true);
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertTapped("Grizzly Bears", true);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_GOLDEN_CITY_OF_ORAZCA)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Golden City of Orazca", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Golden City of Orazca", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
