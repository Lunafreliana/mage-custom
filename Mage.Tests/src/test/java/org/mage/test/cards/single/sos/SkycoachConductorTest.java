package org.mage.test.cards.single.sos;

import mage.abilities.keyword.FlashAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class SkycoachConductorTest extends CardTestPlayerBase {

    private static final String CONDUCTOR = "Skycoach Conductor";
    private static final String ALL_ABOARD = "All Aboard";
    private static final String BEARS = "Grizzly Bears";

    @Test
    public void testAllAboardReturnsCreatureUnderOwnersControl() {
        addCard(Zone.HAND, playerA, CONDUCTOR);
        addCard(Zone.HAND, playerA, "Act of Treason");
        addCard(Zone.BATTLEFIELD, playerA, "Volcanic Island", 7);
        addCard(Zone.BATTLEFIELD, playerB, BEARS);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, CONDUCTOR);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Act of Treason", BEARS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, BEARS, CounterType.P1P1, 1);
        checkPlayableAbility("prepared instant is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + ALL_ABOARD, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ALL_ABOARD, BEARS);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, CONDUCTOR, 1);
        assertAbility(playerA, CONDUCTOR, FlashAbility.getInstance(), true);
        assertAbility(playerA, CONDUCTOR, FlyingAbility.getInstance(), true);
        assertAbility(playerA, CONDUCTOR, VigilanceAbility.getInstance(), true);
        assertPermanentCount(playerA, BEARS, 0);
        assertPermanentCount(playerB, BEARS, 1);
        assertCounterCount(playerB, BEARS, CounterType.P1P1, 0);
        assertExileCount(playerA, ALL_ABOARD, 0);
    }

    @Test
    public void testAllAboardCannotTargetPilotOrOpponentsCreature() {
        addCard(Zone.HAND, playerA, CONDUCTOR);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.BATTLEFIELD, playerB, BEARS);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, CONDUCTOR);
        checkPlayableAbility("there is no legal non-Pilot creature you control", 1,
                PhaseStep.POSTCOMBAT_MAIN, playerA, "Cast " + ALL_ABOARD, false);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, CONDUCTOR, 1);
        assertPermanentCount(playerB, BEARS, 1);
        assertExileCount(playerA, ALL_ABOARD, 1);
    }
}
