package com.example.rickandmortyapp.profile

data class AvatarOption(
    val characterId: Int,
    val name: String
) {
    val imageUrl: String
        get() = "https://rickandmortyapi.com/api/character/avatar/$characterId.jpeg"
}

object AvatarCatalog {
    val options: List<AvatarOption> = listOf(
        AvatarOption(1, "Rick Sanchez"),
        AvatarOption(2, "Morty Smith"),
        AvatarOption(3, "Summer Smith"),
        AvatarOption(4, "Beth Smith"),
        AvatarOption(5, "Jerry Smith"),
        AvatarOption(242, "Mr. Meeseeks"),
        AvatarOption(47, "Birdperson"),
        AvatarOption(265, "Pickle Rick")
    )

    fun find(characterId: Int?): AvatarOption? =
        characterId?.let { id -> options.find { it.characterId == id } }

    fun imageUrl(characterId: Int?): String? = find(characterId)?.imageUrl
}
