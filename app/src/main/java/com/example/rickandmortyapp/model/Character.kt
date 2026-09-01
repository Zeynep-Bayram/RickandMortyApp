package com.example.rickandmortyapp.model

// API'den gelen tek bir karakterin modeli
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: CharacterLocation,
    val location: CharacterLocation,
    val image: String
)

data class CharacterLocation(
    val name: String,
    val url: String
)