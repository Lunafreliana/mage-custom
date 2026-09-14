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
    public void chaosGrantsSacrificeAndFlipAbilityToNoncreatureArtifacts() {
        addPlane(playerA, Planes.PLANE_KERBLAM_WAREHOUSE);
        addCard(Zone.BATTLEFIELD, playerA, "Sol Ring");
        addCard(Zone.BATTLEFIELD, playerA, "Ornithopter");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        // The granted ability is not available until both the spell and the
        // resulting chaos trigger have finished resolving.
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, playerA);
        // Ability commands match from the beginning of the generated rule.
        // SacrificeSourceCost renders {this} as the actual source name.
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA,
                "{T}, sacrifice Sol Ring: Flip a coin", playerB);
        setFlipCoinResult(playerA, true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Sol Ring", 0);
        assertPermanentCount(playerA, "Ornithopter", 1);
        assertLife(playerB, 17);
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
