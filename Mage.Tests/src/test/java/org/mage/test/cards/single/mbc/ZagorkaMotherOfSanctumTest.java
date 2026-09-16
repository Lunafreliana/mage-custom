package org.mage.test.cards.single.mbc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class ZagorkaMotherOfSanctumTest extends CardTestPlayerBase {

    private static final String zagorka = "Zagorka, Mother of Sanctum";

    @Test
    public void playersMayCreateTappedSanctums() {
        addCard(Zone.BATTLEFIELD, playerA, zagorka);

        setChoice(playerA, true);
        setChoice(playerB, false);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Sanctum", 1);
        assertTapped("Sanctum", true);
        assertPermanentCount(playerB, "Sanctum", 0);
    }

    @Test
    public void opponentWhoCreatesSanctumCantAttackYouNextTurn() {
        addCard(Zone.BATTLEFIELD, playerA, zagorka);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        setChoice(playerA, false);
        setChoice(playerB, true);
        attack(2, playerB, "Grizzly Bears", playerA);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        try {
            execute();
            Assert.fail("Attacking Zagorka's controller should be illegal");
        } catch (Throwable e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("must have 0 actions but found 1"));
        }

        assertPermanentCount(playerB, "Sanctum", 1);
        assertLife(playerA, 20);
        assertTapped("Grizzly Bears", false);
    }

    @Test
    public void opponentWhoDeclinesMayAttack() {
        addCard(Zone.BATTLEFIELD, playerA, zagorka);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        setChoice(playerA, false);
        setChoice(playerB, false);
        attack(2, playerB, "Grizzly Bears", playerA);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Sanctum", 0);
        assertLife(playerA, 18);
        assertTapped("Grizzly Bears", true);
    }
}
