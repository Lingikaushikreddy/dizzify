package app.dizzify.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages focus state across the launcher for DPAD navigation
 */
class LauncherFocusManager {
    private val _currentSection = MutableStateFlow(FocusSection.HOME)
    val currentSection: StateFlow<FocusSection> = _currentSection.asStateFlow()
    
    private val _isSidebarFocused = MutableStateFlow(false)
    val isSidebarFocused: StateFlow<Boolean> = _isSidebarFocused.asStateFlow()
    
    private val _focusedItemIndex = MutableStateFlow(0)
    val focusedItemIndex: StateFlow<Int> = _focusedItemIndex.asStateFlow()
    
    private val _focusedRowIndex = MutableStateFlow(0)
    val focusedRowIndex: StateFlow<Int> = _focusedRowIndex.asStateFlow()
    
    fun setSection(section: FocusSection) {
        _currentSection.value = section
    }
    
    fun setSidebarFocused(focused: Boolean) {
        _isSidebarFocused.value = focused
    }
    
    fun setFocusedItem(index: Int) {
        _focusedItemIndex.value = index
    }
    
    fun setFocusedRow(index: Int) {
        _focusedRowIndex.value = index
    }
}

enum class FocusSection {
    SIDEBAR,
    HOME,
    APPS,
    SETTINGS,
    SEARCH
}

val LocalLauncherFocusManager = staticCompositionLocalOf { LauncherFocusManager() }

/**
 * Modifier for TV-optimized focus handling with restore capability
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.tvFocusable(
    focusRequester: FocusRequester? = null,
    onFocused: () -> Unit = {},
    onUnfocused: () -> Unit = {}
): Modifier = this
    .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
    .onFocusChanged { state ->
        if (state.isFocused) onFocused() else onUnfocused()
    }
    .focusable()

/**
 * Handle DPAD key events for custom navigation
 */
fun Modifier.dpadNavigation(
    onLeft: (() -> Boolean)? = null,
    onRight: (() -> Boolean)? = null,
    onUp: (() -> Boolean)? = null,
    onDown: (() -> Boolean)? = null,
    onSelect: (() -> Boolean)? = null,
    onBack: (() -> Boolean)? = null
): Modifier = this.onPreviewKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
    
    when (event.key) {
        Key.DirectionLeft -> onLeft?.invoke() ?: false
        Key.DirectionRight -> onRight?.invoke() ?: false
        Key.DirectionUp -> onUp?.invoke() ?: false
        Key.DirectionDown -> onDown?.invoke() ?: false
        Key.DirectionCenter, Key.Enter -> onSelect?.invoke() ?: false
        Key.Back, Key.Escape -> onBack?.invoke() ?: false
        else -> false
    }
}

/**
 * Remembers and restores focus within a composable scope
 */
@Composable
fun rememberFocusRestorer(): FocusRestorer {
    return remember { FocusRestorer() }
}

class FocusRestorer {
    private var lastFocusedIndex: Int = 0
    private val focusRequesters = mutableMapOf<Int, FocusRequester>()
    
    fun getFocusRequester(index: Int): FocusRequester {
        return focusRequesters.getOrPut(index) { FocusRequester() }
    }
    
    fun saveFocus(index: Int) {
        lastFocusedIndex = index
    }
    
    fun restoreFocus() {
        focusRequesters[lastFocusedIndex]?.requestFocus()
    }
    
    fun requestInitialFocus() {
        focusRequesters[0]?.requestFocus()
    }
}