package mage.game.command.planes;

import mage.ObjectColor;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.common.PutCardIntoGraveFromAnywhereAllTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.LoseLifeTargetEffect;
import mage.abilities.effects.common.discard.DiscardEachPlayerEffect;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.command.Plane;

/**
 * @author spjspj
 */
public class TheDarkBaronyPlane extends Plane {

    private static final FilterCard filter = new FilterCard("a nonblack card");

    static {
        filter.add(Predicates.not(new ColorPredicate(ObjectColor.BLACK)));
    }

    public TheDarkBaronyPlane() {
        this.setPlaneType(Planes.PLANE_THE_DARK_BARONY);

        // Whenever a nonblack card is put into a player's graveyard from anywhere, that player loses 1 life
        Ability ability = new PutCardIntoGraveFromAnywhereAllTriggeredAbility(Zone.COMMAND,
                new LoseLifeTargetEffect(1), false, filter, TargetController.ANY, SetTargetPointer.PLAYER);
        this.getAbilities().add(ability);

        // Whenever chaos ensues, each player dicards a card
        Effect chaosEffect = new DiscardEachPlayerEffect(TargetController.OPPONENT);

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private TheDarkBaronyPlane(final TheDarkBaronyPlane plane) {
        super(plane);
    }

    @Override
    public TheDarkBaronyPlane copy() {
        return new TheDarkBaronyPlane(this);
    }
}
