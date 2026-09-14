package org.mage.test.decks.importer;

import mage.cards.decks.Deck;
import mage.cards.decks.DeckCardLists;
import mage.cards.decks.DeckCardInfo;
import mage.cards.decks.DeckCardLayout;
import mage.cards.decks.PlanarDeckCard;
import mage.cards.decks.SupplementalDeckCard;
import mage.cards.decks.exporter.XmageDeckExporter;
import mage.cards.decks.importer.CardLookup;
import mage.cards.decks.importer.DckDeckImporter;
import mage.constants.Planes;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DckDeckImportTest {

    private static final FakeCardLookup LOOKUP = new FakeCardLookup();

    @Test
    public void testImport() {
        StringBuilder errors = new StringBuilder();
        DckDeckImporter importer = new DckDeckImporter() {
            @Override
            public CardLookup getCardLookup() {
                return LOOKUP;
            }
        };
        DeckCardLists deck = importer.importDeck(
                Paths.get("src", "test", "data", "importer", "testdeck.dck").toString(),
                errors,
                false
        );

        Assert.assertEquals("", errors.toString());
        TestDeckChecker.checker()
                .addMain("Ugin, the Ineffable", 1)
                .addMain("Cephalid Looter", 1)
                .addMain("Adventure Awaits", 1)
                .addMain("Acquisitions Expert", 1)
                .addSide("Archon of Emeria", 3)
                .addSide("Akoum Hellhound", 1)
                .verify(deck, 4, 4);
    }

    @Test
    public void planarCardRoundTripsThroughPregameStorage() throws Exception {
        Path file = Files.createTempFile("planar-deck", ".dck");
        try {
            Files.write(file, ("SB: 1 [PCA:plane:plane_akoum] Akoum" + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
            StringBuilder errors = new StringBuilder();
            DeckCardLists cardLists = new DckDeckImporter().importDeck(file.toString(), errors, false);

            Assert.assertEquals("", errors.toString());
            Assert.assertTrue(cardLists.getCards().isEmpty());
            Assert.assertEquals(1, cardLists.getSideboard().size());
            Assert.assertEquals("plane:plane_akoum", cardLists.getSideboard().get(0).getCardNumber());

            Deck deck = Deck.load(cardLists, false, true);
            Assert.assertEquals(1, deck.getSideboard().size());
            Assert.assertTrue(deck.getSideboard().iterator().next() instanceof SupplementalDeckCard);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void sideboardLayoutWithPlanarCardsRoundTripsWithoutMismatch() throws Exception {
        DeckCardInfo ordinaryCard = new DeckCardInfo("Akoum Hellhound", "133", "ZNR");
        DeckCardInfo akoum = planarCardInfo(Planes.PLANE_AKOUM);
        DeckCardInfo agyrem = planarCardInfo(Planes.PLANE_AGYREM);
        DeckCardInfo academy = planarCardInfo(Planes.PLANE_ACADEMY_AT_TOLARIA_WEST);
        DeckCardLists original = new DeckCardLists();
        original.getSideboard().addAll(Arrays.asList(ordinaryCard, akoum, agyrem, academy));
        original.setCardLayout(new DeckCardLayout(new ArrayList<>(), "(NONE,false,50)"));
        original.setSideboardLayout(new DeckCardLayout(
                Collections.singletonList(Collections.singletonList(
                        Arrays.asList(ordinaryCard, akoum, agyrem, academy)
                )),
                "(NONE,false,50)"
        ));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new XmageDeckExporter().writeDeck(output, original);
        Path file = Files.createTempFile("planar-layout", ".dck");
        try {
            Files.write(file, output.toByteArray());
            StringBuilder errors = new StringBuilder();
            DeckCardLists imported = new DckDeckImporter().importDeck(file.toString(), errors, false);

            Assert.assertEquals("", errors.toString());
            Assert.assertEquals(4, imported.getSideboard().size());
            Assert.assertNotNull(imported.getSideboardLayout());
            Assert.assertEquals(4, imported.getSideboardLayout().getCards().get(0).get(0).size());
            Assert.assertEquals("plane:plane_akoum",
                    imported.getSideboardLayout().getCards().get(0).get(0).get(1).getCardNumber());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static DeckCardInfo planarCardInfo(Planes plane) {
        PlanarDeckCard card = new PlanarDeckCard(PlanarCardRegistry.getId(plane));
        return new DeckCardInfo(card.getName(), card.getCardNumber(), card.getExpansionSetCode());
    }

    @Test
    public void planarCardInMainIsNotSilentlyReroutedOnImport() throws Exception {
        Path file = Files.createTempFile("illegal-planar-main", ".dck");
        try {
            Files.write(file, ("1 [PCA:plane:plane_akoum] Akoum" + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
            DeckCardLists cardLists = new DckDeckImporter()
                    .importDeck(file.toString(), new StringBuilder(), false);

            Assert.assertEquals(1, cardLists.getCards().size());
            Assert.assertTrue(cardLists.getSideboard().isEmpty());
            Assert.assertTrue(Deck.load(cardLists, false, true).getCards().iterator().next()
                    instanceof SupplementalDeckCard);
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
