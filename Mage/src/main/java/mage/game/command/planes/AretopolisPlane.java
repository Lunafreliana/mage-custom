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
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.players.Player;

/**
 * @author The XMage Developers
 */
public final class AretopolisPlane extends Plane {

    private int scrollCounters;

    public AretopolisPlane() {
        this.setPlaneType(Planes.PLANE_ARETOPOLIS);

        // When you planeswalk to Aretopolis and at the beginning of your upkeep,
        // put a scroll counter on Aretopolis, then you gain life equal to the
        // number of scroll counters on it.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new AretopolisCounterEffect(false)));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new AretopolisCounterEffect(false), false
        ));

        // When Aretopolis has ten or more scroll counters on it, planeswalk.
        this.getAbilities().add(new AretopolisStateTriggeredAbility());

        // Whenever chaos ensues, put a scroll counter on Aretopolis, then draw
        // cards equal to the number of scroll counters on it.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new AretopolisCounterEffect(true), false));
    }

    private AretopolisPlane(final AretopolisPlane plane) {
        super(plane);
        this.scrollCounters = plane.scrollCounters;
    }

    public int getScrollCounters() {
        return scrollCounters;
    }

    void addScrollCounter() {
        scrollCounters++;
    }

    @Override
    public void setFaceUp(boolean faceUp) {
        if (isFaceUp() && !faceUp) {
            scrollCounters = 0;
        }
        super.setFaceUp(faceUp);
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
        staticText = "put a scroll counter on {this}, then you "
                + (drawCards ? "draw cards" : "gain life")
                + " equal to the number of scroll counters on it";
    }

    private AretopolisCounterEffect(final AretopolisCounterEffect effect) {
        super(effect);
        this.drawCards = effect.drawCards;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        AretopolisPlane plane = game.getState().getFaceUpPlanarCards().stream()
                .filter(card -> source.getSourceId().equals(card.getId()))
                .filter(AretopolisPlane.class::isInstance)
                .map(AretopolisPlane.class::cast)
                .findFirst()
                .orElse(null);
        Player player = game.getPlayer(source.getControllerId());
        if (plane == null || player == null) {
            return false;
        }
        plane.addScrollCounter();
        int count = plane.getScrollCounters();
        if (drawCards) {
            player.drawCards(count, source, game);
        } else {
            player.gainLife(count, game, source);
        }
        return true;
    }

    @Override
    public AretopolisCounterEffect copy() {
        return new AretopolisCounterEffect(this);
    }
}

class AretopolisStateTriggeredAbility extends StateTriggeredAbility {

    AretopolisStateTriggeredAbility() {
        super(Zone.COMMAND, new PlaneswalkEffect(false));
        setTriggerPhrase("When {this} has ten or more scroll counters on it, ");
    }

    private AretopolisStateTriggeredAbility(final AretopolisStateTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return game.getState().getFaceUpPlanarCards().stream()
                .filter(card -> getSourceId().equals(card.getId()))
                .filter(AretopolisPlane.class::isInstance)
                .map(AretopolisPlane.class::cast)
                .anyMatch(plane -> plane.getScrollCounters() >= 10);
    }

    @Override
    public AretopolisStateTriggeredAbility copy() {
        return new AretopolisStateTriggeredAbility(this);
    }
}
