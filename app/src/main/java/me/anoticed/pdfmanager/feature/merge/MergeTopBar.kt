package me.anoticed.pdfmanager.feature.merge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.anoticed.pdfmanager.ui.theme.Colors

/* -------------------- TOP BAR -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeTopBar(
    total: Int,
    isActive: Boolean,
    onAddClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Column {
                    Text(text = "Merge PDFs", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = "$total files selected", fontSize = 12.sp, color = Colors.textMutedColor)
                }
            },
            actions = {
                Button(
                    onClick = onAddClick  /* TODO: SAF picker */ ,
                    colors = ButtonDefaults.buttonColors(containerColor = Colors.blueColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add PDFs", tint = Color.White)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = if (isActive) "Change" else "Add PDFs", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
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