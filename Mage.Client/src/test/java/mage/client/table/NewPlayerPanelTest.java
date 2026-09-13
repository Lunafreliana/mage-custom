package mage.client.table;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFileChooser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NewPlayerPanelTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void approvedFileUpdatesDeckPathAndContainingDirectory() throws Exception {
        File directory = temporaryFolder.newFolder("decks");
        File deck = new File(directory, "test.dck");
        AtomicReference<String> deckPath = new AtomicReference<>();
        AtomicReference<String> lastFolder = new AtomicReference<>();

        NewPlayerPanel.processDeckSelection(
                JFileChooser.APPROVE_OPTION, deck, deckPath::set, lastFolder::set
        );

        assertEquals(deck.getPath(), deckPath.get());
        assertEquals(directory.getCanonicalPath(), lastFolder.get());
    }

    @Test
    public void approvedNullFileDoesNotUpdateAnything() throws Exception {
        assertSelectionIgnored(JFileChooser.APPROVE_OPTION, null);
    }

    @Test
    public void cancelledSelectionDoesNotUpdateAnything() throws Exception {
        assertSelectionIgnored(JFileChooser.CANCEL_OPTION, new File("ignored.dck"));
    }

    private static void assertSelectionIgnored(int result, File file) throws Exception {
        AtomicReference<String> deckPath = new AtomicReference<>();
        AtomicReference<String> lastFolder = new AtomicReference<>();

        NewPlayerPanel.processDeckSelection(result, file, deckPath::set, lastFolder::set);

        assertNull(deckPath.get());
        assertNull(lastFolder.get());
    }
}
