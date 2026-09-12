package mage.game.command.phenomena;

import mage.abilities.Ability;
import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.constants.Phenomena;
import mage.game.Game;
import mage.game.command.Phenomenon;
import mage.game.command.PlaneswalkContext;
import mage.players.Player;

/**
 * @author VibecodingQueens
 */
public final class SpatialMergingPhenomenon extends Phenomenon {

    public SpatialMergingPhenomenon() {
        super(Phenomena.SPATIAL_MERGING.getFullName());

        // When you encounter Spatial Merging, reveal cards from the top of your planar deck
        // until you reveal two plane cards. Simultaneously planeswalk to both of them. Put all
        // other cards revealed this way on the bottom of your planar deck in any order.
        this.getAbilities().add(new EncounterPhenomenonTriggeredAbility(new SpatialMergingEffect()));
    }

    private SpatialMergingPhenomenon(final SpatialMergingPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public SpatialMergingPhenomenon copy() {
        return new SpatialMergingPhenomenon(this);
    }
}

class SpatialMergingEffect extends OneShotEffect {

    SpatialMergingEffect() {
        super(Outcome.Benefit);
        staticText = "reveal cards from the top of your planar deck until you reveal two plane cards. "
                + "Simultaneously planeswalk to both of them. Put all other cards revealed this way "
                + "on the bottom of your planar deck in any order";
    }

    private SpatialMergingEffect(final SpatialMergingEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        return controller != null && game.planeswalkToNextPlanes(new PlaneswalkContext(
                controller.getId(), PlaneswalkContext.Cause.SPELL_OR_ABILITY, source.getSourceId()), 2);
    }

    @Override
    public SpatialMergingEffect copy() {
        return new SpatialMergingEffect(this);
    }
}
