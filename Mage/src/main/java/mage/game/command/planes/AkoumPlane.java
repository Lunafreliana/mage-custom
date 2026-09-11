package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.effects.common.continuous.CastAsThoughItHadFlashAllEffect;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.permanent.EnchantedPredicate;
import mage.game.command.Plane;
import mage.target.Target;
import mage.target.TargetPermanent;

/**
 * @author spjspj
 */
public class AkoumPlane extends Plane {

    private static final FilterCard filterCard = new FilterCard("enchantment spells");
    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creature that isn't enchanted");

    static {
        filter.add(Predicates.not(EnchantedPredicate.instance));
        filterCard.add(CardType.ENCHANTMENT.getPredicate());
    }

    public AkoumPlane() {
        this.setPlaneType(Planes.PLANE_AKOUM);

        // Players may cast enchantment spells as if they had flash
        SimpleStaticAbility ability = new SimpleStaticAbility(Zone.COMMAND, new CastAsThoughItHadFlashAllEffect(Duration.Custom, filterCard, true));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, destroy target creature that isn't enchanted
        Effect chaosEffect = new DestroyTargetEffect("destroy target creature that isn't enchanted");
        Target chaosTarget = new TargetPermanent(filter);

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        chaosAbility.addTarget(chaosTarget);
        this.getAbilities().add(chaosAbility);
    }

    private AkoumPlane(final AkoumPlane plane) {
        super(plane);
    }

    @Override
    public AkoumPlane copy() {
        return new AkoumPlane(this);
    }
}
