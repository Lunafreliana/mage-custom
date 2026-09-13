package org.mage.card.arcane;

import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.decks.PlanarDeckCard;
import mage.constants.CardType;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.game.command.PlanarCardRegistry;
import mage.view.CardView;
import org.junit.Assert;
import org.junit.Test;

public class CardPanelRenderModeImageTest {

    @Test
    public void missingPlanarArtUsesTextRendererButAvailableArtDoesNot() {
        CardView plane = new CardView(new PlanarDeckCard(PlanarCardRegistry.getId(Planes.PLANE_AKOUM)));
        CardView phenomenon = new CardView(new PlanarDeckCard(
                PlanarCardRegistry.getId(Phenomena.MUTUAL_EPIPHANY)));

        Assert.assertTrue(CardPanelRenderModeImage.usesTextFallback(plane, false));
        Assert.assertTrue(CardPanelRenderModeImage.usesTextFallback(phenomenon, false));
        Assert.assertFalse(CardPanelRenderModeImage.usesTextFallback(plane, true));
        Assert.assertFalse(CardPanelRenderModeImage.usesTextFallback(phenomenon, true));
        Assert.assertFalse(CardPanelRenderModeImage.usesTextFallback(new CardView(new OrdinaryCard()), false));
    }

    private static final class OrdinaryCard extends CardImpl {

        private OrdinaryCard() {
            super(null, "Ordinary Card");
            this.cardType.add(CardType.SORCERY);
        }

        private OrdinaryCard(OrdinaryCard card) {
            super(card);
        }

        @Override
        public Card copy() {
            return new OrdinaryCard(this);
        }
    }
}
