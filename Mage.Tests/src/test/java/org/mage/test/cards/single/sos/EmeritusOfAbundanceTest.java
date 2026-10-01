package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class EmeritusOfAbundanceTest extends CardTestPlayerBase {

    private static final String EMERITUS = "Emeritus of Abundance";
    private static final String PREPARE_SPELL = "Regrowth";
    private static final String SOS_EMERITUS = "SOS-" + EMERITUS;

    @Test
    public void entersPreparedAndCastsRegrowth() {
        // Use the set-qualified printing so this test also guards card-repository registration.
        addCard(Zone.HAND, playerA, SOS_EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared Regrowth is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + PREPARE_SPELL, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PREPARE_SPELL, "Memnite");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, EMERITUS, 1);
        assertPowerToughness(playerA, EMERITUS, 3, 4);
        assertHandCount(playerA, "Memnite", 1);
        assertGraveyardCount(playerA, "Memnite", 0);
        assertExileCount(playerA, PREPARE_SPELL, 0);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
    }

    @Test
    public void attackWithEightLandsPreparesEmeritusAgain() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PREPARE_SPELL, "Memnite");
        attack(3, playerA, EMERITUS, playerB);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertTrue(emeritus.isPrepared());
        Assert.assertFalse(emeritus.isTapped());
        assertExileCount(playerA, PREPARE_SPELL, 1);
    }

    @Test
    public void attackWithSevenLandsDoesNotPrepareEmeritusAgain() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 7);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PREPARE_SPELL, "Memnite");
        attack(3, playerA, EMERITUS, playerB);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
        Assert.assertFalse(emeritus.isTapped());
        assertExileCount(playerA, PREPARE_SPELL, 0);
    }
}
