package org.mage.test.cards.single.ecl;

import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.abilities.mana.GreenManaAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class AbigaleEloquentFirstYearTest extends CardTestPlayerBase {

    private static final String ABIGALE = "Abigale, Eloquent First-Year";
    private static final String TARGET = "Llanowar Elves";

    @Test
    public void testGrantedCounterAbilitiesSurviveLosingAllAbilities() {
        addCard(Zone.HAND, playerA, ABIGALE);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);
        addCard(Zone.BATTLEFIELD, playerA, TARGET);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ABIGALE);
        addTarget(playerA, TARGET);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertAbility(playerA, TARGET, new GreenManaAbility(), false);
        assertCounterCount(playerA, TARGET, CounterType.FLYING, 1);
        assertCounterCount(playerA, TARGET, CounterType.FIRST_STRIKE, 1);
        assertCounterCount(playerA, TARGET, CounterType.LIFELINK, 1);
        assertAbility(playerA, TARGET, FlyingAbility.getInstance(), true);
        assertAbility(playerA, TARGET, FirstStrikeAbility.getInstance(), true);
        assertAbility(playerA, TARGET, LifelinkAbility.getInstance(), true);
    }
}
