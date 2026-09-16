package org.mage.test.cards.single.mbc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.k.KurokiThiefOfTalents Kuroki, Thief of Talents}
 *
 * @author muz
 */
public class KurokiThiefOfTalentsTest extends CardTestPlayerBase {

    @Test
    public void testOpponentDrawsAndControllerCastsSpell() {
        addCard(Zone.BATTLEFIELD, playerA, "Kuroki, Thief of Talents");
        addCard(Zone.HAND, playerB, "Lightning Bolt");
        addCard(Zone.LIBRARY, playerB, "Forest", 4);

        addTarget(playerA, playerB);
        setChoice(playerB, true); // Draw four cards
        setChoice(playerA, true); // Cast Lightning Bolt
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertHandCount(playerB, 4);
        assertGraveyardCount(playerB, "Lightning Bolt", 1);
        assertLife(playerB, 17);
        assertPowerToughness(playerA, "Kuroki, Thief of Talents", 4, 4);
    }

    @Test
    public void testOpponentDeclinesDraw() {
        addCard(Zone.BATTLEFIELD, playerA, "Kuroki, Thief of Talents");
        addCard(Zone.LIBRARY, playerB, "Forest", 4);

        addTarget(playerA, playerB);
        setChoice(playerB, false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertHandCount(playerB, 0);
        assertPowerToughness(playerA, "Kuroki, Thief of Talents", 6, 6);
    }
}
