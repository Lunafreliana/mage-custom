package mage.cards.h;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.PreventionEffectImpl;
import mage.abilities.effects.common.BecomesMonarchSourceEffect;
import mage.abilities.effects.common.ReturnMORToBattlefieldUnderOwnerControlWithCounterEffect;
import mage.abilities.hint.common.MonarchHint;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class HeartShapedHerb extends CardImpl {

    public HeartShapedHerb(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        // If a source an opponent controls would deal damage to you, prevent 1 of that damage.
        this.addAbility(new SimpleStaticAbility(new HeartShapedHerbPreventionEffect()));

        // {2}, {T}, Sacrifice this artifact: You may sacrifice a creature. If you do, return that card
        // to the battlefield under its owner's control with three +1/+1 counters on it and you become the monarch.
        Ability ability = new SimpleActivatedAbility(
                new HeartShapedHerbReturnEffect(), new ManaCostsImpl<>("{2}")
        ).addHint(MonarchHint.instance);
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost().setText("sacrifice this artifact"));
        this.addAbility(ability);
    }

    private HeartShapedHerb(final HeartShapedHerb card) {
        super(card);
    }

    @Override
    public HeartShapedHerb copy() {
        return new HeartShapedHerb(this);
    }
}

class HeartShapedHerbPreventionEffect extends PreventionEffectImpl {

    HeartShapedHerbPreventionEffect() {
        super(Duration.WhileOnBattlefield, 1, false, false);
        staticText = "If a source an opponent controls would deal damage to you, prevent 1 of that damage";
    }

    private HeartShapedHerbPreventionEffect(final HeartShapedHerbPreventionEffect effect) {
        super(effect);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGE_PLAYER;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        UUID damageControllerId = game.getControllerId(event.getSourceId());
        return source.isControlledBy(event.getTargetId())
                && damageControllerId != null
                && game.getOpponents(source.getControllerId()).contains(damageControllerId)
                && super.applies(event, source, game);
    }

    @Override
    public HeartShapedHerbPreventionEffect copy() {
        return new HeartShapedHerbPreventionEffect(this);
    }
}

class HeartShapedHerbReturnEffect extends OneShotEffect {

    HeartShapedHerbReturnEffect() {
        super(Outcome.Benefit);
        staticText = "You may sacrifice a creature. If you do, return that card to the battlefield under its "
                + "owner's control with three +1/+1 counters on it and you become the monarch";
    }

    private HeartShapedHerbReturnEffect(final HeartShapedHerbReturnEffect effect) {
        super(effect);
    }

    @Override
    public HeartShapedHerbReturnEffect copy() {
        return new HeartShapedHerbReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        SacrificeTargetCost cost = new SacrificeTargetCost(StaticFilters.FILTER_PERMANENT_CREATURE);
        if (controller == null
                || !cost.canPay(source, source, source.getControllerId(), game)
                || !controller.chooseUse(outcome, "Sacrifice a creature?", source, game)
                || !cost.pay(source, game, source, source.getControllerId(), false)) {
            return false;
        }

        Permanent sacrificed = cost.getPermanents().stream().findFirst().orElse(null);
        Card card = sacrificed == null ? null : game.getCard(sacrificed.getId());
        if (card != null) {
            new ReturnMORToBattlefieldUnderOwnerControlWithCounterEffect(
                    new MageObjectReference(card, game),
                    CounterType.P1P1.createInstance(3),
                    "three +1/+1 counters"
            ).apply(game, source);
        }
        new BecomesMonarchSourceEffect().apply(game, source);
        return true;
    }
}
