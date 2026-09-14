package mage.client.game;

import mage.view.CardView;
import mage.view.CommandObjectView;
import mage.view.GameView;
import mage.view.PlanarCardView;
import mage.view.PlaneView;
import org.apache.log4j.Logger;
import org.mage.card.arcane.Util;
import org.mage.plugins.card.images.ImageCache;
import org.mage.plugins.card.utils.CardImageUtils;

import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

/** Client-only artwork for games using the default battlefield background. */
final class PlanechaseBackground {

    private static final Logger LOGGER = Logger.getLogger(PlanechaseBackground.class);
    private static final long MISSING_IMAGE_RETRY_NANOS = TimeUnit.SECONDS.toNanos(5);

    private final boolean enabled;
    private final BufferedImage defaultImage;
    private final Consumer<BufferedImage> displayImage;
    private final Function<CardView, BufferedImage> loadImage;
    private final Executor imageExecutor;
    private final Executor uiExecutor;
    private final LongSupplier nanoTime;

    // Only accessed through uiExecutor (the Swing event dispatch thread in the client).
    private String requestedImage;
    private long requestVersion;
    private long nextRetryNanos;
    private boolean loading;
    private boolean hasImage;
    private boolean disposed;

    PlanechaseBackground(boolean enabled, BufferedImage defaultImage, Consumer<BufferedImage> displayImage) {
        this(enabled, defaultImage, displayImage, PlanechaseBackground::loadPlaneArtwork,
                Util.threadPool, SwingUtilities::invokeLater, System::nanoTime);
    }

    PlanechaseBackground(boolean enabled, BufferedImage defaultImage, Consumer<BufferedImage> displayImage,
                        Function<CardView, BufferedImage> loadImage, Executor imageExecutor,
                        Executor uiExecutor, LongSupplier nanoTime) {
        this.enabled = enabled;
        this.defaultImage = defaultImage;
        this.displayImage = displayImage;
        this.loadImage = loadImage;
        this.imageExecutor = imageExecutor;
        this.uiExecutor = uiExecutor;
        this.nanoTime = nanoTime;
    }

    void update(GameView game) {
        if (!enabled) {
            return;
        }
        List<CommandObjectView> commandObjects = game.getPlayers().stream()
                .flatMap(player -> player.getCommandObjectList().stream())
                .collect(Collectors.toList());
        updatePlane(selectPlane(game.getFaceUpPlanarCards(), commandObjects));
    }

    static CardView selectPlane(List<PlanarCardView> faceUpCards, Collection<CommandObjectView> commandObjects) {
        // Multiple Planes may be face up. Use the last Plane in the public view's order
        // for decoration only; all of them remain active rules objects in command.
        for (int i = faceUpCards.size() - 1; i >= 0; i--) {
            for (CommandObjectView object : commandObjects) {
                if (object instanceof PlaneView && object.getId().equals(faceUpCards.get(i).getId())) {
                    return new CardView((PlaneView) object);
                }
            }
        }
        return null;
    }

    void updatePlane(CardView plane) {
        if (enabled) {
            uiExecutor.execute(() -> updateOnUi(plane));
        }
    }

    private void updateOnUi(CardView plane) {
        if (disposed) {
            return;
        }
        String imageKey = plane == null ? null : plane.getId() + "#" + plane.getName()
                + "#" + plane.getExpansionSetCode() + "#" + plane.getImageFileName()
                + "#" + plane.getImageNumber();
        boolean changed = !Objects.equals(requestedImage, imageKey);
        if (changed) {
            requestedImage = imageKey;
            ++requestVersion;
            loading = false;
            hasImage = false;
            nextRetryNanos = 0;
            // Never leave artwork for a Plane that is no longer selected on screen.
            displayImage.accept(defaultImage);
        }
        if (plane == null || loading || hasImage || (!changed && nanoTime.getAsLong() < nextRetryNanos)) {
            return;
        }

        loading = true;
        final long version = ++requestVersion;
        imageExecutor.execute(() -> {
            BufferedImage artwork = null;
            try {
                artwork = loadImage.apply(plane);
            } catch (Exception e) {
                LOGGER.warn("Unable to load Planechase background for " + plane.getName(), e);
            }
            final BufferedImage result = artwork;
            uiExecutor.execute(() -> {
                if (disposed || version != requestVersion) {
                    return;
                }
                loading = false;
                hasImage = result != null;
                nextRetryNanos = nanoTime.getAsLong() + MISSING_IMAGE_RETRY_NANOS;
                displayImage.accept(result == null ? defaultImage : result);
            });
        });
    }

    void dispose() {
        uiExecutor.execute(() -> {
            disposed = true;
            ++requestVersion;
        });
    }

    private static BufferedImage loadPlaneArtwork(CardView plane) {
        // Use the same token-repository file as the command-zone card, including
        // zipped image folders. Read it directly so a missing image cannot resolve
        // to a card back or remain in a negative cache after images are downloaded.
        String path = CardImageUtils.buildImagePathToCardView(plane);
        return cropArtwork(ImageCache.loadImage(ImageCache.getTFile(path)));
    }

    static BufferedImage cropArtwork(BufferedImage image) {
        if (image == null) {
            return null;
        }
        // Planar scans are commonly stored in portrait orientation. Match BigCard's
        // clockwise landscape presentation, while accepting already-landscape scans.
        if (image.getWidth() < image.getHeight()) {
            BufferedImage landscape = new BufferedImage(image.getHeight(), image.getWidth(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = landscape.createGraphics();
            try {
                graphics.translate(landscape.getWidth(), 0);
                graphics.rotate(Math.PI / 2);
                graphics.drawImage(image, 0, 0, null);
            } finally {
                graphics.dispose();
            }
            image = landscape;
        }
        // The upper illustration inside the standard planar frame, without its
        // title, type line or rules box. Relative bounds also work for small scans.
        int x = (int) (image.getWidth() * 0.08);
        int y = (int) (image.getHeight() * 0.18);
        int width = Math.max(1, (int) (image.getWidth() * 0.84));
        int height = Math.max(1, (int) (image.getHeight() * 0.38));
        return image.getSubimage(x, y, width, height);
    }
}
