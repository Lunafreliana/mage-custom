package mage.abilities.effects.common.continuous;

import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author The XMage Developers
 */
public class BoostAllCreaturesForSharedCreatureTypesEffect extends ContinuousEffectImpl {

    public BoostAllCreaturesForSharedCreatureTypesEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7,
                SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "Each creature gets +1/+1 for each other creature on the battlefield "
                + "that shares at least one creature type with it";
    }

    protected BoostAllCreaturesForSharedCreatureTypesEffect(
            final BoostAllCreaturesForSharedCreatureTypesEffect effect) {
        super(effect);
    }

    @Override
    public BoostAllCreaturesForSharedCreatureTypesEffect copy() {
        return new BoostAllCreaturesForSharedCreatureTypesEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<Permanent> creatures = game.getBattlefield().getActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURE, source.getControllerId(), game
        );
        for (Permanent creature : creatures) {
            int amount = (int) creatures.stream()
                    .filter(other -> !other.getId().equals(creature.getId()))
                    .filter(other -> other.shareCreatureTypes(game, creature))
                    .count();
            creature.addPower(amount);
            creature.addToughness(amount);
        }
        return true;
    }
}
