package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class CastAwayDoubtTest extends CardTestPlayerBase {

    @Test
    public void testDrawsTwoAndDamagesEachPlayer() {
        removeAllCardsFromLibrary(playerA);
        removeAllCardsFromLibrary(playerB);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Hill Giant");
        addCard(Zone.HAND, playerA, "Cast Away Doubt");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Away Doubt");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerA, 2);
        assertLibraryCount(playerA, 0);
        assertLife(playerA, 18);
        assertLife(playerB, 18);
        assertGraveyardCount(playerA, "Cast Away Doubt", 1);
    }
}
