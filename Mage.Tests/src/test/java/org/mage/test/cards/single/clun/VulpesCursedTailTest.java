package org.mage.test.cards.single.clun;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommander4Players;

public class VulpesCursedTailTest extends CardTestCommander4Players {

    private static final String vulpes = "Vulpes, Cursed Tail";

    @Test
    public void createsAndAttachesTailcurseOnEntry() {
        addCard(Zone.HAND, playerA, vulpes);
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island");
        addCard(Zone.BATTLEFIELD, playerA, "Volcanic Island");
        addCard(Zone.BATTLEFIELD, playerA, "Taiga");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, vulpes);
        addTarget(playerA, playerB);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, vulpes, 1);
        assertPermanentCount(playerA, "Tailcurse", 1);
    }

    @Test
    public void simultaneousCombatDamageIsCombinedAndRewardsEachEligiblePlayerOnce() {
        addCard(Zone.BATTLEFIELD, playerA, vulpes);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        // Vulpes's entry trigger targets D. Both of B's creatures deal damage simultaneously.
        addTarget(playerA, playerD);
        attack(4, playerB, "Grizzly Bears", playerD);
        attack(4, playerB, "Hill Giant", playerD);

        setStopAt(4, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerD, 15);
        assertPermanentCount(playerA, "Elemental Token", 5);
        assertPermanentCount(playerB, "Elemental Token", 5);
        assertPermanentCount(playerC, "Elemental Token", 0);
        assertPermanentCount(playerD, "Elemental Token", 0);
    }

    @Test
    public void curseControllerIsNotRewardedTwiceForOwnDamage() {
        addCard(Zone.BATTLEFIELD, playerA, vulpes);
        addTarget(playerA, playerB);
        attack(1, playerA, vulpes, playerB);

        // Put the Tailcurse from the attack trigger on C so only B's Tailcurse sees the damage.
        addTarget(playerA, playerC);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 17);
        assertPermanentCount(playerA, "Elemental Token", 3);
        assertPermanentCount(playerB, "Elemental Token", 0);
    }
}
