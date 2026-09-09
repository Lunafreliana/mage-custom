package org.mage.test.cards.single.msc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestMultiPlayerBaseWithRangeAll;

import static org.junit.Assert.assertTrue;

public class PuppetMasterStringPullerTest extends CardTestMultiPlayerBaseWithRangeAll {

    private static final String puppetMaster = "Puppet Master, String Puller";
    private static final String bears = "Grizzly Bears";
    private static final String lion = "Silvercoat Lion";

    @Test
    public void testAttackTriggerGoadsAndPreventsBlocking() {
        addCard(Zone.BATTLEFIELD, playerA, puppetMaster);
        addCard(Zone.BATTLEFIELD, playerA, bears);
        addCard(Zone.BATTLEFIELD, playerD, lion);

        attack(1, playerA, bears, playerD);
        block(1, playerD, lion, bears); // The block is illegal and is ignored.

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerD, 18);
        assertTrue(getPermanent(lion).getGoadingPlayers().contains(playerA.getId()));
    }

    @Test
    public void testGoadedCreaturesDamageOpponentTriggersOnlyOnce() {
        addCard(Zone.BATTLEFIELD, playerA, puppetMaster);
        addCard(Zone.HAND, playerA, "Jeering Homunculus", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.BATTLEFIELD, playerD, bears);
        addCard(Zone.BATTLEFIELD, playerD, lion);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Jeering Homunculus");
        setChoice(playerA, "Yes");
        addTarget(playerA, bears);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Jeering Homunculus");
        setChoice(playerA, "Yes");
        addTarget(playerA, lion);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerC, 16);
        assertPermanentCount(playerA, "Treasure Token", 1);
    }
}
