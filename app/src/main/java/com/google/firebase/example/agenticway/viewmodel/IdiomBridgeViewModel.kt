package com.google.firebase.example.agenticway.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.8-flash")
    }

    /**
     * Updates the user's input text for the British idiom.
     */
    fun onBritishIdiomChanged(newText: String) {
        _uiState.update { it.copy(britishIdiomInput = newText, errorMessage = null) }
    }

    /**
     * Translates the British idiom to its Brazilian equivalent.
     */
    fun onTranslateClicked() {
        val currentInput = _uiState.value.britishIdiomInput.trim()
        if (currentInput.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a British idiom to translate.")
            }
            return
        }
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                brazilianIdiomOutput = null
            )
        }

        viewModelScope.launch {
            try {
                val prompt = """
                    You are a bilingual cultural translation expert. Translate the input British English idiom to its closest culturally equivalent Brazilian Portuguese idiom - the expression Brazilians actually use to convey the exact same meaning, tone, and humor, not a literal word-for-word translation. Return the exact Brazilian Portuguese idiom as your response. 

                    $currentInput
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val translatedText = response.text?.trim()

                if (!translatedText.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(
                            brazilianIdiomOutput = translatedText,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No translation was returned. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to translate idiom. Please try again."
                    )
                }
            }
        }
    }
}

