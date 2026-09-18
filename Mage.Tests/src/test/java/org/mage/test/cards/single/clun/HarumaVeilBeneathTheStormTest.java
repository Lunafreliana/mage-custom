package org.mage.test.cards.single.clun;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class HarumaVeilBeneathTheStormTest extends CardTestPlayerBase {

    private static final String HARUMA = "Haruma, Veil Beneath the Storm";

    @Test
    public void lookingAtOpponentsHandAddsPerceptionCounter() {
        addCard(Zone.BATTLEFIELD, playerA, HARUMA);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerB, "Grizzly Bears");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{U}: Look at target opponent's hand");
        addTarget(playerA, playerB);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, HARUMA, CounterType.PERCEPTION, 1);
    }

    @Test
    public void healingArtsRevealsChosenCardsAndGainsLife() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, HARUMA);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Hill Giant");

        setChoice(playerA, true); // use the optional end-step trigger
        addTarget(playerA, "Grizzly Bears^Hill Giant");
        setStopAt(1, PhaseStep.CLEANUP);
        execute();

        assertLife(playerA, 24);
    }
}
