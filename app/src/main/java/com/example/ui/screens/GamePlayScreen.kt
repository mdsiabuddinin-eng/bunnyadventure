package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ads.AdMobBanner
import com.example.ads.AdMobManager
import com.example.game.GameRenderer
import com.example.model.AppScreen
import com.example.model.CharacterType
import com.example.model.WorldType
import com.example.ui.GameOverData
import com.example.ui.GameViewModel
import com.example.ui.theme.CarrotOrange
import com.example.ui.theme.DeepGrassGreen
import com.example.ui.theme.DeepOrange
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.LavenderPurple
import com.example.ui.theme.OverlayBlack
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyOrange
import kotlin.math.abs

@Composable
fun GamePlayScreen(viewModel: GameViewModel) {
    val engine = viewModel.gameEngine
    val userData by viewModel.userData.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val gameOverData by viewModel.gameOverData.collectAsState()

    val liveScore by viewModel.liveScore.collectAsState()
    val liveCoins by viewModel.liveCoins.collectAsState()
    val liveLives by viewModel.liveLives.collectAsState()

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    // Synchronize engine screen dimensions
    LaunchedEffect(screenWidthPx, screenHeightPx) {
        engine.screenWidth = screenWidthPx
        engine.screenHeight = screenHeightPx
    }

    // 60FPS Game Loop using withFrameNanos
    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPaused, gameOverData) {
        lastFrameTimeNanos = 0L
        while (!engine.isGameOver && !isPaused) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    engine.update(dt)
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("game_screen")
    ) {
        // Main Interactive Game Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Tap to jump
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            viewModel.jump()
                        }
                    )
                }
                // Swipe down to slide, swipe up to jump
                .pointerInput(Unit) {
                    var totalDragY = 0f
                    detectDragGestures(
                        onDragStart = { totalDragY = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragY += dragAmount.y
                            if (totalDragY > 40f) {
                                viewModel.slide()
                                totalDragY = 0f
                            } else if (totalDragY < -40f) {
                                viewModel.jump()
                                totalDragY = 0f
                            }
                        }
                    )
                }
        ) {
            val groundY = engine.groundY

            // 1. Draw World Background & Scenery
            GameRenderer.drawWorldBackground(
                drawScope = this,
                world = engine.currentWorld,
                distanceTraveled = engine.distanceTraveled,
                groundY = groundY
            )

            // 2. Draw Collectibles
            engine.collectibles.forEach { item ->
                if (!item.isCollected) {
                    GameRenderer.drawCollectible(this, item)
                }
            }

            // 3. Draw Obstacles
            engine.obstacles.forEach { obs ->
                if (!obs.isHit) {
                    GameRenderer.drawObstacle(this, obs)
                }
            }

            // 4. Draw Particles (Dust, Sparkles, Rainbow trail)
            engine.particles.forEach { particle ->
                GameRenderer.drawParticle(this, particle)
            }

            // 5. Draw Player Character
            GameRenderer.drawPlayer(
                drawScope = this,
                character = engine.currentCharacter,
                playerX = engine.playerX,
                playerY = engine.playerY,
                isSliding = engine.isSliding,
                isJumping = !engine.isGrounded,
                runFrame = engine.runAnimFrame,
                isInvulnerable = engine.isInvulnerable,
                hasCarrotBoost = engine.hasCarrotBoost
            )
        }

        // --- HUD Overlay (Top Screen) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 6.dp,
                    modifier = Modifier.testTag("hud_score")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$liveScore",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepOrange
                        )
                    }
                }

                // Hearts / Lives Display
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 6.dp,
                    modifier = Modifier.testTag("hud_lives")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..engine.maxLives) {
                            val isHeartActive = i <= liveLives
                            Icon(
                                imageVector = if (isHeartActive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Heart $i",
                                tint = if (isHeartActive) HeartRed else Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Coins Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 6.dp,
                    modifier = Modifier.testTag("hud_coins")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$liveCoins",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF57F17)
                        )
                    }
                }

                // Pause Button
                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = DeepOrange,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Carrot Boost Active Powerup Indicator
            if (engine.hasCarrotBoost) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🥕 SUPER CARROT BOOST! 🌈",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CarrotOrange
                            )
                            Text(
                                text = "${String.format("%.1f", engine.carrotBoostTimeRemaining)}s",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (engine.carrotBoostTimeRemaining / engine.carrotBoostMaxDuration).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CarrotOrange,
                            trackColor = PastelPink
                        )
                    }
                }
            }
        }

        // --- On-Screen Touch Buttons for Kids (Bottom Corners) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Large SLIDE Button (Bottom Left)
            Button(
                onClick = { viewModel.slide() },
                colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(width = 130.dp, height = 72.dp)
                    .testTag("slide_action_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Slide Down",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "SLIDE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // Large JUMP Button (Bottom Right)
            Button(
                onClick = { viewModel.jump() },
                colors = ButtonDefaults.buttonColors(containerColor = GrassGreen.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(width = 130.dp, height = 72.dp)
                    .testTag("jump_action_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Jump Up",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "JUMP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // --- PAUSE DIALOG ---
        if (isPaused) {
            PauseGameDialog(
                viewModel = viewModel,
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartGame() },
                onHome = { viewModel.returnToHome() }
            )
        }

        // --- GAME OVER DIALOG ---
        gameOverData?.let { data ->
            GameOverScreenDialog(
                viewModel = viewModel,
                data = data,
                onRetry = { viewModel.startGame() },
                onWorlds = { viewModel.navigateTo(AppScreen.WORLDS) },
                onHome = { viewModel.returnToHome() }
            )
        }
    }
}

@Composable
private fun PauseGameDialog(
    viewModel: GameViewModel,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val userData by viewModel.userData.collectAsState()

    Dialog(
        onDismissRequest = onResume,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("pause_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⏸️ GAME PAUSED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepOrange,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Quick Audio Toggles in Pause Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { viewModel.toggleMusic() },
                        modifier = Modifier
                            .size(50.dp)
                            .background(if (userData.musicEnabled) SunnyOrange else Color.LightGray, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (userData.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "Toggle Music",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleSfx() },
                        modifier = Modifier
                            .size(50.dp)
                            .background(if (userData.sfxEnabled) SkyBlue else Color.LightGray, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (userData.sfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound Effects",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("resume_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESUME", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("restart_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESTART", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPurple),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("home_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("HOME", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun GameOverScreenDialog(
    viewModel: GameViewModel,
    data: GameOverData,
    onRetry: () -> Unit,
    onWorlds: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isReviveAvailable by viewModel.isReviveAvailable.collectAsState()
    val hasDoubledCoins by viewModel.hasDoubledCoins.collectAsState()

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .testTag("game_over_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Text(
                    text = if (data.isNewBestScore) "🏆 NEW RECORD! 🏆" else "🥕 GREAT RUN! 🥕",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (data.isNewBestScore) SunnyOrange else CarrotOrange,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "GAME OVER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Score stats card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF1F8E9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Score:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF33691E))
                            Text("${data.score}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = DeepOrange)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Best Score:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("${data.bestScore}", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Coins Collected:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("+${data.coinsEarned} 🪙", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFFF57F17))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // REWARDED VIDEO AD 1: Revive / Extra Life
                if (isReviveAvailable) {
                    Button(
                        onClick = {
                            if (activity != null) {
                                AdMobManager.showRewardedAd(
                                    activity = activity,
                                    onUserEarnedReward = { _, _ ->
                                        viewModel.reviveRun()
                                    }
                                )
                            } else {
                                viewModel.reviveRun()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepOrange),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("revive_ad_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = "Watch Ad for Revive",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REVIVE & CONTINUE (+1 ❤️)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // REWARDED VIDEO AD 2: Double Coins
                if (!hasDoubledCoins && data.coinsEarned > 0) {
                    Button(
                        onClick = {
                            if (activity != null) {
                                AdMobManager.showRewardedAd(
                                    activity = activity,
                                    onUserEarnedReward = { _, _ ->
                                        viewModel.doubleGameOverCoins()
                                    }
                                )
                            } else {
                                viewModel.doubleGameOverCoins()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("double_coins_ad_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Watch Ad to Double Coins",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DOUBLE COINS (2X 🪙)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Play Again Button with Interstitial
                Button(
                    onClick = {
                        if (activity != null) {
                            AdMobManager.showInterstitialAd(
                                activity = activity,
                                onDismissed = onRetry
                            )
                        } else {
                            onRetry()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("retry_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.White, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PLAY AGAIN!", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (activity != null) {
                                AdMobManager.showInterstitialAd(
                                    activity = activity,
                                    onDismissed = onWorlds
                                )
                            } else {
                                onWorlds()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSkyBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("game_over_worlds_button")
                    ) {
                        Text("WORLDS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (activity != null) {
                                AdMobManager.showInterstitialAd(
                                    activity = activity,
                                    onDismissed = onHome
                                )
                            } else {
                                onHome()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPurple),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("game_over_home_button")
                    ) {
                        Text("HOME", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AdMob Banner inside Game Over Dialog
                AdMobBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}
