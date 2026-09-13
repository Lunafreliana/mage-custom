package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.common.ReturnToBattlefieldUnderOwnerControlTargetEffect;
import mage.abilities.effects.common.RevealCardsFromLibraryUntilEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.PutCards;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetCardInYourGraveyard;

/**
 * @author The XMage Developers
 */
public final class GroveOfTheDreampodsPlane extends Plane {

    public GroveOfTheDreampodsPlane() {
        this.setPlaneType(Planes.PLANE_GROVE_OF_THE_DREAMPODS);

        // When you planeswalk to Grove of the Dreampods and at the beginning of your upkeep,
        // reveal cards from the top of your library until you reveal a creature card.
        // Put that card onto the battlefield and the rest on the bottom of your library in a random order.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(createRevealEffect()));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, createRevealEffect(), false
        ));

        // Whenever chaos ensues, return target creature card from your graveyard to the battlefield.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new ReturnToBattlefieldUnderOwnerControlTargetEffect(false, false), false
        );
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_CREATURE));
        this.getAbilities().add(ability);
    }

    private GroveOfTheDreampodsPlane(final GroveOfTheDreampodsPlane plane) {
        super(plane);
    }

    @Override
    public GroveOfTheDreampodsPlane copy() {
        return new GroveOfTheDreampodsPlane(this);
    }

    private static RevealCardsFromLibraryUntilEffect createRevealEffect() {
        return new RevealCardsFromLibraryUntilEffect(
                StaticFilters.FILTER_CARD_CREATURE, PutCards.BATTLEFIELD, PutCards.BOTTOM_RANDOM
        );
    }
}
