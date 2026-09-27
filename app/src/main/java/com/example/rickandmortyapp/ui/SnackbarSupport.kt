package com.example.rickandmortyapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PortalGreen = Color(0xFF35D66F)
private val CardCream = Color(0xFFE4E6B4)
const val NoticeDurationMs = 4000L

val LocalShowSnackbar = staticCompositionLocalOf<(String) -> Unit> {
    error("Snackbar messenger not provided")
}

@Composable
fun rememberShowSnackbar(): (String) -> Unit = LocalShowSnackbar.current

fun createShowSnackbar(
    hostState: SnackbarHostState,
    scope: CoroutineScope
): (String) -> Unit = { message ->
    scope.launch {
        hostState.currentSnackbarData?.dismiss()
        val shown = launch {
            hostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite
            )
        }
        delay(NoticeDurationMs)
        hostState.currentSnackbarData?.dismiss()
        shown.cancel()
    }
}

@Composable
fun rememberAppSnackbarState(): Pair<SnackbarHostState, (String) -> Unit> {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showSnackbar = remember(hostState, scope) {
        createShowSnackbar(hostState, scope)
    }
    return hostState to showSnackbar
}

@Composable
fun PortalNoticeHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) { data ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardCream)
                .border(2.dp, PortalGreen, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .background(PortalGreen)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = data.visuals.message,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
