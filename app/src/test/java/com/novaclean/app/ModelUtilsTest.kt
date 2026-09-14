package com.novaclean.app

import com.novaclean.app.domain.model.formatFileSize
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelUtilsTest {

    @Test
    fun testFormatFileSizeZero() {
        assertEquals("0 B", formatFileSize(0))
        assertEquals("0 B", formatFileSize(-100))
    }

    @Test
    fun testFormatFileSizeKilobytes() {
        assertEquals("1.0 KB", formatFileSize(1024))
        assertEquals("2.5 KB", formatFileSize((1024 * 2.5).toLong()))
    }

    @Test
    fun testFormatFileSizeMegabytes() {
        assertEquals("1.0 MB", formatFileSize(1024 * 1024))
        assertEquals("500.0 MB", formatFileSize(500L * 1024 * 1024))
    }

    @Test
    fun testFormatFileSizeGigabytes() {
        assertEquals("1.5 GB", formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
    }
}
