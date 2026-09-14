package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;

/**
 * Exiles through the first nonland permanent card, then lets the controller put
 * that card into their hand or onto the battlefield. Earlier cards stay exiled.
 */
public class ExileUntilNonlandPermanentEffect extends OneShotEffect {

    public ExileUntilNonlandPermanentEffect() {
        super(Outcome.Benefit);
        staticText = "exile cards from the top of your library until you exile a nonland permanent card. "
                + "Put that card onto the battlefield or into your hand";
    }

    private ExileUntilNonlandPermanentEffect(final ExileUntilNonlandPermanentEffect effect) {
        super(effect);
    }

    @Override
    public ExileUntilNonlandPermanentEffect copy() {
        return new ExileUntilNonlandPermanentEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        Card permanentCard = null;
        Cards cards = new CardsImpl();
        for (Card card : player.getLibrary().getCards(game)) {
            cards.add(card);
            if (card != null && !card.isLand(game) && card.isPermanent(game)) {
                permanentCard = card;
                break;
            }
        }
        player.moveCards(cards, Zone.EXILED, source, game);
        if (permanentCard == null) {
            return true;
        }
        Zone zone = player.chooseUse(
                outcome, "Put " + permanentCard.getName() + " into your hand or onto the battlefield?",
                "", "Hand", "Battlefield", source, game
        ) ? Zone.HAND : Zone.BATTLEFIELD;
        return player.moveCards(permanentCard, zone, source, game);
    }
}
