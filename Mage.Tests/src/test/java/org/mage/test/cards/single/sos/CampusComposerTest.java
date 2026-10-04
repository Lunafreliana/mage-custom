package org.mage.test.cards.single.sos;

import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.WardAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class CampusComposerTest extends CardTestPlayerBase {

    private static final String COMPOSER = "Campus Composer";
    private static final String ARIA = "Aqueous Aria";

    @Test
    public void testEntersPreparedAndCastsAqueousAria() {
        addCard(Zone.HAND, playerA, COMPOSER);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 9);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, COMPOSER);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + ARIA, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ARIA);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, COMPOSER, 1);
        assertPowerToughness(playerA, COMPOSER, 3, 4);
        assertAbility(playerA, COMPOSER, new WardAbility(new ManaCostsImpl<>("{2}"), false), true);
        assertPermanentCount(playerA, "Elemental Token", 1);
        assertPowerToughness(playerA, "Elemental Token", 3, 3);
        assertAbility(playerA, "Elemental Token", FlyingAbility.getInstance(), true);
        assertExileCount(playerA, ARIA, 0);
    }
}
