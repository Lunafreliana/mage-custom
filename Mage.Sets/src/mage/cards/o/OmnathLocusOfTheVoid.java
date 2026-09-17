package mage.cards.o;

import mage.MageInt;
import mage.Mana;
import mage.abilities.common.LandfallAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.UnspentManaCount;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.effects.common.continuous.ManaBecomesColorlessEffect;
import mage.abilities.effects.mana.AddManaToManaPoolSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;

import java.util.UUID;

/**
 * @author muz
 */
public final class OmnathLocusOfTheVoid extends CardImpl {

    private static final DynamicValue xValue = UnspentManaCount.instance;

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
        this.addAbility(new SimpleStaticAbility(new ManaBecomesColorlessEffect()));

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
