package io.github.emmanuel_pastor.fake.progress

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.exp
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.times

@OptIn(ExperimentalAtomicApi::class)
class FakeProgress(private val eta: Duration) {
    private companion object {
        val UPDATE_INTERVAL = 16.milliseconds

        const val A = 0.85
        const val B = 0.95
        const val K = 4.0

        val ENDING_DURATION = 200.milliseconds
    }

    private val isStarted: AtomicBoolean = AtomicBoolean(false)
    private val isTaskFinished: AtomicBoolean = AtomicBoolean(false)

    private val _progress = MutableStateFlow(0.0)
    val progress: StateFlow<Double> = _progress

    suspend fun start() {
        check(isStarted.compareAndSet(expectedValue = false, newValue = true)) { "FakeProgress already started" }
        _progress.value = 0.0
        isTaskFinished.store(false)
        var elapsedTime = Duration.ZERO

        while (_progress.value < A && !isTaskFinished.load()) {
            _progress.value = (elapsedTime / eta).coerceIn(0.0, A)

            delay(UPDATE_INTERVAL)
            elapsedTime += UPDATE_INTERVAL
        }

        elapsedTime = Duration.ZERO
        while (!isTaskFinished.load()) {
            val progress = elapsedTime / (B * eta - A * eta)
            _progress.value = A + (1 - exp(-K * progress)) * (B - A)

            delay(UPDATE_INTERVAL)
            elapsedTime += UPDATE_INTERVAL
        }

        elapsedTime = Duration.ZERO
        while (_progress.value < 1.0) {
            delay(UPDATE_INTERVAL)
            elapsedTime += UPDATE_INTERVAL

            _progress.value = min(elapsedTime / ENDING_DURATION, 1.0)
        }
    }

    fun finish() {
        isTaskFinished.store(true)
    }
}
