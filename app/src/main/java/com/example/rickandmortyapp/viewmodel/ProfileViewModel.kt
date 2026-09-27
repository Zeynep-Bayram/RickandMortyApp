package com.example.rickandmortyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.auth.AuthRepository
import com.example.rickandmortyapp.favorites.FavoritesRepository
import com.example.rickandmortyapp.profile.ProfileRepository
import com.example.rickandmortyapp.profile.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ProfileViewModel : ViewModel() {
    private val _profile = MutableStateFlow(
        UserProfile(displayName = ProfileRepository.fallbackDisplayName())
    )
    val profile = _profile.asStateFlow()

    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount = _favoriteCount.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var pendingAvatarId: Int? = null

    init {
        ProfileRepository.observeProfile()
            .catch { error ->
                _error.value = error.message ?: "Couldn't load profile."
                emit(null)
            }
            .onEach { loaded ->
                if (loaded != null) {
                    val pending = pendingAvatarId
                    _profile.value = loaded.copy(
                        displayName = loaded.displayName.ifBlank { _profile.value.displayName },
                        avatarCharacterId = when {
                            pending != null && loaded.avatarCharacterId != pending -> pending
                            else -> {
                                pendingAvatarId = null
                                loaded.avatarCharacterId ?: _profile.value.avatarCharacterId
                            }
                        }
                    )
                    _error.value = null
                }
            }
            .launchIn(viewModelScope)

        FavoritesRepository.observeFavorites()
            .catch { emit(emptyList()) }
            .onEach { favorites ->
                _favoriteCount.value = favorites.size
            }
            .launchIn(viewModelScope)
    }

    fun saveProfile(displayName: String, avatarCharacterId: Int?) {
        val name = displayName.trim().ifBlank { _profile.value.displayName }
        val avatarId = avatarCharacterId ?: _profile.value.avatarCharacterId
        pendingAvatarId = avatarId
        _error.value = null
        _profile.value = UserProfile(
            displayName = name,
            avatarCharacterId = avatarId
        )
        ProfileRepository.saveProfile(name, avatarId)
    }

    fun signOut() {
        AuthRepository.signOut()
    }
}
