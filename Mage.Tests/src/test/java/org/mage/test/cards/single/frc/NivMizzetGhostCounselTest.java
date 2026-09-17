package org.mage.test.cards.single.frc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class NivMizzetGhostCounselTest extends CardTestPlayerBase {

    private static final String niv = "Niv-Mizzet, Ghost Counsel";

    @Test
    public void testPayLifeToDraw() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, niv);
        addCard(Zone.HAND, playerA, "Heroes' Reunion");
        addCard(Zone.BATTLEFIELD, playerA, "Savannah");
        addCard(Zone.BATTLEFIELD, playerA, "Plateau");
        addCard(Zone.BATTLEFIELD, playerA, "Taiga");
        addCard(Zone.LIBRARY, playerA, "Mountain", 7);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Heroes' Reunion", playerA);
        setChoice(playerA, true);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerA, 20);
        assertHandCount(playerA, 7);
    }

    @Test
    public void testDeclinePayment() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, niv);
        addCard(Zone.HAND, playerA, "Heroes' Reunion");
        addCard(Zone.BATTLEFIELD, playerA, "Savannah");
        addCard(Zone.BATTLEFIELD, playerA, "Plateau");
        addCard(Zone.BATTLEFIELD, playerA, "Taiga");
        addCard(Zone.LIBRARY, playerA, "Mountain", 7);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Heroes' Reunion", playerA);
        setChoice(playerA, false); // Don't pay 7 life

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerA, 27);
        assertHandCount(playerA, 0);
    }

    @Test
    public void testTapAbility() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, niv);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Each opponent");
        setChoice(playerA, false); // Don't pay the life gained by the activated ability

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertTapped(niv, true);
        assertLife(playerA, 21);
        assertLife(playerB, 19);
    }
}
