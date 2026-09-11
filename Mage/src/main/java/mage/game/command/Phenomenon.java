package mage.game.command;

import mage.MageInt;
import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Abilities;
import mage.abilities.AbilitiesImpl;
import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.Effect;
import mage.cards.FrameStyle;
import mage.cards.repository.TokenInfo;
import mage.cards.repository.TokenRepository;
import mage.constants.CardType;
import mage.constants.Phenomena;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.game.events.ZoneChangeEvent;
import mage.util.SubTypes;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author spjspj
 */
public abstract class Phenomenon extends CommandObjectImpl implements PlanarCard {

    private static final ManaCosts emptyCost = new ManaCostsImpl<>();

    private String name;

    private UUID controllerId;
    private MageObject sourceObject;
    private boolean copy;
    private MageObject copyFrom; // copied card INFO (used to call original adjusters)
    private FrameStyle frameStyle;
    private Abilities<Ability> abilites = new AbilitiesImpl<>();
    private UUID planarDeckId;
    private UUID planarDeckOwnerId;
    private boolean faceUp;
    private int planarZoneChangeCounter;

    protected Phenomenon(String name) {
        super(name);
        this.name = name;
        this.frameStyle = FrameStyle.M15_NORMAL;
    }

    protected Phenomenon(final Phenomenon phenomenon) {
        super(phenomenon);
        this.name = phenomenon.name;
        this.frameStyle = phenomenon.frameStyle;
        this.controllerId = phenomenon.controllerId;
        this.sourceObject = phenomenon.sourceObject;
        this.copy = phenomenon.copy;
        this.copyFrom = (phenomenon.copyFrom != null ? phenomenon.copyFrom.copy() : null);
        this.abilites = phenomenon.abilites.copy();
        this.planarDeckId = phenomenon.planarDeckId;
        this.planarDeckOwnerId = phenomenon.planarDeckOwnerId;
        this.faceUp = phenomenon.faceUp;
        this.planarZoneChangeCounter = phenomenon.planarZoneChangeCounter;
    }

    @Override
    public FrameStyle getFrameStyle() {
        return frameStyle;
    }

    public void setSourceObjectAndInitImage() {
        this.sourceObject = null;

        // choose set code due source
        TokenInfo foundInfo = TokenRepository.instance.findPreferredTokenInfoForClass(this.getClass().getName(), null);
        if (foundInfo != null) {
            this.setExpansionSetCode(foundInfo.getSetCode());
            this.setUsesVariousArt(false);
            this.setCardNumber("");
            this.setImageFileName(""); // use default
            this.setImageNumber(foundInfo.getImageNumber());
        } else {
            // how-to fix: add phenomenon to the tokens database
            throw new IllegalArgumentException("Wrong code usage: can't find token info for the phenomenon: " + this.getClass().getName());
        }
    }

    @Override
    public MageObject getSourceObject() {
        return sourceObject;
    }

    @Override
    public UUID getSourceId() {
        if (sourceObject != null) {
            return sourceObject.getId();
        }
        return null;
    }

    @Override
    public UUID getControllerId() {
        return this.controllerId;
    }

    public void setControllerId(UUID controllerId) {
        this.controllerId = controllerId;
        this.abilites.setControllerId(controllerId);
    }

    @Override
    public UUID getControllerOrOwnerId() {
        return getControllerId();
    }

    @Override
    abstract public Phenomenon copy();

    @Override
    public void setCopy(boolean isCopy, MageObject copyFrom) {
        this.copy = isCopy;
        this.copyFrom = (copyFrom != null ? copyFrom.copy() : null);
    }

    @Override
    public boolean isCopy() {
        return this.copy;
    }

    @Override
    public MageObject getCopyFrom() {
        return this.copyFrom;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Phenomena have immutable names");
    }

    @Override
    public CardType getPlanarCardType() {
        return CardType.PHENOMENON;
    }

    @Override
    public UUID getPlanarDeckId() {
        return planarDeckId;
    }

    @Override
    public void setPlanarDeckId(UUID planarDeckId) {
        this.planarDeckId = planarDeckId;
    }

    @Override
    public UUID getPlanarDeckOwnerId() {
        return planarDeckOwnerId;
    }

    @Override
    public void setPlanarDeckOwnerId(UUID planarDeckOwnerId) {
        this.planarDeckOwnerId = planarDeckOwnerId;
    }

    @Override
    public boolean isFaceUp() {
        return faceUp;
    }

    @Override
    public void setFaceUp(boolean faceUp) {
        if (this.faceUp && !faceUp) {
            planarZoneChangeCounter++;
        }
        this.faceUp = faceUp;
    }

    @Override
    public List<CardType> getCardType(Game game) {
        return Collections.singletonList(CardType.PHENOMENON);
    }

    @Override
    public SubTypes getSubtype() {
        return new SubTypes();
    }

    @Override
    public SubTypes getSubtype(Game game) {
        return new SubTypes();
    }

    @Override
    public boolean hasSubtype(SubType subtype, Game game) {
        return false;
    }

    @Override
    public List<SuperType> getSuperType(Game game) {
        return Collections.emptyList();
    }

    @Override
    public Abilities<Ability> getAbilities() {
        return abilites;
    }

    @Override
    public boolean hasAbility(Ability ability, Game game) {
        return getAbilities().contains(ability);
    }

    @Override
    public ObjectColor getColor() {
        return ObjectColor.COLORLESS;
    }

    @Override
    public ObjectColor getColor(Game game) {
        return ObjectColor.COLORLESS;
    }

    @Override
    public ObjectColor getFrameColor(Game game) {
        return ObjectColor.COLORLESS;
    }

    @Override
    public ManaCosts<ManaCost> getManaCost() {
        return emptyCost;
    }

    @Override
    public void setManaCost(ManaCosts<ManaCost> costs) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public int getManaValue() {
        return 0;
    }

    @Override
    public MageInt getPower() {
        return MageInt.EmptyMageInt;
    }

    @Override
    public MageInt getToughness() {
        return MageInt.EmptyMageInt;
    }

    @Override
    public int getStartingLoyalty() {
        return 0;
    }

    @Override
    public void setStartingLoyalty(int startingLoyalty) {
    }

    @Override
    public int getStartingDefense() {
        return 0;
    }

    @Override
    public void setStartingDefense(int startingDefense) {
    }

    @Override
    public int getZoneChangeCounter(Game game) {
        return planarZoneChangeCounter;
    }

    @Override
    public void updateZoneChangeCounter(Game game, ZoneChangeEvent event) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public void setZoneChangeCounter(int value, Game game) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public boolean isAllCreatureTypes(Game game) {
        return false;
    }

    @Override
    public void setIsAllCreatureTypes(boolean value) {
    }

    @Override
    public void setIsAllCreatureTypes(Game game, boolean value) {
    }

    @Override
    public boolean isAllNonbasicLandTypes(Game game) {
        return false;
    }

    @Override
    public void setIsAllNonbasicLandTypes(boolean value) {
    }

    @Override
    public void setIsAllNonbasicLandTypes(Game game, boolean value) {
    }

    public void discardEffects() {
        for (Ability ability : abilites) {
            for (Effect effect : ability.getEffects()) {
                if (effect instanceof ContinuousEffect) {
                    ((ContinuousEffect) effect).discard();
                }
            }
        }
    }

    @Override
    public void removePTCDA() {
    }

    public static Phenomenon createPhenomenon(Phenomena phenomenonType) {
        if (phenomenonType == null) {
            return null;
        }
        String fullClassName = "mage.game.command.phenomena." + phenomenonType.getClassName();
        try {
            Class<?> phenomenonClass = Class.forName(fullClassName);
            Constructor<?> constructor = phenomenonClass.getConstructor();
            Object phenomenon = constructor.newInstance();
            return phenomenon instanceof Phenomenon ? (Phenomenon) phenomenon : null;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

}
