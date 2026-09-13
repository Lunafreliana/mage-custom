package mage.game.command.planes;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.common.continuous.ExchangeControlTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.triggers.AtStepTriggeredAbility;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetCreaturePermanent;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Plane - Glen Elendra
 */
public final class GlenElendraPlane extends Plane {

    public GlenElendraPlane() {
        this.setPlaneType(Planes.PLANE_GLEN_ELENDRA);

        // At end of combat, you may exchange control of target creature you control that dealt combat damage
        // to a player this combat and target creature that player controls.
        Ability ability = new GlenElendraEndOfCombatTriggeredAbility(
                new ExchangeControlTargetEffect(
                        Duration.EndOfGame,
                        "exchange control of target creature you control that dealt combat damage to a player "
                                + "this combat and target creature that player controls",
                        false, true
                )
        );
        ability.addTarget(new GlenElendraDamagingCreatureTarget());
        ability.addTarget(new GlenElendraDamagedPlayersCreatureTarget());
        ability.addWatcher(new GlenElendraCombatDamageWatcher());
        this.getAbilities().add(ability);

        // Whenever chaos ensues, gain control of target creature you own.
        Ability chaosAbility = new ChaosEnsuesTriggeredAbility(
                new GainControlTargetEffect(Duration.EndOfGame), false
        );
        chaosAbility.addTarget(new GlenElendraOwnedCreatureTarget());
        this.getAbilities().add(chaosAbility);
    }

    private GlenElendraPlane(final GlenElendraPlane plane) {
        super(plane);
    }

    @Override
    public GlenElendraPlane copy() {
        return new GlenElendraPlane(this);
    }
}

class GlenElendraEndOfCombatTriggeredAbility extends AtStepTriggeredAbility {

    GlenElendraEndOfCombatTriggeredAbility(ExchangeControlTargetEffect effect) {
        super(Zone.COMMAND, TargetController.ANY, effect, true);
    }

    private GlenElendraEndOfCombatTriggeredAbility(final GlenElendraEndOfCombatTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public GlenElendraEndOfCombatTriggeredAbility copy() {
        return new GlenElendraEndOfCombatTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.END_COMBAT_STEP_PRE;
    }

    @Override
    protected String generateTriggerPhrase() {
        return "At end of combat, ";
    }
}

class GlenElendraCombatDamageWatcher extends Watcher {

    private final Map<MageObjectReference, UUID> damagedPlayers = new HashMap<>();

    GlenElendraCombatDamageWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.END_COMBAT_STEP_POST) {
            damagedPlayers.clear();
            return;
        }
        if (event.getType() != GameEvent.EventType.DAMAGED_PLAYER
                || !((DamagedPlayerEvent) event).isCombatDamage()) {
            return;
        }
        Permanent permanent = game.getPermanent(event.getSourceId());
        if (permanent != null && permanent.isCreature(game)) {
            damagedPlayers.put(new MageObjectReference(permanent, game), event.getPlayerId());
        }
    }

    UUID getDamagedPlayer(UUID permanentId, Game game) {
        return damagedPlayers.entrySet().stream()
                .filter(entry -> entry.getKey().refersTo(permanentId, game))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}

class GlenElendraDamagingCreatureTarget extends TargetControlledCreaturePermanent {

    GlenElendraDamagingCreatureTarget() {
        withTargetName("creature you control that dealt combat damage to a player this combat");
    }

    private GlenElendraDamagingCreatureTarget(final GlenElendraDamagingCreatureTarget target) {
        super(target);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        GlenElendraCombatDamageWatcher watcher = game.getState().getWatcher(GlenElendraCombatDamageWatcher.class);
        return watcher != null && watcher.getDamagedPlayer(id, game) != null
                && super.canTarget(playerId, id, source, game);
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Ability source, Game game) {
        Set<UUID> possibleTargets = new HashSet<>(super.possibleTargets(sourceControllerId, source, game));
        GlenElendraCombatDamageWatcher watcher = game.getState().getWatcher(GlenElendraCombatDamageWatcher.class);
        possibleTargets.removeIf(id -> watcher == null || watcher.getDamagedPlayer(id, game) == null);
        return possibleTargets;
    }

    @Override
    public GlenElendraDamagingCreatureTarget copy() {
        return new GlenElendraDamagingCreatureTarget(this);
    }
}

class GlenElendraDamagedPlayersCreatureTarget extends TargetCreaturePermanent {

    GlenElendraDamagedPlayersCreatureTarget() {
        withTargetName("creature that player controls");
    }

    private GlenElendraDamagedPlayersCreatureTarget(final GlenElendraDamagedPlayersCreatureTarget target) {
        super(target);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        Permanent permanent = game.getPermanent(id);
        return permanent != null && permanent.isControlledBy(getDamagedPlayer(source, game))
                && super.canTarget(playerId, id, source, game);
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Ability source, Game game) {
        UUID damagedPlayerId = getDamagedPlayer(source, game);
        Set<UUID> possibleTargets = new HashSet<>(super.possibleTargets(sourceControllerId, source, game));
        possibleTargets.removeIf(id -> {
            Permanent permanent = game.getPermanent(id);
            return permanent == null || !permanent.isControlledBy(damagedPlayerId);
        });
        return possibleTargets;
    }

    private static UUID getDamagedPlayer(Ability source, Game game) {
        Permanent firstTarget = game.getPermanent(source.getTargets().get(0).getFirstTarget());
        GlenElendraCombatDamageWatcher watcher = game.getState().getWatcher(GlenElendraCombatDamageWatcher.class);
        return firstTarget == null || watcher == null ? null : watcher.getDamagedPlayer(firstTarget.getId(), game);
    }

    @Override
    public GlenElendraDamagedPlayersCreatureTarget copy() {
        return new GlenElendraDamagedPlayersCreatureTarget(this);
    }
}

class GlenElendraOwnedCreatureTarget extends TargetCreaturePermanent {

    GlenElendraOwnedCreatureTarget() {
        withTargetName("creature you own");
    }

    private GlenElendraOwnedCreatureTarget(final GlenElendraOwnedCreatureTarget target) {
        super(target);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        Permanent permanent = game.getPermanent(id);
        return permanent != null && permanent.isOwnedBy(source.getControllerId())
                && super.canTarget(playerId, id, source, game);
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Ability source, Game game) {
        Set<UUID> possibleTargets = new HashSet<>(super.possibleTargets(sourceControllerId, source, game));
        possibleTargets.removeIf(id -> {
            Permanent permanent = game.getPermanent(id);
            return permanent == null || !permanent.isOwnedBy(sourceControllerId);
        });
        return possibleTargets;
    }

    @Override
    public boolean chooseTarget(Outcome outcome, UUID playerId, Ability source, Game game) {
        return super.chooseTarget(Outcome.GainControl, playerId, source, game);
    }

    @Override
    public GlenElendraOwnedCreatureTarget copy() {
        return new GlenElendraOwnedCreatureTarget(this);
    }
}
