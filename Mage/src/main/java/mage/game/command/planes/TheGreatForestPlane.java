package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;

/**
 * @author VibecodingQueens
 */
public class TheGreatForestPlane extends Plane {

    public TheGreatForestPlane() {
        this.setPlaneType(Planes.PLANE_THE_GREAT_FOREST);

        // Each creature assigns combat damage equal to its toughness rather than its power
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new TheGreatForestCombatDamageRuleEffect());
        this.getAbilities().add(ability);

        // Whenever chaos ensues, creatures you control get +0/+2 and gain trample until end of turn
        Effect chaosEffect = new BoostControlledEffect(0, 2, Duration.EndOfTurn);
        Effect chaosEffect2 = new GainAbilityControlledEffect(TrampleAbility.getInstance(), Duration.EndOfTurn, StaticFilters.FILTER_PERMANENT_CREATURES);
        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        chaosAbility.addEffect(chaosEffect2);
        this.getAbilities().add(chaosAbility);
    }

    private TheGreatForestPlane(final TheGreatForestPlane plane) {
        super(plane);
    }

    @Override
    public TheGreatForestPlane copy() {
        return new TheGreatForestPlane(this);
    }
}

class TheGreatForestCombatDamageRuleEffect extends ContinuousEffectImpl {

    public TheGreatForestCombatDamageRuleEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        staticText = "Each creature assigns combat damage equal to its toughness rather than its power";
    }

    protected TheGreatForestCombatDamageRuleEffect(final TheGreatForestCombatDamageRuleEffect effect) {
        super(effect);
    }

    @Override
    public TheGreatForestCombatDamageRuleEffect copy() {
        return new TheGreatForestCombatDamageRuleEffect(this);
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_THE_GREAT_FOREST)) {
            return false;
        }

        // Change the rule
        game.getCombat().setUseToughnessForDamage(true);
        game.getCombat().addUseToughnessForDamageFilter(StaticFilters.FILTER_PERMANENT_CREATURES);
        return true;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.RulesEffects;
    }
}
