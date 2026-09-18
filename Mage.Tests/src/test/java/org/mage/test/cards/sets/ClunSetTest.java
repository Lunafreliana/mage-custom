package org.mage.test.cards.sets;

import mage.constants.Rarity;
import mage.constants.SetType;
import mage.sets.CLUN;
import org.junit.Assert;
import org.junit.Test;

public class ClunSetTest {

    @Test
    public void customSetHasStableIdentityAndFirstCard() {
        CLUN set = CLUN.getInstance();

        Assert.assertSame(set, CLUN.getInstance());
        Assert.assertEquals("CLUN", set.getName());
        Assert.assertEquals("CLUN", set.getCode());
        Assert.assertEquals(SetType.CUSTOM_SET, set.getSetType());
        Assert.assertEquals(1, set.getSetCardInfo().size());
        Assert.assertEquals("Vulpes, Cursed Tail", set.getSetCardInfo().get(0).getName());
        Assert.assertEquals("1", set.getSetCardInfo().get(0).getCardNumber());
        Assert.assertEquals(Rarity.RARE, set.getSetCardInfo().get(0).getRarity());
    }
}
