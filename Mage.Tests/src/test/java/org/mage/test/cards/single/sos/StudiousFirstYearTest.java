package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class StudiousFirstYearTest extends CardTestPlayerBase {

    private static final String FIRST_YEAR = "Studious First-Year";
    private static final String RAMPANT_GROWTH = "Rampant Growth";

    @Test
    public void testPrepareSpellSearchesForTappedBasicLand() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.HAND, playerA, FIRST_YEAR);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, FIRST_YEAR);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + RAMPANT_GROWTH, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, RAMPANT_GROWTH);
        addTarget(playerA, "Island");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, FIRST_YEAR, 1);
        assertExileCount(playerA, RAMPANT_GROWTH, 0);
        assertPermanentCount(playerA, "Island", 1);
        assertTapped("Island", true);
    }
}
