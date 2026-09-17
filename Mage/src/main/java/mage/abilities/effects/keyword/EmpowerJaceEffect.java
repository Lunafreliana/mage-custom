package mage.abilities.effects.keyword;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.JacePlaneswalkerToken;
import mage.players.Player;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.util.CardUtil;

import java.util.Set;
import java.util.UUID;

/**
 * @author muz
 */
public class EmpowerJaceEffect extends OneShotEffect {

    private static final FilterPermanent filter = new FilterControlledPermanent("Jace token you control");

    static {
        filter.add(SubType.JACE.getPredicate());
        filter.add(TokenPredicate.TRUE);
    }

    private final DynamicValue amount;

    public EmpowerJaceEffect(int amount) {
        this(StaticValue.get(amount));
    }

    public EmpowerJaceEffect(DynamicValue amount) {
        super(Outcome.Benefit);
        this.amount = amount.copy();
        staticText = "empower Jace " + amount + ". <i>(Put "
                + CardUtil.numberToText(amount.toString(), "that many")
                + " loyalty counters on a Jace token you control. If you don't control one, first create "
                + "a blue Jace planeswalker token with \"[−1]: Surveil 1\" and \"[−3]: Draw a card.\")</i>";
    }

    private EmpowerJaceEffect(final EmpowerJaceEffect effect) {
        super(effect);
        this.amount = effect.amount.copy();
    }

    @Override
    public EmpowerJaceEffect copy() {
        return new EmpowerJaceEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return doEmpower(amount.calculate(game, source, this), game, source) != null;
    }

    public static Permanent doEmpower(int amount, Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return null;
        }
        if (!game.getBattlefield().contains(filter, source, game, 1)) {
            new JacePlaneswalkerToken().putOntoBattlefield(1, game, source);
        }

        Target target = new TargetPermanent(filter);
        target.withNotTarget(true);
        Set<UUID> choices = target.possibleTargets(source.getControllerId(), source, game);
        if (choices.isEmpty()) {
            return null;
        }
        UUID chosenId = choices.iterator().next();
        if (choices.size() > 1) {
            player.choose(Outcome.Benefit, target, source, game);
            chosenId = target.getFirstTarget();
        }
        Permanent jace = game.getPermanent(chosenId);
        if (jace != null && amount > 0) {
            jace.addCounters(CounterType.LOYALTY.createInstance(amount), source.getControllerId(), source, game);
        }
        return jace;
    }
}
