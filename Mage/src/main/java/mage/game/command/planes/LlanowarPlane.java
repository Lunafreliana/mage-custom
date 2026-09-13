package mage.game.command.planes;

import mage.Mana;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.UntapAllControllerEffect;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.abilities.mana.SimpleManaAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;

/**
 * @author Codex
 */
public final class LlanowarPlane extends Plane {

    public LlanowarPlane() {
        this.setPlaneType(Planes.PLANE_LLANOWAR);

        // All creatures have "{T}: Add {G}{G}."
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new GainAbilityAllEffect(
                new SimpleManaAbility(Zone.BATTLEFIELD, Mana.GreenMana(2), new TapSourceCost()),
                Duration.WhileOnBattlefield, StaticFilters.FILTER_PERMANENT_ALL_CREATURES
        )));

        // Whenever chaos ensues, untap all creatures you control.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new UntapAllControllerEffect(
                StaticFilters.FILTER_CONTROLLED_CREATURES
        ), false));
    }

    private LlanowarPlane(final LlanowarPlane plane) {
        super(plane);
    }

    @Override
    public LlanowarPlane copy() {
        return new LlanowarPlane(this);
    }
}
