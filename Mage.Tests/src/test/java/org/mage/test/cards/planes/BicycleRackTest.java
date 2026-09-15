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

public class BicycleRackTest extends CardTestPlayerBase {

    @Test
    public void grantsCyclingToEachPlayersHand() {
        addPlane(playerA, Planes.PLANE_BICYCLE_RACK);
        removeAllCardsFromHand(playerA);
        removeAllCardsFromHand(playerB);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerB, "Glory Seeker");
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Wastes", 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cycling {2}");
        activateAbility(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Cycling {2}");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Glory Seeker", 1);
    }

    @Test
    public void chaosBoostUsesOnlyItsControllersDiscards() {
        addPlane(playerA, Planes.PLANE_BICYCLE_RACK);
        removeAllCardsFromHand(playerA);
        removeAllCardsFromHand(playerB);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerB, "Glory Seeker");
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Wastes", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerB, "Silvercoat Lion");
        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cycling {2}");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Cycling {2}");
        runCode("verify controller-specific boost", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, (info, player, game) -> {
                    assertPowerToughness(playerA, "Runeclaw Bear", 3, 3);
                    assertPowerToughness(playerB, "Silvercoat Lion", 2, 2);
                });

        setStopAt(3, PhaseStep.UPKEEP);
        execute();

        assertPowerToughness(playerA, "Runeclaw Bear", 2, 2);
    }

    @Test
    public void isAvailableThroughPlanarCardRegistry() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_BICYCLE_RACK);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Bicycle Rack", metadata.getEnglishName());
        Assert.assertEquals("Plane - Bicycle Rack", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private static SpellAbility createCauseChaosAbility() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        return ability;
    }
}
