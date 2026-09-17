package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.choices.TwoChoiceVote;
import mage.constants.AbilityWord;
import mage.constants.Outcome;
import mage.game.Game;
import mage.game.command.PlaneswalkContext;

import java.util.UUID;

/**
 * @author TheElk801
 */
public class WillOfThePlaneswalkersEffect extends OneShotEffect {

    public WillOfThePlaneswalkersEffect() {
        super(Outcome.Benefit);
        staticText = AbilityWord.WILL_OF_THE_PLANESWALKERS.formatWord() + "Starting with you, each player votes " +
                "for planeswalk or chaos. If planeswalk gets more votes, planeswalk. " +
                "If chaos gets more votes or the vote is tied, chaos ensues";
        concatBy("<br>");
    }

    private WillOfThePlaneswalkersEffect(final WillOfThePlaneswalkersEffect effect) {
        super(effect);
    }

    @Override
    public WillOfThePlaneswalkersEffect copy() {
        return new WillOfThePlaneswalkersEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        TwoChoiceVote vote = new TwoChoiceVote("Planeswalk", "Chaos", Outcome.Benefit);
        vote.doVotes(source, game);
        int planeswalkCount = vote.getVoteCount(true);
        int chaosCount = vote.getVoteCount(false);
        if (planeswalkCount > chaosCount) {
            if (!game.getState().isPlaneChase() || game.getState().getFaceUpPlanarCards().isEmpty()) {
                return true;
            }
            UUID planarControllerId = game.getState().getPlanarControllerId();
            return planarControllerId != null && game.planeswalk(new PlaneswalkContext(
                    planarControllerId, PlaneswalkContext.Cause.SPELL_OR_ABILITY, source.getSourceId()
            ));
        }
        return new ChaosEnsuesEffect().apply(game, source);
    }
}
