package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.target.common.TargetCardInYourGraveyard;

/**
 * @author TheElk801
 */
public class ThePyramidOfMarsPlane extends Plane {

    public ThePyramidOfMarsPlane() {
        this.setPlaneType(Planes.PLANE_THE_PYRAMID_OF_MARS);

        // When you planeswalk to The Pyramid of Mars and at the beginning of your upkeep, surveil 2.
        this.getAbilities().add(new ThePyramidOfMarsTriggeredAbility());

        // Whenever chaos ensues, return target creature card from your graveyard to the battlefield.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new ReturnFromGraveyardToBattlefieldTargetEffect(), false
        );
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_CREATURE_YOUR_GRAVEYARD));
        this.getAbilities().add(ability);
    }

    private ThePyramidOfMarsPlane(final ThePyramidOfMarsPlane plane) {
        super(plane);
    }

    @Override
    public ThePyramidOfMarsPlane copy() {
        return new ThePyramidOfMarsPlane(this);
    }
}

class ThePyramidOfMarsTriggeredAbility extends TriggeredAbilityImpl {

    ThePyramidOfMarsTriggeredAbility() {
        super(Zone.COMMAND, new SurveilEffect(2));
        setTriggerPhrase("When you planeswalk to {this} and at the beginning of your upkeep, ");
    }

    private ThePyramidOfMarsTriggeredAbility(final ThePyramidOfMarsTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PLANESWALKED
                || event.getType() == GameEvent.EventType.UPKEEP_STEP_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.PLANESWALKED) {
            return getSourceId().equals(event.getTargetId());
        }
        return game.isActivePlayer(getControllerId());
    }

    @Override
    public ThePyramidOfMarsTriggeredAbility copy() {
        return new ThePyramidOfMarsTriggeredAbility(this);
    }
}
