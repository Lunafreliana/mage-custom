package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class TheMomentTest extends CardTestPlayerBase {

    private static final String moment = "The Moment";

    @Test
    public void phasesCreatureOutUntilItLeaves() {
        addCard(Zone.BATTLEFIELD, playerA, moment);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears", 1, true);
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 2);
        addCard(Zone.HAND, playerB, "Naturalize");
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{2}, {T}", "Grizzly Bears");
        checkPermanentCount("creature is phased out", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Grizzly Bears", 0);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Naturalize", moment);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, moment, 0);
        assertPermanentCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void destroysByManaValueThenSacrifices() {
        addCard(Zone.BATTLEFIELD, playerA, moment);
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Darksteel Myr"); // mana value 3, indestructible
        addCard(Zone.BATTLEFIELD, playerB, "Memnite"); // mana value 0
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears"); // mana value 2
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant"); // mana value 4
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, moment, CounterType.TIME, 3);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{3}, {T}");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, moment, 0);
        assertPermanentCount(playerA, "Darksteel Myr", 1);
        assertPermanentCount(playerB, "Memnite", 0);
        assertPermanentCount(playerB, "Grizzly Bears", 0);
        assertPermanentCount(playerB, "Hill Giant", 1);
        assertPermanentCount(playerA, "Wastes", 3);
    }
}
