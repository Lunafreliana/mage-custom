package mage.abilities.special;

import mage.MageIdentifier;
import mage.abilities.ActivatedAbility.ActivationStatus;
import mage.abilities.SpecialAction;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.RollPlanarDieEffect;
import mage.game.Game;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Rule 901.9's priority-time special action. */
public class RollPlanarDieSpecialAction extends SpecialAction {

    private boolean costPrepared;

    public RollPlanarDieSpecialAction() {
        super();
        addEffect(new RollPlanarDieEffect(null, null));
        setName("Roll the planar die");
    }

    protected RollPlanarDieSpecialAction(final RollPlanarDieSpecialAction action) {
        super(action);
        this.costPrepared = action.costPrepared;
    }

    private String countKey() {
        return "planechaseSpecialRolls|" + getControllerId();
    }

    private String turnKey() {
        return "planechaseSpecialRollsTurn|" + getControllerId();
    }

    private int usesThisTurn(Game game) {
        Object turn = game.getState().getValue(turnKey());
        Object count = game.getState().getValue(countKey());
        if (!(turn instanceof Integer) || !(count instanceof Integer)
                || (Integer) turn != game.getTurnNum()) {
            return 0;
        }
        return (Integer) count;
    }

    @Override
    public ActivationStatus canActivate(UUID playerId, Game game) {
        if (!game.getState().isPlaneChase()
                || !Objects.equals(playerId, getControllerId())
                || !Objects.equals(playerId, game.getActivePlayerId())
                || !Objects.equals(playerId, game.getPriorityPlayerId())
                || !game.isMainPhase()
                || !game.getStack().isEmpty()) {
            return ActivationStatus.getFalse();
        }
        return super.canActivate(playerId, game);
    }

    @Override
    public boolean activate(Game game, Set<MageIdentifier> allowedIdentifiers, boolean noMana) {
        if (!costPrepared) {
            int cost = usesThisTurn(game);
            if (cost > 0) {
                addCost(new GenericManaCost(cost));
            }
            costPrepared = true;
        }
        if (!super.activate(game, allowedIdentifiers, noMana)) {
            return false;
        }
        int nextCount = usesThisTurn(game) + 1;
        game.getState().setValue(turnKey(), game.getTurnNum());
        game.getState().setValue(countKey(), nextCount);
        return true;
    }

    @Override
    public RollPlanarDieSpecialAction copy() {
        return new RollPlanarDieSpecialAction(this);
    }
}
