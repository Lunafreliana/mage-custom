package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class SkycoachConductorTest extends CardTestPlayerBase {

    private static final String CONDUCTOR = "Skycoach Conductor";
    private static final String ALL_ABOARD = "All Aboard";

    @Test
    public void canBeCastWithFlashAndEntersPrepared() {
        addCard(Zone.HAND, playerA, CONDUCTOR);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, CONDUCTOR);
        checkPlayableAbility("prepared spell is available", 2, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Cast " + ALL_ABOARD, false); // No legal non-Pilot target

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, CONDUCTOR, 1);
        assertExileCount(playerA, ALL_ABOARD, 1);
    }

    @Test
    public void allAboardFlickersNonPilotCreature() {
        skipInitShuffling();
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.HAND, playerA, CONDUCTOR);
        addCard(Zone.BATTLEFIELD, playerA, "Wall of Omens");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, CONDUCTOR);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ALL_ABOARD, "Wall of Omens");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Wall of Omens", 1);
        assertHandCount(playerA, "Forest", 1);
        assertExileCount(playerA, ALL_ABOARD, 0);
    }
}
