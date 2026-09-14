package mage.filter.predicate.card;

import mage.cards.Card;
import mage.constants.CardUniverse;
import mage.filter.predicate.Predicate;
import mage.game.Game;

/**
 * Matches cards represented by a Universes Beyond printing.
 */
public enum MultiversalHighCouncilPredicate implements Predicate<Card> {
    instance;

    @Override
    public boolean apply(Card card, Game game) {
        return CardUniverse.from(card).isUniversesBeyond();
    }
}
