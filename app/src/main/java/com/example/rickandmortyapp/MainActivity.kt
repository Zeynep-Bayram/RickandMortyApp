package com.example.rickandmortyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.rickandmortyapp.ui.CharacterListScreen
import com.example.rickandmortyapp.ui.HomeScreen
import com.example.rickandmortyapp.ui.theme.RickAndMortyAppTheme

private sealed class AppScreen {
    data object Home : AppScreen()
    data object Characters : AppScreen()
}

class MainActivity : ComponentActivity() {
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
                var selectedScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

                BackHandler(enabled = selectedScreen != AppScreen.Home) {
                    selectedScreen = AppScreen.Home
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF0C1217)
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = selectedScreen,
                        transitionSpec = {
                            val enteringFromHome = initialState == AppScreen.Home
                            val returningHome = targetState == AppScreen.Home

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
                            AppScreen.Home -> HomeScreen(
                                onCharactersClick = { selectedScreen = AppScreen.Characters }
                            )

                            AppScreen.Characters -> CharacterListScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackClick = { selectedScreen = AppScreen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}
