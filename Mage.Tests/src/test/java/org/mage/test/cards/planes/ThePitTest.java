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

public class ThePitTest extends CardTestPlayerBase {

    private static final String ANGEL = "3/3 white Angel creature token with flying";
    private static final String DEMON = "6/6 black Demon creature token with flying and trample";

    @Test
    public void eachPlayerChoosesWhichTokenToCreateOnArrival() {
        usePitAsPlaneswalkDestination();
        setChoice(playerA, ANGEL);
        setChoice(playerB, DEMON);

        runCode("planeswalk to The Pit", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertTokenCount(playerA, "Angel Token", 1);
        assertPowerToughness(playerA, "Angel Token", 3, 3);
        assertTokenCount(playerB, "Demon Token", 1);
        assertPowerToughness(playerB, "Demon Token", 6, 6);
    }

    @Test
    public void demonDealsDamageIfItsControllerCannotSacrificeAnotherCreature() {
        usePitAsPlaneswalkDestination();
        setChoice(playerA, ANGEL);
        setChoice(playerB, DEMON);

        runCode("planeswalk to The Pit", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerA, 20);
        assertLife(playerB, 14);
        assertTokenCount(playerB, "Demon Token", 1);
    }

    @Test
    public void chaosMakesEachPlayerSacrificeANonartifactCreature() {
        addPlane(playerA, Planes.PLANE_THE_PIT);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Ornithopter");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerA, "Ornithopter", 1);
        assertGraveyardCount(playerB, "Hill Giant", 1);
        assertPermanentCount(playerB, "Memnite", 1);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_THE_PIT);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Pit", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Pit", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void usePitAsPlaneswalkDestination() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(Planes.PLANE_AKOUM, Planes.PLANE_THE_PIT);
        skipInitShuffling();
    }
}
