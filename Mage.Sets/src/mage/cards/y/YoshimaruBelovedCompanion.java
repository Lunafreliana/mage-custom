package mage.cards.y;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.effects.common.replacement.ModifyCountersAddedEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class YoshimaruBelovedCompanion extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("legendary creature");

    static {
        filter.add(CardType.CREATURE.getPredicate());
        filter.add(SuperType.LEGENDARY.getPredicate());
    }

    public YoshimaruBelovedCompanion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DOG);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // If one or more +1/+1 counters would be put on a creature you control, that many plus one +1/+1 counters are put on it instead.
        this.addAbility(new SimpleStaticAbility(new ModifyCountersAddedEffect(
                StaticFilters.FILTER_CONTROLLED_CREATURE, CounterType.P1P1
        )));

        // {6}: Put a +1/+1 counter on target legendary creature.
        Ability ability = new SimpleActivatedAbility(
                new AddCountersTargetEffect(CounterType.P1P1.createInstance()), new GenericManaCost(6)
        );
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private YoshimaruBelovedCompanion(final YoshimaruBelovedCompanion card) {
        super(card);
    }

    @Override
    public YoshimaruBelovedCompanion copy() {
        return new YoshimaruBelovedCompanion(this);
    }
}
