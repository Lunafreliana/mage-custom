package mage.client.game;

import mage.components.ImagePanel;
import mage.components.ImagePanelStyle;
import mage.game.command.Plane;
import mage.game.command.planes.AgyremPlane;
import mage.game.command.planes.AkoumPlane;
import mage.game.command.phenomena.MutualEpiphanyPhenomenon;
import mage.view.CardView;
import mage.view.CommandObjectView;
import mage.view.PlanarCardView;
import mage.view.PlaneView;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlanechaseBackgroundTest {

    @Test
    public void defaultBackgroundFollowsPlaneChangesAndRollbackWithoutReloadingEachUpdate() {
        Harness h = new Harness(true);
        CardView akoum = view(new AkoumPlane());
        CardView agyrem = view(new AgyremPlane());
        BufferedImage firstArt = h.addArt(akoum);
        BufferedImage secondArt = h.addArt(agyrem);

        h.update(akoum);
        h.update(akoum);
        Assert.assertEquals(1, h.loads.size());
        h.loadNext();
        Assert.assertSame(firstArt, h.shown);
        h.update(akoum);
        Assert.assertTrue(h.loads.isEmpty());

        h.update(agyrem);
        Assert.assertSame(h.fallback, h.shown);
        h.loadNext();
        Assert.assertSame(secondArt, h.shown);

        h.update(akoum); // A restored game view after rollback.
        h.loadNext();
        Assert.assertSame(firstArt, h.shown);
        h.update(null);
        Assert.assertSame(h.fallback, h.shown);
    }

    @Test
    public void randomOrCustomBackgroundDoesNotLoadOrReplaceAnything() {
        // Both existing non-default choices pass enabled=false, even if their
        // selected image is missing and the theme image is used as a fallback.
        Harness h = new Harness(false);
        h.update(view(new AkoumPlane()));
        h.update(view(new AgyremPlane()));
        h.update(null);
        Assert.assertTrue(h.loads.isEmpty());
        Assert.assertEquals(0, h.displayCount);
        Assert.assertSame(h.fallback, h.shown);
    }

    @Test
    public void lateLoadCannotOverwriteTheNewPlaneOrRestoreAClearedPlane() {
        Harness h = new Harness(true);
        CardView akoum = view(new AkoumPlane());
        CardView agyrem = view(new AgyremPlane());
        h.addArt(akoum);
        BufferedImage currentArt = h.addArt(agyrem);
        h.update(akoum);
        h.update(agyrem);

        h.loads.removeLast().run(); // New Plane finishes before the old request.
        h.flushUi();
        Assert.assertSame(currentArt, h.shown);
        h.loadNext();
        Assert.assertSame(currentArt, h.shown);

        h.update(akoum);
        h.update(null);
        h.loadNext();
        Assert.assertSame(h.fallback, h.shown);
    }

    @Test
    public void missingArtFallsBackAndCanBeRetriedAfterImagesAreDownloaded() {
        Harness h = new Harness(true);
        CardView akoum = view(new AkoumPlane());
        h.update(akoum);
        h.loadNext();
        Assert.assertSame(h.fallback, h.shown);

        h.update(akoum);
        Assert.assertTrue(h.loads.isEmpty());
        BufferedImage downloadedArt = h.addArt(akoum);
        h.now += TimeUnit.SECONDS.toNanos(6);
        h.update(akoum);
        h.loadNext();
        Assert.assertSame(downloadedArt, h.shown);
    }

    @Test
    public void disposalRejectsPendingImagesAndReconnectionLoadsTheCurrentPlane() {
        Harness old = new Harness(true);
        CardView akoum = view(new AkoumPlane());
        old.addArt(akoum);
        old.update(akoum);
        old.background.dispose();
        old.flushUi();
        int displayCount = old.displayCount;
        old.loadNext();
        old.update(akoum);
        Assert.assertEquals(displayCount, old.displayCount);
        Assert.assertTrue(old.loads.isEmpty());

        Harness reconnected = new Harness(true);
        BufferedImage art = reconnected.addArt(akoum);
        reconnected.update(akoum);
        reconnected.loadNext();
        Assert.assertSame(art, reconnected.shown);
    }

    @Test
    public void selectionUsesPublicFaceUpPlanesAndIgnoresPhenomenaAndDuplicatePlayerViews() {
        Plane akoum = new AkoumPlane();
        Plane agyrem = new AgyremPlane();
        PlanarCardView phenomenon = new PlanarCardView(new MutualEpiphanyPhenomenon());
        List<CommandObjectView> commands = Arrays.asList(
                new PlaneView(akoum, null), new PlaneView(agyrem, null), new PlaneView(akoum, null));

        Assert.assertNull(PlanechaseBackground.selectPlane(Collections.emptyList(), commands));
        Assert.assertNull(PlanechaseBackground.selectPlane(Collections.singletonList(phenomenon), commands));
        Assert.assertEquals(akoum.getId(), PlanechaseBackground.selectPlane(
                Collections.singletonList(new PlanarCardView(akoum)), commands).getId());
        Assert.assertEquals(agyrem.getId(), PlanechaseBackground.selectPlane(Arrays.asList(
                new PlanarCardView(akoum), new PlanarCardView(agyrem), phenomenon), commands).getId());
    }

    @Test
    public void fullPlaneBackgroundPreservesCardAndLeavesBlackRightAndHandAreas() {
        // Match the actual Plane image size used by the client download path.
        BufferedImage landscape = new BufferedImage(936, 672, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = landscape.createGraphics();
        try {
            graphics.setColor(Color.MAGENTA); // title/frame
            graphics.fillRect(0, 0, 936, 672);
            graphics.setColor(Color.GREEN); // illustration
            graphics.fillRect(70, 100, 796, 340);
            graphics.setColor(Color.YELLOW); // type/rules/chaos text area
            graphics.fillRect(0, 500, 936, 172);
        } finally {
            graphics.dispose();
        }

        BufferedImage portrait = new BufferedImage(672, 936, BufferedImage.TYPE_INT_RGB);
        // The stored scan is counterclockwise relative to its readable landscape view.
        for (int x = 0; x < landscape.getWidth(); x++) {
            for (int y = 0; y < landscape.getHeight(); y++) {
                portrait.setRGB(y, landscape.getWidth() - 1 - x, landscape.getRGB(x, y));
            }
        }

        BufferedImage prepared = PlanechaseBackground.prepareArtwork(landscape);
        BufferedImage rotatedPrepared = PlanechaseBackground.prepareArtwork(portrait);
        Assert.assertEquals(936, prepared.getWidth());
        Assert.assertEquals(672, prepared.getHeight());
        Assert.assertEquals(ImagePanel.IMAGE_LAYOUT_FIT_TOP_LEFT,
                prepared.getProperty(ImagePanel.IMAGE_LAYOUT_PROPERTY));
        Assert.assertEquals(0.75f,
                ((Number) prepared.getProperty(ImagePanel.IMAGE_MAX_HEIGHT_RATIO_PROPERTY)).floatValue(), 0.0f);
        Assert.assertArrayEquals(
                prepared.getRGB(0, 0, prepared.getWidth(), prepared.getHeight(), null, 0, prepared.getWidth()),
                rotatedPrepared.getRGB(0, 0, rotatedPrepared.getWidth(), rotatedPrepared.getHeight(), null, 0, rotatedPrepared.getWidth()));

        // The former crop discarded the frame/title/rules area. All of it must now survive.
        Color title = new Color(prepared.getRGB(0, 0), true);
        Color art = new Color(prepared.getRGB(100, 150), true);
        Color rules = new Color(prepared.getRGB(100, 600), true);
        Assert.assertTrue(title.getRed() > 0 && title.getBlue() > 0);
        Assert.assertTrue(art.getGreen() > 0);
        Assert.assertTrue(rules.getRed() > 0 && rules.getGreen() > 0);

        // Even though the hosting ImagePanel normally uses COVER, Plane backgrounds
        // opt into native-size top-left rendering with intentional black fill.
        ImagePanel panel = new ImagePanel(prepared, ImagePanelStyle.COVER);
        panel.setSize(1600, 1000);
        BufferedImage rendered = new BufferedImage(1600, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D renderedGraphics = rendered.createGraphics();
        try {
            panel.paint(renderedGraphics);
        } finally {
            renderedGraphics.dispose();
        }

        Assert.assertNotEquals(Color.BLACK.getRGB(), rendered.getRGB(935, 671));
        Assert.assertEquals(Color.BLACK.getRGB(), rendered.getRGB(936, 671));
        Assert.assertEquals(Color.BLACK.getRGB(), rendered.getRGB(1200, 100));
        Assert.assertEquals(Color.BLACK.getRGB(), rendered.getRGB(100, 800));
        Assert.assertNull(PlanechaseBackground.prepareArtwork(null));
    }

    private static CardView view(Plane plane) {
        return new CardView(new PlaneView(plane, null));
    }

    private static final class Harness {
        private final BufferedImage fallback = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        private final Map<UUID, BufferedImage> images = new HashMap<>();
        private final Deque<Runnable> loads = new ArrayDeque<>();
        private final Deque<Runnable> ui = new ArrayDeque<>();
        private final PlanechaseBackground background;
        private BufferedImage shown = fallback;
        private long now;
        private int displayCount;

        private Harness(boolean enabled) {
            background = new PlanechaseBackground(enabled, fallback, image -> {
                shown = image;
                displayCount++;
            }, card -> images.get(card.getId()), loads::addLast, ui::addLast, () -> now);
        }

        private BufferedImage addArt(CardView card) {
            BufferedImage art = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            images.put(card.getId(), art);
            return art;
        }

        private void update(CardView plane) {
            background.updatePlane(plane);
            flushUi();
        }

        private void loadNext() {
            loads.removeFirst().run();
            flushUi();
        }

        private void flushUi() {
            while (!ui.isEmpty()) {
                ui.removeFirst().run();
            }
        }
    }
}
