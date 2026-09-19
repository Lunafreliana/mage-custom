package mage.cards.u;

import mage.abilities.Ability;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.discard.DiscardControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.watchers.common.ManaPaidSourceWatcher;

import java.util.Optional;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class UncoverTheMoonLetters extends CardImpl {

    public UncoverTheMoonLetters(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{U}");

        // Whenever you cast a noncreature spell, you may draw X cards, where X is the amount of mana spent to cast that spell. If you do, discard two cards.
        Ability ability = new SpellCastControllerTriggeredAbility(
                new DrawCardSourceControllerEffect(UncoverTheMoonLettersValue.instance),
                StaticFilters.FILTER_SPELL_A_NON_CREATURE,
                true
        );
        ability.addEffect(new DiscardControllerEffect(2).setText("If you do, discard two cards"));
        this.addAbility(ability);
    }

    private UncoverTheMoonLetters(final UncoverTheMoonLetters card) {
        super(card);
    }

    @Override
    public UncoverTheMoonLetters copy() {
        return new UncoverTheMoonLetters(this);
    }
}

enum UncoverTheMoonLettersValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability source, Effect effect) {
        return Optional
                .ofNullable((Spell) effect.getValue("spellCast"))
                .map(spell -> ManaPaidSourceWatcher.getTotalPaid(spell.getId(), game))
                .orElse(0);
    }

    @Override
    public UncoverTheMoonLettersValue copy() {
        return this;
    }

    @Override
    public String getMessage() {
        return "the amount of mana spent to cast that spell";
    }

    @Override
    public String toString() {
        return "X";
    }
}
