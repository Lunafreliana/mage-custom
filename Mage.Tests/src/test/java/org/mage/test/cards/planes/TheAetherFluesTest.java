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

public class TheAetherFluesTest extends CardTestPlayerBase {

    @Test
    public void upkeepSacrificeFindsCreatureAndShufflesOtherRevealedCards() {
        useTheAetherFluesPlanechase();
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Hill Giant");
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");
        skipInitShuffling();

        setChoice(playerA, true); // Sacrifice a creature.
        setChoice(playerA, "Grizzly Bears");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerA, "Hill Giant", 1);
        assertLibraryCount(playerA, "Lightning Bolt", 1);
    }

    @Test
    public void upkeepMayDeclineToSacrifice() {
        useTheAetherFluesPlanechase();
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Hill Giant");
        skipInitShuffling();

        setChoice(playerA, false);
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerA, "Hill Giant", 0);
        assertLibraryCount(playerA, "Hill Giant", 1);
    }

    @Test
    public void planeswalkToTriggerCanExchangeCreature() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_AKOUM, Planes.PLANE_THE_AETHER_FLUES
        );
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Hill Giant");
        skipInitShuffling();

        runCode("planeswalk to The Aether Flues", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        setChoice(playerA, true);
        setChoice(playerA, "Grizzly Bears");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerA, "Hill Giant", 1);
    }

    @Test
    public void chaosMayPutCreatureFromHandOntoBattlefield() {
        useTheAetherFluesPlanechase();
        addCard(Zone.HAND, playerA, "Hill Giant");
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, true);
        setChoice(playerA, "Hill Giant");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Hill Giant", 1);
        assertHandCount(playerA, "Hill Giant", 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_AETHER_FLUES)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Aether Flues", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Aether Flues", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }

    private void useTheAetherFluesPlanechase() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_THE_AETHER_FLUES);
    }
}
