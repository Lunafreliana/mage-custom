package org.mage.test.cards.single.msc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.h.HERBIELovableRobot H.E.R.B.I.E., Lovable Robot}
 *
 * @author VibecodingQueens
 */
public class HERBIELovableRobotTest extends CardTestPlayerBase {

    private static final String herbie = "H.E.R.B.I.E., Lovable Robot";

    @Test
    public void testSurveilsAfterCastingNoncreatureSpell() {
        skipInitShuffling();
        addCard(Zone.BATTLEFIELD, playerA, herbie);
        addCard(Zone.HAND, playerA, "Ornithopter");
        addCard(Zone.LIBRARY, playerA, "Mountain");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Ornithopter");
        addTarget(playerA, "Mountain");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Mountain", 1);
    }

    @Test
    public void testCreatureSpellDoesNotEnableSurveil() {
        skipInitShuffling();
        addCard(Zone.BATTLEFIELD, playerA, herbie);
        addCard(Zone.HAND, playerA, "Memnite");
        addCard(Zone.LIBRARY, playerA, "Mountain");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLibraryCount(playerA, "Mountain", 1);
        assertGraveyardCount(playerA, "Mountain", 0);
    }
}
