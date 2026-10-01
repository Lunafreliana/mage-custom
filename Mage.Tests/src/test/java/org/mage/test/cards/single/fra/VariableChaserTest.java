package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class VariableChaserTest extends CardTestPlayerBase {

    @Test
    public void arcOfFortuneLetsEachPlayerChooseIndependently() {
        addCard(Zone.HAND, playerA, "Variable Chaser");
        addCard(Zone.HAND, playerA, "Memnite", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 6);
        addCard(Zone.HAND, playerB, "Memnite", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Variable Chaser");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkExileCount("prepare spell copy was created", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Arc of Fortune", 1);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Arc of Fortune");
        setChoice(playerA, true);
        setChoice(playerB, false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerA, 7);
        assertHandCount(playerB, 2);
        assertGraveyardCount(playerA, "Memnite", 2);
        assertGraveyardCount(playerB, "Memnite", 0);
        assertPowerToughness(playerA, "Variable Chaser", 3, 4);
        assertExileCount(playerA, "Arc of Fortune", 0);
    }
}
