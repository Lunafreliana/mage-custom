package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.UntapAllControllerEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

import java.util.Objects;

/**
 * @author spjspj
 */
public class EdgeOfMalacolPlane extends Plane {

    public EdgeOfMalacolPlane() {
        this.setPlaneType(Planes.PLANE_EDGE_OF_MALACOL);

        // If a creature you control would untap during your untap step, put two +1/+1 counters on it instead.
        SimpleStaticAbility ability = new SimpleStaticAbility(Zone.COMMAND, new EdgeOfMalacolEffect());
        this.getAbilities().add(ability);

        // Whenever chaos ensues, untap each creature you control
        Effect chaosEffect = new UntapAllControllerEffect(new FilterControlledCreaturePermanent(), "untap each creature you control");

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private EdgeOfMalacolPlane(final EdgeOfMalacolPlane plane) {
        super(plane);
    }

    @Override
    public EdgeOfMalacolPlane copy() {
        return new EdgeOfMalacolPlane(this);
    }
}

class EdgeOfMalacolEffect extends ContinuousRuleModifyingEffectImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent();

    public EdgeOfMalacolEffect() {
        super(Duration.Custom, Outcome.Detriment);
        this.staticText = "If a creature you control would untap during your untap step, put two +1/+1 counters on it instead";
    }

    protected EdgeOfMalacolEffect(final EdgeOfMalacolEffect effect) {
        super(effect);
    }

    @Override
    public EdgeOfMalacolEffect copy() {
        return new EdgeOfMalacolEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.UNTAP;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        // Prevent untap event of creatures of target player
        if (game.getTurnStepType() == PhaseStep.UNTAP) {
            if (!game.getState().hasFaceUpPlane(Planes.PLANE_EDGE_OF_MALACOL)) {
                return false;
            }
            Permanent permanent = game.getPermanent(event.getTargetId());
            if (filter.match(permanent, game)
                    && Objects.equals(source.getControllerId(), game.getActivePlayerId())
                    && Objects.equals(permanent.getControllerId(), source.getControllerId())) {
                Effect effect = new AddCountersTargetEffect(CounterType.P1P1.createInstance(2));
                effect.setTargetPointer(new FixedTarget(permanent, game));
                effect.apply(game, source);
                return true;
            }
        }
        return false;
    }
}
