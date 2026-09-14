package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SpellCastAllTriggeredAbility;
import mage.abilities.effects.common.AddContinuousEffectToGame;
import mage.abilities.effects.common.CopyTargetStackObjectEffect;
import mage.abilities.effects.common.cost.CostModificationEffectImpl;
import mage.constants.CostModificationType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.common.FilterInstantOrSorcerySpell;
import mage.game.Game;
import mage.game.command.Plane;
import mage.util.CardUtil;

/**
 * @author The XMage Developers
 */
public final class IzzetSteamMazePlane extends Plane {

    public IzzetSteamMazePlane() {
        this.setPlaneType(Planes.PLANE_IZZET_STEAM_MAZE);

        // Whenever a player casts an instant or sorcery spell, that player copies it.
        // The player may choose new targets for the copy.
        this.getAbilities().add(new SpellCastAllTriggeredAbility(
                Zone.COMMAND, new CopyTargetStackObjectEffect(true, true, true),
                new FilterInstantOrSorcerySpell(), false, SetTargetPointer.SPELL
        ));

        // Whenever chaos ensues, instant and sorcery spells you cast this turn cost {3} less to cast.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new AddContinuousEffectToGame(new IzzetSteamMazeCostReductionEffect()), false
        ));
    }

    private IzzetSteamMazePlane(final IzzetSteamMazePlane plane) {
        super(plane);
    }

    @Override
    public IzzetSteamMazePlane copy() {
        return new IzzetSteamMazePlane(this);
    }
}

class IzzetSteamMazeCostReductionEffect extends CostModificationEffectImpl {

    IzzetSteamMazeCostReductionEffect() {
        super(Duration.EndOfTurn, Outcome.Benefit, CostModificationType.REDUCE_COST);
        staticText = "instant and sorcery spells you cast this turn cost {3} less to cast";
    }

    private IzzetSteamMazeCostReductionEffect(final IzzetSteamMazeCostReductionEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source, Ability abilityToModify) {
        CardUtil.reduceCost(abilityToModify, 3);
        return true;
    }

    @Override
    public boolean applies(Ability abilityToModify, Ability source, Game game) {
        return abilityToModify instanceof SpellAbility
                && abilityToModify.isControlledBy(source.getControllerId())
                && ((SpellAbility) abilityToModify).getCharacteristics(game).isInstantOrSorcery(game);
    }

    @Override
    public IzzetSteamMazeCostReductionEffect copy() {
        return new IzzetSteamMazeCostReductionEffect(this);
    }
}
