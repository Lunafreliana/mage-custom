$ErrorActionPreference = 'Stop'

$gameImplPath = 'Mage/src/main/java/mage/game/GameImpl.java'
$gameImpl = (Get-Content -Raw $gameImplPath).Replace("`r`n", "`n")
$oldLeave = @'
        logger.debug("Start leave game: " + player.getName());
        if (state.isPlaneChase() && playerId.equals(getPlanarControllerId(null))) {
            UUID successorId = getActivePlayerId();
            if (playerId.equals(successorId)) {
                Player successor = state.getPlayerList(playerId).getNext(this, false);
                successorId = successor == null ? null : successor.getId();
            }
            setPlanarControllerId(successorId);
        }
        player.leave();
        if (checkIfGameIsOver()) {
'@
$newLeave = @'
        logger.debug("Start leave game: " + player.getName());
        boolean revealReplacementPlane = false;
        UUID replacementPlanarPlayerId = null;
        if (state.isPlaneChase()) {
            if (playerId.equals(getPlanarControllerId(null))) {
                UUID successorId = getActivePlayerId();
                if (playerId.equals(successorId)) {
                    Player successor = state.getPlayerList(playerId).getNext(this, false);
                    successorId = successor == null ? null : successor.getId();
                }
                setPlanarControllerId(successorId);
            }
            replacementPlanarPlayerId = getPlanarControllerId(null);
            if (state.getPlanarDeckMode() == PlanarDeckMode.INDIVIDUAL) {
                for (PlanarCard planarCard : new ArrayList<>(state.getFaceUpPlanarCards())) {
                    if (!playerId.equals(planarCard.getPlanarDeckOwnerId())) {
                        continue;
                    }
                    state.getCommand().remove(planarCard);
                    // The new planar controller takes control before the owner's Plane leaves,
                    // so planeswalk-away abilities are controlled by the surviving player.
                    fireEvent(new GameEvent(GameEvent.EventType.PLANESWALKED_AWAY,
                            planarCard.getId(), (Ability) null, replacementPlanarPlayerId, 0, true));
                    state.removeTriggersOfSourceId(planarCard.getId());
                    revealReplacementPlane = true;
                }
                // Rule 800.4a also removes the rest of that player's planar deck.
                state.removePlayerPlanarDeck(playerId);
            }
        }
        player.leave();
        if (revealReplacementPlane && replacementPlanarPlayerId != null) {
            turnTopPlanarCardFaceUp(replacementPlanarPlayerId);
        }
        if (checkIfGameIsOver()) {
'@
if (-not $gameImpl.Contains($oldLeave)) {
    throw 'GameImpl leave block not found'
}
$gameImpl = $gameImpl.Replace($oldLeave, $newLeave)
[System.IO.File]::WriteAllText($gameImplPath, $gameImpl, [System.Text.UTF8Encoding]::new($false))

$gameStatePath = 'Mage/src/main/java/mage/game/GameState.java'
$gameState = (Get-Content -Raw $gameStatePath).Replace("`r`n", "`n")
$oldDeckMethods = @'
    public Map<UUID, SharedPlanarDeck> getPlayerPlanarDecks() {
        return Collections.unmodifiableMap(playerPlanarDecks);
    }

    public void setPlayerPlanarDeck(UUID playerId, Collection<? extends PlanarCard> cards, boolean shuffle) {
'@
$newDeckMethods = @'
    public Map<UUID, SharedPlanarDeck> getPlayerPlanarDecks() {
        return Collections.unmodifiableMap(playerPlanarDecks);
    }

    public void removePlayerPlanarDeck(UUID playerId) {
        playerPlanarDecks.remove(playerId);
    }

    public void setPlayerPlanarDeck(UUID playerId, Collection<? extends PlanarCard> cards, boolean shuffle) {
'@
if (-not $gameState.Contains($oldDeckMethods)) {
    throw 'GameState planar deck block not found'
}
$gameState = $gameState.Replace($oldDeckMethods, $newDeckMethods)
[System.IO.File]::WriteAllText($gameStatePath, $gameState, [System.Text.UTF8Encoding]::new($false))

git diff --check
if ($LASTEXITCODE -ne 0) {
    throw 'git diff --check failed'
}

git diff -- Mage/src/main/java/mage/game/GameImpl.java Mage/src/main/java/mage/game/GameState.java
