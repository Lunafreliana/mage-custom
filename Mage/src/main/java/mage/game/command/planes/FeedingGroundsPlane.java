package mage.game.command.planes;

import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.TargetManaValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.cost.CostModificationEffectImpl;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.Card;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.stack.Spell;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.util.CardUtil;

/**
 * @author VibecodingQueens
 */
public class FeedingGroundsPlane extends Plane {

    private static final String rule = "put X +1/+1 counters on target creature, where X is that creature's mana value";

    public FeedingGroundsPlane() {
        this.setPlaneType(Planes.PLANE_FEEDING_GROUNDS);

        // Red spells cost {1} less to cast.  Green spells cost {1} less to cast
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new FeedingGroundsEffect());
        this.getAbilities().add(ability);

        // Whenever chaos ensues, target red or green creature gets X +1/+1 counters
        Effect chaosEffect = new AddCountersTargetEffect(CounterType.P1P1.createInstance(), TargetManaValue.instance);
        Target chaosTarget = new TargetPermanent(StaticFilters.FILTER_PERMANENT_A_CREATURE);

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        chaosAbility.addTarget(chaosTarget);
        this.getAbilities().add(chaosAbility);
    }

    private FeedingGroundsPlane(final FeedingGroundsPlane plane) {
        super(plane);
    }

    @Override
    public FeedingGroundsPlane copy() {
        return new FeedingGroundsPlane(this);
    }
}

class FeedingGroundsEffect extends CostModificationEffectImpl {

    private static final FilterCard filter = new FilterCard("Red spells or Green spells");

    static {
        filter.add(Predicates.or(
                new ColorPredicate(ObjectColor.RED),
                new ColorPredicate(ObjectColor.GREEN)));
    }

    private static final String rule = "Red spells cost {1} less to cast. Green spells cost {1} less to cast";
    private int amount = 1;

    public FeedingGroundsEffect() {
        super(Duration.Custom, Outcome.Benefit, CostModificationType.REDUCE_COST);
        this.amount = 1;
        this.staticText = rule;
    }

    protected FeedingGroundsEffect(FeedingGroundsEffect effect) {
        super(effect);
        this.amount = effect.amount;
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
    }

    @Override
    public boolean apply(Game game, Ability source, Ability abilityToModify) {
        MageObject object = abilityToModify.getSourceObject(game);
        int reduce = 0;
        if (object != null) {
            if (object.getColor(game).isRed()) {
                reduce++;
            }
            if (object.getColor(game).isGreen()) {
                reduce++;
            }
        }
        CardUtil.reduceCost(abilityToModify, reduce);
        return true;
    }

    /**
     * Overwrite this in effect that inherits from this
     *
     * @param card
     * @param source
     * @param game
     * @return
     */
    protected boolean selectedByRuntimeData(Card card, Ability source, Game game) {
        return true;
    }

    @Override
    public boolean applies(Ability abilityToModify, Ability source, Game game) {
        if (abilityToModify instanceof SpellAbility) {
            if (!game.getState().hasFaceUpPlane(Planes.PLANE_FEEDING_GROUNDS)) {
                return false;
            }

            Spell spell = (Spell) game.getStack().getStackObject(abilityToModify.getId());
            if (spell != null) {
                return filter.match(spell, game) && selectedByRuntimeData(spell, source, game);
            } else {
                // used at least for flashback ability because Flashback ability doesn't use stack
                Card sourceCard = game.getCard(abilityToModify.getSourceId());
                return filter.match(sourceCard, game) && selectedByRuntimeData(sourceCard, source, game);
            }
        }
        return false;
    }

    @Override
    public FeedingGroundsEffect copy() {
        return new FeedingGroundsEffect(this);
    }
}
