package com.example.rickandmortyapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.rickandmortyapp.profile.AvatarCatalog
import com.example.rickandmortyapp.profile.AvatarOption
import com.example.rickandmortyapp.viewmodel.ProfileViewModel

private val PortalGreen = Color(0xFF35D66F)
private val ScreenBackground = Color(0xFF16181F)
private val AvatarDiameter = 64.dp
private val AvatarCellWidth = 72.dp

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val focusManager = LocalFocusManager.current
    var displayName by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableStateOf<Int?>(null) }
    var originalName by remember { mutableStateOf("") }
    var originalAvatarId by remember { mutableStateOf<Int?>(null) }
    var didInitialize by remember { mutableStateOf(false) }

    LaunchedEffect(profile.displayName, profile.avatarCharacterId) {
        if (!didInitialize && (profile.displayName.isNotBlank() || profile.avatarCharacterId != null)) {
            displayName = profile.displayName
            selectedAvatarId = profile.avatarCharacterId
            originalName = profile.displayName
            originalAvatarId = profile.avatarCharacterId
            didInitialize = true
        }
    }

    val showSnackbar = rememberShowSnackbar()

    fun commitAndLeave() {
        focusManager.clearFocus()
        val nameChanged = displayName.trim() != originalName.trim()
        val avatarChanged = selectedAvatarId != originalAvatarId
        if (nameChanged || avatarChanged) {
            viewModel.saveProfile(displayName, selectedAvatarId)
            showSnackbar("Profile updated")
        }
        onBackClick()
    }

    BackHandler { commitAndLeave() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        PortalTopBar(title = "Edit Profile", onBackClick = { commitAndLeave() })
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Username",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            placeholder = {
                Text("Enter a username", color = Color.Gray)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White,
                focusedContainerColor = Color(0xFF232733),
                unfocusedContainerColor = Color(0xFF232733),
                disabledContainerColor = Color(0xFF232733),
                focusedBorderColor = PortalGreen,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                cursorColor = PortalGreen
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Avatar",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Tap a character to select",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 12.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 4
            ) {
                AvatarCatalog.options.forEach { option ->
                    AvatarChoice(
                        option = option,
                        selected = option.characterId == selectedAvatarId,
                        onClick = { selectedAvatarId = option.characterId }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AvatarChoice(
    option: AvatarOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(AvatarCellWidth)
            .padding(4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 40.dp),
                onClick = onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.requiredSize(AvatarDiameter)
        ) {
            AsyncImage(
                model = option.imageUrl,
                contentDescription = option.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF232733))
                    .border(
                        width = 2.dp,
                        color = if (selected) PortalGreen else Color.White.copy(alpha = 0.45f),
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = option.name,
            color = if (selected) PortalGreen else Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            lineHeight = 12.sp,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
