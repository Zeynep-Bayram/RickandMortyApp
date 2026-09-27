package com.example.rickandmortyapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.api.CharacterApi
import com.example.rickandmortyapp.api.RetrofitInstance
import com.example.rickandmortyapp.favorites.FavoritesRepository
import com.example.rickandmortyapp.model.Character
import com.example.rickandmortyapp.model.FavoriteCharacter
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
sealed interface CharacterUiState {
    object Loading : CharacterUiState
    data class Success(
        val characters: List<Character>,
        val isLoadingNextPage: Boolean = false,
        val nextPageError: String? = null
    ) : CharacterUiState
    object Empty : CharacterUiState
    data class Error(val message: String) : CharacterUiState

}

class CharacterViewModel(
    private val api: CharacterApi = RetrofitInstance.characterApi,
    favoritesFlow: Flow<List<FavoriteCharacter>> = FavoritesRepository.observeFavorites()
) : ViewModel() {
    private val _uiState =
        mutableStateOf<CharacterUiState>(CharacterUiState.Loading)
    val uiState = _uiState

    private val _searchText = MutableStateFlow("")

    val searchText = _searchText.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds = _favoriteIds.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteCharacter>>(emptyList())
    val favorites = _favorites.asStateFlow()

    private val _favoritesLoading = MutableStateFlow(true)
    val favoritesLoading = _favoritesLoading.asStateFlow()

    private val _favoriteError = MutableStateFlow<String?>(null)
    val favoriteError = _favoriteError.asStateFlow()

    private var searchJob: Job? = null
    private var requestJob: Job? = null
    private var lastSearchText: String? = null
    private var currentPage = 1
    private var hasNextPage = true
    private var characters = listOf<Character>()

    init {
        observeSearch()

        favoritesFlow
            .catch { error ->
                _favoriteError.value = error.message ?: "Couldn't load favorites."
                _favoritesLoading.value = false
                emit(emptyList())
            }
            .onEach { favorites ->
                _favorites.value = favorites
                _favoriteIds.value = favorites.map { it.characterId }.toSet()
                _favoritesLoading.value = false
                _favoriteError.value = null
            }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        searchJob?.cancel()
        searchJob = searchText
            .map { it.trim() }
            .distinctUntilChanged()
            .onEach {
                // Cancel the previous request before waiting for the next query to settle.
                requestJob?.cancel()
                val state = _uiState.value
                if (state is CharacterUiState.Success && state.isLoadingNextPage) {
                    _uiState.value = state.copy(isLoadingNextPage = false)
                }
            }
            .debounce { query -> if (query.isEmpty()) 0L else 500L }
            .onEach { query -> fetchCharacters(query.ifEmpty { null }) }
            .launchIn(viewModelScope)
    }

    private fun fetchCharacters(searchText: String?, page: Int = 1) {
        requestJob?.cancel()
        if (page == 1) {
            currentPage = 1
            hasNextPage = true
            characters = emptyList()
            lastSearchText = searchText
            _uiState.value = CharacterUiState.Loading
        } else {
            _uiState.value = CharacterUiState.Success(characters, isLoadingNextPage = true)
        }

        requestJob = viewModelScope.launch {
            try {
                val response = api.searchCharacters(
                    searchText,
                    page = page
                )
                ensureActive()
                currentPage = page
                hasNextPage = response.info.next != null
                characters = if (page == 1) response.results else characters + response.results
                _uiState.value = if (characters.isEmpty()) {
                    CharacterUiState.Empty
                } else {
                    CharacterUiState.Success(characters)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                ensureActive()
                if (e.code() == 404 && page == 1 && !searchText.isNullOrBlank()) {
                    hasNextPage = false
                    _uiState.value = CharacterUiState.Empty
                } else {
                    showRequestError(e.message ?: "Something went wrong", page)
                }
            } catch (e: UnknownHostException) {
                ensureActive()
                showRequestError(
                    "Couldn't reach the server. Check your internet connection.", page
                )
            } catch (e: IOException) {
                ensureActive()
                showRequestError("Connection error. Please try again.", page)
            } catch (e: Exception) {
                ensureActive()
                showRequestError(e.message ?: "Something went wrong", page)
            }
        }
    }

    private fun showRequestError(message: String, page: Int) {
        _uiState.value = if (page > 1) {
            CharacterUiState.Success(characters, nextPageError = message)
        } else {
            CharacterUiState.Error(message)
        }
    }

    fun loadNextPage() {
        val state = _uiState.value as? CharacterUiState.Success ?: return
        if (!hasNextPage || requestJob?.isActive == true || state.nextPageError != null) return
        if (_searchText.value.trim().ifEmpty { null } != lastSearchText) return
        fetchCharacters(lastSearchText, page = currentPage + 1)
    }

    fun retry() {
        if (requestJob?.isActive == true) return
        if (_searchText.value.trim().ifEmpty { null } != lastSearchText) return
        val state = _uiState.value
        if (state is CharacterUiState.Success && state.nextPageError != null) {
            fetchCharacters(lastSearchText, page = currentPage + 1)
        } else if (state is CharacterUiState.Error) {
            fetchCharacters(lastSearchText)
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun resetSearch() {
        requestJob?.cancel()
        _searchText.value = ""
        _uiState.value = CharacterUiState.Loading
        // Restart even when the text was already blank; discard any pending query.
        observeSearch()
    }

    fun toggleFavorite(character: Character) {
        viewModelScope.launch {
            val currentlyFavorite = character.id in _favoriteIds.value
            _favoriteError.value = null
            _favoriteIds.value = if (currentlyFavorite) {
                _favoriteIds.value - character.id
            } else {
                _favoriteIds.value + character.id
            }

            val result = if (currentlyFavorite) {
                FavoritesRepository.removeFavorite(character.id)
            } else {
                FavoritesRepository.addFavorite(character)
            }

            result.onFailure { error ->
                _favoriteIds.value = if (currentlyFavorite) {
                    _favoriteIds.value + character.id
                } else {
                    _favoriteIds.value - character.id
                }
                _favoriteError.value = error.localizedMessage
                    ?: "Couldn't update favorite."
            }
        }
    }

    fun removeFavorite(characterId: Int) {
        viewModelScope.launch {
            val previous = _favorites.value
            _favoriteError.value = null
            _favorites.value = previous.filter { it.characterId != characterId }
            _favoriteIds.value = _favoriteIds.value - characterId

            FavoritesRepository.removeFavorite(characterId)
                .onFailure { error ->
                    _favorites.value = previous
                    _favoriteIds.value = previous.map { it.characterId }.toSet()
                    _favoriteError.value = error.localizedMessage
                        ?: "Couldn't update favorite."
                }
        }
    }

}


