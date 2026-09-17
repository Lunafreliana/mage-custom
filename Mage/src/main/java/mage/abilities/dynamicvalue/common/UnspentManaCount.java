package mage.abilities.dynamicvalue.common;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.game.Game;
import mage.players.Player;

/**
 * The total amount of unspent mana in the source controller's mana pool.
 *
 * @author muz
 */
public enum UnspentManaCount implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        Player player = game.getPlayer(sourceAbility.getControllerId());
        return player == null ? 0 : player.getManaPool().count();
    }

    @Override
    public UnspentManaCount copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "unspent mana you have";
    }

    @Override
    public String toString() {
        return "1";
    }
}
