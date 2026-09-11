package com.example.rickandmortyapp.model


// API response içindeki karakter listesinin modeli
data class CharacterResponse(
    val results: List<Character>,
    val info: Info
)

data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)