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
    public void enteringCreatureUsesCurrentBattlefieldPermanentToFight() {
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

        assertPermanentCount(playerA, "Roaming Throne", 1);
        assertDamageReceived(playerA, "Roaming Throne", 2);
        assertGraveyardCount(playerB, "Atla Palani, Nest Tender", 1);
    }
}
