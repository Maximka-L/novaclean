package com.novaclean.app

import com.novaclean.app.domain.model.LargeFileCategory
import com.novaclean.app.domain.model.LargeFileItem
import com.novaclean.app.domain.model.SizeThreshold
import com.novaclean.app.domain.repository.LargeFilesRepository
import com.novaclean.app.domain.usecase.ScanLargeFilesUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LargeFilesScannerTest {

    private class FakeLargeFilesRepository(
        private val sampleFiles: List<LargeFileItem>
    ) : LargeFilesRepository {
        override suspend fun getLargeFiles(minSizeBytes: Long): List<LargeFileItem> {
            return sampleFiles.filter { it.sizeBytes >= minSizeBytes }
        }

        override suspend fun deleteFiles(files: List<LargeFileItem>): Result<Int> {
            return Result.success(files.size)
        }
    }

    private val testFiles = listOf(
        LargeFileItem(1L, "movie.mp4", "/path/movie.mp4", "content://1", 1_200_000_000L, "video/mp4", LargeFileCategory.VIDEO, 0L),
        LargeFileItem(2L, "video_clip.mkv", "/path/clip.mkv", "content://2", 600_000_000L, "video/x-matroska", LargeFileCategory.VIDEO, 0L),
        LargeFileItem(3L, "archive.zip", "/path/archive.zip", "content://3", 120_000_000L, "application/zip", LargeFileCategory.ARCHIVE, 0L),
        LargeFileItem(4L, "doc.pdf", "/path/doc.pdf", "content://4", 60_000_000L, "application/pdf", LargeFileCategory.DOCUMENT, 0L),
        LargeFileItem(5L, "small.txt", "/path/small.txt", "content://5", 10_000_000L, "text/plain", LargeFileCategory.DOCUMENT, 0L)
    )

    @Test
    fun testThresholdFiltering() = runBlocking {
        val repo = FakeLargeFilesRepository(testFiles)
        val useCase = ScanLargeFilesUseCase(repo)

        val files50MB = useCase.execute(SizeThreshold.SIZE_50MB.bytes, LargeFileCategory.ALL)
        assertEquals(4, files50MB.size) // movie (1.2GB), clip (600MB), archive (120MB), doc (60MB)

        val files100MB = useCase.execute(SizeThreshold.SIZE_100MB.bytes, LargeFileCategory.ALL)
        assertEquals(3, files100MB.size)

        val files500MB = useCase.execute(SizeThreshold.SIZE_500MB.bytes, LargeFileCategory.ALL)
        assertEquals(2, files500MB.size)

        val files1GB = useCase.execute(SizeThreshold.SIZE_1GB.bytes, LargeFileCategory.ALL)
        assertEquals(1, files1GB.size)
        assertEquals("movie.mp4", files1GB.first().name)
    }

    @Test
    fun testCategoryFiltering() = runBlocking {
        val repo = FakeLargeFilesRepository(testFiles)
        val useCase = ScanLargeFilesUseCase(repo)

        val videoFiles = useCase.execute(SizeThreshold.SIZE_50MB.bytes, LargeFileCategory.VIDEO)
        assertEquals(2, videoFiles.size)
        assertTrue(videoFiles.all { it.category == LargeFileCategory.VIDEO })

        val archiveFiles = useCase.execute(SizeThreshold.SIZE_50MB.bytes, LargeFileCategory.ARCHIVE)
        assertEquals(1, archiveFiles.size)
        assertEquals("archive.zip", archiveFiles.first().name)
    }
}
