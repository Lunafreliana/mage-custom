package org.mage.test.cards.single.ncc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.b.BoxingRing Boxing Ring}
 *
 * @author JayDi85
 */
public class BoxingRingTest extends CardTestPlayerBase {

    @Test
    public void ordinaryCreatureFightsOnceAndSurvives() {
        addCard(Zone.BATTLEFIELD, playerA, "Boxing Ring");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.HAND, playerA, "Hill Giant"); // 3/3, mana value 4
        addCard(Zone.BATTLEFIELD, playerB, "Atla Palani, Nest Tender"); // 2/3, mana value 4

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Hill Giant");
        addTarget(playerA, "Atla Palani, Nest Tender");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Hill Giant", 1);
        assertDamageReceived(playerA, "Hill Giant", 2);
        assertGraveyardCount(playerA, "Hill Giant", 0);
        assertPermanentCount(playerB, "Atla Palani, Nest Tender", 0);
        assertGraveyardCount(playerB, "Atla Palani, Nest Tender", 1);
    }

    @Test
    public void ordinaryCreaturesDealLethalDamageToEachOther() {
        addCard(Zone.BATTLEFIELD, playerA, "Boxing Ring");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, "Grizzly Bears"); // 2/2, mana value 2
        addCard(Zone.BATTLEFIELD, playerB, "Silvercoat Lion"); // 2/2, mana value 2

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");
        addTarget(playerA, "Silvercoat Lion");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 0);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerB, "Silvercoat Lion", 0);
        assertGraveyardCount(playerB, "Silvercoat Lion", 1);
    }

    @Test
    public void roamingThroneFightsOnlyOnceAndSurvives() {
        addCard(Zone.BATTLEFIELD, playerA, "Boxing Ring");
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 4);
        addCard(Zone.HAND, playerA, "Roaming Throne"); // 4/4, mana value 4
        addCard(Zone.BATTLEFIELD, playerB, "Atla Palani, Nest Tender"); // 2/3, mana value 4

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Roaming Throne");
        setChoice(playerA, "Golem");
        addTarget(playerA, "Atla Palani, Nest Tender");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // Boxing Ring is an artifact, not a creature, so Roaming Throne must not
        // cause its triggered ability to trigger an additional time. A second
        // fight would give Roaming Throne lethal damage.
        assertPermanentCount(playerA, "Roaming Throne", 1);
        assertDamageReceived(playerA, "Roaming Throne", 2);
        assertGraveyardCount(playerA, "Roaming Throne", 0);
        assertPermanentCount(playerB, "Atla Palani, Nest Tender", 0);
        assertGraveyardCount(playerB, "Atla Palani, Nest Tender", 1);
    }
}
