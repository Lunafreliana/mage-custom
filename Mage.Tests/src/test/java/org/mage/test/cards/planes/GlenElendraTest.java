package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.events.GameEvent;
import mage.target.common.TargetCreaturePermanent;
import mage.watchers.common.CombatDamageToPlayerThisCombatWatcher;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.UUID;

/**
 * Tests for Plane - Glen Elendra.
 */
public class GlenElendraTest extends CardTestPlayerBase {

    @Test
    public void exchangesWithCreatureControlledByDamagedPlayer() {
        setStrictChooseMode(true);
        addPlane(playerA, Planes.PLANE_GLEN_ELENDRA);
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");

        attack(1, playerA, "Runeclaw Bear", playerB);
        addTarget(playerA, "Runeclaw Bear");
        addTarget(playerA, "Glory Seeker");
        setChoice(playerA, true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Glory Seeker", 1);
        assertPermanentCount(playerB, "Runeclaw Bear", 1);
    }

    @Test
    public void chaosReturnsCreatureToItsOwner() {
        setStrictChooseMode(true);
        addPlane(playerA, Planes.PLANE_GLEN_ELENDRA);
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");

        SpellAbility steal = new SpellAbility(new ManaCostsImpl<>("{0}"), "Steal");
        steal.addEffect(new GainControlTargetEffect(Duration.EndOfGame));
        steal.addTarget(new TargetCreaturePermanent());
        addCustomCardWithSpell(playerB, steal, null, CardType.SORCERY);

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Steal", "Runeclaw Bear");
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Runeclaw Bear");

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Runeclaw Bear", 1);
        assertPermanentCount(playerB, "Runeclaw Bear", 0);
    }

    @Test
    public void mayDeclineTheExchange() {
        setStrictChooseMode(true);
        addPlane(playerA, Planes.PLANE_GLEN_ELENDRA);
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");

        attack(1, playerA, "Runeclaw Bear", playerB);
        addTarget(playerA, "Runeclaw Bear");
        addTarget(playerA, "Glory Seeker");
        setChoice(playerA, false);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertPermanentCount(playerA, "Runeclaw Bear", 1);
        assertPermanentCount(playerB, "Glory Seeker", 1);
    }

    @Test
    public void firstStrikeDamageStillQualifiesAfterTheNormalDamageStep() {
        setStrictChooseMode(true);
        addPlane(playerA, Planes.PLANE_GLEN_ELENDRA);
        addCard(Zone.BATTLEFIELD, playerA, "Youthful Knight");
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");

        attack(1, playerA, "Youthful Knight", playerB);
        attack(1, playerA, "Runeclaw Bear", playerB);
        addTarget(playerA, "Youthful Knight");
        addTarget(playerA, "Glory Seeker");
        setChoice(playerA, true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 16);
        assertPermanentCount(playerA, "Glory Seeker", 1);
        assertPermanentCount(playerB, "Youthful Knight", 1);
        assertPermanentCount(playerA, "Runeclaw Bear", 1);
    }

    @Test
    public void noExchangeWhenTheDamagedPlayersOnlyCreatureHasHexproof() {
        setStrictChooseMode(true);
        addPlane(playerA, Planes.PLANE_GLEN_ELENDRA);
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerB, "Slippery Bogle");

        attack(1, playerA, "Runeclaw Bear", playerB);

        // There is no legal pair, so there must be no targeting or may prompt.
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertPermanentCount(playerA, "Runeclaw Bear", 1);
        assertPermanentCount(playerB, "Slippery Bogle", 1);
    }

    @Test
    public void endingTheTurnDuringCombatClearsDamageHistory() {
        setStrictChooseMode(true);
        addPlane(playerA, Planes.PLANE_GLEN_ELENDRA);
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 6);
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");
        addCard(Zone.HAND, playerA, "Time Stop");

        attack(1, playerA, "Runeclaw Bear", playerB);
        runCode("damage is recorded and watcher copies are independent", 1, PhaseStep.COMBAT_DAMAGE, playerA,
                (info, player, game) -> {
                    CombatDamageToPlayerThisCombatWatcher watcher = game.getState()
                            .getWatcher(CombatDamageToPlayerThisCombatWatcher.class);
                    Assert.assertNotNull(info, watcher);
                    UUID creatureId = getPermanent("Runeclaw Bear", playerA).getId();
                    Assert.assertEquals(info, playerB.getId(), watcher.getDamagedPlayer(creatureId, game));

                    CombatDamageToPlayerThisCombatWatcher copy = watcher.copy();
                    copy.watch(new GameEvent(GameEvent.EventType.BEGIN_COMBAT_STEP_PRE,
                            null, null, player.getId()), game);
                    Assert.assertNull(info, copy.getDamagedPlayer(creatureId, game));
                    Assert.assertEquals(info, playerB.getId(), watcher.getDamagedPlayer(creatureId, game));
                });
        castSpell(1, PhaseStep.COMBAT_DAMAGE, playerA, "Time Stop");
        runCode("skipping end of combat cannot retain last turn's damage", 2, PhaseStep.UPKEEP, playerB,
                (info, player, game) -> Assert.assertNull(info, game.getState()
                        .getWatcher(CombatDamageToPlayerThisCombatWatcher.class)
                        .getDamagedPlayer(getPermanent("Runeclaw Bear", playerA).getId(), game)));

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertExileCount(playerA, "Time Stop", 1);
        assertPermanentCount(playerA, "Runeclaw Bear", 1);
        assertPermanentCount(playerB, "Glory Seeker", 1);
    }

    @Test
    public void isAvailableThroughPlanarCardRegistry() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_GLEN_ELENDRA);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Glen Elendra", metadata.getEnglishName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
