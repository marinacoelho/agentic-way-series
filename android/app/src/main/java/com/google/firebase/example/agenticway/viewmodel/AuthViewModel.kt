package com.google.firebase.example.agenticway.viewmodel

import android.content.Context
import android.content.MutableContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.example.agenticway.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Lightweight representation of the currently authenticated user for the UI layer.
 */
data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)

/**
 * UI State for Firebase Authentication.
 */
data class AuthUiState(
    val user: AuthUser? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel managing Firebase Authentication with Google Sign-In via Credential Manager.
 */
class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _uiState = MutableStateFlow(
        AuthUiState(user = auth.currentUser?.toAuthUser())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _uiState.update { currentState ->
            currentState.copy(user = firebaseAuth.currentUser?.toAuthUser())
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }

    /**
     * Initiates Sign-in with Google using Android Credential Manager and exchanges
     * the resulting Google ID token for a Firebase Authentication session.
     */
    fun signInWithGoogle(activityContext: Context) {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activityContext)
                val serverClientId = activityContext.getString(R.string.default_web_client_id)

                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                    serverClientId = serverClientId
                ).build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val mutableContext = MutableContextWrapper(activityContext)
                val result = credentialManager.getCredential(
                    request = request,
                    context = mutableContext
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken,
                        null
                    )
                    val authResult = auth.signInWithCredential(firebaseCredential).awaitResult()
                    val firebaseUser = authResult.user ?: auth.currentUser

                    _uiState.update {
                        it.copy(
                            user = firebaseUser?.toAuthUser(),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    Log.w(TAG, "Unexpected credential type: ${credential.type}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unexpected credential type returned. Please try again."
                        )
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "Google Sign-In cancelled by user", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
            } catch (e: NoCredentialException) {
                Log.w(TAG, "No Google credentials available", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No Google account found on this device. Please add a Google account in Settings and try again."
                    )
                }
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager sign-in failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.errorMessage?.toString()
                            ?: e.localizedMessage
                            ?: "Google Sign-In failed. Please try again."
                    )
                }
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Failed to parse Google ID token", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Invalid Google ID token received. Please try again."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase sign-in failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Authentication failed. Please try again."
                    )
                }
            }
        }
    }

    /**
     * Signs the user out of Firebase Auth and clears the Credential Manager state.
     */
    fun signOut(context: Context) {
        auth.signOut()
        _uiState.update { it.copy(user = null, isLoading = false, errorMessage = null) }

        val appContext = context.applicationContext
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(appContext)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: ClearCredentialException) {
                Log.w(TAG, "Couldn't clear user credentials: ${e.localizedMessage}", e)
            }
        }
    }

    private companion object {
        private const val TAG = "AuthViewModel"
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
    uid = uid,
    displayName = displayName,
    email = email,
    photoUrl = photoUrl?.toString()
)

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(
                task.exception ?: IllegalStateException("Firebase task failed without an exception.")
            )
        }
    }
}
