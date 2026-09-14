package mage.cards.i;

import mage.MageInt;
import mage.abilities.common.MutatesSourceTriggeredAbility;
import mage.abilities.effects.common.ExileUntilNonlandPermanentEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.MutateAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class IllunaApexOfWishes extends CardImpl {

    public IllunaApexOfWishes(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BEAST);
        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.DINOSAUR);
        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // Mutate {3}{R/G}{U}{U}
        this.addAbility(new MutateAbility(this, "{3}{R/G}{U}{U}"));

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Whenever this creature mutates, exile cards from the top of your library until you exile a nonland permanent card. Put that card onto the battlefield or into your hand.
        this.addAbility(new MutatesSourceTriggeredAbility(new ExileUntilNonlandPermanentEffect()));
    }

    private IllunaApexOfWishes(final IllunaApexOfWishes card) {
        super(card);
    }

    @Override
    public IllunaApexOfWishes copy() {
        return new IllunaApexOfWishes(this);
    }
}
