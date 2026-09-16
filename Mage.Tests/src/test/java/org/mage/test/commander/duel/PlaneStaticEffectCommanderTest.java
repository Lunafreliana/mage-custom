package org.mage.test.commander.duel;

import mage.abilities.keyword.HasteAbility;
import mage.cards.Card;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommanderDuelBase;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PlaneStaticEffectCommanderTest extends CardTestCommanderDuelBase {

    private static final String COMMANDER = "Isamaru, Hound of Konda";

    @Test
    public void sokenzanBonusDoesNotFollowCommanderThroughCommandZone() {
        checkCommanderRoundTrip(Planes.PLANE_SOKENZAN, Planes.PLANE_MEGAFLORA_JUNGLE);
    }

    @Test
    public void megafloraBonusDoesNotFollowCommanderThroughCommandZone() {
        checkCommanderRoundTrip(Planes.PLANE_MEGAFLORA_JUNGLE, Planes.PLANE_SOKENZAN);
    }

    private void checkCommanderRoundTrip(Planes firstPlane, Planes secondPlane) {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(firstPlane, secondPlane, Planes.PLANE_AGYREM);
        addCard(Zone.COMMAND, playerA, COMMANDER);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        AtomicReference<UUID> commanderId = new AtomicReference<>();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, COMMANDER);
        runCode("sacrifice the boosted commander",
                1, PhaseStep.BEGIN_COMBAT, playerA, (info, player, game) -> {
                    assertCommanderOnPlane(game, firstPlane);
                    Permanent commander = getPermanent(COMMANDER, playerA);
                    commanderId.set(commander.getId());
                    Assert.assertTrue(commander.sacrifice(null, game));
                });
        setChoice(playerA, true); // Move the commander from the graveyard to the command zone.

        runCode("commander has printed stats in command before and after planeswalking",
                1, PhaseStep.POSTCOMBAT_MAIN, playerA, (info, player, game) -> {
                    assertCommanderInCommand(game, commanderId.get());
                    Assert.assertTrue(game.planeswalk(playerA.getId()));
                    game.applyEffects();
                    Assert.assertEquals(secondPlane, game.getState().getFaceUpPlanes().get(0).getPlaneType());
                    assertCommanderInCommand(game, commanderId.get());
                });

        // Three Plains pay {W} plus the first commander tax after untapping.
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, COMMANDER);
        runCode("recast commander receives only the current Plane's bonus",
                3, PhaseStep.BEGIN_COMBAT, playerA, (info, player, game) -> {
                    assertCommanderOnPlane(game, secondPlane);
                });
        runCode("remove all Plane bonuses, then revisit each Plane without stacking",
                3, PhaseStep.POSTCOMBAT_MAIN, playerA, (info, player, game) -> {
                    Assert.assertTrue(game.planeswalk(playerA.getId()));
                    assertCommanderOnPlane(game, Planes.PLANE_AGYREM);
                    Assert.assertTrue(game.planeswalk(playerA.getId()));
                    assertCommanderOnPlane(game, firstPlane);
                    Assert.assertTrue(game.planeswalk(playerA.getId()));
                    assertCommanderOnPlane(game, secondPlane);
                    Assert.assertTrue(game.planeswalk(playerA.getId()));
                    assertCommanderOnPlane(game, Planes.PLANE_AGYREM);
                });

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.END_TURN);
        execute();

        assertCommandZoneCount(playerA, COMMANDER, 0);
        assertGraveyardCount(playerA, COMMANDER, 0);
        assertPermanentCount(playerA, COMMANDER, 1);
        assertPowerToughness(playerA, COMMANDER, 2, 2);
        assertAbility(playerA, COMMANDER, HasteAbility.getInstance(), false);
    }

    private void assertCommanderInCommand(Game game, UUID commanderId) {
        Assert.assertEquals(Zone.COMMAND, game.getState().getZone(commanderId));
        assertCommandZoneCount(playerA, COMMANDER, 1);
        Card commander = game.getCard(commanderId);
        Assert.assertNotNull(commander);
        Assert.assertEquals(2, commander.getPower().getValue());
        Assert.assertEquals(2, commander.getToughness().getValue());
        Assert.assertFalse(commander.getAbilities().contains(HasteAbility.getInstance()));
    }

    private void assertCommanderOnPlane(Game game, Planes plane) {
        Assert.assertEquals(plane, game.getState().getFaceUpPlanes().get(0).getPlaneType());
        game.applyEffects();
        int expectedPT = plane == Planes.PLANE_SOKENZAN ? 3
                : plane == Planes.PLANE_MEGAFLORA_JUNGLE ? 4 : 2;
        assertPowerToughness(playerA, COMMANDER, expectedPT, expectedPT);
        assertAbility(playerA, COMMANDER, HasteAbility.getInstance(), plane == Planes.PLANE_SOKENZAN);
    }
}
