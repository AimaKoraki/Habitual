package com.aima.habitual.ui.components

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext

/**
 * Decode a bitmap from app/src/main/assets/<assetPath> once per path and wrap it
 * in a [BitmapPainter]. Returns null if the asset is missing, so callers can
 * fall back to a placeholder for not-yet-shipped sprite folders.
 */
@Composable
fun rememberAssetPainter(assetPath: String): Painter? {
    val context = LocalContext.current
    val bitmap: ImageBitmap? = remember(assetPath) {
        runCatching {
            context.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream).asImageBitmap()
            }
        }.getOrNull()
    }
    return bitmap?.let { BitmapPainter(it) }
}
