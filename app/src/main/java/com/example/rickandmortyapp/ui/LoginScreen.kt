package com.example.rickandmortyapp.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rickandmortyapp.auth.AuthRepository
import com.example.rickandmortyapp.auth.PasswordRules
import com.example.rickandmortyapp.auth.SignInCancelledException
import kotlinx.coroutines.launch

private val PortalGreen = Color(0xFF35D66F)
private val FieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = PortalGreen,
        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
        focusedLabelColor = PortalGreen,
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = PortalGreen,
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f)
    )

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordFieldFocused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val passwordRules = remember(password) { PasswordRules.evaluate(password) }

    fun submitEmailAuth() {
        if (isLoading || isGoogleLoading) return
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = if (isRegisterMode) {
                AuthRepository.registerWithEmail(email, password, confirmPassword)
            } else {
                AuthRepository.signInWithEmail(email, password)
            }
            result
                .onSuccess { onSignedIn() }
                .onFailure { error ->
                    errorMessage = error.localizedMessage ?: "Something went wrong. Please try again."
                }
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RICK AND MORTY",
            color = PortalGreen,
            fontSize = 28.sp,
            fontFamily = getSchwiftyFont,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isRegisterMode) "CREATE AN ACCOUNT TO ENTER THE MULTIVERSE" else "SIGN IN TO ENTER THE MULTIVERSE",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
            singleLine = true,
            enabled = !isLoading && !isGoogleLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = FieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            singleLine = true,
            enabled = !isLoading && !isGoogleLoading,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = {
                    focusManager.clearFocus()
                    submitEmailAuth()
                }
            ),
            colors = FieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    passwordFieldFocused = focusState.hasFocus
                }
        )

        if (isRegisterMode && passwordFieldFocused) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Password rules",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                passwordRules.forEach { rule ->
                    Text(
                        text = (if (rule.satisfied) "✓ " else "• ") + rule.label,
                        color = if (rule.satisfied) PortalGreen else Color(0xFFFFB74D),
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (isRegisterMode) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text("Confirm password") },
                singleLine = true,
                enabled = !isLoading && !isGoogleLoading,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        submitEmailAuth()
                    }
                ),
                colors = FieldColors,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                submitEmailAuth()
            },
            enabled = !isLoading && !isGoogleLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = PortalGreen,
                contentColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isRegisterMode) "Sign up" else "Sign in",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!isGoogleLoading) {
            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage = null
                    confirmPassword = ""
                    passwordFieldFocused = false
                },
                enabled = !isLoading
            ) {
                Text(
                    text = if (isRegisterMode) {
                        "Already have an account? Sign in"
                    } else {
                        "Don't have an account? Sign up"
                    },
                    color = PortalGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.25f))
            Text(
                text = "  or  ",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.25f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val activity = context as? Activity
                if (activity == null) {
                    errorMessage = "Unable to start Google sign-in."
                    return@OutlinedButton
                }
                scope.launch {
                    isGoogleLoading = true
                    errorMessage = null
                    AuthRepository.signInWithGoogle(activity)
                        .onSuccess { onSignedIn() }
                        .onFailure { error ->
                            if (error !is SignInCancelledException) {
                                errorMessage = error.localizedMessage
                                    ?: "Google sign-in failed. Try again."
                            }
                        }
                    isGoogleLoading = false
                }
            },
            enabled = !isLoading && !isGoogleLoading,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGoogleLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Continue with Google",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color(0xFFFF8A80),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}
