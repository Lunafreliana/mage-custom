package mage.client.game;

import mage.cards.decks.PlanarDeckCard;
import mage.cards.repository.TokenInfo;
import mage.cards.repository.TokenRepository;
import mage.constants.Planes;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import mage.sets.TheLordOfTheRingsTalesOfMiddleEarth;
import mage.view.CardView;
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
    public void test_PlanarTokenDownloadLinks() throws Exception {
        CardImageSource imageSource = ScryfallImageSource.getInstance();

        CardDownloadData mappedPlane = planarToken("Plane - Academy at Tolaria West", "PCA");
        Assert.assertTrue(imageSource.isTokenImageProvided("PCA", mappedPlane.getName(), 0));
        Assert.assertEquals(
                "https://api.scryfall.com/cards/opca/9/en?format=image",
                imageSource.generateTokenUrl(mappedPlane).getBaseUrl());

        CardDownloadData unmappedPlane = planarToken("Plane - Antarctic Research Base", "WHO");
        Assert.assertTrue(imageSource.isTokenImageProvided("WHO", unmappedPlane.getName(), 0));
        Assert.assertEquals(
                "https://api.scryfall.com/cards/named?exact=Antarctic+Research+Base&format=image",
                imageSource.generateTokenUrl(unmappedPlane).getBaseUrl());
        Assert.assertEquals(
                "https://api.scryfall.com/cards/named?exact=Antarctic+Research+Base&format=image&version=small",
                ScryfallImageSourceSmall.getInstance().generateTokenUrl(unmappedPlane).getBaseUrl());

        CardDownloadData phenomenon = planarToken("Phenomenon - Interplanar Tunnel", "PCA");
        Assert.assertTrue(imageSource.isTokenImageProvided("PCA", phenomenon.getName(), 0));
        Assert.assertEquals(
                "https://api.scryfall.com/cards/named?exact=Interplanar+Tunnel&format=image",
                imageSource.generateTokenUrl(phenomenon).getBaseUrl());
    }

    @Test
    public void test_PlanarCarrierUsesRuntimeTokenImagePath() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_AKOUM));
        PlanarDeckCard carrier = new PlanarDeckCard(metadata.getId());
        PlanarCard runtime = PlanarCardRegistry.create(metadata.getId());
        runtime.setSourceObjectAndInitImage();

        Assert.assertEquals(
                CardImageUtils.buildImagePathToCardView(new CardView((mage.MageObject) runtime, null)),
                CardImageUtils.buildImagePathToCardView(new CardView(carrier)));
        Assert.assertTrue(CardImageUtils.buildImagePathToCardView(new CardView(carrier))
                .contains(java.io.File.separator + "TOK"));
    }

    @Test
    public void test_CybermanReminderUsesWhoTokenImage() throws Exception {
        TokenInfo reminder = TokenRepository.instance.findPreferredTokenInfoForXmage(
                TokenRepository.XMAGE_IMAGE_NAME_FACE_DOWN_CYBERMAN, null);
        Assert.assertNotNull(reminder);
        Assert.assertEquals(TokenRepository.CYBERMAN_REMINDER_SET_CODE, reminder.getSetCode());

        CardDownloadData token = new CardDownloadData(
                reminder.getName(), reminder.getSetCode(), "0", false, reminder.getImageNumber());
        token.setToken(true);
        CardImageSource imageSource = ScryfallImageSource.getInstance();
        Assert.assertTrue(imageSource.isTokenImageProvided(
                reminder.getSetCode(), reminder.getName(), reminder.getImageNumber()));
        Assert.assertEquals(reminder.getDownloadUrl(), imageSource.generateTokenUrl(token).getBaseUrl());
        Assert.assertEquals(CardImageUtils.buildImagePathToTokens() + "WHO"
                        + java.io.File.separator + "Cyberman 1.full.jpg",
                CardImageUtils.buildImagePathToCardOrToken(token));
    }

    @Test
    public void test_RealTokensRemainOnTokenPath() throws Exception {
        CardDownloadData token = new CardDownloadData("Angel", "PCA", "0", false, 0);
        token.setToken(true);

        Assert.assertTrue(token.isToken());
        Assert.assertEquals(
                "https://api.scryfall.com/cards/tpca/5/en?format=image",
                ScryfallImageSource.getInstance().generateTokenUrl(token).getBaseUrl());
    }

    private static CardDownloadData planarToken(String name, String setCode) {
        CardDownloadData result = new CardDownloadData(name, setCode, "0", false, 0);
        result.setToken(true);
        return result;
    }
}
