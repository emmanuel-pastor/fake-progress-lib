package io.github.emmanuel_pastor.fake.progress

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.exp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.times

/**
 * A utility class to simulate realistic progress for asynchronous tasks where the exact duration is unknown.
 *
 * The progress simulation follows three phases:
 * 1. **Linear Phase**: Progress increases linearly towards 85% ([A]) over the estimated duration ([eta]).
 * 2. **Asymptotic Phase**: Progress slows down and asymptotically approaches 95% ([B]), ensuring the user
 *    sees continued activity even if the task takes longer than expected.
 * 3. **Ending Phase**: Once [markTaskComplete] is called, the progress quickly reaches 100% (1.0) over a short duration.
 *
 * @param eta The estimated time the task is expected to take.
 */
@OptIn(ExperimentalAtomicApi::class)
class FakeProgress(private val eta: Duration) {
    private companion object {
        /** The interval at which progress updates are emitted. */
        val UPDATE_INTERVAL = 16.milliseconds

        /** The progress value (0.85) where the linear phase ends and the asymptotic phase begins. */
        const val A = 0.85

        /** The progress value (0.95) that the asymptotic phase approaches. */
        const val B = 0.95

        /** The decay constant for the asymptotic phase. */
        const val K = 4.0

        /** The duration over which the progress reaches 1.0 after [markTaskComplete] is called. */
        val ENDING_DURATION = 200.milliseconds
    }

    private val isProgressRunning: AtomicBoolean = AtomicBoolean(false)
    private val isTaskComplete: AtomicBoolean = AtomicBoolean(false)

    private val _progress = MutableStateFlow(0.0)

    /**
     * A [StateFlow] emitting progress values from 0.0 to 1.0.
     */
    val progress: StateFlow<Double> = _progress

    /**
     * Starts the progress simulation.
     *
     * This is a suspending function that runs the progress loop. It will:
     * - Reset progress to 0.0.
     * - Move through the linear and asymptotic phases.
     * - Wait for [markTaskComplete] to be called to complete the ending phase.
     *
     * @throws IllegalStateException if the progress has already been started.
     */
    suspend fun start() {
        check(
            isProgressRunning.compareAndSet(
                expectedValue = false,
                newValue = true
            )
        ) { "FakeProgress already started" }
        try {
            _progress.value = 0.0
            var elapsedTime = Duration.ZERO

            while (_progress.value < A && !isTaskComplete.load()) {
                // Phase 1 (linear): ensure monotonic and do not exceed A due to rounding
                val candidate = (elapsedTime / eta).coerceIn(0.0, A)
                _progress.value = candidate.coerceIn(_progress.value, A)

                delay(UPDATE_INTERVAL)
                elapsedTime += UPDATE_INTERVAL
            }

            elapsedTime = Duration.ZERO
            while (!isTaskComplete.load()) {
                val progress = elapsedTime / (B * eta - A * eta)
                // Phase 2 (asymptotic towards B): clamp to be monotonic and not exceed B
                val candidate = A + (1 - exp(-K * progress)) * (B - A)
                _progress.value = candidate.coerceIn(_progress.value, B)

                delay(UPDATE_INTERVAL)
                elapsedTime += UPDATE_INTERVAL
            }

            elapsedTime = Duration.ZERO
            val phase3StartProgress = _progress.value
            while (_progress.value < 1.0) {
                delay(UPDATE_INTERVAL)
                elapsedTime += UPDATE_INTERVAL

                val t = (elapsedTime / ENDING_DURATION).coerceIn(0.0, 1.0)
                // Phase 3 (finish to 1.0): clamp to be monotonic and never exceed 1.0
                val candidate = phase3StartProgress + (1.0 - phase3StartProgress) * t
                _progress.value = candidate.coerceIn(_progress.value, 1.0)
            }
            // Make sure we end exactly at 1.0 (avoid tiny rounding residue)
            _progress.value = 1.0
        } finally {
            isProgressRunning.store(false)
            isTaskComplete.store(false)
        }
    }

    /**
     * Signals that the actual task has finished, triggering the final phase of the fake progress.
     *
     * Calling this will cause the [progress] to quickly move to 1.0 and the [start] function to return.
     */
    fun markTaskComplete() {
        isTaskComplete.store(true)
    }
}
