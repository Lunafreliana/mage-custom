package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.OneOrMoreCombatDamagePlayerTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.FlipCoinEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.game.permanent.token.TreasureToken;
import mage.target.common.TargetAnyTarget;

/**
 * @author The XMage Developers
 */
public final class KerblamWarehousePlane extends Plane {

    public KerblamWarehousePlane() {
        this.setPlaneType(Planes.PLANE_KERBLAM_WAREHOUSE);

        // Whenever one or more creatures you control deal combat damage to a player, create a Treasure token.
        this.getAbilities().add(new OneOrMoreCombatDamagePlayerTriggeredAbility(
                Zone.COMMAND, new CreateTokenEffect(new TreasureToken()),
                StaticFilters.FILTER_PERMANENT_CREATURES, SetTargetPointer.NONE, false
        ));

        // Whenever chaos ensues, until your next turn, noncreature artifacts you control gain
        // "{T}, Sacrifice this artifact: Flip a coin. If you win the flip, this artifact deals 3 damage to any target."
        Ability grantedAbility = new SimpleActivatedAbility(
                new FlipCoinEffect(new DamageTargetEffect(3, "this artifact")), new TapSourceCost()
        );
        grantedAbility.addCost(new SacrificeSourceCost());
        grantedAbility.addTarget(new TargetAnyTarget());
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new GainAbilityControlledEffect(
                        grantedAbility, Duration.UntilYourNextTurn, StaticFilters.FILTER_ARTIFACT_NON_CREATURE
                ).setText("until your next turn, noncreature artifacts you control gain "
                        + "\"{T}, Sacrifice this artifact: Flip a coin. If you win the flip, "
                        + "this artifact deals 3 damage to any target.\""),
                false
        ));
    }

    private KerblamWarehousePlane(final KerblamWarehousePlane plane) {
        super(plane);
    }

    @Override
    public KerblamWarehousePlane copy() {
        return new KerblamWarehousePlane(this);
    }
}
