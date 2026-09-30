package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/** Focused behavior tests for Stensia. */
public class StensiaTest extends CardTestPlayerBase {

    @Test
    public void creatureGetsOnlyOneCounterForItsFirstDamageEachTurn() {
        addPlane(playerA, Planes.PLANE_STENSIA);
        addCard(Zone.BATTLEFIELD, playerA, "Fencing Ace");

        attack(1, playerA, "Fencing Ace", playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Fencing Ace", CounterType.P1P1, 1);
        assertLife(playerB, 17); // 1 first-strike damage, then 2 regular damage
    }

    @Test
    public void everyPlayersCreatureCanTriggerStensia() {
        addPlane(playerA, Planes.PLANE_STENSIA);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        attack(2, playerB, "Grizzly Bears", playerA);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerB, "Grizzly Bears", CounterType.P1P1, 1);
    }

    @Test
    public void chaosGrantsPingAbilityOnlyToCurrentControlledCreatures() {
        addPlane(playerA, Planes.PLANE_STENSIA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.HAND, playerA, "Raise the Alarm");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Raise the Alarm");
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                "{T}: This creature deals 1 damage to target player or planeswalker", playerB);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 19);
        assertTapped("Grizzly Bears", true);
        assertTapped("Hill Giant", false);
        assertTapped("Soldier Token", false);
    }

    @Test
    public void registryExposesStensiaMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_STENSIA);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Stensia", metadata.getEnglishName());
        Assert.assertEquals("Plane - Stensia", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
