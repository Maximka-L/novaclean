package com.novaclean.app.domain.usecase

import com.novaclean.app.R
import com.novaclean.app.domain.model.PasswordEvaluation
import com.novaclean.app.domain.model.PasswordOptions
import com.novaclean.app.domain.model.PasswordStrengthLevel
import java.security.SecureRandom
import kotlin.math.log2
import kotlin.math.pow

class PasswordGeneratorUseCase {

    private val secureRandom = SecureRandom()

    private val upperChars = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val upperSimilar = "IO"
    private val lowerChars = "abcdefghijkmnpqrstuvwxyz"
    private val lowerSimilar = "lo"
    private val digitChars = "23456789"
    private val digitSimilar = "01"
    private val symbolChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    fun generatePassword(options: PasswordOptions): String {
        var pool = ""
        val mandatoryChars = mutableListOf<Char>()

        if (options.includeUppercase) {
            val u = if (options.excludeSimilar) upperChars else upperChars + upperSimilar
            pool += u
            mandatoryChars.add(u[secureRandom.nextInt(u.length)])
        }
        if (options.includeLowercase) {
            val l = if (options.excludeSimilar) lowerChars else lowerChars + lowerSimilar
            pool += l
            mandatoryChars.add(l[secureRandom.nextInt(l.length)])
        }
        if (options.includeDigits) {
            val d = if (options.excludeSimilar) digitChars else digitChars + digitSimilar
            pool += d
            mandatoryChars.add(d[secureRandom.nextInt(d.length)])
        }
        if (options.includeSymbols) {
            pool += symbolChars
            mandatoryChars.add(symbolChars[secureRandom.nextInt(symbolChars.length)])
        }

        if (pool.isEmpty()) {
            pool = lowerChars
            mandatoryChars.add(pool[secureRandom.nextInt(pool.length)])
        }

        val length = options.length.coerceIn(6, 64)
        val passwordChars = ArrayList(mandatoryChars)

        while (passwordChars.size < length) {
            passwordChars.add(pool[secureRandom.nextInt(pool.length)])
        }

        // Fisher-Yates shuffle
        for (i in passwordChars.size - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = temp
        }

        return passwordChars.joinToString("")
    }

    fun evaluateStrength(password: String): PasswordEvaluation {
        if (password.isEmpty()) {
            return PasswordEvaluation(
                level = PasswordStrengthLevel.WEAK,
                titleRes = R.string.password_gen_strength_weak,
                timeToCrackRes = R.string.password_gen_time_instant,
                entropyBits = 0,
                scoreRatio = 0.05f
            )
        }

        var poolSize = 0
        if (password.any { it.isUpperCase() }) poolSize += 26
        if (password.any { it.isLowerCase() }) poolSize += 26
        if (password.any { it.isDigit() }) poolSize += 10
        if (password.any { !it.isLetterOrDigit() }) poolSize += 32

        val entropy = (password.length * log2(poolSize.coerceAtLeast(2).toDouble())).toInt()

        return when {
            entropy < 40 -> PasswordEvaluation(
                level = PasswordStrengthLevel.WEAK,
                titleRes = R.string.password_gen_strength_weak,
                timeToCrackRes = R.string.password_gen_time_instant,
                entropyBits = entropy,
                scoreRatio = 0.25f
            )
            entropy in 40..58 -> PasswordEvaluation(
                level = PasswordStrengthLevel.MEDIUM,
                titleRes = R.string.password_gen_strength_medium,
                timeToCrackRes = R.string.password_gen_time_minutes,
                entropyBits = entropy,
                scoreRatio = 0.5f
            )
            entropy in 59..80 -> PasswordEvaluation(
                level = PasswordStrengthLevel.STRONG,
                titleRes = R.string.password_gen_strength_strong,
                timeToCrackRes = R.string.password_gen_time_years,
                timeToCrackArg = 500,
                entropyBits = entropy,
                scoreRatio = 0.78f
            )
            else -> PasswordEvaluation(
                level = PasswordStrengthLevel.EXCELLENT,
                titleRes = R.string.password_gen_strength_excellent,
                timeToCrackRes = R.string.password_gen_time_centuries,
                entropyBits = entropy,
                scoreRatio = 1.0f
            )
        }
    }
}
