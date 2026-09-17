package mage.cards.k;

import java.util.UUID;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

/**
 * @author muz
 */
public final class KindredJudgment extends CardImpl {

    public KindredJudgment(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{5}{W}{W}");

        // Choose a creature type. Destroy all creatures that aren't of the chosen type.
        this.getSpellAbility().addEffect(new KindredDominanceEffect());
    }

    private KindredJudgment(final KindredJudgment card) {
        super(card);
    }

    @Override
    public KindredJudgment copy() {
        return new KindredJudgment(this);
    }
}
