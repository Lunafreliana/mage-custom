package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.game.Game;

/**
 * @author The XMage Developers
 */
public class ReverseTurnOrderEffect extends OneShotEffect {

    public ReverseTurnOrderEffect() {
        super(Outcome.Neutral);
        staticText = "reverse the game's turn order";
    }

    protected ReverseTurnOrderEffect(final ReverseTurnOrderEffect effect) {
        super(effect);
    }

    @Override
    public ReverseTurnOrderEffect copy() {
        return new ReverseTurnOrderEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        game.getState().setReverseTurnOrder(true);
        return true;
    }
}
