package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class ThePyramidOfMarsTest extends CardTestPlayerBase {

    @Test
    public void testUpkeepSurveilsForPlanarController() {
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);
        // Seven are drawn for the opening hand, leaving three known cards.
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 10);
        usePyramidOfMarsDeck();

        // The current Planechase test setup fires the planeswalk-to trigger when the starting plane is revealed.
        // Move one of its two cards to the graveyard, avoiding an ordering prompt for the one card left on top.
        addTarget(playerA, "Grizzly Bears");
        // The upkeep trigger then sees the two remaining cards and moves both to the graveyard.
        addTarget(playerA, "Grizzly Bears^Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 3);
        assertLibraryCount(playerA, 0);
    }

    @Test
    public void testChaosReturnsCreatureFromPlanarControllersGraveyard() {
        usePyramidOfMarsDeck();
        addCard(Zone.GRAVEYARD, playerB, "Grizzly Bears");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerB, causeChaos, null, CardType.SORCERY);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Cause Chaos");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Grizzly Bears", 0);
    }

    @Test
    public void testRegistryMetadataUsesDoctorWhoPrinting() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_THE_PYRAMID_OF_MARS);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);
        PlanarCard card = PlanarCardRegistry.create(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Pyramid of Mars", metadata.getEnglishName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(card);
        Assert.assertEquals("Plane - The Pyramid of Mars", card.getName());
    }

    private void usePyramidOfMarsDeck() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_THE_PYRAMID_OF_MARS);
    }
}
