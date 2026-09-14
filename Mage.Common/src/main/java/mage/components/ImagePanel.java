package mage.components;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * GUI component: JPanel with background image
 */
public class ImagePanel extends JPanel {

    /** Optional BufferedImage property used by special backgrounds that must not be cropped or upscaled. */
    public static final String IMAGE_LAYOUT_PROPERTY = "xmage.imagePanel.layout";
    public static final String IMAGE_LAYOUT_FIT_TOP_LEFT = "fit-top-left";
    public static final String IMAGE_MAX_HEIGHT_RATIO_PROPERTY = "xmage.imagePanel.maxHeightRatio";

    private BufferedImage image;
    private ImagePanelStyle style;
    private float alignmentX = 0.5f;
    private float alignmentY = 0.5f;

    public ImagePanel(BufferedImage image) {
        this(image, ImagePanelStyle.TILED);
    }

    public ImagePanel(BufferedImage image, ImagePanelStyle style) {
        this.image = image;
        this.style = style;
        setLayout(new BorderLayout());

        setOpaque(true);
    }

    /** Replace the background on the Swing event dispatch thread. */
    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    public void setImageAlignmentX(float alignmentX) {
        this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;
    }

    public void setImageAlignmentY(float alignmentY) {
        this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;

    }

    public void add(JComponent component) {
        add(component, null);
    }

    public void add(JComponent component, Object constraints) {
        component.setOpaque(false);

        if (component instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) component;
            JViewport viewport = scrollPane.getViewport();
            viewport.setOpaque(false);
            Component c = viewport.getView();

            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(false);
            }
        }

        super.add(component, constraints);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null)
            return;

        if (IMAGE_LAYOUT_FIT_TOP_LEFT.equals(image.getProperty(IMAGE_LAYOUT_PROPERTY))) {
            drawFitTopLeft(g);
            return;
        }

        switch (style) {
            case TILED:
                drawTiled(g);
                break;
            case SCALED:
                Dimension d = getSize();
                g.drawImage(image, 0, 0, d.width, d.height, null);
                break;
            case ACTUAL:
                drawActual(g);
                break;
            case COVER:
                drawCover(g);
                break;
        }
    }

    private void drawTiled(Graphics g) {
        Dimension d = getSize();
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        for (int x = 0; x < d.width; x += width) {
            for (int y = 0; y < d.height; y += height) {
                g.drawImage(image, x, y, null, null);
            }
        }
    }

    private void drawActual(Graphics g) {
        Dimension d = getSize();
        float x = (d.width - image.getWidth(null)) * alignmentX;
        float y = (d.height - image.getHeight(null)) * alignmentY;
        g.drawImage(image, (int) x, (int) y, this);
    }

    private void drawCover(Graphics g) {
        Dimension d = getSize();
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);

        // Calculate scale to cover the entire panel while maintaining aspect ratio
        double scaleX = (double) d.width / imageWidth;
        double scaleY = (double) d.height / imageHeight;
        double scale = Math.max(scaleX, scaleY);

        // Calculate the scaled dimensions
        int scaledWidth = (int) (imageWidth * scale);
        int scaledHeight = (int) (imageHeight * scale);

        // Center the image
        int x = (d.width - scaledWidth) / 2;
        int y = (d.height - scaledHeight) / 2;

        g.drawImage(image, x, y, scaledWidth, scaledHeight, null);
    }

    private void drawFitTopLeft(Graphics g) {
        Dimension d = getSize();
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);

        Graphics2D graphics = (Graphics2D) g.create();
        try {
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, d.width, d.height);

            double maxHeightRatio = 1.0;
            Object maxHeightProperty = image.getProperty(IMAGE_MAX_HEIGHT_RATIO_PROPERTY);
            if (maxHeightProperty instanceof Number) {
                maxHeightRatio = Math.max(0.0, Math.min(1.0, ((Number) maxHeightProperty).doubleValue()));
            }

            // Never upscale these images. They are meant to stay sharp and leave
            // intentional black space around the unused parts of the game panel.
            double scaleX = (double) d.width / imageWidth;
            double scaleY = (d.height * maxHeightRatio) / imageHeight;
            double scale = Math.min(1.0, Math.min(scaleX, scaleY));
            int scaledWidth = Math.max(1, (int) Math.round(imageWidth * scale));
            int scaledHeight = Math.max(1, (int) Math.round(imageHeight * scale));

            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(image, 0, 0, scaledWidth, scaledHeight, this);
        } finally {
            graphics.dispose();
        }
    }
}
