package com.example.pdfmanager.feature.pdflist

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdfmanager.app.AppBottomBar
import com.example.pdfmanager.app.Screen
import com.example.pdfmanager.ui.theme.Colors
import com.example.pdfmanager.ui.theme.PdfManagerTheme

/* -------------------- ACTIVITY -------------------- */
@Composable
fun PdfListActivity(active: Screen, onSelect: (String) -> Unit) {
    Scaffold(
        topBar = {
            PdfListTopBar(totalDocuments = 0)
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = active.route,
                onItemClick = onSelect
            )
        },
        containerColor = Colors.backgroundColor
    ) { paddingValues ->
        PdfListScreen(
            Modifier.padding(paddingValues),
            viewModel = PdfListViewModel()
        )
    }
}
/* -------------------------------------------------- */











/* --------------------  -------------------- */
data class Document(
    val name: String,
    val meta: String,
    val time: String,
    val bitmap: Bitmap? = null,
    val locked: Boolean = false
)

@Composable
fun DocumentCard(
    document: Document,
    onMoreClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Colors.cardColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(76.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A2F37)), // TODO: change
                    contentAlignment = Alignment.Center
                ) {
                    if (document.bitmap == null) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "PDF icon",
                            tint = Color(0xFF7D8592), // TODO: change
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    else {
                        Image(
                            bitmap = document.bitmap.asImageBitmap(),
                            contentDescription = "PDF preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(scaleX = 1.2f, scaleY = 1.2f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = document.name,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = document.meta,
                        color = Colors.textMutedColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = document.time,
                        color = Colors.textMutedColor,
                        fontSize = 11.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (document.locked) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Locked icon",
                            tint = Color(0xFFFFB74D), // TODO: change
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(onClick = onMoreClick /* TODO: options menu */ ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "More icon",
                            tint = Colors.textMutedColor
                        )
                    }
                }
            }
        }
    }
}
/* ---------------------------------------------- */



@Preview(showBackground = true)
@Composable
fun PdfListActivityPreview() {
    PdfManagerTheme {
        PdfListActivity(active = Screen.PdfList, onSelect = {})
//        DocumentCard(Document("test.pdf", "meta", "12.12.2023"))
    }
}