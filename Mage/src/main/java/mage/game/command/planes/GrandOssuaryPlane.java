package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.abilities.effects.common.counter.DistributeCountersEffect;
import mage.cards.Card;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.SaprolingToken;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanentAmount;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class GrandOssuaryPlane extends Plane {

    public GrandOssuaryPlane() {
        this.setPlaneType(Planes.PLANE_GRAND_OSSUARY);

        // Whenever a creature dies, its controller distributes a number of +1/+1 counters equal to
        // its power among any number of target creatures they control.
        Ability ability = new GrandOssuaryDiesTriggeredAbility();
        ability.addTarget(new TargetCreaturePermanentAmount(
                GrandOssuaryDiedCreaturePowerValue.instance,
                StaticFilters.FILTER_CONTROLLED_CREATURES
        ));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, each player exiles all creatures they control and creates X 1/1
        // green Saproling creature tokens, where X is the total power of the creatures they exiled
        // this way. Then planeswalk.
        ability = new ChaosEnsuesTriggeredAbility(new GrandOssuaryChaosEffect(), false);
        ability.addEffect(new PlaneswalkEffect(false));
        this.getAbilities().add(ability);
    }

    private GrandOssuaryPlane(final GrandOssuaryPlane plane) {
        super(plane);
    }

    @Override
    public GrandOssuaryPlane copy() {
        return new GrandOssuaryPlane(this);
    }
}

enum GrandOssuaryDiedCreaturePowerValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability source, Effect effect) {
        Object value = effect == null
                ? source.getEffects().get(0).getValue("diedCreaturePower")
                : effect.getValue("diedCreaturePower");
        return value instanceof Integer ? Math.max(0, (Integer) value) : 0;
    }

    @Override
    public GrandOssuaryDiedCreaturePowerValue copy() {
        return instance;
    }

    @Override
    public String toString() {
        return "the creature's power";
    }

    @Override
    public String getMessage() {
        return "the creature's power";
    }
}

class GrandOssuaryDiesTriggeredAbility extends TriggeredAbilityImpl {

    GrandOssuaryDiesTriggeredAbility() {
        super(Zone.COMMAND, new DistributeCountersEffect(), false);
        setLeavesTheBattlefieldTrigger(true);
        setTriggerPhrase("Whenever a creature dies, ");
    }

    private GrandOssuaryDiesTriggeredAbility(final GrandOssuaryDiesTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public GrandOssuaryDiesTriggeredAbility copy() {
        return new GrandOssuaryDiesTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        ZoneChangeEvent zoneChangeEvent = (ZoneChangeEvent) event;
        Permanent creature = zoneChangeEvent.getTarget();
        if (!zoneChangeEvent.isDiesEvent() || creature == null || !creature.isCreature(game)) {
            return false;
        }
        // Route the targeting and distribution choices through the creature's controller.
        setControllerId(creature.getControllerId());
        getEffects().setValue("diedCreaturePower", creature.getPower().getValue());
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever a creature dies, its controller distributes a number of +1/+1 counters "
                + "equal to its power among any number of target creatures they control.";
    }
}

class GrandOssuaryChaosEffect extends OneShotEffect {

    GrandOssuaryChaosEffect() {
        super(Outcome.Neutral);
        staticText = "each player exiles all creatures they control and creates X 1/1 green "
                + "Saproling creature tokens, where X is the total power of the creatures they "
                + "exiled this way";
    }

    private GrandOssuaryChaosEffect(final GrandOssuaryChaosEffect effect) {
        super(effect);
    }

    @Override
    public GrandOssuaryChaosEffect copy() {
        return new GrandOssuaryChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Map<UUID, Set<Card>> creaturesByController = new HashMap<>();
        Map<UUID, Map<UUID, Integer>> powerByController = new HashMap<>();

        // Snapshot power before anything leaves so continuous effects, Auras, Equipment, and
        // counters are included, as required by Grand Ossuary's ruling.
        for (Player player : game.getPlayers().values()) {
            Set<Card> creatures = new HashSet<>(game.getBattlefield().getAllActivePermanents(
                    StaticFilters.FILTER_CONTROLLED_CREATURES, player.getId(), game
            ));
            creaturesByController.put(player.getId(), creatures);
            Map<UUID, Integer> powers = new HashMap<>();
            creatures.stream()
                    .map(Permanent.class::cast)
                    .forEach(permanent -> powers.put(permanent.getId(), permanent.getPower().getValue()));
            powerByController.put(player.getId(), powers);
        }

        for (Player player : game.getPlayers().values()) {
            Set<Card> creatures = creaturesByController.get(player.getId());
            if (!creatures.isEmpty()) {
                player.moveCardsToExile(creatures, source, game, true, null, "Grand Ossuary");
            }
        }
        for (Player player : game.getPlayers().values()) {
            // A replacement may move a creature somewhere other than exile, in which case that
            // creature wasn't "exiled this way" and must not contribute to X.
            int power = Math.max(0, powerByController.get(player.getId()).entrySet().stream()
                    .filter(entry -> game.getState().getZone(entry.getKey()) == Zone.EXILED)
                    .mapToInt(Map.Entry::getValue)
                    .sum());
            if (power > 0) {
                new SaprolingToken().putOntoBattlefield(power, game, source, player.getId());
            }
        }
        return true;
    }
}
