package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SpellCastAllTriggeredAbility;
import mage.abilities.effects.common.DrawCardTargetEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.mageobject.HistoricPredicate;
import mage.game.command.Plane;
import mage.target.common.TargetCardInYourGraveyard;

/**
 * @author The XMage Developers
 */
public final class CoalHillSchoolPlane extends Plane {

    private static final FilterCard filter = new FilterCard("historic card in your graveyard");

    static {
        filter.add(HistoricPredicate.instance);
    }

    public CoalHillSchoolPlane() {
        this.setPlaneType(Planes.PLANE_COAL_HILL_SCHOOL);

        // Whenever a player casts a historic spell, that player draws a card.
        this.getAbilities().add(new SpellCastAllTriggeredAbility(
                Zone.COMMAND, new DrawCardTargetEffect(1), StaticFilters.FILTER_SPELL_HISTORIC,
                false, SetTargetPointer.PLAYER
        ));

        // Whenever chaos ensues, return target historic card from your graveyard to your hand.
        Ability ability = new ChaosEnsuesTriggeredAbility(new ReturnFromGraveyardToHandTargetEffect(), false);
        ability.addTarget(new TargetCardInYourGraveyard(filter));
        this.getAbilities().add(ability);
    }

    private CoalHillSchoolPlane(final CoalHillSchoolPlane plane) {
        super(plane);
    }

    @Override
    public CoalHillSchoolPlane copy() {
        return new CoalHillSchoolPlane(this);
    }
}
