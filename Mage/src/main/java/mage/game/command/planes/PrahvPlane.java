package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.CardsInControllerHandCount;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.watchers.common.AttackedThisTurnWatcher;
import mage.watchers.common.CastSpellLastTurnWatcher;

/**
 * @author JayDi85
 */
public final class PrahvPlane extends Plane {

    public PrahvPlane() {
        this.setPlaneType(Planes.PLANE_PRAHV);

        // If you cast a spell this turn, you can't attack with creatures.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new PrahvCantAttackEffect()
        ));

        // If you attacked with creatures this turn, you can't cast spells.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new PrahvCantCastEffect()
        ));

        // Whenever chaos ensues, you gain life equal to the number of cards in your hand.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new GainLifeEffect(CardsInControllerHandCount.ANY), false
        ));
    }

    private PrahvPlane(final PrahvPlane plane) {
        super(plane);
    }

    @Override
    public PrahvPlane copy() {
        return new PrahvPlane(this);
    }
}

class PrahvCantAttackEffect extends RestrictionEffect {

    PrahvCantAttackEffect() {
        super(Duration.WhileOnBattlefield);
        staticText = "If you cast a spell this turn, you can't attack with creatures";
    }

    private PrahvCantAttackEffect(final PrahvCantAttackEffect effect) {
        super(effect);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        CastSpellLastTurnWatcher watcher = game.getState().getWatcher(CastSpellLastTurnWatcher.class);
        return permanent.isCreature(game)
                && permanent.isControlledBy(source.getControllerId())
                && watcher != null
                && watcher.getAmountOfSpellsPlayerCastOnCurrentTurn(source.getControllerId()) > 0;
    }

    @Override
    public boolean canAttack(Game game, boolean canUseChooseDialogs) {
        return false;
    }

    @Override
    public PrahvCantAttackEffect copy() {
        return new PrahvCantAttackEffect(this);
    }
}

class PrahvCantCastEffect extends ContinuousRuleModifyingEffectImpl {

    PrahvCantCastEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "If you attacked with creatures this turn, you can't cast spells";
    }

    private PrahvCantCastEffect(final PrahvCantCastEffect effect) {
        super(effect);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CAST_SPELL;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        AttackedThisTurnWatcher watcher = game.getState().getWatcher(AttackedThisTurnWatcher.class);
        return event.getPlayerId().equals(source.getControllerId())
                && watcher != null
                && watcher.getAttackedThisTurnCreaturesPermanentLKI().stream()
                .anyMatch(permanent -> permanent.isControlledBy(source.getControllerId()));
    }

    @Override
    public PrahvCantCastEffect copy() {
        return new PrahvCantCastEffect(this);
    }
}
