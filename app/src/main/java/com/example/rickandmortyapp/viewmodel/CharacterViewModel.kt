package com.example.rickandmortyapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.api.RetrofitInstance
import com.example.rickandmortyapp.model.Character
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

sealed interface CharacterUiState {
    object Loading : CharacterUiState
    data class Success(val characters: List<Character>) : CharacterUiState
    data class Error(val message: String) : CharacterUiState

}

class CharacterViewModel : ViewModel() {
    private val api = RetrofitInstance.characterApi
    private val _uiState =
        mutableStateOf<CharacterUiState>(CharacterUiState.Loading)
    val uiState = _uiState


    fun fetchCharacters() {
        viewModelScope.launch {
            _uiState.value = CharacterUiState.Loading
            try {
                val response = api.getCharacters()
                _uiState.value = CharacterUiState.Success(response.results)
            } catch (e: Exception) {
                _uiState.value = CharacterUiState.Error(
                    e.message ?: "Bir hata oluştu"
                )
            }
        }

    }
    fun searchCharacters(searchText: String){
        viewModelScope.launch{
            _uiState.value = CharacterUiState.Loading
            try {
                val response=api.searchCharacters(searchText)
                _uiState.value= CharacterUiState.Success(response.results)
            }catch (e: Exception){
                _uiState.value = CharacterUiState.Error(
                    e.message ?: "Bir hata oluştu")

            }


        }


    }


}