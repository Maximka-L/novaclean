package com.novaclean.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactsNormalizationTest {

    private fun normalizePhoneNumber(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        if (digits.length == 11 && (digits.startsWith("7") || digits.startsWith("8"))) {
            return "7" + digits.substring(1)
        }
        return digits
    }

    @Test
    fun testRussianPhoneFormats() {
        val format1 = "+7 (999) 123-45-67"
        val format2 = "8 (999) 123 45 67"
        val format3 = "89991234567"
        val format4 = "+79991234567"

        val norm1 = normalizePhoneNumber(format1)
        val norm2 = normalizePhoneNumber(format2)
        val norm3 = normalizePhoneNumber(format3)
        val norm4 = normalizePhoneNumber(format4)

        assertEquals("79991234567", norm1)
        assertEquals(norm1, norm2)
        assertEquals(norm2, norm3)
        assertEquals(norm3, norm4)
    }

    @Test
    fun testInternationalNumbers() {
        val usNumber = "+1 (555) 234-5678"
        assertEquals("15552345678", normalizePhoneNumber(usNumber))
    }
}
