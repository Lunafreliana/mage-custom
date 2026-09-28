package org.mage.test.cards.planes;

import mage.constants.MultiplayerAttackOption;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.RangeOfInfluence;
import mage.constants.Zone;
import mage.game.FreeForAll;
import mage.game.Game;
import mage.game.GameException;
import mage.game.command.Plane;
import mage.game.command.phenomena.SpatialMergingPhenomenon;
import mage.game.events.GameEvent;
import mage.game.mulligan.MulliganType;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestMultiPlayerBase;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/** Multiplayer regression tests for Agyrem's chaos ability. */
public class AgyremSpatialMergingTest extends CardTestMultiPlayerBase {

    @Override
    protected Game createNewGameAndPlayers() throws GameException, FileNotFoundException {
        Game game = new FreeForAll(MultiplayerAttackOption.MULTIPLE, RangeOfInfluence.ALL,
                MulliganType.GAME_DEFAULT.getMulligan(0), 20, 7);
        playerA = createPlayer(game, "PlayerA");
        playerB = createPlayer(game, "PlayerB");
        playerC = createPlayer(game, "PlayerC");
        playerD = createPlayer(game, "PlayerD");
        return game;
    }

    @Test
    public void threePlayersWhoEachRollChaosCannotAttackOneAnother() {
        verifyEveryChaosRollerIsProtected(false);
    }

    @Test
    public void fourPlayersWhoEachRollChaosCannotAttackOneAnother() {
        verifyEveryChaosRollerIsProtected(true);
    }

    private void verifyEveryChaosRollerIsProtected(boolean includeFourthPlayer) {
        addPlane(playerA, Planes.PLANE_FIELDS_OF_SUMMER);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");
        addCard(Zone.BATTLEFIELD, playerC, "Hill Giant");
        if (includeFourthPlayer) {
            addCard(Zone.BATTLEFIELD, playerD, "Walking Corpse");
        }

        runCode("encounter Spatial Merging and have every player roll chaos",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
                    if (!includeFourthPlayer) {
                        playerD.lostForced(game);
                    }

                    game.getState().getSharedPlanarDeck().setPlanes(Arrays.asList(
                            Plane.createPlane(Planes.PLANE_AGYREM),
                            Plane.createPlane(Planes.PLANE_TRAIL_OF_THE_MAGE_RINGS)
                    ), false);
                    Assert.assertTrue(info, game.addPhenomenon(
                            new SpatialMergingPhenomenon(), playerA.getId()));
                    resolveStack(game);

                    Assert.assertTrue(info, game.getState().hasFaceUpPlane(Planes.PLANE_AGYREM));
                    Assert.assertTrue(info, game.getState().hasFaceUpPlane(
                            Planes.PLANE_TRAIL_OF_THE_MAGE_RINGS));

                    List<TestPlayer> players = includeFourthPlayer
                            ? Arrays.asList(playerA, playerB, playerC, playerD)
                            : Arrays.asList(playerA, playerB, playerC);
                    for (TestPlayer chaosRoller : players) {
                        // A planar-die roll can normally happen only during the
                        // roller's main phase. Move planar control as it would
                        // move when that player's turn begins, while keeping
                        // this multiplayer regression test compact.
                        game.setPlanarControllerId(chaosRoller.getId());
                        game.fireEvent(new GameEvent(GameEvent.EventType.CHAOS_ENSUES,
                                null, null, chaosRoller.getId()));
                        resolveStack(game);
                    }

                    List<String> creatures = includeFourthPlayer
                            ? Arrays.asList("Grizzly Bears", "Glory Seeker", "Hill Giant", "Walking Corpse")
                            : Arrays.asList("Grizzly Bears", "Glory Seeker", "Hill Giant");
                    for (int attackerIndex = 0; attackerIndex < players.size(); attackerIndex++) {
                        Permanent attacker = getPermanent(creatures.get(attackerIndex), players.get(attackerIndex));
                        for (int defenderIndex = 0; defenderIndex < players.size(); defenderIndex++) {
                            if (attackerIndex != defenderIndex) {
                                Assert.assertFalse(info + ": " + players.get(attackerIndex).getName()
                                                + " must not be able to attack " + players.get(defenderIndex).getName(),
                                        attacker.canAttack(players.get(defenderIndex).getId(), game));
                            }
                        }
                    }
                });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    private static void resolveStack(Game game) {
        game.checkStateAndTriggered();
        while (!game.getStack().isEmpty()) {
            game.getStack().resolve(game);
            game.checkStateAndTriggered();
        }
    }
}
