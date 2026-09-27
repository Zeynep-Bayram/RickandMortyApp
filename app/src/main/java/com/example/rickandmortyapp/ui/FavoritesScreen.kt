package com.example.rickandmortyapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.rickandmortyapp.model.FavoriteCharacter
import com.example.rickandmortyapp.viewmodel.CharacterViewModel

@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.favoritesLoading.collectAsState()
    val favoriteError by viewModel.favoriteError.collectAsState()
    val showSnackbar = rememberShowSnackbar()

    val backgroundColor = Color(0xFF16181F)
    val portalGreen = Color(0xFF329F5B)
    val cardBackgroundColor = Color(0xFFE4E6B4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        PortalTopBar(
            title = "Favorites",
            onBackClick = onBackClick
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = portalGreen)
            }

            favoriteError != null && favorites.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = favoriteError.orEmpty(),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            favorites.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorites yet.\nTap the heart on a character to add one.",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            else -> Column {
                favoriteError?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFFF8A80),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(
                        items = favorites,
                        key = { favorite -> favorite.characterId }
                    ) { favorite ->
                        FavoriteRow(
                            favorite = favorite,
                            cardBackgroundColor = cardBackgroundColor,
                            onRemoveClick = {
                                viewModel.removeFavorite(favorite.characterId)
                                showSnackbar("Removed from favorites")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: FavoriteCharacter,
    cardBackgroundColor: Color,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = favorite.image,
                contentDescription = favorite.name,
                modifier = Modifier
                    .size(88.dp)
                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = favorite.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = "Status: ${favorite.status}",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "Species: ${favorite.species}",
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = Color(0xFFE53935)
                )
            }
        }
    }
}
