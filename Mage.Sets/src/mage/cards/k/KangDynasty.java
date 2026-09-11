package mage.cards.k;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.BatchTriggeredAbility;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.SagaAbility;
import mage.abilities.dynamicvalue.common.CardsInControllerHandCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.abilities.effects.common.combat.CantBeBlockedTargetEffect;
import mage.abilities.effects.common.combat.GoadTargetEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SagaChapter;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.events.DamagedBatchBySourceEvent;
import mage.game.events.DamagedEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetadjustment.ForEachPlayerTargetsAdjuster;
import mage.target.targetpointer.EachTargetPointer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class KangDynasty extends CardImpl {

    public KangDynasty(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{U}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this);

        // I, II — For each opponent, tap up to one target creature that player controls. Goad those creatures. Until your next turn, whenever any of those creatures deals combat damage to a player, draw a card.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_II, chapterAbility -> {
            chapterAbility.addEffect(new TapTargetEffect()
                    .setTargetPointer(new EachTargetPointer())
                    .setText("for each opponent, tap up to one target creature that player controls"));
            chapterAbility.addEffect(new GoadTargetEffect()
                    .setTargetPointer(new EachTargetPointer())
                    .setText("Goad those creatures"));
            chapterAbility.addEffect(new KangDynastyCreateDelayedTriggerEffect());
            chapterAbility.addTarget(new TargetCreaturePermanent(0, 1));
            chapterAbility.setTargetAdjuster(new ForEachPlayerTargetsAdjuster(false, true));
        });

        // III — Target creature you control gets +1/+1 until end of turn for each card in your hand and can't be blocked this turn.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_III, chapterAbility -> {
            chapterAbility.addEffect(new BoostTargetEffect(
                    CardsInControllerHandCount.ANY, CardsInControllerHandCount.ANY
            ));
            chapterAbility.addEffect(new CantBeBlockedTargetEffect().setText("and can't be blocked this turn"));
            chapterAbility.addTarget(new TargetControlledCreaturePermanent());
        });

        this.addAbility(sagaAbility);
    }

    private KangDynasty(final KangDynasty card) {
        super(card);
    }

    @Override
    public KangDynasty copy() {
        return new KangDynasty(this);
    }
}

class KangDynastyCreateDelayedTriggerEffect extends OneShotEffect {

    KangDynastyCreateDelayedTriggerEffect() {
        super(Outcome.DrawCard);
        this.setTargetPointer(new EachTargetPointer());
        staticText = "Until your next turn, whenever any of those creatures deals combat damage to a player, draw a card";
    }

    private KangDynastyCreateDelayedTriggerEffect(final KangDynastyCreateDelayedTriggerEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<MageObjectReference> creatures = new ArrayList<>();
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Permanent permanent = game.getPermanent(targetId);
            if (permanent != null) {
                creatures.add(new MageObjectReference(permanent, game));
            }
        }
        if (!creatures.isEmpty()) {
            game.addDelayedTriggeredAbility(new KangDynastyDelayedTriggeredAbility(creatures), source);
        }
        return true;
    }

    @Override
    public KangDynastyCreateDelayedTriggerEffect copy() {
        return new KangDynastyCreateDelayedTriggerEffect(this);
    }
}

class KangDynastyDelayedTriggeredAbility extends DelayedTriggeredAbility implements BatchTriggeredAbility<DamagedEvent> {

    private final List<MageObjectReference> creatures;

    KangDynastyDelayedTriggeredAbility(List<MageObjectReference> creatures) {
        super(new DrawCardSourceControllerEffect(1), Duration.UntilYourNextTurn, false, false);
        this.creatures = creatures;
        setTriggerPhrase("Until your next turn, whenever any of those creatures deals combat damage to a player, ");
    }

    private KangDynastyDelayedTriggeredAbility(final KangDynastyDelayedTriggeredAbility ability) {
        super(ability);
        this.creatures = new ArrayList<>(ability.creatures);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_BATCH_BY_SOURCE;
    }

    @Override
    public boolean checkEvent(DamagedEvent event, Game game) {
        return event.isCombatDamage()
                && event.getAmount() > 0
                && game.getPlayer(event.getTargetId()) != null
                && creatures.contains(new MageObjectReference(event.getSourceId(), game));
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return !getFilteredEvents((DamagedBatchBySourceEvent) event, game).isEmpty();
    }

    @Override
    public KangDynastyDelayedTriggeredAbility copy() {
        return new KangDynastyDelayedTriggeredAbility(this);
    }
}
