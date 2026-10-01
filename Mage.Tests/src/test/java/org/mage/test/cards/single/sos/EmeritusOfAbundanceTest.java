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
    public void entersPreparedCastsRegrowthAndPreparesAgainOnAttack() {
        // Use the set-qualified printing so this test also guards card-repository registration.
        addCard(Zone.HAND, playerA, "SOS-" + EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Lightning Bolt");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared Regrowth is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + REGROWTH, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Lightning Bolt");
        attack(3, playerA, EMERITUS, playerB);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Lightning Bolt", 1);
        assertExileCount(playerA, REGROWTH, 1);
        assertTapped(EMERITUS, false);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertTrue(emeritus.isPrepared());
    }

    @Test
    public void attackTriggerChecksLandCountAgainOnResolution() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Lightning Bolt");
        addCard(Zone.HAND, playerA, "Boomerang");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 6);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Lightning Bolt");
        attack(3, playerA, EMERITUS, playerB);
        // Remove the eighth land while the intervening-if attack trigger is on the stack.
        castSpell(3, PhaseStep.DECLARE_ATTACKERS, playerA, "Boomerang", "Forest");

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Forest", 5);
        assertExileCount(playerA, REGROWTH, 0);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
    }
}
