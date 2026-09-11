package mage.cards.m;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DiesCreatureTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.FaceVillainousChoiceOpponentsEffect;
import mage.abilities.effects.common.continuous.BecomesCybermanEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.FaceVillainousChoice;
import mage.choices.VillainousChoice;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class Missy extends CardImpl {

    private static final FilterCreaturePermanent filter =
            new FilterCreaturePermanent("another nonartifact creature");
    private static final FaceVillainousChoice choice = new FaceVillainousChoice(
            Outcome.Damage, new MissyFirstChoice(), new MissySecondChoice()
    );

    static {
        filter.add(AnotherPredicate.instance);
        filter.add(Predicates.not(CardType.ARTIFACT.getPredicate()));
    }

    public Missy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TIME_LORD);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(4);
        this.toughness = new MageInt(5);

        // Whenever another nonartifact creature dies, return it to the battlefield under your control face down and tapped. It's a 2/2 Cyberman artifact creature.
        this.addAbility(new DiesCreatureTriggeredAbility(new MissyReturnEffect(), false, filter, true));

        // At the beginning of your end step, each opponent faces a villainous choice -- Each artifact creature you control deals 1 damage to that opponent, or you draw a card and chaos ensues.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                new FaceVillainousChoiceOpponentsEffect(choice)
        ));
    }

    private Missy(final Missy card) {
        super(card);
    }

    @Override
    public Missy copy() {
        return new Missy(this);
    }
}

class MissyReturnEffect extends OneShotEffect {

    MissyReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "return it to the battlefield under your control face down and tapped. " +
                "It's a 2/2 Cyberman artifact creature";
    }

    private MissyReturnEffect(final MissyReturnEffect effect) {
        super(effect);
    }

    @Override
    public MissyReturnEffect copy() {
        return new MissyReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        MageObjectReference mor = new MageObjectReference(
                card.getId(), card.getZoneChangeCounter(game) + 1, game
        );
        game.addEffect(new BecomesCybermanEffect(mor), source);
        return controller.moveCards(card, Zone.BATTLEFIELD, source, game, true, true, false, null);
    }
}

class MissyFirstChoice extends VillainousChoice {

    MissyFirstChoice() {
        super("Each artifact creature you control deals 1 damage to that opponent",
                "Each artifact creature {controller} controls deals 1 damage to you");
    }

    @Override
    public boolean doChoice(Player player, Game game, Ability source) {
        for (Permanent permanent : game.getBattlefield().getActivePermanents(
                StaticFilters.FILTER_PERMANENT_ARTIFACT_CREATURE, source.getControllerId(), source, game
        )) {
            player.damage(1, permanent.getId(), source, game);
        }
        return true;
    }
}

class MissySecondChoice extends VillainousChoice {

    MissySecondChoice() {
        super("you draw a card and chaos ensues", "{controller} draws a card and chaos ensues");
    }

    @Override
    public boolean doChoice(Player player, Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.drawCards(1, source, game);
        // Plane cards currently model their chaos abilities inside planar-die
        // roll abilities, so XMage has no general chaos-ensues event to fire.
        return true;
    }
}
