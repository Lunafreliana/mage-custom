package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class FleshDuplicateTest extends CardTestPlayerBase {

    @Test
    public void testGrantedVanishingRemovesTimeCounters() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Flesh Duplicate");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Flesh Duplicate");
        setChoice(playerA, "Grizzly Bears");

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertCounterCount(playerA, "Grizzly Bears", CounterType.TIME, 2);
    }

    @Test
    public void testKeepsCopiedDreamtideWhaleVanishing() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Flesh Duplicate");
        addCard(Zone.BATTLEFIELD, playerB, "Dreamtide Whale");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Flesh Duplicate");
        setChoice(playerA, "Dreamtide Whale");

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Dreamtide Whale", 1);
        assertCounterCount(playerA, "Dreamtide Whale", CounterType.TIME, 1);
    }
}
