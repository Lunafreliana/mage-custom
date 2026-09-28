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

public class JundTest extends CardTestPlayerBase {

    @Test
    public void coloredCreatureSpellGainsDevourFive() {
        addPlane(playerA, Planes.PLANE_JUND);
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Memnite", 2);
        addCard(Zone.HAND, playerB, "Grizzly Bears");

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Grizzly Bears");
        setChoice(playerB, true);
        addTarget(playerB, "Memnite^Memnite");
        setStrictChooseMode(true);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Memnite", 0);
        assertCounterCount(playerB, "Grizzly Bears", CounterType.P1P1, 10);
        assertPowerToughness(playerB, "Grizzly Bears", 12, 12);
    }

    @Test
    public void colorlessCreatureSpellDoesNotGainDevour() {
        addPlane(playerA, Planes.PLANE_JUND);
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        addCard(Zone.HAND, playerA, "Stone Golem");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Stone Golem");
        setStrictChooseMode(true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Memnite", 1);
        assertPowerToughness(playerA, "Stone Golem", 3, 3);
    }

    @Test
    public void chaosCreatesTwoGoblinsForPlanarController() {
        addPlane(playerA, Planes.PLANE_JUND);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Goblin Token", 2);
        assertPermanentCount(playerB, "Goblin Token", 0);
    }

    @Test
    public void registryExposesJundMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_JUND);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Jund", metadata.getEnglishName());
        Assert.assertEquals("Plane - Jund", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
