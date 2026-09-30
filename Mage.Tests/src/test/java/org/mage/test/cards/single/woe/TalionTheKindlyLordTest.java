package org.mage.test.cards.single.woe;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author JayDi85
 */
public class TalionTheKindlyLordTest extends CardTestPlayerBase {

    private static final String talion = "Talion, the Kindly Lord";

    @Test
    public void triggersForChosenToughnessOnly() {
        setStrictChooseMode(true);

        addCard(Zone.HAND, playerA, talion);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 4);
        // One card is drawn during player A's first draw step; the other is
        // available for Talion's trigger.
        addCard(Zone.LIBRARY, playerA, "Island", 2);

        // Ornithopter has mana value 0 and power 0, but toughness 2.
        addCard(Zone.HAND, playerB, "Ornithopter");
        // Memnite has mana value 0 and is 1/1, so it must not trigger Talion.
        addCard(Zone.HAND, playerB, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, talion);
        setChoiceAmount(playerA, 2);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Ornithopter");
        castSpell(2, PhaseStep.POSTCOMBAT_MAIN, playerB, "Memnite");

        setStopAt(2, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 18);
        assertHandCount(playerA, "Island", 2);
    }
}
