package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.PutCardFromHandOntoBattlefieldEffect;
import mage.abilities.effects.common.RevealCardsFromLibraryUntilEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.PutCards;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;

/**
 * @author The XMage Developers
 */
public final class TheAetherFluesPlane extends Plane {

    public TheAetherFluesPlane() {
        this.setPlaneType(Planes.PLANE_THE_AETHER_FLUES);

        // When you planeswalk to The Aether Flues and at the beginning of your upkeep, you may sacrifice a creature.
        // If you do, reveal cards from the top of your library until you reveal a creature card, put that card onto the
        // battlefield, then shuffle all other cards revealed this way into your library.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(makeCreatureExchangeEffect()));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, makeCreatureExchangeEffect(), false
        ));

        // Whenever chaos ensues, you may put a creature card from your hand onto the battlefield.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new PutCardFromHandOntoBattlefieldEffect(StaticFilters.FILTER_CARD_CREATURE), false
        );
        this.getAbilities().add(ability);
    }

    private TheAetherFluesPlane(final TheAetherFluesPlane plane) {
        super(plane);
    }

    @Override
    public TheAetherFluesPlane copy() {
        return new TheAetherFluesPlane(this);
    }

    private static DoIfCostPaid makeCreatureExchangeEffect() {
        return new DoIfCostPaid(
                new RevealCardsFromLibraryUntilEffect(
                        StaticFilters.FILTER_CARD_CREATURE, PutCards.BATTLEFIELD, PutCards.SHUFFLE
                ),
                new SacrificeTargetCost(StaticFilters.FILTER_PERMANENT_CREATURE)
        );
    }
}
