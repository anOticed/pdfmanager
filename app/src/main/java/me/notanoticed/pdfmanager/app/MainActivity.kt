/**
 * Entry point Activity.
 *
 * This Activity configures edge-to-edge system bars and hosts the entire Compose UI.
 * It also installs app-wide CompositionLocals (toasts, pickers, preview overlay) before
 * rendering the main App() composable.
 */

package me.notanoticed.pdfmanager.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import me.notanoticed.pdfmanager.core.pickers.ProvidePickers
import me.notanoticed.pdfmanager.core.toast.ProvideToasts
import me.notanoticed.pdfmanager.feature.preview.ProvidePreview

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProvideToasts {
                ProvidePickers {
                    ProvidePreview{
                        App()
                    }
                }
            }
        }
    }
}
