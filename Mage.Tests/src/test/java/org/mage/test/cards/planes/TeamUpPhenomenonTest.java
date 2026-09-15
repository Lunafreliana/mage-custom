package org.mage.test.cards.planes;

import mage.constants.MultiplayerAttackOption;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.RangeOfInfluence;
import mage.constants.Zone;
import mage.game.FreeForAll;
import mage.game.Game;
import mage.game.GameException;
import mage.game.command.phenomena.TeamUpPhenomenon;
import mage.game.mulligan.MulliganType;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestMultiPlayerBase;

import java.io.FileNotFoundException;
import java.util.Collections;

public class TeamUpPhenomenonTest extends CardTestMultiPlayerBase {

    @Override
    protected Game createNewGameAndPlayers() throws GameException, FileNotFoundException {
        Game game = new FreeForAll(MultiplayerAttackOption.MULTIPLE, RangeOfInfluence.ALL,
                MulliganType.GAME_DEFAULT.getMulligan(0), 20, 7);
        // Player order: A -> D -> C -> B
        playerA = createPlayer(game, "PlayerA");
        playerB = createPlayer(game, "PlayerB");
        playerC = createPlayer(game, "PlayerC");
        playerD = createPlayer(game, "PlayerD");
        return game;
    }

    @Test
    public void winsAfterBothAdjacentPlayersAreEliminated() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        addCard(Zone.LIBRARY, playerC, "Mountain", 20);
        addCard(Zone.LIBRARY, playerD, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);

        runCode("encounter Team-Up!", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Assert.assertTrue(info, game.addPhenomenon(new TeamUpPhenomenon(), player.getId()));
                    game.checkStateAndTriggered();
                    game.getStack().resolve(game);

                    // A's seated neighbors are D and B. Eliminating only D must
                    // not satisfy Team-Up!'s condition.
                    playerD.lostForced(game);
                    game.checkStateAndTriggered();
                    game.getStack().resolve(game);
                    Assert.assertFalse(info, playerA.hasWon());

                    // The delayed ability remains after the Phenomenon is left.
                    playerB.lostForced(game);
                    game.checkStateAndTriggered();
                    game.getStack().resolve(game);
                    Assert.assertTrue(info, playerA.hasWon());
                });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }
}
