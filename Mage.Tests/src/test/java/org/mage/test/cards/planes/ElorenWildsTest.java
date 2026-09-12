package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.events.GameEvent;
import org.junit.Assert;
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

        runCode("resolve chaos and planeswalk", 1, PhaseStep.UPKEEP, playerA, (info, player, game) -> {
            game.fireEvent(new GameEvent(GameEvent.EventType.CHAOS_ENSUES,
                    game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId()));
            game.checkStateAndTriggered();
            game.getStack().resolve(game);

            GameEvent castBeforePlaneswalk = new GameEvent(
                    GameEvent.EventType.CAST_SPELL, null, null, playerB.getId());
            Assert.assertTrue(info + " -- target player must be unable to cast",
                    game.replaceEvent(castBeforePlaneswalk));
            Assert.assertFalse(info + " -- other players must still be able to cast",
                    game.replaceEvent(new GameEvent(
                            GameEvent.EventType.CAST_SPELL, null, null, playerA.getId())));

            game.fireEvent(new GameEvent(GameEvent.EventType.PLANESWALKED,
                    game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId()));

            GameEvent castAfterPlaneswalk = new GameEvent(
                    GameEvent.EventType.CAST_SPELL, null, null, playerB.getId());
            Assert.assertFalse(info + " -- target player must be able to cast after planeswalking",
                    game.replaceEvent(castAfterPlaneswalk));
        });
        addTarget(playerA, playerB);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}
