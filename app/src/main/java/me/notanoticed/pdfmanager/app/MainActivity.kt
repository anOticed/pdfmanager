/**
 * Entry point Activity.
 *
 * This Activity configures edge-to-edge system bars and hosts the entire Compose UI.
 * It also installs app-wide CompositionLocals (toasts, pickers, preview overlay) before
 * rendering the main App() composable.
 */

package me.notanoticed.pdfmanager.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import me.notanoticed.pdfmanager.core.pickers.ProvidePickers
import me.notanoticed.pdfmanager.core.toast.ProvideToasts
import me.notanoticed.pdfmanager.feature.preview.ProvidePreview

class MainActivity : AppCompatActivity() {
    private var incomingPdfIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingPdfIntent.value = if (savedInstanceState == null) intent else null
        enableEdgeToEdge()
        setContent {
            ProvideToasts {
                ProvidePickers {
                    ProvidePreview{
                        App(
                            externalIntent = incomingPdfIntent.value
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingPdfIntent.value = intent
    }
}
