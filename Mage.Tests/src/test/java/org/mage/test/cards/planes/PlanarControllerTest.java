package org.mage.test.cards.planes;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.GameState;
import mage.game.command.Plane;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Planechase phase 2 tests for the shared planar controller.
 */
public class PlanarControllerTest extends CardTestPlayerBase {

    @Test
    public void testControllerFollowsActivePlayer() {
        gameOptions.planeChase = true;

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPlanarController(playerB.getId());
    }

    @Test
    public void testControllerFollowsActivePlayerDuringExtraTurn() {
        gameOptions.planeChase = true;
        addCard(Zone.HAND, playerA, "Time Warp");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Time Warp", playerA);
        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertActivePlayer(playerA);
        assertPlanarController(playerA.getId());
    }

    @Test
    public void testChaosAbilityUsesCurrentPlanarController() {
        gameOptions.planeChase = true;
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerB, causeChaos, null, CardType.SORCERY);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Cause Chaos");
        setChoice(playerB, true);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 20);
        assertLife(playerB, 30);
        assertPlanarController(playerB.getId());
    }

    @Test
    public void testControllerStateIsCopied() {
        gameOptions.planeChase = true;

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        GameState copiedState = currentGame.getState().copy();
        Assert.assertEquals(playerB.getId(), copiedState.getPlanarControllerId());
        Plane copiedPlane = copiedState.getCurrentPlane();
        Assert.assertNotNull(copiedPlane);
        Assert.assertEquals(playerB.getId(), copiedPlane.getControllerId());
        long copiedPlaneTriggers = copiedState.getTriggers().values().stream()
                .filter(ability -> copiedPlane.getId().equals(ability.getSourceId()))
                .peek(ability -> Assert.assertEquals(playerB.getId(), ability.getControllerId()))
                .count();
        Assert.assertTrue("The copied plane must retain its registered triggers", copiedPlaneTriggers > 0);
    }

    private void assertPlanarController(UUID expectedControllerId) {
        Plane plane = currentGame.getState().getCurrentPlane();
        Assert.assertNotNull("Planechase must have a face-up plane", plane);
        Assert.assertEquals(expectedControllerId, currentGame.getPlanarControllerId(plane.getId()));
        Assert.assertEquals(expectedControllerId, currentGame.getControllerId(plane.getId()));
        Assert.assertEquals(expectedControllerId, currentGame.getOwnerId(plane));
        Assert.assertEquals(expectedControllerId, plane.getControllerId());
        for (Ability ability : plane.getAbilities()) {
            Assert.assertEquals(expectedControllerId, ability.getControllerId());
        }
    }
}
