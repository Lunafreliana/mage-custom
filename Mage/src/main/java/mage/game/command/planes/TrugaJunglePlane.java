package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.IsStillOnPlaneCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.RevealLibraryPutIntoHandEffect;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.common.FilterLandCard;
import mage.game.command.Plane;

/**
 * @author spjspj
 */
public class TrugaJunglePlane extends Plane {

    private static final String rule = "All lands have '{t}: Add one mana of any color";

    public TrugaJunglePlane() {
        this.setPlaneType(Planes.PLANE_TRUGA_JUNGLE);

        SimpleStaticAbility ability
                = new SimpleStaticAbility(Zone.COMMAND, new ConditionalContinuousEffect(
                new GainAbilityAllEffect(new AnyColorManaAbility(), Duration.Custom, StaticFilters.FILTER_LANDS),
                new IsStillOnPlaneCondition(this.getName()),
                rule));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, reveal the top three cards of your libary.  Put all land cards revealed this way into your hand the rest on the bottom of your library in any order.
        Effect chaosEffect = new RevealLibraryPutIntoHandEffect(3, new FilterLandCard(), Zone.LIBRARY);

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private TrugaJunglePlane(final TrugaJunglePlane plane) {
        super(plane);
    }

    @Override
    public TrugaJunglePlane copy() {
        return new TrugaJunglePlane(this);
    }
}
