package org.mage.test.cards.single.msc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class HeartShapedHerbTest extends CardTestPlayerBase {

    @Test
    public void preventsOnlyOpponentDamage() {
        addCard(Zone.BATTLEFIELD, playerA, "Heart-Shaped Herb");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.HAND, playerA, "Shock");
        addCard(Zone.BATTLEFIELD, playerB, "Mountain", 2);
        addCard(Zone.HAND, playerB, "Shock");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Shock", playerA);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Shock", playerA);

        setStopAt(2, PhaseStep.END_TURN);
        execute();

        assertLife(playerA, 17);
    }

    @Test
    public void sacrificesAndReturnsCreatureWithCounters() {
        addCard(Zone.BATTLEFIELD, playerA, "Heart-Shaped Herb");
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{2}, {T}, Sacrifice this artifact");
        setChoice(playerA, true);
        setChoice(playerA, "Silvercoat Lion");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Heart-Shaped Herb", 0);
        assertPermanentCount(playerA, "Silvercoat Lion", 1);
        assertPowerToughness(playerA, "Silvercoat Lion", 5, 5);
        Assert.assertEquals("Player A should be the monarch", playerA.getId(), currentGame.getMonarchId());
    }

    @Test
    public void mayDeclineSacrifice() {
        addCard(Zone.BATTLEFIELD, playerA, "Heart-Shaped Herb");
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{2}, {T}, Sacrifice this artifact");
        setChoice(playerA, false);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Heart-Shaped Herb", 0);
        assertPermanentCount(playerA, "Silvercoat Lion", 1);
        Assert.assertNull("There should be no monarch", currentGame.getMonarchId());
    }
}
