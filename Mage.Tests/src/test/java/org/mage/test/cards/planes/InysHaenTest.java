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

public class InysHaenTest extends CardTestPlayerBase {

    private void setupPlanechase(Planes... planes) {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(planes);

        // Library additions do not replace the default test decks.
        removeAllCardsFromLibrary(playerA);
        removeAllCardsFromLibrary(playerB);
        addCard(Zone.LIBRARY, playerA, "Silvercoat Lion", 10);
        addCard(Zone.LIBRARY, playerB, "Memnite", 10);
        skipInitShuffling();
        setStrictChooseMode(true);
    }

    @Test
    public void startingPlaneMillsOnlyAtFirstUpkeep() {
        // The normal starting-plane reveal is not a planeswalk.
        setupPlanechase(Planes.PLANE_INYS_HAEN);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Silvercoat Lion", 3);
        assertLibraryCount(playerA, 7);
        assertGraveyardCount(playerB, 0);
        assertLibraryCount(playerB, 10);
    }

    @Test
    public void planeswalkingToInysHaenMillsPlanarController() {
        setupPlanechase(Planes.PLANE_AKOUM, Planes.PLANE_INYS_HAEN);

        runCode("planeswalk to Inys Haen after upkeep", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Assert.assertEquals(info, 10, player.getLibrary().size());
                    Assert.assertTrue(info, player.getGraveyard().isEmpty());
                    Assert.assertTrue(info, game.planeswalk(player.getId()));
                });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        Assert.assertEquals("Plane - Inys Haen", currentGame.getState().getFaceUpPlanes().get(0).getName());
        assertGraveyardCount(playerA, "Silvercoat Lion", 3);
        assertLibraryCount(playerA, 7);
        assertGraveyardCount(playerB, 0);
        assertLibraryCount(playerB, 10);
    }

    @Test
    public void upkeepMillFollowsPlanarController() {
        setupPlanechase(Planes.PLANE_INYS_HAEN);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Silvercoat Lion", 3);
        assertLibraryCount(playerA, 7);
        assertGraveyardCount(playerB, "Memnite", 3);
        // Player B also takes their normal draw after the upkeep mill.
        assertLibraryCount(playerB, 6);
        assertHandCount(playerB, "Memnite", 1);
    }

    @Test
    public void planeswalkingAwayReturnsEachPlayersLandsTapped() {
        setupPlanechase(Planes.PLANE_INYS_HAEN, Planes.PLANE_AKOUM);
        addCard(Zone.GRAVEYARD, playerA, "Mountain", 2);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerB, "Island", 2);
        addCard(Zone.GRAVEYARD, playerB, "Hill Giant");

        runCode("planeswalk away", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        Assert.assertEquals("Plane - Akoum", currentGame.getState().getFaceUpPlanes().get(0).getName());
        assertPermanentCount(playerA, "Mountain", 2);
        assertPermanentCount(playerA, "Island", 0);
        assertPermanentCount(playerB, "Island", 2);
        assertPermanentCount(playerB, "Mountain", 0);
        assertTappedCount("Mountain", true, 2);
        assertTappedCount("Island", true, 2);
        assertGraveyardCount(playerA, "Mountain", 0);
        assertGraveyardCount(playerB, "Island", 0);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Silvercoat Lion", 3);
        assertGraveyardCount(playerB, "Hill Giant", 1);
    }

    @Test
    public void chaosReturnsOnlyNonlandCard() {
        setupPlanechase(Planes.PLANE_INYS_HAEN);
        addCard(Zone.GRAVEYARD, playerA, "Mountain");
        addCard(Zone.GRAVEYARD, playerA, "Hill Giant");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Hill Giant", 1);
        assertGraveyardCount(playerA, "Hill Giant", 0);
        assertGraveyardCount(playerA, "Mountain", 1);
        assertGraveyardCount(playerA, "Silvercoat Lion", 3);
    }

    @Test
    public void registryExposesInysHaenMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_INYS_HAEN));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Inys Haen", metadata.getEnglishName());
        Assert.assertEquals("Plane - Inys Haen", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
