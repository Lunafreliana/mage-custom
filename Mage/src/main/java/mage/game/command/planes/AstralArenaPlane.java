package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.effects.common.DamageAllEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.watchers.common.AttackedThisTurnWatcher;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public class AstralArenaPlane extends Plane {

    public AstralArenaPlane() {
        this.setPlaneType(Planes.PLANE_ASTRAL_ARENA);

        // No more than one creature can attack each turn.  No more than one creature can block each turn.
        SimpleStaticAbility ability = new SimpleStaticAbility(Zone.COMMAND, new AstralArenaAttackRestrictionEffect());
        ability.addWatcher(new AttackedThisTurnWatcher());
        SimpleStaticAbility ability2 = new SimpleStaticAbility(Zone.COMMAND, new AstralArenaBlockRestrictionEffect());
        ability2.addWatcher(new AttackedThisTurnWatcher());
        this.getAbilities().add(ability);
        this.getAbilities().add(ability2);

        // Whenever chaos ensues, {this} deals 2 damage to each creature
        Effect chaosEffect = new DamageAllEffect(2, new FilterCreaturePermanent());

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
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

    AstralArenaAttackRestrictionEffect(final AstralArenaAttackRestrictionEffect effect) {
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

    AstralArenaBlockRestrictionEffect(final AstralArenaBlockRestrictionEffect effect) {
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
    public boolean canBlock(Permanent attacker, Permanent blocker, Ability source, Game game, boolean canUseChooseDialogs) {
        return game.getCombat().getBlockers().isEmpty();
    }
}
