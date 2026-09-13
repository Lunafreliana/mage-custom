package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Plane;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class InysHaenTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndUpkeepMillPlanarController() {
        addPlane(playerA, Planes.PLANE_INYS_HAEN);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 10);
        skipInitShuffling();

        setChoice(playerA, "When you planeswalk");
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 6);
        assertLibraryCount(playerA, 4);
        assertGraveyardCount(playerB, 0);
    }

    @Test
    public void planeswalkingAwayReturnsEachPlayersLandsTapped() {
        addPlane(playerA, Planes.PLANE_INYS_HAEN);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 10);
        addCard(Zone.GRAVEYARD, playerA, "Mountain");
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerB, "Island");
        addCard(Zone.GRAVEYARD, playerB, "Hill Giant");
        skipInitShuffling();

        setChoice(playerA, "When you planeswalk");
        runCode("planeswalk away", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    game.getState().getSharedPlanarDeck().setPlanes(Collections.singletonList(
                            Plane.createPlane(Planes.PLANE_AKOUM)), false);
                    Assert.assertTrue(info, game.planeswalk(playerA.getId()));
                });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertTapped("Mountain", true);
        assertTapped("Island", true);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Hill Giant", 1);
    }

    @Test
    public void chaosReturnsOnlyNonlandCard() {
        addPlane(playerA, Planes.PLANE_INYS_HAEN);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 10);
        addCard(Zone.GRAVEYARD, playerA, "Mountain");
        addCard(Zone.GRAVEYARD, playerA, "Hill Giant");
        skipInitShuffling();

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        setChoice(playerA, "When you planeswalk");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Hill Giant", 1);
        assertGraveyardCount(playerA, "Mountain", 1);
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
