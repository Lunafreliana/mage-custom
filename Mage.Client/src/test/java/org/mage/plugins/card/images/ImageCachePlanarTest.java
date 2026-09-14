package org.mage.plugins.card.images;

import mage.cards.decks.PlanarDeckCard;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import mage.view.CardView;
import org.junit.Assert;
import org.junit.Test;

public class ImageCachePlanarTest {

    @Test
    public void planarDeckCarriersUseRuntimeImageCacheKeys() {
        assertPlanarCarrierMatchesRuntime(PlanarCardRegistry.getId(Planes.PLANE_AKOUM));
        assertPlanarCarrierMatchesRuntime(PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY));
    }

    private static void assertPlanarCarrierMatchesRuntime(String registryId) {
        PlanarDeckCard carrier = new PlanarDeckCard(registryId);
        PlanarCard runtime = PlanarCardRegistry.create(registryId);
        Assert.assertNotNull(runtime);
        runtime.setSourceObjectAndInitImage();

        CardView carrierView = new CardView(carrier);
        CardView runtimeView = new CardView((mage.MageObject) runtime, null);

        // The carrier must retain its registry id for .dck serialization.
        Assert.assertEquals(registryId, carrierView.getCardNumber());

        // Image lookup must still behave exactly like the runtime planar object,
        // whose empty card number routes ImageCache through the token repository.
        Assert.assertEquals(
                ImageCache.getKey(runtimeView, runtimeView.getName(), 0),
                ImageCache.getKey(carrierView, carrierView.getName(), 0));
    }
}
