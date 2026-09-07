package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureAllEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.PermanentIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

public final class CyberConversion extends CardImpl {

    public CyberConversion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}{U}");

        // Turn target creature face down. It's a 2/2 Cyberman artifact creature.
        this.getSpellAbility().addEffect(new CyberConversionEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private CyberConversion(final CyberConversion card) {
        super(card);
    }

    @Override
    public CyberConversion copy() {
        return new CyberConversion(this);
    }
}

class CyberConversionEffect extends OneShotEffect {

    CyberConversionEffect() {
        super(Outcome.Detriment);
        this.staticText = "turn target creature face down. It's a 2/2 Cyberman artifact creature";
    }

    private CyberConversionEffect(final CyberConversionEffect effect) {
        super(effect);
    }

    @Override
    public CyberConversionEffect copy() {
        return new CyberConversionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || permanent.isTransformable()) {
            return false;
        }
        FilterCreaturePermanent filter = new FilterCreaturePermanent();
        filter.add(new PermanentIdPredicate(permanent.getId()));
        game.addEffect(new BecomesFaceDownCreatureAllEffect(filter), source);
        game.addEffect(new CyberConversionTypeEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
        return true;
    }
}

class CyberConversionTypeEffect extends ContinuousEffectImpl {

    CyberConversionTypeEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private CyberConversionTypeEffect(final CyberConversionTypeEffect effect) {
        super(effect);
    }

    @Override
    public CyberConversionTypeEffect copy() {
        return new CyberConversionTypeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || !permanent.isFaceDown(game)) {
            discard();
            return false;
        }
        permanent.addCardType(game, CardType.ARTIFACT);
        permanent.addSubType(game, SubType.CYBERMAN);
        return true;
    }
}
