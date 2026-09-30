package mage.game.command.planes;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetPlayerOrPlaneswalker;
import mage.target.targetpointer.FixedTarget;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class StensiaPlane extends Plane {

    public StensiaPlane() {
        this.setPlaneType(Planes.PLANE_STENSIA);

        // Whenever a creature deals damage to one or more players for the first time each turn,
        // put a +1/+1 counter on it.
        this.getAbilities().add(new StensiaTriggeredAbility());

        // Whenever chaos ensues, each creature you control gains
        // "{T}: This creature deals 1 damage to target player or planeswalker" until end of turn.
        Ability gainedAbility = new SimpleActivatedAbility(
                new DamageTargetEffect(1, "this creature"), new TapSourceCost()
        );
        gainedAbility.addTarget(new TargetPlayerOrPlaneswalker());
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new GainAbilityControlledEffect(
                gainedAbility, Duration.EndOfTurn, StaticFilters.FILTER_CONTROLLED_CREATURES
        ), false));
    }

    private StensiaPlane(final StensiaPlane plane) {
        super(plane);
    }

    @Override
    public StensiaPlane copy() {
        return new StensiaPlane(this);
    }
}

class StensiaTriggeredAbility extends TriggeredAbilityImpl {

    StensiaTriggeredAbility() {
        super(Zone.COMMAND, null, false);
        setTriggerPhrase("Whenever a creature deals damage to one or more players for the first time each turn, ");
        addWatcher(new StensiaWatcher());
    }

    private StensiaTriggeredAbility(final StensiaTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public StensiaTriggeredAbility copy() {
        return new StensiaTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_STENSIA)) {
            return false;
        }
        Permanent creature = game.getPermanentOrLKIBattlefield(event.getSourceId());
        if (creature == null || !creature.isCreature(game)
                || !StensiaWatcher.isFirstDamageEvent(event, game)) {
            return false;
        }
        getEffects().clear();
        AddCountersTargetEffect effect = new AddCountersTargetEffect(CounterType.P1P1.createInstance());
        effect.setTargetPointer(new FixedTarget(creature, game));
        addEffect(effect);
        return true;
    }
}

class StensiaWatcher extends Watcher {

    private final Map<MageObjectReference, UUID> firstDamageEvent = new HashMap<>();

    StensiaWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.DAMAGED_PLAYER) {
            return;
        }
        Permanent creature = game.getPermanentOrLKIBattlefield(event.getSourceId());
        if (creature != null && creature.isCreature(game)) {
            firstDamageEvent.putIfAbsent(new MageObjectReference(creature, game), event.getId());
        }
    }

    @Override
    public void reset() {
        super.reset();
        firstDamageEvent.clear();
    }

    static boolean isFirstDamageEvent(GameEvent event, Game game) {
        StensiaWatcher watcher = game.getState().getWatcher(StensiaWatcher.class);
        Permanent creature = game.getPermanentOrLKIBattlefield(event.getSourceId());
        return watcher != null && creature != null && Objects.equals(
                event.getId(), watcher.firstDamageEvent.get(new MageObjectReference(creature, game))
        );
    }
}
