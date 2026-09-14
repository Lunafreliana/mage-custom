package mage.client.game;

import mage.components.ImagePanel;
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
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Hashtable;
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
    private static final float BACKGROUND_DARKEN_ALPHA = 0.25f;
    private static final float BACKGROUND_MAX_HEIGHT_RATIO = 0.75f;

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
        return prepareArtwork(ImageCache.loadImage(ImageCache.getTFile(path)));
    }

    static BufferedImage prepareArtwork(BufferedImage image) {
        if (image == null) {
            return null;
        }

        // Planar scans may be stored in portrait orientation. Match BigCard's
        // clockwise landscape presentation, but keep the complete card frame,
        // title and rules text instead of cropping down to just the illustration.
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

        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(ImagePanel.IMAGE_LAYOUT_PROPERTY, ImagePanel.IMAGE_LAYOUT_FIT_TOP_LEFT);
        properties.put(ImagePanel.IMAGE_MAX_HEIGHT_RATIO_PROPERTY, BACKGROUND_MAX_HEIGHT_RATIO);
        BufferedImage background = new BufferedImage(
                image.getColorModel(), image.copyData(null), image.isAlphaPremultiplied(), properties);

        // Slightly darken the Plane so cards, targeting overlays and client text stay
        // readable while the Plane's own name and rules text remain visible.
        Graphics2D graphics = background.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.SrcOver.derive(BACKGROUND_DARKEN_ALPHA));
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, background.getWidth(), background.getHeight());
        } finally {
            graphics.dispose();
        }
        return background;
    }
}
