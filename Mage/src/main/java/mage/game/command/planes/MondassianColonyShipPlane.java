package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.AttacksAllTriggeredAbility;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.TurnFaceDownCybermanTargetEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.SharesCreatureTypePredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetpointer.FixedTarget;

/**
 * @author The XMage Developers
 */
public final class MondassianColonyShipPlane extends Plane {

    public MondassianColonyShipPlane() {
        this.setPlaneType(Planes.PLANE_MONDASSIAN_COLONY_SHIP);

        // Whenever a creature attacks, it gets +1/+1 until end of turn for each other creature its
        // controller controls that shares a creature type with it.
        this.getAbilities().add(new AttacksAllTriggeredAbility(
                Zone.COMMAND, new MondassianColonyShipAttackEffect(), false,
                StaticFilters.FILTER_PERMANENT_CREATURE, SetTargetPointer.PERMANENT, false, false
        ));

        // Whenever chaos ensues, turn target creature face down. It becomes a 2/2 Cyberman artifact creature.
        Ability ability = new ChaosEnsuesTriggeredAbility(new TurnFaceDownCybermanTargetEffect(), false);
        ability.addTarget(new TargetCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private MondassianColonyShipPlane(final MondassianColonyShipPlane plane) {
        super(plane);
    }

    @Override
    public MondassianColonyShipPlane copy() {
        return new MondassianColonyShipPlane(this);
    }
}

class MondassianColonyShipAttackEffect extends OneShotEffect {

    MondassianColonyShipAttackEffect() {
        super(Outcome.BoostCreature);
        staticText = "it gets +1/+1 until end of turn for each other creature its controller controls "
                + "that shares a creature type with it";
    }

    private MondassianColonyShipAttackEffect(final MondassianColonyShipAttackEffect effect) {
        super(effect);
    }

    @Override
    public MondassianColonyShipAttackEffect copy() {
        return new MondassianColonyShipAttackEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent attacker = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (attacker == null) {
            return false;
        }
        FilterPermanent filter = new FilterControlledCreaturePermanent();
        filter.add(new SharesCreatureTypePredicate(attacker));
        // AnotherPredicate would compare against the Plane rather than the attacker.
        int count = game.getBattlefield().count(filter, attacker.getControllerId(), source, game) - 1;
        if (count > 0) {
            game.addEffect(new BoostTargetEffect(count, count, Duration.EndOfTurn)
                    .setTargetPointer(new FixedTarget(attacker, game)), source);
        }
        return true;
    }
}
