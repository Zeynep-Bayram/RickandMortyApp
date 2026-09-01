package com.example.rickandmortyapp.api

import com.example.rickandmortyapp.model.CharacterResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface CharacterApi {
    @GET("character")
    suspend fun getCharacters(): CharacterResponse

    @GET("character")
    suspend fun searchCharacters(
        @Query("name")name: String
    ): CharacterResponse

}