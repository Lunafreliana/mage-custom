package org.mage.test.cards.sets;

import mage.constants.SetType;
import mage.sets.CLUN;
import org.junit.Assert;
import org.junit.Test;

public class ClunSetTest {

    @Test
    public void customSetHasStableIdentityAndHaruma() {
        CLUN set = CLUN.getInstance();

        Assert.assertSame(set, CLUN.getInstance());
        Assert.assertEquals("CLUN", set.getName());
        Assert.assertEquals("CLUN", set.getCode());
        Assert.assertEquals(SetType.CUSTOM_SET, set.getSetType());
        Assert.assertEquals(1, set.getSetCardInfo().size());
        Assert.assertEquals("Haruma, Veil Beneath the Storm", set.getSetCardInfo().get(0).getName());
        Assert.assertEquals("2", set.getSetCardInfo().get(0).getCardNumber());
    }
}
