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

import java.util.Arrays;

public class TheWindyCityTest extends CardTestPlayerBase {

    @Test
    public void arrivalAndUpkeepAddFlyingCountersOnlyToLegalTargets() {
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerA, "Birds of Paradise");

        addPlane(playerA, Planes.PLANE_THE_WINDY_CITY);
        addTarget(playerA, "Grizzly Bears");
        addTarget(playerA, "Runeclaw Bear");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Grizzly Bears", CounterType.FLYING, 1);
        assertCounterCount(playerA, "Runeclaw Bear", CounterType.FLYING, 1);
        assertCounterCount(playerA, "Birds of Paradise", CounterType.FLYING, 0);
    }

    @Test
    public void chaosExilesFlyingCreatureAndOwnerCastsItForTwo() {
        addPlane(playerA, Planes.PLANE_THE_WINDY_CITY);
        addCard(Zone.BATTLEFIELD, playerB, "Serra Angel");
        addCard(Zone.BATTLEFIELD, playerB, "Wastes", 2);
        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Serra Angel");
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Serra Angel");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Serra Angel", 1);
    }

    @Test
    public void planeswalkingAwayRemovesAllFlyingCounters() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_THE_WINDY_CITY, Planes.PLANE_AKOUM
        );
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears", CounterType.FLYING, 1);
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Glory Seeker", CounterType.FLYING, 1);
        runCode("planeswalk away", 1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertCounterCount(playerA, "Grizzly Bears", CounterType.FLYING, 0);
        assertCounterCount(playerB, "Glory Seeker", CounterType.FLYING, 0);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_THE_WINDY_CITY);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Windy City", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Windy City", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private static SpellAbility createCauseChaosAbility() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        return ability;
    }
}
