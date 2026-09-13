
package mage.cards.c;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.BoostAllCreaturesForSharedCreatureTypesEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;

import java.util.UUID;

/**
 * @author North
 */
public final class CoatOfArms extends CardImpl {

    public CoatOfArms(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        // Each creature gets +1/+1 for each other creature on the battlefield that shares at least one creature type with it.
        this.addAbility(new SimpleStaticAbility(new BoostAllCreaturesForSharedCreatureTypesEffect()));
    }

    private CoatOfArms(final CoatOfArms card) {
        super(card);
    }

    @Override
    public CoatOfArms copy() {
        return new CoatOfArms(this);
    }
}
