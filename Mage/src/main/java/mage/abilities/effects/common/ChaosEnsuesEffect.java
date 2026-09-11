package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.game.Game;
import mage.game.events.GameEvent;

import java.util.UUID;

/** Emits the semantic Planechase "chaos ensues" event. */
public class ChaosEnsuesEffect extends OneShotEffect {

    private final UUID planarCardId;

    public ChaosEnsuesEffect() {
        this(null);
    }

    public ChaosEnsuesEffect(UUID planarCardId) {
        super(Outcome.Neutral);
        this.planarCardId = planarCardId;
        staticText = "chaos ensues";
    }

    protected ChaosEnsuesEffect(final ChaosEnsuesEffect effect) {
        super(effect);
        this.planarCardId = effect.planarCardId;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        UUID controllerId = source == null ? null : source.getControllerId();
        game.fireEvent(new GameEvent(GameEvent.EventType.CHAOS_ENSUES,
                planarCardId, source, controllerId));
        return true;
    }

    @Override
    public ChaosEnsuesEffect copy() {
        return new ChaosEnsuesEffect(this);
    }
}
