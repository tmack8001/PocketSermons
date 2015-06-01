package com.tmack.pocketsermons.common.model;

import junit.framework.TestCase;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class SermonTest extends TestCase {
    public void testIncrementCount() throws Exception {
        assertEquals(0, Sermon.getCount());
        Sermon.incCount();
        assertEquals(1, Sermon.getCount());
        Sermon.incCount();
        Sermon.incCount();
        assertEquals(3, Sermon.getCount());
    }
}