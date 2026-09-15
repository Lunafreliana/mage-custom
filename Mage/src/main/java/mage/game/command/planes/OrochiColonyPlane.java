package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.DealsDamageToAPlayerAllTriggeredAbility;
import mage.abilities.effects.common.combat.CantBeBlockedTargetEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInPlayEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetCardInLibrary;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class OrochiColonyPlane extends Plane {

    public OrochiColonyPlane() {
        this.setPlaneType(Planes.PLANE_OROCHI_COLONY);

        // Whenever a creature you control deals combat damage to a player, you may search your library for a
        // basic land card, put it onto the battlefield tapped, then shuffle.
        this.getAbilities().add(new DealsDamageToAPlayerAllTriggeredAbility(
                Zone.COMMAND,
                new SearchLibraryPutInPlayEffect(
                        new TargetCardInLibrary(StaticFilters.FILTER_CARD_BASIC_LAND), true
                ), StaticFilters.FILTER_CONTROLLED_A_CREATURE, true,
                SetTargetPointer.NONE, true, false
        ));

        // Whenever chaos ensues, target creature can't be blocked this turn.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new CantBeBlockedTargetEffect(Duration.EndOfTurn), false
        );
        ability.addTarget(new TargetCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private OrochiColonyPlane(final OrochiColonyPlane plane) {
        super(plane);
    }

    @Override
    public OrochiColonyPlane copy() {
        return new OrochiColonyPlane(this);
    }
}
