package mage.cards.o;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.InvertCondition;
import mage.abilities.condition.common.SourceHasSubtypeCondition;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.effects.common.ReturnToBattlefieldUnderOwnerControlSourceEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureTypeTargetEffect;
import mage.abilities.keyword.FlashAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.counters.Counters;
import mage.counters.CounterType;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.other.DamagedPlayerThisTurnPredicate;
import mage.game.Game;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.Arrays;
import java.util.UUID;

public final class OtherworldlyEscort extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creature that dealt damage to you this turn");
    private static final InvertCondition condition = new InvertCondition(new SourceHasSubtypeCondition(SubType.SPIRIT), "it's not a Spirit");

    static {
        filter.add(new DamagedPlayerThisTurnPredicate(TargetController.YOU));
    }

    public OtherworldlyEscort(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DETECTIVE);
        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // When this creature dies, if it's not a Spirit, return it to the battlefield under its owner's control with four charge counters on it. It's a Spirit Detective.
        this.addAbility(new DiesSourceTriggeredAbility(new OtherworldlyEscortReturnEffect()).withInterveningIf(condition));

        // {1}{W}, {T}, Remove a charge counter from this creature: Destroy target creature that dealt damage to you this turn.
        final SimpleActivatedAbility ability = new SimpleActivatedAbility(new DestroyTargetEffect(), new ManaCostsImpl<>("{1}{W}"));
        ability.addTarget(new TargetPermanent(filter));
        ability.addCost(new TapSourceCost());
        ability.addCost(new RemoveCountersSourceCost(CounterType.CHARGE.createInstance(1)));
        this.addAbility(ability);
    }

    private OtherworldlyEscort(final OtherworldlyEscort card) {
        super(card);
    }

    @Override
    public OtherworldlyEscort copy() {
        return new OtherworldlyEscort(this);
    }
}

class OtherworldlyEscortReturnEffect extends ReturnToBattlefieldUnderOwnerControlSourceEffect {

    OtherworldlyEscortReturnEffect() {
        super();
        this.staticText = "return it to the battlefield under its owner's control with four charge counters on it. It's a Spirit Detective";
    }

    private OtherworldlyEscortReturnEffect(final OtherworldlyEscortReturnEffect effect) {
        super(effect);
    }

    @Override
    public OtherworldlyEscortReturnEffect copy() {
        return new OtherworldlyEscortReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        final Card card = game.getCard(source.getSourceId());
        if (card == null) {
            return false;
        }
        // Apply to the new battlefield object as it enters (CR 611.2e).
        // A group effect would snapshot the battlefield before this card returns.
        game.addEffect(new BecomesCreatureTypeTargetEffect(
                Duration.WhileOnBattlefield, Arrays.asList(SubType.SPIRIT, SubType.DETECTIVE), true
        ).setTargetPointer(new FixedTarget(card.getId(), card.getZoneChangeCounter(game) + 1)), source);
        game.setEnterWithCounters(card.getId(), new Counters(CounterType.CHARGE.createInstance(4)));
        return super.apply(game, source);
    }
}
