package org.mage.test.cards.single.who;

import mage.abilities.keyword.WardAbility;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class PsychicPaperTest extends CardTestPlayerBase {

    private static final String PAPER = "Psychic Paper";

    @Test
    public void testEffectsAndNewChoicesWhenReequipped() {
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 4);
        addCard(Zone.BATTLEFIELD, playerA, PAPER);
        addCard(Zone.BATTLEFIELD, playerA, "Llanowar Elves");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerB, "Mountain");
        addCard(Zone.HAND, playerB, "Shock");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Equip", "Llanowar Elves");
        setChoice(playerA, "Grizzly Bears");
        setChoice(playerA, "Time Lord");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        checkAbility("ward is granted", 1, PhaseStep.BEGIN_COMBAT, playerA,
                "Grizzly Bears", WardAbility.class, true);
        castSpell(1, PhaseStep.BEGIN_COMBAT, playerB, "Shock", "Grizzly Bears");
        attack(1, playerA, "Grizzly Bears", playerB);
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Equip", "Memnite");
        setChoice(playerA, "Elite Vanguard");
        setChoice(playerA, "Doctor");
        checkAbility("ward is granted to the newly equipped creature", 1, PhaseStep.END_TURN,
                playerA, "Elite Vanguard", WardAbility.class, true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 19); // The renamed Llanowar Elves couldn't be blocked.
        assertGraveyardCount(playerB, "Shock", 1);
        assertPermanentCount(playerA, "Llanowar Elves", 1);
        assertNotSubtype("Llanowar Elves", SubType.TIME_LORD);
        assertPermanentCount(playerA, "Elite Vanguard", 1);
        assertSubtype("Elite Vanguard", SubType.DOCTOR);
        assertNotSubtype("Elite Vanguard", SubType.CONSTRUCT);
    }
}
