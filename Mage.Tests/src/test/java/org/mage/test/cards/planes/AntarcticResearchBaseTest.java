package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class AntarcticResearchBaseTest extends CardTestPlayerBase {

    @Test
    public void planeswalkingAndUpkeepInvestigate() {
        addPlane(playerA, Planes.PLANE_ANTARCTIC_RESEARCH_BASE);
        removeAllCardsFromLibrary(playerA);
        skipInitShuffling();

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Clue Token", 2);
        assertPermanentCount(playerB, "Clue Token", 0);
    }

    @Test
    public void chaosCountsArtifactsAndAddsPlantSubtype() {
        addPlane(playerA, Planes.PLANE_ANTARCTIC_RESEARCH_BASE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
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

        // The two Clues and Memnite are all artifacts when the chaos ability resolves.
        assertCounterCount("Grizzly Bears", CounterType.P1P1, 3);
        assertSubtype("Grizzly Bears", SubType.PLANT);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_ANTARCTIC_RESEARCH_BASE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Antarctic Research Base", metadata.getEnglishName());
        Assert.assertEquals("Plane - Antarctic Research Base", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
