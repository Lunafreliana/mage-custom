package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class EliteInterceptorTest extends CardTestPlayerBase {

    private static final String ELITE_INTERCEPTOR = "Elite Interceptor";
    private static final String REJOINDER = "Rejoinder";

    @Test
    public void rejoinderCanTapTargetAndDrawsCard() {
        skipInitShuffling();
        addCard(Zone.HAND, playerA, ELITE_INTERCEPTOR);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Silvercoat Lion");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ELITE_INTERCEPTOR);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REJOINDER, "Grizzly Bears");
        setChoice(playerA, true); // Tap the untapped target.

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertTapped("Grizzly Bears", true);
        assertHandCount(playerA, "Silvercoat Lion", 1);
        assertExileCount(playerA, REJOINDER, 0);
    }

    @Test
    public void rejoinderMayLeaveTargetUntappedAndStillDrawsCard() {
        skipInitShuffling();
        addCard(Zone.HAND, playerA, ELITE_INTERCEPTOR);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Silvercoat Lion");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ELITE_INTERCEPTOR);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REJOINDER, "Grizzly Bears");
        setChoice(playerA, false); // Decline to tap the target.

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertTapped("Grizzly Bears", false);
        assertHandCount(playerA, "Silvercoat Lion", 1);
    }
}
