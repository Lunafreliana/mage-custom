package org.mage.test.cards.single.sos;

import mage.abilities.keyword.DeathtouchAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class VastlandsScavengerTest extends CardTestPlayerBase {

    private static final String SCAVENGER = "Vastlands Scavenger";
    private static final String BIND_TO_LIFE = "Bind to Life";

    @Test
    public void testEntersPreparedWithDeathtouch() {
        // Use the set-qualified printing so this test also guards card-repository registration.
        addCard(Zone.HAND, playerA, "SOS-" + SCAVENGER);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, SCAVENGER);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, SCAVENGER, 1);
        assertPowerToughness(playerA, SCAVENGER, 4, 4);
        assertAbility(playerA, SCAVENGER, DeathtouchAbility.getInstance(), true);
        assertExileCount(playerA, BIND_TO_LIFE, 1);
        Permanent scavenger = getPermanent(SCAVENGER, playerA);
        Assert.assertNotNull(scavenger);
        Assert.assertTrue(scavenger.isPrepared());
    }

    @Test
    public void testBindToLifeMillsSevenAndReturnsChosenCreature() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Silvercoat Lion");
        addCard(Zone.LIBRARY, playerA, "Plains", 5);
        addCard(Zone.HAND, playerA, SCAVENGER);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, SCAVENGER);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + BIND_TO_LIFE, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, BIND_TO_LIFE);
        setChoice(playerA, "Silvercoat Lion");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, SCAVENGER, 1);
        assertPermanentCount(playerA, "Silvercoat Lion", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Plains", 5);
        assertLibraryCount(playerA, 0);
        assertExileCount(playerA, BIND_TO_LIFE, 0);
    }
}
