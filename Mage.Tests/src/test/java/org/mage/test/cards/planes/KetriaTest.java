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
import java.util.Collections;

public class KetriaTest extends CardTestPlayerBase {

    @Test
    public void startingPlaneTriggersAtUpkeepAndChoiceIsMadeOnResolution() {
        useSharedPlanechase(Planes.PLANE_KETRIA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        addTarget(playerA, "Grizzly Bears");
        setChoice(playerA, "Menace");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Grizzly Bears", CounterType.MENACE, 1);
        assertCounterCount(playerA, "Grizzly Bears", CounterType.VIGILANCE, 0);
    }

    @Test
    public void planeswalkToKetriaTriggersForPlaneswalkingPlayer() {
        useSharedPlanechase(Planes.PLANE_AKOUM, Planes.PLANE_KETRIA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        runCode("planeswalk to Ketria", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        addTarget(playerA, "Grizzly Bears");
        setChoice(playerA, "Trample");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        Assert.assertEquals("Plane - Ketria", currentGame.getState().getFaceUpPlanes().get(0).getName());
        assertCounterCount(playerA, "Grizzly Bears", CounterType.TRAMPLE, 1);
    }

    @Test
    public void chaosExilesThroughNonlandPermanentAndPutsItOntoBattlefield() {
        useSharedPlanechase(Planes.PLANE_KETRIA);
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Island"); // Drawn before chaos resolves.
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, false); // Battlefield rather than hand.
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount("Forest", 1);
        assertExileCount("Lightning Bolt", 1);
        assertExileCount("Grizzly Bears", 0);
        assertPermanentCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void chaosCanPutPermanentIntoHand() {
        useSharedPlanechase(Planes.PLANE_KETRIA);
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Island"); // Drawn before chaos resolves.
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, true); // Hand rather than battlefield.
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_KETRIA)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Ketria", metadata.getEnglishName());
        Assert.assertEquals("Plane - Ketria", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }

    private void useSharedPlanechase(Planes... planes) {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = planes.length == 1
                ? Collections.singletonList(planes[0])
                : Arrays.asList(planes);
        skipInitShuffling();
    }
}
