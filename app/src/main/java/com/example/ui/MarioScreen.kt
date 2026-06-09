package com.example.ui

import com.example.R

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.foundation.focusable
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Retro Pixel Colors
val RedRetro = Color(0xFFE52521)
val BlueRetro = Color(0xFF002286)
val SkinRetro = Color(0xFFFDC391)
val BrownRetro = Color(0xFF904A00)
val YellowRetro = Color(0xFFFFD700)
val DarkRetro = Color(0xFF1E1E1E)
val LightBlueRetro = Color(0xFF5C94FC) // Standard sky color
val LightGreenRetro = Color(0xFF00A800)
val UndergroundBackground = Color(0xFF000000)
val CastleBackground = Color(0xFF280000)

// Helper Pixel Art Matrices
// 0: transparent, 1: red, 2: blue, 3: peach (skin), 4: brown, 5: gold/yellow, 6: dark-grey/black
val SmallMarioMatrix = arrayOf(
    intArrayOf(0,0,0,1,1,1,1,1,0,0,0),
    intArrayOf(0,0,1,1,1,1,1,1,1,1,1),
    intArrayOf(0,0,4,4,4,3,3,4,3,0,0),
    intArrayOf(0,4,3,4,3,3,3,4,3,3,0),
    intArrayOf(0,4,3,4,4,3,3,3,4,3,3),
    intArrayOf(0,4,4,3,3,3,3,4,4,0,0),
    intArrayOf(0,0,0,3,3,3,3,3,3,0,0),
    intArrayOf(0,0,1,1,2,1,1,1,0,0,0),
    intArrayOf(0,1,1,1,2,1,1,2,1,1,0),
    intArrayOf(1,1,1,1,2,2,2,2,1,1,1),
    intArrayOf(3,3,1,2,5,2,2,5,2,1,3),
    intArrayOf(3,3,3,2,2,2,2,2,2,3,3),
    intArrayOf(3,3,2,2,2,2,2,2,2,2,3),
    intArrayOf(0,0,2,2,2,0,0,2,2,2,0),
    intArrayOf(0,4,4,4,0,0,0,4,4,4,0),
    intArrayOf(4,4,4,4,0,0,0,4,4,4,4)
)

val SmallMarioFireMatrix = arrayOf(
    intArrayOf(0,0,0,8,8,8,8,8,0,0,0), // Authentic white cap
    intArrayOf(0,0,8,8,8,8,8,8,8,8,8), // Authentic white cap
    intArrayOf(0,0,4,4,4,3,3,4,3,0,0), // Hair/skin
    intArrayOf(0,4,3,4,3,3,3,4,3,3,0), // Face
    intArrayOf(0,4,3,4,4,3,3,3,4,3,3), // Face
    intArrayOf(0,4,4,3,3,3,3,4,4,0,0), // Hair/face
    intArrayOf(0,0,0,3,3,3,3,3,3,0,0), // Neck
    intArrayOf(0,0,8,8,1,8,8,8,0,0,0), // White shirt, red overalls (1)
    intArrayOf(0,8,8,8,1,8,8,1,8,8,0), // White shirt, red overalls
    intArrayOf(8,8,8,8,1,1,1,1,8,8,8), // White shirt, red overalls
    intArrayOf(3,3,8,1,5,1,1,5,1,8,3), // Skin hands, red overalls, yellow buttons
    intArrayOf(3,3,3,1,1,1,1,1,1,3,3), // Skin hands, red overalls
    intArrayOf(3,3,1,1,1,1,1,1,1,1,3), // Overalls
    intArrayOf(0,0,1,1,1,0,0,1,1,1,0), // Red legs
    intArrayOf(0,4,4,4,0,0,0,4,4,4,0), // Brown shoes
    intArrayOf(4,4,4,4,0,0,0,4,4,4,4)  // Brown shoes
)

val BigMarioMatrix = arrayOf(
    intArrayOf(0,0,0,0,1,1,1,1,1,0,0,0,0),
    intArrayOf(0,0,0,1,1,1,1,1,1,1,1,1,0),
    intArrayOf(0,0,0,4,4,4,3,3,4,3,0,0,0),
    intArrayOf(0,0,4,3,4,3,3,3,4,3,3,3,0),
    intArrayOf(0,0,4,3,4,4,3,3,3,4,3,3,0),
    intArrayOf(0,0,4,4,3,3,3,3,4,4,4,0,0),
    intArrayOf(0,0,0,0,3,3,3,3,3,3,3,0,0),
    intArrayOf(0,0,0,1,1,2,1,1,1,1,0,0,0),
    intArrayOf(0,0,1,1,1,2,1,1,2,1,1,1,0),
    intArrayOf(0,1,1,1,1,2,2,2,2,1,1,1,1),
    intArrayOf(0,3,3,1,1,2,5,2,2,5,1,1,3),
    intArrayOf(0,3,3,3,2,2,2,2,2,2,3,3,3),
    intArrayOf(0,3,3,2,2,2,2,2,2,2,2,3,3),
    intArrayOf(0,0,0,2,2,2,2,2,2,2,2,0,0),
    intArrayOf(0,0,2,2,2,2,0,0,2,2,2,2,0),
    intArrayOf(0,4,4,4,4,4,0,0,4,4,4,4,4),
    intArrayOf(0,4,4,4,4,4,0,0,4,4,4,4,4),
    intArrayOf(4,4,4,4,4,4,0,0,4,4,4,4,4)
)

val PeachStartMatrix = arrayOf(
    intArrayOf(0,0,0,5,5,5,0,0,0), // Crown
    intArrayOf(0,5,5,1,5,1,5,5,0), // Blonde Hair + red jewels
    intArrayOf(0,5,5,3,3,3,5,5,0), // Face Skin
    intArrayOf(0,0,3,3,3,3,3,0,0),
    intArrayOf(0,0,1,1,1,1,1,0,0), // Pink collar
    intArrayOf(0,1,1,1,1,1,1,1,0), // Dress top
    intArrayOf(1,1,1,5,1,5,1,1,1), // Jeweled waist
    intArrayOf(0,1,1,1,1,1,1,1,0), // Skirt top
    intArrayOf(1,1,1,1,1,1,1,1,1), // Skirt wide
    intArrayOf(1,1,1,1,1,1,1,1,1)
)

val ToadStartMatrix = arrayOf(
    intArrayOf(0,0,1,1,1,1,1,0,0), // Mushroom Hat (Red/White)
    intArrayOf(0,1,1,5,5,5,1,1,0), // White dots (5 represents white / yellow in classic, let's substitute as white)
    intArrayOf(1,1,5,5,5,5,5,1,1),
    intArrayOf(1,1,1,5,5,5,1,1,1),
    intArrayOf(0,1,1,1,1,1,1,1,0),
    intArrayOf(0,0,3,3,3,3,3,0,0), // Face Skin
    intArrayOf(0,0,6,3,6,3,6,0,0), // Eyes
    intArrayOf(0,0,2,2,2,2,2,0,0), // Blue Vest
    intArrayOf(0,2,2,2,2,2,2,2,0),
    intArrayOf(0,0,4,0,0,0,4,0,0)  // Feet
)

val GoombaMatrix = arrayOf(
    intArrayOf(0,0,0,0,4,4,4,4,4,0,0,0,0),
    intArrayOf(0,0,0,4,4,4,4,4,4,4,0,0,0),
    intArrayOf(0,0,4,4,4,4,4,4,4,4,4,0,0),
    intArrayOf(0,4,4,4,4,6,6,6,4,4,4,4,0),
    intArrayOf(0,4,4,4,6,3,6,3,6,4,4,4,0),
    intArrayOf(4,4,4,4,6,3,6,3,6,4,4,4,4),
    intArrayOf(4,4,4,4,4,4,4,4,4,4,4,4,4),
    intArrayOf(0,0,4,4,3,3,3,3,3,4,4,0,0),
    intArrayOf(0,0,0,3,3,3,3,3,3,3,0,0,0),
    intArrayOf(0,0,3,3,3,3,3,3,3,3,3,0,0),
    intArrayOf(0,6,6,6,6,0,0,0,6,6,6,6,0),
    intArrayOf(6,6,6,6,6,0,0,0,6,6,6,6,6),
    intArrayOf(6,6,6,6,0,0,0,0,0,6,6,6,6)
)

val MysteryBlockMatrix = arrayOf(
    intArrayOf(5,5,5,5,5,5,5,5,5,5,5,5),
    intArrayOf(5,5,6,6,6,6,6,6,6,6,5,5),
    intArrayOf(5,6,5,5,3,3,3,5,5,6,5,5),
    intArrayOf(5,6,5,3,5,5,5,3,5,6,5,5),
    intArrayOf(5,6,5,5,5,3,5,5,5,6,5,5),
    intArrayOf(5,6,5,5,5,3,5,5,5,6,5,5),
    intArrayOf(5,6,5,5,5,5,5,5,5,6,5,5),
    intArrayOf(5,6,5,5,5,3,5,5,5,6,5,5),
    intArrayOf(5,6,5,5,5,3,5,5,5,6,5,5),
    intArrayOf(5,5,6,6,6,6,6,6,6,6,5,5),
    intArrayOf(5,5,5,5,5,5,5,5,5,5,5,5)
)

val MushroomMatrix = arrayOf(
    intArrayOf(0,0,0,0,1,1,1,1,0,0,0,0),
    intArrayOf(0,0,1,1,1,1,1,1,1,1,0,0),
    intArrayOf(0,1,1,3,3,1,1,3,3,1,1,0),
    intArrayOf(1,1,3,3,3,3,3,3,3,3,1,1),
    intArrayOf(1,1,3,3,3,3,3,3,3,3,1,1),
    intArrayOf(1,1,1,3,3,1,1,3,3,1,1,1),
    intArrayOf(0,1,1,1,1,1,1,1,1,1,1,0),
    intArrayOf(0,0,0,3,3,3,3,3,3,0,0,0),
    intArrayOf(0,0,3,3,3,6,6,3,3,3,0,0),
    intArrayOf(0,0,3,3,3,6,6,3,3,3,0,0),
    intArrayOf(0,0,3,3,3,3,3,3,3,3,0,0),
    intArrayOf(0,0,0,3,3,3,3,3,3,0,0,0)
)

val FlowerMatrix = arrayOf(
    intArrayOf(0,0,0,1,1,1,1,1,1,0,0,0),
    intArrayOf(0,0,1,1,5,5,5,5,1,1,0,0),
    intArrayOf(0,1,1,5,5,3,3,5,5,1,1,0),
    intArrayOf(1,1,5,5,3,6,6,3,5,5,1,1),
    intArrayOf(1,1,5,5,3,6,6,3,5,5,1,1),
    intArrayOf(0,1,1,5,5,3,3,5,5,1,1,0),
    intArrayOf(0,0,1,1,5,5,5,5,1,1,0,0),
    intArrayOf(0,0,0,1,1,1,1,1,1,0,0,0),
    intArrayOf(0,0,0,0,4,2,2,4,0,0,0,0),
    intArrayOf(0,0,0,2,2,2,2,2,2,0,0,0),
    intArrayOf(0,0,0,0,2,2,2,2,0,0,0,0),
    intArrayOf(0,0,0,0,0,2,2,0,0,0,0,0)
)

val StarMatrix = arrayOf(
    intArrayOf(0,0,0,0,0,5,5,0,0,0,0,0),
    intArrayOf(0,0,0,0,5,5,5,5,0,0,0,0),
    intArrayOf(0,0,0,5,5,5,5,5,5,0,0,0),
    intArrayOf(0,0,5,5,6,5,5,6,5,5,0,0),
    intArrayOf(5,5,5,5,5,5,5,5,5,5,5,5),
    intArrayOf(0,5,5,5,5,5,5,5,5,5,5,0),
    intArrayOf(0,0,5,5,5,5,5,5,5,5,0,0),
    intArrayOf(0,0,0,5,5,5,5,5,5,0,0,0),
    intArrayOf(0,0,5,5,5,0,0,5,5,5,0,0),
    intArrayOf(0,5,5,5,0,0,0,0,5,5,5,0),
    intArrayOf(5,5,5,0,0,0,0,0,0,5,5,5)
)

val KoopaMatrix = arrayOf(
    intArrayOf(0,0,0,7,7,7,7,0,0,0),
    intArrayOf(0,0,7,7,7,7,7,7,0,0),
    intArrayOf(0,7,3,3,6,3,3,6,7,0),
    intArrayOf(0,7,3,3,3,3,3,3,7,0),
    intArrayOf(0,7,7,7,5,5,5,5,7,0),
    intArrayOf(0,0,7,7,7,7,7,0,0,0),
    intArrayOf(0,7,7,7,7,7,7,7,0,0),
    intArrayOf(7,7,7,7,7,7,7,7,7,0),
    intArrayOf(7,7,3,7,7,7,3,7,7,0),
    intArrayOf(0,7,7,7,7,7,7,7,0,0),
    intArrayOf(0,0,6,6,0,0,6,6,0,0),
    intArrayOf(0,6,6,6,0,0,6,6,6,0)
)

val KoopaShellMatrix = arrayOf(
    intArrayOf(0,0,0,7,7,7,7,7,0,0,0),
    intArrayOf(0,0,7,7,7,7,7,7,7,0,0),
    intArrayOf(0,7,7,6,6,6,6,6,7,7,0),
    intArrayOf(7,7,6,7,7,6,7,7,6,7,7),
    intArrayOf(7,7,6,7,7,6,7,7,6,7,7),
    intArrayOf(7,7,6,6,6,6,6,6,6,7,7),
    intArrayOf(7,7,7,7,7,7,7,7,7,7,7),
    intArrayOf(0,7,7,7,7,7,7,7,7,7,0),
    intArrayOf(0,0,7,7,7,7,7,7,7,0,0)
)

@Composable
fun MarioScreen(viewModel: MarioViewModel) {
    val status by viewModel.gameStatus.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(status) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B071E), // Extremely rich deep violet
                        Color(0xFF130D2C), // Sleek high-fidelity dark indigo
                        Color(0xFF05030A)  // Dark carbon obsidian
                    )
                )
            )
            .systemBarsPadding()
            .retroCrtOverlay()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                try {
                    focusRequester.requestFocus()
                } catch (e: Exception) {}
            }
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                val isPressed = keyEvent.type == KeyEventType.KeyDown
                val isReleased = keyEvent.type == KeyEventType.KeyUp

                if (status == GameStatus.PLAYING || status == GameStatus.PAUSED) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.A -> {
                            if (isPressed) viewModel.isLeftPressed = true
                            if (isReleased) viewModel.isLeftPressed = false
                            true
                        }
                        Key.DirectionRight, Key.D -> {
                            if (isPressed) viewModel.isRightPressed = true
                            if (isReleased) viewModel.isRightPressed = false
                            true
                        }
                        Key.DirectionUp, Key.W, Key.Spacebar -> {
                            if (isPressed) viewModel.isJumpPressed = true
                            if (isReleased) viewModel.isJumpPressed = false
                            true
                        }
                        Key.DirectionDown, Key.S -> {
                            if (isPressed) {
                                viewModel.isSprintPressed = true
                            }
                            if (isReleased) {
                                viewModel.isSprintPressed = false
                            }
                            true
                        }
                        Key.ShiftLeft, Key.ShiftRight, Key.J, Key.Z, Key.X -> {
                            if (isPressed) {
                                viewModel.isSprintPressed = true
                                viewModel.fireAction()
                            }
                            if (isReleased) {
                                viewModel.isSprintPressed = false
                            }
                            true
                        }
                        Key.P, Key.Escape -> {
                            if (isPressed) {
                                viewModel.togglePause()
                            }
                            true
                        }
                        else -> false
                    }
                } else if (status == GameStatus.START_SCREEN) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.A -> {
                            if (isPressed) {
                                val current = viewModel.selectedWorld.value
                                if (current > 1) {
                                    viewModel.selectedWorld.value = current - 1
                                }
                            }
                            true
                        }
                        Key.DirectionRight, Key.D -> {
                            if (isPressed) {
                                val current = viewModel.selectedWorld.value
                                if (current < 3) {
                                    viewModel.selectedWorld.value = current + 1
                                }
                            }
                            true
                        }
                        Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
                            if (isPressed) {
                                viewModel.startGame(viewModel.selectedWorld.value)
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        when (status) {
            GameStatus.START_SCREEN -> MarioStartScreen(viewModel)
            GameStatus.PLAYING,
            GameStatus.PAUSED,
            GameStatus.VICTORY_SLIDE,
            GameStatus.VICTORY_WALK,
            GameStatus.VICTORY_SCORING,
            GameStatus.DEATH_ANIMATION,
            GameStatus.GAME_OVER -> MarioGameView(viewModel)
            GameStatus.LEADERBOARD -> MarioLeaderboardScreen(viewModel)
        }
    }
}

@Composable
fun MarioStartScreen(viewModel: MarioViewModel) {
    var selectedTab by viewModel.selectedWorld
    var debugClicks by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Fully immersive full-screen responsive retro-arcade vector background
        RetroArcadeStartBackground()

        // 2. Translucent dark overlay with gradient mask at the bottom for absolute readability of controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // 3. User Controls Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Arcade Cyber Header Title with neon shadows, scaled gracefully
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(
                    text = "SUPER MARIO",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00FF66), // Cyber Resistance Neon Green
                    textAlign = TextAlign.Center,
                    modifier = Modifier.shadow(16.dp)
                )
                Text(
                    text = "► CYBER RESISTANCE TERMINAL v2.6 ◄",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = Color(0xFF10B981), // Resistance Emerald Green
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Beautiful spacer to keep central characters of Bowser & Mario fully visible
            Spacer(modifier = Modifier.weight(1f))

            // Level selection & Play Button Panel with elegant dark-glass backing
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090812).copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                    .border(BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.9f)), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                // Cyber status command text
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SYS_STATUS: READY",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "BYPASS_CORE: ONLINE",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "CHOOSE SECTOR TARGET",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.8.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(1, 2, 3).forEach { num ->
                        val isSel = selectedTab == num
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSel) {
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF059669), Color(0xFF047857))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF1F2937).copy(alpha = 0.5f), Color(0xFF111827).copy(alpha = 0.7f))
                                        )
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        if (isSel) 2.5.dp else 1.dp,
                                        if (isSel) Color(0xFF34D399) else Color.White.copy(alpha = 0.15f)
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTab = num }
                                .testTag("stage_tab_$num"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SECTOR 1-$num",
                                color = if (isSel) Color.White else Color.White.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Play Button: Vibrant Resistance Green Design
                Button(
                    onClick = { 
                        debugClicks++
                        viewModel.startGame(selectedTab) 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981), // Resistance Green
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("play_mario_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow, 
                            contentDescription = "PLAY", 
                            modifier = Modifier.size(28.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INITIALIZE BYPASS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Leaderboard viewing option: Glassmorphic outlined Green button
                OutlinedButton(
                    onClick = { 
                        debugClicks++
                        viewModel.enterLeaderboardDirect() 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("leaderboard_direct_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                    border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Star, "Leaderboard", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DECRYPT GLOBAL DATABASE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                if (debugClicks > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Touch Registered - Click Count: $debugClicks",
                        color = Color(0xFF00FF66), // Cyber Neon green
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MarioGameView(viewModel: MarioViewModel) {
    val status by viewModel.gameStatus.collectAsState()
    val scoreValue by remember { derivedStateOf { viewModel.score.value } }
    val coinsValue by remember { derivedStateOf { viewModel.coins.value } }
    val livesValue by remember { derivedStateOf { viewModel.lives.value } }
    val worldValue by remember { derivedStateOf { viewModel.selectedWorld.value } }
    val timeValue by remember { derivedStateOf { viewModel.timeRemaining.value } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070F))
    ) {
        // HUD / Scoreboard (Retro Arcade Cabinet Header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14141F))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mario Score column
                    Column {
                        Text(
                            text = "MARIO",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RedRetro
                        )
                        Text(
                            text = scoreValue.toString().padStart(6, '0'),
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Coins count
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "COINS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = YellowRetro
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(YellowRetro, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "x${coinsValue.toString().padStart(2, '0')}",
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // World Indicator
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "WORLD",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "1-$worldValue",
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Remaining Time
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TIME",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FFCC)
                        )
                        Text(
                            text = timeValue.toString().padStart(3, '0'),
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Lives
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LIVES",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = LightGreenRetro
                        )
                        Text(
                            text = "x$livesValue",
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Indicators Bar (P-Meter & Starman mode!)
                val pVal = viewModel.pMeter.value
                val starActive = viewModel.isStarman.value
                val isSuperCharged = pVal >= 100f

                if (pVal > 0f || starActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // P-Meter Gauge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "P-METER: ",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuperCharged) Color(0xFFFFD700) else Color.LightGray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Draw block bars
                            val filledBlocks = (pVal / 10f).toInt()
                            repeat(10) { i ->
                                val active = i < filledBlocks
                                Box(
                                    modifier = Modifier
                                        .size(width = 8.dp, height = 10.dp)
                                        .padding(horizontal = 1.dp)
                                        .background(
                                            if (active) {
                                                if (isSuperCharged) Color(0xFFFFD700) else Color(0xFFFF5555)
                                            } else {
                                                Color(0xFF33334F)
                                            }
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSuperCharged) "⚡ SPRINT MAX!" else ">>>",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = if (isSuperCharged) Color(0xFFFFD700) else Color.Red
                            )
                        }

                        // Starman Active text
                        if (starActive) {
                            val flashColor = if ((System.currentTimeMillis() / 150) % 2 == 0L) Color(0xFFFFD700) else Color(0xFFEC4899)
                            Text(
                                text = "★ INVINCIBLE STARMAN ★",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = flashColor
                            )
                        }
                    }
                }
            }
        }

        // Interactive Game Play Area (Scrollable platform graphics canvas)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .background(Color(0xFF1A1A2E))
        ) {
            CanvasView(viewModel = viewModel)

            // Sleek floating Pause & Home (Exit) buttons in top-right
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f)), CircleShape)
                        .testTag("btn_pause")
                ) {
                    Icon(
                        imageVector = if (status == GameStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Refresh,
                        contentDescription = "Pause",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.exitToMain() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.6f)), CircleShape)
                        .testTag("btn_exit")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home, 
                        contentDescription = "Exit", 
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Overlays: Pause, Game Over, Victory
            if (status == GameStatus.PAUSED) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PAUSED",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (status == GameStatus.GAME_OVER) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GAME OVER",
                            color = RedRetro,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Score: ${scoreValue}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.startGame(worldValue) },
                            colors = ButtonDefaults.buttonColors(containerColor = YellowRetro, contentColor = Color.Black)
                        ) {
                            Text("TRY AGAIN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (status == GameStatus.VICTORY_SCORING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "STAGE CLEAR!",
                            color = LightGreenRetro,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "TIME BONUS RECORD: ${timeValue} sec",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TOTAL SCORE: ${scoreValue}",
                            color = YellowRetro,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Score Submission initials
                        Text(
                            text = "ENTER PLAYER INITIALS",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = viewModel.playerNameInput.value,
                            onValueChange = { viewModel.playerNameInput.value = it.take(3).uppercase() },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YellowRetro,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier
                                .width(120.dp)
                                .height(56.dp)
                                .padding(vertical = 4.dp)
                                .testTag("playerNameInput"),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.saveScoreAndEnterLeaderboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = YellowRetro, contentColor = Color.Black),
                            modifier = Modifier.testTag("saveScoreBtn")
                        ) {
                            Text("SAVE SCORE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Onscreen NES Controller Panel for mobile playability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF14121E), // Premium deep graphite slate
                            Color(0xFF1D1B2D), // Rich high-tech carbon velvet
                            Color(0xFF0C0A12)  // Deep edge-tone shadow black
                        )
                    )
                )
                .border(BorderStroke(1.5.dp, Color(0xFF3B335C).copy(alpha = 0.95f)))
                .padding(bottom = 20.dp, top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Classic D-PAD Left & Right (Big, responsive, and wide for perfect touch capture)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF0F0E18), RoundedCornerShape(20.dp))
                        .padding(8.dp)
                        .border(BorderStroke(1.dp, Color(0xFF312E4F)), RoundedCornerShape(20.dp))
                ) {
                    // LEFT key
                    PressingButton(
                        onPressed = { viewModel.isLeftPressed = it },
                        modifier = Modifier
                            .size(width = 78.dp, height = 60.dp)
                            .border(BorderStroke(2.dp, Color(0xFF6366F1)), RoundedCornerShape(16.dp))
                            .testTag("dpad_left"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181532), contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Left",
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // RIGHT key
                    PressingButton(
                        onPressed = { viewModel.isRightPressed = it },
                        modifier = Modifier
                            .size(width = 78.dp, height = 60.dp)
                            .border(BorderStroke(2.dp, Color(0xFF6366F1)), RoundedCornerShape(16.dp))
                            .testTag("dpad_right"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181532), contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Right",
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Mid-Console Branding Decor (Elegant Retro Arcade Deck branding plate)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFEF4444), CircleShape) // Power indicator glow
                        )
                        Text(
                            text = "NES-80",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFEF4444),
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "CLASSIC DECK",
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                // Action buttons arranged with authentic retro SLANTED diagonal layouts
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF0F0E18), RoundedCornerShape(20.dp))
                        .padding(8.dp)
                        .border(BorderStroke(1.dp, Color(0xFF312E4F)), RoundedCornerShape(20.dp))
                ) {
                    // ACTION B button (Fireball / Sprint) - Cherry Red, slanted lower
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = 8.dp)
                    ) {
                        PressingButton(
                            onPressed = {
                                if (it) {
                                    viewModel.isSprintPressed = true
                                    viewModel.fireAction()
                                } else {
                                    viewModel.isSprintPressed = false
                                }
                            },
                            modifier = Modifier
                                .size(62.dp)
                                .border(BorderStroke(2.5.dp, Color(0xFFFDA4AF)), CircleShape)
                                .testTag("btn_action_b"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C), contentColor = Color.White)
                        ) {
                            Text(
                                text = "B",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "RUN/FIRE",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // ACTION A button (Jump) - Rose Gold Magenta, slanted higher
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = (-8).dp)
                    ) {
                        PressingButton(
                            onPressed = { viewModel.isJumpPressed = it },
                            modifier = Modifier
                                .size(62.dp)
                                .border(BorderStroke(2.5.dp, Color(0xFFFCA5A5)), CircleShape)
                                .testTag("btn_action_a"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48), contentColor = Color.White)
                        ) {
                            Text(
                                text = "A",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "JUMP",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Custom resilient button to track physical pressing states (isDown = true/false) with zero latency
@Composable
fun PressingButton(
    onPressed: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit
) {
    var isDown by remember { mutableStateOf(false) }
    val scale = if (isDown) 0.88f else 1.0f
    val containerColor = if (isDown) colors.containerColor.copy(alpha = 0.8f) else colors.containerColor

    Box(
        modifier = modifier
            .scale(scale)
            .background(containerColor, shape)
            .pointerInput(onPressed) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        // Touch Capture Lock: Track finger press status with zero-latency.
                        // Stay pressed as long as the touch is held down anywhere, allowing comfortable slider play!
                        val firstChange = changes.firstOrNull()
                        if (firstChange != null) {
                            val currentlyPressed = firstChange.pressed
                            if (currentlyPressed != isDown) {
                                isDown = currentlyPressed
                                onPressed(currentlyPressed)
                            }
                        } else {
                            if (isDown) {
                                isDown = false
                                onPressed(false)
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun CanvasView(viewModel: MarioViewModel) {
    val worldNum = viewModel.selectedWorld.value

    // Listen to local ticking triggers
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer()
            .testTag("mario_canvas")
    ) {
        val logicalHeight = 448f
        val scale = size.height / logicalHeight

        val camX = viewModel.cameraX.value

        // 1. Draw atmospheric background depending on World Theme with vibrant, modern gradients
        when (worldNum) {
            1 -> {
                // Vibrant, high-contrast, modern sky blue-turquoise gradient
                drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFE0F2FE))))
                // Draw decorative hills and fluffy clouds
                drawSkyScenery(camX, scale)
            }
            2 -> {
                // Sophisticated deep midnight-slate gradient for underworld
                drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF0F172A), Color(0xFF020617))))
                drawSkySceneryUnderworld(camX, scale)
            }
            3 -> {
                // Extreme volcanic-core crimson gradient for the Castle
                drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF2D0707), Color(0xFF450A0A), Color(0xFF0F0101))))
                drawSkySceneryCastle(camX, scale)
            }
        }

        // 2. Draw static level elements
        val startCol = max(0, (camX / viewModel.tileSize).toInt())
        val endCol = min(viewModel.tileCols - 1, ((camX + (size.width / scale)) / viewModel.tileSize).toInt() + 1)

        for (r in 0 until viewModel.tileRows) {
            for (c in startCol..endCol) {
                val tile = viewModel.levelGrid[r][c]
                if (tile == TILE_EMPTY) continue

                val tileX = c * viewModel.tileSize
                val tileY = r * viewModel.tileSize

                drawLevelTile(tile, tileX, tileY, viewModel.tileSize, camX, scale, worldNum)
            }
        }

        // 3. Draw powerups
        viewModel.getPowerupsCopy().forEach { powerup ->
            drawPowerupEntity(powerup, camX, scale)
        }

        // 4. Draw enemies
        viewModel.getEnemiesCopy().forEach { enemy ->
            drawEnemyEntity(enemy, camX, scale)
        }

        // 5. Draw fireballs projectiles
        viewModel.getFireballsCopy().forEach { fb ->
            drawCircle(
                color = Color(0xFFFF5D00),
                center = Offset((fb.x - camX) * scale, fb.y * scale),
                radius = 4f * scale
            )
            drawCircle(
                color = Color(0xFFFFD700),
                center = Offset((fb.x - camX) * scale, fb.y * scale),
                radius = 2f * scale
            )
        }

        // 6. Draw local bits & particles
        viewModel.getParticlesCopy().forEach { part ->
            val pSz = 6f * scale
            drawRect(
                color = Color(0xFFCD5C5C),
                topLeft = Offset((part.x - camX) * scale - pSz/2, part.y * scale - pSz/2),
                size = Size(pSz, pSz)
            )
        }

        // 7. Draw Player (Mario Classic styling)
        val mX = viewModel.marioX.value
        val mY = viewModel.marioY.value
        val mW = viewModel.getMarioWidth()
        val mH = viewModel.getMarioHeight()
        val mSize = viewModel.marioSize.value
        val facing = viewModel.facingRight.value
        val isInv = viewModel.invincibilityFrames.value

        // Only draw Mario if not flashing (invincibility frame blink effect)
        if (isInv == 0 || (isInv / 5) % 2 == 0) {
            val matrixToDraw = when {
                mSize == "FIRE" -> SmallMarioFireMatrix
                mSize == "BIG" -> BigMarioMatrix
                else -> SmallMarioMatrix
            }
            drawPixelMatrix(
                matrix = matrixToDraw,
                x = (mX - camX) * scale,
                y = mY * scale,
                width = mW * scale,
                height = mH * scale,
                facingRight = facing,
                isStarmanColor = viewModel.isStarman.value
            )
        }

        // 8. Draw floating score popups
        viewModel.getPopupsCopy().forEach { pop ->
            // Let's draw simplified popup locations (drawn as circles or texts, here colored points)
            drawCircle(
                color = YellowRetro.copy(alpha = pop.alpha),
                center = Offset((pop.x - camX) * scale, pop.y * scale),
                radius = 3f * scale
            )
        }
    }
}

fun DrawScope.drawSkyScenery(camX: Float, scale: Float) {
    // 1. Soft retro glowing sun in the sky (parallax speed 0.02f)
    val sunX = 320f - (camX * 0.02f)
    val sunY = 90f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE047).copy(alpha = 0.25f), Color.Transparent),
            center = Offset(sunX * scale, sunY * scale),
            radius = 65f * scale
        ),
        center = Offset(sunX * scale, sunY * scale),
        radius = 65f * scale
    )
    drawCircle(
        color = Color(0xFFFEF08A),
        center = Offset(sunX * scale, sunY * scale),
        radius = 18f * scale
    )

    // 2. Far teal-emerald mountains (parallax speed 0.06f)
    for (i in 0..12) {
        val mountainX = i * 480f - (camX * 0.06f)
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F766E).copy(alpha = 0.15f), Color(0xFF115E59).copy(alpha = 0.35f))
            ),
            center = Offset(mountainX * scale, 410f * scale),
            radius = 150f * scale
        )
    }

    // 3. Mid vibrant emerald-green hills (parallax speed 0.15f)
    for (i in 0..18) {
        val hillX = i * 260f - (camX * 0.15f)
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF16A34A).copy(alpha = 0.4f), Color(0xFF14532D).copy(alpha = 0.55f))
            ),
            center = Offset(hillX * scale, 395f * scale),
            radius = 100f * scale
        )
        // Draw elegant highlight stroke on the hill edge
        drawCircle(
            color = Color(0xFF86EFAC).copy(alpha = 0.15f),
            center = Offset(hillX * scale, 395f * scale),
            radius = 99f * scale,
            style = Stroke(width = 1.5f * scale)
        )
    }

    // 4. Near round lush green bushes (parallax speed 0.25f)
    for (i in 0..22) {
        val bushX = i * 190f - (camX * 0.25f)
        val bushY = 365f
        drawCircle(
            color = Color(0xFF15803D).copy(alpha = 0.75f),
            center = Offset(bushX * scale, bushY * scale),
            radius = 24f * scale
        )
        drawCircle(
            color = Color(0xFF15803D).copy(alpha = 0.75f),
            center = Offset((bushX + 14f) * scale, (bushY + 4f) * scale),
            radius = 18f * scale
        )
        drawCircle(
            color = Color(0xFF15803D).copy(alpha = 0.75f),
            center = Offset((bushX - 12f) * scale, (bushY + 6f) * scale),
            radius = 15f * scale
        )
    }

    // 5. Fluffy, flat-bottomed clouds (slowly drift in background over time + parallax speed 0.04f)
    val timeDrift = (System.currentTimeMillis() / 2000f) % 2000f
    for (i in 0..10) {
        val cloudX = i * 550f - (camX * 0.04f) + (timeDrift * scale * 0.08f)
        val cloudY = (70f + (i % 3) * 18f) * scale
        
        // Draw beautiful rounded cloud base
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.85f), Color(0xFFE2E8F0).copy(alpha = 0.65f))
            ),
            topLeft = Offset(cloudX, cloudY),
            size = Size(80f * scale, 24f * scale),
            cornerRadius = CornerRadius(12f * scale, 12f * scale)
        )
        // Overlapping fluffy peaks
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            center = Offset(cloudX + 22f * scale, cloudY + 4f * scale),
            radius = 18f * scale
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            center = Offset(cloudX + 44f * scale, cloudY),
            radius = 22f * scale
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            center = Offset(cloudX + 62f * scale, cloudY + 6f * scale),
            radius = 14f * scale
        )
    }
}

fun DrawScope.drawSkySceneryUnderworld(camX: Float, scale: Float) {
    // Sophisticated deep underworld slate columns and grid
    for (i in 0..20) {
        val colX = i * 160f - (camX * 0.12f)
        // Background columns
        drawRect(
            color = Color(0xFF0F172A).copy(alpha = 0.35f),
            topLeft = Offset(colX * scale, 0f),
            size = Size(40f * scale, size.height)
        )
        // Texture brick joints on columns
        for (j in 0..12) {
            val y = j * 36f * scale
            drawLine(
                color = Color(0xFF1E293B).copy(alpha = 0.18f),
                start = Offset(colX * scale, y),
                end = Offset((colX + 40f) * scale, y),
                strokeWidth = 1f * scale
            )
        }
    }

    // Magical glowing cave crystals / floating ambient embers (underworld vibe)
    for (i in 1..8) {
        val crystalX = ((i * 320f - (camX * 0.08f)) * scale) % (size.width + 120f * scale)
        val crystalY = (140f + (i * 50f) % 210f) * scale
        // Soft radial emerald-light glow aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x2810B981), Color.Transparent),
                center = Offset(crystalX, crystalY),
                radius = 35f * scale
            ),
            center = Offset(crystalX, crystalY),
            radius = 35f * scale
        )
        // Inner glowing crystal seed point
        drawCircle(
            color = Color(0xAA34D399),
            center = Offset(crystalX, crystalY),
            radius = 3f * scale
        )
    }
}

fun DrawScope.drawSkySceneryCastle(camX: Float, scale: Float) {
    // Castle backgrounds: heavy stone brick columns
    for (i in 0..18) {
        val colX = i * 200f - (camX * 0.15f)
        // Heavy volcanic stone column
        drawRect(
            color = Color(0xFF180707),
            topLeft = Offset(colX * scale, 0f),
            size = Size(44f * scale, size.height)
        )
        // Column brick joints
        for (j in 0..14) {
            val y = j * 30f * scale
            drawLine(
                color = Color(0xFF2A0A0A),
                start = Offset(colX * scale, y),
                end = Offset((colX + 44f) * scale, y),
                strokeWidth = 1.2f * scale
            )
        }
    }

    // Heavy iron chains hanging from ceiling
    for (i in 0..8) {
        val chainX = (i * 360f - (camX * 0.1f)) * scale
        val chainLen = (90f + (i % 3) * 45f) * scale
        drawLine(
            color = Color(0xFF374151).copy(alpha = 0.45f),
            start = Offset(chainX, 0f),
            end = Offset(chainX, chainLen),
            strokeWidth = 2f * scale
        )
        var linkY = 8f * scale
        while (linkY < chainLen) {
            drawCircle(
                color = Color(0xFF4B5563).copy(alpha = 0.55f),
                center = Offset(chainX, linkY),
                radius = 2.5f * scale
            )
            linkY += 14f * scale
        }
    }

    // Dynamic rising burning magma sparks swaying in the air!
    val waveT = System.currentTimeMillis() / 1000f
    for (i in 0..12) {
        val emberSeedX = (i * 140f)
        val sway = kotlin.math.sin(waveT + i) * 12f
        val emberX = ((emberSeedX - (camX * 0.25f) + sway) * scale) % (size.width + 100f * scale)
        
        // Embers rise vertical animation
        val riseSpeed = 35f + (i % 4) * 12f
        val emberY = ((size.height + 40f * scale) - (System.currentTimeMillis() / 12f + i * 160f) % (size.height + 80f * scale))
        
        if (emberY > 0f && emberY < size.height) {
            drawCircle(
                color = Color(0xFFFF5F00).copy(alpha = 0.85f),
                center = Offset(emberX, emberY),
                radius = (1.5f + (i % 3) * 0.8f) * scale
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(emberX, emberY),
                    radius = 7f * scale
                ),
                center = Offset(emberX, emberY),
                radius = 7f * scale
            )
        }
    }
}

fun DrawScope.drawLevelTile(tile: Int, tx: Float, ty: Float, tSize: Float, camX: Float, scale: Float, worldNum: Int) {
    val x = (tx - camX) * scale
    val y = ty * scale
    val sz = tSize * scale

    when (tile) {
        TILE_GROUND -> {
            when (worldNum) {
                1 -> {
                    // Vibrant modern grassy soil block
                    drawRect(color = Color(0xFF9C6644), topLeft = Offset(x, y + sz * 0.25f), size = Size(sz, sz * 0.75f)) // soil
                    drawRect(color = Color(0xFF10B981), topLeft = Offset(x, y), size = Size(sz, sz * 0.25f)) // grassland Emerald cap
                    drawLine(color = Color(0xFF34D399), start = Offset(x, y + 1f * scale), end = Offset(x + sz, y + 1f * scale), strokeWidth = 1.5f * scale) // light highlight edge
                    
                    // soil retro crack detailing
                    val detailColor = Color(0xFF78310E).copy(alpha = 0.5f)
                    drawLine(color = detailColor, start = Offset(x + sz*0.25f, y + sz*0.45f), end = Offset(x + sz*0.75f, y + sz*0.45f), strokeWidth = 1f * scale)
                    drawLine(color = detailColor, start = Offset(x + sz*0.5f, y + sz*0.45f), end = Offset(x + sz*0.5f, y + sz*0.75f), strokeWidth = 1f * scale)
                }
                2 -> {
                    // Deep slate cave rock
                    drawRect(color = Color(0xFF1E293B), topLeft = Offset(x, y), size = Size(sz, sz))
                    drawRect(color = Color(0xFF334155), topLeft = Offset(x, y), size = Size(sz, sz * 0.2f)) // specular highlight cap
                    drawLine(color = Color(0xFF0F172A), start = Offset(x + sz*0.3f, y), end = Offset(x + sz*0.6f, y + sz), strokeWidth = 1.5f * scale) // bedrock crack
                }
                3 -> {
                    // Volcanic magma stone block with bright glowing yellow/red lava veins
                    drawRect(color = Color(0xFF2C0F0F), topLeft = Offset(x, y), size = Size(sz, sz))
                    drawRect(color = Color(0xFF110707), topLeft = Offset(x, y), size = Size(sz, sz * 0.15f)) // charcoal crown
                    // Glowing molten lava crack
                    drawLine(color = Color(0xFFEF4444), start = Offset(x + sz*0.5f, y + sz*0.15f), end = Offset(x + sz*0.5f, y + sz), strokeWidth = 2f * scale)
                    drawLine(color = Color(0xFFFBBF24), start = Offset(x + sz*0.48f, y + sz*0.35f), end = Offset(x + sz*0.52f, y + sz*0.8f), strokeWidth = 1f * scale)
                }
            }
        }
        TILE_SOLID -> {
            // Beveled mechanical concrete block with 3D outline and 4 corner rivets
            drawRect(color = Color(0xFF4B5563), topLeft = Offset(x, y), size = Size(sz, sz))
            // 3D light top/left edges
            drawLine(color = Color(0xFF9CA3AF), start = Offset(x, y), end = Offset(x + sz, y), strokeWidth = 1.5f * scale)
            drawLine(color = Color(0xFF9CA3AF), start = Offset(x, y), end = Offset(x, y + sz), strokeWidth = 1.5f * scale)
            // 3D dark bottom/right edges
            drawLine(color = Color(0xFF1F2937), start = Offset(x, y + sz), end = Offset(x + sz, y + sz), strokeWidth = 1.5f * scale)
            drawLine(color = Color(0xFF1F2937), start = Offset(x + sz, y), end = Offset(x + sz, y + sz), strokeWidth = 1.5f * scale)
            
            // Rivets
            val m = 3f * scale
            val r = 1.2f * scale
            val rCol = Color(0xFF1F2937)
            drawCircle(color = rCol, center = Offset(x + m, y + m), radius = r)
            drawCircle(color = rCol, center = Offset(x + sz - m, y + m), radius = r)
            drawCircle(color = rCol, center = Offset(x + m, y + sz - m), radius = r)
            drawCircle(color = rCol, center = Offset(x + sz - m, y + sz - m), radius = r)
        }
        TILE_CASTLE_BRICK -> {
            // Obsidian-crimson clean castle block with fine detail
            drawRect(color = Color(0xFF450A0A), topLeft = Offset(x, y), size = Size(sz, sz))
            drawRect(color = Color(0xFF110101), topLeft = Offset(x, y), size = Size(sz, sz), style = Stroke(width = 1.5f * scale))
            val traceColor = Color(0xFFEF4444).copy(alpha = 0.25f)
            drawLine(color = traceColor, start = Offset(x + 1f, y + 1f), end = Offset(x + sz - 1f, y + 1f), strokeWidth = 1f * scale)
            drawLine(color = traceColor, start = Offset(x + 1f, y + 1f), end = Offset(x + 1f, y + sz - 1f), strokeWidth = 1f * scale)
        }
        TILE_CASTLE_DOOR -> {
            drawRect(color = Color(0xFF2E0909), topLeft = Offset(x, y), size = Size(sz, sz))
            drawRect(color = Color.Black, topLeft = Offset(x, y), size = Size(sz, sz), style = Stroke(width = 1f * scale))
        }
        TILE_HITSOLID -> {
            // Visually disabled but cleanly detailed metal block
            drawRect(color = Color(0xFF475569), topLeft = Offset(x, y), size = Size(sz, sz))
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(x, y), size = Size(sz, sz), style = Stroke(width = 1f * scale))
            drawLine(color = Color(0xFF64748B), start = Offset(x + 1f, y + 1f), end = Offset(x + sz - 1f, y + 1f), strokeWidth = 1f * scale)
        }
        TILE_BRICK -> {
            // Embossed, highly polished 3D brick layout with lighting highlights
            drawRect(color = Color(0xFFEA580C), topLeft = Offset(x, y), size = Size(sz, sz))
            
            val shadowColor = Color(0xFF7C2D12)
            drawLine(color = shadowColor, start = Offset(x, y + sz/2), end = Offset(x + sz, y + sz/2), strokeWidth = 1.5f * scale)
            drawLine(color = shadowColor, start = Offset(x + sz/2, y), end = Offset(x + sz/2, y + sz/2), strokeWidth = 1.5f * scale)
            drawLine(color = shadowColor, start = Offset(x + sz/4, y + sz/2), end = Offset(x + sz/4, y + sz), strokeWidth = 1.5f * scale)
            drawLine(color = shadowColor, start = Offset(x + 3*sz/4, y + sz/2), end = Offset(x + 3*sz/4, y + sz), strokeWidth = 1.5f * scale)
            
            // Refined highlight lines for brick edges
            val hiCol = Color(0xFFFDBA74).copy(alpha = 0.55f)
            drawLine(color = hiCol, start = Offset(x + 1f*scale, y + 1f*scale), end = Offset(x + sz/2 - 1f*scale, y + 1f*scale), strokeWidth = 1f * scale)
            drawLine(color = hiCol, start = Offset(x + sz/2 + 1f*scale, y + 1f*scale), end = Offset(x + sz - 1f*scale, y + 1f*scale), strokeWidth = 1f * scale)
            drawLine(color = hiCol, start = Offset(x + sz/4 + 1f*scale, y + sz/2 + 1f*scale), end = Offset(x + 3*sz/4 - 1f*scale, y + sz/2 + 1f*scale), strokeWidth = 1f * scale)
        }
        TILE_QUESTION_COIN, TILE_QUESTION_MUSHROOM, TILE_QUESTION_FLOWER, TILE_QUESTION_STAR -> {
            drawPixelMatrix(MysteryBlockMatrix, x, y, sz, sz, true)
        }
        TILE_COIN -> {
            // Elegant, highly reflective gold coin with double glow layer
            drawCircle(color = Color(0xFFD97706), center = Offset(x + sz/2, y + sz/2), radius = 7f * scale) // outer copper frame
            drawCircle(color = Color(0xFFFBBF24), center = Offset(x + sz/2, y + sz/2), radius = 5f * scale) // golden core
            drawCircle(color = Color(0xFFFFFDF0), center = Offset(x + sz/2 - 1.5f * scale, y + sz/2 - 1.5f * scale), radius = 2f * scale) // glossy specular shine
        }
        TILE_PIPE_TL -> {
            // Shiny cylindrical vertical gradient representing top-left pipe lip
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF14532D), Color(0xFF166534), Color(0xFF22C55E), Color(0xFF86EFAC))
                ),
                topLeft = Offset(x, y),
                size = Size(sz, sz)
            )
            drawRect(color = Color.Black, topLeft = Offset(x, y), size = Size(sz, sz), style = Stroke(width = 1.5f * scale))
        }
        TILE_PIPE_TR -> {
            // Shiny cylindrical vertical gradient representing top-right pipe lip
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF166534), Color(0xFF14532D))
                ),
                topLeft = Offset(x, y),
                size = Size(sz, sz)
            )
            drawRect(color = Color.Black, topLeft = Offset(x, y), size = Size(sz, sz), style = Stroke(width = 1.5f * scale))
        }
        TILE_PIPE_BL -> {
            // Ground-level left side pipe body with nice horizontal cylindrical glare
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF14532D), Color(0xFF15803D), Color(0xFF22C55E), Color(0xFF4ADE80))
                ),
                topLeft = Offset(x + 4f * scale, y),
                size = Size(sz - 4f * scale, sz)
            )
            drawRect(color = Color.Black, topLeft = Offset(x + 4f * scale, y), size = Size(sz - 4f * scale, sz), style = Stroke(width = 1.5f * scale))
        }
        TILE_PIPE_BR -> {
            // Ground-level right side pipe body with nice horizontal cylindrical glare
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF15803D), Color(0xFF14532D))
                ),
                topLeft = Offset(x, y),
                size = Size(sz - 4f * scale, sz)
            )
            drawRect(color = Color.Black, topLeft = Offset(x, y), size = Size(sz - 4f * scale, sz), style = Stroke(width = 1.5f * scale))
        }
        TILE_FLAGPOLE -> {
            // Smooth metallic vertical gradient shine on Flagpole
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF64748B), Color(0xFFE2E8F0), Color(0xFF475569))
                ),
                topLeft = Offset(x + sz/2 - 3f * scale, y),
                size = Size(6f * scale, sz)
            )
        }
        TILE_FLAG_TOP -> {
            // Glossy red glass orb for flag top
            drawCircle(color = Color(0xFFB91C1C), center = Offset(x + sz/2, y + sz/2), radius = 6f * scale)
            drawCircle(color = Color(0xFFEF4444), center = Offset(x + sz/2, y + sz/2), radius = 4f * scale)
            drawCircle(color = Color(0xFFFFF1F1), center = Offset(x + sz/2 - 1.5f * scale, y + sz/2 - 1.5f * scale), radius = 1.8f * scale) // glossy bubble point
        }
    }
}

fun DrawScope.drawPowerupEntity(powerup: Powerup, camX: Float, scale: Float) {
    val x = (powerup.x - camX) * scale
    val y = powerup.y * scale
    val w = powerup.width * scale
    val h = powerup.height * scale

    val matrix = when (powerup.type) {
        PowerupType.MUSHROOM -> MushroomMatrix
        PowerupType.FLOWER -> FlowerMatrix
        PowerupType.STAR -> StarMatrix
    }

    drawPixelMatrix(matrix, x, y, w, h, true, powerup.type == PowerupType.STAR)
}

fun DrawScope.drawEnemyEntity(enemy: Enemy, camX: Float, scale: Float) {
    val x = (enemy.x - camX) * scale
    val y = enemy.y * scale
    val w = enemy.width * scale
    val h = enemy.height * scale

    if (enemy.isSquashed) {
        if (enemy.type == EnemyType.KOOPA) {
            drawPixelMatrix(KoopaShellMatrix, x, y + h/2, w, h/2, true)
        } else {
            val flatY = (enemy.y + enemy.height / 2) * scale
            val flatH = (enemy.height / 4) * scale
            drawRect(color = Color(0xFF904A00), topLeft = Offset(x, flatY), size = Size(w, flatH))
        }
        return
    }

    if (enemy.type == EnemyType.KOOPA) {
        if (enemy.isShell) {
            drawPixelMatrix(KoopaShellMatrix, x, y, w, h, true)
        } else {
            drawPixelMatrix(KoopaMatrix, x, y, w, h, enemy.vx > 0f)
        }
    } else {
        drawPixelMatrix(GoombaMatrix, x, y, w, h, true)
    }
}

private val spriteCache = java.util.concurrent.ConcurrentHashMap<String, ImageBitmap>()

fun DrawScope.drawPixelMatrix(
    matrix: Array<IntArray>,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    facingRight: Boolean,
    isStarmanColor: Boolean = false,
    colorSubstitution: Map<Int, Int>? = null
) {
    val rows = matrix.size
    var cols = 0
    for (r in 0 until rows) {
        if (matrix[r].size > cols) {
            cols = matrix[r].size
        }
    }
    if (rows == 0 || cols == 0) return

    val matrixId = System.identityHashCode(matrix)
    val starmanCycle = (System.currentTimeMillis() / 80) % 5
    val substKey = colorSubstitution?.hashCode() ?: 0
    val cacheKey = "${matrixId}_${facingRight}_${isStarmanColor}_${if (isStarmanColor) starmanCycle else 0}_$substKey"

    var cachedBmp = spriteCache[cacheKey]
    if (cachedBmp == null) {
        val bmp = android.graphics.Bitmap.createBitmap(cols, rows, android.graphics.Bitmap.Config.ARGB_8888)
        for (r in 0 until rows) {
            val rowData = matrix[r]
            val cSize = rowData.size
            for (c in 0 until cols) {
                val colorInt = if (c < cSize) {
                    val colorIndex = if (facingRight) rowData[c] else rowData[cSize - 1 - c]
                    if (colorIndex != 0) {
                        var brushCol = when (colorIndex) {
                            1 -> 0xFFE52521.toInt() // Retro Mario Red
                            2 -> 0xFF002286.toInt() // Retro Mario Blue
                            3 -> 0xFFFDC391.toInt() // Peach Skin
                            4 -> 0xFF904A00.toInt() // Brick Brown
                            5 -> 0xFFFFD700.toInt() // Golden Yellow
                            6 -> 0xFF111111.toInt() // Dark Gray / Black
                            7 -> 0xFF00A800.toInt() // Retro Green
                            else -> 0xFFFFFFFF.toInt()
                        }
                        if (colorSubstitution != null && colorSubstitution.containsKey(colorIndex)) {
                            brushCol = colorSubstitution[colorIndex]!!
                        }
                        if (isStarmanColor && colorIndex in 1..5) {
                            brushCol = when ((colorIndex + starmanCycle) % 5) {
                                0L -> 0xFFEC4899.toInt() // Neon Pink
                                1L -> 0xFF3B82F6.toInt() // Neon Blue
                                2L -> 0xFF10B981.toInt() // Neon Green
                                3L -> 0xFFF59E0B.toInt() // Neon Orange
                                else -> 0xFFEF4444.toInt() // Neon Red
                            }
                        }
                        brushCol
                    } else {
                        0x00000000 // Transparent
                    }
                } else {
                    0x00000000 // Transparent
                }
                bmp.setPixel(c, r, colorInt)
            }
        }
        cachedBmp = bmp.asImageBitmap()
        spriteCache[cacheKey] = cachedBmp
    }

    drawImage(
        image = cachedBmp,
        dstOffset = IntOffset(x.toInt(), y.toInt()),
        dstSize = IntSize(width.toInt(), height.toInt()),
        filterQuality = FilterQuality.None
    )
}

@Composable
fun MarioLeaderboardScreen(viewModel: MarioViewModel) {
    val scoresList by viewModel.topScores.collectAsState()

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star, 
                    contentDescription = "Leaderboard Gold", 
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HALL OF FAME",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFFD700),
                    letterSpacing = 1.sp
                )
            }

            // Direct play button
            IconButton(
                onClick = { viewModel.exitToMain() },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0x22FFFFFF), CircleShape)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), CircleShape)
                    .testTag("mario_home_btn")
            ) {
                Icon(Icons.Default.Home, "Go Home", tint = Color(0xFFF43F5E), modifier = Modifier.size(24.dp))
            }
        }

        // Leaderboard cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(
                    BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color(0x66FFFFFF), Color(0x11FFFFFF)))),
                    RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x10FFFFFF))
        ) {
            if (scoresList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO HIGH SCORES YET\n\nBE THE FIRST TO SET A RECORD!",
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(scoresList, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF1E1B4B).copy(alpha = 0.7f), Color(0xFF131034).copy(alpha = 0.7f))
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color(0xFFF43F5E), Color(0xFFBE123C))
                                            ), 
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.playerName.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.playerName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = item.levelName,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.score} PTS",
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFBBF24),
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = formatter.format(Date(item.timestamp)),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.purgeLeaderboard() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF450A0A)),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .border(BorderStroke(1.dp, Color(0xFF991B1B)), RoundedCornerShape(14.dp))
                    .testTag("reset_leaderboard_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Delete, "Reset", tint = Color(0xFFFCA5A5), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CLEAR ALL RECS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFFFCA5A5)
                )
            }

            Button(
                onClick = { viewModel.startGame(1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("controller_play_again_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PLAY WORLD 1-1",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun Modifier.retroCrtOverlay(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "crt_effects")
    
    // Slow rolling beam (12 seconds per loop)
    val rollingBeamY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rolling_beam_y"
    )

    // Rapid subtle CRT phosphor flicker
    val flickerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.015f,
        targetValue = 0.045f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 300
                0.015f at 0
                0.040f at 60
                0.010f at 125
                0.045f at 180
                0.020f at 240
                0.035f at 300
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker_alpha"
    )

    return this.drawWithContent {
        // 1. Draw the actual screen content beneath
        drawContent()

        // 2. Draw CRT Micro-Scanlines across horizontal coordinates
        val lineSpacing = 6f
        var currentY = 0f
        val scanlineColor = Color.Black.copy(alpha = 0.22f)
        
        while (currentY < size.height) {
            drawLine(
                color = scanlineColor,
                start = Offset(0f, currentY),
                end = Offset(size.width, currentY),
                strokeWidth = 1.5f
            )
            currentY += lineSpacing
        }

        // 3. Draw phosphor glow tint with flicker modulation
        drawRect(
            color = Color(0xFFB4E49B).copy(alpha = flickerAlpha),
            size = size
        )

        // 4. Draw rolling CRT V-Sync refresh beam/shutter bar
        val beamCenter = rollingBeamY * size.height
        val beamHeight = size.height * 0.15f
        
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFB4E49B).copy(alpha = 0.03f),
                    Color(0xFFB4E49B).copy(alpha = 0.12f),
                    Color(0xFFB4E49B).copy(alpha = 0.03f),
                    Color.Transparent
                ),
                startY = beamCenter - beamHeight / 2f,
                endY = beamCenter + beamHeight / 2f
            ),
            topLeft = Offset(0f, beamCenter - beamHeight / 2f),
            size = Size(size.width, beamHeight)
        )

        if (beamCenter + beamHeight / 2f > size.height) {
            val wrapCenter = beamCenter - size.height
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFB4E49B).copy(alpha = 0.03f),
                        Color(0xFFB4E49B).copy(alpha = 0.12f),
                        Color(0xFFB4E49B).copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    startY = wrapCenter - beamHeight / 2f,
                    endY = wrapCenter + beamHeight / 2f
                ),
                topLeft = Offset(0f, wrapCenter - beamHeight / 2f),
                size = Size(size.width, beamHeight)
            )
        }

        // 5. Curved Vignette shadow overlay to simulate curved glass CRT display 
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.08f),
                    Color.Black.copy(alpha = 0.45f),
                    Color.Black.copy(alpha = 0.80f)
                ),
                center = center,
                radius = size.maxDimension * 0.72f
            ),
            size = size
        )
    }
}

@Composable
fun RetroArcadeStartBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "arcade_bg_animations")
    
    // Smooth looping drift offsets for fluffy summer clouds
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 650f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_scroll_1"
    )
    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 550f,
        targetValue = -250f,
        animationSpec = infiniteRepeatable(
            animation = tween(36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_scroll_2"
    )

    // Bowser breathing high-boss float index
    val bowserBobbing by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bowser_bob"
    )

    // Gentle floating offsets for the main heroes
    val marioBobbing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -24f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mario_bob"
    )
    val luigiBobbing by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "luigi_bob"
    )

    val sparksTime = System.currentTimeMillis()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2FE))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Gorgeous Summer Kingdom gradient sky backdrop
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0284C7), // Rich Saturated summer blue
                        Color(0xFF38BDF8), // Radiant warm cyan sky
                        Color(0xFFBAE6FD)  // Clean cream mist near horizon
                    )
                ),
                size = size
            )

            // 2. Draw drifting fluffy white clouds
            // Cloud 1
            drawCircle(Color.White.copy(alpha = 0.85f), radius = 35f, center = Offset(cloudOffset1, h * 0.12f))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = 50f, center = Offset(cloudOffset1 + 35f, h * 0.12f))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = 35f, center = Offset(cloudOffset1 + 70f, h * 0.12f))
            drawRect(Color.White.copy(alpha = 0.85f), topLeft = Offset(cloudOffset1, h * 0.12f), size = Size(70f, 38f))

            // Cloud 2
            drawCircle(Color.White.copy(alpha = 0.75f), radius = 25f, center = Offset(cloudOffset2, h * 0.22f))
            drawCircle(Color.White.copy(alpha = 0.75f), radius = 38f, center = Offset(cloudOffset2 + 25f, h * 0.22f))
            drawCircle(Color.White.copy(alpha = 0.75f), radius = 25f, center = Offset(cloudOffset2 + 50f, h * 0.22f))
            drawRect(Color.White.copy(alpha = 0.75f), topLeft = Offset(cloudOffset2, h * 0.22f), size = Size(50f, 28f))

            // 3. Draw Huge Menacing Shadow Bowser looming in the sky
            val bX = w * 0.5f
            val bY = h * 0.24f + bowserBobbing
            
            // Outer evil boss aura glow (neon ruby outline)
            drawCircle(Color(0xFFF43F5E).copy(alpha = 0.45f), radius = 130f, center = Offset(bX, bY))
            // Main Bowser silhouette circle in deep mystic boss indigo
            drawCircle(Color(0xFF2E195E).copy(alpha = 0.95f), radius = 115f, center = Offset(bX, bY))
            
            // Spiked shell outline bumps
            val shellSpikeRadius = 112f
            for (ang in 40..140 step 25) {
                val rad = Math.toRadians(ang.toDouble())
                val spikeX = bX + Math.cos(rad).toFloat() * shellSpikeRadius
                val spikeY = bY + Math.sin(rad).toFloat() * shellSpikeRadius
                drawCircle(Color(0xFFF97316), radius = 12f, center = Offset(spikeX, spikeY))
                drawCircle(Color(0xFF2E195E), radius = 8f, center = Offset(spikeX, spikeY))
            }

            // Big yellow crown/horn spike points
            val leftHorn = Path().apply {
                moveTo(bX - 55f, bY - 65f)
                lineTo(bX - 90f, bY - 120f)
                lineTo(bX - 25f, bY - 85f)
                close()
            }
            drawPath(leftHorn, Color(0xFFFCD34D)) // Golden Yellow
            val rightHorn = Path().apply {
                moveTo(bX + 55f, bY - 65f)
                lineTo(bX + 90f, bY - 120f)
                lineTo(bX + 25f, bY - 85f)
                close()
            }
            drawPath(rightHorn, Color(0xFFFCD34D))

            // Ghoulish glowing neon-red evil predator eyes
            drawCircle(Color(0xFFEF4444), radius = 16f, center = Offset(bX - 35f, bY - 18f))
            drawCircle(Color(0xFFFDE047), radius = 6f, center = Offset(bX - 32f, bY - 16f))
            drawCircle(Color(0xFFEF4444), radius = 16f, center = Offset(bX + 35f, bY - 18f))
            drawCircle(Color(0xFFFDE047), radius = 6f, center = Offset(bX + 29f, bY - 16f))

            // Predator snout and razor-sharp white fangs breathing dark power
            drawCircle(Color(0xFF13072E), radius = 32f, center = Offset(bX, bY + 28f))
            val fang1 = Path().apply {
                moveTo(bX - 16f, bY + 20f)
                lineTo(bX - 22f, bY + 38f)
                lineTo(bX - 8f, bY + 20f)
                close()
            }
            drawPath(fang1, Color.White)
            val fang2 = Path().apply {
                moveTo(bX + 6f, bY + 20f)
                lineTo(bX + 12f, bY + 38f)
                lineTo(bX + 18f, bY + 20f)
                close()
            }
            drawPath(fang2, Color.White)

            // Dynamic fire-ember micro particles floating out of Bowser's jaws
            for (i in 0..5) {
                val sparkX = bX + Math.sin((sparksTime / 240.0) + i).toFloat() * 75f
                val sparkY = bY + 55f + ((sparksTime / 12 + i * 45) % 190f)
                if (sparkY < h * 0.72f) {
                    drawCircle(
                        color = Color(0xFFF97316).copy(alpha = 0.8f - (sparkY - bY) / 240f),
                        radius = 4f + (i % 3) * 2.5f,
                        center = Offset(sparkX, sparkY)
                    )
                }
            }

            // 4. Draw Parallax green rolling hills
            drawCircle(Color(0xFF15803D), radius = w * 0.52f, center = Offset(w * 0.18f, h * 0.74f))
            drawCircle(Color(0xFF166534), radius = w * 0.58f, center = Offset(w * 0.82f, h * 0.77f))

            // Main round central grass hill
            val hillCX = w * 0.5f
            val hillCY = h * 0.69f
            drawCircle(Color(0xFF22C55E), radius = w * 0.45f, center = Offset(hillCX, hillCY))

            // 5. Draw Highly detailed vector Princess Peach's Castle in the back
            val cWX = hillCX
            val cWY = h * 0.49f
            
            // Base structure
            drawRect(Color(0xFFFFF1F2), topLeft = Offset(cWX - 55f, cWY), size = Size(110f, 65f))
            drawRect(Color(0xFFFFF1F2), topLeft = Offset(cWX - 30f, cWY - 40f), size = Size(60f, 40f))
            
            drawRect(Color(0xFFF43F5E).copy(alpha = 0.28f), topLeft = Offset(cWX - 38f, cWY + 10f), size = Size(14f, 8f))
            drawRect(Color(0xFFF43F5E).copy(alpha = 0.28f), topLeft = Offset(cWX + 25f, cWY + 35f), size = Size(14f, 8f))

            // Central tower spire
            drawRect(Color(0xFFFFF1F2), topLeft = Offset(cWX - 12f, cWY - 95f), size = Size(24f, 55f))
            
            val mainRoof = Path().apply {
                moveTo(cWX - 16f, cWY - 95f)
                lineTo(cWX, cWY - 122f)
                lineTo(cWX + 16f, cWY - 95f)
                close()
            }
            drawPath(mainRoof, Color(0xFFF43F5E))
            drawCircle(Color(0xFFFFD700), radius = 4f, center = Offset(cWX, cWY - 122f))
            val flagPath = Path().apply {
                moveTo(cWX, cWY - 122f)
                lineTo(cWX - 20f, cWY - 117f)
                lineTo(cWX, cWY - 112f)
                close()
            }
            drawPath(flagPath, Color(0xFFEF4444))

            // Side towers
            drawRect(Color(0xFFFFF1F2), topLeft = Offset(cWX - 72f, cWY - 15f), size = Size(18f, 80f))
            val sideLeftRoof = Path().apply {
                moveTo(cWX - 75f, cWY - 15f)
                lineTo(cWX - 63f, cWY - 35f)
                lineTo(cWX - 51f, cWY - 15f)
                close()
            }
            drawPath(sideLeftRoof, Color(0xFFEC4899))
            drawCircle(Color(0xFFFFD700), radius = 3f, center = Offset(cWX - 63f, cWY - 35f))

            drawRect(Color(0xFFFFF1F2), topLeft = Offset(cWX + 54f, cWY - 15f), size = Size(18f, 80f))
            val sideRightRoof = Path().apply {
                moveTo(cWX + 51f, cWY - 15f)
                lineTo(cWX + 63f, cWY - 35f)
                lineTo(cWX + 75f, cWY - 15f)
                close()
            }
            drawPath(sideRightRoof, Color(0xFFEC4899))
            drawCircle(Color(0xFFFFD700), radius = 3f, center = Offset(cWX + 63f, cWY - 35f))

            val doorW = 22f
            val doorH = 26f
            drawRect(Color(0xFF854D0E), topLeft = Offset(cWX - doorW/2, cWY + 65f - doorH), size = Size(doorW, doorH))
            drawCircle(Color(0xFF3F2104), radius = doorW/2, center = Offset(cWX, cWY + 65f - doorH))

            drawCircle(Color(0xFF06B6D4), radius = 10f, center = Offset(cWX, cWY - 15f))
            drawCircle(Color(0xFFFFD700), radius = 12f, center = Offset(cWX, cWY - 15f), style = Stroke(width = 2f))

            // 6. Draw Traditional Ground Grid / Checkered brick deck
            val groundY = h * 0.68f
            drawRect(Color(0xFF166534), topLeft = Offset(0f, groundY), size = Size(w, h - groundY))
            drawRect(Color(0xFFD97706), topLeft = Offset(0f, groundY + 12f), size = Size(w, h - (groundY + 12f)))

            val groundBlockSize = 26f
            val colCount = (w / groundBlockSize).toInt() + 1
            for (gi in 0..colCount) {
                val blockX = gi * groundBlockSize
                drawLine(
                    color = Color(0xFF92400E),
                    start = Offset(blockX, groundY + 12f),
                    end = Offset(blockX, h),
                    strokeWidth = 2.5f
                )
                drawCircle(
                    color = Color(0xFF166534),
                    radius = 9f,
                    center = Offset(blockX, groundY + 10f)
                )
                drawCircle(
                    color = Color(0xFF22C55E),
                    radius = 6.5f,
                    center = Offset(blockX, groundY + 10f)
                )
            }

            // 7. Render high-quality character assemblies
            // HERO MARIO
            val mSize = w * 0.17f
            drawPixelMatrix(
                matrix = BigMarioMatrix,
                x = w * 0.42f,
                y = h * 0.55f + marioBobbing,
                width = mSize,
                height = mSize,
                facingRight = true
            )

            // HERO LUIGI
            val luigiRecolor = mapOf(
                1 to 0xFF00A800.toInt(),
                2 to 0xFF021B59.toInt()
            )
            val lSize = w * 0.16f
            drawPixelMatrix(
                matrix = BigMarioMatrix,
                x = w * 0.13f,
                y = h * 0.58f + luigiBobbing,
                width = lSize,
                height = lSize,
                facingRight = true,
                colorSubstitution = luigiRecolor
            )

            // PRINCESS PEACH
            val pSize = w * 0.12f
            drawPixelMatrix(
                matrix = PeachStartMatrix,
                x = w * 0.69f,
                y = h * 0.60f + marioBobbing * 0.4f,
                width = pSize,
                height = pSize * 1.1f,
                facingRight = true
            )

            // TOAD
            val tSize = w * 0.10f
            drawPixelMatrix(
                matrix = ToadStartMatrix,
                x = w * 0.81f,
                y = h * 0.62f + luigiBobbing * 0.3f,
                width = tSize,
                height = tSize * 1.1f,
                facingRight = true
            )
        }
    }
}

