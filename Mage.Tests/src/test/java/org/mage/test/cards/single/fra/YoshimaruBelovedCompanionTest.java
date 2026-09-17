package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class YoshimaruBelovedCompanionTest extends CardTestPlayerBase {

    private static final String yoshimaru = "Yoshimaru, Beloved Companion";

    @Test
    public void testAddsExtraCounterToControlledCreature() {
        addCard(Zone.BATTLEFIELD, playerA, yoshimaru);
        addCard(Zone.BATTLEFIELD, playerA, "Isamaru, Hound of Konda");
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 6);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{6}:", "Isamaru, Hound of Konda");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertCounterCount(playerA, "Isamaru, Hound of Konda", CounterType.P1P1, 2);
    }

    @Test
    public void testCanTargetOpponentsLegendaryCreatureWithoutExtraCounter() {
        addCard(Zone.BATTLEFIELD, playerA, yoshimaru);
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 6);
        addCard(Zone.BATTLEFIELD, playerB, "Isamaru, Hound of Konda");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{6}:", "Isamaru, Hound of Konda");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertCounterCount(playerB, "Isamaru, Hound of Konda", CounterType.P1P1, 1);
    }
}
