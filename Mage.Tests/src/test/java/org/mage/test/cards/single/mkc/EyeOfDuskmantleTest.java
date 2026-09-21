package org.mage.test.cards.single.mkc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author OpenAI
 */
public class EyeOfDuskmantleTest extends CardTestPlayerBase {

    private static final String eye = "Eye of Duskmantle";
    private static final String gaze = "Otherworldly Gaze";
    private static final String bears = "Grizzly Bears";

    @Test
    public void canCastCardSurveilledBeforeEyeEntered() {
        setStrictChooseMode(true);
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerA, bears);
        addCard(Zone.HAND, playerA, gaze);
        addCard(Zone.HAND, playerA, eye);
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        // Leave one mana beyond the exact sequence so automatic mana selection
        // cannot make this history-tracking test depend on payment ordering.
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 8);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, gaze);
        addTarget(playerA, bears);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, eye);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, bears);
        setChoice(playerA, "Cast with alternative cost: Pay 2 life");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, eye, 1);
        assertPermanentCount(playerA, bears, 1);
        assertLife(playerA, 18);
    }

    @Test
    public void canPlayLandPutIntoGraveyardWhileSurveilling() {
        setStrictChooseMode(true);
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerA, "Swamp");
        addCard(Zone.HAND, playerA, gaze);
        addCard(Zone.BATTLEFIELD, playerA, eye);
        addCard(Zone.BATTLEFIELD, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, gaze);
        addTarget(playerA, "Swamp");
        playLand(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Swamp");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Swamp", 1);
        assertGraveyardCount(playerA, "Swamp", 0);
    }
}
