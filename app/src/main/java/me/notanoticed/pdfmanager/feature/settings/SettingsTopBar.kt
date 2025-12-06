package me.notanoticed.pdfmanager.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- TOP BAR -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar() {
    Column {
        TopAppBar(
            title = {
                Column {
                    Text(text = "Settings", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = "Customize your experience", fontSize = 12.sp, color = Colors.textMutedColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Colors.cardColor,
                titleContentColor = Colors.textMainColor,
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Gray.copy(alpha = 0.2f)) // TODO: change
        )
    }
}
/* ------------------------------------------------- */