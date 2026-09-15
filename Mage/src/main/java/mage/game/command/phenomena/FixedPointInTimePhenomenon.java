package mage.game.command.phenomena;

import mage.abilities.Ability;
import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Phenomena;
import mage.game.Game;
import mage.game.command.Phenomenon;
import mage.game.events.GameEvent;

/**
 * @author VibecodingQueens
 */
public final class FixedPointInTimePhenomenon extends Phenomenon {

    public FixedPointInTimePhenomenon() {
        super(Phenomena.FIXED_POINT_IN_TIME.getFullName());

        // When you encounter Fixed Point in Time, until your next turn, if a player would planeswalk
        // as a result of rolling the planar die, chaos ensues instead.
        this.getAbilities().add(new EncounterPhenomenonTriggeredAbility(new FixedPointInTimeEffect()));
    }

    private FixedPointInTimePhenomenon(final FixedPointInTimePhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public FixedPointInTimePhenomenon copy() {
        return new FixedPointInTimePhenomenon(this);
    }
}

class FixedPointInTimeEffect extends OneShotEffect {

    FixedPointInTimeEffect() {
        super(Outcome.Neutral);
        staticText = "until your next turn, if a player would planeswalk as a result of rolling "
                + "the planar die, chaos ensues instead";
    }

    private FixedPointInTimeEffect(final FixedPointInTimeEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        game.addEffect(new FixedPointInTimeReplacementEffect(), source);
        return true;
    }

    @Override
    public FixedPointInTimeEffect copy() {
        return new FixedPointInTimeEffect(this);
    }
}

class FixedPointInTimeReplacementEffect extends ReplacementEffectImpl {

    FixedPointInTimeReplacementEffect() {
        super(Duration.UntilYourNextTurn, Outcome.Neutral);
    }

    private FixedPointInTimeReplacementEffect(final FixedPointInTimeReplacementEffect effect) {
        super(effect);
    }

    @Override
    public FixedPointInTimeReplacementEffect copy() {
        return new FixedPointInTimeReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PLANESWALK_FROM_PLANAR_DIE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return true;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        return new ChaosEnsuesEffect().apply(game, source);
    }
}
