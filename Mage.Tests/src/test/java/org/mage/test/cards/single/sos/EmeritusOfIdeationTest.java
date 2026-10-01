package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class EmeritusOfIdeationTest extends CardTestPlayerBase {

    private static final String EMERITUS = "Emeritus of Ideation";
    private static final String RECALL = "Ancestral Recall";

    @Test
    public void entersPreparedAndCastsAncestralRecall() {
        skipInitShuffling();
        addCard(Zone.LIBRARY, playerB, "Darksteel Relic", 3);
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 6);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, RECALL, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerB, "Darksteel Relic", 3);
        assertExileCount(playerA, RECALL, 0);
    }

    @Test
    public void attackCanExileEightCardsToBecomePreparedAgain() {
        skipInitShuffling();
        addCard(Zone.LIBRARY, playerB, "Darksteel Relic", 6);
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 7);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears", 8);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, RECALL, playerB);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        attack(3, playerA, EMERITUS, playerB);
        setChoice(playerA, true);
        setChoice(playerA, "Grizzly Bears^Grizzly Bears^Grizzly Bears^Grizzly Bears^"
                + "Grizzly Bears^Grizzly Bears^Grizzly Bears^Grizzly Bears");
        waitStackResolved(3, PhaseStep.DECLARE_ATTACKERS);
        castSpell(3, PhaseStep.POSTCOMBAT_MAIN, playerA, RECALL, playerB);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.END_TURN);
        execute();

        assertHandCount(playerB, "Darksteel Relic", 6);
        assertGraveyardCount(playerA, "Grizzly Bears", 0);
        assertExileCount(playerA, "Grizzly Bears", 8);
        assertExileCount(playerA, RECALL, 0);
    }
}
