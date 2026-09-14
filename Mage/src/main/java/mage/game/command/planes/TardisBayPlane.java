package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.keyword.CascadeAbility;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubLayer;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.target.common.TargetArtifactPermanent;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class TardisBayPlane extends Plane {

    public TardisBayPlane() {
        this.setPlaneType(Planes.PLANE_TARDIS_BAY);

        // The first spell you cast during each of your turns with mana value 2 or greater has cascade.
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new TardisBayCascadeEffect());
        ability.addWatcher(new TardisBayFirstQualifyingSpellWatcher());
        this.getAbilities().add(ability);

        // When chaos ensues, gain control of target artifact. Then planeswalk.
        ability = new ChaosEnsuesTriggeredAbility(
                new GainControlTargetEffect(Duration.EndOfGame, true), false
        );
        ability.addTarget(new TargetArtifactPermanent());
        ability.addEffect(new PlaneswalkEffect(false).concatBy("Then"));
        this.getAbilities().add(ability);
    }

    private TardisBayPlane(final TardisBayPlane plane) {
        super(plane);
    }

    @Override
    public TardisBayPlane copy() {
        return new TardisBayPlane(this);
    }
}

class TardisBayCascadeEffect extends ContinuousEffectImpl {

    private final Ability cascadeAbility;

    TardisBayCascadeEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.cascadeAbility = new CascadeAbility(false);
        staticText = "The first spell you cast during each of your turns with mana value 2 or greater has cascade";
    }

    private TardisBayCascadeEffect(final TardisBayCascadeEffect effect) {
        super(effect);
        this.cascadeAbility = effect.cascadeAbility.copy();
    }

    @Override
    public TardisBayCascadeEffect copy() {
        return new TardisBayCascadeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        UUID controllerId = source.getControllerId();
        if (!controllerId.equals(game.getActivePlayerId())
                || game.getState().getFaceUpPlanarCards().stream()
                .noneMatch(card -> card.getId().equals(source.getSourceId()))) {
            return false;
        }
        TardisBayFirstQualifyingSpellWatcher watcher = game.getState()
                .getWatcher(TardisBayFirstQualifyingSpellWatcher.class);
        if (watcher == null) {
            return false;
        }
        UUID firstQualifyingSpellId = watcher.getFirstQualifyingSpellId(controllerId);
        if (firstQualifyingSpellId == null) {
            return true;
        }
        for (StackObject stackObject : game.getStack()) {
            if (!(stackObject instanceof Spell)
                    || !stackObject.getId().equals(firstQualifyingSpellId)) {
                continue;
            }
            Card card = game.getCard(stackObject.getSourceId());
            if (card != null) {
                game.getState().addOtherAbility(card, cascadeAbility);
            }
        }
        return true;
    }
}

class TardisBayFirstQualifyingSpellWatcher extends Watcher {

    private final Map<UUID, UUID> firstQualifyingSpellByPlayer = new HashMap<>();

    TardisBayFirstQualifyingSpellWatcher() {
        super(WatcherScope.GAME);
    }

    private TardisBayFirstQualifyingSpellWatcher(final TardisBayFirstQualifyingSpellWatcher watcher) {
        super(watcher);
        this.firstQualifyingSpellByPlayer.putAll(watcher.firstQualifyingSpellByPlayer);
    }

    @Override
    public TardisBayFirstQualifyingSpellWatcher copy() {
        return new TardisBayFirstQualifyingSpellWatcher(this);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.CAST_SPELL
                && event.getType() != GameEvent.EventType.SPELL_CAST) {
            return;
        }
        Spell spell = (Spell) game.getObject(event.getTargetId());
        if (spell == null
                || spell.getManaValue() < 2
                || !spell.getControllerId().equals(game.getActivePlayerId())) {
            return;
        }
        firstQualifyingSpellByPlayer.putIfAbsent(spell.getControllerId(), spell.getId());
    }

    @Override
    public void reset() {
        super.reset();
        firstQualifyingSpellByPlayer.clear();
    }

    UUID getFirstQualifyingSpellId(UUID playerId) {
        return firstQualifyingSpellByPlayer.get(playerId);
    }
}
