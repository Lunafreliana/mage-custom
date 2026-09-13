package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.EntersBattlefieldAllTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileThenReturnTargetEffect;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetAnyTarget;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetadjustment.TargetAdjuster;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class ImmersturmPlane extends Plane {

    public ImmersturmPlane() {
        this.setPlaneType(Planes.PLANE_IMMERSTURM);

        // Whenever a creature enters, that creature's controller may have it deal damage
        // equal to its power to any target of their choice.
        Ability ability = new EntersBattlefieldAllTriggeredAbility(
                Zone.COMMAND, new ImmersturmDamageEffect(),
                StaticFilters.FILTER_PERMANENT_A_CREATURE, false, SetTargetPointer.PERMANENT
        );
        ability.addTarget(new TargetAnyTarget());
        ability.setTargetAdjuster(ImmersturmAdjuster.instance);
        this.getAbilities().add(ability);

        // Whenever chaos ensues, exile target creature, then return it to the battlefield
        // under its owner's control.
        ability = new ChaosEnsuesTriggeredAbility(new ExileThenReturnTargetEffect(false, false), false);
        ability.addTarget(new TargetCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private ImmersturmPlane(final ImmersturmPlane plane) {
        super(plane);
    }

    @Override
    public ImmersturmPlane copy() {
        return new ImmersturmPlane(this);
    }
}

enum ImmersturmAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        UUID creatureId = ability.getEffects().get(0).getTargetPointer().getFirst(game, ability);
        Permanent creature = game.getPermanent(creatureId);
        if (creature != null) {
            ability.getTargets().get(0).setTargetController(creature.getControllerId());
        }
    }
}

class ImmersturmDamageEffect extends OneShotEffect {

    ImmersturmDamageEffect() {
        super(Outcome.Damage);
        staticText = "that creature's controller may have it deal damage equal to its power "
                + "to any target of their choice";
    }

    private ImmersturmDamageEffect(final ImmersturmDamageEffect effect) {
        super(effect);
    }

    @Override
    public ImmersturmDamageEffect copy() {
        return new ImmersturmDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player choosingPlayer = game.getPlayer(source.getTargets().get(0).getTargetController());
        Permanent enteringCreature = getTargetPointer().getFirstTargetPermanentOrLKI(game, source);
        if (choosingPlayer == null || enteringCreature == null) {
            return false;
        }
        if (!choosingPlayer.chooseUse(
                outcome,
                "Have " + enteringCreature.getLogName() + " deal damage equal to its power?",
                source,
                game
        )) {
            return true;
        }
        int damage = enteringCreature.getPower().getValue();
        Permanent targetPermanent = game.getPermanent(source.getTargets().getFirstTarget());
        if (targetPermanent != null) {
            targetPermanent.damage(damage, enteringCreature.getId(), source, game, false, true);
            return true;
        }
        Player targetPlayer = game.getPlayer(source.getTargets().getFirstTarget());
        if (targetPlayer != null) {
            targetPlayer.damage(damage, enteringCreature.getId(), source, game);
            return true;
        }
        return false;
    }
}
