package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class SokenzanTest extends CardTestPlayerBase {

    @Test
    public void allCreaturesGetPlusOnePlusOneAndHaste() {
        addPlane(playerA, Planes.PLANE_SOKENZAN);
        addCard(Zone.HAND, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");
        attack(1, playerA, "Memnite");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertPowerToughness(playerA, "Memnite", 2, 2);
        assertPowerToughness(playerB, "Grizzly Bears", 3, 3);
    }

    @Test
    public void chaosUntapsAttackersAndAddsCombatAndMainPhase() {
        addPlane(playerA, Planes.PLANE_SOKENZAN);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        attack(1, playerA, "Grizzly Bears");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");
        attack(1, playerA, "Grizzly Bears");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 14);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_SOKENZAN)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Sokenzan", metadata.getEnglishName());
        Assert.assertEquals("Plane - Sokenzan", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
