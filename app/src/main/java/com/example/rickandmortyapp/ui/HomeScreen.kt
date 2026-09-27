package com.example.rickandmortyapp.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.rickandmortyapp.R
import com.example.rickandmortyapp.profile.AvatarCatalog
import com.example.rickandmortyapp.viewmodel.ProfileViewModel
import kotlin.math.roundToInt

private val ReadableWhiteShadow = Shadow(
    color = Color.Black.copy(alpha = 0.85f),
    offset = Offset(0f, 2f),
    blurRadius = 10f
)

@Composable
fun HomeScreen(
    onCharactersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val portalGreen = Color(0xFF35D66F)
    val profile by viewModel.profile.collectAsState()
    val avatarUrl = AvatarCatalog.imageUrl(profile.avatarCharacterId)
    var showInfoDialog by remember { mutableStateOf(false) }
    val portalPulse = rememberInfiniteTransition(label = "portalPulse")
    val portalScale by portalPulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portalScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_home_space),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.45f),
                            0.28f to Color.Black.copy(alpha = 0.2f),
                            0.55f to Color.Black.copy(alpha = 0.35f),
                            1.0f to Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fitHomeContent()
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 8.dp)
            ) {
                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = Color(0xFFD4E190),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Text(
                        text = "RICK AND MORTY",
                        color = portalGreen,
                        fontSize = 30.sp,
                        fontFamily = getSchwiftyFont,
                        style = TextStyle(shadow = ReadableWhiteShadow)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "EXPLORE THE MULTIVERSE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        style = TextStyle(shadow = ReadableWhiteShadow)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .background(portalGreen)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(56.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 36.dp),
                            onClick = onProfileClick
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, portalGreen, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color(0xFFD4E190),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(shadow = ReadableWhiteShadow)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    // Move the portal and its lettering together toward the Characters image.
                    .offset(y = 40.dp)
                    .scale(portalScale)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.btn_characters),
                    contentDescription = "Portal",
                    // Align the visible ring, which is off-center inside the artwork.
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (-8).dp),
                    contentScale = ContentScale.Fit
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = 4.dp)
                ) {
                    Text(
                        text = "WHERE\nDO YOU WANT\nTO GO?",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontFamily = getSchwiftyFont,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp,
                        style = TextStyle(shadow = ReadableWhiteShadow)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(2.dp)
                            .background(Color(0xFF9CFF57))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            NavigationOrb(
                imageResId = R.drawable.orb_characters,
                contentDescription = "Characters",
                label = "CHARACTERS",
                subtitle = "MEET THE\nRESIDENTS",
                onClick = onCharactersClick
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = onFavoritesClick,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Favorites",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\"INFINITE UNIVERSES.\nINFINITE POSSIBILITIES.\"",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.2.sp,
                    lineHeight = 20.sp,
                    style = TextStyle(shadow = ReadableWhiteShadow)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(2.dp)
                        .background(Color(0xFFD4E190))
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = Color(0xFF232733),
            titleContentColor = portalGreen,
            textContentColor = Color(0xFFE4E8EF),
            tonalElevation = 0.dp,
            title = {
                Text(
                    text = "About",
                    fontFamily = getSchwiftyFont
                )
            },
            text = {
                Text(
                    text = "Browse Rick and Morty characters via the public Rick and Morty API. More portals (episodes & locations) coming soon.",
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Wubba Lubba Dub Dub", color = portalGreen)
                }
            }
        )
    }
}

private fun Modifier.fitHomeContent(): Modifier = layout { measurable, constraints ->
    // Measure the same design on every device before fitting it into the safe viewport.
    val designWidth = 412.dp.roundToPx()
    val content = measurable.measure(Constraints.fixedWidth(designWidth))
    val scale = minOf(
        1f,
        constraints.maxWidth.toFloat() / content.width.coerceAtLeast(1),
        constraints.maxHeight.toFloat() / content.height.coerceAtLeast(1)
    )
    val left = ((constraints.maxWidth - content.width * scale) / 2f).roundToInt()

    layout(constraints.maxWidth, constraints.maxHeight) {
        content.placeWithLayer(x = left, y = 0) {
            transformOrigin = TransformOrigin(0f, 0f)
            scaleX = scale
            scaleY = scale
        }
    }
}

@Composable
private fun NavigationOrb(
    imageResId: Int,
    contentDescription: String,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    orbSize: Dp = 100.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pulse = rememberInfiniteTransition(label = "orbPulse-$label")
    val orbScale by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbScale-$label"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 78.dp),
                onClick = onClick
            )
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(orbSize + 8.dp)
                .scale(orbScale)
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(orbSize)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF35D66F))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            lineHeight = 16.sp,
            style = TextStyle(shadow = ReadableWhiteShadow)
        )
    }
}
