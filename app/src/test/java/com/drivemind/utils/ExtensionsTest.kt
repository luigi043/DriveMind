package com.drivemind.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    @Test
    fun `toRelativeTime - less than 1 min shows Just now`() {
        val ts = System.currentTimeMillis() - 30_000L
        assertEquals("Just now", ts.toRelativeTime())
    }

    @Test
    fun `toRelativeTime - 5 minutes ago`() {
        val ts = System.currentTimeMillis() - 5 * 60_000L
        assertEquals("5 min ago", ts.toRelativeTime())
    }

    @Test
    fun `toRelativeTime - 2 hours ago`() {
        val ts = System.currentTimeMillis() - 2 * 3_600_000L
        assertEquals("2h ago", ts.toRelativeTime())
    }

    @Test
    fun `toFullDateTime - returns non-empty string`() {
        val ts = System.currentTimeMillis()
        assertTrue(ts.toFullDateTime().isNotBlank())
    }

    @Test
    fun `toFullDateTime - contains the current year`() {
        val ts = System.currentTimeMillis()
        val result = ts.toFullDateTime()
        assertTrue(result.contains(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()))
    }
}
