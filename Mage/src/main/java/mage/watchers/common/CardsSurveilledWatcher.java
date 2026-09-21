package mage.watchers.common;

import mage.MageObjectReference;
import mage.cards.Card;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks cards put into graveyards while being surveilled this turn.
 *
 * @author OpenAI
 */
public class CardsSurveilledWatcher extends Watcher {

    private final Set<MageObjectReference> surveilledThisTurn = new HashSet<>();

    public CardsSurveilledWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.SURVEILLED_CARD) {
            return;
        }
        Card card = game.getCard(event.getTargetId());
        if (card == null || game.getState().getZone(card.getId()) != Zone.GRAVEYARD) {
            return;
        }
        surveilledThisTurn.add(new MageObjectReference(card.getMainCard(), game));
    }

    @Override
    public void reset() {
        super.reset();
        surveilledThisTurn.clear();
    }

    public boolean checkCard(Card card, Game game) {
        return surveilledThisTurn.contains(new MageObjectReference(card.getMainCard(), game));
    }
}
