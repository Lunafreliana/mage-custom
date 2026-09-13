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

/**
 * Tests Velis Vel in the common Planechase runtime.
 */
public class VelisVelTest extends CardTestPlayerBase {

    @Test
    public void boostsCreaturesForEachOtherCreatureSharingAType() {
        addPlane(playerA, Planes.PLANE_VELIS_VEL);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, "Grizzly Bears", 3, 3);
        assertPowerToughness(playerB, "Balduvian Bears", 3, 3);
        assertPowerToughness(playerB, "Glory Seeker", 2, 2);
    }

    @Test
    public void chaosGrantsAllTypesUntilEndOfTurn() {
        addPlane(playerA, Planes.PLANE_VELIS_VEL);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");
        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Glory Seeker");
        runCode("all types create a shared creature type", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, (info, player, game) -> {
                    assertPowerToughness(playerA, "Grizzly Bears", 3, 3);
                    assertPowerToughness(playerB, "Glory Seeker", 3, 3);
                });

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPowerToughness(playerA, "Grizzly Bears", 2, 2);
        assertPowerToughness(playerB, "Glory Seeker", 2, 2);
    }

    @Test
    public void isAvailableThroughPlanarCardRegistry() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_VELIS_VEL);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Velis Vel", metadata.getEnglishName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private static SpellAbility createCauseChaosAbility() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        return ability;
    }
}
