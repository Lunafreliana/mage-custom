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

    private int usesThisTurn(Game game) {
        Object value = game.getState().getValue(countKey());
        if (!(value instanceof int[])) {
            return 0;
        }
        int[] state = (int[]) value;
        return state[0] == game.getTurnNum() ? state[1] : 0;
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
        game.getState().setValue(countKey(), new int[]{game.getTurnNum(), usesThisTurn(game) + 1});
        return true;
    }

    @Override
    public RollPlanarDieSpecialAction copy() {
        return new RollPlanarDieSpecialAction(this);
    }
}
