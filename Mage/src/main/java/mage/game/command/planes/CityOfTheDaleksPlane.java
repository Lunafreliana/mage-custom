package mage.game.command.planes;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.AttacksWithCreaturesTriggeredAbility;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.RequirementEffect;
import mage.abilities.effects.common.LoseLifeTargetEffect;
import mage.abilities.effects.common.SacrificeTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.DalekToken;
import mage.players.Player;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;
import mage.target.targetpointer.FixedTargets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class CityOfTheDaleksPlane extends Plane {

    public CityOfTheDaleksPlane() {
        this.setPlaneType(Planes.PLANE_CITY_OF_THE_DALEKS);

        // Whenever you attack, target opponent loses X life, where X is the number of artifacts you control.
        Ability attackAbility = new AttacksWithCreaturesTriggeredAbility(
                Zone.COMMAND,
                new LoseLifeTargetEffect(new PermanentsOnBattlefieldCount(
                        StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACT
                )),
                1,
                StaticFilters.FILTER_PERMANENT_CREATURES
        );
        attackAbility.addTarget(new TargetOpponent());
        this.getAbilities().add(attackAbility);

        // Whenever chaos ensues, for each opponent, you create a 3/3 black Dalek artifact creature
        // token with menace that attacks that opponent this turn if able. Those tokens gain haste.
        // Sacrifice them at the beginning of the next end step.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new CityOfTheDaleksChaosEffect(), false));
    }

    private CityOfTheDaleksPlane(final CityOfTheDaleksPlane plane) {
        super(plane);
    }

    @Override
    public CityOfTheDaleksPlane copy() {
        return new CityOfTheDaleksPlane(this);
    }
}

class CityOfTheDaleksChaosEffect extends OneShotEffect {

    CityOfTheDaleksChaosEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "for each opponent, you create a 3/3 black Dalek artifact creature token with menace " +
                "that attacks that opponent this turn if able. Those tokens gain haste. " +
                "Sacrifice them at the beginning of the next end step";
    }

    private CityOfTheDaleksChaosEffect(final CityOfTheDaleksChaosEffect effect) {
        super(effect);
    }

    @Override
    public CityOfTheDaleksChaosEffect copy() {
        return new CityOfTheDaleksChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<MageObjectReference> tokens = new ArrayList<>();
        for (UUID opponentId : game.getOpponents(source.getControllerId())) {
            DalekToken token = new DalekToken();
            if (!token.putOntoBattlefield(1, game, source)) {
                continue;
            }
            for (UUID tokenId : token.getLastAddedTokenIds()) {
                MageObjectReference tokenReference = new MageObjectReference(tokenId, game);
                tokens.add(tokenReference);
                game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.Custom)
                        .setTargetPointer(new FixedTarget(tokenReference)), source);
                game.addEffect(new CityOfTheDaleksAttackRequirementEffect(tokenReference, opponentId), source);
            }
        }
        if (!tokens.isEmpty()) {
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                    new SacrificeTargetEffect("sacrifice them").setTargetPointer(new FixedTargets(tokens))
            ), source);
        }
        return !tokens.isEmpty();
    }
}

class CityOfTheDaleksAttackRequirementEffect extends RequirementEffect {

    private final MageObjectReference token;
    private final UUID opponentId;

    CityOfTheDaleksAttackRequirementEffect(MageObjectReference token, UUID opponentId) {
        super(Duration.EndOfTurn);
        this.token = token;
        this.opponentId = opponentId;
    }

    private CityOfTheDaleksAttackRequirementEffect(final CityOfTheDaleksAttackRequirementEffect effect) {
        super(effect);
        this.token = effect.token;
        this.opponentId = effect.opponentId;
    }

    @Override
    public CityOfTheDaleksAttackRequirementEffect copy() {
        return new CityOfTheDaleksAttackRequirementEffect(this);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        return token.refersTo(permanent, game);
    }

    @Override
    public boolean mustAttack(Game game) {
        Player opponent = game.getPlayer(opponentId);
        return opponent != null && opponent.isInGame();
    }

    @Override
    public boolean mustBlock(Game game) {
        return false;
    }

    @Override
    public UUID mustAttackDefender(Ability source, Game game) {
        return mustAttack(game) ? opponentId : null;
    }
}
