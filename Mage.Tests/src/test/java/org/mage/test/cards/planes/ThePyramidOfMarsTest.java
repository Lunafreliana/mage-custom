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
        // Library additions go on top, so the seven Plains form the opening hand and leave these two cards.
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Silvercoat Lion");
        addCard(Zone.LIBRARY, playerA, "Plains", 7);
        usePyramidOfMarsDeck();

        addTarget(playerA, "Grizzly Bears^Silvercoat Lion");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Silvercoat Lion", 1);
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
