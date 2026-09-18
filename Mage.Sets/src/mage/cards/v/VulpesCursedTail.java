package mage.cards.v;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.game.permanent.token.TailcurseToken;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class VulpesCursedTail extends CardImpl {

    public VulpesCursedTail(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{U}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.FOX);
        this.subtype.add(SubType.SPIRIT);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Whenever Vulpes enters or attacks, create a colorless Aura Curse enchantment token named Tailcurse attached to target player. It has enchant player and “Whenever enchanted player is dealt combat damage, you and each other player who controlled a source that dealt combat damage to that player this way each create that many 1/1 red Elemental creature tokens.”
        Ability ability = new EntersBattlefieldOrAttacksSourceTriggeredAbility(new VulpesCursedTailEffect());
        ability.addTarget(new TargetPlayer());
        this.addAbility(ability);
    }

    private VulpesCursedTail(final VulpesCursedTail card) {
        super(card);
    }

    @Override
    public VulpesCursedTail copy() {
        return new VulpesCursedTail(this);
    }
}

class VulpesCursedTailEffect extends OneShotEffect {

    VulpesCursedTailEffect() {
        super(Outcome.Benefit);
        staticText = "create a colorless Aura Curse enchantment token named Tailcurse attached to target player. "
                + "It has enchant player and \"Whenever enchanted player is dealt combat damage, you and each other "
                + "player who controlled a source that dealt combat damage to that player this way each create that "
                + "many 1/1 red Elemental creature tokens.\"";
    }

    private VulpesCursedTailEffect(final VulpesCursedTailEffect effect) {
        super(effect);
    }

    @Override
    public VulpesCursedTailEffect copy() {
        return new VulpesCursedTailEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        UUID targetId = getTargetPointer().getFirst(game, source);
        return targetId != null && new TailcurseToken().putOntoBattlefield(
                1, game, source, source.getControllerId(), false, false, null, targetId
        );
    }
}
