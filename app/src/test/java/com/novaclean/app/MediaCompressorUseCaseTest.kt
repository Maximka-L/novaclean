package com.novaclean.app

import com.novaclean.app.domain.model.CompressedResult
import com.novaclean.app.domain.model.CompressionPreset
import com.novaclean.app.domain.repository.BillingRepository
import com.novaclean.app.domain.repository.CompressorRepository
import com.novaclean.app.domain.usecase.CompressMediaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaCompressorUseCaseTest {

    private class FakeCompressorRepository(
        var freeCount: Int = 2
    ) : CompressorRepository {
        override suspend fun compressImage(uriString: String, preset: CompressionPreset): Result<CompressedResult> {
            val original = 10_000_000L
            val saved = (original * preset.estimatedSavingsPercent) / 100
            val compressed = original - saved
            return Result.success(
                CompressedResult(
                    originalSizeBytes = original,
                    compressedSizeBytes = compressed,
                    savedBytes = saved,
                    savedPercent = preset.estimatedSavingsPercent,
                    outputUri = "content://fake/output"
                )
            )
        }

        override fun getRemainingFreeCompressions(): Int = freeCount

        override fun decrementFreeCompressions() {
            if (freeCount > 0) freeCount--
        }

        override fun addBonusFreeCompressions(count: Int) {
            freeCount += count
        }

        override fun getFileSizeBytes(uriString: String): Long = 10_000_000L
    }

    private class FakeBillingRepository(var isPro: Boolean = false) : BillingRepository {
        override val isProUser: StateFlow<Boolean> = MutableStateFlow(isPro)
        override fun getAvailablePlans(): List<com.novaclean.app.domain.model.SubscriptionPlan> = emptyList()
        override suspend fun purchasePlan(activity: android.app.Activity?, plan: com.novaclean.app.domain.model.SubscriptionPlan) = Result.success(Unit)
        override suspend fun restorePurchases() = Result.success(false)
        override fun checkBatchCleanAllowed(itemCount: Int) = true
    }

    @Test
    fun testFreeUserLimitDecrements() = runBlocking {
        val compressorRepo = FakeCompressorRepository(freeCount = 2)
        val billingRepo = FakeBillingRepository(isPro = false)
        val useCase = CompressMediaUseCase(compressorRepo, billingRepo)

        assertTrue(useCase.canCompress())
        assertEquals(2, useCase.getRemainingFree())

        val res1 = useCase.execute("uri1", CompressionPreset.BALANCED)
        assertTrue(res1.isSuccess)
        assertEquals(1, useCase.getRemainingFree())

        val res2 = useCase.execute("uri2", CompressionPreset.HIGH_SAVINGS)
        assertTrue(res2.isSuccess)
        assertEquals(0, useCase.getRemainingFree())

        assertFalse(useCase.canCompress())
        val res3 = useCase.execute("uri3", CompressionPreset.BALANCED)
        assertTrue(res3.isFailure)
    }

    @Test
    fun testProUserHasUnlimitedCompressions() = runBlocking {
        val compressorRepo = FakeCompressorRepository(freeCount = 0)
        val billingRepo = FakeBillingRepository(isPro = true)
        val useCase = CompressMediaUseCase(compressorRepo, billingRepo)

        assertTrue(useCase.canCompress())
        val res = useCase.execute("uri_pro", CompressionPreset.HIGH_SAVINGS)
        assertTrue(res.isSuccess)
        assertEquals(0, useCase.getRemainingFree()) // Didn't decrement or block
    }

    @Test
    fun testPresetSavingsCalculation() {
        val high = CompressionPreset.HIGH_SAVINGS
        val balanced = CompressionPreset.BALANCED
        val low = CompressionPreset.LOW_COMPRESSION

        assertTrue(high.estimatedSavingsPercent > balanced.estimatedSavingsPercent)
        assertTrue(balanced.estimatedSavingsPercent > low.estimatedSavingsPercent)
    }

    @Test
    fun testRewardedAdBonusCompressions() = runBlocking {
        val compressorRepo = FakeCompressorRepository(freeCount = 0)
        val billingRepo = FakeBillingRepository(isPro = false)
        val useCase = CompressMediaUseCase(compressorRepo, billingRepo)

        assertFalse(useCase.canCompress())
        assertEquals(0, useCase.getRemainingFree())

        // User watched rewarded ad -> +1 compression granted
        useCase.addBonusFree(1)
        assertTrue(useCase.canCompress())
        assertEquals(1, useCase.getRemainingFree())

        val res = useCase.execute("bonus_uri", CompressionPreset.BALANCED)
        assertTrue(res.isSuccess)
        assertEquals(0, useCase.getRemainingFree())
    }
}
