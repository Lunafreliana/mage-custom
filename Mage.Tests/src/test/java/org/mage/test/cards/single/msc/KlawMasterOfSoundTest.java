package org.mage.test.cards.single.msc;

import mage.abilities.keyword.IndestructibleAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.k.KlawMasterOfSound Klaw, Master of Sound}
 *
 * @author Susucr
 */
public class KlawMasterOfSoundTest extends CardTestPlayerBase {

    @Test
    public void testExilesAndPlaysLand() {
        addCard(Zone.BATTLEFIELD, playerA, "Klaw, Master of Sound");
        addCard(Zone.LIBRARY, playerB, "Plains");

        attack(1, playerA, "Klaw, Master of Sound");
        playLand(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Plains");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 17);
        assertPermanentCount(playerA, "Plains", 1);
        assertExileCount("Plains", 0);
        assertAbility(playerA, "Klaw, Master of Sound", IndestructibleAbility.getInstance(), true);
    }

    @Test
    public void testAnyManaTypeCanCastExiledSpell() {
        addCard(Zone.BATTLEFIELD, playerA, "Klaw, Master of Sound");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);
        addCard(Zone.LIBRARY, playerB, "Savannah Lions");

        attack(1, playerA, "Klaw, Master of Sound");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Savannah Lions");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Savannah Lions", 1);
        assertAbility(playerA, "Klaw, Master of Sound", IndestructibleAbility.getInstance(), true);
    }

    @Test
    public void testCardPlayedFromHandDoesNotGrantIndestructible() {
        addCard(Zone.BATTLEFIELD, playerA, "Klaw, Master of Sound");
        addCard(Zone.HAND, playerA, "Ornithopter");
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 3);
        addCard(Zone.HAND, playerB, "Murder");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Ornithopter");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Murder", "Klaw, Master of Sound");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, "Klaw, Master of Sound", 1);
    }
}
