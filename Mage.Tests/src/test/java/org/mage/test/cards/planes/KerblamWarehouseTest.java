package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleActivatedAbility;
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

public class KerblamWarehouseTest extends CardTestPlayerBase {

    @Test
    public void combatDamageCreatesOnlyOneTreasure() {
        addPlane(playerA, Planes.PLANE_KERBLAM_WAREHOUSE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");

        attack(1, playerA, "Grizzly Bears");
        attack(1, playerA, "Memnite");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Treasure Token", 1);
    }

    @Test
    public void chaosGrantsAbilityOnlyToNoncreatureArtifactsUntilYourNextTurn() {
        addPlane(playerA, Planes.PLANE_KERBLAM_WAREHOUSE);
        addCard(Zone.BATTLEFIELD, playerA, "Howling Mine");
        addCard(Zone.BATTLEFIELD, playerA, "Ornithopter");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        checkAbility("noncreature artifact gains the ability", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Howling Mine", SimpleActivatedAbility.class, true);
        checkAbility("artifact creature does not gain the ability", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Ornithopter", SimpleActivatedAbility.class, false);
        checkAbility("the ability expires as player A's next turn begins", 3, PhaseStep.UPKEEP,
                playerA, "Howling Mine", SimpleActivatedAbility.class, false);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.UPKEEP);
        execute();
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_KERBLAM_WAREHOUSE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Kerblam! Warehouse", metadata.getEnglishName());
        Assert.assertEquals("Plane - Kerblam! Warehouse", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
