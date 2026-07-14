package io.github.emmanuel_pastor.fake.progress

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

class FakeProgress(private val eta: Duration) {
    private val _value = MutableStateFlow(0.0)
    val value: StateFlow<Double> = _value

    suspend fun start() = coroutineScope {
        _value.value = 0.0
        while (_value.value < 1.0) {
            _value.value += 0.01
            delay(eta / 100)
        }
    }

    fun finish() {
    }
}
