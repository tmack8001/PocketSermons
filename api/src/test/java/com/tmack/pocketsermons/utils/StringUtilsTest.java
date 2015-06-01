package com.tmack.pocketsermons.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class StringUtilsTest {
    @Test
    public void testIsEmpty() throws Exception {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(" "));
        assertTrue(StringUtils.isEmpty("\n"));

        assertFalse(StringUtils.isEmpty("abcde"));
        assertFalse(StringUtils.isEmpty(" abcde "));
        assertFalse(StringUtils.isEmpty(" abcde \n"));
    }

    @Test
    public void testIsNotEmpty() throws Exception {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertFalse(StringUtils.isNotEmpty(" "));
        assertFalse(StringUtils.isNotEmpty("\r"));

        assertTrue(StringUtils.isNotEmpty("abcde"));
        assertTrue(StringUtils.isNotEmpty(" abcde "));
        assertTrue(StringUtils.isNotEmpty(" abcde \n"));
    }
}
