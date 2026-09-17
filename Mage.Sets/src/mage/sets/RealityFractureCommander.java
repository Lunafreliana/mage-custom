package mage.sets;

import mage.cards.ExpansionSet;
import mage.constants.Rarity;
import mage.constants.SetType;

/**
 * @author muz
 */
public final class RealityFractureCommander extends ExpansionSet {

    private static final RealityFractureCommander instance = new RealityFractureCommander();

    public static RealityFractureCommander getInstance() {
        return instance;
    }

    private RealityFractureCommander() {
        super("Reality Fracture Commander", "FRC", ExpansionSet.buildDate(2026, 10, 2), SetType.SUPPLEMENTAL);
        this.hasBasicLands = false;

        cards.add(new SetCardInfo("Avacyn, Angel of Horror", 7, Rarity.RARE, mage.cards.a.AvacynAngelOfHorror.class));
        cards.add(new SetCardInfo("Darksteel Angel", 98, Rarity.RARE, mage.cards.d.DarksteelAngel.class, NON_FULL_USE_VARIOUS));
        cards.add(new SetCardInfo("Darksteel Angel", 13, Rarity.RARE, mage.cards.d.DarksteelAngel.class, NON_FULL_USE_VARIOUS));
        cards.add(new SetCardInfo("Ob Nixilis, the Ascended", 5, Rarity.RARE, mage.cards.o.ObNixilisTheAscended.class));
        cards.add(new SetCardInfo("Omnath, Locus of the Void", 3, Rarity.RARE, mage.cards.o.OmnathLocusOfTheVoid.class));
    }
}
