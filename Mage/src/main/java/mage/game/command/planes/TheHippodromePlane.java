package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostAllEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class TheHippodromePlane extends Plane {

    public TheHippodromePlane() {
        this.setPlaneType(Planes.PLANE_THE_HIPPODROME);

        // All creatures get -5/-0.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new BoostAllEffect(
                -5, 0, Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENT_ALL_CREATURES, false
        )));

        // Whenever chaos ensues, you may destroy target creature if its power is 0 or less.
        Ability ability = new ChaosEnsuesTriggeredAbility(new TheHippodromeDestroyEffect(), true);
        ability.addTarget(new TargetCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private TheHippodromePlane(final TheHippodromePlane plane) {
        super(plane);
    }

    @Override
    public TheHippodromePlane copy() {
        return new TheHippodromePlane(this);
    }
}

class TheHippodromeDestroyEffect extends OneShotEffect {

    TheHippodromeDestroyEffect() {
        super(Outcome.DestroyPermanent);
        staticText = "destroy target creature if its power is 0 or less";
    }

    private TheHippodromeDestroyEffect(final TheHippodromeDestroyEffect effect) {
        super(effect);
    }

    @Override
    public TheHippodromeDestroyEffect copy() {
        return new TheHippodromeDestroyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || permanent.getPower().getValue() > 0) {
            return false;
        }
        return permanent.destroy(source, game);
    }
}
