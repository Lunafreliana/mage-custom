package org.mage.test.cards.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkAwayFromSourceTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Plane;
import mage.game.command.PlanarCardRegistry;
import mage.game.events.GameEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class InysHaenTest extends CardTestPlayerBase {

    @Test
    public void testRegistryMetadataAndAbilities() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_INYS_HAEN));
        Assert.assertNotNull(metadata);
        Assert.assertEquals(CardType.PLANE, metadata.getType());
        Assert.assertEquals("Inys Haen", metadata.getEnglishName());
        Assert.assertEquals("MOC", metadata.getSetCode());

        Plane plane = Plane.createPlane(Planes.PLANE_INYS_HAEN);
        Assert.assertNotNull(plane);
        Assert.assertEquals(1, plane.getAbilities().stream()
                .filter(PlaneswalkToSourceTriggeredAbility.class::isInstance).count());
        Assert.assertEquals(1, plane.getAbilities().stream()
                .filter(PlaneswalkAwayFromSourceTriggeredAbility.class::isInstance).count());
        Assert.assertEquals(1, plane.getAbilities().stream()
                .filter(ChaosEnsuesTriggeredAbility.class::isInstance).count());
    }

    @Test
    public void testUpkeepMillsPlanarController() {
        addPlane(playerA, Planes.PLANE_INYS_HAEN);
        addCard(Zone.LIBRARY, playerA, "Mountain", 10);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, 3);
        assertLibraryCount(playerA, 7);
    }

    @Test
    public void testPlaneswalkAwayReturnsEachPlayersLandsTapped() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_INYS_HAEN,
                Planes.PLANE_FIELDS_OF_SUMMER);
        addCard(Zone.LIBRARY, playerA, "Forest", 10);
        addCard(Zone.LIBRARY, playerB, "Forest", 10);
        addCard(Zone.GRAVEYARD, playerA, "Mountain");
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerB, "Island");

        runCode("planeswalk away from Inys Haen", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(playerA.getId())));
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Mountain", 1);
        assertPermanentCount(playerB, "Island", 1);
        assertTapped("Mountain", true);
        assertTapped("Island", true);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void testChaosReturnsTargetNonlandCard() {
        addPlane(playerA, Planes.PLANE_INYS_HAEN);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerA, "Mountain");
        addTarget(playerA, "Grizzly Bears");

        runCode("chaos ensues", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> game.fireEvent(new GameEvent(
                        GameEvent.EventType.CHAOS_ENSUES, null, null, player.getId())));
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Mountain", 1);
    }
}
