package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.Effect;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledLandPermanent;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;

/**
 * @author spjspj
 */
public class TazeemPlane extends Plane {

    public TazeemPlane() {
        this.setPlaneType(Planes.PLANE_TAZEEM);

        // Creatures can't block
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new TazeemCantBlockAllEffect());
        this.getAbilities().add(ability);

        // Whenever chaos ensues, draw a card for each land you control
        Effect chaosEffect = new DrawCardSourceControllerEffect(new PermanentsOnBattlefieldCount(new FilterControlledLandPermanent()));

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private TazeemPlane(final TazeemPlane plane) {
        super(plane);
    }

    @Override
    public TazeemPlane copy() {
        return new TazeemPlane(this);
    }
}

class TazeemCantBlockAllEffect extends RestrictionEffect {

    TazeemCantBlockAllEffect() {
        super(Duration.Custom);
        staticText = "creatures can't block";
    }

    protected TazeemCantBlockAllEffect(final TazeemCantBlockAllEffect effect) {
        super(effect);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_TAZEEM)) {
            return false;
        }
        return StaticFilters.FILTER_PERMANENT_CREATURES.match(permanent, source.getControllerId(), source, game);
    }

    @Override
    public boolean canBlock(Permanent attacker, Permanent blocker, Ability source, Game game, boolean canUseChooseDialogs) {
        return false;
    }

    @Override
    public TazeemCantBlockAllEffect copy() {
        return new TazeemCantBlockAllEffect(this);
    }

}
