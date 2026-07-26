package io.github.emmanuel_pastor.fake.progress

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

            backgroundScope.launch { progressTracker.start() }
            delay(ETA)
            progressTracker.finish()
            delay(ENDING_DURATION)

            assertEquals(1.0, progressTracker.progress.value, EPSILON)
        }

    @Test
    fun `reach 100 percent within 200ms or less when finish has been called before estimated time has elapsed`() =
        runTest {
            val progressTracker = FakeProgress(ETA)

            backgroundScope.launch { progressTracker.start() }
            yield()
            progressTracker.finish()
            delay(ENDING_DURATION)

            assertEquals(1.0, progressTracker.progress.value, EPSILON)
        }

    @Test
    fun `reach 100 percent within 200ms or less when finish has been called long after estimated time has elapsed`() =
        runTest {
            val progressTracker = FakeProgress(ETA)

            backgroundScope.launch { progressTracker.start() }

            delay(4 * ETA)
            progressTracker.finish()
            delay(ENDING_DURATION)

            assertEquals(1.0, progressTracker.progress.value, EPSILON)
        }

    @Test
    fun `don't reach 100 percent before finish has been called`() = runTest {
        val progressTracker = FakeProgress(ETA)

        backgroundScope.launch { progressTracker.start() }
        delay(ETA)

        assertTrue { progressTracker.progress.value < 1.0 }
    }

    @Test
    fun `don't go above 100 percent`() = runTest {
        val progressTracker = FakeProgress(ETA)

        backgroundScope.launch { progressTracker.start() }

        launch {
            delay(ETA)
            progressTracker.finish()
        }

        progressTracker.progress.test {
            while (true) {
                val current = awaitItem()
                assertTrue(current <= 1.0, "Progress went above 100%: $current")
                if (current >= 1.0 - EPSILON) break
            }
        }
    }

    @Test
    fun `progress must be monotonic`() = runTest {
        val progressTracker = FakeProgress(ETA)

        backgroundScope.launch { progressTracker.start() }

        launch {
            delay(ETA)
            progressTracker.finish()
        }

        progressTracker.progress.test {
            var previous = awaitItem()
            while (previous < 1.0 - EPSILON) {
                val current = awaitItem()
                assertTrue(current >= previous, "Progress decreased from $previous to $current")
                previous = current
            }
        }
    }

    @Test
    fun `progress must reach 100 percent smoothly`() = runTest {
        val progressTracker = FakeProgress(ETA)

        backgroundScope.launch { progressTracker.start() }

        launch {
            delay(ETA / 2)
            progressTracker.finish()
        }

        progressTracker.progress.test {
            var previous = awaitItem()
            while (previous < 1.0 - EPSILON) {
                val current = awaitItem()
                val delta = current - previous
                assertTrue(delta <= 0.1, "Progress jumped too much: from $previous to $current (delta=$delta)")
                previous = current
            }
        }
    }

    @Test
    fun `progress must reach 100 percent smoothly when finished immediately`() = runTest {
        val progressTracker = FakeProgress(ETA)

        backgroundScope.launch { progressTracker.start() }

        launch {
            yield()
            progressTracker.finish()
        }

        progressTracker.progress.test {
            var previous = awaitItem()
            while (previous < 1.0 - EPSILON) {
                val current = awaitItem()
                val delta = current - previous
                assertTrue(delta <= 0.1, "Progress jumped too much: from $previous to $current (delta=$delta)")
                previous = current
            }
        }
    }

    @Test
    fun `multiple calls to start should raise an exception`() = runTest {
        val progressTracker = FakeProgress(ETA)

        backgroundScope.launch { progressTracker.start() }
        yield()

        assertFailsWith<IllegalStateException> { progressTracker.start() }
    }

    @Test
    fun `should be able to start again after a full cycle`() = runTest {
        val progressTracker = FakeProgress(ETA)

        // First cycle
        val job1 = launch { progressTracker.start() }
        delay(ETA)
        progressTracker.finish()
        job1.join()
        assertEquals(1.0, progressTracker.progress.value, EPSILON)

        // Second cycle
        val job2 = launch { progressTracker.start() }
        yield()
        assertTrue(progressTracker.progress.value < 1.0, "Progress should be reset for the second cycle")

        progressTracker.finish()
        job2.join()
        assertEquals(1.0, progressTracker.progress.value, EPSILON)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `finish before start only affects the first cycle`() = runTest {
        val progressTracker = FakeProgress(ETA)

        // Signal finish before first start
        progressTracker.finish()

        // First start should finish quickly
        val startTime = testScheduler.currentTime
        progressTracker.start()
        val duration = testScheduler.currentTime - startTime
        assertTrue(duration < ETA.inWholeMilliseconds, "Should have finished quickly, but took $duration ms")
        assertEquals(1.0, progressTracker.progress.value, EPSILON)

        // Second start should run normally
        val job2 = launch { progressTracker.start() }
        delay(ETA / 2)
        assertTrue(progressTracker.progress.value > 0.0)
        assertTrue(progressTracker.progress.value < 1.0)

        progressTracker.finish()
        job2.join()
        assertEquals(1.0, progressTracker.progress.value, EPSILON)
    }
}
