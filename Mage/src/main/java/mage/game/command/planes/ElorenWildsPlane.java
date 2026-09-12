package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.AddContinuousEffectToGame;
import mage.abilities.effects.mana.AddManaOfAnyTypeProducedEffect;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.TriggeredManaAbility;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.events.TappedForManaEvent;
import mage.game.permanent.Permanent;
import mage.target.TargetPlayer;
import mage.target.targetpointer.FixedTarget;
import mage.watchers.Watcher;

import java.util.UUID;

/**
 * @author Codex
 */
public class ElorenWildsPlane extends Plane {

    public ElorenWildsPlane() {
        this.setPlaneType(Planes.PLANE_ELOREN_WILDS);

        // Whenever a player taps a permanent for mana, that player adds one mana
        // of any type that permanent produced.
        this.getAbilities().add(new ElorenWildsManaAbility());

        // Whenever chaos ensues, target player can't cast spells until a player planeswalks.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new AddContinuousEffectToGame(new ElorenWildsCantCastEffect()), false);
        ability.addTarget(new TargetPlayer());
        ability.addWatcher(new ElorenWildsPlaneswalkWatcher());
        this.getAbilities().add(ability);
    }

    private ElorenWildsPlane(final ElorenWildsPlane plane) {
        super(plane);
    }

    @Override
    public ElorenWildsPlane copy() {
        return new ElorenWildsPlane(this);
    }
}

class ElorenWildsManaAbility extends TriggeredManaAbility {

    ElorenWildsManaAbility() {
        super(Zone.COMMAND, makeEffect());
        setTriggerPhrase("Whenever a player taps a permanent for mana, ");
    }

    private static ManaEffect makeEffect() {
        ManaEffect effect = new AddManaOfAnyTypeProducedEffect();
        effect.setText("that player adds one mana of any type that permanent produced");
        return effect;
    }

    private ElorenWildsManaAbility(final ElorenWildsManaAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.TAPPED_FOR_MANA;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_ELOREN_WILDS)) {
            return false;
        }
        TappedForManaEvent manaEvent = (TappedForManaEvent) event;
        Permanent permanent = manaEvent.getPermanent();
        if (permanent == null) {
            return false;
        }
        getEffects().setValue("mana", manaEvent.getMana());
        getEffects().setValue("tappedPermanent", permanent);
        getEffects().setTargetPointer(new FixedTarget(permanent.getControllerId()));
        return true;
    }

    @Override
    public ElorenWildsManaAbility copy() {
        return new ElorenWildsManaAbility(this);
    }
}

class ElorenWildsCantCastEffect extends ContinuousRuleModifyingEffectImpl {

    private int planeswalkCount = -1;

    ElorenWildsCantCastEffect() {
        super(Duration.Custom, Outcome.Detriment);
        staticText = "Target player can't cast spells until a player planeswalks";
    }

    private ElorenWildsCantCastEffect(final ElorenWildsCantCastEffect effect) {
        super(effect);
        this.planeswalkCount = effect.planeswalkCount;
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        ElorenWildsPlaneswalkWatcher watcher = game.getState().getWatcher(ElorenWildsPlaneswalkWatcher.class);
        planeswalkCount = watcher == null ? 0 : watcher.getPlaneswalkCount();
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CAST_SPELL;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ElorenWildsPlaneswalkWatcher watcher = game.getState().getWatcher(ElorenWildsPlaneswalkWatcher.class);
        if (watcher != null && watcher.getPlaneswalkCount() > planeswalkCount) {
            discard();
            return false;
        }
        return event.getPlayerId().equals(getTargetPointer().getFirst(game, source));
    }

    @Override
    public ElorenWildsCantCastEffect copy() {
        return new ElorenWildsCantCastEffect(this);
    }
}

class ElorenWildsPlaneswalkWatcher extends Watcher {

    private int planeswalkCount;

    ElorenWildsPlaneswalkWatcher() {
        super(WatcherScope.GAME);
    }

    private ElorenWildsPlaneswalkWatcher(final ElorenWildsPlaneswalkWatcher watcher) {
        super(watcher);
        this.planeswalkCount = watcher.planeswalkCount;
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.PLANESWALKED) {
            planeswalkCount++;
        }
    }

    int getPlaneswalkCount() {
        return planeswalkCount;
    }

    @Override
    public ElorenWildsPlaneswalkWatcher copy() {
        return new ElorenWildsPlaneswalkWatcher(this);
    }
}
