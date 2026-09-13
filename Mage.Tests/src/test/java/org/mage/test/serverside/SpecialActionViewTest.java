package org.mage.test.serverside;

import mage.abilities.SpecialAction;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.keyword.ConvokeAbility;
import mage.abilities.special.RollPlanarDieSpecialAction;
import mage.cards.Card;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;
import mage.view.GameView;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class SpecialActionViewTest extends CardTestPlayerBase {

    @Test
    public void testPlanarRollVisibilityFollowsTimingAndPriority() {
        preparePlanechase();

        runCode("upkeep", 1, PhaseStep.UPKEEP, playerA,
                (info, player, game) -> assertRollVisibility(info, player, game, false));
        runCode("first main phase", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            assertRollVisibility(info, player, game, true);
            Assert.assertFalse("The other player has no priority", getGameView(playerB).getSpecial());
            Assert.assertFalse("Spectators have no special actions", getGameView(null).getSpecial());
        });
        runCode("combat", 1, PhaseStep.BEGIN_COMBAT, playerA,
                (info, player, game) -> assertRollVisibility(info, player, game, false));
        runCode("second main phase", 1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                (info, player, game) -> assertRollVisibility(info, player, game, true));
        runCode("end step", 1, PhaseStep.END_TURN, playerA,
                (info, player, game) -> assertRollVisibility(info, player, game, false));
        runCode("opponent's main phase", 2, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> assertRollVisibility(info, player, game, false));

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testPlanarRollIsHiddenWhileASpellIsPending() {
        preparePlanechase();
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");
        addCard(Zone.HAND, playerA, "Lightning Bolt");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        runCode("spell on stack", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertFalse(info, game.getStack().isEmpty());
            assertRollVisibility(info, player, game, false);
        });
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        runCode("stack empty again", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertTrue(info, game.getStack().isEmpty());
            assertRollVisibility(info, player, game, true);
        });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
        assertLife(playerB, 17);
    }

    @Test
    public void testAnotherAvailableActionKeepsTheButtonVisibleDuringCombat() {
        preparePlanechase();
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, "Channel");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Channel");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        runCode("Channel during combat", 1, PhaseStep.BEGIN_COMBAT, playerA, (info, player, game) -> {
            Map<UUID, SpecialAction> registered = game.getState().getSpecialActions()
                    .getControlledBy(player.getId(), false);
            Assert.assertEquals(info, 2, registered.size());

            Map<UUID, SpecialAction> choices = game.getState().getSpecialActions()
                    .getAvailableActions(player.getId(), false, game);
            Assert.assertEquals(info, 1, choices.size());
            SpecialAction choice = choices.values().iterator().next();
            Assert.assertFalse(info, choice instanceof RollPlanarDieSpecialAction);
            Assert.assertEquals(info, "Channel", game.getCard(choice.getSourceId()).getName());
            Assert.assertTrue(info, getGameView(player).getSpecial());
        });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testManaPaymentActionsRemainAvailableWithoutOfferingAPlanarRoll() {
        preparePlanechase();
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        addCard(Zone.HAND, playerA, "Conclave Tribunal");

        runCode("convoke payment view", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            // Build a payment snapshot without starting an interactive payment in the test runner.
            Game paymentGame = game.copy();
            Player payingPlayer = paymentGame.getPlayer(player.getId());
            Card spell = payingPlayer.getHand().getCards(paymentGame).stream()
                    .filter(card -> card.getName().equals("Conclave Tribunal"))
                    .findFirst().orElse(null);
            Assert.assertNotNull(info, spell);
            new ConvokeAbility().addSpecialAction(spell.getSpellAbility(), paymentGame, new GenericManaCost(1));
            payingPlayer.setPayManaMode(true);

            Map<UUID, SpecialAction> choices = paymentGame.getState().getSpecialActions()
                    .getAvailableActions(payingPlayer.getId(), true, paymentGame);
            Assert.assertEquals(info, 1, choices.size());
            Assert.assertTrue(info, choices.values().iterator().next().isManaAction());
            Assert.assertFalse(info, choices.values().stream().anyMatch(RollPlanarDieSpecialAction.class::isInstance));
            Assert.assertTrue(info, new GameView(paymentGame.getState(), paymentGame, payingPlayer.getId(), null).getSpecial());
            Assert.assertTrue("Original game's roll remains available", getGameView(player).getSpecial());
        });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    private void preparePlanechase() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_HEDRON_FIELDS_OF_AGADEEM);
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
    }

    private void assertRollVisibility(String info, Player player, Game game, boolean expected) {
        Map<UUID, SpecialAction> registered = game.getState().getSpecialActions()
                .getControlledBy(player.getId(), false);
        Assert.assertEquals("The planar action stays registered: " + info, 1, registered.size());
        Assert.assertTrue(info, registered.values().iterator().next() instanceof RollPlanarDieSpecialAction);

        Map<UUID, SpecialAction> choices = game.getState().getSpecialActions()
                .getAvailableActions(player.getId(), false, game);
        Assert.assertEquals("Special-action menu: " + info, expected, !choices.isEmpty());
        // The existing helper uses GameSessionPlayer's copied-game view path.
        Assert.assertEquals("Special button: " + info, expected, getGameView(player).getSpecial());
    }
}
