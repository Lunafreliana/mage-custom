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
import mage.constants.Planes;
import mage.game.Game;
import mage.game.GameState;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.SupplementalDeckRuntimeHandler;
import mage.game.command.SupplementalDeckRuntimeHandlers;
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
