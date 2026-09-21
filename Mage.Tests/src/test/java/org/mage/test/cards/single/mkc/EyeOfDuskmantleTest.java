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
    private static final String doomWhisperer = "Doom Whisperer";

    @Test
    public void testPlaySurveilledLandAndCastSpellForLife() {
        setStrictChooseMode(true);
        skipInitShuffling();

        addCard(Zone.BATTLEFIELD, playerA, eye);
        addCard(Zone.BATTLEFIELD, playerA, doomWhisperer);
        addCard(Zone.LIBRARY, playerA, "Swamp");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");

        activateAbility(1, PhaseStep.UPKEEP, playerA, "Pay 2 life: Surveil 2");
        addTarget(playerA, "Swamp^Grizzly Bears");
        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Swamp");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Swamp", 1);
        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertLife(playerA, 20 - 2 - 2);
    }

    @Test
    public void testOrdinaryGraveyardCardIsNotPlayable() {
        addCard(Zone.BATTLEFIELD, playerA, eye);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");

        checkPlayableAbility(
                "Card that wasn't surveilled can't be cast",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Grizzly Bears", false
        );

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();
    }

    @Test
    public void testPermissionExpiresAtEndOfTurn() {
        setStrictChooseMode(true);
        skipInitShuffling();

        addCard(Zone.BATTLEFIELD, playerA, eye);
        addCard(Zone.BATTLEFIELD, playerA, doomWhisperer);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");

        activateAbility(1, PhaseStep.UPKEEP, playerA, "Pay 2 life: Surveil 2");
        addTarget(playerA, "Grizzly Bears");
        checkPlayableAbility(
                "Card can be cast on the turn it was surveilled",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Grizzly Bears", true
        );
        checkPlayableAbility(
                "Card can't be cast on a later turn",
                3, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Grizzly Bears", false
        );

        setStopAt(3, PhaseStep.BEGIN_COMBAT);
        execute();
    }
}
