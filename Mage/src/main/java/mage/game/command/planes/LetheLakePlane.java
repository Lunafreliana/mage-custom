package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.MillCardsTargetEffect;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.command.Plane;
import mage.target.Target;
import mage.target.TargetPlayer;

/**
 * @author spjspj
 */
public class LetheLakePlane extends Plane {

    public LetheLakePlane() {
        this.setPlaneType(Planes.PLANE_LETHE_LAKE);

        // At the beginning of your upkeep, put the top ten cards of your libary into your graveyard
        Ability ability = new BeginningOfUpkeepTriggeredAbility(Zone.COMMAND, TargetController.ANY, new MillCardsTargetEffect(10).setText("that player mills 10 cards"), false);
        this.getAbilities().add(ability);

        // Whenever chaos ensues, target player puts the top ten cards of their library into their graveyard
        Effect chaosEffect = new MillCardsTargetEffect(10);
        Target chaosTarget = new TargetPlayer();

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        chaosAbility.addTarget(chaosTarget);
        this.getAbilities().add(chaosAbility);
    }

    private LetheLakePlane(final LetheLakePlane plane) {
        super(plane);
    }

    @Override
    public LetheLakePlane copy() {
        return new LetheLakePlane(this);
    }
}
