package mage.game.command.phenomena;

import mage.abilities.Ability;
import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.constants.CounterType;
import mage.constants.Outcome;
import mage.constants.Phenomena;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Phenomenon;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Codex
 */
public final class HumanTimeLordMetaCrisisPhenomenon extends Phenomenon {

    public HumanTimeLordMetaCrisisPhenomenon() {
        super(Phenomena.HUMAN_TIME_LORD_META_CRISIS.getFullName());

        // When you encounter Human—Time Lord Meta-Crisis, each player chooses one or two creatures
        // they control. Each player creates a token that's a copy of the first creature they chose,
        // except it isn't legendary. Then each player who chose a second creature puts a number of
        // +1/+1 counters on the token they created equal to the power of the second creature they chose.
        this.getAbilities().add(new EncounterPhenomenonTriggeredAbility(
                new HumanTimeLordMetaCrisisEffect()
        ));
    }

    private HumanTimeLordMetaCrisisPhenomenon(final HumanTimeLordMetaCrisisPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public HumanTimeLordMetaCrisisPhenomenon copy() {
        return new HumanTimeLordMetaCrisisPhenomenon(this);
    }
}

class HumanTimeLordMetaCrisisEffect extends OneShotEffect {

    HumanTimeLordMetaCrisisEffect() {
        super(Outcome.Copy);
        staticText = "each player chooses one or two creatures they control. Each player creates a token "
                + "that's a copy of the first creature they chose, except it isn't legendary. Then each "
                + "player who chose a second creature puts a number of +1/+1 counters on the token they "
                + "created equal to the power of the second creature they chose";
    }

    private HumanTimeLordMetaCrisisEffect(final HumanTimeLordMetaCrisisEffect effect) {
        super(effect);
    }

    @Override
    public HumanTimeLordMetaCrisisEffect copy() {
        return new HumanTimeLordMetaCrisisEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<MetaCrisisChoice> choices = new ArrayList<>();
        for (UUID playerId : game.getPlayerList()) {
            Player player = game.getPlayer(playerId);
            if (player == null || game.getBattlefield().countAll(
                    StaticFilters.FILTER_CONTROLLED_CREATURE, playerId, game) == 0) {
                continue;
            }
            TargetControlledCreaturePermanent target = new TargetControlledCreaturePermanent(1, 2);
            target.withNotTarget(true);
            if (!player.choose(Outcome.Copy, target, source, game) || target.getTargets().isEmpty()) {
                continue;
            }
            Permanent first = game.getPermanent(target.getTargets().get(0));
            Permanent second = target.getTargets().size() > 1
                    ? game.getPermanent(target.getTargets().get(1))
                    : null;
            if (first != null) {
                choices.add(new MetaCrisisChoice(playerId, first, second));
            }
        }

        // All players choose before any of the tokens are created, and all tokens are created before
        // the instruction beginning with "Then" puts counters on them.
        for (MetaCrisisChoice choice : choices) {
            CreateTokenCopyTargetEffect effect = new CreateTokenCopyTargetEffect(choice.playerId)
                    .setIsntLegendary(true)
                    .setSavedPermanent(choice.firstCreature);
            effect.apply(game, source);
            choice.tokens.addAll(effect.getAddedTokenPermanents());
        }
        for (MetaCrisisChoice choice : choices) {
            if (choice.secondCreature == null || choice.tokens.isEmpty()) {
                continue;
            }
            int power = choice.secondCreature.getPower().getValue();
            if (power > 0) {
                choice.tokens.forEach(token -> token.addCounters(
                        CounterType.P1P1.createInstance(power), choice.playerId, source, game
                ));
            }
        }
        return true;
    }

    private static final class MetaCrisisChoice {
        private final UUID playerId;
        private final Permanent firstCreature;
        private final Permanent secondCreature;
        private final List<Permanent> tokens = new ArrayList<>();

        private MetaCrisisChoice(UUID playerId, Permanent firstCreature, Permanent secondCreature) {
            this.playerId = playerId;
            this.firstCreature = firstCreature;
            this.secondCreature = secondCreature;
        }
    }
}
