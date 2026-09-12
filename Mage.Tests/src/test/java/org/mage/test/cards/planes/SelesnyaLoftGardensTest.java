package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests Selesnya Loft Gardens in the common Planechase runtime.
 */
public class SelesnyaLoftGardensTest extends CardTestPlayerBase {

    @Test
    public void testDoublesTokensForEveryPlayer() {
        addPlane(playerA, Planes.PLANE_SELESNYA_LOFT_GARDENS);
        addCard(Zone.HAND, playerA, "Dragon Fodder");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.HAND, playerB, "Dragon Fodder");
        addCard(Zone.BATTLEFIELD, playerB, "Mountain", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Dragon Fodder");
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Dragon Fodder");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Goblin Token", 4);
        assertPermanentCount(playerB, "Goblin Token", 4);
    }

    @Test
    public void testDoublesEffectCountersForEveryPlayer() {
        addPlane(playerA, Planes.PLANE_SELESNYA_LOFT_GARDENS);
        addCard(Zone.BATTLEFIELD, playerA, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Battlegrowth");
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.HAND, playerB, "Battlegrowth");
        addCard(Zone.BATTLEFIELD, playerB, "Forest");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Battlegrowth", "Balduvian Bears");
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Battlegrowth", "Grizzly Bears");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Balduvian Bears", CounterType.P1P1, 2);
        assertCounterCount(playerB, "Grizzly Bears", CounterType.P1P1, 2);
    }

    @Test
    public void testChaosManaAbilityIsCumulativeUntilEndOfTurn() {
        addPlane(playerA, Planes.PLANE_SELESNYA_LOFT_GARDENS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);
        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        activateManaAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Add {G}", 2);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertManaPool(playerA, ManaType.GREEN, 6);
    }

    private static SpellAbility createCauseChaosAbility() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        return ability;
    }
}
