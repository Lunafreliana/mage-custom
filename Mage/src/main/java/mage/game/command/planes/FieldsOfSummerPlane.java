package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SpellCastAllTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.GainLifeTargetEffect;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;

/**
 * @author spjspj
 */
public class FieldsOfSummerPlane extends Plane {


    public FieldsOfSummerPlane() {
        this.setPlaneType(Planes.PLANE_FIELDS_OF_SUMMER);

        // Whenever a player casts a spell, that player may gain 2 life
        SpellCastAllTriggeredAbility ability = new SpellCastAllTriggeredAbility(Zone.COMMAND, new FieldsOfSummerEffect(), StaticFilters.FILTER_SPELL_A, false, SetTargetPointer.PLAYER);
        this.getAbilities().add(ability);

        // Whenever chaos ensues, you may gain 10 life.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new GainLifeEffect(10), true));
    }

    private FieldsOfSummerPlane(final FieldsOfSummerPlane plane) {
        super(plane);
    }

    @Override
    public FieldsOfSummerPlane copy() {
        return new FieldsOfSummerPlane(this);
    }
}

class FieldsOfSummerEffect extends OneShotEffect {

    public FieldsOfSummerEffect() {
        super(Outcome.GainLife);
        this.staticText = "that player may gain 2 life";
    }

    protected FieldsOfSummerEffect(final FieldsOfSummerEffect effect) {
        super(effect);
    }

    @Override
    public FieldsOfSummerEffect copy() {
        return new FieldsOfSummerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_FIELDS_OF_SUMMER)) {
            return false;
        }
        Player owner = game.getPlayer(this.getTargetPointer().getFirst(game, source));
        if (owner != null && owner.canRespond() && owner.chooseUse(Outcome.Benefit, "Gain 2 life?", source, game)) {
            Effect effect = new GainLifeTargetEffect(2);
            effect.setTargetPointer(new FixedTarget(owner.getId())).apply(game, source);
            return true;
        }
        return false;
    }
}
