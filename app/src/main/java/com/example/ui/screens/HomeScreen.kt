package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.game.GameRenderer
import com.example.model.AppScreen
import com.example.model.CharacterType
import com.example.model.WorldType
import com.example.ui.GameViewModel
import com.example.ui.theme.CarrotOrange
import com.example.ui.theme.DeepGrassGreen
import com.example.ui.theme.DeepOrange
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.LavenderPurple
import com.example.ui.theme.OffWhite
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyOrange

@Composable
fun HomeScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val userData by viewModel.userData.collectAsState()
    val selectedCharacter = CharacterType.fromId(userData.selectedCharacterId)
    val selectedWorld = WorldType.fromId(userData.selectedWorldId)

    val infiniteTransition = rememberInfiniteTransition(label = "home_bounce")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_pulse"
    )

    val idleRunFrame by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "idle_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        selectedWorld.skyColorTop,
                        selectedWorld.skyColorBottom,
                        selectedWorld.groundColor
                    )
                )
            )
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Stats Bar (Best Score & Coins)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Best Score Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 6.dp,
                    modifier = Modifier.testTag("best_score_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Best Score",
                            tint = SunnyOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "BEST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "${userData.bestScore}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = DeepOrange
                            )
                        }
                    }
                }

                // Total Coins Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 6.dp,
                    modifier = Modifier.testTag("coins_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪙",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "COINS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "${userData.totalCoins}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF57F17)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Title Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "🐰 BUNNY ADVENTURE 🥕",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = CarrotOrange,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Kids Run • World: ${selectedWorld.displayName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepGrassGreen
                        )
                    }
                }
            }

            // Interactive Animated Character Mascot Showcase
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.soundManager.playJump()
                            }
                    ) {
                        val cx = size.width / 2f
                        val cy = size.height * 0.65f

                        GameRenderer.drawPlayer(
                            drawScope = this,
                            character = selectedCharacter,
                            playerX = cx,
                            playerY = cy,
                            isSliding = false,
                            isJumping = false,
                            runFrame = idleRunFrame,
                            isInvulnerable = false,
                            hasCarrotBoost = false
                        )
                    }

                    // Pet Name & Perk Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = selectedCharacter.accentColor.copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Tap ${selectedCharacter.displayName}! • ${selectedCharacter.perkName}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Menu Buttons (Child-Friendly, Big Rounded Targets)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Giant PLAY Button
                Button(
                    onClick = { viewModel.startGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                    shape = RoundedCornerShape(26.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .scale(pulseScale)
                        .testTag("play_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PLAY NOW!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Row for CHARACTERS and WORLDS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuSubButton(
                        title = "CHARACTERS",
                        subtitle = "${selectedCharacter.displayName} 🐾",
                        icon = Icons.Default.Face,
                        backgroundColor = SunnyOrange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("characters_button")
                    ) {
                        viewModel.navigateTo(AppScreen.CHARACTERS)
                    }

                    MenuSubButton(
                        title = "WORLDS",
                        subtitle = "${selectedWorld.displayName} 🌍",
                        icon = Icons.Default.Public,
                        backgroundColor = DeepSkyBlue,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("worlds_button")
                    ) {
                        viewModel.navigateTo(AppScreen.WORLDS)
                    }
                }

                // SETTINGS Button
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPurple),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("settings_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SETTINGS & SOUNDS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // FREE COINS REWARDED AD BUTTON
                Button(
                    onClick = {
                        if (activity != null) {
                            AdMobManager.showRewardedAd(
                                activity = activity,
                                onUserEarnedReward = { _, _ ->
                                    viewModel.claimAdBonusCoins(50)
                                }
                            )
                        } else {
                            viewModel.claimAdBonusCoins(50)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("free_coins_ad_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = "Watch Video for Free Coins",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FREE +50 COINS (WATCH AD) 🪙",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MenuSubButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(22.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        modifier = modifier.height(62.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
