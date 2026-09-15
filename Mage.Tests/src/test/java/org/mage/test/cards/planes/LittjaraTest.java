package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class LittjaraTest extends CardTestPlayerBase {

    @Test
    public void startingPlaneCreatesShapeshifterAtUpkeep() {
        useLittjaraPlanechase();

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // Starting-plane initialization is not a planeswalk, so only the upkeep ability triggers.
        assertPermanentCount(playerA, "Shapeshifter Token", 1);
        assertPowerToughness(playerA, "Shapeshifter Token", 2, 2);
    }

    @Test
    public void chaosCountersOnlyControlledCreaturesOfChosenType() {
        addPlane(playerA, Planes.PLANE_LITTJARA);
        addCard(Zone.BATTLEFIELD, playerA, "Llanowar Elves");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Llanowar Elves");
        addChaosSpell();

        setChoice(playerA, "When you planeswalk"); // Order the planeswalk and upkeep triggers.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, "Elf");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Llanowar Elves", CounterType.P1P1, 1);
        assertCounterCount(playerA, "Grizzly Bears", CounterType.P1P1, 0);
        assertCounterCount(playerB, "Llanowar Elves", CounterType.P1P1, 0);
        // Changeling makes each token an Elf as well.
        assertCounterCount(playerA, "Shapeshifter Token", CounterType.P1P1, 1);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_LITTJARA)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Littjara", metadata.getEnglishName());
        Assert.assertEquals("Plane - Littjara", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }

    private void useLittjaraPlanechase() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_LITTJARA);
    }
}
