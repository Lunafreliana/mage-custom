package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Phenomenon;
import mage.game.command.Plane;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class TheFertileLandsOfSaulviniaTest extends CardTestPlayerBase {

    @Test
    public void landsProduceAnAdditionalManaForEachPlayer() {
        addPlane(playerA, Planes.PLANE_THE_FERTILE_LANDS_OF_SAULVINIA);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, "Upwelling");
        addCard(Zone.BATTLEFIELD, playerB, "Mountain");
        addCard(Zone.BATTLEFIELD, playerB, "Upwelling");

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}");
        checkManaPool("player A gets additional green", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "G", 2);
        activateManaAbility(2, PhaseStep.PRECOMBAT_MAIN, playerB, "{T}: Add {R}");
        checkManaPool("player B gets additional red", 2, PhaseStep.PRECOMBAT_MAIN, playerB, "R", 2);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void chaosSkipsPhenomenaAndTriggersRevealedPlanesChaosAbility() {
        addPlane(playerA, Planes.PLANE_THE_FERTILE_LANDS_OF_SAULVINIA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears", 1, true);

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        runCode("install known planar deck", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> game.getState().getSharedPlanarDeck().setPlanes(Arrays.asList(
                        Phenomenon.createPhenomenon(Phenomena.MUTUAL_EPIPHANY),
                        Plane.createPlane(Planes.PLANE_LLANOWAR),
                        Plane.createPlane(Planes.PLANE_BANT)), false));
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, "Phenomenon - Mutual Epiphany");
        runCode("check revealed cards were bottomed", 1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    PlanarCard top = game.getState().getSharedPlanarDeck().draw();
                    Assert.assertNotNull(info, top);
                    Assert.assertEquals(info, "Plane - Bant", top.getName());
                });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertTapped("Grizzly Bears", false);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_FERTILE_LANDS_OF_SAULVINIA));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Fertile Lands of Saulvinia", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Fertile Lands of Saulvinia", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
