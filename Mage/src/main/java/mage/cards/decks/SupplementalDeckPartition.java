package mage.cards.decks;

import mage.cards.Card;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Separates pregame supplemental cards before format-specific validation. */
public final class SupplementalDeckPartition {

    private final List<Card> ordinaryPregame = new ArrayList<>();
    private final Map<SupplementalDeckType, List<SupplementalDeckCard>> supplemental
            = new EnumMap<>(SupplementalDeckType.class);
    private final List<SupplementalDeckCard> illegalMain = new ArrayList<>();

    public static SupplementalDeckPartition create(Collection<Card> main, Collection<Card> pregame) {
        SupplementalDeckPartition result = new SupplementalDeckPartition();
        main.stream()
                .filter(SupplementalDeckCard.class::isInstance)
                .map(SupplementalDeckCard.class::cast)
                .forEach(result.illegalMain::add);
        for (Card card : pregame) {
            if (card instanceof SupplementalDeckCard) {
                SupplementalDeckCard supplementalCard = (SupplementalDeckCard) card;
                result.supplemental
                        .computeIfAbsent(supplementalCard.getSupplementalDeckType(), key -> new ArrayList<>())
                        .add(supplementalCard);
            } else {
                result.ordinaryPregame.add(card);
            }
        }
        return result;
    }

    public List<Card> getOrdinaryPregame() {
        return Collections.unmodifiableList(ordinaryPregame);
    }

    public List<SupplementalDeckCard> get(SupplementalDeckType type) {
        return Collections.unmodifiableList(supplemental.getOrDefault(type, Collections.emptyList()));
    }

    public List<SupplementalDeckCard> getIllegalMain() {
        return Collections.unmodifiableList(illegalMain);
    }

    public boolean hasIllegalMainPlacement() {
        return !illegalMain.isEmpty();
    }
}
