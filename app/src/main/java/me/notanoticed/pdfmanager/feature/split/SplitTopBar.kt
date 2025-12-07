package me.notanoticed.pdfmanager.feature.split

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun SplitTopBar(
    viewModel: SplitViewModel,
    onAddClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val isActive = viewModel.selectedSplitPdf != null
    val fileName = viewModel.selectedSplitPdf?.name ?: ""

    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Split PDF",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = if (isActive) fileName else "No file selected",
                        fontSize = 12.sp,
                        color = Colors.Text.secondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            },
            actions = {
                Button(
                    onClick = onAddClick  /* TODO: SAF picker */ ,
                    colors = ButtonDefaults.buttonColors(containerColor = Colors.Button.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "Change",
                        tint = Colors.Icon.white
                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    Text(
                        text = if (isActive) "Change" else "Select PDF",
                        color = Colors.Text.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isActive) {
                    FilledIconButton(
                        onClick = onCloseClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledIconButtonColors().copy(
                            containerColor = Colors.Button.iconBackground,
                            contentColor = Colors.Icon.white
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close File",
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Colors.Surface.card,
                titleContentColor = Colors.Text.primary,
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Colors.Border.subtle)
        )
    }

}
/* ------------------------------------------------- */