package me.notanoticed.pdfmanager.core.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.notanoticed.pdfmanager.ui.theme.Colors

typealias ToastMessage = (String) -> Unit

private val LocalToastHostState = staticCompositionLocalOf<SnackbarHostState> { error("ProvideToasts not provided") }

@Composable
fun ProvideToasts(
    content: @Composable () -> Unit
) {
    val hostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalToastHostState provides hostState) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            ToastHost(
                hostState = hostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 72.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            )
        }
    }
}

@Composable
fun rememberToast(): ToastMessage {
    val hostState = LocalToastHostState.current
    val scope = rememberCoroutineScope()

    return remember(hostState) {
        { message ->
            scope.launch {
                hostState.currentSnackbarData?.dismiss()
                hostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                    withDismissAction = false
                )
            }
        }
    }
}

@Composable
private fun ToastHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val data = hostState.currentSnackbarData

    var lastMessage by remember { mutableStateOf<String?>(null) }
    if (data != null) {
        lastMessage = data.visuals.message
    }

    LaunchedEffect(data) {
        val current = data ?: return@LaunchedEffect

        when (current.visuals.duration) {
            SnackbarDuration.Short -> delay(2500)
            SnackbarDuration.Long -> delay(4500)
            SnackbarDuration.Indefinite -> return@LaunchedEffect
        }

        current.dismiss()
    }

    AnimatedVisibility(
        visible = data != null,
        modifier = modifier,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) + scaleIn(initialScale = 0.98f),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut(targetScale = 0.98f)
    ) {
        val message = data?.visuals?.message ?: lastMessage ?: return@AnimatedVisibility
        AppToast(message = message)
    }
}

@Composable
private fun AppToast(message: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Colors.Surface.card,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Colors.Primary.lightBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Colors.Primary.blue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                color = Colors.Text.primary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 2.dp)
            )
        }
    }
}
