package mage.client.game;

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
    public void artworkCropAcceptsBothOrientationsAndExcludesTheCardFrame() {
        BufferedImage landscape = new BufferedImage(1000, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = landscape.createGraphics();
        try {
            graphics.setColor(Color.MAGENTA); // Title, frame and rules are not artwork.
            graphics.fillRect(0, 0, 1000, 700);
            graphics.setColor(Color.GREEN);
            graphics.fillRect(70, 110, 860, 300);
            graphics.setColor(Color.BLUE);
            graphics.fillRect(480, 240, 40, 40);
        } finally {
            graphics.dispose();
        }
        BufferedImage portrait = new BufferedImage(700, 1000, BufferedImage.TYPE_INT_RGB);
        // The stored scan is counterclockwise relative to its readable landscape view.
        for (int x = 0; x < landscape.getWidth(); x++) {
            for (int y = 0; y < landscape.getHeight(); y++) {
                portrait.setRGB(y, landscape.getWidth() - 1 - x, landscape.getRGB(x, y));
            }
        }

        BufferedImage cropped = PlanechaseBackground.cropArtwork(landscape);
        BufferedImage rotatedCrop = PlanechaseBackground.cropArtwork(portrait);
        Assert.assertEquals(cropped.getWidth(), rotatedCrop.getWidth());
        Assert.assertEquals(cropped.getHeight(), rotatedCrop.getHeight());
        Assert.assertEquals(Color.GREEN.getRGB(), cropped.getRGB(0, 0));
        Assert.assertEquals(Color.GREEN.getRGB(), cropped.getRGB(cropped.getWidth() - 1, cropped.getHeight() - 1));
        Assert.assertEquals(Color.BLUE.getRGB(), cropped.getRGB(cropped.getWidth() / 2, cropped.getHeight() / 2));
        Assert.assertArrayEquals(cropped.getRGB(0, 0, cropped.getWidth(), cropped.getHeight(), null, 0, cropped.getWidth()),
                rotatedCrop.getRGB(0, 0, rotatedCrop.getWidth(), rotatedCrop.getHeight(), null, 0, rotatedCrop.getWidth()));
        Assert.assertEquals(Color.MAGENTA.getRGB(), landscape.getRGB(0, 0));
        Assert.assertNull(PlanechaseBackground.cropArtwork(null));
        Assert.assertNotNull(PlanechaseBackground.cropArtwork(new BufferedImage(1, 2, BufferedImage.TYPE_INT_RGB)));
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
