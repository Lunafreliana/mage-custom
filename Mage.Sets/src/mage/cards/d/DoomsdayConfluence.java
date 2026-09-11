package mage.cards.d;

import mage.abilities.Mode;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.SacrificeAllEffect;
import mage.abilities.effects.common.discard.DiscardEachPlayerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.TargetController;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.game.permanent.token.DalekToken;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class DoomsdayConfluence extends CardImpl {

    private static final FilterCreaturePermanent filter
            = new FilterCreaturePermanent("nonartifact creature");

    static {
        filter.add(Predicates.not(CardType.ARTIFACT.getPredicate()));
    }

    public DoomsdayConfluence(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{X}{X}{B}");

        // Choose X. You may choose the same mode more than once.
        this.getSpellAbility().getModes().setMinModes(0);
        this.getSpellAbility().getModes().setMaxModes(0);
        this.getSpellAbility().getModes().setModesToChoose(GetXValue.instance);
        this.getSpellAbility().getModes().setChooseText("choose X");
        this.getSpellAbility().getModes().setMayChooseSameModeMoreThanOnce(true);

        // Each player sacrifices a nonartifact creature.
        this.getSpellAbility().addEffect(new SacrificeAllEffect(filter));

        // Create a 3/3 black Dalek artifact creature token with menace.
        this.getSpellAbility().addMode(new Mode(new CreateTokenEffect(new DalekToken())));

        // Each opponent discards a card.
        this.getSpellAbility().addMode(new Mode(
                new DiscardEachPlayerEffect(TargetController.OPPONENT)
        ));
    }

    private DoomsdayConfluence(final DoomsdayConfluence card) {
        super(card);
    }

    @Override
    public DoomsdayConfluence copy() {
        return new DoomsdayConfluence(this);
    }
}
