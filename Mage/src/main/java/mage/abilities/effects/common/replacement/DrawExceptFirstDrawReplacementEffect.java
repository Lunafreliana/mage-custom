package mage.abilities.effects.common.replacement;

import mage.abilities.Ability;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.PhaseStep;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.watchers.common.CardsDrawnDuringDrawStepWatcher;

/**
 * @author VibecodingQueens
 */
public class DrawExceptFirstDrawReplacementEffect extends ReplacementEffectImpl {

    private final int amount;

    public DrawExceptFirstDrawReplacementEffect(int amount) {
        super(Duration.WhileOnBattlefield, Outcome.DrawCard);
        this.amount = amount;
    }

    protected DrawExceptFirstDrawReplacementEffect(final DrawExceptFirstDrawReplacementEffect effect) {
        super(effect);
        this.amount = effect.amount;
    }

    @Override
    public DrawExceptFirstDrawReplacementEffect copy() {
        return new DrawExceptFirstDrawReplacementEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            controller.drawCards(amount, source, game, event);
        }
        return true;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DRAW_CARD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!event.getPlayerId().equals(source.getControllerId())) {
            return false;
        }
        if (!game.isActivePlayer(event.getPlayerId())
                || game.getPhase().getStep().getType() != PhaseStep.DRAW) {
            return true;
        }
        CardsDrawnDuringDrawStepWatcher watcher = game.getState().getWatcher(CardsDrawnDuringDrawStepWatcher.class);
        return watcher != null && watcher.getAmountCardsDrawn(event.getPlayerId()) > 0;
    }
}
