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

public class BesiegedVikingVillageTest extends CardTestPlayerBase {

    @Test
    public void allCreaturesGainBoast() {
        addPlane(playerA, Planes.PLANE_BESIEGED_VIKING_VILLAGE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Wastes");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerB, "Wastes");

        attack(1, playerA, "Grizzly Bears");
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Boast");
        attack(2, playerB, "Hill Giant");
        activateAbility(2, PhaseStep.POSTCOMBAT_MAIN, playerB, "Boast");

        setStopAt(2, PhaseStep.END_TURN);
        execute();

        assertCounterCount("Grizzly Bears", CounterType.P1P1, 1);
        assertCounterCount("Hill Giant", CounterType.P1P1, 1);
    }

    @Test
    public void chaosCanTargetOnlyControlledCreatureThatAttackedThisTurn() {
        addPlane(playerA, Planes.PLANE_BESIEGED_VIKING_VILLAGE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        attack(1, playerA, "Grizzly Bears");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertCounterCount("Grizzly Bears", CounterType.INDESTRUCTIBLE, 1);
        assertCounterCount("Memnite", CounterType.INDESTRUCTIBLE, 0);
        assertCounterCount("Hill Giant", CounterType.INDESTRUCTIBLE, 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_BESIEGED_VIKING_VILLAGE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Besieged Viking Village", metadata.getEnglishName());
        Assert.assertEquals("Plane - Besieged Viking Village", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
