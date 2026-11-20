package com.google.firebase.example.agenticway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.example.agenticway.ui.screens.IdiomBridgeScreen
import com.google.firebase.example.agenticway.ui.screens.SignInScreen
import com.google.firebase.example.agenticway.ui.theme.AgenticWayTheme
import com.google.firebase.example.agenticway.viewmodel.AuthUser
import com.google.firebase.example.agenticway.viewmodel.AuthViewModel
import com.google.firebase.example.agenticway.viewmodel.IdiomBridgeViewModel
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {

    private val viewModel: IdiomBridgeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        enableEdgeToEdge()
        setContent {
            AgenticWayTheme {
                val authUiState by authViewModel.uiState.collectAsState()
                val currentUser = authUiState.user
                if (currentUser == null) {
                    SignInScreen(
                        uiState = authUiState,
                        onSignInClick = { authViewModel.signInWithGoogle(this) }
                    )
                } else {
                    IdiomBridgeScreen(
                        viewModel = viewModel,
                        user = currentUser,
                        onSignOutClick = { authViewModel.signOut(this) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun IdiomBridgeScreenPreview() {
    AgenticWayTheme {
        IdiomBridgeScreen(
            viewModel = IdiomBridgeViewModel(),
            user = AuthUser(
                uid = "preview-uid",
                displayName = "Alex Morgan",
                email = "alex.morgan@example.com",
                photoUrl = null
            )
        )
    }
}
