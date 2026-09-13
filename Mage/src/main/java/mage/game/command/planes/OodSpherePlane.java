package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.IsStillOnPlaneCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.combat.GoadTargetEffect;
import mage.abilities.keyword.ConvokeAbility;
import mage.cards.Card;
import mage.constants.CardType;
import mage.constants.CommanderCardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.filter.common.FilterNonlandCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetadjustment.ForEachPlayerTargetsAdjuster;
import mage.target.targetpointer.EachTargetPointer;

/**
 * @author The XMage Developers
 */
public final class OodSpherePlane extends Plane {

    public OodSpherePlane() {
        this.setPlaneType(Planes.PLANE_OOD_SPHERE);

        // Song of the Ood — Noncreature spells have convoke.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new ConditionalContinuousEffect(
                new OodSphereConvokeEffect(),
                new IsStillOnPlaneCondition(this.getName()),
                "Noncreature spells have convoke."
        )).withFlavorWord("Song of the Ood"));

        // Red-Eye — Whenever chaos ensues, for each opponent, goad up to one target creature that
        // opponent controls. Until your next turn, those creatures can't become tapped unless
        // they're being declared as attackers.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new GoadTargetEffect().setTargetPointer(new EachTargetPointer()), false
        );
        ability.addEffect(new OodSphereCantTapEffect().setTargetPointer(new EachTargetPointer()));
        ability.addTarget(new TargetCreaturePermanent(0, 1));
        ability.setTargetAdjuster(new ForEachPlayerTargetsAdjuster(false, true));
        this.getAbilities().add(ability.withFlavorWord("Red-Eye"));
    }

    private OodSpherePlane(final OodSpherePlane plane) {
        super(plane);
    }

    @Override
    public OodSpherePlane copy() {
        return new OodSpherePlane(this);
    }
}

class OodSphereConvokeEffect extends ContinuousEffectImpl {

    private static final FilterNonlandCard filter = new FilterNonlandCard("noncreature spells");
    private final Ability convokeAbility;

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
        filter.add(Predicates.not(new AbilityPredicate(ConvokeAbility.class)));
    }

    OodSphereConvokeEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.convokeAbility = new ConvokeAbility();
        this.staticText = "noncreature spells have convoke";
    }

    private OodSphereConvokeEffect(final OodSphereConvokeEffect effect) {
        super(effect);
        this.convokeAbility = effect.convokeAbility.copy();
    }

    @Override
    public OodSphereConvokeEffect copy() {
        return new OodSphereConvokeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Player player : game.getPlayers().values()) {
            for (Card card : player.getLibrary().getCards(game)) {
                addConvoke(card, player, source, game);
            }
            for (Card card : player.getHand().getCards(game)) {
                addConvoke(card, player, source, game);
            }
            for (Card card : player.getGraveyard().getCards(game)) {
                addConvoke(card, player, source, game);
            }
            game.getCommanderCardsFromCommandZone(player, CommanderCardType.ANY)
                    .forEach(card -> addConvoke(card, player, source, game));
        }
        for (Card card : game.getExile().getAllCards(game)) {
            Player owner = game.getPlayer(card.getOwnerId());
            if (owner != null) {
                addConvoke(card, owner, source, game);
            }
        }
        for (StackObject stackObject : game.getStack()) {
            if (stackObject instanceof Spell) {
                Card card = game.getCard(stackObject.getSourceId());
                Player controller = game.getPlayer(stackObject.getControllerId());
                if (card != null && controller != null) {
                    addConvoke(card, controller, source, game);
                }
            }
        }
        return true;
    }

    private void addConvoke(Card card, Player player, Ability source, Game game) {
        if (filter.match(card, player.getId(), source, game)) {
            game.getState().addOtherAbility(card, convokeAbility);
        }
    }
}

class OodSphereCantTapEffect extends ReplacementEffectImpl {

    OodSphereCantTapEffect() {
        super(Duration.UntilYourNextTurn, Outcome.Detriment);
        staticText = "Until your next turn, those creatures can't become tapped unless they're being declared as attackers";
    }

    private OodSphereCantTapEffect(final OodSphereCantTapEffect effect) {
        super(effect);
    }

    @Override
    public OodSphereCantTapEffect copy() {
        return new OodSphereCantTapEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.TAP;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return !event.getFlag()
                && getTargetPointer().getTargets(game, source).contains(event.getTargetId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        return true;
    }
}
