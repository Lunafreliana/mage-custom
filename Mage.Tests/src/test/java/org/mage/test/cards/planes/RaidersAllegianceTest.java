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

public class RaidersAllegianceTest extends CardTestPlayerBase {

    @Test
    public void raidCountsPlayersRatherThanDamageEvents() {
        addPlane(playerA, Planes.PLANE_RAIDERS_ALLEGIANCE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");

        attack(1, playerA, "Grizzly Bears");
        attack(1, playerA, "Memnite");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertCounterCount(playerA, CounterType.POINT, 1);
        assertPermanentCount(playerA, "Pirate Token", 1);
    }

    @Test
    public void departureRewardsLeaderAndRemovesEveryPlayersPointCounters() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_RAIDERS_ALLEGIANCE, Planes.PLANE_AKOUM
        );
        removeAllCardsFromHand(playerA);
        removeAllCardsFromLibrary(playerA);
        removeAllCardsFromHand(playerB);
        removeAllCardsFromLibrary(playerB);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Island", 5);
        addCard(Zone.LIBRARY, playerB, "Mountain", 3);
        skipInitShuffling();

        attack(1, playerA, "Grizzly Bears");
        runCode("planeswalk away", 3, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Treasure Token", 3);
        assertPermanentCount(playerB, "Treasure Token", 0);
        // Two normal draw steps plus the three departure draws.
        assertHandCount(playerA, "Island", 5);
        assertHandCount(playerB, "Mountain", 1);
        assertCounterCount(playerA, CounterType.POINT, 0);
        assertCounterCount(playerB, CounterType.POINT, 0);
    }

    @Test
    public void chaosProliferatesOnceForEachControlledPirate() {
        addPlane(playerA, Planes.PLANE_RAIDERS_ALLEGIANCE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Pirate Ship");
        addChaosSpell();

        attack(1, playerA, "Grizzly Bears");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, playerA.getName());
        setChoice(playerA, playerA.getName());

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        // The raid-created Pirate and Pirate Ship each cause one proliferation.
        assertCounterCount(playerA, CounterType.POINT, 3);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_RAIDERS_ALLEGIANCE);

        Assert.assertEquals("Raiders' Allegiance", PlanarCardRegistry.getMetadata(id).getEnglishName());
        Assert.assertEquals("Plane - Raiders' Allegiance", PlanarCardRegistry.getMetadata(id).getImageName());
        Assert.assertEquals("PUNK", PlanarCardRegistry.getMetadata(id).getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
