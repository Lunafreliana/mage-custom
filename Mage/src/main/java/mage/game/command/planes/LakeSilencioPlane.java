package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.IsStillOnPlaneCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.ExileTargetIfDiesEffect;
import mage.abilities.keyword.SplitSecondAbility;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.target.TargetPermanent;

/**
 * @author The XMage Developers
 */
public final class LakeSilencioPlane extends Plane {

    public LakeSilencioPlane() {
        this.setPlaneType(Planes.PLANE_LAKE_SILENCIO);

        // Still Point in Time — All spells have split second.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new ConditionalContinuousEffect(
                new LakeSilencioAllSpellsHaveSplitSecondEffect(),
                new IsStillOnPlaneCondition(this.getName()),
                "All spells have split second."
        )).withFlavorWord("Still Point in Time"));

        // Whenever chaos ensues, Lake Silencio deals 6 damage to target creature an opponent controls.
        // If a creature dealt damage this way would die this turn, exile it instead.
        Ability ability = new ChaosEnsuesTriggeredAbility(new DamageTargetEffect(6), false);
        ability.addEffect(new ExileTargetIfDiesEffect()
                .setText("If a creature dealt damage this way would die this turn, exile it instead"));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_OPPONENTS_PERMANENT_CREATURE));
        this.getAbilities().add(ability);
    }

    private LakeSilencioPlane(final LakeSilencioPlane plane) {
        super(plane);
    }

    @Override
    public LakeSilencioPlane copy() {
        return new LakeSilencioPlane(this);
    }
}

class LakeSilencioAllSpellsHaveSplitSecondEffect extends ContinuousEffectImpl {

    private final Ability splitSecondAbility;

    LakeSilencioAllSpellsHaveSplitSecondEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.splitSecondAbility = new SplitSecondAbility();
        this.staticText = "all spells have split second";
    }

    private LakeSilencioAllSpellsHaveSplitSecondEffect(
            final LakeSilencioAllSpellsHaveSplitSecondEffect effect
    ) {
        super(effect);
        this.splitSecondAbility = effect.splitSecondAbility.copy();
    }

    @Override
    public LakeSilencioAllSpellsHaveSplitSecondEffect copy() {
        return new LakeSilencioAllSpellsHaveSplitSecondEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (StackObject stackObject : game.getStack()) {
            if (stackObject instanceof Spell) {
                Card card = ((Spell) stackObject).getCard();
                if (card != null) {
                    game.getState().addOtherAbility(card, splitSecondAbility);
                }
            }
        }
        return true;
    }
}
