package com.example.autoclicker

import kotlinx.coroutines.*

object ClickerEngine {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start(x: Float, y: Float, intervalMs: Long, onTick: (Int) -> Unit = {}) {
        stop()
        var count = 0
        job = scope.launch {
            while (isActive) {
                ClickerService.instance?.clickAt(x, y)
                count++
                onTick(count)
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
