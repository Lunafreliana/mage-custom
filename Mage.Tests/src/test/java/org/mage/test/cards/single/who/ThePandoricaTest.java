package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class ThePandoricaTest extends CardTestPlayerBase {

    private static final String pandorica = "The Pandorica";
    private static final String target = "Grizzly Bears";

    @Test
    public void testTargetStaysPhasedOutWhilePandoricaIsTapped() {
        addCard(Zone.BATTLEFIELD, playerA, pandorica);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerB, target, 1, true);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{W}, {T}:", target);
        checkPermanentCount("target phased out", 1, PhaseStep.POSTCOMBAT_MAIN, playerB, target, 0);
        checkPermanentCount("target cannot phase in", 2, PhaseStep.POSTCOMBAT_MAIN, playerB, target, 0);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertTapped(pandorica, true);
    }

    @Test
    public void testTargetPhasesInWhenPandoricaUntaps() {
        addCard(Zone.BATTLEFIELD, playerA, pandorica);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerB, target, 1, true);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{W}, {T}:", target);
        checkPermanentCount("target phased out", 2, PhaseStep.POSTCOMBAT_MAIN, playerB, target, 0);
        setChoice(playerA, true); // Untap The Pandorica on turn 3.

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertTapped(pandorica, false);
        assertPermanentCount(playerB, target, 1);
        assertTapped(target, false);
    }

    @Test
    public void testMayKeepPandoricaTapped() {
        addCard(Zone.BATTLEFIELD, playerA, pandorica);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerB, target);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{W}, {T}:", target);
        setChoice(playerA, false); // Don't untap The Pandorica on turn 3.

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertTapped(pandorica, true);
        assertPermanentCount(playerB, target, 0);
    }

    @Test
    public void testTargetPhasesInWhenPandoricaLeaves() {
        addCard(Zone.BATTLEFIELD, playerA, pandorica);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerB, target);
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);
        addCard(Zone.HAND, playerB, "Naturalize");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{W}, {T}:", target);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Naturalize", pandorica);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, pandorica, 1);
        assertPermanentCount(playerB, target, 1);
    }
}
