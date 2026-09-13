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
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class ThePyramidOfMarsTest extends CardTestPlayerBase {

    @Test
    public void testUpkeepSurveilsForPlanarController() {
        addPlane(playerA, Planes.PLANE_THE_PYRAMID_OF_MARS);
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        skipInitShuffling();

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        addTarget(playerA, "Grizzly Bears"); // planeswalk surveil: put the Bears into the graveyard
        addTarget(playerA, TestPlayer.TARGET_SKIP); // upkeep surveil: leave the Island on top

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertLibraryCount(playerA, "Island", 1);
        assertGraveyardCount(playerB, 0);
    }

    @Test
    public void chaosReturnsCreatureFromPlanarControllersGraveyard() {
        addPlane(playerA, Planes.PLANE_THE_PYRAMID_OF_MARS);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerA, "Divination");
        removeAllCardsFromLibrary(playerA);
        skipInitShuffling();

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 0);
        assertGraveyardCount(playerA, "Divination", 1);
    }

    @Test
    public void registryExposesThePyramidOfMarsMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_PYRAMID_OF_MARS));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Pyramid of Mars", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Pyramid of Mars", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
