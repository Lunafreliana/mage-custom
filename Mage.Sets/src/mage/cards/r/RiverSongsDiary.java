package mage.cards.r;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.condition.Condition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class RiverSongsDiary extends CardImpl {

    static UUID getImprintExileZoneId(Game game, Ability source) {
        return CardUtil.getExileZoneId(
                game,
                source.getSourceId(),
                CardUtil.getActualSourceObjectZoneChangeCounter(game, source)
        );
    }

    public RiverSongsDiary(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // Imprint -- Whenever a player casts an instant or sorcery spell from their hand,
        // exile it instead of putting it into a graveyard as it resolves.
        this.addAbility(new RiverSongsDiaryTriggeredAbility());

        // At the beginning of your upkeep, if there are four or more cards exiled with
        // River Song's Diary, choose one of them at random. You may cast it without paying its mana cost.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new RiverSongsDiaryCastEffect())
                .withInterveningIf(RiverSongsDiaryCondition.instance));
    }

    private RiverSongsDiary(final RiverSongsDiary card) {
        super(card);
    }

    @Override
    public RiverSongsDiary copy() {
        return new RiverSongsDiary(this);
    }
}

class RiverSongsDiaryTriggeredAbility extends TriggeredAbilityImpl {

    RiverSongsDiaryTriggeredAbility() {
        super(Zone.BATTLEFIELD, null, false);
    }

    private RiverSongsDiaryTriggeredAbility(final RiverSongsDiaryTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public RiverSongsDiaryTriggeredAbility copy() {
        return new RiverSongsDiaryTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Spell spell = game.getStack().getSpell(event.getTargetId());
        if (spell == null || !spell.isInstantOrSorcery(game) || spell.getFromZone() != Zone.HAND) {
            return false;
        }
        this.getEffects().clear();
        this.addEffect(new RiverSongsDiaryExileEffect(spell, game));
        return true;
    }

    @Override
    public String getRule() {
        return "Imprint &mdash; Whenever a player casts an instant or sorcery spell from their hand, "
                + "exile it instead of putting it into a graveyard as it resolves.";
    }
}

class RiverSongsDiaryExileEffect extends ReplacementEffectImpl {

    private final MageObjectReference spellReference;
    private final MageObjectReference cardReference;

    RiverSongsDiaryExileEffect(Spell spell, Game game) {
        super(Duration.WhileOnStack, Outcome.Benefit);
        this.spellReference = new MageObjectReference(spell.getCard(), game);
        this.cardReference = new MageObjectReference(spell.getMainCard(), game);
    }

    private RiverSongsDiaryExileEffect(final RiverSongsDiaryExileEffect effect) {
        super(effect);
        this.spellReference = effect.spellReference;
        this.cardReference = effect.cardReference;
    }

    @Override
    public RiverSongsDiaryExileEffect copy() {
        return new RiverSongsDiaryExileEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Spell spell = spellReference.getSpell(game);
        Player owner = spell == null ? null : game.getPlayer(spell.getOwnerId());
        if (spell == null || owner == null || !spell.wasCast()) {
            return false;
        }
        owner.moveCardsToExile(
                spell, source, game, false,
                RiverSongsDiary.getImprintExileZoneId(game, source),
                CardUtil.getSourceName(game, source)
        );
        return true;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zoneChangeEvent = (ZoneChangeEvent) event;
        return zoneChangeEvent.getFromZone() == Zone.STACK
                && zoneChangeEvent.getToZone() == Zone.GRAVEYARD
                && spellReference.refersTo(event.getSourceId(), game)
                && cardReference.refersTo(event.getTargetId(), game);
    }
}

enum RiverSongsDiaryCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        ExileZone exileZone = game.getExile().getExileZone(RiverSongsDiary.getImprintExileZoneId(game, source));
        return exileZone != null && exileZone.size() >= 4;
    }

    @Override
    public String toString() {
        return "there are four or more cards exiled with {this}";
    }
}

class RiverSongsDiaryCastEffect extends OneShotEffect {

    RiverSongsDiaryCastEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "choose one of them at random. You may cast it without paying its mana cost";
    }

    private RiverSongsDiaryCastEffect(final RiverSongsDiaryCastEffect effect) {
        super(effect);
    }

    @Override
    public RiverSongsDiaryCastEffect copy() {
        return new RiverSongsDiaryCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        ExileZone exileZone = game.getExile().getExileZone(RiverSongsDiary.getImprintExileZoneId(game, source));
        if (controller == null || exileZone == null || exileZone.isEmpty()) {
            return false;
        }
        Card card = exileZone.getRandom(game);
        return card != null && CardUtil.castSpellWithAttributesForFree(controller, source, game, card);
    }
}
