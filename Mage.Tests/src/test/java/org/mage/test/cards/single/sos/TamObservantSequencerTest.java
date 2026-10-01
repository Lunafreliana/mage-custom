package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class TamObservantSequencerTest extends CardTestPlayerBase {

    private static final String TAM = "Tam, Observant Sequencer";
    private static final String DEEP_SIGHT = "Deep Sight";

    @Test
    public void testLandfallPreparesTamAndDeepSightDrawsAndGainsLife() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.BATTLEFIELD, playerA, TAM);
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island");
        addCard(Zone.HAND, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");

        checkPlayableAbility("Tam begins unprepared", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + DEEP_SIGHT, false);
        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Forest");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("landfall creates a castable prepare spell", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + DEEP_SIGHT, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, DEEP_SIGHT);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, TAM, 1);
        assertHandCount(playerA, "Grizzly Bears", 1);
        assertLife(playerA, 21);
        assertExileCount(playerA, DEEP_SIGHT, 0);
    }
}
