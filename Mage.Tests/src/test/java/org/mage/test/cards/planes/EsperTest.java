package org.mage.test.cards.planes;

import mage.abilities.keyword.LifelinkAbility;
import mage.abilities.keyword.MenaceAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/** Focused behavior tests for Esper. */
public class EsperTest extends CardTestPlayerBase {

    @Test
    public void artifactSpellsCostOneLess() {
        addPlane(playerA, Planes.PLANE_ESPER);
        addCard(Zone.HAND, playerA, "Howling Mine");
        addCard(Zone.BATTLEFIELD, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Howling Mine");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Howling Mine", 1);
    }

    @Test
    public void chaosChangesAndGrantsAbilitiesToCorrectCreatures() {
        addPlane(playerA, Planes.PLANE_ESPER);
        addCard(Zone.BATTLEFIELD, playerA, "Savannah Lions");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Ornithopter");
        addCard(Zone.BATTLEFIELD, playerB, "Savannah Lions");

        runCode("resolve chaos", 1, PhaseStep.UPKEEP, playerA, (info, player, game) -> {
            game.fireEvent(new GameEvent(GameEvent.EventType.CHAOS_ENSUES,
                    game.getState().getFaceUpPlanes().get(0).getId(), null, player.getId()));
            game.checkStateAndTriggered();
            game.getStack().resolve(game);
        });

        runCode("verify chaos", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Permanent lions = getPermanent("Savannah Lions", playerA);
            Permanent bears = getPermanent("Grizzly Bears", playerA);
            Permanent ornithopter = getPermanent("Ornithopter", playerA);
            Permanent opposingLions = getPermanent("Savannah Lions", playerB);

            Assert.assertTrue(lions.getCardType(game).contains(CardType.ARTIFACT));
            Assert.assertFalse(bears.getCardType(game).contains(CardType.ARTIFACT));
            Assert.assertTrue(ornithopter.getAbilities().containsClass(VigilanceAbility.class));
            Assert.assertTrue(ornithopter.getAbilities().containsClass(MenaceAbility.class));
            Assert.assertTrue(ornithopter.getAbilities().containsClass(LifelinkAbility.class));
            Assert.assertFalse(opposingLions.getCardType(game).contains(CardType.ARTIFACT));
            Assert.assertFalse(opposingLions.getAbilities().containsClass(VigilanceAbility.class));
        });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }
}
