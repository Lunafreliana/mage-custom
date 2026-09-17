package mage.cards.o;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.LandfallAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.effects.mana.AddManaToManaPoolSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class OmnathLocusOfTheVoid extends CardImpl {

    private static final DynamicValue xValue = OmnathLocusOfTheVoidValue.instance;

    public OmnathLocusOfTheVoid(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{7}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELEMENTAL);

        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // Omnath gets +1/+1 for each unspent mana you have.
        this.addAbility(new SimpleStaticAbility(new BoostSourceEffect(
                xValue, xValue, Duration.WhileOnBattlefield
        )));

        // If you would lose unspent mana, that mana becomes colorless instead.
        this.addAbility(new SimpleStaticAbility(new OmnathLocusOfTheVoidManaEffect()));

        // Landfall — Whenever a land you control enters, add {C}{C}.
        this.addAbility(new LandfallAbility(new AddManaToManaPoolSourceControllerEffect(
                Mana.ColorlessMana(2)
        )));
    }

    private OmnathLocusOfTheVoid(final OmnathLocusOfTheVoid card) {
        super(card);
    }

    @Override
    public OmnathLocusOfTheVoid copy() {
        return new OmnathLocusOfTheVoid(this);
    }
}

class OmnathLocusOfTheVoidManaEffect extends ContinuousEffectImpl {

    OmnathLocusOfTheVoidManaEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Benefit);
        staticText = "if you would lose unspent mana, that mana becomes colorless instead";
    }

    private OmnathLocusOfTheVoidManaEffect(final OmnathLocusOfTheVoidManaEffect effect) {
        super(effect);
    }

    @Override
    public OmnathLocusOfTheVoidManaEffect copy() {
        return new OmnathLocusOfTheVoidManaEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            player.getManaPool().setManaBecomesColorless(true);
        }
        return true;
    }
}

enum OmnathLocusOfTheVoidValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        Player player = game.getPlayer(sourceAbility.getControllerId());
        return player == null ? 0 : player.getManaPool().count();
    }

    @Override
    public OmnathLocusOfTheVoidValue copy() {
        return this;
    }

    @Override
    public String getMessage() {
        return "unspent mana you have";
    }
}
