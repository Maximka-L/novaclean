package com.novaclean.app.domain.model

enum class PasswordStrengthLevel {
    WEAK,
    MEDIUM,
    STRONG,
    EXCELLENT
}

data class PasswordEvaluation(
    val level: PasswordStrengthLevel,
    val titleRes: Int,
    val timeToCrackRes: Int,
    val timeToCrackArg: Int? = null,
    val entropyBits: Int,
    val scoreRatio: Float // 0.0 to 1.0 for progress bar
)

data class PasswordOptions(
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeDigits: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeSimilar: Boolean = true
)
