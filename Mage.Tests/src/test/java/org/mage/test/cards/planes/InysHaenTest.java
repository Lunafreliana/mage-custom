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

public class InysHaenTest extends CardTestPlayerBase {

    @Test
    public void planeswalkToAndUpkeepMillPlanarController() {
        addPlane(playerA, Planes.PLANE_INYS_HAEN);
        addCard(Zone.LIBRARY, playerA, "Mountain", 10);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Mountain", 6);
        assertGraveyardCount(playerB, 0);
    }

    @Test
    public void planeswalkAwayReturnsEachPlayersLandsTapped() {
        addPlane(playerA, Planes.PLANE_INYS_HAEN);
        addCard(Zone.GRAVEYARD, playerA, "Forest");
        addCard(Zone.GRAVEYARD, playerB, "Island");
        addCard(Zone.GRAVEYARD, playerB, "Grizzly Bears");

        runCode("planeswalk away", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Forest", 1);
        assertPermanentCount(playerB, "Island", 1);
        assertTapped("Forest", true);
        assertTapped("Island", true);
        assertGraveyardCount(playerB, "Grizzly Bears", 1);
    }

    @Test
    public void chaosReturnsTargetNonlandCardToHand() {
        addPlane(playerA, Planes.PLANE_INYS_HAEN);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerA, "Forest");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Forest", 1);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_INYS_HAEN));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Inys Haen", metadata.getEnglishName());
        Assert.assertEquals("Plane - Inys Haen", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
