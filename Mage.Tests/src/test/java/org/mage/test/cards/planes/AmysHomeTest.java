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
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class AmysHomeTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndUpkeepSuspendNonlandCards() {
        addPlane(playerA, Planes.PLANE_AMYS_HOME);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Hill Giant");
        addCard(Zone.HAND, playerA, "Island");

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        setChoice(playerA, "Grizzly Bears");
        setChoice(playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertExileCount(playerA, "Grizzly Bears", 1);
        assertExileCount(playerA, "Hill Giant", 1);
        assertCounterOnExiledCardCount("Grizzly Bears", CounterType.TIME, 2);
        assertCounterOnExiledCardCount("Hill Giant", CounterType.TIME, 4);
        assertHandCount(playerA, "Island", 1);
    }

    @Test
    public void chaosTimeTravelsForPlanarController() {
        addPlane(playerA, Planes.PLANE_AMYS_HOME);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears", CounterType.TIME, 1);

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        setChoice(playerA, TestPlayer.CHOICE_SKIP); // Skip the first optional suspend effect.
        setChoice(playerA, TestPlayer.CHOICE_SKIP); // Skip the second optional suspend effect.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, "Grizzly Bears"); // Time travel: add a time counter.

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount("Grizzly Bears", CounterType.TIME, 2);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_AMYS_HOME)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Amy's Home", metadata.getEnglishName());
        Assert.assertEquals("Plane - Amy's Home", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
