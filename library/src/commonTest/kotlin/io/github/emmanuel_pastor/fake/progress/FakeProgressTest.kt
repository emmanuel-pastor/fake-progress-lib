package io.github.emmanuel_pastor.fake.progress

import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.times

class FakeProgressTest {
    private companion object {
        const val EPSILON = 1e-6
        val ETA = 1.minutes
        val ENDING_DURATION = 250.milliseconds
    }

    @Test
    fun `reach 100 percent within 200ms or less when finish is called right after estimated time has elapsed`() =
        runTest {
            val progressTracker = FakeProgress(ETA)

            launch { progressTracker.start() }
            delay(ETA)
            progressTracker.finish()
            delay(ENDING_DURATION)

            assertEquals(1.0, progressTracker.progress.value, EPSILON)
        }

    @Test
    fun `reach 100 percent within 200ms or less when finish has been called before estimated time has elapsed`() =
        runTest {
            val progressTracker = FakeProgress(ETA)

            val job = launch { progressTracker.start() }
            yield()
            progressTracker.finish()
            delay(ENDING_DURATION)

            assertEquals(1.0, progressTracker.progress.value, EPSILON)
            job.cancel()
        }

    @Test
    fun `reach 100 percent within 200ms or less when finish has been called long after estimated time has elapsed`() =
        runTest {
            val progressTracker = FakeProgress(ETA)

            val job = launch { progressTracker.start() }
            delay(4 * ETA)
            progressTracker.finish()
            delay(ENDING_DURATION)

            assertEquals(1.0, progressTracker.progress.value, EPSILON)
            job.cancel()
        }

    @Test
    fun `don't reach 100 percent before finish has been called`() = runTest {
        val progressTracker = FakeProgress(ETA)

        val job = launch { progressTracker.start() }
        delay(ETA)

        assertTrue { progressTracker.progress.value < 1.0 }
        job.cancel()
    }

    @Test
    fun `don't go above 100 percent`() = runTest {
        val progressTracker = FakeProgress(ETA)

        val job = launch { progressTracker.start() }
        delay(ETA)
        progressTracker.finish()
        delay(ETA)

        progressTracker.progress.test {
            assertTrue { awaitItem() <= 1.0 }
        }
        job.cancel()
    }

    @Test
    fun `progress must be monotonic`() = runTest {
        val progressTracker = FakeProgress(ETA)

        launch { progressTracker.start() }

        launch {
            delay(ETA)
            progressTracker.finish()
        }

        progressTracker.progress.test {
            var previous = awaitItem()
            while (previous < 1.0) {
                val current = awaitItem()
                assertTrue(current >= previous, "Progress decreased from $previous to $current")
                previous = current
            }
        }
    }

    @Test
    fun `multiple calls to start should raise an exception`() = runTest {
        val progressTracker = FakeProgress(ETA)

        val job = launch { progressTracker.start() }
        yield()

        assertFailsWith<IllegalStateException> { progressTracker.start() }
        job.cancel()
    }

}
