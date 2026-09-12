package mage.game.command.phenomena;

import mage.abilities.Ability;
import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.constants.Outcome;
import mage.constants.Phenomena;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Phenomenon;
import mage.players.Player;
import mage.target.common.TargetCardInHand;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class RealityShapingPhenomenon extends Phenomenon {

    public RealityShapingPhenomenon() {
        super(Phenomena.REALITY_SHAPING.getFullName());

        // When you encounter Reality Shaping, starting with you, each player may put a permanent card
        // from their hand onto the battlefield.
        this.getAbilities().add(new EncounterPhenomenonTriggeredAbility(new RealityShapingEffect()));
    }

    private RealityShapingPhenomenon(final RealityShapingPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public RealityShapingPhenomenon copy() {
        return new RealityShapingPhenomenon(this);
    }
}

class RealityShapingEffect extends OneShotEffect {

    RealityShapingEffect() {
        super(Outcome.PutCardInPlay);
        staticText = "starting with you, each player may put a permanent card from their hand onto the battlefield";
    }

    private RealityShapingEffect(final RealityShapingEffect effect) {
        super(effect);
    }

    @Override
    public RealityShapingEffect copy() {
        return new RealityShapingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        // The choices and moves happen one at a time in turn order. Later players therefore see
        // permanents put onto the battlefield earlier during this ability's resolution.
        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player == null || !player.canRespond()) {
                continue;
            }
            TargetCardInHand target = new TargetCardInHand(StaticFilters.FILTER_CARD_PERMANENT);
            if (target.canChoose(playerId, source, game)
                    && player.chooseUse(outcome, "Put a permanent card from your hand onto the battlefield?", source, game)
                    && player.chooseTarget(outcome, target, source, game)) {
                Card card = game.getCard(target.getFirstTarget());
                if (card != null) {
                    player.moveCards(card, Zone.BATTLEFIELD, source, game);
                }
            }
        }
        return true;
    }
}
