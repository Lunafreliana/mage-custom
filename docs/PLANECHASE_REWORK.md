Ja. Ich habe **#11316 jetzt wirklich als Architekturentwurf auseinandergenommen**, nicht nur oberflächlich angeschaut. Das wichtigste Ergebnis zuerst:

**Ich würde PR #11316 auf keinen Fall cherry-picken. Ich würde seine Grundideen übernehmen und die Implementierung neu aufsetzen.** Der PR ist seit 2023 ein offener Draft, besteht nur aus zwei Commits, verändert 35 Dateien und wurde vom Autor ausdrücklich als nie fertiggestellter Versuch eingestellt.

Und ich habe beim Nachschlagen der **aktuellen Comprehensive Rules von Juni 2026** noch eine wichtige Präzisierung gegenüber meiner vorigen Antwort gefunden: Wir sollten **keine neue `Zone.PLANAR_DECK` einführen**. Plane- und Phenomenon-Karten bleiben regeltechnisch **immer in der Command Zone**, auch solange sie Teil des verdeckten Planar Decks sind. Das „Planar Deck“ ist also eine zusätzliche **Deckordnung innerhalb der Command Zone**, keine eigene Zone. 

Das macht die Sache für XMage interessanter – und eigentlich etwas eleganter.

## Was #11316 richtig erkannt hat

Der PR versucht im Kern, das hier:

```text
Plane
 ├─ eigentliche Plane-Fähigkeit
 ├─ "Roll planar die"-ActivatedAbility
 ├─ CHAOS-Effekt
 ├─ PlanarRollWatcher
 └─ CostIncreasingEffect
```

in das hier zu verwandeln:

```text
Planechase-Regeln
 └─ RollPlanarDieSpecialAction
       ↓
     planar die
       ↓
 ┌─────┼──────────┐
blank chaos   planeswalker
       │
       ▼
 CHAOS_ENSUES
       │
       ▼
Plane-Chaos-Trigger
```

**Diese Richtung ist absolut richtig.**

`SpecialAction` ist in XMage bereits dafür gemacht, nicht den Stack zu benutzen; die Basisklasse setzt ausdrücklich `usesStack = false`.  Damit passt sie viel besser zu Regel 901.9 als die heutige Activated Ability auf jeder Plane.

Auch die andere große Idee ist richtig: Der PR führt `ChaosEnsuesTriggeredAbility` ein. Statt dass jede Plane ihren Chaos-Effekt in `RollPlanarDieEffect` hineinreicht, wird aus:

```java
new RollPlanarDieEffect(chaosEffects, chaosTargets)
```

auf der Plane schlicht:

```java
new ChaosEnsuesTriggeredAbility(
    new DiscardHandControllerEffect()
)
```

Das sieht man beispielsweise bei Academy at Tolaria West. Genau so sollten moderne Plane-Implementierungen ungefähr aussehen. Der aktuelle Oracle-Mechanismus ist tatsächlich „Whenever chaos ensues“, und Chaos kann inzwischen ausdrücklich auch durch Zaubersprüche und Fähigkeiten entstehen, nicht nur durch den Würfel. ([MAGIC: THE GATHERING][1])

Auch der Versuch, Panopticons selbstgeschriebene Spezialklasse

```java
PanopticonTriggeredAbility
```

durch eine generische

```java
PlaneswalkToSourceTriggeredAbility
```

zu ersetzen, ist konzeptionell goldrichtig. Damit würden später 100 Plane-Klassen nicht jeweils dieselben Event-Prüfungen neu implementieren.

## Aber #11316 ist als laufender Code ziemlich kaputt

Hier sind die wichtigsten Punkte meiner Sezierung:

| Teil von #11316                      | Bewertung                             | Warum                                                                        |
| ------------------------------------ | ------------------------------------- | ---------------------------------------------------------------------------- |
| `RollPlanarDieSpecialAction`         | **Idee behalten, Code neu schreiben** | richtiger Mechanismus, aber Timing und Ergebnisverarbeitung fehlerhaft       |
| `ChaosEnsuesTriggeredAbility`        | **behalten**                          | grundsätzlich richtige Abstraktion                                           |
| `PlaneswalkToSourceTriggeredAbility` | **Idee behalten**                     | im PR komplett unfertig                                                      |
| `ROLLED_PLANESWALK` Event            | **Idee ändern**                       | es fehlt die eigentliche inherent triggered ability                          |
| `Player.rollPlanarDieResult()`       | **teilweise behalten**                | Rohwurf ist gut, Planechase-Regeln gehören aber nicht ins `Player`-Interface |
| `getActivatedThisTurnCount()`        | **Konzept behalten**                  | richtige Lösung für die Rollkosten                                           |
| Migration der 21 Planes              | **später wiederverwenden**            | erst Engine korrekt machen                                                   |
| `Plane.addAbility()`                 | **harmlos**                           | reine Convenience                                                            |
| Planar Deck                          | **fehlt komplett**                    | PR löst das Kernproblem nicht                                                |
| Phenomena                            | **fehlen komplett**                   | keine Encounter-/SBA-Engine                                                  |
| planar controller                    | **nicht gelöst**                      | weiterhin strukturell falsch                                                 |

Der erste konkrete Bug ist ziemlich heftig: `SpecialAction` erbt von `ActivatedAbilityImpl`, und dort ist das Standard-Timing:

```java
protected TimingRule timing = TimingRule.INSTANT;
```

Der neue `RollPlanarDieSpecialAction` setzt dieses Timing **nicht** auf Sorcery. Die Comprehensive Rules sagen dagegen ausdrücklich: aktiver Spieler, Priority, Main Phase, Stack leer. ([Wizards Media][2])

Der PR würde die schöne neue Special Action also ausgerechnet mit dem falschen Timing einführen.

## Noch schlimmer: Der neue Würfel-Flow ist nicht fertig verbunden

Der PR teilt sinnvollerweise das reine Würfeln in:

```java
rollPlanarDieResult(...)
```

und eine Methode auf, die das Ergebnis verarbeitet.

Aber `RollPlanarDieSpecialActionEffect` ruft anschließend ausgerechnet nur:

```java
player.rollPlanarDieResult(...)
```

auf und verwirft das Resultat.

Damit bekommt man zwar einen Würfelwurf, aber die neue Special Action löst anschließend weder ordentlich Chaos noch Planeswalking aus.

Dazu kommt ein bestätigter Fehler im `Player`-Patch: Dort wurden `CHAOS_ROLL` und Planeswalker-Ergebnis beim Feuern der neuen Events vertauscht. Ein Reviewer hat genau das markiert; der Autor antwortete, das sei vermutlich durch einen Merge-Konflikt entstanden, weil der Code schon alt gewesen sei.

Das zeigt ziemlich deutlich, in welchem Zustand der PR ist: **Design-Skizze, nicht Implementierung zum Übernehmen.**

## `ROLLED_PLANESWALK` löst außerdem gar kein Planeswalking aus

Das ist der nächste zentrale Punkt.

#11316 fügt hinzu:

```java
GameEvent.EventType.ROLLED_PLANESWALK
GameEvent.EventType.CHAOS_ENSUES
```

und `WillOfThePlaneswalkersEffect` feuert bei entsprechendem Abstimmungsergebnis `ROLLED_PLANESWALK`.

Aber im gesamten PR gibt es keine fertiggestellte Ability, die daraus korrekt **Planeswalk** macht.

Das ist besonders wichtig, weil die echten Regeln etwas subtiler sind als „PW würfeln → sofort nächste Plane“.

Aktuell, 2026, gilt:

```text
Planar die zeigt Planeswalker
             ↓
"inherent planeswalking ability" triggert
             ↓
Ability geht auf den Stack
             ↓
Spieler können reagieren
             ↓
bei Resolution:
planeswalk
```

Regel 901.8 definiert diese source-less inherent triggered ability ausdrücklich, und 901.9c sagt ausdrücklich, dass sie auf den Stack geht. 

Die heutige XMage-Implementierung ist also auch hier nicht ganz richtig, weil `RollPlanarDieEffect` direkt:

```java
new PlaneswalkEffect(false).apply(...)
```

ausführt.

#11316 hatte offenbar erkannt, dass ein Event dazwischengehört – hat aber die zweite Hälfte nicht mehr gebaut.

## Dafür ist seine neue Kostenidee überraschend wichtig

Hier hat der alte Code tatsächlich einen subtilen Regelfehler, und #11316 läuft in die richtige Richtung.

Heute verwendet XMage `PlanarRollWatcher`. Der zählt:

```text
wie oft hat dieser Spieler diesen Zug
den planar die gewürfelt?
```

Aber **das ist nicht das, was die Rollkosten zählen sollen**.

Die aktuelle Regel 901.9 sagt ausdrücklich, dass die Kosten davon abhängen, wie oft der Spieler **diese spezielle Spielaktion** bereits genommen hat. Würfelt ein Karteneffekt zusätzlich den Weltenwürfel, erhöht das die Kosten des nächsten freiwilligen Wurfs **nicht**. ([Wizards Media][2])

Also beispielsweise:

```text
erster freiwilliger Roll    → {0}
Fractured Powerstone rollt  → kein Einfluss
zweiter freiwilliger Roll   → {1}
dritter freiwilliger Roll   → {2}
```

Der alte `PlanarRollWatcher` kann das nicht korrekt unterscheiden.

#11316 benutzt stattdessen die **Activation Count der Special Action**.

Das ist konzeptionell exakt richtig.

Ich würde dafür allerdings nicht unbedingt `getActivatedThisTurnCount()` öffentlich in die allgemeine `ActivatedAbilityImpl` einbauen. Der `RollPlanarDieSpecialAction`-Subclass kann seine eigene ActivationInfo verwenden. Dadurch verändern wir weniger Engine-Core.

## Das Controller-Problem wird durch #11316 sogar sichtbarer

Nehmen wir Panopticon.

Heute kompensiert die Klasse das schlechte Controller-Modell teilweise manuell. #11316 vereinfacht das zu:

```java
new PlaneswalkToSourceTriggeredAbility(
    new DrawCardSourceControllerEffect(1)
)
```

Das wäre wunderschön — **wenn die Plane korrekt vom planar controller kontrolliert würde.**

Tut sie aber nicht.

Das aktuelle `Plane`-Objekt speichert einfach:

```java
private UUID controllerId;
```

und dieser Controller wird beim Erzeugen gesetzt.

Die echten Regeln sagen dagegen:

> Normalerweise ist der aktive Spieler der planar controller.

Und bei der Single-Planar-Deck-Option gilt dieser planar controller sogar als Besitzer aller Karten dieses gemeinsamen Planar Decks. 

Damit würden viele hübsch vereinfachte `SourceControllerEffect`s aus #11316 weiterhin den falschen Spieler treffen.

**Deshalb muss planar-controller handling vor der Massenmigration der Planes erledigt werden.**

---

# Meine Zielarchitektur

Ich würde nicht alles in `Player`, `GameImpl` und 100 Plane-Klassen verteilen. Stattdessen sollte ein kleiner Planechase-Kern existieren:

```text
GameState
   │
   └── PlanechaseState
          │
          ├── PlanarDeck
          │      └── geordnete PlanarCard-IDs
          │
          ├── faceUpPlanarCards
          │
          ├── planarControllerId
          │
          └── mode: SHARED / INDIVIDUAL

PlanarCard
   ├── Plane
   └── Phenomenon

Planechase Rules
   ├── RollPlanarDieSpecialAction
   ├── PlanarDieRollResolver
   ├── ChaosEnsuesEffect
   ├── ChaosEnsuesTriggeredAbility
   ├── PlanarDiePlaneswalkTriggeredAbility
   ├── PlaneswalkEffect
   └── EncounterPhenomenonAbility
```

Und dabei würde ich **vorerst `PlanarCard` weiter auf der CommandObject-Seite von XMage aufbauen**, statt sofort alle Planes zu `CardImpl` umzubauen.

Das minimiert den Eingriff in Mage.Sets, Card Repository, Serialization, Views usw.

Aber `PlanarCard` muss Dinge bekommen, die `Plane` heute fehlen:

```java
CardType getPlanarCardType(); // PLANE / PHENOMENON

UUID ownerId;
UUID controllerId;

boolean faceUp;
boolean revealed;
```

sowie eine echte Identität im Planar Deck.

Das jetzige:

```java
getCardType() -> Collections.emptyList()
```

muss mittelfristig verschwinden.

## Und das „Deck“ wird keine Zone

Das ist die wichtigste Änderung zu meiner gestrigen Skizze.

Nach den aktuellen Regeln:

```text
COMMAND ZONE
│
├─ verdeckte PlanarCard
├─ verdeckte PlanarCard
├─ verdeckte PlanarCard
├─ verdeckte PlanarCard
│
└─ FACE-UP Plane
```

Der `PlanarDeck` sagt lediglich:

```text
top
 ↓
[Agyrem]
[Tazeem]
[Phenomenon]
[Jund]
[...]
 ↑
bottom
```

Alle diese Karten sind regeltechnisch weiterhin in `Zone.COMMAND`. 

Das heißt, `PlanarDeck` sollte eher etwas wie:

```java
class PlanarDeck {
    Deque<UUID> cardOrder;
}
```

sein und **keine Zone**.

Das ist langfristig außerdem ein sehr brauchbares Modell für weitere supplementary decks, ohne die Magic-Zonen künstlich aufzublähen.

## Der Würfel-Flow sollte exakt getrennt werden

Ich würde `Player` ausschließlich würfeln lassen:

```java
PlanarDieRollResult result =
    player.rollPlanarDieResult(...);
```

Danach übernimmt Planechase:

```java
planechase.resolvePlanarDieRoll(
    playerId,
    result,
    source,
    game
);
```

Dann:

```text
BLANK
  ↓
nichts


CHAOS
  ↓
ChaosEnsuesEffect
  ↓
CHAOS_ENSUES Event
  ↓
ChaosEnsuesTriggeredAbility der passenden Plane
  ↓
Stack


PLANESWALKER
  ↓
PLANAR_DIE_PLANESWALK Event
  ↓
inherent PlaneswalkingTriggeredAbility
  ↓
Stack
  ↓
PlaneswalkEffect bei Resolution
```

Damit sind **Würfeln** und **Planechase-Regeln** endlich sauber getrennt.

Und Karten können weiterhin einfach sagen:

```java
new RollPlanarDieEffect()
```

ohne dadurch den Kosten-Counter der Special Action zu erhöhen.

## `ChaosEnsuesEffect` sollten wir zusätzlich zu #11316 bauen

#11316 hat nur den Trigger:

```java
ChaosEnsuesTriggeredAbility
```

Der Kommentar im PR deutet selbst schon darauf hin, dass analog zu `PlaneswalkEffect` ein eigener Chaos-Effekt sinnvoll wäre. Ein Maintainer nannte dabei ausdrücklich Karten wie Missy.

Also:

```java
new ChaosEnsuesEffect()
```

soll:

```java
game.fireEvent(
    CHAOS_ENSUES
);
```

auslösen.

Dann kann eine Karte wie:

```text
"... draw a card and chaos ensues."
```

einfach schreiben:

```java
ability.addEffect(new DrawCardSourceControllerEffect(1));
ability.addEffect(new ChaosEnsuesEffect());
```

Das wäre genau die Art wiederverwendbarer Primitive, die wir für WHO/MOC brauchen.

Ich würde den Effekt sogar schon so designen, dass später optional eine **bestimmte Plane** adressiert werden kann. Die aktuellen Regeln kennen nämlich auch „chaos ensues for a particular object“; in diesem Fall kann die Chaos-Ability sogar einer aufgedeckten Plane im Planar Deck triggern. 

## `PlaneswalkToSourceTriggeredAbility` bauen wir fertig

Das Grundprinzip ist simpel. Wir haben bereits `PLANESWALK` und `PLANESWALKED`. `GameImpl.addPlane()` feuert heute `PLANESWALKED`, nachdem das neue Plane-Objekt hinzugefügt wurde.

Der generische Trigger kann daher grob prüfen:

```java
event.getType() == PLANESWALKED
&& event.getTargetId().equals(source.getSourceId())
```

und muss dann unter dem korrekten planar controller triggern.

#11316 legt die Klasse zwar an, aber beide Kernmethoden enden dort faktisch mit:

```java
// TODO: implement
return false;
```

Damit funktioniert beispielsweise die in diesem PR umgebaute Panopticon-Ability gar nicht.

Die Klasse ist also ein guter **Namens- und API-Vorschlag**, nicht mehr.

## Wir dürfen nicht mehr von „der einen aktuellen Plane“ ausgehen

Das ist ein weiterer Punkt, den ich gegenüber meiner ersten Analyse verschärfen würde.

XMage hat überall:

```java
game.getState().getCurrentPlane()
```

und `GameImpl.addPlane()` verhindert sogar eine zweite Plane.

Für vollständiges Planechase reicht das nicht.

Die aktuellen Regeln kennen ausdrücklich Situationen mit **mehreren gleichzeitig offenen Plane-Karten**; außerdem brauchen Phenomena Mechanismen, die solche Situationen erzeugen können. Regel 901.11c definiert sogar ausdrücklich, was beim Planeswalken passiert, wenn mehrere Planes offen liegen. 

Deshalb muss langfristig:

```java
Plane getCurrentPlane()
```

ersetzt werden durch etwas wie:

```java
Collection<Plane> getFaceUpPlanes()
Collection<PlanarCard> getFaceUpPlanarCards()
```

`getCurrentPlane()` können wir während der Migration als Legacy-Helfer behalten:

```java
@Deprecated
Plane getCurrentPlane()
```

aber neuer Code sollte ihn nicht mehr benutzen.

Das erspart uns später einen zweiten großen Umbau.

# Phenomena verlangen eine richtige Engine

Das ist der Punkt, an dem das heutige Zufallsmodell endgültig auseinanderfällt.

Die aktuellen Regeln sagen:

```text
Top card des Planar Decks wird face-up
            ↓
ist es eine Plane?
      → normale Plane

ist es ein Phenomenon?
      ↓
"When you encounter ..." triggert
      ↓
Ability geht auf Stack
      ↓
nachdem die Phenomenon-Ability den Stack verlassen hat
      ↓
State-Based Action
      ↓
planar controller planeswalkt weiter
```

Regel 312.5 definiert „encounter“, und 312.7 definiert genau diese State-Based Action. 

Beim **Spielstart** ist es nochmals anders: Wird dort ein Phenomenon oben getroffen, wird es einfach unten ins Planar Deck gelegt und weitergesucht; seine Ability triggert ausdrücklich nicht. 

Das kann man mit `Plane.createRandomPlane()` schlicht nicht sauber abbilden.

---

# So würde ich das Projekt tatsächlich schneiden

Ich würde das nicht als einen 100-Dateien-Monstercommit machen. Ich würde es in folgende sieben sauber testbare Schritte schneiden:

1. **Planechase Rules Core.** `RollPlanarDieSpecialAction` sauber neu bauen, Timing korrekt auf Main-Phase/Stack-empty beschränken, Aktivierungszähler statt `PlanarRollWatcher` für die Kosten verwenden, `ChaosEnsuesEffect` + `CHAOS_ENSUES` ergänzen und die inherent planeswalking ability korrekt auf den Stack bringen. Noch keine 21 Planes massenhaft umbauen.

2. **Planar Controller.** Zentrale `planarControllerId`-Logik einführen und bei Turn-Wechsel bzw. Player-leaves korrekt aktualisieren. Face-up Plane-Abilities müssen ihren Controller automatisch mitwechseln. Keine manuellen `source.setControllerId(activePlayer)`-Hacks mehr.

3. **Bestehende Plane-Abilities migrieren.** Jetzt die 21 alten Plane-Klassen auf `ChaosEnsuesTriggeredAbility`, `PlaneswalkToSourceTriggeredAbility` und den neuen Controller umstellen. Den ganzen `ActivateIfConditionActivatedAbility + PlanarRollWatcher + CostIncreasingEffect`-Boilerplate löschen. Genau hier ist der größte Teil von #11316 als Vorlage nützlich.

4. **Echtes `PlanarDeck`.** `seenPlanes` und Random-Auswahl entfernen. Eine geordnete, mischbare Deckstruktur über PlanarCard-IDs einführen. Wichtig: keine neue Magic-Zone; die Objekte bleiben Command-Zone-Objekte. `PlaneswalkEffect` legt die offenen Objekte tatsächlich unten unter ihre Decks und deckt die richtige oberste Karte auf.

5. **`PlanarCard` + mehrere offene Planes.** `Plane` und neues `Phenomenon` unter einer gemeinsamen Runtime-Abstraktion vereinigen, Face-up/Face-down-Zustand und Ownership korrekt modellieren und die Engine von `getCurrentPlane()` auf Collections umstellen.

6. **Phenomena.** Encounter-Trigger, Startgame-Sonderfall und die entsprechende State-Based Action implementieren. Erst hier würde ich anfangen, echte Phenomenon-Karten hinzuzufügen.

7. **Content und UI.** MOC, WHO und die restlichen alten Planes/Phenomena implementieren; anschließend Shared-Planar-Deck-Auswahl und später Individual Planar Decks im Client. Der aktuelle XMage-Modus bleibt zunächst als automatisch generiertes Shared Deck aller implementierten Planar Cards kompatibel.

Damit hätten wir nach **Schritt 1 schon einen deutlich besseren Planechase-Core**, ohne gleich alles aufzureißen. Nach Schritt 3 wären die bisherigen 21 Planes sauber. Und Schritt 4–6 verwandeln die Simulation schließlich in echtes Planechase.

## Was ich konkret aus #11316 kopieren würde

Nicht den Code als Ganzes. Ich würde praktisch diese drei Ideen extrahieren:

```text
RollPlanarDieSpecialAction
        ✓ Konzept

ChaosEnsuesTriggeredAbility
        ✓ fast direkt verwendbar

PlaneswalkToSourceTriggeredAbility
        ✓ API/Name
        ✗ Implementation neu
```

Die Massenänderungen der Plane-Klassen dienen uns anschließend als **Migrationsvorlage**.

Dagegen würde ich diese Teile bewusst **nicht übernehmen**:

```text
Player.rollPlanarDie() mit Planechase-Events
ROLLED_PLANESWALK ohne echte triggered ability
RollPlanarDieSpecialAction in seiner aktuellen Form
Plane-Controller-Behandlung
den unfertigen PlaneswalkToSource-Trigger
```

Und vor allem würde ich **nicht versuchen, #11316 erst irgendwie compilefähig zu machen**. Wir würden dann mehrere seiner Architekturfehler konservieren und danach wieder entfernen.

## Der erste Codex-Auftrag sollte deshalb viel kleiner sein als „implementiere Planechase“

Ich würde Codex im ersten Durchgang ausschließlich diesen Zielzustand geben:

```text
OLD
Plane owns planar die ability
        ↓
RollPlanarDieEffect directly runs chaos/planeswalk


NEW
Game owns RollPlanarDieSpecialAction
        ↓
raw planar die result
        ↓
Planechase result resolver

CHAOS
 → ChaosEnsuesEffect
 → CHAOS_ENSUES
 → plane trigger

PLANESWALKER
 → inherent planeswalking triggered ability
 → STACK
 → existing PlaneswalkEffect
```

**Kein Planar Deck im selben Auftrag. Keine Phenomena. Keine 21-Plane-Massenmigration.**

Das ist der entscheidende Unterschied zu einem Mammut-Prompt: Wir bauen erst das Fundament so, dass wir anschließend nicht 100 Plane-Klassen zweimal anfassen müssen.

Und ein Detail würde ich sofort mitnehmen: den XMage-**9-Seiten-Würfel würde ich in Phase 1 noch nicht ändern**. Erst Engine-Verhalten korrigieren und Tests grün bekommen; die Umstellung auf regelkonforme `1 Chaos / 1 PW / 4 blank` ist danach eine winzige, isolierte Änderung. So wissen wir bei Regressionen immer, ob die Architektur oder bloß die Wahrscheinlichkeitsänderung schuld ist.

**Mein nächster Schritt wäre jetzt der konkrete Codex-Prompt für Phase 1**, bereits auf deinen `Lunafreliana/mage-custom:custom`-Stand zugeschnitten, inklusive exakter Dateien, gewünschter Tests, Dingen die Codex ausdrücklich *nicht* verändern darf und Akzeptanzkriterien. Das wäre jetzt ein sinnvoller erster Implementierungsauftrag.

[1]: https://magic.wizards.com/en/news/feature/march-of-the-machine-release-notes?utm_source=chatgpt.com "March of the Machine Release Notes"
[2]: https://media.wizards.com/2026/downloads/MagicCompRules%2020260619.pdf?utm_source=chatgpt.com "Magic: The Gathering Comprehensive Rules"
