package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class EmeritusOfAbundanceTest extends CardTestPlayerBase {

    private static final String EMERITUS = "Emeritus of Abundance";
    private static final String REGROWTH = "Regrowth";

    @Test
    public void entersPreparedAndCastsRegrowth() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Lightning Bolt");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("Regrowth copy is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + REGROWTH, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Lightning Bolt");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        setStrictChooseMode(true);
        execute();

        assertHandCount(playerA, "Lightning Bolt", 1);
        assertExileCount(playerA, REGROWTH, 0);
        Permanent permanent = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(permanent);
        Assert.assertFalse(permanent.isPrepared());
    }

    @Test
    public void attackWithEightLandsBecomesPrepared() {
        addCard(Zone.BATTLEFIELD, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);

        attack(1, playerA, EMERITUS);

        setStopAt(1, PhaseStep.DECLARE_BLOCKERS);
        setStrictChooseMode(true);
        execute();

        assertExileCount(playerA, REGROWTH, 1);
        Permanent permanent = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(permanent);
        Assert.assertTrue(permanent.isPrepared());
    }

    @Test
    public void attackTriggerRechecksLandCountOnResolution() {
        addCard(Zone.BATTLEFIELD, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);
        addCard(Zone.HAND, playerB, "Boomerang");
        addCard(Zone.BATTLEFIELD, playerB, "Island", 2);

        attack(1, playerA, EMERITUS);
        castSpell(1, PhaseStep.DECLARE_ATTACKERS, playerB, "Boomerang", "Forest");

        setStopAt(1, PhaseStep.DECLARE_BLOCKERS);
        setStrictChooseMode(true);
        execute();

        assertPermanentCount(playerA, "Forest", 7);
        assertExileCount(playerA, REGROWTH, 0);
        Permanent permanent = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(permanent);
        Assert.assertFalse(permanent.isPrepared());
    }
}
