package mage.abilities.effects.common.continuous;

import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.RestrictionEffect;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

/**
 * @author LevelX2
 */
public class UntapAllDuringEachOtherPlayersUntapStepEffect extends ContinuousEffectImpl {

    private final FilterPermanent filter;
    private final boolean eachPlayersUntapStep;

    public UntapAllDuringEachOtherPlayersUntapStepEffect(FilterPermanent filter) {
        this(filter, false);
    }

    public UntapAllDuringEachOtherPlayersUntapStepEffect(FilterPermanent filter, boolean eachPlayersUntapStep) {
        super(Duration.WhileOnBattlefield, Outcome.Untap);
        this.filter = filter;
        this.eachPlayersUntapStep = eachPlayersUntapStep;
        staticText = setStaticText();
    }

    protected UntapAllDuringEachOtherPlayersUntapStepEffect(final UntapAllDuringEachOtherPlayersUntapStepEffect effect) {
        super(effect);
        this.filter = effect.filter;
        this.eachPlayersUntapStep = effect.eachPlayersUntapStep;
    }

    @Override
    public UntapAllDuringEachOtherPlayersUntapStepEffect copy() {
        return new UntapAllDuringEachOtherPlayersUntapStepEffect(this);
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        if (layer == Layer.RulesEffects
                && game.getTurnStepType() == PhaseStep.UNTAP
                && (eachPlayersUntapStep || !source.isControlledBy(game.getActivePlayerId()))) {
            Integer appliedTurn = (Integer) game.getState().getValue(source.getSourceId() + "appliedTurn");
            if (appliedTurn == null) {
                appliedTurn = 0;
            }
            if (appliedTurn < game.getTurnNum()) {
                game.getState().setValue(source.getSourceId() + "appliedTurn", game.getTurnNum());
                for (Permanent permanent : game.getBattlefield().getActivePermanents(
                        filter, source.getControllerId(), source, game)) {
                    boolean untap = true;
                    for (RestrictionEffect effect : game.getContinuousEffects().getApplicableRestrictionEffects(permanent, game).keySet()) {
                        untap &= effect.canBeUntapped(permanent, source, game, true);
                    }
                    if (untap) {
                        permanent.untap(game);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.RulesEffects;
    }

    private String setStaticText() {
        StringBuilder sb = new StringBuilder("Untap ");
        if (!filter.getMessage().startsWith("each")) {
            sb.append("all ");
        }
        sb.append(filter.getMessage());
        sb.append(eachPlayersUntapStep
                ? " during each player's untap step"
                : " during each other player's untap step");
        return sb.toString();
    }
}
