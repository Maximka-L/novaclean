package com.novaclean.app

import com.novaclean.app.domain.model.PasswordOptions
import com.novaclean.app.domain.model.PasswordStrengthLevel
import com.novaclean.app.domain.usecase.PasswordGeneratorUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGeneratorTest {

    private val generator = PasswordGeneratorUseCase()

    @Test
    fun testPasswordLength() {
        val options12 = PasswordOptions(length = 12)
        val pass12 = generator.generatePassword(options12)
        assertEquals(12, pass12.length)

        val options24 = PasswordOptions(length = 24)
        val pass24 = generator.generatePassword(options24)
        assertEquals(24, pass24.length)
    }

    @Test
    fun testContainsSelectedCharSets() {
        val options = PasswordOptions(
            length = 20,
            includeUppercase = true,
            includeLowercase = true,
            includeDigits = true,
            includeSymbols = true
        )
        val pass = generator.generatePassword(options)

        assertTrue("Should contain uppercase", pass.any { it.isUpperCase() })
        assertTrue("Should contain lowercase", pass.any { it.isLowerCase() })
        assertTrue("Should contain digits", pass.any { it.isDigit() })
        assertTrue("Should contain symbols", pass.any { !it.isLetterOrDigit() })
    }

    @Test
    fun testExcludeSimilarCharacters() {
        val options = PasswordOptions(
            length = 30,
            includeUppercase = true,
            includeLowercase = true,
            includeDigits = true,
            excludeSimilar = true
        )
        val pass = generator.generatePassword(options)

        assertFalse("Should not contain 0", pass.contains('0'))
        assertFalse("Should not contain O", pass.contains('O'))
        assertFalse("Should not contain 1", pass.contains('1'))
        assertFalse("Should not contain l", pass.contains('l'))
        assertFalse("Should not contain I", pass.contains('I'))
    }

    @Test
    fun testStrengthEvaluation() {
        val weakEvaluation = generator.evaluateStrength("abc")
        assertEquals(PasswordStrengthLevel.WEAK, weakEvaluation.level)

        val mediumEvaluation = generator.evaluateStrength("Abc123xyz")
        assertEquals(PasswordStrengthLevel.MEDIUM, mediumEvaluation.level)

        val strongEvaluation = generator.evaluateStrength("X9#mK2@vL4%pQ7!")
        assertTrue(
            strongEvaluation.level == PasswordStrengthLevel.STRONG ||
            strongEvaluation.level == PasswordStrengthLevel.EXCELLENT
        )
    }
}
