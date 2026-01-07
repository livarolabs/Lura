package com.lura.domain.hardware

import android.view.KeyEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.NoLiveLiterals
import javax.inject.Inject
import javax.inject.Singleton

@NoLiveLiterals
@Singleton
class HardwareKeyManager @Inject constructor() {

    private val _events = MutableSharedFlow<KeyEventType>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<KeyEventType> = _events.asSharedFlow()

    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // We only care about Volume keys for now
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                _events.tryEmit(KeyEventType.VolumeDown)
                false // Return false to allow system volume change if not consumed? 
                // PRD says "Override... to intercept". So usually we return true if we handle it.
                // But we need to know if we are in a state that handles it.
                // For simplicity, we emit and let the UI decide if it wants to consume.
                // Actually, if we want to BLOCK volume change, we must return true here.
                // Let's return true for now to block system volume when app is open (aggressive but matches "Instrument" philosophy).
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                _events.tryEmit(KeyEventType.VolumeUp)
                true
            }
            else -> false
        }
    }

    fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                _events.tryEmit(KeyEventType.VolumeDownLong)
                true
            }
            else -> false
        }
    }
}

enum class KeyEventType {
    VolumeUp,
    VolumeDown,
    VolumeDownLong
}
