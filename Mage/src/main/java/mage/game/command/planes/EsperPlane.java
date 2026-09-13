package mage.game.command.planes;

import mage.MageObjectReference;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.AddCardTypeTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.cost.SpellsCostReductionAllEffect;
import mage.abilities.keyword.LifelinkAbility;
import mage.abilities.keyword.MenaceAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.Card;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.common.FilterArtifactCard;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTargets;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author TheElk801
 */
public final class EsperPlane extends Plane {

    public EsperPlane() {
        this.setPlaneType(Planes.PLANE_ESPER);

        // Artifact spells cost {1} less to cast.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new EsperCostReductionEffect()
        ));

        // Whenever chaos ensues, creatures you control that are white, blue, and/or black become artifacts in addition to their other types until end of turn. Then each artifact creature you control gains vigilance, menace, and lifelink until end of turn.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new EsperChaosEffect(), false));
    }

    private EsperPlane(final EsperPlane plane) {
        super(plane);
    }

    @Override
    public EsperPlane copy() {
        return new EsperPlane(this);
    }
}

class EsperCostReductionEffect extends SpellsCostReductionAllEffect {

    EsperCostReductionEffect() {
        super(new FilterArtifactCard("Artifact spells"), 1);
    }

    private EsperCostReductionEffect(final EsperCostReductionEffect effect) {
        super(effect);
    }

    @Override
    protected boolean selectedByRuntimeData(Card card, Ability source, Game game) {
        return game.getState().hasFaceUpPlane(Planes.PLANE_ESPER);
    }

    @Override
    public EsperCostReductionEffect copy() {
        return new EsperCostReductionEffect(this);
    }
}

class EsperChaosEffect extends OneShotEffect {

    EsperChaosEffect() {
        super(Outcome.Benefit);
        staticText = "creatures you control that are white, blue, and/or black become artifacts in addition "
                + "to their other types until end of turn. Then each artifact creature you control gains "
                + "vigilance, menace, and lifelink until end of turn";
    }

    private EsperChaosEffect(final EsperChaosEffect effect) {
        super(effect);
    }

    @Override
    public EsperChaosEffect copy() {
        return new EsperChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Set<MageObjectReference> coloredCreatures = new LinkedHashSet<>();
        Set<MageObjectReference> artifactCreatures = new LinkedHashSet<>();
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(source.getControllerId())) {
            if (!permanent.isCreature(game)) {
                continue;
            }
            if (permanent.isArtifact(game)) {
                artifactCreatures.add(new MageObjectReference(permanent, game));
            }
            if (permanent.getColor(game).contains(ObjectColor.WHITE)
                    || permanent.getColor(game).contains(ObjectColor.BLUE)
                    || permanent.getColor(game).contains(ObjectColor.BLACK)) {
                MageObjectReference reference = new MageObjectReference(permanent, game);
                coloredCreatures.add(reference);
                artifactCreatures.add(reference);
            }
        }

        if (!coloredCreatures.isEmpty()) {
            game.addEffect(new AddCardTypeTargetEffect(Duration.EndOfTurn, CardType.ARTIFACT)
                    .setTargetPointer(new FixedTargets(coloredCreatures)), source);
        }
        if (!artifactCreatures.isEmpty()) {
            FixedTargets targets = new FixedTargets(artifactCreatures);
            game.addEffect(new GainAbilityTargetEffect(VigilanceAbility.getInstance(), Duration.EndOfTurn)
                    .setTargetPointer(targets), source);
            game.addEffect(new GainAbilityTargetEffect(new MenaceAbility(false), Duration.EndOfTurn)
                    .setTargetPointer(targets), source);
            game.addEffect(new GainAbilityTargetEffect(LifelinkAbility.getInstance(), Duration.EndOfTurn)
                    .setTargetPointer(targets), source);
        }
        return true;
    }
}
