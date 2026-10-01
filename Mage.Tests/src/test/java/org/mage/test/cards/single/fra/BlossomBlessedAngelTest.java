package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class BlossomBlessedAngelTest extends CardTestPlayerBase {

    private static final String ANGEL = "Blossom-Blessed Angel";
    private static final String PREPARE_SPELL = "Seed Suture";

    @Test
    public void castsSeedSutureWhilePrepared() {
        addCard(Zone.HAND, playerA, ANGEL);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ANGEL);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PREPARE_SPELL, ANGEL);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerA, 21);
        assertCounterCount(ANGEL, CounterType.P1P1, 1);
        assertPowerToughness(playerA, ANGEL, 3, 5);
        assertExileCount(playerA, PREPARE_SPELL, 0);
        Assert.assertFalse(getPermanent(ANGEL, playerA).isPrepared());
    }

    @Test
    public void fliesOverBlockerAndAttacksWithoutTapping() {
        addCard(Zone.BATTLEFIELD, playerA, ANGEL);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        attack(1, playerA, ANGEL, playerB);
        block(1, playerB, "Grizzly Bears", ANGEL);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertTapped(ANGEL, false);
        assertPowerToughness(playerA, ANGEL, 2, 4);
    }
}
