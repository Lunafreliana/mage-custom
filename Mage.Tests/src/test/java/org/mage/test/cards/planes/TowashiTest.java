package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests for Plane - Towashi.
 */
public class TowashiTest extends CardTestPlayerBase {

    @Test
    public void modifiedCreatureHasTrampleAndDrawTrigger() {
        removeAllCardsFromHand(playerA);
        addPlane(playerA, Planes.PLANE_TOWASHI);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");

        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears", CounterType.P1P1, 1);
        attack(1, playerA, "Grizzly Bears");
        block(1, playerB, "Memnite", "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertHandCount(playerA, 1);
    }

    @Test
    public void unmodifiedCreatureGetsNeitherAbility() {
        removeAllCardsFromHand(playerA);
        addPlane(playerA, Planes.PLANE_TOWASHI);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");

        attack(1, playerA, "Grizzly Bears");
        block(1, playerB, "Memnite", "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 20);
        assertHandCount(playerA, 0);
    }

    @Test
    public void chaosDistributesThreeCountersAmongControlledCreatures() {
        addPlane(playerA, Planes.PLANE_TOWASHI);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Hill Giant");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears^X=2");
        addTarget(playerA, "Hill Giant^X=1");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount("Grizzly Bears", CounterType.P1P1, 2);
        assertCounterCount("Hill Giant", CounterType.P1P1, 1);
    }
}
