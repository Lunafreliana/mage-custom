package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetCardInYourGraveyard;

/**
 * @author The XMage Developers
 */
public final class ThePyramidOfMarsPlane extends Plane {

    public ThePyramidOfMarsPlane() {
        this.setPlaneType(Planes.PLANE_THE_PYRAMID_OF_MARS);

        // When you planeswalk to The Pyramid of Mars and at the beginning of your upkeep, surveil 2.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new SurveilEffect(2)));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new SurveilEffect(2), false
        ));

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
