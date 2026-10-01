package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class EliteInterceptorTest extends CardTestPlayerBase {

    private static final String INTERCEPTOR = "Elite Interceptor";
    private static final String REJOINDER = "Rejoinder";

    @Test
    public void testEntersPreparedAndCastsRejoinder() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.HAND, playerA, INTERCEPTOR);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, INTERCEPTOR);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + REJOINDER, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REJOINDER, "Grizzly Bears");
        setChoice(playerA, "Yes"); // Tap the targeted creature.

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, INTERCEPTOR, 1);
        assertPowerToughness(playerA, INTERCEPTOR, 1, 2);
        assertTapped("Grizzly Bears", true);
        assertHandCount(playerA, "Island", 1);
        assertExileCount(playerA, REJOINDER, 0);
    }
}
