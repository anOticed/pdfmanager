package me.notanoticed.pdfmanager.core.system.files

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object AppFileProvider {
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
