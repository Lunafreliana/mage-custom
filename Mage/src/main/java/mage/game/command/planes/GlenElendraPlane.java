package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.common.continuous.ExchangeControlTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.triggers.AtStepTriggeredAbility;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetCreaturePermanent;
import mage.watchers.common.CombatDamageToPlayerThisCombatWatcher;

import java.util.HashSet;
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

class GlenElendraDamagingCreatureTarget extends TargetControlledCreaturePermanent {

    GlenElendraDamagingCreatureTarget() {
        withTargetName("creature you control that dealt combat damage to a player this combat");
    }

    private GlenElendraDamagingCreatureTarget(final GlenElendraDamagingCreatureTarget target) {
        super(target);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        CombatDamageToPlayerThisCombatWatcher watcher = game.getState()
                .getWatcher(CombatDamageToPlayerThisCombatWatcher.class);
        return watcher != null && watcher.getDamagedPlayer(id, game) != null
                && super.canTarget(playerId, id, source, game);
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Ability source, Game game) {
        Set<UUID> possibleTargets = new HashSet<>(super.possibleTargets(sourceControllerId, source, game));
        CombatDamageToPlayerThisCombatWatcher watcher = game.getState()
                .getWatcher(CombatDamageToPlayerThisCombatWatcher.class);
        possibleTargets.removeIf(id -> watcher == null || watcher.getDamagedPlayer(id, game) == null
                || !GlenElendraDamagedPlayersCreatureTarget.canChooseForCreature(
                        id, sourceControllerId, source, game));
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
    public boolean canChoose(UUID sourceControllerId, Ability source, Game game) {
        // The engine checks every target before choosing the first one. Check
        // that a complete legal pair exists without changing the actual targets.
        if (source.getTargets().get(0).getFirstTarget() == null) {
            return !source.getTargets().get(0).possibleTargets(sourceControllerId, source, game).isEmpty();
        }
        return super.canChoose(sourceControllerId, source, game);
    }

    static boolean canChooseForCreature(UUID creatureId, UUID sourceControllerId, Ability source, Game game) {
        CombatDamageToPlayerThisCombatWatcher watcher = game.getState()
                .getWatcher(CombatDamageToPlayerThisCombatWatcher.class);
        UUID damagedPlayerId = watcher == null ? null : watcher.getDamagedPlayer(creatureId, game);
        if (damagedPlayerId == null) {
            return false;
        }
        // Use the ordinary target implementation here to include protection,
        // hexproof and shroud without recursing into the dependent target.
        return new TargetCreaturePermanent().possibleTargets(sourceControllerId, source, game).stream()
                .map(game::getPermanent)
                .anyMatch(permanent -> permanent != null && permanent.isControlledBy(damagedPlayerId));
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
        CombatDamageToPlayerThisCombatWatcher watcher = game.getState()
                .getWatcher(CombatDamageToPlayerThisCombatWatcher.class);
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
