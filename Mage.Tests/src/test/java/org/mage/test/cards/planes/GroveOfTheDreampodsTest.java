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

import java.util.Arrays;
import java.util.Collections;

public class GroveOfTheDreampodsTest extends CardTestPlayerBase {

    @Test
    public void upkeepRevealsUntilCreatureAndPutsItOntoBattlefield() {
        useGroveOfTheDreampodsPlanechase();
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Island");
        skipInitShuffling();

        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertLibraryCount(playerA, 2);
    }

    @Test
    public void planeswalkingToGroveTriggersRevealAbility() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_AKOUM, Planes.PLANE_GROVE_OF_THE_DREAMPODS
        );
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        // Player A skips the first draw step in this duel. The noncreature
        // remains in the library after Grove puts the creature onto the battlefield.
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");
        skipInitShuffling();

        runCode("planeswalk to Grove of the Dreampods", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertLibraryCount(playerA, 1);
        assertHandCount(playerA, "Lightning Bolt", 0);
    }

    @Test
    public void noCreatureRandomizesLibraryWithoutMovingCardsElsewhere() {
        useGroveOfTheDreampodsPlanechase();
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");
        addCard(Zone.LIBRARY, playerA, "Island");
        skipInitShuffling();

        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertLibraryCount(playerA, 2);
        assertGraveyardCount(playerA, 0);
        assertPermanentCount(playerA, 0);
    }

    @Test
    public void chaosReturnsCreatureFromPlanarControllersGraveyard() {
        setStrictChooseMode(true);
        useGroveOfTheDreampodsPlanechase();
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        removeAllCardsFromLibrary(playerA);
        // The upkeep reveals this noncreature and returns it to the library.
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");
        skipInitShuffling();
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_GROVE_OF_THE_DREAMPODS)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Grove of the Dreampods", metadata.getEnglishName());
        Assert.assertEquals("Plane - Grove of the Dreampods", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void useGroveOfTheDreampodsPlanechase() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_GROVE_OF_THE_DREAMPODS);
    }
}
