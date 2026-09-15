package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.abilities.keyword.HasteAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class BloodhillBastionTest extends CardTestPlayerBase {

    @Test
    public void enteringCreatureGainsDoubleStrikeAndHaste() {
        addPlane(playerA, Planes.PLANE_BLOODHILL_BASTION);
        addCard(Zone.HAND, playerA, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");
        checkAbility("Memnite has double strike", 1, PhaseStep.BEGIN_COMBAT,
                playerA, "Memnite", DoubleStrikeAbility.class, true);
        checkAbility("Memnite has haste", 1, PhaseStep.BEGIN_COMBAT,
                playerA, "Memnite", HasteAbility.class, true);
        attack(1, playerA, "Memnite");
        checkAbility("double strike expires", 2, PhaseStep.UPKEEP,
                playerA, "Memnite", DoubleStrikeAbility.class, false);
        checkAbility("haste expires", 2, PhaseStep.UPKEEP,
                playerA, "Memnite", HasteAbility.class, false);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertLife(playerB, 18);
    }

    @Test
    public void enteringCreatureControlledByPlanarControllerGainsAbilities() {
        addPlane(playerA, Planes.PLANE_BLOODHILL_BASTION);
        addCard(Zone.HAND, playerB, "Memnite");

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Memnite");
        attack(2, playerB, "Memnite");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 18);
    }

    @Test
    public void chaosBlinksNontokenCreatureYouControl() {
        addPlane(playerA, Planes.PLANE_BLOODHILL_BASTION);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears", CounterType.P1P1, 1);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertCounterCount("Grizzly Bears", CounterType.P1P1, 0);
    }

    @Test
    public void registryExposesBloodhillBastionMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_BLOODHILL_BASTION);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Bloodhill Bastion", metadata.getEnglishName());
        Assert.assertEquals("Plane - Bloodhill Bastion", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
