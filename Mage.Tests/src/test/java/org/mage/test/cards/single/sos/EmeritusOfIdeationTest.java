package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class EmeritusOfIdeationTest extends CardTestPlayerBase {

    private static final String EMERITUS = "Emeritus of Ideation";
    private static final String RECALL = "Ancestral Recall";
    private static final String SOS_EMERITUS = "SOS-" + EMERITUS;

    @Test
    public void entersPrepared() {
        // Use the set-qualified printing so this test also guards card-repository registration.
        addCard(Zone.HAND, playerA, SOS_EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, EMERITUS, 1);
        assertExileCount(playerA, RECALL, 1);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertTrue(emeritus.isPrepared());
    }

    @Test
    public void castsPreparedAncestralRecall() {
        removeAllCardsFromLibrary(playerB);
        addCard(Zone.LIBRARY, playerB, "Darksteel Relic", 3);
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 6);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + RECALL, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, RECALL, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerB, "Darksteel Relic", 3);
        assertExileCount(playerA, RECALL, 0);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
    }

    @Test
    public void attackCanExileEightCardsToBecomePreparedAgain() {
        removeAllCardsFromLibrary(playerB);
        addCard(Zone.LIBRARY, playerB, "Darksteel Relic", 7);
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
        checkPlayableAbility("re-prepared spell is castable", 3, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Cast " + RECALL, true);
        castSpell(3, PhaseStep.POSTCOMBAT_MAIN, playerA, RECALL, playerB);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.END_TURN);
        execute();

        assertHandCount(playerB, "Darksteel Relic", 7);
        assertGraveyardCount(playerA, "Grizzly Bears", 0);
        assertExileCount(playerA, "Grizzly Bears", 8);
        assertExileCount(playerA, RECALL, 0);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
    }
}
