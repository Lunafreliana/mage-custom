package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class TamObservantSequencerTest extends CardTestPlayerBase {

    private static final String TAM = "Tam, Observant Sequencer";
    private static final String DEEP_SIGHT = "Deep Sight";

    @Test
    public void landfallPreparesTamAndDeepSightDrawsAndGainsLife() {
        removeAllCardsFromLibrary(playerA);
        // Use the set-qualified printing so this test also guards card-repository registration.
        addCard(Zone.BATTLEFIELD, playerA, "SOS-" + TAM);
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
        Permanent tam = getPermanent(TAM, playerA);
        Assert.assertNotNull(tam);
        Assert.assertFalse(tam.isPrepared());
    }

    @Test
    public void opponentsLandfallDoesNotPrepareTam() {
        addCard(Zone.BATTLEFIELD, playerA, "SOS-" + TAM);
        addCard(Zone.HAND, playerB, "Forest");

        playLand(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Forest");
        checkPlayableAbility("an opponent's land does not prepare Tam", 3, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + DEEP_SIGHT, false);

        setStopAt(3, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount(playerA, DEEP_SIGHT, 0);
        Permanent tam = getPermanent(TAM, playerA);
        Assert.assertNotNull(tam);
        Assert.assertFalse(tam.isPrepared());
    }
}
