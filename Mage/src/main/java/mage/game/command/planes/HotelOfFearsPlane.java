package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.dynamicvalue.common.DevotionCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileTopXMayPlayUntilEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.choices.ChoiceColor;
import mage.constants.AbilityWord;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.permanent.PermanentIdPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetSacrifice;

import java.util.Set;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class HotelOfFearsPlane extends Plane {

    public HotelOfFearsPlane() {
        this.setPlaneType(Planes.PLANE_HOTEL_OF_FEARS);

        // At the beginning of your upkeep, exile the top card of your library. You lose life equal
        // to its mana value. You may play that card this turn.
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU,
                new HotelOfFearsUpkeepEffect(), false
        ));

        // Praise Him — Whenever chaos ensues, choose a color. Put X +1/+1 counters on target creature
        // you control, where X is your devotion to that color. Then sacrifice another creature.
        Ability ability = new ChaosEnsuesTriggeredAbility(new HotelOfFearsChaosEffect(), false);
        ability.setAbilityWord(AbilityWord.PRAISE_HIM);
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private HotelOfFearsPlane(final HotelOfFearsPlane plane) {
        super(plane);
    }

    @Override
    public HotelOfFearsPlane copy() {
        return new HotelOfFearsPlane(this);
    }
}

class HotelOfFearsUpkeepEffect extends ExileTopXMayPlayUntilEffect {

    HotelOfFearsUpkeepEffect() {
        super(1, Duration.EndOfTurn);
        staticText = "exile the top card of your library. You lose life equal to its mana value. "
                + "You may play that card this turn";
    }

    private HotelOfFearsUpkeepEffect(final HotelOfFearsUpkeepEffect effect) {
        super(effect);
    }

    @Override
    public HotelOfFearsUpkeepEffect copy() {
        return new HotelOfFearsUpkeepEffect(this);
    }

    @Override
    protected void effectCards(Game game, Ability source, Set<Card> cards) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return;
        }
        int manaValue = cards.stream().mapToInt(Card::getManaValue).sum();
        controller.loseLife(manaValue, game, source, false);
    }
}

class HotelOfFearsChaosEffect extends OneShotEffect {

    HotelOfFearsChaosEffect() {
        super(Outcome.BoostCreature);
        staticText = "choose a color. Put X +1/+1 counters on target creature you control, where X is "
                + "your devotion to that color. Then sacrifice another creature. "
                + "<i>(Your devotion to a color is the number of mana symbols of that color in the "
                + "mana costs of permanents you control.)</i>";
    }

    private HotelOfFearsChaosEffect(final HotelOfFearsChaosEffect effect) {
        super(effect);
    }

    @Override
    public HotelOfFearsChaosEffect copy() {
        return new HotelOfFearsChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        ChoiceColor choice = new ChoiceColor();
        if (controller == null || target == null || !controller.choose(outcome, choice, game)) {
            return false;
        }
        int devotion;
        switch (choice.getChoice()) {
            case "White": devotion = DevotionCount.W.calculate(game, source, this); break;
            case "Blue": devotion = DevotionCount.U.calculate(game, source, this); break;
            case "Black": devotion = DevotionCount.B.calculate(game, source, this); break;
            case "Red": devotion = DevotionCount.R.calculate(game, source, this); break;
            case "Green": devotion = DevotionCount.G.calculate(game, source, this); break;
            default: return false;
        }
        target.addCounters(CounterType.P1P1.createInstance(devotion), source, game);

        FilterPermanent filter = new FilterControlledCreaturePermanent("another creature");
        filter.add(Predicates.not(new PermanentIdPredicate(target.getId())));
        TargetSacrifice sacrifice = new TargetSacrifice(1, filter);
        if (game.getBattlefield().count(TargetSacrifice.makeFilter(filter), controller.getId(), source, game) > 0
                && sacrifice.choose(Outcome.Sacrifice, controller.getId(), source.getSourceId(), source, game)) {
            for (UUID permanentId : sacrifice.getTargets()) {
                Permanent permanent = game.getPermanent(permanentId);
                if (permanent != null) {
                    permanent.sacrifice(source, game);
                }
            }
        }
        return true;
    }
}
