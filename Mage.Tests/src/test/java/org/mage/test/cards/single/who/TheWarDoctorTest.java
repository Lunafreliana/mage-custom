package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author Susucr
 */
public class TheWarDoctorTest extends CardTestPlayerBase {

    private static final String doctor = "The War Doctor";

    @Test
    public void testMultipleCardsExiledTogetherGiveOneCounter() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerA, "Tormod's Crypt");
        addCard(Zone.GRAVEYARD, playerB, "Grizzly Bears", 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}, Sacrifice {this}", playerB);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount(playerB, "Grizzly Bears", 2);
        assertCounterCount(playerA, doctor, CounterType.TIME, 1);
    }

    @Test
    public void testOtherPermanentPhasingOutGivesCounter() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Guardian of Faith");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Guardian of Faith");
        setTarget(playerA, "Grizzly Bears");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertCounterCount(playerA, doctor, CounterType.TIME, 1);
    }

    @Test
    public void testAttackDamageAndExileReplacement() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, doctor, CounterType.TIME, 2);

        attack(1, playerA, doctor, playerB);
        setTarget(playerA, "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount(playerB, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Grizzly Bears", 0);
    }
}
