package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.AddCombatAndMainPhaseEffect;
import mage.abilities.effects.common.UntapAllEffect;
import mage.abilities.effects.common.continuous.BoostAllEffect;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.abilities.keyword.HasteAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.AttackedThisTurnPredicate;
import mage.game.command.Plane;

/**
 * @author The XMage Developers
 */
public final class SokenzanPlane extends Plane {

    private static final FilterCreaturePermanent ATTACKED_FILTER
            = new FilterCreaturePermanent("creatures that attacked this turn");

    static {
        ATTACKED_FILTER.add(AttackedThisTurnPredicate.instance);
    }

    public SokenzanPlane() {
        this.setPlaneType(Planes.PLANE_SOKENZAN);

        // All creatures get +1/+1 and have haste.
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new BoostAllEffect(
                1, 1, Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENT_ALL_CREATURES, false
        ));
        ability.addEffect(new GainAbilityAllEffect(
                HasteAbility.getInstance(), Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENT_ALL_CREATURES
        ).setText("and have haste"));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, untap all creatures that attacked this turn. If it's a main phase,
        // there is an additional combat phase after this phase, followed by an additional main phase.
        ability = new ChaosEnsuesTriggeredAbility(new UntapAllEffect(ATTACKED_FILTER), false);
        ability.addEffect(new AddCombatAndMainPhaseEffect().setText(
                "If it's a main phase, there is an additional combat phase after this phase, "
                        + "followed by an additional main phase"
        ));
        this.getAbilities().add(ability);
    }

    private SokenzanPlane(final SokenzanPlane plane) {
        super(plane);
    }

    @Override
    public SokenzanPlane copy() {
        return new SokenzanPlane(this);
    }
}
