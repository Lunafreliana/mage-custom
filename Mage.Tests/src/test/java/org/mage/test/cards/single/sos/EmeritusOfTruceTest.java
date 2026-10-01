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
public class EmeritusOfTruceTest extends CardTestPlayerBase {

    private static final String EMERITUS = "Emeritus of Truce";
    private static final String PREPARE_SPELL = "Swords to Plowshares";

    @Test
    public void targetPlayerCreatesInklingAndCreatureBecomesPrepared() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, EMERITUS, 1);
        assertPermanentCount(playerB, "Inkling Token", 1);
        assertExileCount(playerA, PREPARE_SPELL, 1);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertTrue(emeritus.isPrepared());
    }

    @Test
    public void creatureCountIsCheckedAfterCreatingTheInkling() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        addTarget(playerA, playerA);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Inkling Token", 1);
        assertExileCount(playerA, PREPARE_SPELL, 0);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
    }

    @Test
    public void castsPreparedSwordsToPlowshares() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 4);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        addTarget(playerA, playerB);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + PREPARE_SPELL, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PREPARE_SPELL, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount(playerB, "Grizzly Bears", 1);
        assertLife(playerB, 22);
        assertExileCount(playerA, PREPARE_SPELL, 0);
        Permanent emeritus = getPermanent(EMERITUS, playerA);
        Assert.assertNotNull(emeritus);
        Assert.assertFalse(emeritus.isPrepared());
    }
}
