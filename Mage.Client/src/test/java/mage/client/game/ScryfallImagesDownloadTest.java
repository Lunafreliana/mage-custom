package mage.client.game;

import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.game.command.PlanarCardRegistry;
import mage.sets.TheLordOfTheRingsTalesOfMiddleEarth;
import org.junit.Assert;
import org.junit.Test;
import org.mage.plugins.card.dl.sources.CardImageSource;
import org.mage.plugins.card.dl.sources.CardImageUrls;
import org.mage.plugins.card.dl.sources.ScryfallImageSource;
import org.mage.plugins.card.dl.sources.ScryfallImageSourceSmall;
import org.mage.plugins.card.images.CardDownloadData;
import org.mage.plugins.card.utils.CardImageUtils;

/**
 * @author JayDi85
 */
public class ScryfallImagesDownloadTest {

    @Test
    public void test_Cards_DownloadLinks() throws Exception {
        CardImageSource imageSource = ScryfallImageSource.getInstance();

        // normal card
        CardImageUrls urls = imageSource.generateCardUrl(new CardDownloadData("Grizzly Bears", "10E", "268", false, 0));
        Assert.assertEquals("https://api.scryfall.com/cards/10e/268/en?format=image", urls.getBaseUrl());

        // various card
        urls = imageSource.generateCardUrl(new CardDownloadData("Grizzly Bears", "30A", "195", true, 1));
        Assert.assertEquals("https://api.scryfall.com/cards/30a/195/en?format=image", urls.getBaseUrl());
        urls = imageSource.generateCardUrl(new CardDownloadData("Grizzly Bears", "30A", "492", true, 2));
        Assert.assertEquals("https://api.scryfall.com/cards/30a/492/en?format=image", urls.getBaseUrl());

        // api link
        urls = imageSource.generateCardUrl(new CardDownloadData("Ajani, the Greathearted", "WAR", "184*", false, 0));
        Assert.assertEquals("https://api.scryfall.com/cards/war/184★/en?format=image", urls.getBaseUrl());

        // direct api link
        urls = imageSource.generateCardUrl(new CardDownloadData("Command Tower", "REX", "26b", false, 0));
        Assert.assertEquals("https://api.scryfall.com/cards/rex/26/en?format=image&face=back", urls.getBaseUrl());

        // the one ring
        Assert.assertTrue("LTR must use The One Ring with 001 number, not 0", TheLordOfTheRingsTalesOfMiddleEarth.getInstance().getSetCardInfo()
                .stream()
                .filter(c -> c.getName().equals("The One Ring"))
                .anyMatch(c -> c.getCardNumber().equals("001"))
        );
        urls = imageSource.generateCardUrl(new CardDownloadData("The One Ring", "LTR", "001", false, 0));
        Assert.assertEquals("https://api.scryfall.com/cards/ltr/0/qya?format=image", urls.getBaseUrl());


        // added same tests for small images
        CardImageSource imageSourceSmall = ScryfallImageSourceSmall.getInstance();

        // normal card
        urls = imageSourceSmall.generateCardUrl(new CardDownloadData("Grizzly Bears", "10E", "268", false, 0));
        Assert.assertEquals("https://api.scryfall.com/cards/10e/268/en?format=image&version=small", urls.getBaseUrl());

        // various card
        urls = imageSourceSmall.generateCardUrl(new CardDownloadData("Grizzly Bears", "30A", "195", true, 1));
        Assert.assertEquals("https://api.scryfall.com/cards/30a/195/en?format=image&version=small", urls.getBaseUrl());
        urls = imageSourceSmall.generateCardUrl(new CardDownloadData("Grizzly Bears", "30A", "492", true, 2));
        Assert.assertEquals("https://api.scryfall.com/cards/30a/492/en?format=image&version=small", urls.getBaseUrl());

        // api link
        urls = imageSourceSmall.generateCardUrl(new CardDownloadData("Ajani, the Greathearted", "WAR", "184*", false, 0));
        Assert.assertEquals("https://api.scryfall.com/cards/war/184★/en?format=image&version=small", urls.getBaseUrl());

        // direct api link
        urls = imageSourceSmall.generateCardUrl(new CardDownloadData("Command Tower", "REX", "26b", false, 0));
        Assert.assertEquals("https://api.scryfall.com/cards/rex/26/en?format=image&version=small&face=back", urls.getBaseUrl());

        // the one ring
        Assert.assertTrue("LTR must use The One Ring with 001 number, not 0", TheLordOfTheRingsTalesOfMiddleEarth.getInstance().getSetCardInfo()
                .stream()
                .filter(c -> c.getName().equals("The One Ring"))
                .anyMatch(c -> c.getCardNumber().equals("001"))
        );
        urls = imageSourceSmall.generateCardUrl(new CardDownloadData("The One Ring", "LTR", "001", false, 0));
        Assert.assertEquals("https://api.scryfall.com/cards/ltr/0/qya?format=image&version=small", urls.getBaseUrl());
    }

    @Test
    public void test_PlanarProxyExactNameDownloadLinks() throws Exception {
        CardImageSource imageSource = ScryfallImageSource.getInstance();

        PlanarCardRegistry.Metadata planeMetadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_GREAT_FOREST));
        CardDownloadData plane = planarProxy(planeMetadata);
        Assert.assertFalse("Planes are real cards, not tokens", plane.isToken());
        Assert.assertEquals(
                "https://api.scryfall.com/cards/named?exact=The+Great+Forest&format=image",
                imageSource.generateCardUrl(plane).getBaseUrl());
        Assert.assertEquals(
                "https://api.scryfall.com/cards/named?exact=The+Great+Forest&format=image&version=small",
                ScryfallImageSourceSmall.getInstance().generateCardUrl(plane).getBaseUrl());

        PlanarCardRegistry.Metadata phenomenonMetadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY));
        CardDownloadData phenomenon = planarProxy(phenomenonMetadata);
        Assert.assertFalse("Phenomena use the same real-card path", phenomenon.isToken());
        Assert.assertEquals(
                "https://api.scryfall.com/cards/named?exact=Mutual+Epiphany&format=image",
                imageSource.generateCardUrl(phenomenon).getBaseUrl());

        CardDownloadData punctuated = CardDownloadData.forExactNameCard(
                "Norn's Dominion, Ravnica-City", "PCA", "plane:unknown");
        Assert.assertEquals(
                "https://api.scryfall.com/cards/named?exact=Norn%27s+Dominion%2C+Ravnica-City&format=image",
                imageSource.generateCardUrl(punctuated).getBaseUrl());

        CardDownloadData unknown = CardDownloadData.forExactNameCard(
                "Unknown Planar Card", "PCA", "plane:not_registered");
        Assert.assertEquals(
                "https://api.scryfall.com/cards/named?exact=Unknown+Planar+Card&format=image",
                imageSource.generateCardUrl(unknown).getBaseUrl());

        Assert.assertTrue("The downloaded image must use the proxy's existing card cache path",
                CardImageUtils.buildImagePathToCardOrToken(plane)
                        .endsWith("PCA" + java.io.File.separator + "The Great Forest.full.jpg"));
    }

    @Test
    public void test_RealTokensRemainOnTokenPath() throws Exception {
        CardDownloadData token = new CardDownloadData("Angel", "PCA", "0", false, 0);
        token.setToken(true);

        Assert.assertTrue(token.isToken());
        Assert.assertFalse(token.isExactNameLookup());
        Assert.assertEquals(
                "https://api.scryfall.com/cards/tpca/5/en?format=image",
                ScryfallImageSource.getInstance().generateTokenUrl(token).getBaseUrl());
    }

    private static CardDownloadData planarProxy(PlanarCardRegistry.Metadata metadata) {
        return CardDownloadData.forExactNameCard(
                metadata.getEnglishName(), metadata.getSetCode(), metadata.getId());
    }
}
