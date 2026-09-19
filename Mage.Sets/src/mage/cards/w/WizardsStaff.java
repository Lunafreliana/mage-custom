package mage.cards.w;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.abilities.keyword.ProwessAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AttachmentType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.target.TargetPermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class WizardsStaff extends CardImpl {

    private static final FilterPermanent filter
            = new FilterControlledCreaturePermanent(SubType.WIZARD, "Wizard");

    public WizardsStaff(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}{U}");

        this.subtype.add(SubType.EQUIPMENT);

        // Equipped creature has prowess.
        this.addAbility(new SimpleStaticAbility(new GainAbilityAttachedEffect(
                new ProwessAbility(), AttachmentType.EQUIPMENT
        )));

        // If an ability of equipped creature triggers, that ability triggers an additional time.
        this.addAbility(new SimpleStaticAbility(new GainAbilityAttachedEffect(
                new SimpleStaticAbility(new WizardsStaffEffect()), AttachmentType.EQUIPMENT
        )));

        // Equip Wizard {1}
        this.addAbility(new EquipAbility(
                Outcome.AddAbility, new GenericManaCost(1), new TargetPermanent(filter), false
        ));

        // Equip {3}
        this.addAbility(new EquipAbility(3, false));
    }

    private WizardsStaff(final WizardsStaff card) {
        super(card);
    }

    @Override
    public WizardsStaff copy() {
        return new WizardsStaff(this);
    }
}

class WizardsStaffEffect extends ReplacementEffectImpl {

    WizardsStaffEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "If an ability of this creature triggers, that ability triggers an additional time.";
    }

    private WizardsStaffEffect(final WizardsStaffEffect effect) {
        super(effect);
    }

    @Override
    public WizardsStaffEffect copy() {
        return new WizardsStaffEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.NUMBER_OF_TRIGGERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return source.getSourceId().equals(event.getSourceId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmount(CardUtil.overflowInc(event.getAmount(), 1));
        return false;
    }
}
