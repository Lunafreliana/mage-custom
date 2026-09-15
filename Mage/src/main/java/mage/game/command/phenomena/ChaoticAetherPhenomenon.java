package mage.game.command.phenomena;

import mage.abilities.Ability;
import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.constants.Phenomena;
import mage.game.Game;
import mage.game.command.Phenomenon;
import mage.watchers.common.ChaoticAetherWatcher;

/**
 * @author VibecodingQueens
 */
public final class ChaoticAetherPhenomenon extends Phenomenon {

    public ChaoticAetherPhenomenon() {
        super(Phenomena.CHAOTIC_AETHER.getFullName());

        // When you encounter Chaotic Aether, each blank roll of the planar die is a chaos roll
        // until a player planeswalks away from a plane.
        Ability ability = new EncounterPhenomenonTriggeredAbility(new ChaoticAetherEffect());
        ability.addWatcher(new ChaoticAetherWatcher());
        this.getAbilities().add(ability);
    }

    private ChaoticAetherPhenomenon(final ChaoticAetherPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public ChaoticAetherPhenomenon copy() {
        return new ChaoticAetherPhenomenon(this);
    }
}

class ChaoticAetherEffect extends OneShotEffect {

    ChaoticAetherEffect() {
        super(Outcome.Benefit);
        staticText = "each blank roll of the planar die is a chaos roll until a player planeswalks away from a plane";
    }

    private ChaoticAetherEffect(final ChaoticAetherEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        ChaoticAetherWatcher watcher = game.getState().getWatcher(ChaoticAetherWatcher.class);
        if (watcher == null) {
            return false;
        }
        watcher.activate();
        return true;
    }

    @Override
    public ChaoticAetherEffect copy() {
        return new ChaoticAetherEffect(this);
    }
}
