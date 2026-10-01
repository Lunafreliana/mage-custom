package org.mage.test.cards.single.mbc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.v.VenserVisionaryTraveler Venser, Visionary Traveler}
 *
 * @author TheElk801
 */
public class VenserVisionaryTravelerTest extends CardTestPlayerBase {

    @Test
    public void testCountersOnlyWhenNotCastFromHand() {
        addCard(Zone.BATTLEFIELD, playerA, "Venser, Visionary Traveler");
        addCard(Zone.HAND, playerA, "Llanowar Elves");
        addCard(Zone.HAND, playerA, "Fyndhorn Elves");
        addCard(Zone.BATTLEFIELD, playerA, "Elvish Piper");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Llanowar Elves");
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{G},");
        setChoice(playerA, true);
        setChoice(playerA, "Fyndhorn Elves");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPowerToughness(playerA, "Llanowar Elves", 1, 1);
        assertPowerToughness(playerA, "Fyndhorn Elves", 3, 3);
    }

    @Test
    public void testPlusOneExilesAndReturnsOtherPermanent() {
        addCard(Zone.BATTLEFIELD, playerA, "Venser, Visionary Traveler");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "+1:");
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertPowerToughness(playerA, "Grizzly Bears", 4, 4);
        assertCounterCount(playerA, "Venser, Visionary Traveler", CounterType.LOYALTY, 5);
    }

    @Test
    public void testMinusTwoReturnsOpponentsPermanent() {
        addCard(Zone.BATTLEFIELD, playerA, "Venser, Visionary Traveler");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "-2:");
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Grizzly Bears", 0);
        assertHandCount(playerB, "Grizzly Bears", 1);
        assertCounterCount(playerA, "Venser, Visionary Traveler", CounterType.LOYALTY, 2);
    }
}
