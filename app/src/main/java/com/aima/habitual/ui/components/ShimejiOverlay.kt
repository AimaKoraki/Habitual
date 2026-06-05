package com.aima.habitual.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.CompanionMetadata
import com.aima.habitual.model.VirtualCompanion
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Walking-companion overlay (Shimeji-style).
 *
 * Behaviour is driven entirely by the companion's metadata.json:
 *  - Walks across the screen using walkFrames.
 *  - On hitting an edge: plays turnFrames (if any — e.g. Marshall Lee's walk_4),
 *    flips direction, then optionally idles for a random duration using idleFrames.
 *  - idleChanceOnEdge and idle duration range are per-companion.
 *
 * All sprites face LEFT in their source PNGs. The overlay flips scaleX when moving
 * right so the sprite always faces its direction of travel.
 *
 * Touch pass-through: no Modifier.clickable / pointerInput installed — taps fall
 * through to BottomNavigationBar below.
 */
@Composable
fun ShimejiOverlay(
    activeCompanion: VirtualCompanion?,
    modifier: Modifier = Modifier
) {
    // ── Tokens ─────────────────────────────────
    val spriteSize = 48.dp
    val bandHeight = 56.dp
    val walkSpeedDpPerSec = 60f
    val tickMs = 16L
    // ────────────────────────────────────────────

    if (activeCompanion == null) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val spriteSizePx = with(density) { spriteSize.toPx() }
    val walkSpeedPxPerMs = with(density) { walkSpeedDpPerSec.dp.toPx() } / 1000f

    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var xPx by remember { mutableFloatStateOf(0f) }
    var facingRight by remember { mutableStateOf(false) }
    var currentFramePath by remember {
        mutableStateOf("companions/${activeCompanion.spriteAsset}/walk_0.png")
    }

    // Load the bitmap whenever the frame path changes.
    val bitmap: ImageBitmap? = remember(currentFramePath) {
        runCatching {
            context.assets.open(currentFramePath).use {
                BitmapFactory.decodeStream(it).asImageBitmap()
            }
        }.getOrNull()
    }

    LaunchedEffect(activeCompanion.spriteAsset) {
        // Load this companion's metadata from assets (IO dispatcher, tiny file).
        val meta: CompanionMetadata = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("companions/${activeCompanion.spriteAsset}/metadata.json")
                    .use { Gson().fromJson(it.bufferedReader(), CompanionMetadata::class.java) }
            }.getOrElse { CompanionMetadata() }
        }

        val base = "companions/${activeCompanion.spriteAsset}"

        fun walkPath(frame: String) = "$base/$frame"

        // Helpers for sequential frame playback used during edge transitions.
        suspend fun playTurnFrames() {
            for (frame in meta.turnFrames) {
                currentFramePath = walkPath(frame)
                delay(meta.walkFrameDurationMs)
            }
        }

        suspend fun playIdleLoop() {
            val idleDuration = Random.nextLong(meta.idleMinMs, meta.idleMaxMs)
            val idleEnd = System.currentTimeMillis() + idleDuration
            var idx = 0
            while (System.currentTimeMillis() < idleEnd) {
                currentFramePath = walkPath(meta.idleFrames[idx % meta.idleFrames.size])
                delay(meta.idleFrameDurationMs)
                idx++
            }
        }

        // Set initial walk frame.
        currentFramePath = walkPath(meta.walkFrames[0])
        var walkFrameIdx = 0
        var walkFrameTimer = 0L

        while (true) {
            delay(tickMs)
            val travel = (containerWidthPx - spriteSizePx).coerceAtLeast(0f)
            if (travel == 0f) continue

            // Advance position.
            val step = walkSpeedPxPerMs * tickMs
            xPx = (xPx + (if (facingRight) step else -step)).coerceIn(0f, travel)

            // Advance walk frame on timer.
            walkFrameTimer += tickMs
            if (walkFrameTimer >= meta.walkFrameDurationMs) {
                walkFrameTimer = 0L
                walkFrameIdx = if (meta.walkHoldOnLastFrame) {
                    // Single pass: advance up to the last frame and hold there.
                    // Index resets to 0 on each edge hit (start of next walk pass).
                    (walkFrameIdx + 1).coerceAtMost(meta.walkFrames.size - 1)
                } else {
                    // Normal loop: wrap back to walk_0 after the last frame.
                    (walkFrameIdx + 1) % meta.walkFrames.size
                }
                currentFramePath = walkPath(meta.walkFrames[walkFrameIdx])
            }

            // Edge hit handling: turn → flip → maybe idle → resume walk.
            if (xPx <= 0f || xPx >= travel) {
                // Play turn animation (e.g. Marshall Lee's walk_4) before flipping.
                if (meta.turnFrames.isNotEmpty()) {
                    playTurnFrames()
                }

                facingRight = !facingRight

                // Random idle pause after the turn.
                if (meta.idleFrames.isNotEmpty() && Random.nextFloat() < meta.idleChanceOnEdge) {
                    playIdleLoop()
                }

                // Back to first walk frame after any pause.
                walkFrameIdx = 0
                walkFrameTimer = 0L
                currentFramePath = walkPath(meta.walkFrames[0])
            }
        }
    }

    if (bitmap == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bandHeight)
            .onSizeChanged { containerWidthPx = it.width.toFloat() }
        // No clickable / pointerInput — taps fall through to BottomNav below.
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .size(spriteSize)
                .offset { IntOffset(xPx.toInt(), 0) }
                .graphicsLayer {
                    // Source PNGs face left. Flip when moving right.
                    scaleX = if (facingRight) -1f else 1f
                }
        )
    }
}
