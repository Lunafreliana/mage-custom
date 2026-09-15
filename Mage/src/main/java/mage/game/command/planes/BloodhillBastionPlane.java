package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.EntersBattlefieldAllTriggeredAbility;
import mage.abilities.effects.common.ExileThenReturnTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.abilities.keyword.HasteAbility;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class BloodhillBastionPlane extends Plane {

    public BloodhillBastionPlane() {
        this.setPlaneType(Planes.PLANE_BLOODHILL_BASTION);

        // Whenever a creature enters, it gains double strike and haste until end of turn.
        Ability ability = new EntersBattlefieldAllTriggeredAbility(
                Zone.COMMAND,
                new GainAbilityTargetEffect(DoubleStrikeAbility.getInstance())
                        .setText("it gains double strike"),
                StaticFilters.FILTER_PERMANENT_A_CREATURE, false, SetTargetPointer.PERMANENT
        );
        ability.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance())
                .setText("and haste until end of turn"));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, exile target nontoken creature you control, then return it
        // to the battlefield under your control.
        ability = new ChaosEnsuesTriggeredAbility(new ExileThenReturnTargetEffect(true, false), false);
        ability.addTarget(new TargetCreaturePermanent(StaticFilters.FILTER_CONTROLLED_CREATURE_NON_TOKEN));
        this.getAbilities().add(ability);
    }

    private BloodhillBastionPlane(final BloodhillBastionPlane plane) {
        super(plane);
    }

    @Override
    public BloodhillBastionPlane copy() {
        return new BloodhillBastionPlane(this);
    }
}
