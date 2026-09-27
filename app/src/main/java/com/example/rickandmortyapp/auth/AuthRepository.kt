package com.example.rickandmortyapp.auth

import android.app.Activity
import android.util.Patterns
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.rickandmortyapp.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object AuthRepository {
    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUid: String?
        get() = auth.currentUser?.uid

    val isSignedIn: Boolean
        get() = auth.currentUser != null

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        val normalizedEmail = email.trim()
        validateEmail(normalizedEmail)?.let { return Result.failure(IllegalArgumentException(it)) }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty."))
        }
        return try {
            val result = auth.signInWithEmailAndPassword(normalizedEmail, password).await()
            val user = result.user
                ?: return Result.failure(IllegalStateException("Sign-in failed."))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthError(e))
        }
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<FirebaseUser> {
        val normalizedEmail = email.trim()
        validateEmail(normalizedEmail)?.let { return Result.failure(IllegalArgumentException(it)) }
        PasswordRules.firstError(password)?.let {
            return Result.failure(IllegalArgumentException(it))
        }
        if (password != confirmPassword) {
            return Result.failure(IllegalArgumentException("Passwords do not match."))
        }
        return try {
            val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
            val user = result.user
                ?: return Result.failure(IllegalStateException("Registration failed."))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthError(e))
        }
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email cannot be empty."
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Enter a valid email address."
        }
        return null
    }

    private fun mapAuthError(error: Exception): Exception {
        val message = when (error) {
            is FirebaseAuthUserCollisionException ->
                "An account already exists with this email. Try signing in."
            is FirebaseAuthWeakPasswordException ->
                "Password is not strong enough."
            is FirebaseAuthInvalidUserException ->
                "No account found with this email."
            is FirebaseAuthInvalidCredentialsException ->
                "Incorrect email or password."
            else -> {
                val raw = error.localizedMessage.orEmpty()
                if (raw.contains("Trust anchor", ignoreCase = true) ||
                    raw.contains("CertPathValidatorException", ignoreCase = true)
                ) {
                    "Secure connection failed. Check emulator date/time and internet, then try again."
                } else {
                    raw.ifBlank { "Something went wrong. Please try again." }
                }
            }
        }
        return IllegalStateException(message)
    }

    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        return try {
            val webClientId = activity.getString(R.string.default_web_client_id)
            val credentialManager = CredentialManager.create(activity)

            val credentialResult = try {
                requestGoogleCredential(
                    activity = activity,
                    credentialManager = credentialManager,
                    webClientId = webClientId,
                    filterByAuthorizedAccounts = true
                )
            } catch (_: NoCredentialException) {
                requestGoogleCredential(
                    activity = activity,
                    credentialManager = credentialManager,
                    webClientId = webClientId,
                    filterByAuthorizedAccounts = false
                )
            }

            val credential = credentialResult.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(
                    IllegalStateException("Unexpected credential type")
                )
            }

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleIdTokenCredential.idToken,
                null
            )
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
                ?: return Result.failure(IllegalStateException("Firebase user was null"))
            Result.success(user)
        } catch (_: GetCredentialCancellationException) {
            Result.failure(SignInCancelledException())
        } catch (_: NoCredentialException) {
            Result.failure(
                IllegalStateException(
                    "No Google account found on this device. Add one in Settings, then try again."
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun requestGoogleCredential(
        activity: Activity,
        credentialManager: CredentialManager,
        webClientId: String,
        filterByAuthorizedAccounts: Boolean
    ) = credentialManager.getCredential(
        context = activity,
        request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()
            )
            .build()
    )

    fun signOut() {
        auth.signOut()
    }
}

class SignInCancelledException : Exception("Sign-in cancelled")
