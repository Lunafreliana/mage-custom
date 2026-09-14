package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class AgyremTest extends CardTestPlayerBase {

    @Test
    public void creatureReturnsAtEndStepAfterPlaneswalkingAway() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(Planes.PLANE_AGYREM, Planes.PLANE_AKOUM);

        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerB, "Centaur Courser");

        attack(1, playerA, "Silvercoat Lion");
        block(1, playerB, "Centaur Courser", "Silvercoat Lion");

        checkGraveyardCount("after combat", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Silvercoat Lion", 1);
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "roll the planar die");
        setDieRollResult(playerA, 6); // Planeswalker symbol.

        runCode("check planeswalked away from Agyrem", 1, PhaseStep.END_TURN, playerA,
                (info, player, game) -> Assert.assertEquals(
                        info, "Plane - Akoum", game.getState().getFaceUpPlanes().get(0).getName()));
        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Silvercoat Lion", 1);
        assertGraveyardCount(playerA, "Silvercoat Lion", 0);
    }
}
