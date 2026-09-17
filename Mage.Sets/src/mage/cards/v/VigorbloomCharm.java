package mage.cards.v;

import mage.abilities.Mode;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.FightTargetsEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.HexproofAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetControlledPermanent;
import mage.target.common.TargetOpponentsCreaturePermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class VigorbloomCharm extends CardImpl {

    public VigorbloomCharm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{G}{W}");

        // Choose one --
        // * Target permanent you control gains hexproof and indestructible until end of turn.
        this.getSpellAbility().addEffect(new GainAbilityTargetEffect(HexproofAbility.getInstance())
                .setText("target permanent you control gains hexproof"));
        this.getSpellAbility().addEffect(new GainAbilityTargetEffect(IndestructibleAbility.getInstance())
                .setText("and indestructible until end of turn"));
        this.getSpellAbility().addTarget(new TargetControlledPermanent());

        // * You draw a card and gain 3 life.
        Mode mode = new Mode(new DrawCardSourceControllerEffect(1));
        mode.addEffect(new GainLifeEffect(3).concatBy("and"));
        this.getSpellAbility().addMode(mode);

        // * Put a +1/+1 counter on target creature you control. Then it fights target creature an opponent controls.
        mode = new Mode(new AddCountersTargetEffect(CounterType.P1P1.createInstance()));
        mode.addEffect(new FightTargetsEffect().setText(
                "Then it fights target creature an opponent controls. "
                        + "<i>(Each deals damage equal to its power to the other.)</i>"));
        mode.addTarget(new TargetControlledCreaturePermanent());
        mode.addTarget(new TargetOpponentsCreaturePermanent());
        this.getSpellAbility().addMode(mode);
    }

    private VigorbloomCharm(final VigorbloomCharm card) {
        super(card);
    }

    @Override
    public VigorbloomCharm copy() {
        return new VigorbloomCharm(this);
    }
}
