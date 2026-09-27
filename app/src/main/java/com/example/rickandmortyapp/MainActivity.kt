package com.example.rickandmortyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.example.rickandmortyapp.api.RetrofitInstance
import com.example.rickandmortyapp.auth.AuthRepository
import com.example.rickandmortyapp.ui.CharacterListScreen
import com.example.rickandmortyapp.ui.EditProfileScreen
import com.example.rickandmortyapp.ui.FavoritesScreen
import com.example.rickandmortyapp.ui.HomeScreen
import com.example.rickandmortyapp.ui.LoginScreen
import com.example.rickandmortyapp.ui.LocalShowSnackbar
import com.example.rickandmortyapp.ui.PortalNoticeHost
import com.example.rickandmortyapp.ui.ProfileScreen
import com.example.rickandmortyapp.ui.rememberAppSnackbarState
import com.example.rickandmortyapp.ui.theme.RickAndMortyAppTheme
import com.example.rickandmortyapp.viewmodel.CharacterViewModel

private sealed class AppScreen {
    data object Login : AppScreen()
    data object Home : AppScreen()
    data object Characters : AppScreen()
    data object Favorites : AppScreen()
    data object Profile : AppScreen()
    data object EditProfile : AppScreen()
}

class MainActivity : ComponentActivity() {
    private val characterViewModel: CharacterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            setSingletonImageLoaderFactory { context ->
                ImageLoader.Builder(context)
                    .components {
                        add(
                            OkHttpNetworkFetcherFactory(
                                callFactory = { RetrofitInstance.unsafeOkHttpClient }
                            )
                        )
                    }
                    .build()
            }

            RickAndMortyAppTheme {
                var selectedScreen by remember {
                    mutableStateOf(
                        if (AuthRepository.isSignedIn) AppScreen.Home else AppScreen.Login
                    )
                }

                LaunchedEffect(Unit) {
                    AuthRepository.authState.collect { user ->
                        if (user != null && selectedScreen == AppScreen.Login) {
                            selectedScreen = AppScreen.Home
                        } else if (user == null && selectedScreen != AppScreen.Login) {
                            selectedScreen = AppScreen.Login
                        }
                    }
                }

                BackHandler(
                    enabled = selectedScreen == AppScreen.Characters ||
                        selectedScreen == AppScreen.Favorites ||
                        selectedScreen == AppScreen.Profile
                ) {
                    selectedScreen = AppScreen.Home
                }

                val (snackbarHostState, showSnackbar) = rememberAppSnackbarState()

                CompositionLocalProvider(LocalShowSnackbar provides showSnackbar) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF0C1217),
                    snackbarHost = {
                        PortalNoticeHost(hostState = snackbarHostState)
                    }
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = selectedScreen,
                        transitionSpec = {
                            val enteringFromHome =
                                initialState == AppScreen.Home &&
                                    (targetState == AppScreen.Characters ||
                                        targetState == AppScreen.Favorites ||
                                        targetState == AppScreen.Profile)
                            val returningHome =
                                (initialState == AppScreen.Characters ||
                                    initialState == AppScreen.Favorites ||
                                    initialState == AppScreen.Profile) &&
                                    targetState == AppScreen.Home

                            if (enteringFromHome) {
                                // Portal zoom-in: hedef ekran büyüyerek gelir
                                (
                                    fadeIn(tween(450, easing = FastOutSlowInEasing)) +
                                        scaleIn(
                                            initialScale = 0.35f,
                                            animationSpec = tween(550, easing = FastOutSlowInEasing)
                                        )
                                    ) togetherWith (
                                    fadeOut(tween(280)) +
                                        scaleOut(
                                            targetScale = 1.35f,
                                            animationSpec = tween(400)
                                        )
                                    )
                            } else if (returningHome) {
                                // Portal zoom-out: ana sayfa küçülüp açılır
                                (
                                    fadeIn(tween(400)) +
                                        scaleIn(
                                            initialScale = 1.25f,
                                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                                        )
                                    ) togetherWith (
                                    fadeOut(tween(350)) +
                                        scaleOut(
                                            targetScale = 0.4f,
                                            animationSpec = tween(450)
                                        )
                                    )
                            } else {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                            }
                        },
                        label = "portalNavigation"
                    ) { screen ->
                        when (screen) {
                            AppScreen.Login -> LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSignedIn = { selectedScreen = AppScreen.Home }
                            )

                            AppScreen.Home -> HomeScreen(
                                onCharactersClick = {
                                    characterViewModel.resetSearch()
                                    selectedScreen = AppScreen.Characters
                                },
                                onFavoritesClick = { selectedScreen = AppScreen.Favorites },
                                onProfileClick = { selectedScreen = AppScreen.Profile }
                            )

                            AppScreen.Characters -> CharacterListScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackClick = { selectedScreen = AppScreen.Home },
                                viewModel = characterViewModel
                            )

                            AppScreen.Favorites -> FavoritesScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackClick = { selectedScreen = AppScreen.Home }
                            )

                            AppScreen.Profile -> ProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackClick = { selectedScreen = AppScreen.Home },
                                onEditClick = { selectedScreen = AppScreen.EditProfile },
                                onFavoritesClick = { selectedScreen = AppScreen.Favorites }
                            )

                            AppScreen.EditProfile -> EditProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackClick = { selectedScreen = AppScreen.Profile }
                            )
                        }
                    }
                }
                }
            }
        }
    }
}
