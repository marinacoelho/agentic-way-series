package com.google.firebase.example.agenticway.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.example.agenticway.R
import com.google.firebase.example.agenticway.ui.theme.BrazilianBadgeBackground
import com.google.firebase.example.agenticway.ui.theme.BrazilianBadgeText
import com.google.firebase.example.agenticway.ui.theme.BritishBadgeBackground
import com.google.firebase.example.agenticway.ui.theme.BritishBadgeText
import com.google.firebase.example.agenticway.ui.theme.EmptyStateCircleBackground
import com.google.firebase.example.agenticway.ui.theme.EmptyStateIconTint
import com.google.firebase.example.agenticway.ui.theme.HeaderIconBackground
import com.google.firebase.example.agenticway.ui.theme.HeaderIconTint
import com.google.firebase.example.agenticway.ui.theme.IdiomScreenBackground
import com.google.firebase.example.agenticway.ui.theme.InputCardBackground
import com.google.firebase.example.agenticway.ui.theme.OutputCardBackground
import com.google.firebase.example.agenticway.ui.theme.TextPrimary
import com.google.firebase.example.agenticway.ui.theme.TextSecondary
import com.google.firebase.example.agenticway.ui.theme.TranslateButtonBackground
import com.google.firebase.example.agenticway.ui.theme.TranslateButtonContent
import com.google.firebase.example.agenticway.viewmodel.IdiomBridgeViewModel
import com.google.firebase.example.agenticway.viewmodel.IdiomLanguage

@Composable
fun IdiomBridgeScreen(
    viewModel: IdiomBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = IdiomScreenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            IdiomBridgeHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Cards Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input Card (From Language Idiom)
                InputIdiomCard(
                    fromLanguage = uiState.fromLanguage,
                    toLanguage = uiState.toLanguage,
                    onFromLanguageSelected = viewModel::onFromLanguageSelected,
                    inputText = uiState.idiomInput,
                    isLoading = uiState.isLoading,
                    onTextChanged = viewModel::onIdiomInputChanged,
                    onTranslateClick = viewModel::onTranslateClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Output Card (To Language Idiom)
                OutputIdiomCard(
                    toLanguage = uiState.toLanguage,
                    fromLanguage = uiState.fromLanguage,
                    onToLanguageSelected = viewModel::onToLanguageSelected,
                    outputText = uiState.idiomOutput,
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

/**
 * Top Header row featuring the circular translate icon badge and app title.
 */
@Composable
fun IdiomBridgeHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(HeaderIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_translate),
                contentDescription = stringResource(R.string.app_name),
                tint = HeaderIconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "Idiom Bridge",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

/**
 * Upper Card where the user selects the source language and inputs the idiom.
 */
@Composable
fun InputIdiomCard(
    fromLanguage: IdiomLanguage,
    toLanguage: IdiomLanguage,
    onFromLanguageSelected: (IdiomLanguage) -> Unit,
    inputText: String,
    onTextChanged: (String) -> Unit,
    onTranslateClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = InputCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
        ) {
            // Dropdown Language Chip
            LanguageDropdownBadge(
                selectedLanguage = fromLanguage,
                disabledLanguage = toLanguage,
                onLanguageSelected = onFromLanguageSelected,
                backgroundColor = BritishBadgeBackground,
                textColor = BritishBadgeText,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Seamless Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = onTextChanged,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(TextPrimary),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Type the ${fromLanguage.displayName} idiom",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // Translate Action Button
            Button(
                onClick = onTranslateClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TranslateButtonBackground,
                    contentColor = TranslateButtonContent
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = toLanguage.flagEmoji,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isLoading) "Translating..." else "Translate",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TranslateButtonContent
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward),
                        contentDescription = null,
                        tint = TranslateButtonContent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Lower Card showing the target language dropdown, placeholder state, loading state, error message, or translated idiom.
 */
@Composable
fun OutputIdiomCard(
    toLanguage: IdiomLanguage,
    fromLanguage: IdiomLanguage,
    onToLanguageSelected: (IdiomLanguage) -> Unit,
    outputText: String?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = OutputCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
        ) {
            // Dropdown Language Chip
            LanguageDropdownBadge(
                selectedLanguage = toLanguage,
                disabledLanguage = fromLanguage,
                onLanguageSelected = onToLanguageSelected,
                backgroundColor = BrazilianBadgeBackground,
                textColor = BrazilianBadgeText,
                enabled = !isLoading
            )

            // Center Content (Loading, Error, Placeholder Empty State, or Result)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = HeaderIconTint,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Finding the ${toLanguage.displayName} equivalent...",
                                fontSize = 16.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    !errorMessage.isNullOrBlank() -> {
                        Text(
                            text = errorMessage,
                            fontSize = 16.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    outputText.isNullOrBlank() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(EmptyStateCircleBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_chat_bubble),
                                    contentDescription = null,
                                    tint = EmptyStateIconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "You'll see the ${toLanguage.displayName} version of\nthis idiom here",
                                fontSize = 16.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 260.dp)
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = outputText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pill-shaped dropdown chip displaying a flag emoji, uppercase language label, and dropdown menu
 * for choosing between British English, Brazilian Portuguese, and Chilean Spanish.
 */
@Composable
fun LanguageDropdownBadge(
    selectedLanguage: IdiomLanguage,
    disabledLanguage: IdiomLanguage,
    onLanguageSelected: (IdiomLanguage) -> Unit,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(enabled = enabled) { expanded = true },
            shape = RoundedCornerShape(50),
            color = backgroundColor
        ) {
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedLanguage.flagEmoji,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = selectedLanguage.badgeLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.width(2.dp))

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                    contentDescription = "Select language",
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(InputCardBackground)
        ) {
            IdiomLanguage.entries.forEach { language ->
                val isOptionEnabled = language != disabledLanguage
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = language.flagEmoji,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = language.displayName,
                                fontSize = 14.sp,
                                fontWeight = if (language == selectedLanguage) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOptionEnabled) TextPrimary else TextSecondary.copy(alpha = 0.45f)
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onLanguageSelected(language)
                    },
                    enabled = isOptionEnabled
                )
            }
        }
    }
}

