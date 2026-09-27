package com.example.rickandmortyapp.model

data class FavoriteCharacter(
    val characterId: Int = 0,
    val name: String = "",
    val image: String = "",
    val species: String = "",
    val status: String = "",
    val addedAt: Long = 0L
)
