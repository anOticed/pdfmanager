package me.notanoticed.pdfmanager.core.pdf.util

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import java.util.concurrent.atomic.AtomicBoolean

private val isPdfBoxInitialized = AtomicBoolean(false)

fun ensurePdfBoxInitialized(context: Context) {
    if (isPdfBoxInitialized.get()) return

    synchronized(isPdfBoxInitialized) {
        if (isPdfBoxInitialized.get()) return
        PDFBoxResourceLoader.init(context.applicationContext)
        isPdfBoxInitialized.set(true)
    }
}
