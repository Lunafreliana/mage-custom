package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class ProctorOfPotentialTest extends CardTestPlayerBase {

    private static final String PROCTOR = "Proctor of Potential";

    @Test
    public void triggersForItselfAndAnotherCreature() {
        skipInitShuffling();
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.HAND, playerA, PROCTOR);
        addCard(Zone.HAND, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PROCTOR);
        addTarget(playerA, "Mountain");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");
        addTarget(playerA, "Forest");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, "Mountain", 1);
        assertGraveyardCount(playerA, "Forest", 1);
    }

    @Test
    public void returnsAfterSurveillingWithFinalityCounter() {
        skipInitShuffling();
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.HAND, playerA, PROCTOR);
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PROCTOR);
        addTarget(playerA, "Forest");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", PROCTOR);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{W}{U}: Return this card");
        addTarget(playerA, TestPlayer.TARGET_SKIP); // Proctor entering triggers surveil again

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, PROCTOR, 1);
        assertCounterCount(PROCTOR, CounterType.FINALITY, 1);
    }

    @Test
    public void cannotReturnWithoutScryingOrSurveilling() {
        addCard(Zone.GRAVEYARD, playerA, PROCTOR);
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 2);

        checkPlayableAbility(
                "Return is restricted", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                "{W}{U}: Return this card", false
        );

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, PROCTOR, 1);
    }
}
