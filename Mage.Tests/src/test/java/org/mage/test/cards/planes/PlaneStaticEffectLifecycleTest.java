package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.PlaneswalkContext;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class PlaneStaticEffectLifecycleTest extends CardTestPlayerBase {

    @Test
    public void repeatedPlaneswalksReplaceBonusesAndRemoveHaste() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_SOKENZAN, Planes.PLANE_MEGAFLORA_JUNGLE, Planes.PLANE_AGYREM);
        addCreatures();

        runCode("cycle through both boosting Planes and a neutral Plane",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
                    UUID sokenzanId = game.getState().getFaceUpPlanes().get(0).getId();
                    for (int visit = 0; visit < 3; visit++) {
                        assertPlaneAndCreatures(game, Planes.PLANE_SOKENZAN, 3, 4, true);
                        Assert.assertTrue(game.planeswalk(playerA.getId()));
                        assertPlaneAndCreatures(game, Planes.PLANE_MEGAFLORA_JUNGLE, 4, 3, false);
                        Assert.assertNotNull("Departed sources must still resolve for delayed triggers",
                                game.getObject(sokenzanId));
                        Assert.assertFalse(game.getState().isFaceUpPlanarCard(sokenzanId));
                        Assert.assertTrue(game.planeswalk(playerA.getId()));
                        assertPlaneAndCreatures(game, Planes.PLANE_AGYREM, 2, 3, false);
                        if (visit < 2) {
                            Assert.assertTrue(game.planeswalk(playerA.getId()));
                        }
                    }
                });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, "Grizzly Bears", 2, 2);
        assertPowerToughness(playerB, "Grizzly Bears", 2, 2);
        assertPowerToughness(playerA, "Hill Giant", 3, 3);
        assertAbility(playerA, "Grizzly Bears", HasteAbility.getInstance(), false);
    }

    @Test
    public void returningImmediatelyToTheSamePlaneDoesNotDuplicateItsEffects() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_SOKENZAN);
        addCreatures();

        runCode("leave and return without an intervening effects update",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
                    for (int visit = 0; visit < 4; visit++) {
                        assertPlaneAndCreatures(game, Planes.PLANE_SOKENZAN, 3, 4, true);
                        Assert.assertTrue(game.planeswalk(playerA.getId()));
                    }
                    assertPlaneAndCreatures(game, Planes.PLANE_SOKENZAN, 3, 4, true);
                });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();
        assertPowerToughness(playerA, "Grizzly Bears", 3, 3);
    }

    @Test
    public void multipleFaceUpPlanesCombineOnlyUntilTheyLeave() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_AGYREM, Planes.PLANE_SOKENZAN, Planes.PLANE_MEGAFLORA_JUNGLE);
        addCreatures();

        runCode("both face-up Planes contribute exactly once",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
                    // Use the same simultaneous-arrival path as Spatial Merging.
                    Assert.assertTrue(game.planeswalkToNextPlanes(PlaneswalkContext.forPlayer(playerA.getId()), 2));
                    Assert.assertEquals(2, game.getState().getFaceUpPlanes().size());
                    game.applyEffects();
                    assertCreatures(5, 4, true);

                    Assert.assertTrue(game.planeswalk(playerA.getId()));
                    assertPlaneAndCreatures(game, Planes.PLANE_AGYREM, 2, 3, false);
                });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();
        assertPowerToughness(playerA, "Grizzly Bears", 2, 2);
    }

    @Test
    public void resolvedChaosEffectsKeepTheirOwnDurationAfterPlaneswalking() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_THE_GREAT_FOREST, Planes.PLANE_SOKENZAN, Planes.PLANE_MEGAFLORA_JUNGLE);
        addCreatures();
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        runCode("Great Forest's resolved chaos bonus outlives its Plane",
                1, PhaseStep.BEGIN_COMBAT, playerA, (info, player, game) -> {
                    // Chaos gives only our creatures +0/+2 and trample until end of turn.
                    assertPowerToughness(playerA, "Grizzly Bears", 2, 4);
                    assertAbility(playerA, "Grizzly Bears", TrampleAbility.getInstance(), true);
                    Assert.assertTrue(game.planeswalk(playerA.getId()));
                    game.applyEffects();
                    assertPowerToughness(playerA, "Grizzly Bears", 3, 5);
                    assertPowerToughness(playerB, "Grizzly Bears", 3, 3);
                    assertAbility(playerA, "Grizzly Bears", TrampleAbility.getInstance(), true);
                    assertAbility(playerA, "Grizzly Bears", HasteAbility.getInstance(), true);

                    Assert.assertTrue(game.planeswalk(playerA.getId()));
                    game.applyEffects();
                    // Sokenzan's static bonus ends, Great Forest's chaos bonus remains.
                    assertPowerToughness(playerA, "Grizzly Bears", 4, 6);
                    assertPowerToughness(playerB, "Grizzly Bears", 4, 4);
                    assertPowerToughness(playerA, "Hill Giant", 3, 5);
                    assertAbility(playerA, "Grizzly Bears", TrampleAbility.getInstance(), true);
                    assertAbility(playerB, "Grizzly Bears", TrampleAbility.getInstance(), false);
                    assertAbility(playerA, "Grizzly Bears", HasteAbility.getInstance(), false);
                });

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        // The chaos effect expires at cleanup; the currently face-up Plane still works.
        assertPowerToughness(playerA, "Grizzly Bears", 4, 4);
        assertPowerToughness(playerB, "Grizzly Bears", 4, 4);
        assertPowerToughness(playerA, "Hill Giant", 3, 3);
        assertAbility(playerA, "Grizzly Bears", TrampleAbility.getInstance(), false);
        assertAbility(playerA, "Grizzly Bears", HasteAbility.getInstance(), false);
    }

    private void addCreatures() {
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        // Mana value 4: Sokenzan applies, Megaflora Jungle does not.
        addCard(Zone.BATTLEFIELD, playerA, "Hill Giant");
    }

    private void assertPlaneAndCreatures(Game game, Planes plane, int bearsPT, int giantPT, boolean haste) {
        Assert.assertEquals(1, game.getState().getFaceUpPlanes().size());
        Assert.assertEquals(plane, game.getState().getFaceUpPlanes().get(0).getPlaneType());
        game.applyEffects();
        assertCreatures(bearsPT, giantPT, haste);
        // Recalculation itself must not accumulate another copy of the bonus.
        game.applyEffects();
        assertCreatures(bearsPT, giantPT, haste);
    }

    private void assertCreatures(int bearsPT, int giantPT, boolean haste) {
        assertPowerToughness(playerA, "Grizzly Bears", bearsPT, bearsPT);
        assertPowerToughness(playerB, "Grizzly Bears", bearsPT, bearsPT);
        assertPowerToughness(playerA, "Hill Giant", giantPT, giantPT);
        assertAbility(playerA, "Grizzly Bears", HasteAbility.getInstance(), haste);
        assertAbility(playerB, "Grizzly Bears", HasteAbility.getInstance(), haste);
        assertAbility(playerA, "Hill Giant", HasteAbility.getInstance(), haste);
    }
}
