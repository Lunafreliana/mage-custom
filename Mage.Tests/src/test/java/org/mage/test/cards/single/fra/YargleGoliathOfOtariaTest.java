package org.mage.test.cards.single.fra;

import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class YargleGoliathOfOtariaTest extends CardTestPlayerBase {

    private static final String yargle = "Yargle, Goliath of Otaria";

    @Test
    public void testCharacteristics() {
        addCard(Zone.HAND, playerA, yargle);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, yargle);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, yargle, 1);
        assertPowerToughness(playerA, yargle, 3, 9);
        assertType(yargle, CardType.CREATURE, SubType.FROG);
        assertSubtype(yargle, SubType.SPIRIT);
    }
}
