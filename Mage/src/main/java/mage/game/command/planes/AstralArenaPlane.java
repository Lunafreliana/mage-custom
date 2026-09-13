package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.effects.common.DamageAllEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class AstralArenaPlane extends Plane {

    public AstralArenaPlane() {
        this.setPlaneType(Planes.PLANE_ASTRAL_ARENA);

        // No more than one creature can attack each combat.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new AstralArenaAttackRestrictionEffect()
        ));

        // No more than one creature can block each combat.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new AstralArenaBlockRestrictionEffect()
        ));

        // Whenever chaos ensues, Astral Arena deals 2 damage to each creature.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new DamageAllEffect(2, new FilterCreaturePermanent()), false
        ));
    }

    private AstralArenaPlane(final AstralArenaPlane plane) {
        super(plane);
    }

    @Override
    public AstralArenaPlane copy() {
        return new AstralArenaPlane(this);
    }
}

class AstralArenaAttackRestrictionEffect extends RestrictionEffect {

    AstralArenaAttackRestrictionEffect() {
        super(Duration.WhileOnBattlefield);
        staticText = "No more than one creature can attack each combat";
    }

    private AstralArenaAttackRestrictionEffect(final AstralArenaAttackRestrictionEffect effect) {
        super(effect);
    }

    @Override
    public AstralArenaAttackRestrictionEffect copy() {
        return new AstralArenaAttackRestrictionEffect(this);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        return game.getState().hasFaceUpPlane(Planes.PLANE_ASTRAL_ARENA);
    }

    @Override
    public boolean canAttack(Permanent attacker, UUID defenderId, Ability source, Game game, boolean canUseChooseDialogs) {
        return game.getCombat().getAttackers().isEmpty();
    }
}

class AstralArenaBlockRestrictionEffect extends RestrictionEffect {

    AstralArenaBlockRestrictionEffect() {
        super(Duration.WhileOnBattlefield);
        staticText = "No more than one creature can block each combat";
    }

    private AstralArenaBlockRestrictionEffect(final AstralArenaBlockRestrictionEffect effect) {
        super(effect);
    }

    @Override
    public AstralArenaBlockRestrictionEffect copy() {
        return new AstralArenaBlockRestrictionEffect(this);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        return game.getState().hasFaceUpPlane(Planes.PLANE_ASTRAL_ARENA);
    }

    @Override
    public boolean canBlock(Permanent attacker, Permanent newBlocker, Ability source, Game game, boolean canUseChooseDialogs) {
        if (attacker == null) {
            return true;
        }
        for (UUID creatureId : game.getCombat().getBlockers()) {
            Permanent existingBlocker = game.getPermanent(creatureId);
            if (existingBlocker != null
                    && existingBlocker.isControlledBy(newBlocker.getControllerId())
                    && game.getPlayer(existingBlocker.getControllerId())
                    .hasOpponent(attacker.getControllerId(), game)) {
                return false;
            }
        }
        return true;
    }
}
