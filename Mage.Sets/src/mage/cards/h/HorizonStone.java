package mage.cards.h;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.ManaBecomesColorlessEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class HorizonStone extends CardImpl {

    public HorizonStone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        // If you would lose unspent mana, that mana becomes colorless instead.
        this.addAbility(new SimpleStaticAbility(new ManaBecomesColorlessEffect()));
    }

    private HorizonStone(final HorizonStone card) {
        super(card);
    }

    @Override
    public HorizonStone copy() {
        return new HorizonStone(this);
    }
}
