package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.EntersBattlefieldControlledTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.DevotionCount;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.choices.ChoiceColor;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.DependencyType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.players.Player;

/**
 * @author The XMage Developers
 */
public final class NyxPlane extends Plane {

    private static final FilterPermanent FILTER_NONTOKEN_CREATURE =
            new FilterPermanent("nontoken creatures");
    private static final FilterPermanent FILTER_ENCHANTMENT =
            new FilterPermanent("enchantment");

    static {
        FILTER_NONTOKEN_CREATURE.add(CardType.CREATURE.getPredicate());
        FILTER_NONTOKEN_CREATURE.add(TokenPredicate.FALSE);
        FILTER_ENCHANTMENT.add(CardType.ENCHANTMENT.getPredicate());
    }

    public NyxPlane() {
        setPlaneType(Planes.PLANE_NYX);

        // Nontoken creatures are enchantments in addition to their other types.
        getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new NyxTypeEffect()));

        // Constellation — Whenever an enchantment you control enters, you gain 1 life.
        Ability ability = new EntersBattlefieldControlledTriggeredAbility(
                Zone.COMMAND, new GainLifeEffect(1), FILTER_ENCHANTMENT, false
        );
        ability.setAbilityWord(AbilityWord.CONSTELLATION);
        getAbilities().add(ability);

        // Whenever chaos ensues, choose a color. Add an amount of mana of that color
        // equal to your devotion to that color.
        getAbilities().add(new ChaosEnsuesTriggeredAbility(new NyxChaosEffect(), false));
    }

    private NyxPlane(final NyxPlane plane) {
        super(plane);
    }

    @Override
    public NyxPlane copy() {
        return new NyxPlane(this);
    }

    private static class NyxTypeEffect extends ContinuousEffectImpl {

        private NyxTypeEffect() {
            super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
            dependencyTypes.add(DependencyType.EnchantmentAddingRemoving);
            dependencyTypes.add(DependencyType.AuraAddingRemoving);
            staticText = "Nontoken creatures are enchantments in addition to their other types";
        }

        private NyxTypeEffect(final NyxTypeEffect effect) {
            super(effect);
        }

        @Override
        public NyxTypeEffect copy() {
            return new NyxTypeEffect(this);
        }

        @Override
        public boolean apply(Game game, Ability source) {
            for (Permanent permanent : game.getBattlefield().getActivePermanents(
                    FILTER_NONTOKEN_CREATURE, source.getControllerId(), source, game
            )) {
                permanent.addCardType(game, CardType.ENCHANTMENT);
            }
            return true;
        }
    }

    private static class NyxChaosEffect extends OneShotEffect {

        private NyxChaosEffect() {
            super(Outcome.PutManaInPool);
            staticText = "choose a color. Add an amount of mana of that color equal to your devotion to that color";
        }

        private NyxChaosEffect(final NyxChaosEffect effect) {
            super(effect);
        }

        @Override
        public NyxChaosEffect copy() {
            return new NyxChaosEffect(this);
        }

        @Override
        public boolean apply(Game game, Ability source) {
            Player controller = game.getPlayer(source.getControllerId());
            ChoiceColor choice = new ChoiceColor();
            if (controller == null || !controller.choose(outcome, choice, game)) {
                return false;
            }
            int devotion;
            switch (choice.getChoice()) {
                case "White": devotion = DevotionCount.W.calculate(game, source, this); break;
                case "Blue": devotion = DevotionCount.U.calculate(game, source, this); break;
                case "Black": devotion = DevotionCount.B.calculate(game, source, this); break;
                case "Red": devotion = DevotionCount.R.calculate(game, source, this); break;
                case "Green": devotion = DevotionCount.G.calculate(game, source, this); break;
                default: return false;
            }
            controller.getManaPool().addMana(choice.getMana(devotion), game, source);
            return true;
        }
    }
}
