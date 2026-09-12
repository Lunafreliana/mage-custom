package org.mage.test.cards.planes;

import mage.constants.ManaType;
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

        assertManaPool(playerA, ManaType.GREEN, 2);
    }

    @Test
    public void testChaosStopsTargetCastingOnlyUntilPlaneswalk() {
        addPlane(playerA, Planes.PLANE_ELOREN_WILDS);
        addCard(Zone.HAND, playerB, "Memnite", 2);

        runCode("chaos ensues", 1, PhaseStep.UPKEEP, playerA, (info, player, game) -> {
            game.fireEvent(new GameEvent(GameEvent.EventType.CHAOS_ENSUES,
                    game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId()));
            game.checkStateAndTriggered();
            game.getStack().resolve(game);
        });
        setChoice(playerA, playerB.getName());
        checkPlayableAbility("casting is prohibited", 1, PhaseStep.PRECOMBAT_MAIN, playerB,
                "Cast Memnite", false);
        runCode("a player planeswalks", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) ->
                game.fireEvent(new GameEvent(GameEvent.EventType.PLANESWALKED,
                        game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId())));
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Memnite");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerB, "Memnite", 1);
    }
}
