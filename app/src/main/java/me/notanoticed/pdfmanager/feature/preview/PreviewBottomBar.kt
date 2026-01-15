/**
 * Bottom bar used inside the preview overlay.
 *
 * Currently kept as a placeholder to match the app scaffold structure.
 */

package me.notanoticed.pdfmanager.feature.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- BOTTOM BAR -------------------- */
@Composable
fun PreviewBottomBar(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Colors.Surface.card)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Colors.Border.subtle)
        )

        Box(modifier = Modifier.height(0.dp))
    }
}
/* ---------------------------------------------------- */