package org.mage.test.decks.exporter;

import mage.cards.decks.Deck;
import mage.cards.decks.DeckCardInfo;
import mage.cards.decks.DeckCardLists;
import mage.cards.decks.PlanarDeckCard;
import mage.cards.decks.exporter.DeckExporter;
import mage.cards.decks.exporter.XmageDeckExporter;
import mage.constants.Planes;
import mage.game.command.PlanarCardRegistry;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class XmageDeckExporterTest {

    @Test
    public void writeDeck() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeckCardLists deck = new DeckCardLists();
        deck.getCards().add(new DeckCardInfo("Forest", "1", "RNA", 2));
        deck.getCards().add(new DeckCardInfo("Plains", "2", "RNA", 3));
        deck.getCards().add(new DeckCardInfo("Plains", "2", "RNA", 5)); // must combine
        deck.getCards().add(new DeckCardInfo("Mountain", "3", "RNA", 1));
        deck.getSideboard().add(new DeckCardInfo("Island", "1", "RNA", 2));
        deck.getSideboard().add(new DeckCardInfo("Island", "1", "RNA", 5)); // must combine
        deck.getSideboard().add(new DeckCardInfo("Mountain", "2", "RNA", 3));
        DeckExporter exporter = new XmageDeckExporter();
        exporter.writeDeck(baos, deck);
        assertEquals("2 [RNA:1] Forest" + System.lineSeparator() +
                        "8 [RNA:2] Plains" + System.lineSeparator() +
                        "1 [RNA:3] Mountain" + System.lineSeparator() +
                        "SB: 7 [RNA:1] Island" + System.lineSeparator() +
                        "SB: 3 [RNA:2] Mountain" + System.lineSeparator(),
                baos.toString());
    }

    @Test
    public void writePlanarDeckInPregameSection() {
        Deck deck = new Deck();
        deck.getSideboard().add(new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        new XmageDeckExporter().writeDeck(baos, deck.prepareCardsOnlyDeck());

        assertEquals("SB: 1 [PCA:plane:plane_akoum] Akoum" + System.lineSeparator(), baos.toString());
    }

}
