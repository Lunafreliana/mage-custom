package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.triggers.BeginningOfDrawTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.DrawCardTargetEffect;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.command.Plane;

/**
 * @author VibecodingQueens
 */
public class PanopticonPlane extends Plane {

    private static final String rule = "At the beginning of your draw step, draw an additional card";

    public PanopticonPlane() {
        this.setPlaneType(Planes.PLANE_PANOPTICON);

        // When you planeswalk to Panopticon, draw a card
        Ability pwability = new PlaneswalkToSourceTriggeredAbility(new DrawCardSourceControllerEffect(1));
        this.getAbilities().add(pwability);

        // At the beginning of your draw step, draw an additional card.
        Ability ability = new BeginningOfDrawTriggeredAbility(Zone.COMMAND, TargetController.YOU, new DrawCardTargetEffect(1), false);
        this.getAbilities().add(ability);

        // Whenever chaos ensues, draw a card
        Effect chaosEffect = new DrawCardSourceControllerEffect(1);

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private PanopticonPlane(final PanopticonPlane plane) {
        super(plane);
    }

    @Override
    public PanopticonPlane copy() {
        return new PanopticonPlane(this);
    }
}
