package org.mage.test.cards.sets;

import mage.constants.SetType;
import mage.sets.Clun;
import org.junit.Assert;
import org.junit.Test;

public class ClunSetTest {

    @Test
    public void customSetStartsEmptyWithStableIdentity() {
        Clun set = Clun.getInstance();

        Assert.assertSame(set, Clun.getInstance());
        Assert.assertEquals("CLUN", set.getName());
        Assert.assertEquals("CLUN", set.getCode());
        Assert.assertEquals(SetType.CUSTOM_SET, set.getSetType());
        Assert.assertTrue(set.getSetCardInfo().isEmpty());
    }
}
