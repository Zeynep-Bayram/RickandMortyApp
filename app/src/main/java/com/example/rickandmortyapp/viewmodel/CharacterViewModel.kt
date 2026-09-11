package com.example.rickandmortyapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.api.RetrofitInstance
import com.example.rickandmortyapp.model.Character
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
sealed interface CharacterUiState {
    object Loading : CharacterUiState
    data class Success(val characters: List<Character>) : CharacterUiState
    object Empty : CharacterUiState
    data class Error(val message: String) : CharacterUiState

}

class CharacterViewModel : ViewModel() {
    private val api = RetrofitInstance.characterApi
    private val _uiState =
        mutableStateOf<CharacterUiState>(CharacterUiState.Loading)
    val uiState = _uiState

    private val _searchText = MutableStateFlow("")

    val searchText = _searchText.asStateFlow()

    init {
        searchText
            .debounce(500)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    fetchCharacters()
                } else {
                    fetchCharacters(query)
                }
            }
            .launchIn(viewModelScope)
    }

    private var lastSearchText: String? = null

    private var currentPage = 1
    private var hasNextPage = true

    private var isLoadingNextPage = false
    private var characters = listOf<Character>()

    fun fetchCharacters(searchText: String? = null) {
        println("FETCH CALLED: $searchText")
        viewModelScope.launch {
            _uiState.value = CharacterUiState.Loading

            if (searchText != lastSearchText) {
                currentPage = 1
                characters = emptyList()
            }

            lastSearchText = searchText

            try {
                val response = api.searchCharacters(
                    searchText,
                    page = currentPage
                )

                hasNextPage = response.info.next != null

                if (currentPage == 1) { //ilk sayfadaysak listeyi oluştur
                    characters = response.results
                } else {//sonraki sayfadaysak gelen sonuçları mevcut listeye ekle
                    characters = characters + response.results
                }

                _uiState.value = CharacterUiState.Success(characters)

            } catch (e: HttpException) {
                if (e.code() == 404 && !searchText.isNullOrBlank()) {
                    _uiState.value = CharacterUiState.Empty
                } else {
                    _uiState.value = CharacterUiState.Error(
                        e.message ?: "Bir hata oluştu"
                    )
                }
            } catch (e: UnknownHostException) {
                _uiState.value = CharacterUiState.Error(
                    "Sunucuya ulaşılamadı. Emülatörün internetini kontrol et."
                )
            } catch (e: IOException) {
                _uiState.value = CharacterUiState.Error(
                    "Bağlantı hatası. Tekrar dene."
                )
            } catch (e: Exception) {
                _uiState.value = CharacterUiState.Error(
                    e.message ?: "Bir hata oluştu"
                )
            } finally {
                isLoadingNextPage = false
            }
        }
    }

    fun loadNextPage() {
        // Yeni sayfayı yalnızca daha fazla sayfa varsa
        // veya başka bir sayfa yüklenmiyorsa getirir
        if (!hasNextPage || isLoadingNextPage) return

        isLoadingNextPage = true
        currentPage++

        // Mevcut arama filtresini koruyarak yeni sayfayı getirir
        fetchCharacters(lastSearchText)
    }


    fun retry() {
        fetchCharacters(lastSearchText)
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

}


