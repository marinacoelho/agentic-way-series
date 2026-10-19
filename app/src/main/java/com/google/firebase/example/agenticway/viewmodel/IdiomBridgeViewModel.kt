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
 * Supported languages/varieties for idiom translation.
 */
enum class IdiomLanguage(
    val displayName: String,
    val shortName: String,
    val peopleName: String,
    val flagEmoji: String
) {
    BRITISH_ENGLISH(
        displayName = "British English",
        shortName = "British",
        peopleName = "Brits",
        flagEmoji = "🇬🇧"
    ),
    BRAZILIAN_PORTUGUESE(
        displayName = "Brazilian Portuguese",
        shortName = "Brazilian",
        peopleName = "Brazilians",
        flagEmoji = "🇧🇷"
    ),
    CHILEAN_SPANISH(
        displayName = "Chilean Spanish",
        shortName = "Chilean",
        peopleName = "Chileans",
        flagEmoji = "🇨🇱"
    );

    val badgeLabel: String
        get() = "${displayName.uppercase()} IDIOM"
}

/**
 * UI State for the Idiom Bridge screen.
 */
data class IdiomBridgeUiState(
    val fromLanguage: IdiomLanguage = IdiomLanguage.BRITISH_ENGLISH,
    val toLanguage: IdiomLanguage = IdiomLanguage.BRAZILIAN_PORTUGUESE,
    val idiomInput: String = "",
    val idiomOutput: String? = null,
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
     * Updates the user's input text for the source idiom.
     */
    fun onIdiomInputChanged(newText: String) {
        _uiState.update { it.copy(idiomInput = newText, errorMessage = null) }
    }

    /**
     * Updates the source ("from") language.
     * Prevents "from" and "to" languages from being the same by swapping if necessary.
     */
    fun onFromLanguageSelected(newLanguage: IdiomLanguage) {
        _uiState.update { currentState ->
            if (currentState.fromLanguage == newLanguage) return@update currentState
            val updatedToLanguage = if (newLanguage == currentState.toLanguage) {
                currentState.fromLanguage
            } else {
                currentState.toLanguage
            }
            currentState.copy(
                fromLanguage = newLanguage,
                toLanguage = updatedToLanguage,
                idiomOutput = null,
                errorMessage = null
            )
        }
    }

    /**
     * Updates the target ("to") language.
     * Prevents "from" and "to" languages from being the same by swapping if necessary.
     */
    fun onToLanguageSelected(newLanguage: IdiomLanguage) {
        _uiState.update { currentState ->
            if (currentState.toLanguage == newLanguage) return@update currentState
            val updatedFromLanguage = if (newLanguage == currentState.fromLanguage) {
                currentState.toLanguage
            } else {
                currentState.fromLanguage
            }
            currentState.copy(
                fromLanguage = updatedFromLanguage,
                toLanguage = newLanguage,
                idiomOutput = null,
                errorMessage = null
            )
        }
    }

    /**
     * Translates the source idiom to its culturally equivalent target idiom.
     */
    fun onTranslateClicked() {
        val currentState = _uiState.value
        val currentInput = currentState.idiomInput.trim()
        if (currentInput.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a ${currentState.fromLanguage.displayName} idiom to translate.")
            }
            return
        }
        if (currentState.fromLanguage == currentState.toLanguage) {
            _uiState.update {
                it.copy(errorMessage = "Source and target languages must be different.")
            }
            return
        }
        if (currentState.isLoading) return

        val fromLang = currentState.fromLanguage
        val toLang = currentState.toLanguage

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                idiomOutput = null
            )
        }

        viewModelScope.launch {
            try {
                val prompt = """
                    You are a bilingual cultural translation expert. Translate the input ${fromLang.displayName} idiom to its closest culturally equivalent ${toLang.displayName} idiom - the expression ${toLang.peopleName} actually use to convey the exact same meaning, tone, and humor, not a literal word-for-word translation. Return the exact ${toLang.displayName} idiom as your response. 

                    $currentInput
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val translatedText = response.text?.trim()

                if (!translatedText.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(
                            idiomOutput = translatedText,
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


