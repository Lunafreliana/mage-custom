package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.HasteAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class ShyTownTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndChaosMakeOpponentCreaturesShyCowards() {
        addPlane(playerA, Planes.PLANE_SHY_TOWN);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Runeclaw Bear");
        addTarget(playerA, "Grizzly Bears");
        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.

        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Runeclaw Bear");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerB, "Grizzly Bears", CounterType.SHY, 1);
        assertCounterCount(playerB, "Runeclaw Bear", CounterType.SHY, 1);
        assertSubtype("Grizzly Bears", SubType.COWARD);
        assertSubtype("Runeclaw Bear", SubType.COWARD);
    }

    @Test
    public void upkeepCreatesHastyRedWarrior() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_SHY_TOWN);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Warrior Token", 1);
        assertPowerToughness(playerA, "Warrior Token", 2, 2);
        assertSubtype("Warrior Token", SubType.WARRIOR);
        assertAbility(playerA, "Warrior Token", HasteAbility.getInstance(), true);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_SHY_TOWN)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Shy Town", metadata.getEnglishName());
        Assert.assertEquals("Plane - Shy Town", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private static SpellAbility createCauseChaosAbility() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        return ability;
    }
}
