package com.example.rickandmortyapp.favorites

import com.example.rickandmortyapp.auth.AuthRepository
import com.example.rickandmortyapp.model.Character
import com.example.rickandmortyapp.model.FavoriteCharacter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

object FavoritesRepository {
    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    suspend fun addFavorite(character: Character): Result<Unit> {
        val uid = currentUidOrNull()
            ?: return Result.failure(IllegalStateException("Sign in to add favorites."))
        return try {
            val favorite = FavoriteCharacter(
                characterId = character.id,
                name = character.name,
                image = character.image,
                species = character.species,
                status = character.status,
                addedAt = System.currentTimeMillis()
            )
            favoritesCollection(uid)
                .document(character.id.toString())
                .set(favorite)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(characterId: Int): Result<Unit> {
        val uid = currentUidOrNull()
            ?: return Result.failure(IllegalStateException("Sign in to remove favorites."))
        return try {
            favoritesCollection(uid)
                .document(characterId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeFavorites(): Flow<List<FavoriteCharacter>> {
        return AuthRepository.authState.flatMapLatest { user ->
            val uid = user?.uid
            if (uid == null) {
                flowOf(emptyList())
            } else {
                observeFavoritesForUid(uid)
            }
        }
    }

    private fun observeFavoritesForUid(uid: String): Flow<List<FavoriteCharacter>> {
        return callbackFlow {
            val registration = favoritesCollection(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val favorites = snapshot
                        ?.documents
                        ?.mapNotNull { it.toFavoriteCharacter() }
                        .orEmpty()
                        .sortedByDescending { it.addedAt }
                    trySend(favorites)
                }
            awaitClose { registration.remove() }
        }
    }

    private fun favoritesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("favorites")

    private fun currentUidOrNull(): String? = AuthRepository.currentUid

    private fun DocumentSnapshot.toFavoriteCharacter(): FavoriteCharacter? {
        val characterId = (get("characterId") as? Number)?.toInt()
            ?: id.toIntOrNull()
            ?: return null
        return FavoriteCharacter(
            characterId = characterId,
            name = getString("name").orEmpty(),
            image = getString("image").orEmpty(),
            species = getString("species").orEmpty(),
            status = getString("status").orEmpty(),
            addedAt = (get("addedAt") as? Number)?.toLong() ?: 0L
        )
    }
}
