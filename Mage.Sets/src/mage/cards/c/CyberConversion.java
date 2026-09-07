package mage.cards.c;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
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
        MageObjectReference objectReference = new MageObjectReference(permanent, game);
        game.addEffect(new BecomesFaceDownCreatureEffect(
                null, objectReference, Duration.Custom, BecomesFaceDownCreatureEffect.FaceDownType.MANUAL
        ), source);
        game.addEffect(new CyberConversionTypeEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
        return true;
    }
}

class CyberConversionTypeEffect extends ContinuousEffectImpl {

    CyberConversionTypeEffect() {
        super(Duration.Custom, Layer.CopyEffects_1, SubLayer.FaceDownEffects_1b, Outcome.Neutral);
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
        permanent.removeAllSuperTypes(game);
        permanent.removeAllCardTypes(game);
        permanent.removeAllSubTypes(game);
        permanent.addCardType(game, CardType.ARTIFACT, CardType.CREATURE);
        permanent.addSubType(game, SubType.CYBERMAN);
        return true;
    }
}
