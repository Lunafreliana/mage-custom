package mage.abilities.effects.common.continuous;

import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.players.Player;

/**
 * Keeps the source controller's unspent mana as colorless mana when it would
 * otherwise be lost as a step or phase ends.
 *
 * @author muz
 */
public class ManaBecomesColorlessEffect extends ContinuousEffectImpl {

    public ManaBecomesColorlessEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Benefit);
        staticText = "if you would lose unspent mana, that mana becomes colorless instead";
    }

    protected ManaBecomesColorlessEffect(final ManaBecomesColorlessEffect effect) {
        super(effect);
    }

    @Override
    public ManaBecomesColorlessEffect copy() {
        return new ManaBecomesColorlessEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            player.getManaPool().setManaBecomesColorless(true);
        }
        return true;
    }
}
