# LLM instructions: implementing original custom cards in CLUN

This guide supplements [CARD_IMPLEMENTATION_LLM.md](CARD_IMPLEMENTATION_LLM.md), which remains mandatory. `CLUN` is a technical XMage set for original, unofficial cards without an official printing; it is **not** a claim of an official Wizards set or release. Read the repository's `AGENTS.md` and the main implementation guide first.

## Set identity and first-card workflow

- The set skeleton is [`Mage.Sets/src/mage/sets/CLUN.java`](../Mage.Sets/src/mage/sets/CLUN.java). Its displayed name and set code are both `CLUN`; it uses `SetType.CUSTOM_SET`, disables boosters and basic lands, and intentionally has **no card entries yet**. The date in its constructor is only a technical registration date. An empty skeleton does not create playable cards; whether it appears in a client set list depends on how that view filters empty sets.
- For each original card, first obtain the creator's exact name, mana cost, type line, rules text, power/toughness or loyalty, intended color identity, and any custom-mechanic definitions. Do not invent missing card text, an official Oracle entry, official rulings, Scryfall IDs, or Release Notes for cards that have none. Research the current Comprehensive Rules and analogous modern XMage behavior for existing Magic mechanics; document any original house-rule decisions explicitly.
- Implement the card in `Mage.Sets/src/mage/cards/<first-letter>/<JavaClassName>.java` using the existing `CardImpl`/`CardSetInfo` constructor and copy pattern described in the main guide. Check for a class/name collision first. Do not create a placeholder card just to make the set nonempty.
- Register every intended CLUN printing in the `CLUN` constructor using `cards.add(new SetCardInfo(...))`. Add `import mage.constants.Rarity;` when adding entries. Example for a **hypothetical** card, only after implementing its real class:

```java
cards.add(new SetCardInfo("Example Custom Card", 1, Rarity.RARE,
        mage.cards.e.ExampleCustomCard.class));
```

- Treat the collector number as an internal printing identifier, not a claim of a physical card number. Choose an unused, valid positive number for each printing (`1`, `2`, `3`, ...); keep `(CLUN, collector number)` stable once decklists or images depend on it. The name must match the actual card. Do not register a nonexistent class, duplicate a printing identifier, or reuse a number for a different card.
- `CardSetInfo` is supplied by this set registration to the card constructor. Do not remove set metadata or fabricate an official expansion to work around a missing printing. New sets follow the singleton `ExpansionSet` pattern already used by `CustomAlchemy` and `StarWars`; confirm current repository discovery behavior before changing any scanner or registry.

## Artwork and visibility

- CLUN artwork has no automatic official Scryfall/Gatherer source. Provide artwork through the existing client image-download/custom-image mechanism and validate the actual lookup key and missing-image path in the running client. Do not invent an image URL or assume that registering a set automatically supplies images; do not overwrite shared `Face Down` artwork to represent a custom card.
- A card is usable only after its implemented class and `SetCardInfo` are available in the built server/client card data. Adding the empty `CLUN` set alone does not make a new card searchable or playable.

## Tests and delivery

- Add focused tests under `Mage.Tests/src/test/java/org/mage/test/cards` for each new card's characteristics, choices, rules interactions, and copy/zone behavior as appropriate. Check that the card is discoverable from its `CLUN` registration and that the intended art lookup works when artwork is part of the task. The set skeleton has a metadata/empty-list test in `ClunSetTest`.
- Keep changes scoped to CLUN and the relevant cards. Follow `AGENTS.md`: GitHub Actions is the authoritative build-and-test environment; do not download a new Java/Maven toolchain just to run local validation. Report the actual CI result rather than claiming unrun tests passed.
