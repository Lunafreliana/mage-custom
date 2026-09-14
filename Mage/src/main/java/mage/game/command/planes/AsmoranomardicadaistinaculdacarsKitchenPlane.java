package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.EntersBattlefieldControlledTriggeredAbility;
import mage.abilities.common.GainLifeControllerTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.dynamicvalue.common.SavedGainedLifeValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.LoseLifeTargetEffect;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.FoodToken;
import mage.players.Player;
import mage.target.common.TargetOpponent;
import mage.target.common.TargetSacrifice;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class AsmoranomardicadaistinaculdacarsKitchenPlane extends Plane {

    public AsmoranomardicadaistinaculdacarsKitchenPlane() {
        this.setPlaneType(Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN);

        // Whenever you planeswalk here and whenever a creature enters under your control, create a Food token.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(
                new CreateTokenEffect(new FoodToken())
        ));
        this.getAbilities().add(new EntersBattlefieldControlledTriggeredAbility(
                Zone.COMMAND, new CreateTokenEffect(new FoodToken()),
                StaticFilters.FILTER_PERMANENT_CREATURE, false
        ));

        // Whenever you gain life, target opponent loses that much life.
        Ability ability = new GainLifeControllerTriggeredAbility(
                Zone.COMMAND, new LoseLifeTargetEffect(SavedGainedLifeValue.MUCH), false, false
        );
        ability.addTarget(new TargetOpponent());
        this.getAbilities().add(ability);

        // Whenever chaos ensues, you may sacrifice any number of artifacts or creatures.
        // Draw that many cards and lose that much life.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new AsmoranomardicadaistinaculdacarsKitchenChaosEffect(), false
        ));
    }

    private AsmoranomardicadaistinaculdacarsKitchenPlane(
            final AsmoranomardicadaistinaculdacarsKitchenPlane plane
    ) {
        super(plane);
    }

    @Override
    public AsmoranomardicadaistinaculdacarsKitchenPlane copy() {
        return new AsmoranomardicadaistinaculdacarsKitchenPlane(this);
    }
}

class AsmoranomardicadaistinaculdacarsKitchenChaosEffect extends OneShotEffect {

    AsmoranomardicadaistinaculdacarsKitchenChaosEffect() {
        super(Outcome.Benefit);
        staticText = "you may sacrifice any number of artifacts or creatures. "
                + "Draw that many cards and lose that much life";
    }

    private AsmoranomardicadaistinaculdacarsKitchenChaosEffect(
            final AsmoranomardicadaistinaculdacarsKitchenChaosEffect effect
    ) {
        super(effect);
    }

    @Override
    public AsmoranomardicadaistinaculdacarsKitchenChaosEffect copy() {
        return new AsmoranomardicadaistinaculdacarsKitchenChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetSacrifice target = new TargetSacrifice(
                0, Integer.MAX_VALUE, StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE
        );
        controller.choose(Outcome.Sacrifice, target, source, game);
        int sacrificed = 0;
        for (UUID permanentId : target.getTargets()) {
            Permanent permanent = game.getPermanent(permanentId);
            if (permanent != null && permanent.sacrifice(source, game)) {
                sacrificed++;
            }
        }
        controller.drawCards(sacrificed, source, game);
        controller.loseLife(sacrificed, game, source, false);
        return true;
    }
}
