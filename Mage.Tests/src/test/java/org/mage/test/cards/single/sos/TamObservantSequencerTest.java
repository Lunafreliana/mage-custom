package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class TamObservantSequencerTest extends CardTestPlayerBase {

    private static final String TAM = "Tam, Observant Sequencer";
    private static final String DEEP_SIGHT = "Deep Sight";

    @Test
    public void landfallPreparesTamAndDeepSightDrawsAndGainsLife() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Darksteel Relic");
        // The set-qualified name also verifies that the SOS printing is enabled.
        addCard(Zone.BATTLEFIELD, playerA, "SOS-" + TAM);
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island", 2);
        addCard(Zone.HAND, playerA, "Forest");

        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Forest");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("landfall made the prepare spell castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + DEEP_SIGHT, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, DEEP_SIGHT);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerA, "Darksteel Relic", 1);
        assertLife(playerA, 21);
        assertExileCount(playerA, DEEP_SIGHT, 0);
        Permanent tam = getPermanent(TAM, playerA);
        Assert.assertNotNull(tam);
        Assert.assertFalse(tam.isPrepared());
    }

    @Test
    public void opponentsLandDoesNotPrepareTam() {
        addCard(Zone.BATTLEFIELD, playerA, TAM);
        addCard(Zone.HAND, playerB, "Forest");

        playLand(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Forest");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount(playerA, DEEP_SIGHT, 0);
        Permanent tam = getPermanent(TAM, playerA);
        Assert.assertNotNull(tam);
        Assert.assertFalse(tam.isPrepared());
    }
}
