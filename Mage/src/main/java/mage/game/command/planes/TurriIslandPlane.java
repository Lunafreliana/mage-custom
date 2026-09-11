package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.RevealLibraryPutIntoHandEffect;
import mage.abilities.effects.common.cost.CostModificationEffectImpl;
import mage.cards.Card;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.common.FilterCreatureCard;
import mage.game.Game;
import mage.game.command.Plane;
import mage.util.CardUtil;

/**
 * @author spjspj
 */
public class TurriIslandPlane extends Plane {

    public TurriIslandPlane() {
        this.setPlaneType(Planes.PLANE_TURRI_ISLAND);

        // Creature spells cost {2} less to cast.
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new TurriIslandEffect(2));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, reveal the top three cards of your library. Put all creature cards revealed this way into your hand and the rest into your graveyard.
        Effect chaosEffect = new RevealLibraryPutIntoHandEffect(3, new FilterCreatureCard("creature cards"), Zone.GRAVEYARD);

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private TurriIslandPlane(final TurriIslandPlane plane) {
        super(plane);
    }

    @Override
    public TurriIslandPlane copy() {
        return new TurriIslandPlane(this);
    }
}

class TurriIslandEffect extends CostModificationEffectImpl {

    private static final FilterCard filter = new FilterCard("creature spells");

    static {
        filter.add(CardType.CREATURE.getPredicate());
    }

    private static final String rule = "Creature spells cost {2} less to cast";
    private int amount = 2;

    public TurriIslandEffect(int amount) {
        super(Duration.Custom, Outcome.Benefit, CostModificationType.REDUCE_COST);
        this.amount = 2;
        this.staticText = rule;
    }

    protected TurriIslandEffect(TurriIslandEffect effect) {
        super(effect);
        this.amount = effect.amount;
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
    }

    @Override
    public boolean apply(Game game, Ability source, Ability abilityToModify) {
        CardUtil.reduceCost(abilityToModify, this.amount);
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
            if (!game.getState().hasFaceUpPlane(Planes.PLANE_TURRI_ISLAND)) {
                return false;
            }
            Card spellCard = ((SpellAbility) abilityToModify).getCharacteristics(game);
            if (spellCard != null) {
                return filter.match(spellCard, game) && selectedByRuntimeData(spellCard, source, game);
            }
        }
        return false;
    }

    @Override
    public TurriIslandEffect copy() {
        return new TurriIslandEffect(this);
    }
}
