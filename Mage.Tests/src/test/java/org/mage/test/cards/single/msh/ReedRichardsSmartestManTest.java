package org.mage.test.cards.single.msh;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class ReedRichardsSmartestManTest extends CardTestPlayerBase {

    private static final String reed = "Reed Richards, Smartest Man";

    @Test
    public void testOnlyFirstEligibleDrawEachTurnIsReplaced() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Island", 20);
        addCard(Zone.BATTLEFIELD, playerA, reed);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 6);
        addCard(Zone.HAND, playerA, "Divination", 2);
        addCard(Zone.HAND, playerA, "Ornithopter", 2);

        // Resolve the first sorcery before trying to cast the second one. Without
        // this priority barrier, the second command runs while Divination is on
        // the stack and is therefore illegal at sorcery speed.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination", true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        // Opening 4 - 2 spells + (4 + 1) + (1 + 1). Player A skips the
        // first turn's draw step, and Reed keeps all nine cards through cleanup.
        assertHandCount(playerA, 9);
    }

    @Test
    public void testFirstDrawStepCardIsExemptButAdditionalDrawIsReplaced() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Island", 10);
        addCard(Zone.BATTLEFIELD, playerA, reed);
        addCard(Zone.BATTLEFIELD, playerA, "Howling Mine");

        // Player A skips the draw step on turn 1, so use their next turn.
        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // The turn-based draw is unchanged, then Howling Mine's draw becomes four.
        assertHandCount(playerA, 5);
    }

    @Test
    public void testReplacementResetsOnNextTurn() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Island", 20);
        addCard(Zone.BATTLEFIELD, playerA, reed);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 6);
        addCard(Zone.HAND, playerA, "Divination", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        // Opening 2 + the turn 3 draw step - 2 spells + five cards per Divination.
        // Player A skips the draw step on turn 1.
        assertHandCount(playerA, 11);
    }
}
