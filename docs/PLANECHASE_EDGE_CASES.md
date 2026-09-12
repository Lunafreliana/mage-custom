# Planechase Edge Cases

This file is part of the Phase 10 implementation contract for `PLANECHASE_REWORK.md`. It captures rules interactions that are easy to model incorrectly when individual planar decks, stack interaction, Phenomena, and multiplayer departure are combined.

## Stack and countering

- `Chaos ensues` is a rules event, not a stack object. Applicable Plane chaos abilities trigger from that event and use the normal stack. Countering one of those triggered abilities prevents only that ability's effect; it does not undo chaos having ensued.
- Rolling the planeswalker symbol creates the inherent source-less planeswalking triggered ability controlled by the roller. That triggered ability goes on the stack before planeswalking happens and may be countered by effects that counter triggered abilities.
- Countering the inherent planeswalking trigger prevents that planeswalk, but does not undo the planar die roll or the voluntary-roll action count/cost progression.
- If a resolving spell or ability directly instructs a player to planeswalk, perform that planeswalk during resolution. Do not create an additional inherent planeswalking trigger unless the rules specifically call for one.

## Phenomena

- Encounter abilities are normal triggered abilities and can be countered.
- Countering or otherwise removing the encounter trigger does not leave the game on the Phenomenon. Once the relevant Phenomenon-sourced trigger is no longer pending/on the stack, the Planechase state-based action makes the planar controller planeswalk before priority is given.
- For that state-based-action planeswalk, the planar controller is the planeswalking player. In individual mode, the next planar card therefore comes from that player's own planar deck.

## Player departure

- If the current planar controller leaves the game, the next appropriate remaining player becomes planar controller before departure-dependent planar processing continues.
- In individual mode, planar cards owned by a departing player leave the game with that player. If this removes a face-up Plane or Phenomenon, the planar controller immediately turns up the top card of their own planar deck. This replacement reveal is not a state-based action.
- If a Plane leaves the game while the inherent planeswalking triggered ability is on the stack, that ability ceases to exist and must not later cause a second planeswalk.
- Do not remove unrelated Plane abilities from the stack merely because their source Plane left; normal multiplayer stack/controller rules still apply unless Planechase provides a specific exception.
- Phenomenon abilities whose owner left the game follow the Planechase-specific controller-transfer rule and remain on the stack under the new planar controller where required by the Comprehensive Rules.
- Shared mode has different ownership semantics from individual mode. Do not reuse individual owner-departure logic blindly for the single communal planar deck option.

## Required Phase 10 regression coverage

At minimum, tests must prove:

- a chaos trigger can be countered without undoing chaos;
- multiple applicable face-up Planes produce all applicable chaos triggers;
- object-specific chaos affects only the specified planar object;
- a planeswalker result puts the inherent planeswalking trigger on the stack before planeswalking;
- countering that trigger prevents the planeswalk while preserving roll/action-use bookkeeping;
- direct planeswalk instructions do not manufacture the inherent planar-die trigger;
- a countered Phenomenon encounter trigger still leads to the required state-based-action planeswalk;
- walked-away planar cards return to their owners' planar decks while the planeswalking player reveals from their own deck;
- planar-controller transfer happens before departure-dependent planar processing;
- owner departure removes that player's face-up planar object and immediately reveals from the planar controller's deck;
- a pending inherent planeswalking trigger ceases if a Plane leaves;
- the Phenomenon stack-control exception survives owner departure;
- shared-mode departure follows shared ownership semantics rather than individual-deck ownership semantics.
