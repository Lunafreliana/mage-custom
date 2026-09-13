package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.abilities.effects.common.counter.DistributeCountersEffect;
import mage.cards.Cards;
import mage.cards.CardsImpl;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class GrandOssuaryPlane extends Plane {

    public GrandOssuaryPlane() {
        this.setPlaneType(Planes.PLANE_GRAND_OSSUARY);

        // Whenever a creature dies, its controller distributes a number of +1/+1 counters equal to its power
        // among any number of target creatures they control.
        this.getAbilities().add(new GrandOssuaryDiesTriggeredAbility());

        // Whenever chaos ensues, each player exiles all creatures they control and creates X 1/1 green Saproling
        // creature tokens, where X is the total power of the creatures they exiled this way. Then planeswalk.
        Ability ability = new ChaosEnsuesTriggeredAbility(new GrandOssuaryChaosEffect(), false);
        ability.addEffect(new PlaneswalkEffect(false).concatBy("Then"));
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

class GrandOssuaryDiesTriggeredAbility extends TriggeredAbilityImpl {

    GrandOssuaryDiesTriggeredAbility() {
        super(Zone.COMMAND, new DistributeCountersEffect(), false);
        setTriggerPhrase("Whenever a creature dies, its controller ");
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

        setControllerId(creature.getControllerId());
        getTargets().clear();
        int power = Math.max(0, creature.getPower().getValue());
        if (power > 0) {
            getTargets().add(new TargetCreaturePermanentAmount(
                    power, 0, power, StaticFilters.FILTER_CONTROLLED_CREATURE
            ));
        }
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever a creature dies, its controller distributes a number of +1/+1 counters equal to its power "
                + "among any number of target creatures they control.";
    }
}

class GrandOssuaryChaosEffect extends OneShotEffect {

    GrandOssuaryChaosEffect() {
        super(Outcome.Exile);
        staticText = "each player exiles all creatures they control and creates X 1/1 green Saproling creature "
                + "tokens, where X is the total power of the creatures they exiled this way";
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
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        Cards creatures = new CardsImpl();
        Map<UUID, Map<UUID, Integer>> powersByController = new LinkedHashMap<>();
        for (Permanent creature : game.getBattlefield().getActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURES, source.getControllerId(), source, game
        )) {
            creatures.add(creature);
            powersByController.computeIfAbsent(creature.getControllerId(), ignored -> new LinkedHashMap<>())
                    .put(creature.getId(), creature.getPower().getValue());
        }

        controller.moveCards(creatures, Zone.EXILED, source, game);
        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            int totalPower = powersByController.getOrDefault(playerId, new LinkedHashMap<>()).entrySet().stream()
                    .filter(entry -> Zone.EXILED.equals(game.getState().getZone(entry.getKey())))
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            if (totalPower > 0) {
                new SaprolingToken().putOntoBattlefield(totalPower, game, source, playerId);
            }
        }
        return true;
    }
}
