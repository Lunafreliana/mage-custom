# Repository Instructions for Coding Agents

This repository contains custom XMage card implementations.

## Mandatory instructions for card-related work

For every task that implements, fixes, reviews, refactors, or tests Magic: The Gathering card behavior:

1. Read `docs/CARD_IMPLEMENTATION_LLM.md` in full before changing code.
2. Treat the requirements and workflow in that document as mandatory.
3. Do not implement a card from Oracle text alone when keywords, keyword actions, rules-defined terms, layers, face-down behavior, replacement effects, copy effects, linked abilities, zone changes, or other non-trivial rules are involved.
4. When rules behavior is uncertain or abbreviated by Oracle text:
   - check the current Magic Comprehensive Rules;
   - check the current Oracle text;
   - find and read the official Wizards Release Notes for the card's set when available;
   - search those Release Notes for both the card name and relevant mechanics;
   - check official card-specific rulings;
   - inspect modern XMage implementations of the same mechanic.
5. Prefer existing modern XMage engine functionality over custom reimplementations.
6. Do not assume an existing XMage implementation is correct merely because it already exists. Check whether it is current, legacy, deprecated, or marked with TODOs when relevant.
7. Add or update focused tests for behavioral changes.
8. Run the relevant focused tests and affected-module compilation whenever the environment permits.
9. If a bug or implementation task reveals a reusable lesson for future card development, update:
   - `docs/CARD_IMPLEMENTATION_LLM.md`
   - and, when useful for human developers, `docs/CARD_IMPLEMENTATION_HUMAN.md`.
10. Repository documentation must remain English-only.

## Documentation roles

`docs/CARD_IMPLEMENTATION_LLM.md`
is the authoritative implementation workflow and technical guidance for coding agents.

`docs/CARD_IMPLEMENTATION_HUMAN.md`
is explanatory documentation for human developers and should not replace the LLM implementation guide.

## Scope

These instructions apply to all card-related work anywhere in this repository.

Keep changes focused on the requested task and avoid unrelated modifications.
