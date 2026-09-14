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

public class TheMatrixOfTimeTest extends CardTestPlayerBase {

    @Test
    public void arrivalExilesTopCardOfEachLibrary() {
        useMatrixOfTimeAsPlaneswalkDestination();
        addCard(Zone.LIBRARY, playerA, "Memnite");
        addCard(Zone.LIBRARY, playerB, "Ornithopter");

        runCode("planeswalk to The Matrix of Time", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount(playerA, "Memnite", 1);
        assertExileCount(playerB, "Ornithopter", 1);
    }

    @Test
    public void activePlayerMayCastLinkedCardAndItsOwnerPaysAndExiles() {
        useMatrixOfTimeAsPlaneswalkDestination();
        addCard(Zone.LIBRARY, playerB, "Island");
        addCard(Zone.LIBRARY, playerB, "Memnite");

        runCode("planeswalk to The Matrix of Time", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Memnite", 1);
        assertLife(playerA, 20);
        assertLife(playerB, 17);
        assertExileCount(playerB, "Island", 1);
    }

    @Test
    public void activePlayerMayPlayLinkedLandAndItsOwnerPaysAndExiles() {
        useMatrixOfTimeAsPlaneswalkDestination();
        addCard(Zone.LIBRARY, playerA, "Memnite");
        addCard(Zone.LIBRARY, playerA, "Island");

        runCode("planeswalk to The Matrix of Time", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Island");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Island", 1);
        assertLife(playerA, 17);
        assertExileCount(playerA, "Memnite", 1);
    }

    @Test
    public void chaosCreatesTwoTreasures() {
        addPlane(playerA, Planes.PLANE_THE_MATRIX_OF_TIME);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertTokenCount(playerA, "Treasure Token", 2);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_THE_MATRIX_OF_TIME);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Matrix of Time", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Matrix of Time", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void useMatrixOfTimeAsPlaneswalkDestination() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_AKOUM, Planes.PLANE_THE_MATRIX_OF_TIME
        );
        skipInitShuffling();
    }
}
