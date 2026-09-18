package org.mage.test.cards.single.clun;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
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
        Assert.assertEquals("Tailcurse must enchant the targeted player", playerB.getId(),
                getPermanent("Tailcurse", playerA.getId()).getAttachedTo());
    }

    @Test
    public void simultaneousCombatDamageIsCombinedAndRewardsEachEligiblePlayerOnce() {
        addCard(Zone.HAND, playerA, vulpes);
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island");
        addCard(Zone.BATTLEFIELD, playerA, "Volcanic Island");
        addCard(Zone.BATTLEFIELD, playerA, "Taiga");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        // A casts Vulpes in turn 1 and attaches Tailcurse to D.
        // B takes turn 4 in this four-player test and deals 5 simultaneous combat damage to D.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, vulpes);
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
        attack(1, playerA, vulpes, playerB);

        // Only the attack trigger fires for a creature placed directly on the battlefield.
        // Attach Tailcurse to B before Vulpes deals 3 combat damage to B.
        addTarget(playerA, playerB);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 17);
        assertPermanentCount(playerA, "Elemental Token", 3);
        assertPermanentCount(playerB, "Elemental Token", 0);
    }
}
