package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.StateTriggeredAbility;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.players.Player;

/**
 * @author The XMage Developers
 */
public final class AretopolisPlane extends Plane {

    public AretopolisPlane() {
        this.setPlaneType(Planes.PLANE_ARETOPOLIS);

        // When you planeswalk to Aretopolis and at the beginning of your upkeep, put a scroll counter
        // on Aretopolis, then you gain life equal to the number of scroll counters on it.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new AretopolisCounterEffect(false)));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new AretopolisCounterEffect(false), false
        ));

        // When Aretopolis has ten or more scroll counters on it, planeswalk.
        this.getAbilities().add(new AretopolisStateTriggeredAbility());

        // Whenever chaos ensues, put a scroll counter on Aretopolis, then draw cards equal to the
        // number of scroll counters on it.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new AretopolisCounterEffect(true), false));
    }

    private AretopolisPlane(final AretopolisPlane plane) {
        super(plane);
    }

    @Override
    public AretopolisPlane copy() {
        return new AretopolisPlane(this);
    }
}

class AretopolisCounterEffect extends OneShotEffect {

    private final boolean drawCards;

    AretopolisCounterEffect(boolean drawCards) {
        super(drawCards ? Outcome.DrawCard : Outcome.GainLife);
        this.drawCards = drawCards;
        staticText = "put a scroll counter on {this}, then "
                + (drawCards ? "draw cards" : "you gain life")
                + " equal to the number of scroll counters on it";
    }

    private AretopolisCounterEffect(final AretopolisCounterEffect effect) {
        super(effect);
        this.drawCards = effect.drawCards;
    }

    @Override
    public AretopolisCounterEffect copy() {
        return new AretopolisCounterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Plane plane = game.getState().getFaceUpPlanes().stream()
                .filter(candidate -> candidate.getId().equals(source.getSourceId()))
                .findFirst()
                .orElse(null);
        Player controller = game.getPlayer(source.getControllerId());
        if (plane == null || controller == null) {
            return false;
        }
        plane.addCounter(CounterType.SCROLL.createInstance());
        int count = plane.getCounters().getCount(CounterType.SCROLL);
        if (drawCards) {
            controller.drawCards(count, source, game);
        } else {
            controller.gainLife(count, game, source);
        }
        return true;
    }
}

class AretopolisStateTriggeredAbility extends StateTriggeredAbility {

    AretopolisStateTriggeredAbility() {
        super(Zone.COMMAND, new PlaneswalkEffect(false));
    }

    private AretopolisStateTriggeredAbility(final AretopolisStateTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public AretopolisStateTriggeredAbility copy() {
        return new AretopolisStateTriggeredAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return game.getState().getFaceUpPlanes().stream()
                .filter(plane -> plane.getId().equals(getSourceId()))
                .anyMatch(plane -> plane.getCounters().getCount(CounterType.SCROLL) >= 10);
    }

    @Override
    public String getRule() {
        return "When {this} has ten or more scroll counters on it, planeswalk.";
    }
}
