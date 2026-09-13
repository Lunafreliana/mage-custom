package mage.abilities;

import mage.game.Game;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Special actions with individual activation restrictions (GUI has a special button to show available actions)
 * <p>
 * Two types of action:
 * - mana actions (auto-generated on each mana pay cycle, auto-clean)
 * - another actions (manual added, manual removed - like one short effects)
 *
 * @author BetaSteward_at_googlemail.com
 */
public class SpecialActions extends AbilitiesImpl<SpecialAction> {

    public SpecialActions() {
    }

    protected SpecialActions(final SpecialActions actions) {
        super(actions);
    }

    /**
     * @param controllerId
     * @param manaAction   true  = if mana actions should get returned
     *                     false = only non mana actions get returned
     * @return
     */
    public Map<UUID, SpecialAction> getControlledBy(UUID controllerId, boolean manaAction) {
        LinkedHashMap<UUID, SpecialAction> controlledBy = new LinkedHashMap<>();
        for (SpecialAction action : this) {
            if (action.isControlledBy(controllerId) && action.isManaAction() == manaAction) {
                controlledBy.put(action.getId(), action);
            }
        }
        return controlledBy;
    }

    /**
     * Available choices for the special-action button and its menu. Registration
     * alone does not mean an action is legal at the current priority or payment step.
     */
    public Map<UUID, SpecialAction> getAvailableActions(UUID controllerId, boolean manaAction, Game game) {
        Map<UUID, SpecialAction> available = getControlledBy(controllerId, manaAction);
        available.values().removeIf(action -> !action.canActivate(controllerId, game).canActivate());
        return available;
    }

    @Override
    public SpecialActions copy() {
        return new SpecialActions(this);
    }

    public void removeManaActions() {
        this.removeIf(SpecialAction::isManaAction);
    }
}
