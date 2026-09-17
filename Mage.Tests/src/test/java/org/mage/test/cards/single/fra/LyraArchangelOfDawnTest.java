package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class LyraArchangelOfDawnTest extends CardTestPlayerBase {

    @Test
    public void gainingLifePutsCounterOnEachControlledAngel() {
        addCard(Zone.BATTLEFIELD, playerA, "Lyra, Archangel of Dawn");
        addCard(Zone.BATTLEFIELD, playerA, "Serra Angel");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Serra Angel");
        addCard(Zone.HAND, playerA, "Sacred Nectar");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Sacred Nectar");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 24);
        assertCounterCount(playerA, "Lyra, Archangel of Dawn", CounterType.P1P1, 1);
        assertCounterCount(playerA, "Serra Angel", CounterType.P1P1, 1);
        assertCounterCount(playerA, "Grizzly Bears", CounterType.P1P1, 0);
        assertCounterCount(playerB, "Serra Angel", CounterType.P1P1, 0);
    }

    @Test
    public void oneLifeGainEventAddsOneCounterRegardlessOfAmount() {
        addCard(Zone.BATTLEFIELD, playerA, "Lyra, Archangel of Dawn");
        addCard(Zone.HAND, playerA, "Heroes' Reunion");
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, "Plains");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Heroes' Reunion");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 27);
        assertCounterCount(playerA, "Lyra, Archangel of Dawn", CounterType.P1P1, 1);
    }
}
