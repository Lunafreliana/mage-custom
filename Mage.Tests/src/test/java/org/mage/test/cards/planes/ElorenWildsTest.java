package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.events.GameEvent;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/** Focused behavior tests for Eloren Wilds. */
public class ElorenWildsTest extends CardTestPlayerBase {

    @Test
    public void testPermanentTappedForManaProducesAdditionalMana() {
        addPlane(playerA, Planes.PLANE_ELOREN_WILDS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}");
        checkManaPool("additional mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "G", 2);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testChaosStopsTargetCastingOnlyUntilPlaneswalk() {
        addPlane(playerA, Planes.PLANE_ELOREN_WILDS);
        addCard(Zone.HAND, playerB, "Memnite", 2);
        addCard(Zone.LIBRARY, playerB, "Mountain", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Vedalken Orrery");

        runCode("chaos ensues", 1, PhaseStep.UPKEEP, playerA, (info, player, game) -> {
            game.fireEvent(new GameEvent(GameEvent.EventType.CHAOS_ENSUES,
                    game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId()));
            game.checkStateAndTriggered();
            game.getStack().resolve(game);
        });
        addTarget(playerA, playerB);
        checkPlayableAbility("casting is prohibited", 1, PhaseStep.DRAW, playerB,
                "Cast Memnite", false);
        runCode("a player planeswalks", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) ->
                game.fireEvent(new GameEvent(GameEvent.EventType.PLANESWALKED,
                        game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId())));
        checkPlayableAbility("casting is allowed after planeswalking", 2, PhaseStep.PRECOMBAT_MAIN,
                playerB, "Cast Memnite", true);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }
}
