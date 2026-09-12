package org.mage.test.cards.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.common.PlaneswalkAwayFromSourceTriggeredAbility;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.RollPlanarDieEffect;
import mage.constants.Planes;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

/** Structural regressions for the Phase 3 migration of the original planes. */
public class ExistingPlaneMigrationTest {

    @Test
    public void testEveryRegisteredPlaneHasOneSemanticChaosAbilityAndNoRollEffect() {
        Arrays.stream(Planes.values()).forEach(planeType -> {
            Plane plane = Plane.createPlane(planeType);
            Assert.assertNotNull("Plane must be constructible: " + planeType, plane);
            long chaosAbilities = plane.getAbilities().stream()
                    .filter(ChaosEnsuesTriggeredAbility.class::isInstance)
                    .count();
            Assert.assertEquals("Plane must have exactly one chaos ability: " + planeType, 1, chaosAbilities);
            for (Ability ability : plane.getAbilities()) {
                Assert.assertFalse("Plane must not own a planar die roll effect: " + planeType,
                        ability.getEffects().stream().anyMatch(RollPlanarDieEffect.class::isInstance));
            }
        });
    }

    @Test
    public void testPlaneswalkToSourceMatchesObjectIdentity() {
        PlaneswalkToSourceTriggeredAbility ability
                = new PlaneswalkToSourceTriggeredAbility(new DrawCardSourceControllerEffect(1));
        UUID sourceId = UUID.randomUUID();
        UUID controllerId = UUID.randomUUID();
        ability.setSourceId(sourceId);
        ability.setControllerId(controllerId);

        Assert.assertTrue(ability.checkTrigger(
                new GameEvent(GameEvent.EventType.PLANESWALKED, sourceId, null, controllerId), null));
        Assert.assertFalse(ability.checkTrigger(
                new GameEvent(GameEvent.EventType.PLANESWALKED, UUID.randomUUID(), null, controllerId), null));
        Assert.assertEquals(controllerId, ability.copy().getControllerId());
    }

    @Test
    public void testPlaneswalkAwayFromSourceMatchesObjectIdentity() {
        PlaneswalkAwayFromSourceTriggeredAbility ability
                = new PlaneswalkAwayFromSourceTriggeredAbility(new DrawCardSourceControllerEffect(1));
        UUID sourceId = UUID.randomUUID();
        UUID controllerId = UUID.randomUUID();
        ability.setSourceId(sourceId);
        ability.setControllerId(controllerId);

        Assert.assertTrue(ability.checkTrigger(
                new GameEvent(GameEvent.EventType.PLANESWALKED_AWAY, sourceId, null, controllerId), null));
        Assert.assertFalse(ability.checkTrigger(
                new GameEvent(GameEvent.EventType.PLANESWALKED_AWAY, UUID.randomUUID(), null, controllerId), null));
        Assert.assertTrue(ability.isLeavesTheBattlefieldTrigger());
        Assert.assertEquals(controllerId, ability.copy().getControllerId());
    }
}
