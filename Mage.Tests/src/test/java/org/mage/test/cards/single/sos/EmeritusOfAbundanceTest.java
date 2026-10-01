package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class EmeritusOfAbundanceTest extends CardTestPlayerBase {

    private static final String EMERITUS = "Emeritus of Abundance";
    private static final String REGROWTH = "Regrowth";
    private static final String SOS_EMERITUS = "SOS-" + EMERITUS;

    @Test
    public void entersPrepared() {
        // Use the set-qualified printing so this test also guards card-repository registration.
        addCard(Zone.HAND, playerA, SOS_EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, EMERITUS, 1);
        assertExileCount(playerA, REGROWTH, 1);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertTrue(emeritus.isPrepared());
    }

    @Test
    public void castsPreparedRegrowth() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + REGROWTH, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 0);
        assertExileCount(playerA, REGROWTH, 0);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
    }

    @Test
    public void attackWithEightLandsBecomesPreparedAndVigilanceKeepsItUntapped() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Grizzly Bears");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        attack(3, playerA, EMERITUS, playerB);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.DECLARE_BLOCKERS);
        execute();

        assertExileCount(playerA, REGROWTH, 1);
        assertTapped(EMERITUS, false);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertTrue(emeritus.isPrepared());
    }

    @Test
    public void attackWithOnlySevenLandsDoesNotBecomePrepared() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 7);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Grizzly Bears");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        attack(3, playerA, EMERITUS, playerB);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.DECLARE_BLOCKERS);
        execute();

        assertExileCount(playerA, REGROWTH, 0);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
    }
}
