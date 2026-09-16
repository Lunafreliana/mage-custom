package org.mage.test.cards.single.mbc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.m.MassimoTheMagician Massimo, the Magician}
 *
 * @author muz
 */
public class MassimoTheMagicianTest extends CardTestPlayerBase {

    @Test
    public void testExilesAndCastsCopyOnCombatDamage() {
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Volcanic Island");
        addCard(Zone.HAND, playerA, "Massimo, the Magician");
        addCard(Zone.GRAVEYARD, playerA, "Lightning Bolt");

        setStrictChooseMode(true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Massimo, the Magician");
        addTarget(playerA, "Lightning Bolt");
        attack(3, playerA, "Massimo, the Magician", playerB);
        setChoice(playerA, true);
        addTarget(playerA, playerB);

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount("Lightning Bolt", 1);
        assertLife(playerB, 14);
    }

    @Test
    public void testNoTargetDoesNotGrantAbility() {
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Volcanic Island");
        addCard(Zone.HAND, playerA, "Massimo, the Magician");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Massimo, the Magician");
        attack(3, playerA, "Massimo, the Magician", playerB);

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 17);
    }
}
