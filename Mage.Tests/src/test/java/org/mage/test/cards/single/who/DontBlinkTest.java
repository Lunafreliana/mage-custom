package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class DontBlinkTest extends CardTestPlayerBase {

    private static final String dontBlink = "Don't Blink";

    @Test
    public void creatureEnteringDirectlyFromExileIsShuffledAway() {
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, dontBlink);
        addCard(Zone.HAND, playerA, "Ephemerate");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dontBlink);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Ephemerate", "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 0);
        assertLibraryCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void creatureCastFromExileIsShuffledAway() {
        addCard(Zone.BATTLEFIELD, playerA, "Volcanic Island", 7);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.HAND, playerA, dontBlink);
        addCard(Zone.HAND, playerA, "Bonecrusher Giant");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Stomp", "Hill Giant");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dontBlink);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Bonecrusher Giant");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Bonecrusher Giant", 0);
        assertLibraryCount(playerA, "Bonecrusher Giant", 1);
    }

    @Test
    public void creatureCastFromHandIsUnaffected() {
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island", 4);
        addCard(Zone.HAND, playerA, dontBlink);
        addCard(Zone.HAND, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dontBlink);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void effectExpiresAtEndOfTurn() {
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, dontBlink);
        addCard(Zone.HAND, playerA, "Ephemerate");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dontBlink);
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Ephemerate", "Grizzly Bears");

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertLibraryCount(playerA, "Grizzly Bears", 0);
    }
}
