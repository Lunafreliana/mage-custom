package mage.cards.d;

import mage.MageInt;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterSpell;
import mage.filter.StaticFilters;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.TargetsPermanentPredicate;

import java.util.UUID;

/**
 * @author muz
 */
public final class DanithaSwordOfHope extends CardImpl {

    private static final FilterSpell filter = new FilterSpell(
            "an Equipment spell or a spell that targets a creature you control"
    );

    static {
        filter.add(Predicates.or(
                SubType.EQUIPMENT.getPredicate(),
                new TargetsPermanentPredicate(StaticFilters.FILTER_CONTROLLED_CREATURE)
        ));
    }

    public DanithaSwordOfHope(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.KNIGHT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // First strike
        this.addAbility(FirstStrikeAbility.getInstance());

        // Whenever you cast an Equipment spell or a spell that targets a creature you control, draw a card. This ability triggers only once each turn.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new DrawCardSourceControllerEffect(1), filter, false
        ).setTriggersLimitEachTurn(1));
    }

    private DanithaSwordOfHope(final DanithaSwordOfHope card) {
        super(card);
    }

    @Override
    public DanithaSwordOfHope copy() {
        return new DanithaSwordOfHope(this);
    }
}
