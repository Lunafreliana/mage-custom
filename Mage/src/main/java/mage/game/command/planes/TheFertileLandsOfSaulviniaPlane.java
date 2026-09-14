package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.TapForManaAllTriggeredManaAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.mana.AddManaOfAnyTypeProducedEffect;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.filter.common.FilterLandPermanent;
import mage.game.Game;
import mage.game.command.Plane;

/**
 * @author VibecodingQueens
 */
public final class TheFertileLandsOfSaulviniaPlane extends Plane {

    public TheFertileLandsOfSaulviniaPlane() {
        this.setPlaneType(Planes.PLANE_THE_FERTILE_LANDS_OF_SAULVINIA);

        // Whenever a player taps a land for mana, that player adds one mana of any type that land produced.
        this.getAbilities().add(new TapForManaAllTriggeredManaAbility(
                new AddManaOfAnyTypeProducedEffect(),
                new FilterLandPermanent("a player taps a land"),
                SetTargetPointer.PERMANENT));

        // Whenever chaos ensues, reveal cards from the top of your planar deck until you reveal a plane card.
        // Chaos ensues on that plane. Then put all cards revealed this way on the bottom of your planar deck
        // in any order.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new TheFertileLandsOfSaulviniaEffect(), false));
    }

    private TheFertileLandsOfSaulviniaPlane(final TheFertileLandsOfSaulviniaPlane plane) {
        super(plane);
    }

    @Override
    public TheFertileLandsOfSaulviniaPlane copy() {
        return new TheFertileLandsOfSaulviniaPlane(this);
    }
}

class TheFertileLandsOfSaulviniaEffect extends OneShotEffect {

    TheFertileLandsOfSaulviniaEffect() {
        super(Outcome.Neutral);
        staticText = "reveal cards from the top of your planar deck until you reveal a plane card. "
                + "Chaos ensues on that plane. Then put all cards revealed this way on the bottom "
                + "of your planar deck in any order";
    }

    private TheFertileLandsOfSaulviniaEffect(final TheFertileLandsOfSaulviniaEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return game.chaosEnsuesOnNextPlane(source.getControllerId(), source);
    }

    @Override
    public TheFertileLandsOfSaulviniaEffect copy() {
        return new TheFertileLandsOfSaulviniaEffect(this);
    }
}
