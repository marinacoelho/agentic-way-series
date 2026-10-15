package com.google.firebase.example.agenticway.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * UI State for the Idiom Bridge screen.
 */
data class IdiomBridgeUiState(
    val britishIdiomInput: String = "",
    val brazilianIdiomOutput: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel managing the Idiom Bridge screen state and interactions.
 */
class IdiomBridgeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(IdiomBridgeUiState())
    val uiState: StateFlow<IdiomBridgeUiState> = _uiState.asStateFlow()

    /**
     * Updates the user's input text for the British idiom.
     */
    fun onBritishIdiomChanged(newText: String) {
        _uiState.update { it.copy(britishIdiomInput = newText) }
    }

    /**
     * Translates the British idiom to its Brazilian equivalent.
     * Note: This function is deliberately empty as per Phase 1 requirements.
     */
    fun onTranslateClicked() {
        // Intentionally left empty for now
    }
}
