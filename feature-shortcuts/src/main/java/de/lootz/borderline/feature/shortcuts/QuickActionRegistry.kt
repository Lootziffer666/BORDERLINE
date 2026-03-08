package de.lootz.borderline.feature.shortcuts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import de.lootz.borderline.core.BorderlineLogger

class QuickActionRegistry(private val context: Context) {
    fun actions(): List<QuickAction> = listOf(
        QuickAction(
            id = "copy_package",
            label = "Paket kopieren",
            description = "Kopiert den aktuellen Paketnamen in die Zwischenablage."
        ) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("borderline", "de.lootz.borderline"))
            Toast.makeText(context, "Paketname kopiert", Toast.LENGTH_SHORT).show()
            "Paketname kopiert"
        },
        QuickAction(
            id = "ping_overlay",
            label = "Overlay Ping",
            description = "Testet die Overlay-Antwortkette."
        ) {
            BorderlineLogger.i("Overlay ping action executed")
            Toast.makeText(context, "Overlay lebt noch. Schön gruselig.", Toast.LENGTH_SHORT).show()
            "Overlay-Ping erfolgreich"
        }
    )
}
