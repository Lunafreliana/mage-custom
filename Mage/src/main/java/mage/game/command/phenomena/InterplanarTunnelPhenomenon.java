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
public final class InterplanarTunnelPhenomenon extends Phenomenon {

    public InterplanarTunnelPhenomenon() {
        super(Phenomena.INTERPLANAR_TUNNEL.getFullName());

        // When you encounter Interplanar Tunnel, reveal cards from the top of your planar deck
        // until you reveal five plane cards. Put a plane card from among them on top of your
        // planar deck, then put the rest of the revealed cards on the bottom in a random order.
        this.getAbilities().add(new EncounterPhenomenonTriggeredAbility(new InterplanarTunnelEffect()));
    }

    private InterplanarTunnelPhenomenon(final InterplanarTunnelPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public InterplanarTunnelPhenomenon copy() {
        return new InterplanarTunnelPhenomenon(this);
    }
}

class InterplanarTunnelEffect extends OneShotEffect {

    InterplanarTunnelEffect() {
        super(Outcome.Benefit);
        staticText = "reveal cards from the top of your planar deck until you reveal five plane cards. "
                + "Put a plane card from among them on top of your planar deck, then put the rest "
                + "of the revealed cards on the bottom in a random order";
    }

    private InterplanarTunnelEffect(final InterplanarTunnelEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        return controller != null && game.chooseNextPlane(new PlaneswalkContext(
                controller.getId(), PlaneswalkContext.Cause.SPELL_OR_ABILITY, source.getSourceId()), 5);
    }

    @Override
    public InterplanarTunnelEffect copy() {
        return new InterplanarTunnelEffect(this);
    }
}
