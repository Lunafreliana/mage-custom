package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.abilities.effects.common.ReverseTurnOrderEffect;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TurnPhase;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.turn.TurnMod;

/**
 * @author The XMage Developers
 */
public final class TempleOfAtroposPlane extends Plane {

    public TempleOfAtroposPlane() {
        this.setPlaneType(Planes.PLANE_TEMPLE_OF_ATROPOS);

        // At the beginning of your postcombat main phase, there is an additional beginning phase after this
        // phase. (The beginning phase includes the untap, upkeep, and draw steps.)
        this.getAbilities().add(new TempleOfAtroposPostcombatMainTriggeredAbility());

        // When chaos ensues, reverse the game's turn order. Then planeswalk.
        Ability ability = new ChaosEnsuesTriggeredAbility(new ReverseTurnOrderEffect(), false);
        ability.addEffect(new PlaneswalkEffect(false).concatBy("Then"));
        this.getAbilities().add(ability);
    }

    private TempleOfAtroposPlane(final TempleOfAtroposPlane plane) {
        super(plane);
    }

    @Override
    public TempleOfAtroposPlane copy() {
        return new TempleOfAtroposPlane(this);
    }
}

class TempleOfAtroposPostcombatMainTriggeredAbility extends TriggeredAbilityImpl {

    TempleOfAtroposPostcombatMainTriggeredAbility() {
        super(Zone.COMMAND, new TempleOfAtroposAdditionalBeginningPhaseEffect());
        setTriggerPhrase("At the beginning of each of your postcombat main phases, ");
    }

    private TempleOfAtroposPostcombatMainTriggeredAbility(
            final TempleOfAtroposPostcombatMainTriggeredAbility ability
    ) {
        super(ability);
    }

    @Override
    public TempleOfAtroposPostcombatMainTriggeredAbility copy() {
        return new TempleOfAtroposPostcombatMainTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.POSTCOMBAT_MAIN_PHASE_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return game.getState().getFaceUpPlanarCards().stream()
                .anyMatch(card -> card.getId().equals(getSourceId()))
                && getControllerId().equals(game.getActivePlayerId());
    }
}

class TempleOfAtroposAdditionalBeginningPhaseEffect extends OneShotEffect {

    TempleOfAtroposAdditionalBeginningPhaseEffect() {
        super(Outcome.Benefit);
        staticText = "there is an additional beginning phase after this phase. "
                + "<i>(The beginning phase includes the untap, upkeep, and draw steps.)</i>";
    }

    private TempleOfAtroposAdditionalBeginningPhaseEffect(
            final TempleOfAtroposAdditionalBeginningPhaseEffect effect
    ) {
        super(effect);
    }

    @Override
    public TempleOfAtroposAdditionalBeginningPhaseEffect copy() {
        return new TempleOfAtroposAdditionalBeginningPhaseEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        game.getState().getTurnMods().add(
                new TurnMod(source.getControllerId()).withExtraPhase(TurnPhase.BEGINNING)
        );
        return true;
    }
}
