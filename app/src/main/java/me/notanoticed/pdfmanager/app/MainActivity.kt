package me.notanoticed.pdfmanager.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf

class MainActivity : AppCompatActivity() {
    private val pendingExternalIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingExternalIntent.value = if (savedInstanceState == null) intent else null
        enableEdgeToEdge()
        setContent {
            PdfManagerApp(
                externalIntent = pendingExternalIntent.value,
                onExternalIntentConsumed = { pendingExternalIntent.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingExternalIntent.value = intent
    }
}
