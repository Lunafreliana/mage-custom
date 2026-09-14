package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.keyword.CascadeAbility;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.target.common.TargetArtifactPermanent;
import mage.watchers.common.SpellsCastWatcher;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class TardisBayPlane extends Plane {

    public TardisBayPlane() {
        this.setPlaneType(Planes.PLANE_TARDIS_BAY);

        // The first spell you cast during each of your turns with mana value 2 or greater has cascade.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new TardisBayCascadeEffect()
        ));

        // When chaos ensues, gain control of target artifact. Then planeswalk.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new GainControlTargetEffect(Duration.EndOfGame), false
        );
        ability.addTarget(new TargetArtifactPermanent());
        ability.addEffect(new PlaneswalkEffect(false).concatBy("Then"));
        this.getAbilities().add(ability);
    }

    private TardisBayPlane(final TardisBayPlane plane) {
        super(plane);
    }

    @Override
    public TardisBayPlane copy() {
        return new TardisBayPlane(this);
    }
}

class TardisBayCascadeEffect extends ContinuousEffectImpl {

    private final Ability cascadeAbility;

    TardisBayCascadeEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.cascadeAbility = new CascadeAbility(false);
        staticText = "The first spell you cast during each of your turns with mana value 2 or greater has cascade";
    }

    private TardisBayCascadeEffect(final TardisBayCascadeEffect effect) {
        super(effect);
        this.cascadeAbility = effect.cascadeAbility.copy();
    }

    @Override
    public TardisBayCascadeEffect copy() {
        return new TardisBayCascadeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        UUID controllerId = source.getControllerId();
        if (!controllerId.equals(game.getActivePlayerId())
                || game.getState().getFaceUpPlanarCards().stream()
                .noneMatch(card -> card.getId().equals(source.getSourceId()))) {
            return false;
        }
        SpellsCastWatcher watcher = game.getState().getWatcher(SpellsCastWatcher.class);
        if (watcher == null) {
            return false;
        }
        UUID firstQualifyingSpellId = watcher.getSpellsCastThisTurn(controllerId).stream()
                .filter(spell -> spell.getManaValue() >= 2)
                .map(Spell::getId)
                .findFirst()
                .orElse(null);
        if (firstQualifyingSpellId == null) {
            return true;
        }
        for (StackObject stackObject : game.getStack()) {
            if (!(stackObject instanceof Spell)
                    || !stackObject.getId().equals(firstQualifyingSpellId)) {
                continue;
            }
            Card card = game.getCard(stackObject.getSourceId());
            if (card != null) {
                game.getState().addOtherAbility(card, cascadeAbility);
            }
        }
        return true;
    }
}
