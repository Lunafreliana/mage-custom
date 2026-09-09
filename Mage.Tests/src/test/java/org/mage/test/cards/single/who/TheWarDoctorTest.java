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
    public void testMultipleCardsExiledTogetherAreAllCounted() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerA, "Tormod's Crypt");
        addCard(Zone.GRAVEYARD, playerB, "Grizzly Bears", 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}, Sacrifice {this}", playerB);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount(playerB, "Grizzly Bears", 2);
        assertCounterCount(playerA, doctor, CounterType.TIME, 2);
    }

    @Test
    public void testMultiplePermanentsPhasingOutTogetherAreAllCounted() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears", 2);
        addCard(Zone.HAND, playerA, "Guardian of Faith");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Guardian of Faith");
        addTarget(playerA, "Grizzly Bears^Grizzly Bears");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertCounterCount(playerA, doctor, CounterType.TIME, 2);
    }

    @Test
    public void testWarDoctorDoesNotCountItselfPhasingOut() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Guardian of Faith");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Guardian of Faith");
        addTarget(playerA, doctor + "^Grizzly Bears");

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, doctor, CounterType.TIME, 1);
    }

    @Test
    public void testAttackDamageUsesTimeCounters() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, doctor, CounterType.TIME, 3);

        attack(1, playerA, doctor, playerB);
        addTarget(playerA, playerB);

        setStopAt(1, PhaseStep.DECLARE_BLOCKERS);
        execute();

        assertLife(playerB, 17);
    }

    @Test
    public void testCreatureDamagedByTriggerIsExiledIfItDiesLater() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerB, "Centaur Courser");
        addCard(Zone.HAND, playerA, "Shock");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, doctor, CounterType.TIME, 2);

        attack(1, playerA, doctor, playerB);
        addTarget(playerA, "Centaur Courser");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Shock", "Centaur Courser");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertExileCount(playerB, "Centaur Courser", 1);
        assertGraveyardCount(playerB, "Centaur Courser", 0);
    }

    @Test
    public void testCreatureDamagedByWarDoctorCombatIsNotExiled() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, doctor, CounterType.TIME, 1);

        attack(1, playerA, doctor, playerB);
        addTarget(playerA, playerB);
        block(1, playerB, "Hill Giant", doctor);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerB, "Hill Giant", 1);
        assertExileCount(playerB, "Hill Giant", 0);
    }

    @Test
    public void testPreventedTriggerDamageDoesNotCreateReplacement() {
        addCard(Zone.BATTLEFIELD, playerA, doctor);
        addCard(Zone.BATTLEFIELD, playerB, "Silver Knight");
        addCard(Zone.HAND, playerB, "Murder");
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 3);
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, doctor, CounterType.TIME, 2);

        attack(1, playerA, doctor, playerB);
        addTarget(playerA, "Silver Knight");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Murder", "Silver Knight");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerB, "Silver Knight", 1);
        assertExileCount(playerB, "Silver Knight", 0);
    }
}
