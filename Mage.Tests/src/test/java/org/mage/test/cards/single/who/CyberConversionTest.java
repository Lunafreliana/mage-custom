package org.mage.test.cards.single.who;

import mage.abilities.keyword.FlyingAbility;
import mage.cards.repository.TokenInfo;
import mage.cards.repository.TokenRepository;
import mage.constants.CardType;
import mage.constants.EmptyNames;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;
import mage.view.CardView;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class CyberConversionTest extends CardTestPlayerBase {

    private static final String FACE_DOWN = EmptyNames.FACE_DOWN_CREATURE.getTestCommand();

    @Test
    public void testNormalCreatureBecomesCyberman() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Cyber Conversion");
        addCard(Zone.BATTLEFIELD, playerB, "Serra Angel");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cyber Conversion", "Serra Angel");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent(FACE_DOWN, playerB);
        Assert.assertEquals("The face-down creature should have no name", "", permanent.getName());
        Assert.assertTrue("The face-down creature should be colorless", permanent.getColor(currentGame).isColorless());
        Assert.assertTrue("The face-down creature should have no mana cost", permanent.getManaCost().isEmpty());
        Assert.assertTrue("The face-down creature should have no supertypes", permanent.getSuperType(currentGame).isEmpty());
        assertPermanentCount(playerB, FACE_DOWN, 1);
        assertPowerToughness(playerB, FACE_DOWN, 2, 2);
        assertType(FACE_DOWN, CardType.CREATURE, true);
        assertType(FACE_DOWN, CardType.ARTIFACT, true);
        assertSubtype(FACE_DOWN, SubType.CYBERMAN);
        assertNotSubtype(FACE_DOWN, SubType.ANGEL);
        assertAbility(playerB, FACE_DOWN, FlyingAbility.getInstance(), false);

        TokenInfo reminder = TokenRepository.instance.findPreferredTokenInfoForXmage(
                TokenRepository.XMAGE_IMAGE_NAME_FACE_DOWN_CYBERMAN, permanent.getId());
        Assert.assertNotNull("Cyberman reminder image must be registered", reminder);
        Assert.assertEquals("https://api.scryfall.com/cards/twho/24/en?format=image", reminder.getDownloadUrl());
        Assert.assertEquals("Cyberman", permanent.getImageFileName());
        Assert.assertEquals(TokenRepository.XMAGE_TOKENS_SET_CODE, permanent.getExpansionSetCode());
        Assert.assertEquals(Integer.valueOf(1), permanent.getImageNumber());

        TokenInfo normalFaceDown = TokenRepository.instance.findPreferredTokenInfoForXmage(
                TokenRepository.XMAGE_IMAGE_NAME_FACE_DOWN_MANUAL, permanent.getId());
        Assert.assertNotNull("The ordinary face-down image must remain registered", normalFaceDown);
        Assert.assertNotEquals(normalFaceDown.getName(), reminder.getName());

        CardView view = new CardView((PermanentCard) permanent, currentGame, false, false);
        Assert.assertTrue("The view should show the dynamically added artifact type",
                view.getCardTypes().contains(CardType.ARTIFACT));
        Assert.assertTrue("The view should show the face-down creature type",
                view.getCardTypes().contains(CardType.CREATURE));
        Assert.assertTrue("The view should show the dynamically added Cyberman subtype",
                view.getSubTypes().contains(SubType.CYBERMAN));
        Assert.assertFalse("The view should not show the original Angel subtype",
                view.getSubTypes().contains(SubType.ANGEL));
        Assert.assertEquals("Cyberman", view.getImageFileName());
        Assert.assertEquals(TokenRepository.XMAGE_TOKENS_SET_CODE, view.getExpansionSetCode());
        Assert.assertEquals(1, view.getImageNumber());
    }

    @Test
    public void testLegendaryCreatureLosesLegendary() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Cyber Conversion");
        addCard(Zone.BATTLEFIELD, playerB, "Naban, Dean of Iteration");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cyber Conversion", "Naban, Dean of Iteration");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent(FACE_DOWN, playerB);
        Assert.assertFalse(permanent.getSuperType(currentGame).contains(SuperType.LEGENDARY));
        Assert.assertTrue(permanent.getSuperType(currentGame).isEmpty());
        assertSubtype(FACE_DOWN, SubType.CYBERMAN);
    }

    @Test
    public void testMultipleOriginalSubtypesAreRemoved() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Cyber Conversion");
        addCard(Zone.BATTLEFIELD, playerB, "Veteran Armorsmith");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cyber Conversion", "Veteran Armorsmith");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent(FACE_DOWN, playerB);
        assertNotSubtype(FACE_DOWN, SubType.HUMAN);
        assertNotSubtype(FACE_DOWN, SubType.SOLDIER);
        assertSubtype(FACE_DOWN, SubType.CYBERMAN);
        Assert.assertEquals("Cyberman should be the only subtype", 1, permanent.getSubtype(currentGame).size());
    }

    @Test
    public void testMorphCanTurnFaceUpAndLosesCybermanCharacteristics() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);
        addCard(Zone.HAND, playerA, "Cyber Conversion");
        addCard(Zone.BATTLEFIELD, playerA, "Pine Walker");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cyber Conversion", "Pine Walker");
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{4}{G}: Turn this face-down permanent face up.");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Pine Walker", 1);
        assertPowerToughness(playerA, "Pine Walker", 5, 5);
        assertType("Pine Walker", CardType.ARTIFACT, false);
        assertNotSubtype("Pine Walker", SubType.CYBERMAN);
        assertSubtype("Pine Walker", SubType.ELEMENTAL);

        Permanent permanent = getPermanent("Pine Walker", playerA);
        CardView view = new CardView((PermanentCard) permanent, currentGame, false, false);
        Assert.assertNotEquals("Face-up cards must not retain Cyberman reminder artwork",
                "Cyberman", view.getImageFileName());
    }

    @Test
    public void testDoubleFacedCreatureDoesNotTurnFaceDown() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Cyber Conversion");
        addCard(Zone.BATTLEFIELD, playerB, "Delver of Secrets");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cyber Conversion", "Delver of Secrets");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerB, "Delver of Secrets", 1);
        assertPermanentCount(playerB, FACE_DOWN, 0);
        assertPowerToughness(playerB, "Delver of Secrets", 1, 1);
    }
}
