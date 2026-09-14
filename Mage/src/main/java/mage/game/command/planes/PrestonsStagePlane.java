package mage.game.command.planes;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.CreateTokenTargetEffect;
import mage.abilities.effects.common.continuous.CastAsThoughItHadFlashAllEffect;
import mage.abilities.keyword.FlashAbility;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.FilterSpell;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.token.HatToken;
import mage.game.stack.Spell;
import mage.target.targetpointer.FixedTarget;

/**
 * @author The XMage Developers
 */
public final class PrestonsStagePlane extends Plane {

    public PrestonsStagePlane() {
        this.setPlaneType(Planes.PLANE_PRESTONS_STAGE);

        // Whenever a player performs a magic trick, they create a Hat token.
        this.getAbilities().add(new PrestonsStageMagicTrickTriggeredAbility(
                new CreateTokenTargetEffect(new HatToken()).setText("they create a Hat token")
        ));

        // Whenever chaos ensues, until your next turn, creature cards in your hand have flash.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new CastAsThoughItHadFlashAllEffect(
                        Duration.UntilYourNextTurn, new FilterCreatureCard("creature cards in your hand")
                ).setText("until your next turn, creature cards in your hand have flash"),
                false
        ));
    }

    private PrestonsStagePlane(final PrestonsStagePlane plane) {
        super(plane);
    }

    @Override
    public PrestonsStagePlane copy() {
        return new PrestonsStagePlane(this);
    }
}

class PrestonsStageMagicTrickTriggeredAbility extends TriggeredAbilityImpl {

    private static final FilterSpell MAGIC_TRICK_SPELL = new FilterSpell();

    static {
        MAGIC_TRICK_SPELL.add(Predicates.or(
                CardType.INSTANT.getPredicate(),
                new AbilityPredicate(FlashAbility.class)
        ));
    }

    PrestonsStageMagicTrickTriggeredAbility(Effect effect) {
        super(Zone.COMMAND, effect, false);
        setTriggerPhrase("Whenever a player performs a magic trick, ");
    }

    private PrestonsStageMagicTrickTriggeredAbility(final PrestonsStageMagicTrickTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public PrestonsStageMagicTrickTriggeredAbility copy() {
        return new PrestonsStageMagicTrickTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST
                || event.getType() == GameEvent.EventType.TURNED_FACE_UP;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.getState().getPlayersInRange(getControllerId(), game, false).contains(event.getPlayerId())) {
            return false;
        }
        boolean magicTrick;
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            Spell spell = game.getStack().getSpell(event.getTargetId());
            magicTrick = spell != null && (MAGIC_TRICK_SPELL.match(spell, getControllerId(), this, game)
                    || spell.isCreature(game) && game.getContinuousEffects().asThough(
                            spell.getSourceId(), AsThoughEffectType.CAST_AS_INSTANT,
                            spell.getSpellAbility(), event.getPlayerId(), game
                    ).stream().anyMatch(approver -> approver.getApprovingAbility().getSourceId().equals(getSourceId())));
        } else {
            // A successfully turned-up permanent was a face-down creature immediately before this event.
            magicTrick = game.getPermanent(event.getTargetId()) != null;
        }
        if (!magicTrick) {
            return false;
        }
        for (Effect effect : getEffects()) {
            effect.setTargetPointer(new FixedTarget(event.getPlayerId()));
        }
        return true;
    }

    @Override
    public String getRule() {
        return super.getRule() + " <i>(You perform a magic trick when you cast an instant or spell with flash, "
                + "or turn a face-down creature face-up. Hat tokens are artifact tokens with \"{1}, {T}, "
                + "Sacrifice this artifact: Create a 1/1 white Rabbit creature token.\")</i>";
    }
}
