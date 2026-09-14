package org.mage.test.cards.planes;

import mage.abilities.Ability;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import mage.view.CardsView;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AgyremEndStepInteractionTest extends CardTestPlayerBase {

    @Test
    public void twoAgyremDelayedTriggersSurvivePlaneswalkAlongsideCaetusAndOcelotPride() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(Planes.PLANE_AGYREM, Planes.PLANE_AKOUM);
        AtomicReference<UUID> agyremId = new AtomicReference<>();

        addCard(Zone.BATTLEFIELD, playerA, "Invasion of Segovia");
        addCard(Zone.BATTLEFIELD, playerA, "Ocelot Pride");
        addCard(Zone.BATTLEFIELD, playerA, "Savannah Lions");
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");

        runCode("transform Caetus and kill two white creatures under Agyrem",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
                    Assert.assertEquals(info, Planes.PLANE_AGYREM,
                            game.getState().getFaceUpPlanes().get(0).getPlaneType());
                    agyremId.set(game.getState().getFaceUpPlanes().get(0).getId());

                    Permanent invasion = game.getBattlefield().getAllActivePermanents(playerA.getId()).stream()
                            .filter(permanent -> permanent.getName().equals("Invasion of Segovia"))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError(info + ": Invasion of Segovia not found"));
                    Assert.assertTrue(info + ": battle must transform into Caetus",
                            invasion.transform(null, game, true));
                    game.applyEffects();
                    Assert.assertTrue(info + ": transformed battle must be Caetus",
                            game.getBattlefield().getAllActivePermanents(playerA.getId()).stream()
                                    .anyMatch(permanent -> permanent.getName().equals("Caetus, Sea Tyrant of Segovia")));

                    Permanent savannah = game.getBattlefield().getAllActivePermanents(playerA.getId()).stream()
                            .filter(permanent -> permanent.getName().equals("Savannah Lions"))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError(info + ": Savannah Lions not found"));
                    Permanent silvercoat = game.getBattlefield().getAllActivePermanents(playerA.getId()).stream()
                            .filter(permanent -> permanent.getName().equals("Silvercoat Lion"))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError(info + ": Silvercoat Lion not found"));

                    Assert.assertTrue(info + ": Savannah Lions must die", savannah.sacrifice(null, game));
                    Assert.assertTrue(info + ": Silvercoat Lion must die", silvercoat.sacrifice(null, game));
                });

        checkGraveyardCount("both white creatures died", 1, PhaseStep.BEGIN_COMBAT,
                playerA, "Savannah Lions", 1);
        checkGraveyardCount("both white creatures died", 1, PhaseStep.BEGIN_COMBAT,
                playerA, "Silvercoat Lion", 1);

        // Ocelot Pride has lifelink, so this satisfies its end-step intervening-if condition.
        attack(1, playerA, "Ocelot Pride", playerB);

        runCode("planeswalk away after both Agyrem dies triggers resolved",
                1, PhaseStep.POSTCOMBAT_MAIN, playerA, (info, player, game) -> {
                    Assert.assertTrue(info + ": stack should be clear before planeswalking", game.getStack().isEmpty());
                    Assert.assertEquals(info, Planes.PLANE_AGYREM,
                            game.getState().getFaceUpPlanes().get(0).getPlaneType());
                    Assert.assertTrue(info + ": planeswalk must succeed", game.planeswalk(playerA.getId()));
                    Assert.assertEquals(info, Planes.PLANE_AKOUM,
                            game.getState().getFaceUpPlanes().get(0).getPlaneType());

                    // Agyrem is no longer face up, but delayed triggers still retain its source UUID.
                    // That source must remain resolvable while Agyrem is back in the planar deck.
                    Assert.assertNotNull(info + ": departed Agyrem must remain resolvable as a delayed-trigger source",
                            game.getObject(agyremId.get()));
                });

        // Caetus has one chosen target and then stops selecting more of its "up to four" targets.
        addTarget(playerA, "Ocelot Pride");
        addTarget(playerA, TestPlayer.TARGET_SKIP);

        // At the beginning of the end step the expected simultaneous triggers are exactly:
        // Caetus + Ocelot Pride + two independent delayed Agyrem triggers.
        runCode("all four end-step triggers reach the trigger-order view",
                1, PhaseStep.END_TURN, playerA, (info, player, game) -> {
                    Assert.assertEquals(info, 4, game.getStack().size());

                    List<Ability> triggerChoices = game.getStack().stream()
                            .map(stackObject -> stackObject.getStackAbility())
                            .collect(Collectors.toList());
                    Assert.assertEquals(info + ": two pending abilities must still point at Agyrem",
                            2L, triggerChoices.stream()
                                    .filter(ability -> agyremId.get().equals(ability.getSourceId()))
                                    .count());

                    // HumanPlayer serializes simultaneous trigger choices through CardsView before
                    // asking the client to order them. Previously CardsView silently dropped the two
                    // Agyrem abilities because game.getObject(AgyremId) returned null after the Plane
                    // had been put back into the planar deck. The server still waited for four choices,
                    // leaving the client stuck forever in the trigger-order dialog.
                    CardsView cardsView = new CardsView(triggerChoices, game);
                    Assert.assertEquals(info + ": trigger-order UI must receive every server-side trigger",
                            triggerChoices.size(), cardsView.size());
                });

        // Cleanup normally has no priority, so inspect the resolved state on the
        // following turn instead of scheduling test commands in cleanup.
        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Savannah Lions", 1);
        assertPermanentCount(playerA, "Silvercoat Lion", 1);
        assertGraveyardCount(playerA, "Savannah Lions", 0);
        assertGraveyardCount(playerA, "Silvercoat Lion", 0);
        assertPermanentCount(playerA, "Cat Token", 1);

        Permanent ocelot = currentGame.getBattlefield().getAllActivePermanents(playerA.getId()).stream()
                .filter(permanent -> permanent.getName().equals("Ocelot Pride"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ocelot Pride not found after the end step"));
        Assert.assertFalse("Caetus must untap Ocelot Pride at the end step", ocelot.isTapped());
    }
}
