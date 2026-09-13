package org.mage.test.cards.abilities.keywords;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

/**
 * Regression tests for Escalate cost modification interacting with source-less
 * special actions such as the Planechase planar die roll.
 */
public class EscalatePlanechaseTest extends CardTestPlayerBase {

    @Test
    public void testPlanarDieIgnoresEscalateSpellInHand() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_HEDRON_FIELDS_OF_AGADEEM);

        // Escalate is a spell-only cost modification. Its source being present
        // must not inspect or modify the source-less planar-die special action.
        addCard(Zone.HAND, playerA, "Savage Alliance");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "roll the planar die");
        setDieRollResult(playerA, 3); // blank

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertHandCount(playerA, "Savage Alliance", 1);
    }
}
