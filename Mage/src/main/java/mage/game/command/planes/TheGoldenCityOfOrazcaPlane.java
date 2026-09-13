package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.OneOrMoreCombatDamagePlayerTriggeredAbility;
import mage.abilities.condition.common.CitysBlessingCondition;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.PutCardFromHandOntoBattlefieldEffect;
import mage.abilities.hint.common.CitysBlessingHint;
import mage.abilities.keyword.AscendAbility;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.game.permanent.token.TreasureToken;

/**
 * @author The XMage Developers
 */
public final class TheGoldenCityOfOrazcaPlane extends Plane {

    public TheGoldenCityOfOrazcaPlane() {
        this.setPlaneType(Planes.PLANE_THE_GOLDEN_CITY_OF_ORAZCA);

        // Ascend (If you control ten or more permanents, you get the city's blessing for the rest of the game.)
        this.getAbilities().add(new AscendAbility(Zone.COMMAND));

        // Whenever one or more creatures you control deal combat damage to a player, create a Treasure token.
        // Then draw a card if you have the city's blessing.
        Ability ability = new OneOrMoreCombatDamagePlayerTriggeredAbility(
                Zone.COMMAND, new CreateTokenEffect(new TreasureToken()),
                StaticFilters.FILTER_PERMANENT_CREATURES, SetTargetPointer.NONE, false
        );
        ability.addEffect(new ConditionalOneShotEffect(
                new DrawCardSourceControllerEffect(1), CitysBlessingCondition.instance,
                "draw a card if you have the city's blessing"
        ).concatBy("Then"));
        this.getAbilities().add(ability.addHint(CitysBlessingHint.instance));

        // Whenever chaos ensues, you may put a permanent card from your hand onto the battlefield tapped.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new PutCardFromHandOntoBattlefieldEffect(
                        StaticFilters.FILTER_CARD_A_PERMANENT, false, true
                ), false
        ));
    }

    private TheGoldenCityOfOrazcaPlane(final TheGoldenCityOfOrazcaPlane plane) {
        super(plane);
    }

    @Override
    public TheGoldenCityOfOrazcaPlane copy() {
        return new TheGoldenCityOfOrazcaPlane(this);
    }
}
