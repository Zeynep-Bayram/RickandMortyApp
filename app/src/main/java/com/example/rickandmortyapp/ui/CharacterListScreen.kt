package com.example.rickandmortyapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rickandmortyapp.viewmodel.CharacterUiState
import com.example.rickandmortyapp.viewmodel.CharacterViewModel
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel = viewModel()

) {
    LaunchedEffect(
        Unit
    ) {
        viewModel.fetchCharacters()
    }
    val uiState = viewModel.uiState.value
    var searchText by rememberSaveable() {
        mutableStateOf("")
    }

    when (uiState) {
        is CharacterUiState.Error -> Box(
            modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Column() {
                Text(uiState.message)
                Button(
                    onClick = { viewModel.fetchCharacters() }) {
                    Text("Tekrar dene")

                }
            }
        }

        CharacterUiState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center

        ) {
            CircularProgressIndicator()
        }

        is CharacterUiState.Success -> Column {
            TopAppBar(
                title = {
                    Text("Rick and Morty Characters")
                })
            Row() {
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),

                    value = searchText, onValueChange = { searchText = it }, label = {
                        Text("Search characters")
                    })
                IconButton(onClick = {
                    viewModel.searchCharacters(searchText)
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }

            }



            LazyColumn(
                modifier = Modifier.padding(top = 32.dp)

            ) {
                items(uiState.characters) { character ->
                    var isFlipped by remember { mutableStateOf(false) }
                    val rotation by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(600),
                        label = "cardFlip"
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .clickable {
                                isFlipped = !isFlipped
                            }
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 8 * density
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isFlipped) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            rotationY = 180f
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CompositionLocalProvider(
                                        LocalTextStyle provides LocalTextStyle.current.copy(
                                            fontSize = 18.sp
                                        )
                                    ) {

                                        Text("DETAILS")
                                        Text("Status: ${character.status}")
                                        Text("Species: ${character.species}")
                                        Text("Gender: ${character.gender}")
                                        Text("Type: ${character.type}")
                                        Text("Origin: ${character.origin.name}")
                                        Text("Location: ${character.location.name}")
                                    }
                                }
                            } else {

                                AsyncImage(
                                    model = character.image,
                                    contentDescription = character.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.2f),
                                    contentScale = ContentScale.Fit


                                )

                                Text(
                                    fontSize = 22.sp,
                                    text = character.name,
                                    modifier = Modifier.padding(8.dp)
                                )


                            }
                        }
                    }
                }

            }
        }
    }
}


