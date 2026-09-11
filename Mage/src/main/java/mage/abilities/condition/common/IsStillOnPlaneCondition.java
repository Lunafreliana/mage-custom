
package mage.abilities.condition.common;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.game.Game;

/**
 * @author spjspj
 */
public class IsStillOnPlaneCondition implements Condition {

    private String planeName;

    public IsStillOnPlaneCondition(String planeName) {
        this.planeName = planeName;
    }
    
    public IsStillOnPlaneCondition(IsStillOnPlaneCondition condition) {        
        this.planeName = condition.planeName;
    }

    @Override
    public boolean apply(Game game, Ability source) {

        return game.getState().getFaceUpPlanes().stream()
                .anyMatch(plane -> plane.getName().equalsIgnoreCase(planeName));
    }
}
