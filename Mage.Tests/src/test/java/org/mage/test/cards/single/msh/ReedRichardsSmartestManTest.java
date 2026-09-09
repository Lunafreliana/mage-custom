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

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        // Opening 2 + normal draw step - 2 spells + (4 + 1) + (1 + 1).
        // Reed's maximum-hand-size ability also keeps all eight through cleanup.
        assertHandCount(playerA, 8);
    }

    @Test
    public void testFirstDrawStepCardIsExemptButAdditionalDrawIsReplaced() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Island", 10);
        addCard(Zone.BATTLEFIELD, playerA, reed);
        addCard(Zone.BATTLEFIELD, playerA, "Howling Mine");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
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

        // Opening 2 + two draw steps - 2 spells + five cards per Divination.
        assertHandCount(playerA, 12);
    }
}
