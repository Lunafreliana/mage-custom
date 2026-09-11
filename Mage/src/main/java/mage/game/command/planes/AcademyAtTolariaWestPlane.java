package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.discard.DiscardHandControllerEffect;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;

/**
 * @author spjspj
 */
public class AcademyAtTolariaWestPlane extends Plane {

    public AcademyAtTolariaWestPlane() {
        this.setPlaneType(Planes.PLANE_ACADEMY_AT_TOLARIA_WEST);

        // At the beginning of your end step, if you have no cards in hand, draw seven cards.
        Ability ability = new BeginningOfEndStepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new DrawCardSourceControllerEffect(7), false,
                HellbentPlanarControllerCondition.instance
        );
        this.getAbilities().add(ability);

        // Whenever chaos ensues, discard your hand
        Effect chaosEffect = new DiscardHandControllerEffect();

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private AcademyAtTolariaWestPlane(final AcademyAtTolariaWestPlane plane) {
        super(plane);
    }

    @Override
    public AcademyAtTolariaWestPlane copy() {
        return new AcademyAtTolariaWestPlane(this);
    }
}

enum HellbentPlanarControllerCondition implements Condition {

    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return game.getPlayer(source.getControllerId()).getHand().isEmpty();
    }

    @Override
    public String toString() {
        return "if you have no cards in hand";
    }
}
