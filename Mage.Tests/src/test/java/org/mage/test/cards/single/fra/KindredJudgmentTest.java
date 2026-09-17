package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class KindredJudgmentTest extends CardTestPlayerBase {

    private static final String judgment = "Kindred Judgment";

    @Test
    public void testChosenTypeSurvives() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 7);
        addCard(Zone.HAND, playerA, judgment);
        addCard(Zone.BATTLEFIELD, playerA, "Cylian Elf");
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerB, "Elvish Mystic");
        addCard(Zone.BATTLEFIELD, playerB, "Darksteel Myr");

        setChoice(playerA, "Elf");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, judgment);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Cylian Elf", 1);
        assertGraveyardCount(playerA, "Silvercoat Lion", 1);
        assertPermanentCount(playerB, "Elvish Mystic", 1);
        assertPermanentCount(playerB, "Darksteel Myr", 1);
    }
}
