package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.PreventAllDamageByAllPermanentsEffect;
import mage.abilities.effects.common.continuous.BecomesSubtypeAllEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.game.command.Plane;

import java.util.Collections;

/**
 * @author The XMage Developers
 */
public final class KessigPlane extends Plane {

    private static final FilterCreaturePermanent nonWerewolfFilter
            = new FilterCreaturePermanent("non-Werewolf creatures");
    private static final FilterCreaturePermanent controlledCreatureFilter
            = new FilterCreaturePermanent("creatures you control");

    static {
        nonWerewolfFilter.add(Predicates.not(SubType.WEREWOLF.getPredicate()));
        controlledCreatureFilter.add(TargetController.YOU.getControllerPredicate());
    }

    public KessigPlane() {
        this.setPlaneType(Planes.PLANE_KESSIG);

        // Prevent all combat damage that would be dealt by non-Werewolf creatures.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND,
                new PreventAllDamageByAllPermanentsEffect(
                        nonWerewolfFilter, Duration.WhileOnBattlefield, true
                )));

        // Whenever chaos ensues, each creature you control gets +2/+2, gains trample,
        // and becomes a Werewolf in addition to its other types until end of turn.
        Effect effect = new BoostControlledEffect(2, 2, Duration.EndOfTurn);
        effect.setText("each creature you control gets +2/+2");
        Ability ability = new ChaosEnsuesTriggeredAbility(effect, false);
        ability.addEffect(new GainAbilityControlledEffect(
                TrampleAbility.getInstance(), Duration.EndOfTurn,
                StaticFilters.FILTER_PERMANENT_CREATURES
        ).setText("gains trample"));
        ability.addEffect(new BecomesSubtypeAllEffect(
                Duration.EndOfTurn, Collections.singletonList(SubType.WEREWOLF),
                controlledCreatureFilter, false
        ).setText("and becomes a Werewolf in addition to its other types until end of turn"));
        this.getAbilities().add(ability);
    }

    private KessigPlane(final KessigPlane plane) {
        super(plane);
    }

    @Override
    public KessigPlane copy() {
        return new KessigPlane(this);
    }
}
