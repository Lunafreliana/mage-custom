package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.abilities.keyword.ShadowAbility;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.common.FilterPermanent;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class TheBeanPlane extends Plane {

    private static final FilterPermanent FILTER = new FilterPermanent(SubType.REFLECTION, "Reflections");

    public TheBeanPlane() {
        this.setPlaneType(Planes.PLANE_THE_BEAN);

        // When you planeswalk here, each player chooses a creature they control and creates a token
        // that's a copy of it, except it's a Reflection in addition to its other types.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new TheBeanCopyEffect(true)));

        // All Reflections have shadow.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new GainAbilityAllEffect(
                ShadowAbility.getInstance(), Duration.WhileOnBattlefield, FILTER
        )));

        // Whenever chaos ensues, create a token that's a copy of a creature you control,
        // except it's a Reflection in addition to its other types.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new TheBeanCopyEffect(false), false));
    }

    private TheBeanPlane(final TheBeanPlane plane) {
        super(plane);
    }

    @Override
    public TheBeanPlane copy() {
        return new TheBeanPlane(this);
    }
}

class TheBeanCopyEffect extends OneShotEffect {

    private final boolean eachPlayer;

    TheBeanCopyEffect(boolean eachPlayer) {
        super(Outcome.Copy);
        this.eachPlayer = eachPlayer;
        staticText = eachPlayer
                ? "each player chooses a creature they control and creates a token that's a copy of it, "
                + "except it's a Reflection in addition to its other types"
                : "create a token that's a copy of a creature you control, except it's a Reflection "
                + "in addition to its other types";
    }

    private TheBeanCopyEffect(final TheBeanCopyEffect effect) {
        super(effect);
        this.eachPlayer = effect.eachPlayer;
    }

    @Override
    public TheBeanCopyEffect copy() {
        return new TheBeanCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (eachPlayer) {
            for (UUID playerId : game.getPlayerList()) {
                copyChosenCreature(game.getPlayer(playerId), game, source);
            }
        } else {
            copyChosenCreature(game.getPlayer(source.getControllerId()), game, source);
        }
        return true;
    }

    private static void copyChosenCreature(Player player, Game game, Ability source) {
        if (player == null) {
            return;
        }
        TargetControlledCreaturePermanent target = new TargetControlledCreaturePermanent();
        target.withNotTarget(true);
        if (!player.choose(Outcome.Copy, target, source, game)) {
            return;
        }
        Effect effect = new CreateTokenCopyTargetEffect(player.getId())
                .withAdditionalSubType(SubType.REFLECTION);
        effect.setTargetPointer(new FixedTarget(target.getFirstTarget(), game));
        effect.apply(game, source);
    }
}
