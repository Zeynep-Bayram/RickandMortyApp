package com.example.rickandmortyapp.profile

import com.example.rickandmortyapp.auth.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

object ProfileRepository {
    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeProfile(): Flow<UserProfile?> {
        return AuthRepository.authState.flatMapLatest { user ->
            val uid = user?.uid
            if (uid == null) {
                flowOf(null)
            } else {
                observeProfileForUid(uid)
            }
        }
    }

    fun saveProfile(displayName: String, avatarCharacterId: Int?) {
        val uid = AuthRepository.currentUid ?: return
        val name = displayName.trim().ifBlank { fallbackDisplayName() }
        val data = hashMapOf<String, Any>(
            "displayName" to name
        )
        if (avatarCharacterId != null) {
            data["avatarCharacterId"] = avatarCharacterId
        }
        firestore.collection("users").document(uid)
            .set(data, SetOptions.merge())
    }

    private fun observeProfileForUid(uid: String): Flow<UserProfile> {
        return callbackFlow {
            val registration = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val displayName = snapshot?.getString("displayName")
                        ?.takeIf { it.isNotBlank() }
                        ?: fallbackDisplayName()
                    val avatarId = (snapshot?.get("avatarCharacterId") as? Number)?.toInt()
                    trySend(
                        UserProfile(
                            displayName = displayName,
                            avatarCharacterId = avatarId
                        )
                    )
                }
            awaitClose { registration.remove() }
        }
    }

    fun fallbackDisplayName(): String {
        val user = AuthRepository.currentUser
        return user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: "Traveler"
    }
}
