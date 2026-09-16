package org.mage.test.cards.single.mbc;

import mage.constants.MultiplayerAttackOption;
import mage.constants.PhaseStep;
import mage.constants.RangeOfInfluence;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.FreeForAll;
import mage.game.Game;
import mage.game.GameException;
import mage.game.mulligan.MulliganType;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestMultiPlayerBase;

import java.io.FileNotFoundException;

public class MissHighwaterTest extends CardTestMultiPlayerBase {

    private static final String missHighwater = "Miss Highwater";

    @Override
    protected Game createNewGameAndPlayers() throws GameException, FileNotFoundException {
        Game game = new FreeForAll(
                MultiplayerAttackOption.MULTIPLE, RangeOfInfluence.ALL,
                MulliganType.GAME_DEFAULT.getMulligan(0), 20, 7
        );
        playerA = createPlayer(game, "PlayerA");
        playerB = createPlayer(game, "PlayerB");
        playerC = createPlayer(game, "PlayerC");
        return game;
    }

    @Test
    public void damagedPlayerMayAcceptContract() {
        addCard(Zone.BATTLEFIELD, playerA, missHighwater);
        addCard(Zone.HAND, playerB, "Forest", 2);
        addCard(Zone.LIBRARY, playerB, "Island", 10);

        attack(1, playerA, missHighwater, playerB);
        setChoice(playerB, true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 15);
        assertHandCount(playerB, 7);
        assertGraveyardCount(playerB, "Forest", 2);
        Assert.assertEquals(1, playerB.getCountersCount(CounterType.CONTRACT));
    }

    @Test
    public void contractCopiesControlledArtifactsAndCreaturesWhenPlayerLoses() {
        addCard(Zone.BATTLEFIELD, playerA, missHighwater);
        addCard(Zone.LIBRARY, playerB, "Island", 10);
        addCard(Zone.BATTLEFIELD, playerB, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Sol Ring");
        addCard(Zone.BATTLEFIELD, playerB, "Ornithopter");
        addCard(Zone.BATTLEFIELD, playerB, "Forest");
        setLife(playerB, 6);

        addCard(Zone.HAND, playerC, "Lightning Bolt");
        addCard(Zone.BATTLEFIELD, playerC, "Mountain");

        attack(1, playerA, missHighwater, playerB);
        setChoice(playerB, true);
        // A concession makes a player leave immediately, so use a game loss to exercise the LOST trigger.
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerC, "Lightning Bolt", playerB);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        Assert.assertFalse("Player B has left the game", playerB.isInGame());
        assertPermanentCount(playerA, "Balduvian Bears", 1);
        assertPermanentCount(playerA, "Sol Ring", 1);
        assertPermanentCount(playerA, "Ornithopter", 1);
        assertPermanentCount(playerA, "Forest", 0);
    }

    @Test
    public void playerWithContractDoesNotReceiveAnotherOffer() {
        addCard(Zone.BATTLEFIELD, playerA, missHighwater);
        addCard(Zone.HAND, playerB, "Forest", 2);
        addCard(Zone.LIBRARY, playerB, "Island", 20);

        attack(1, playerA, missHighwater, playerB);
        setChoice(playerB, true);
        attack(4, playerA, missHighwater, playerB);

        setStopAt(4, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 10);
        assertHandCount(playerB, 7);
        Assert.assertEquals(1, playerB.getCountersCount(CounterType.CONTRACT));
    }
}
