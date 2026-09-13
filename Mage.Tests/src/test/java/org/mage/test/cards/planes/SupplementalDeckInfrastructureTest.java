package org.mage.test.cards.planes;

import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.decks.Constructed;
import mage.cards.decks.Deck;
import mage.cards.decks.PlanarDeckCard;
import mage.cards.decks.SupplementalDeckCard;
import mage.cards.decks.SupplementalDeckPartition;
import mage.cards.decks.SupplementalDeckType;
import mage.constants.CardType;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.game.Game;
import mage.game.GameState;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.SupplementalDeckRuntimeHandler;
import mage.game.command.SupplementalDeckRuntimeHandlers;
import mage.view.CardView;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SupplementalDeckInfrastructureTest {

    @Test
    public void planarCardsAreRejectedInMainAndExtractedFromPregame() {
        Deck deck = new Deck();
        PlanarDeckCard illegal = new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM));
        PlanarDeckCard legal = new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_FIELDS_OF_SUMMER));
        deck.getCards().add(illegal);
        deck.getSideboard().add(legal);

        SupplementalDeckPartition partition = deck.partitionSupplementalDecks();

        Assert.assertEquals(Collections.singletonList(illegal), partition.getIllegalMain());
        Assert.assertTrue(deck.getSideboard().isEmpty());
        Assert.assertEquals(Collections.singletonList(legal),
                deck.getSupplementalDeck(SupplementalDeckType.PLANAR));
    }

    @Test
    public void illegalMainPlacementUsesNormalDeckValidatorErrors() {
        Deck deck = new Deck();
        PlanarDeckCard illegal = new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM));
        deck.getCards().add(illegal);
        Constructed validator = new EmptyConstructedValidator();

        Assert.assertFalse(validator.validate(deck));
        Assert.assertTrue(validator.errorsListContainsGroup(illegal.getName()));
        Assert.assertTrue(validator.getErrorsListInfo()
                .contains("Supplemental cards cannot be placed in the main deck"));
    }

    @Test
    public void manyPlanarCardsDoNotLeakIntoSideboard() {
        Deck deck = new Deck();
        for (int i = 0; i < 25; i++) {
            deck.getSideboard().add(new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM)));
        }

        deck.partitionSupplementalDecks();

        Assert.assertEquals(25, deck.getSupplementalDeck(SupplementalDeckType.PLANAR).size());
        Assert.assertEquals(0, deck.getSideboard().size());
    }

    @Test
    public void commanderCompanionAndPlanarPregameEntriesCoexist() {
        Deck deck = new Deck();
        Card commander = new OrdinaryPregameCard("Commander");
        Card companion = new OrdinaryPregameCard("Companion");
        deck.getSideboard().add(commander);
        deck.getSideboard().add(companion);
        deck.getSideboard().add(new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM)));

        SupplementalDeckPartition partition = deck.partitionSupplementalDecks();

        Assert.assertEquals(2, partition.getOrdinaryPregame().size());
        Assert.assertTrue(deck.getSideboard().contains(commander));
        Assert.assertTrue(deck.getSideboard().contains(companion));
        Assert.assertEquals(1, deck.getSupplementalDeck(SupplementalDeckType.PLANAR).size());
    }

    @Test
    public void genericDispatchSupportsASecondSupplementalType() {
        SupplementalDeckRuntimeHandlers handlers = new SupplementalDeckRuntimeHandlers();
        RecordingHandler attractionHandler = new RecordingHandler();
        handlers.register(attractionHandler);
        SupplementalDeckCard attraction = new SyntheticSupplementalCard("attraction:test");

        handlers.initialize(SupplementalDeckType.ATTRACTION, UUID.randomUUID(),
                Collections.singletonList(attraction), null);

        Assert.assertEquals(Collections.singletonList(attraction), attractionHandler.cards);
    }

    @Test
    public void perPlayerPlanarDecksCopyIndependently() {
        GameState state = new GameState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        PlanarCard akoum = PlanarCardRegistry.create(PlanarCardRegistry.getId(Planes.PLANE_AKOUM));
        PlanarCard summer = PlanarCardRegistry.create(PlanarCardRegistry.getId(Planes.PLANE_FIELDS_OF_SUMMER));
        state.setPlayerPlanarDeck(first, Collections.singletonList(akoum), false);
        state.setPlayerPlanarDeck(second, Collections.singletonList(summer), false);

        GameState copy = state.copy();
        copy.getPlayerPlanarDeck(first).draw();

        Assert.assertEquals(1, state.getPlayerPlanarDeck(first).size());
        Assert.assertEquals(0, copy.getPlayerPlanarDeck(first).size());
        Assert.assertEquals(1, copy.getPlayerPlanarDeck(second).size());
    }

    @Test
    public void planarCarrierRoundTripsStableRegistryIdentity() {
        String registryId = PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY);

        PlanarDeckCard card = PlanarDeckCard.create("PCA", registryId);

        Assert.assertNotNull(card);
        Assert.assertEquals(SupplementalDeckType.PLANAR, card.getSupplementalDeckType());
        Assert.assertEquals(registryId, card.getSupplementalDeckId());
        Assert.assertEquals(registryId, card.getCardNumber());
        Assert.assertTrue(card.getCardTypeForDeckbuilding().contains(CardType.PHENOMENON));
    }

    @Test
    public void planeCarrierExposesRuntimeRulesIncludingChaosWithoutArtwork() {
        PlanarDeckCard card = new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM));
        CardView view = new CardView(card);
        String rules = String.join(" ", view.getRules()).toLowerCase();

        Assert.assertEquals("Akoum", view.getName());
        Assert.assertTrue(view.getCardTypes().contains(CardType.PLANE));
        Assert.assertTrue(view.isToRotate());
        Assert.assertTrue(rules.contains("enchantment spells"));
        Assert.assertTrue(rules.contains("chaos ensues"));
        Assert.assertTrue(rules.contains("isn't enchanted"));
    }

    @Test
    public void phenomenonCarrierExposesEncounterRulesWithoutArtwork() {
        PlanarDeckCard card = new PlanarDeckCard(PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY));
        CardView view = new CardView(card);
        String rules = String.join(" ", view.getRules()).toLowerCase();

        Assert.assertEquals("Mutual Epiphany", view.getName());
        Assert.assertTrue(view.getCardTypes().contains(CardType.PHENOMENON));
        Assert.assertTrue(view.isToRotate());
        Assert.assertTrue(rules.contains("encounter mutual epiphany"));
        Assert.assertTrue(rules.contains("each player draws four cards"));
    }

    @Test
    public void planarCarrierRejectsWrongSetAndUnknownIdentity() {
        String registryId = PlanarCardRegistry.getId(Planes.PLANE_AKOUM);

        Assert.assertNull(PlanarDeckCard.create("NOT-PCA", registryId));
        Assert.assertNull(PlanarDeckCard.create("PCA", "plane:not_registered"));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new PlanarDeckCard("plane:not_registered"));
    }

    @Test
    public void planarCarrierCopyPreservesIdentityWithoutSharingObject() {
        PlanarDeckCard original = new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM));

        PlanarDeckCard copy = (PlanarDeckCard) original.copy();

        Assert.assertNotSame(original, copy);
        Assert.assertEquals(original.getId(), copy.getId());
        Assert.assertEquals(original.getSupplementalDeckId(), copy.getSupplementalDeckId());
    }

    @Test
    public void partitionViewsAreImmutable() {
        PlanarDeckCard planar = new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM));
        SupplementalDeckPartition partition = SupplementalDeckPartition.create(
                Collections.singletonList(planar), Collections.singletonList(new OrdinaryPregameCard("Commander")));

        Assert.assertThrows(UnsupportedOperationException.class,
                () -> partition.getIllegalMain().clear());
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> partition.getOrdinaryPregame().clear());
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> partition.get(SupplementalDeckType.PLANAR).clear());
    }

    @Test
    public void absentSupplementalTypeReturnsAnImmutableEmptyView() {
        SupplementalDeckPartition partition = SupplementalDeckPartition.create(
                Collections.emptyList(), Collections.emptyList());

        Assert.assertTrue(partition.get(SupplementalDeckType.ATTRACTION).isEmpty());
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> partition.get(SupplementalDeckType.ATTRACTION).add(
                        new SyntheticSupplementalCard("attraction:test")));
    }

    @Test
    public void gameStateExposesAnImmutablePlayerDeckMap() {
        GameState state = new GameState();
        UUID playerId = UUID.randomUUID();
        state.setPlayerPlanarDeck(playerId, Collections.singletonList(
                PlanarCardRegistry.create(PlanarCardRegistry.getId(Planes.PLANE_AKOUM))), false);

        Assert.assertThrows(UnsupportedOperationException.class,
                () -> state.getPlayerPlanarDecks().clear());
    }

    @Test
    public void replacingOnePlayersDeckDoesNotAffectAnotherPlayersDeck() {
        GameState state = new GameState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        state.setPlayerPlanarDeck(first, Collections.singletonList(
                PlanarCardRegistry.create(PlanarCardRegistry.getId(Planes.PLANE_AKOUM))), false);
        state.setPlayerPlanarDeck(second, Collections.singletonList(
                PlanarCardRegistry.create(PlanarCardRegistry.getId(Planes.PLANE_BANT))), false);

        state.setPlayerPlanarDeck(first, Collections.singletonList(
                PlanarCardRegistry.create(PlanarCardRegistry.getId(Planes.PLANE_NAYA))), false);

        Assert.assertEquals(1, state.getPlayerPlanarDeck(first).size());
        Assert.assertEquals(1, state.getPlayerPlanarDeck(second).size());
        Assert.assertNotEquals(state.getPlayerPlanarDeck(first).getOrder(),
                state.getPlayerPlanarDeck(second).getOrder());
    }

    private static final class RecordingHandler implements SupplementalDeckRuntimeHandler {
        private List<SupplementalDeckCard> cards;

        @Override
        public SupplementalDeckType getType() {
            return SupplementalDeckType.ATTRACTION;
        }

        @Override
        public void initialize(UUID playerId, List<SupplementalDeckCard> cards, Game game) {
            this.cards = cards;
        }
    }

    private static final class EmptyConstructedValidator extends Constructed {
        private EmptyConstructedValidator() {
            super("Supplemental test");
        }

        @Override
        public int getDeckMinSize() {
            return 0;
        }
    }

    private static final class SyntheticSupplementalCard extends CardImpl implements SupplementalDeckCard {
        private final String supplementalId;

        private SyntheticSupplementalCard(String supplementalId) {
            super(null, "Synthetic Attraction");
            this.supplementalId = supplementalId;
            this.cardType.add(CardType.ARTIFACT);
        }

        private SyntheticSupplementalCard(final SyntheticSupplementalCard card) {
            super(card);
            this.supplementalId = card.supplementalId;
        }

        @Override
        public SupplementalDeckType getSupplementalDeckType() {
            return SupplementalDeckType.ATTRACTION;
        }

        @Override
        public String getSupplementalDeckId() {
            return supplementalId;
        }

        @Override
        public Card copy() {
            return new SyntheticSupplementalCard(this);
        }
    }

    private static final class OrdinaryPregameCard extends CardImpl {
        private OrdinaryPregameCard(String name) {
            super(null, name);
            this.cardType.add(CardType.CREATURE);
        }

        private OrdinaryPregameCard(final OrdinaryPregameCard card) {
            super(card);
        }

        @Override
        public Card copy() {
            return new OrdinaryPregameCard(this);
        }
    }
}
