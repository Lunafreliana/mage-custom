package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.UntapAllDuringEachOtherPlayersUntapStepEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInPlayEffect;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetCardInLibrary;

/**
 * @author TheElk801
 */
public final class HorizonBoughsPlane extends Plane {

    public HorizonBoughsPlane() {
        this.setPlaneType(Planes.PLANE_HORIZON_BOUGHS);

        // All permanents untap during each player's untap step.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND,
                new UntapAllDuringEachOtherPlayersUntapStepEffect(
                        StaticFilters.FILTER_PERMANENTS, true)));

        // Whenever chaos ensues, you may search your library for up to three basic land cards,
        // put them onto the battlefield tapped, then shuffle.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new SearchLibraryPutInPlayEffect(new TargetCardInLibrary(
                        0, 3, StaticFilters.FILTER_CARD_BASIC_LANDS), true), true));
    }

    private HorizonBoughsPlane(final HorizonBoughsPlane plane) {
        super(plane);
    }

    @Override
    public HorizonBoughsPlane copy() {
        return new HorizonBoughsPlane(this);
    }
}
