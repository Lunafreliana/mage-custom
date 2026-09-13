package org.mage.test.cards.planes;

import mage.constants.CardType;
import mage.constants.MageObjectType;
import mage.constants.PhaseStep;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Phenomenon;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.phenomena.MutualEpiphanyPhenomenon;
import mage.game.command.phenomena.RealityShapingPhenomenon;
import mage.game.command.phenomena.SpatialMergingPhenomenon;
import mage.game.events.GameEvent;
import mage.game.stack.StackAbility;
import mage.game.stack.StackObject;
import mage.view.CardView;
import mage.view.GameView;
import mage.view.StackAbilityView;
import mage.watchers.common.PlaneswalkedWatcher;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class PhenomenonTest extends CardTestPlayerBase {

    @Test
    public void testRuntimeCharacteristicsAndCopy() {
        Phenomenon phenomenon = new MutualEpiphanyPhenomenon();
        phenomenon.setFaceUp(true);
        Phenomenon copy = phenomenon.copy();

        Assert.assertEquals(CardType.PHENOMENON, copy.getPlanarCardType());
        Assert.assertEquals(Collections.singletonList(CardType.PHENOMENON), copy.getCardType(currentGame));
        Assert.assertTrue(copy.getSubtype().isEmpty());
        Assert.assertTrue(copy.isFaceUp());
        copy.setFaceUp(false);
        Assert.assertEquals(1, copy.getZoneChangeCounter(currentGame));
    }

    @Test
    public void testSetupSkipsPhenomenonWithoutEncounteringIt() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarPhenomena = Collections.singletonList(Phenomena.MUTUAL_EPIPHANY);
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);

        runCode("verify phenomenon-free startup", 1, PhaseStep.UPKEEP, playerA, (info, player, game) -> {
            Assert.assertTrue(info, game.getStack().isEmpty());
            Assert.assertEquals(info, 0,
                    game.getState().getWatcher(PlaneswalkedWatcher.class).getCount());
        });

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        Assert.assertEquals(1, currentGame.getState().getFaceUpPlanes().size());
        Assert.assertTrue(currentGame.getState().getFaceUpPhenomena().isEmpty());
        Assert.assertEquals(1, currentGame.getState().getSharedPlanarDeck().size());
        assertHandCount(playerA, 0);
        assertHandCount(playerB, 0);
    }

    @Test
    public void testMixedRegistryIdDeckStartsWithAConfiguredPlane() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarCardIds = Arrays.asList(
                PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY),
                PlanarCardRegistry.getId(Planes.PLANE_FIELDS_OF_SUMMER),
                PlanarCardRegistry.getId(Planes.PLANE_AKOUM));

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        String startingPlane = currentGame.getState().getFaceUpPlanes().get(0).getName();
        Assert.assertTrue(startingPlane.equals("Plane - Fields of Summer")
                || startingPlane.equals("Plane - Akoum"));
        Assert.assertEquals(2, currentGame.getState().getSharedPlanarDeck().size());
    }

    @Test
    public void testEncounterTriggerDelaysSbaUntilResolution() {
        prepareStartedPlanechaseGame();
        runCode("encounter and resolve phenomenon", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertTrue(game.addPhenomenon(new MutualEpiphanyPhenomenon(), player.getId()));
            game.checkStateAndTriggered();
            Assert.assertEquals(info, 1, game.getStack().size());
            Assert.assertEquals(info, 1, game.getState().getFaceUpPhenomena().size());

            int handA = playerA.getHand().size();
            int handB = playerB.getHand().size();
            game.getStack().resolve(game);
            Assert.assertEquals(info, handA + 4, playerA.getHand().size());
            Assert.assertEquals(info, handB + 4, playerB.getHand().size());

            game.checkStateAndTriggered();
            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
            Assert.assertEquals(info, 1, game.getState().getFaceUpPlanes().size());
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testMutualEpiphanyEncounterTriggerHasStackViews() {
        assertPhenomenonEncounterStackViews(new MutualEpiphanyPhenomenon());
    }

    @Test
    public void testRealityShapingEncounterTriggerHasStackViews() {
        assertPhenomenonEncounterStackViews(new RealityShapingPhenomenon());
    }

    @Test
    public void testSpatialMergingEncounterTriggerHasStackViews() {
        assertPhenomenonEncounterStackViews(new SpatialMergingPhenomenon());
    }

    private void assertPhenomenonEncounterStackViews(Phenomenon phenomenon) {
        prepareStartedPlanechaseGame();
        runCode("inspect " + phenomenon.getName() + " encounter stack views",
                1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertTrue(info, game.addPhenomenon(phenomenon, player.getId()));
            game.checkStateAndTriggered();

            StackAbility encounter = getOnlyStackAbility(info, game);
            Assert.assertNotNull(info, game.getObject(encounter.getSourceId()));
            Assert.assertTrue(info, game.getObject(encounter.getSourceId()) instanceof Phenomenon);
            Phenomenon source = (Phenomenon) game.getObject(encounter.getSourceId());
            Assert.assertEquals(info, phenomenon.getName(), source.getName());
            assertPendingStackViews(info, game, encounter.getId(), source);

            game.getStack().remove(encounter, game);
            game.checkStateAndTriggered();
            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testRemovingEncounterTriggerMakesSbaPlaneswalk() {
        prepareStartedPlanechaseGame();
        runCode("remove encounter trigger", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertTrue(game.addPhenomenon(new MutualEpiphanyPhenomenon(), player.getId()));
            game.checkStateAndTriggered();

            StackObject encounterTrigger = game.getStack().getFirstOrNull();
            Assert.assertNotNull(info, encounterTrigger);
            game.getStack().remove(encounterTrigger, game);
            game.checkStateAndTriggered();

            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
            Assert.assertEquals(info, 1, game.getState().getFaceUpPlanes().size());
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testSpatialMergingPlaneswalksToTwoPlanesSimultaneously() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_FIELDS_OF_SUMMER,
                Planes.PLANE_AKOUM,
                Planes.PLANE_AGYREM);

        runCode("encounter Spatial Merging", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            // Preserve the initialized, deterministic next two cards while the
            // addPhenomenon test seam turns Spatial Merging face up.
            PlanarCard first = game.getState().getSharedPlanarDeck().draw();
            PlanarCard second = game.getState().getSharedPlanarDeck().draw();
            Assert.assertTrue(info, game.addPhenomenon(new SpatialMergingPhenomenon(), player.getId()));
            game.getState().getSharedPlanarDeck().putOnBottom(first);
            game.getState().getSharedPlanarDeck().putOnBottom(second);

            game.checkStateAndTriggered();
            Assert.assertEquals(info, 1, game.getStack().size());
            StackAbility encounter = getOnlyStackAbility(info, game);
            assertPendingStackViews(info, game, encounter.getId(),
                    game.getState().getFaceUpPhenomena().get(0));
            game.getStack().resolve(game);

            Assert.assertEquals(info, 2, game.getState().getFaceUpPlanes().size());
            Assert.assertEquals(info, "Plane - Akoum", game.getState().getFaceUpPlanes().get(0).getName());
            Assert.assertEquals(info, "Plane - Agyrem", game.getState().getFaceUpPlanes().get(1).getName());
            Assert.assertTrue(info, game.getState().getFaceUpPhenomena().isEmpty());
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testPlaneTriggerStillHasStackView() {
        addPlane(playerA, Planes.PLANE_FIELDS_OF_SUMMER);
        runCode("inspect plane trigger stack view", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            PlanarCard plane = game.getState().getFaceUpPlanes().get(0);
            game.fireEvent(new GameEvent(GameEvent.EventType.CHAOS_ENSUES,
                    plane.getId(), null, player.getId()));
            game.checkStateAndTriggered();

            StackAbility trigger = getOnlyStackAbility(info, game);
            GameView view = new GameView(game.getState(), game, player.getId(), null);
            assertStackSourceView(info, view, trigger.getId(), plane.getId(),
                    plane.getName(), CardType.PLANE);
        });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void testRealityShapingResolvesOnceForEachPlayerInTurnOrder() {
        prepareStartedPlanechaseGame();
        addCard(Zone.HAND, playerA, "Soul Warden");
        addCard(Zone.HAND, playerB, "Grizzly Bears");

        setChoice(playerA, true);
        setChoice(playerB, true);
        runCode("encounter Reality Shaping", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertTrue(info, game.addPhenomenon(new RealityShapingPhenomenon(), player.getId()));
            game.checkStateAndTriggered();
            game.getStack().resolve(game);
        });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Soul Warden", 1);
        assertPermanentCount(playerB, "Grizzly Bears", 1);
        assertLife(playerA, 21);
        assertHandCount(playerA, 0);
        assertHandCount(playerB, 0);
    }

    @Test
    public void testRealityShapingIsOptionalAndOnlyAllowsPermanentCards() {
        prepareStartedPlanechaseGame();
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        addCard(Zone.HAND, playerB, "Grizzly Bears");

        setChoice(playerB, false);
        runCode("encounter Reality Shaping", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Assert.assertTrue(info, game.addPhenomenon(new RealityShapingPhenomenon(), player.getId()));
            game.checkStateAndTriggered();
            game.getStack().resolve(game);
        });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Lightning Bolt", 1);
        assertHandCount(playerB, "Grizzly Bears", 1);
        assertPermanentCount(playerB, "Grizzly Bears", 0);
    }

    @Test
    public void testRealityShapingRegistryMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Phenomena.REALITY_SHAPING));

        Assert.assertNotNull(metadata);
        Assert.assertEquals(CardType.PHENOMENON, metadata.getType());
        Assert.assertEquals("Reality Shaping", metadata.getEnglishName());
        Assert.assertEquals("Phenomenon - Reality Shaping", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertTrue(PlanarCardRegistry.create(metadata.getId()) instanceof RealityShapingPhenomenon);
    }

    private void prepareStartedPlanechaseGame() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);
    }

    private static StackAbility getOnlyStackAbility(String info, Game game) {
        Assert.assertEquals(info, 1, game.getStack().size());
        StackObject stackObject = game.getStack().getFirstOrNull();
        Assert.assertTrue(info, stackObject instanceof StackAbility);
        return (StackAbility) stackObject;
    }

    private void assertPendingStackViews(String info, Game game, UUID stackId, Phenomenon phenomenon) {
        int faceUpCount = game.getState().getFaceUpPhenomena().size();
        int planarDeckSize = game.getState().getSharedPlanarDeck().size();

        GameView playerView = new GameView(game.getState(), game, playerA.getId(), null);
        GameView spectatorView = new GameView(game.getState(), game, null, null);
        Game copiedGame = game.copy();
        GameView copiedPlayerView = new GameView(
                copiedGame.getState(), copiedGame, playerA.getId(), null);

        assertStackSourceView(info, playerView, stackId, phenomenon.getId(),
                phenomenon.getName(), CardType.PHENOMENON);
        assertStackSourceView(info, spectatorView, stackId, phenomenon.getId(),
                phenomenon.getName(), CardType.PHENOMENON);
        assertStackSourceView(info, copiedPlayerView, stackId, phenomenon.getId(),
                phenomenon.getName(), CardType.PHENOMENON);

        Assert.assertNotNull(info, game.getStack().getStackObject(stackId));
        Assert.assertEquals(info, faceUpCount, game.getState().getFaceUpPhenomena().size());
        Assert.assertEquals(info, planarDeckSize, game.getState().getSharedPlanarDeck().size());
    }

    private static void assertStackSourceView(String info, GameView gameView, UUID stackId,
                                              UUID sourceId, String sourceName, CardType sourceType) {
        CardView stackView = gameView.getStack().get(stackId);
        Assert.assertTrue(info, stackView instanceof StackAbilityView);
        CardView sourceView = ((StackAbilityView) stackView).getSourceCard();
        Assert.assertEquals(info, sourceId, sourceView.getId());
        Assert.assertEquals(info, sourceName, sourceView.getName());
        Assert.assertTrue(info, sourceView.getCardTypes().contains(sourceType));
        Assert.assertFalse(info, sourceView.getRules().isEmpty());
        Assert.assertFalse(info, sourceView.getRules().get(0).trim().isEmpty());
        Assert.assertEquals(info, "PCA", sourceView.getExpansionSetCode());
        Assert.assertTrue(info, sourceView.getMageObjectType().isUseTokensRepository());
        Assert.assertEquals(info, MageObjectType.ABILITY_STACK_FROM_TOKEN,
                sourceView.getMageObjectType());
    }
}
