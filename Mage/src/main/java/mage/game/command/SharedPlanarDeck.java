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
 * The ordered, shared planar deck. Planes in this structure are face down but
 * remain command-zone-associated objects; a face-up plane is temporarily absent
 * from the ordering.
 */
public final class SharedPlanarDeck implements Serializable, Copyable<SharedPlanarDeck> {

    private final LinkedList<Plane> planes = new LinkedList<>();

    public SharedPlanarDeck() {
    }

    private SharedPlanarDeck(final SharedPlanarDeck deck) {
        deck.planes.forEach(plane -> planes.add(plane.copy()));
    }

    @Override
    public SharedPlanarDeck copy() {
        return new SharedPlanarDeck(this);
    }

    public void setPlanes(Collection<? extends Plane> newPlanes, boolean shuffle) {
        planes.clear();
        newPlanes.forEach(plane -> planes.add(plane.copy()));
        if (shuffle) {
            Collections.shuffle(planes, RandomUtil.getRandom());
        }
    }

    public Plane draw() {
        return planes.pollFirst();
    }

    public void putOnBottom(Plane plane) {
        planes.addLast(plane);
    }

    public int size() {
        return planes.size();
    }

    public boolean isEmpty() {
        return planes.isEmpty();
    }

    /**
     * Returns only identity/order metadata, never the hidden plane objects.
     */
    public List<UUID> getOrder() {
        List<UUID> result = new ArrayList<>(planes.size());
        planes.forEach(plane -> result.add(plane.getId()));
        return Collections.unmodifiableList(result);
    }

    public void clear() {
        planes.clear();
    }
}
