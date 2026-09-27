package com.example.rickandmortyapp.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.rickandmortyapp.viewmodel.CharacterUiState
import com.example.rickandmortyapp.viewmodel.CharacterViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: CharacterViewModel = viewModel()
) {
    val uiState = viewModel.uiState.value
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val favoriteError by viewModel.favoriteError.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current


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
            title = "Characters",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            value = searchText,
            onValueChange = viewModel::onSearchTextChange,
            placeholder = {
                Text(
                    text = "Search the multiverse...",
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = portalGreen
                )
            },
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF232733),
                unfocusedContainerColor = Color(0xFF232733),
                focusedBorderColor = portalGreen,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = portalGreen
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        favoriteError?.let { message ->
            Text(
                text = message,
                color = Color(0xFFFF8A80),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        when (uiState) {
            is CharacterUiState.Empty -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = (-85).dp)
                ) {
                    Text("No characters found", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            viewModel.resetSearch()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = portalGreen)
                    ) {
                        Text("Back to search", color = Color.White)
                    }
                }
            }

            is CharacterUiState.Error -> Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(
                        uiState.message,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.retry()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = portalGreen)
                    ) {
                        Text("Try again", color = Color.White)
                    }
                }
            }

            CharacterUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = portalGreen)
            }

            is CharacterUiState.Success -> Column {
                LaunchedEffect(listState) {
                    snapshotFlow {
                        listState.isScrollInProgress
                    }.collectLatest { isScrolling ->
                        if (isScrolling) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                }

                LaunchedEffect(listState, uiState.characters.size) {
                    snapshotFlow {
                        listState.layoutInfo.visibleItemsInfo
                    }.collectLatest { visibleItems ->
                        val lastVisibleItem = visibleItems.lastOrNull()?.index
                        if (
                            lastVisibleItem != null &&
                            lastVisibleItem >= uiState.characters.size - 3
                        ) {
                            viewModel.loadNextPage()
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.padding(top = 8.dp),
                    state = listState
                ) {
                    items(
                        items = uiState.characters,
                        key = { character -> character.id }
                    ) { character ->
                        val isFavorite = character.id in favoriteIds
                        var isFlipped by rememberSaveable(character.id) { mutableStateOf(false) }
                        val rotation = animateFloatAsState(
                            targetValue = if (isFlipped) 180f else 0f,
                            animationSpec = tween(
                                durationMillis = 450,
                                easing = FastOutSlowInEasing
                            ),
                            label = "cardFlip"
                        )
                        val showBack by remember(rotation) {
                            derivedStateOf { rotation.value >= 90f }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .graphicsLayer {
                                    // Switch faces edge-on and keep the visible face readable.
                                    rotationY = if (showBack) rotation.value - 180f else rotation.value
                                    cameraDistance = size.width * 4f
                                }
                                .clickable {
                                    isFlipped = !isFlipped
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = cardBackgroundColor
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (showBack) {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                AsyncImage(
                                                    model = character.image,
                                                    contentDescription = "${character.name} thumbnail",
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(CircleShape)
                                                        .border(2.dp, Color.Black, CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Text(
                                                    text = "ID DETAILS",
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.Black
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(24.dp))

                                            CompositionLocalProvider(
                                                LocalTextStyle provides LocalTextStyle.current.copy(
                                                    fontSize = 18.sp,
                                                    color = Color.DarkGray
                                                )
                                            ) {
                                                Text("Type: ${character.type.ifEmpty { "Unknown" }}")
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Origin: ${character.origin.name}")
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Location: ${character.location.name}")
                                            }
                                        }

                                        FavoriteHeartButton(
                                            isFavorite = isFavorite,
                                            onClick = { viewModel.toggleFavorite(character) },
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                                    ) {
                                        Text(
                                            text = "Portal License",
                                            fontSize = 28.sp,
                                            color = portalGreen,
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .padding(end = 36.dp),
                                            fontFamily = getSchwiftyFont
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = character.image,
                                                contentDescription = character.name,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(0.8f)
                                                    .padding(end = 12.dp)
                                                    .border(
                                                        width = 2.dp,
                                                        color = Color.Black,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )

                                            Column(
                                                modifier = Modifier.weight(1.5f),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = character.name,
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.Black
                                                )

                                                Text(
                                                    text = "Status: ${character.status}",
                                                    color = Color.Black,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    text = "Species: ${character.species}",
                                                    color = Color.Black,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    text = "Gender: ${character.gender}",
                                                    color = Color.Black,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }

                                    FavoriteHeartButton(
                                        isFavorite = isFavorite,
                                        onClick = { viewModel.toggleFavorite(character) },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                    }
                    if (uiState.isLoadingNextPage) {
                        item(key = "loadingNextPage") {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = portalGreen)
                            }
                        }
                    }
                    uiState.nextPageError?.let { message ->
                        item(key = "nextPageError") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(message, color = Color.White, textAlign = TextAlign.Center)
                                Button(onClick = viewModel::retry) {
                                    Text("Try again")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteHeartButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showSnackbar = rememberShowSnackbar()
    IconButton(
        onClick = {
            onClick()
            showSnackbar(
                if (isFavorite) "Removed from favorites" else "Added to favorites"
            )
        },
        modifier = modifier.padding(4.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) {
                Icons.Filled.Favorite
            } else {
                Icons.Outlined.FavoriteBorder
            },
            contentDescription = if (isFavorite) {
                "Remove from favorites"
            } else {
                "Add to favorites"
            },
            tint = if (isFavorite) Color(0xFFE53935) else Color.Black
        )
    }
}
