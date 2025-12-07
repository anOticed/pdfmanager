package me.notanoticed.pdfmanager.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMerge
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- BOTTOM BAR -------------------- */
data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun AppBottomBar(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Colors.Border.subtle)
        )
        NavigationBar(
            containerColor = Colors.Background.surface
        ) {
            val items = listOf(
                BottomNavItem(title = "PDFs", route = Screen.PdfList.route, icon = Icons.Outlined.Description),
                BottomNavItem(title = "Merge", route = Screen.Merge.route, icon = Icons.AutoMirrored.Outlined.CallMerge),
                BottomNavItem(title = "Split", route = Screen.Split.route, icon = Icons.AutoMirrored.Outlined.CallSplit),
                BottomNavItem(title = "Images", route = Screen.Images.route, icon = Icons.Outlined.Image),
                BottomNavItem(title = "Settings", route = Screen.Settings.route, icon = Icons.Outlined.Settings)
            )

            items.forEachIndexed { index, item ->

                NavigationBarItem(
                    selected = currentRoute == appScreens[index],
                    onClick = { onItemClick(appScreens[index]) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                    label = { Text(text = item.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Colors.Icon.blue,
                        selectedTextColor = Colors.Text.blue,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Colors.Icon.default,
                        unselectedTextColor = Colors.Text.secondary
                    )
                )
            }
        }
    }
}
/* ---------------------------------------------------- */