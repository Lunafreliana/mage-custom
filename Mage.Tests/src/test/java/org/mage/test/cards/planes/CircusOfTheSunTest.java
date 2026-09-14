package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
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

public class CircusOfTheSunTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndUpkeepCreatePerformers() {
        addPlane(playerA, Planes.PLANE_CIRCUS_OF_THE_SUN);

        setChoice(playerA, "When you planeswalk");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Performer Token", 4);
        assertAbilityCount(playerA, "Performer Token", mage.abilities.keyword.FlyingAbility.class, 1);
        assertAbilityCount(playerA, "Performer Token", mage.abilities.keyword.HasteAbility.class, 1);
    }

    @Test
    public void flyingCreatureMayReturnAndDrawItsPower() {
        addPlane(playerA, Planes.PLANE_CIRCUS_OF_THE_SUN);
        addCard(Zone.BATTLEFIELD, playerA, "Wind Drake"); // 2/2 with flying
        addCard(Zone.LIBRARY, playerA, "Mountain", 2);

        setChoice(playerA, "When you planeswalk");
        attack(1, playerA, "Wind Drake");
        setChoice(playerA, true);
        addTarget(playerA, "Wind Drake");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Wind Drake", 1);
        assertHandCount(playerA, "Mountain", 2);
    }

    @Test
    public void nonFlyingCreatureDoesNotTriggerReturn() {
        addPlane(playerA, Planes.PLANE_CIRCUS_OF_THE_SUN);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        setChoice(playerA, "When you planeswalk");
        attack(1, playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void chaosAddsFlyingCounterAndPerformerType() {
        addPlane(playerA, Planes.PLANE_CIRCUS_OF_THE_SUN);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);

        setChoice(playerA, "When you planeswalk");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Grizzly Bears", CounterType.FLYING, 1);
        assertSubtype("Grizzly Bears", SubType.PERFORMER);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_CIRCUS_OF_THE_SUN)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Circus of the Sun", metadata.getEnglishName());
        Assert.assertEquals("Plane - Circus of the Sun", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
