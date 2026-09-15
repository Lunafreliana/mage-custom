package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.target.common.TargetAnyTarget;
import mage.util.CardUtil;

/**
 * @author The XMage Developers
 */
public final class StrongholdFurnacePlane extends Plane {

    public StrongholdFurnacePlane() {
        this.setPlaneType(Planes.PLANE_STRONGHOLD_FURNACE);

        // If a source would deal damage to a permanent or player, it deals double that damage instead.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new StrongholdFurnaceDoubleDamageEffect()
        ));

        // Whenever chaos ensues, Stronghold Furnace deals 1 damage to any target.
        Ability ability = new ChaosEnsuesTriggeredAbility(new DamageTargetEffect(1), false);
        ability.addTarget(new TargetAnyTarget());
        this.getAbilities().add(ability);
    }

    private StrongholdFurnacePlane(final StrongholdFurnacePlane plane) {
        super(plane);
    }

    @Override
    public StrongholdFurnacePlane copy() {
        return new StrongholdFurnacePlane(this);
    }
}

class StrongholdFurnaceDoubleDamageEffect extends ReplacementEffectImpl {

    StrongholdFurnaceDoubleDamageEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Damage);
        staticText = "If a source would deal damage to a permanent or player, "
                + "it deals double that damage instead";
    }

    private StrongholdFurnaceDoubleDamageEffect(final StrongholdFurnaceDoubleDamageEffect effect) {
        super(effect);
    }

    @Override
    public StrongholdFurnaceDoubleDamageEffect copy() {
        return new StrongholdFurnaceDoubleDamageEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGE_PLAYER
                || event.getType() == GameEvent.EventType.DAMAGE_PERMANENT;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return game.getState().hasFaceUpPlane(Planes.PLANE_STRONGHOLD_FURNACE);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmount(CardUtil.overflowMultiply(event.getAmount(), 2));
        return false;
    }
}
