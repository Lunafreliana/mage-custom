package org.mage.test.cards.single.frc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class NivMizzetGhostCounselTest extends CardTestPlayerBase {

    private static final String nivMizzet = "Niv-Mizzet, Ghost Counsel";

    @Test
    public void paysLifeAndDrawsAmountGained() {
        addCard(Zone.BATTLEFIELD, playerA, nivMizzet);
        addCard(Zone.HAND, playerA, "Chaplain's Blessing");
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 5);
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Chaplain's Blessing");
        setChoice(playerA, true); // Pay 5 life and draw five cards.

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerA, 20);
        assertHandCount(playerA, "Grizzly Bears", 5);
    }

    @Test
    public void mayDeclineLifePayment() {
        addCard(Zone.BATTLEFIELD, playerA, nivMizzet);
        addCard(Zone.HAND, playerA, "Chaplain's Blessing");
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Chaplain's Blessing");
        setChoice(playerA, false);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerA, 25);
        assertHandCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void tapAbilityDrainsAndTriggersCardDraw() {
        addCard(Zone.BATTLEFIELD, playerA, nivMizzet);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        skipInitShuffling();

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Each opponent loses 1 life");
        setChoice(playerA, true); // Pay the 1 life gained and draw a card.

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerA, 20);
        assertLife(playerB, 19);
        assertTapped(nivMizzet, true);
        assertHandCount(playerA, "Grizzly Bears", 1);
    }
}
