package org.mage.test.cards.single.mkc;

import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class OtherworldlyEscortTest extends CardTestPlayerBase {

    @Test
    public void testOtherworldlyEscort() {
        addCard(Zone.BATTLEFIELD, playerA, "Otherworldly Escort");
        addCard(Zone.BATTLEFIELD, playerA, "Ashnod's Altar");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Sacrifice");
        setChoice(playerA, "Otherworldly Escort");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Otherworldly Escort", 1);
        assertSubtype("Otherworldly Escort", SubType.DETECTIVE);
        assertSubtype("Otherworldly Escort", SubType.SPIRIT);
        assertNotSubtype("Otherworldly Escort", SubType.HUMAN);
        assertCounterCount("Otherworldly Escort", CounterType.CHARGE, 4);
    }

    @Test
    public void testOtherworldlyEscort2() {
        addCard(Zone.BATTLEFIELD, playerA, "Otherworldly Escort");
        addCard(Zone.BATTLEFIELD, playerA, "Ashnod's Altar");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Sacrifice");
        setChoice(playerA, "Otherworldly Escort");
        activateAbility(2, PhaseStep.PRECOMBAT_MAIN, playerA, "Sacrifice");
        setChoice(playerA, "Otherworldly Escort");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Otherworldly Escort", 0);
        assertGraveyardCount(playerA, "Otherworldly Escort", 1);
    }


    @Test
    public void testBlinkResetsReturnedCreatureTypes() {
        addCard(Zone.BATTLEFIELD, playerA, "Otherworldly Escort");
        addCard(Zone.BATTLEFIELD, playerA, "Ashnod's Altar");
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, "Cloudshift");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Sacrifice");
        setChoice(playerA, "Otherworldly Escort");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cloudshift", "Otherworldly Escort");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Otherworldly Escort", 1);
        assertSubtype("Otherworldly Escort", SubType.HUMAN);
        assertSubtype("Otherworldly Escort", SubType.DETECTIVE);
        assertNotSubtype("Otherworldly Escort", SubType.SPIRIT);
        assertCounterCount("Otherworldly Escort", CounterType.CHARGE, 0);
    }
}
