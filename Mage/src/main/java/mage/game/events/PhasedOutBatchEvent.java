package mage.game.events;

/**
 * Groups permanents that phase out simultaneously for "one or more" triggers.
 *
 * @author Susucr
 */
public class PhasedOutBatchEvent extends BatchEvent<GameEvent> {

    public PhasedOutBatchEvent(GameEvent firstEvent) {
        super(EventType.PHASED_OUT_BATCH, false, false, false, firstEvent);
    }
}
