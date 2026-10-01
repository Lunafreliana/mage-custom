package mage.cards.f;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.common.ExileThenReturnTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class FlickeringHound extends CardImpl {

    private static final FilterPermanent filter =
            new FilterControlledCreaturePermanent("other target creature you control");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public FlickeringHound(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{W}");

        this.subtype.add(SubType.DOG);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever you cast a creature spell, exile up to one other target creature you control, then return that card to the battlefield under its owner's control.
        Ability ability = new SpellCastControllerTriggeredAbility(
                new ExileThenReturnTargetEffect(false, true),
                StaticFilters.FILTER_SPELL_A_CREATURE,
                false
        );
        ability.addTarget(new TargetPermanent(0, 1, filter));
        this.addAbility(ability);
    }

    private FlickeringHound(final FlickeringHound card) {
        super(card);
    }

    @Override
    public FlickeringHound copy() {
        return new FlickeringHound(this);
    }
}
