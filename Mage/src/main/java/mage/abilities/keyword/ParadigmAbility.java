package mage.abilities.keyword;

import mage.ApprovingObject;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.stack.Spell;
import mage.players.Player;

/**
 * Paradigm creates a lasting delayed trigger the first time its controller
 * resolves a spell with that name.  The trigger copies the spell card in
 * exile and offers to cast that copy, rather than merely putting a spell copy
 * on the stack (casting is important for cast triggers such as magecraft).
 *
 * @author VibecodingQueens
 */
public class ParadigmAbility extends SimpleStaticAbility {

    private static final String RULE = "Paradigm <i>(Then exile this spell. After you first resolve a spell with this name, "
            + "you may cast a copy of it from exile without paying its mana cost "
            + "at the beginning of each of your first main phases.)</i>";

    public ParadigmAbility() {
        super(Zone.STACK, new ParadigmReplacementEffect());
    }

    protected ParadigmAbility(final ParadigmAbility ability) {
        super(ability);
    }

    @Override
    public ParadigmAbility copy() {
        return new ParadigmAbility(this);
    }

    @Override
    public String getRule() {
        return RULE;
    }
}

class ParadigmReplacementEffect extends ReplacementEffectImpl {

    ParadigmReplacementEffect() {
        super(Duration.WhileOnStack, Outcome.Benefit);
    }

    private ParadigmReplacementEffect(final ParadigmReplacementEffect effect) {
        super(effect);
    }

    @Override
    public ParadigmReplacementEffect copy() {
        return new ParadigmReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return ((ZoneChangeEvent) event).getFromZone() == Zone.STACK
                && ((ZoneChangeEvent) event).getToZone() == Zone.GRAVEYARD
                // A countered spell and a spell whose targets are all illegal did not resolve.
                && source.getSourceId().equals(event.getSourceId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Spell spell = game.getStack().getSpell(source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        if (spell == null || controller == null) {
            return false;
        }

        String resolvedKey = "paradigmResolved_" + controller.getId() + '_' + spell.getName();
        if (game.getState().getValue(resolvedKey) == null) {
            game.getState().setValue(resolvedKey, Boolean.TRUE);
            game.addDelayedTriggeredAbility(new ParadigmDelayedTriggeredAbility(spell.getCard()), source);
        }

        // Spell copies are not cards and will cease to exist; the physical card is exiled.
        Card card = game.getCard(source.getSourceId());
        if (card != null && !spell.isCopy()) {
            controller.moveCardsToExile(card, source, game, true, null, "Paradigm");
        }
        return true;
    }
}

class ParadigmDelayedTriggeredAbility extends DelayedTriggeredAbility {

    ParadigmDelayedTriggeredAbility(Card card) {
        super(new ParadigmCopyAndCastEffect(card), Duration.EndOfGame, false, false);
        setTriggerPhrase("At the beginning of your first main phase, ");
    }

    private ParadigmDelayedTriggeredAbility(final ParadigmDelayedTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ParadigmDelayedTriggeredAbility copy() {
        return new ParadigmDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PRECOMBAT_MAIN_PHASE_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return event.getPlayerId().equals(getControllerId());
    }
}

class ParadigmCopyAndCastEffect extends OneShotEffect {

    private final Card card;

    ParadigmCopyAndCastEffect(Card card) {
        super(Outcome.PlayForFree);
        this.card = card.copy();
        staticText = "you may cast a copy of the paradigm spell from exile without paying its mana cost";
    }

    private ParadigmCopyAndCastEffect(final ParadigmCopyAndCastEffect effect) {
        super(effect);
        this.card = effect.card.copy();
    }

    @Override
    public ParadigmCopyAndCastEffect copy() {
        return new ParadigmCopyAndCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Card copy = game.copyCard(card, source, controller.getId());
        if (!controller.chooseUse(outcome, "Cast copy of " + card.getName()
                + " without paying its mana cost?", source, game)) {
            return true;
        }
        game.getState().setValue("PlayFromNotOwnHandZone" + copy.getId(), Boolean.TRUE);
        controller.cast(
                controller.chooseAbilityForCast(copy, game, true),
                game, true, new ApprovingObject(source, game)
        );
        game.getState().setValue("PlayFromNotOwnHandZone" + copy.getId(), null);
        return true;
    }
}
