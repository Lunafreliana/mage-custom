package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.AddContinuousEffectToGame;
import mage.abilities.effects.common.combat.CantAttackAllEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.IslandwalkAbility;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.watchers.common.PlaneswalkedWatcher;

/**
 * @author Codex
 */
public final class CelestineReefPlane extends Plane {

    private static final FilterCreaturePermanent filter
            = new FilterCreaturePermanent("creatures without flying or islandwalk");

    static {
        filter.add(Predicates.not(new AbilityPredicate(FlyingAbility.class)));
        filter.add(Predicates.not(new AbilityPredicate(IslandwalkAbility.class)));
    }

    public CelestineReefPlane() {
        this.setPlaneType(Planes.PLANE_CELESTINE_REEF);

        // Creatures without flying or islandwalk can't attack.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new CantAttackAllEffect(Duration.WhileOnBattlefield, filter)
        ));

        // Whenever chaos ensues, until a player planeswalks, you can't lose the
        // game and your opponents can't win the game.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new AddContinuousEffectToGame(new CelestineReefCantLoseEffect()), false
        ));
    }

    private CelestineReefPlane(final CelestineReefPlane plane) {
        super(plane);
    }

    @Override
    public CelestineReefPlane copy() {
        return new CelestineReefPlane(this);
    }
}

class CelestineReefCantLoseEffect extends ContinuousRuleModifyingEffectImpl {

    private int planeswalkCount = -1;

    CelestineReefCantLoseEffect() {
        super(Duration.Custom, Outcome.Benefit, false, false);
        staticText = "Until a player planeswalks, you can't lose the game "
                + "and your opponents can't win the game";
    }

    private CelestineReefCantLoseEffect(final CelestineReefCantLoseEffect effect) {
        super(effect);
        this.planeswalkCount = effect.planeswalkCount;
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        PlaneswalkedWatcher watcher = game.getState().getWatcher(PlaneswalkedWatcher.class);
        planeswalkCount = watcher == null ? 0 : watcher.getCount();
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.WINS
                || event.getType() == GameEvent.EventType.LOSES;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        PlaneswalkedWatcher watcher = game.getState().getWatcher(PlaneswalkedWatcher.class);
        if (watcher != null && watcher.getCount() > planeswalkCount) {
            discard();
            return false;
        }
        if (event.getType() == GameEvent.EventType.WINS) {
            return game.getOpponents(source.getControllerId()).contains(event.getPlayerId());
        }
        return source.isControlledBy(event.getPlayerId());
    }

    @Override
    public CelestineReefCantLoseEffect copy() {
        return new CelestineReefCantLoseEffect(this);
    }
}
