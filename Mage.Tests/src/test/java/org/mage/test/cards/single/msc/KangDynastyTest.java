package org.mage.test.cards.single.msc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.k.KangDynasty Kang Dynasty}
 *
 * @author Susucr
 */
public class KangDynastyTest extends CardTestPlayerBase {

    private static final String dynasty = "Kang Dynasty";
    private static final String bear = "Grizzly Bears";

    @Test
    public void testFirstChapterAndDelayedDrawTrigger() {
        addCard(Zone.HAND, playerA, dynasty);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerB, "Mountain");
        addCard(Zone.BATTLEFIELD, playerB, bear);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dynasty);
        addTarget(playerA, bear);
        checkPermanentTapped("chapter taps the target", 1, PhaseStep.BEGIN_COMBAT, playerB, bear, true, 1);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 18);
        assertHandCount(playerA, 1);
    }

    @Test
    public void testThirdChapterUsesHandSizeAtResolution() {
        addCard(Zone.BATTLEFIELD, playerA, dynasty);
        addCard(Zone.HAND, playerA, "Memnite", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Bear Cub");
        addCard(Zone.BATTLEFIELD, playerB, bear);

        addTarget(playerA, bear); // chapter I
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        addCounters(1, PhaseStep.POSTCOMBAT_MAIN, playerA, dynasty, CounterType.LORE, 1);
        addTarget(playerA, bear); // chapter II
        waitStackResolved(1, PhaseStep.POSTCOMBAT_MAIN);

        addCounters(1, PhaseStep.POSTCOMBAT_MAIN, playerA, dynasty, CounterType.LORE, 1);
        addTarget(playerA, "Bear Cub"); // chapter III
        waitStackResolved(1, PhaseStep.POSTCOMBAT_MAIN);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPowerToughness(playerA, "Bear Cub", 4, 4);
    }
}
