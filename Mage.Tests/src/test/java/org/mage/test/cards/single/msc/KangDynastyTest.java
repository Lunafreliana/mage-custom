package org.mage.test.cards.single.msc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
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
        addCard(Zone.BATTLEFIELD, playerB, bear);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dynasty);
        setTarget(1, PhaseStep.PRECOMBAT_MAIN, playerA, bear);
        checkPermanentTapped("chapter taps the target", 1, PhaseStep.BEGIN_COMBAT, playerB, bear, true, 1);
        attack(2, playerB, bear, playerA);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 18);
        assertHandCount(playerA, 1);
    }

    @Test
    public void testThirdChapterLocksHandSizeAtResolution() {
        addCard(Zone.HAND, playerA, dynasty);
        addCard(Zone.HAND, playerA, "Memnite");
        addCard(Zone.LIBRARY, playerA, "Mountain", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.BATTLEFIELD, playerA, bear);
        addCard(Zone.BATTLEFIELD, playerB, bear);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dynasty);
        setTarget(1, PhaseStep.PRECOMBAT_MAIN, playerA, bear); // chapter I
        setTarget(3, PhaseStep.PRECOMBAT_MAIN, playerA, bear); // chapter II
        setTarget(5, PhaseStep.PRECOMBAT_MAIN, playerA, bear); // chapter III
        castSpell(5, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");

        setStopAt(5, PhaseStep.BEGIN_COMBAT);
        execute();

        // Three cards were in hand when chapter III resolved. Casting Memnite later does not change the bonus.
        assertPowerToughness(playerA, bear, 5, 5);
    }
}
