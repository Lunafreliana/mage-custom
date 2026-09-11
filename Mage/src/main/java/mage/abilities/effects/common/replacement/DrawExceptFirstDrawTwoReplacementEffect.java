package mage.abilities.effects.common.replacement;

/**
 * @author VibecodingQueens
 */
public class DrawExceptFirstDrawTwoReplacementEffect extends DrawExceptFirstDrawReplacementEffect {

    public DrawExceptFirstDrawTwoReplacementEffect() {
        super(2);
        staticText = "If you would draw a card except the first one you draw in each of your draw steps, draw two cards instead";
    }

    protected DrawExceptFirstDrawTwoReplacementEffect(final DrawExceptFirstDrawTwoReplacementEffect effect) {
        super(effect);
    }

    @Override
    public DrawExceptFirstDrawTwoReplacementEffect copy() {
        return new DrawExceptFirstDrawTwoReplacementEffect(this);
    }

}
