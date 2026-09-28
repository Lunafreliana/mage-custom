package mage.game.command.planes;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SpellCastAllTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.keyword.DevourAbility;
import mage.cards.Card;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.filter.FilterSpell;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.token.GoblinToken;
import mage.game.stack.Spell;

/**
 * @author The XMage Developers
 */
public final class JundPlane extends Plane {

    private static final FilterSpell filter = new FilterSpell("creature spell that's black, red, or green");

    static {
        filter.add(CardType.CREATURE.getPredicate());
        filter.add(Predicates.or(
                new ColorPredicate(ObjectColor.BLACK),
                new ColorPredicate(ObjectColor.RED),
                new ColorPredicate(ObjectColor.GREEN)
        ));
    }

    public JundPlane() {
        setPlaneType(Planes.PLANE_JUND);

        // Whenever a player casts a creature spell that's black, red, or green, it gains devour 5.
        getAbilities().add(new SpellCastAllTriggeredAbility(
                Zone.COMMAND, new JundDevourEffect(), filter, false, SetTargetPointer.SPELL
        ));

        // Whenever chaos ensues, create two 1/1 red Goblin creature tokens.
        getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new CreateTokenEffect(new GoblinToken(), 2), false
        ));
    }

    private JundPlane(final JundPlane plane) {
        super(plane);
    }

    @Override
    public JundPlane copy() {
        return new JundPlane(this);
    }
}

class JundDevourEffect extends ContinuousEffectImpl {

    JundDevourEffect() {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "it gains devour 5";
    }

    private JundDevourEffect(final JundDevourEffect effect) {
        super(effect);
    }

    @Override
    public JundDevourEffect copy() {
        return new JundDevourEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Spell spell = game.getStack().getSpell(getTargetPointer().getFirst(game, source));
        if (spell == null) {
            discard();
            return false;
        }
        Card card = spell.getCard();
        if (card == null) {
            discard();
            return false;
        }
        // Multiple instances of devour are cumulative replacement effects. Do not suppress
        // Jund's devour 5 when the spell already has a different devour ability.
        game.getState().addOtherAbility(card, new DevourAbility(5));
        return true;
    }
}
