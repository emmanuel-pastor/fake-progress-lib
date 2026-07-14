package io.github.emmanuel_pastor.fake.progress

import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class FakeProgressTest {
    private val EPSILON = 1e-6

    @Test
    fun `reach 100% within 200ms or less after estimated time`() = runTest {
        val eta = 10.seconds
        val progressTracker = FakeProgress(eta)

        progressTracker.start()
        delay(eta)
        progressTracker.finish()
        delay(200.milliseconds)

        progressTracker.value.test {
            assertEquals(1.0, awaitItem(), EPSILON)
        }
    }
}
