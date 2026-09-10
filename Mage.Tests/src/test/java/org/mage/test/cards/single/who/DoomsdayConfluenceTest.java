package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class DoomsdayConfluenceTest extends CardTestPlayerBase {

    private static final String confluence = "Doomsday Confluence";

    @Test
    public void testChooseXModesAndResolveInPrintedOrder() {
        addCard(Zone.HAND, playerA, confluence);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 7);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");
        addCard(Zone.HAND, playerB, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, confluence);
        setChoice(playerA, "X=3");
        setModeChoice(playerA, "3");
        setModeChoice(playerA, "2");
        setModeChoice(playerA, "1");
        setChoice(playerA, "Grizzly Bears");
        setChoice(playerB, "Grizzly Bears");
        addTarget(playerB, "Island");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Grizzly Bears", 1);
        assertPermanentCount(playerB, "Memnite", 1);
        assertPermanentCount(playerA, "Dalek Token", 1);
        assertGraveyardCount(playerB, "Island", 1);
    }

    @Test
    public void testChooseMoreThanFiveModes() {
        addCard(Zone.HAND, playerA, confluence);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 13);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, confluence);
        setChoice(playerA, "X=6");
        for (int i = 0; i < 6; i++) {
            setModeChoice(playerA, "2");
        }

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Dalek Token", 6);
    }
}
