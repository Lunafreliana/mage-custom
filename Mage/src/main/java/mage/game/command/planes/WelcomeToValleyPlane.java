package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.constants.CardType;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.FilterSpell;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.command.Plane;
import mage.target.TargetPermanent;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author The XMage Developers
 */
public final class WelcomeToValleyPlane extends Plane {

    private static final Set<SubType> ANIMAL_TYPES = EnumSet.of(
            SubType.ANTELOPE, SubType.APE, SubType.ARMADILLO, SubType.AUROCHS,
            SubType.BADGER, SubType.BAT, SubType.BEAR, SubType.BEAVER, SubType.BIRD,
            SubType.BISON, SubType.BOAR, SubType.CAMEL, SubType.CAPYBARA, SubType.CARIBOU,
            SubType.CAT, SubType.COW, SubType.COYOTE, SubType.CRAB, SubType.CROCODILE, SubType.DINOSAUR,
            SubType.DOG, SubType.ECHIDNA, SubType.ELEPHANT, SubType.ELK, SubType.FERRET,
            SubType.FISH, SubType.FOX, SubType.FROG, SubType.GIRAFFE, SubType.GOAT,
            SubType.HAMSTER, SubType.HEDGEHOG, SubType.HIPPO, SubType.HORSE, SubType.HYENA,
            SubType.INSECT, SubType.JACKAL, SubType.JELLYFISH, SubType.KANGAROO, SubType.LEECH,
            SubType.LEMUR, SubType.LIZARD, SubType.LLAMA, SubType.LOBSTER, SubType.MITE,
            SubType.MOLE, SubType.MONGOOSE, SubType.MONKEY, SubType.MOUSE, SubType.NAUTILUS,
            SubType.OCTOPUS, SubType.OTTER, SubType.OX, SubType.OYSTER, SubType.PANGOLIN,
            SubType.PLATYPUS, SubType.PORCUPINE, SubType.POSSUM, SubType.RABBIT, SubType.RACCOON,
            SubType.RAT, SubType.RHINO, SubType.SABLE, SubType.SALAMANDER, SubType.SCORPION,
            SubType.SEAL, SubType.SHARK, SubType.SHEEP, SubType.SKUNK, SubType.SLOTH,
            SubType.SLUG, SubType.SNAIL, SubType.SNAKE,
            SubType.SPIDER, SubType.SPONGE, SubType.SQUID, SubType.SQUIRREL, SubType.STARFISH,
            SubType.TRILOBITE, SubType.TURTLE, SubType.VARMINT, SubType.WALRUS, SubType.WEASEL,
            SubType.WHALE, SubType.WOLF,
            SubType.WOLVERINE, SubType.WOMBAT, SubType.WORM
    );

    private static final FilterSpell FILTER_ANIMAL_SPELL = new FilterSpell("an animal spell");
    private static final FilterControlledCreaturePermanent FILTER_CONTROLLED_CREATURE
            = new FilterControlledCreaturePermanent("creature you control");

    static {
        FILTER_ANIMAL_SPELL.add((spell, game) -> spell.isCreature(game)
                && ANIMAL_TYPES.stream().anyMatch(type -> spell.hasSubtype(type, game)));
    }

    public WelcomeToValleyPlane() {
        this.setPlaneType(Planes.PLANE_WELCOME_TO_VALLEY);

        // Whenever you cast an animal spell, draw a card. (Animals are any non-human creature
        // types that are animals in real life.)
        this.getAbilities().add(new SpellCastControllerTriggeredAbility(
                Zone.COMMAND, new DrawCardSourceControllerEffect(1), FILTER_ANIMAL_SPELL,
                false, SetTargetPointer.NONE
        ));

        // Whenever chaos ensues, create a token that's a copy of target creature you control,
        // except it's a 1/1.
        CreateTokenCopyTargetEffect copyEffect = new CreateTokenCopyTargetEffect().setPower(1).setToughness(1);
        copyEffect.setText("create a token that's a copy of target creature you control, except it's a 1/1");
        Ability ability = new ChaosEnsuesTriggeredAbility(copyEffect, false);
        ability.addTarget(new TargetPermanent(FILTER_CONTROLLED_CREATURE));
        this.getAbilities().add(ability);
    }

    private WelcomeToValleyPlane(final WelcomeToValleyPlane plane) {
        super(plane);
    }

    @Override
    public WelcomeToValleyPlane copy() {
        return new WelcomeToValleyPlane(this);
    }
}
