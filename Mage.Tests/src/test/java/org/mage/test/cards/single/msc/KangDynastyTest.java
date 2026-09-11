package org.mage.test.cards.single.msc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.k.KangDynasty Kang Dynasty}
 *
 * @author VibecodingQueens
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
}
