package org.mage.test.cards.single.fra;

import mage.abilities.keyword.FlyingAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class GeistOfSaintThaliaTest extends CardTestPlayerBase {

    @Test
    public void testReducesOnlyNoncreatureSpells() {
        addCard(Zone.BATTLEFIELD, playerA, "Geist of Saint Thalia");
        addCard(Zone.HAND, playerA, "Divination"); // {2}{U}
        addCard(Zone.HAND, playerA, "Wind Drake"); // {2}{U}
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);

        checkPlayableAbility("noncreature spell is reduced", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast Divination", true);
        checkPlayableAbility("creature spell is not reduced", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast Wind Drake", false);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, "Divination", 1);
        assertHandCount(playerA, 3); // Wind Drake and the two cards drawn
        assertTappedCount("Island", true, 2);
        assertPowerToughness(playerA, "Geist of Saint Thalia", 1, 2);
        assertAbility(playerA, "Geist of Saint Thalia", FlyingAbility.getInstance(), true);
    }
}
