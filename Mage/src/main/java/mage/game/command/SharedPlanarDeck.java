package mage.game.command;

import mage.util.Copyable;
import mage.util.RandomUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * The ordered, shared planar deck. Planar cards in this structure are face down
 * but remain command-zone-associated objects; a face-up planar card is
 * temporarily absent from the ordering.
 */
public final class SharedPlanarDeck implements Serializable, Copyable<SharedPlanarDeck> {

    private final UUID id;
    private final LinkedList<PlanarCard> cards = new LinkedList<>();

    public SharedPlanarDeck() {
        this.id = UUID.randomUUID();
    }

    private SharedPlanarDeck(final SharedPlanarDeck deck) {
        this.id = deck.id;
        deck.cards.forEach(card -> cards.add((PlanarCard) card.copy()));
    }

    @Override
    public SharedPlanarDeck copy() {
        return new SharedPlanarDeck(this);
    }

    public void setPlanes(Collection<? extends PlanarCard> newCards, boolean shuffle) {
        cards.clear();
        newCards.forEach(card -> {
            PlanarCard copy = (PlanarCard) card.copy();
            copy.setPlanarDeckId(id);
            copy.setFaceUp(false);
            cards.add(copy);
        });
        if (shuffle) {
            Collections.shuffle(cards, RandomUtil.getRandom());
        }
    }

    public PlanarCard draw() {
        return cards.pollFirst();
    }

    public void putOnBottom(PlanarCard card) {
        card.setPlanarDeckId(id);
        card.setFaceUp(false);
        cards.addLast(card);
    }

    public void putOnTop(PlanarCard card) {
        card.setPlanarDeckId(id);
        card.setFaceUp(false);
        cards.addFirst(card);
    }

    public UUID getId() {
        return id;
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns only identity/order metadata, never the hidden plane objects.
     */
    public List<UUID> getOrder() {
        List<UUID> result = new ArrayList<>(cards.size());
        cards.forEach(card -> result.add(card.getId()));
        return Collections.unmodifiableList(result);
    }

    /**
     * Internal identity lookup for effects and views that retain a planar card's
     * source id after that card is put back into the planar deck.
     */
    public PlanarCard findById(UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return cards.stream()
                .filter(card -> cardId.equals(card.getId()))
                .findFirst()
                .orElse(null);
    }

    public void clear() {
        cards.clear();
    }
}
