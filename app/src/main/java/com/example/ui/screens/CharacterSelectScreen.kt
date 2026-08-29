package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameRenderer
import com.example.model.AppScreen
import com.example.model.CharacterType
import com.example.ui.GameViewModel
import com.example.ui.theme.CarrotOrange
import com.example.ui.theme.DeepGrassGreen
import com.example.ui.theme.DeepOrange
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyOrange

@Composable
fun CharacterSelectScreen(viewModel: GameViewModel) {
    val userData by viewModel.userData.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "char_anim")
    val idleFrame by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "char_idle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFE0B2),
                        Color(0xFFFFCC80)
                    )
                )
            )
            .testTag("character_select_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .testTag("char_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepOrange
                    )
                }

                Text(
                    text = "🐾 CHARACTERS 🐾",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepOrange
                )

                // Total Coins Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userData.totalCoins}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF57F17)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Characters List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(CharacterType.entries) { character ->
                    val isUnlocked = userData.unlockedCharacters.contains(character.id)
                    val isSelected = userData.selectedCharacterId == character.id
                    val canAfford = userData.totalCoins >= character.cost

                    CharacterCard(
                        character = character,
                        isUnlocked = isUnlocked,
                        isSelected = isSelected,
                        canAfford = canAfford,
                        idleFrame = idleFrame,
                        onSelect = { viewModel.selectCharacter(character.id) },
                        onUnlock = { viewModel.unlockCharacter(character) }
                    )
                }

                item {
                    // Quick testing helper banner
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Want to try everyone?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepOrange
                                )
                                Text(
                                    text = "Tap to add bonus coins for easy unlocking!",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Button(
                                onClick = { viewModel.addTestCoins(500) },
                                colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("+500 🪙", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: CharacterType,
    isUnlocked: Boolean,
    isSelected: Boolean,
    canAfford: Boolean,
    idleFrame: Float,
    onSelect: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("char_card_${character.id.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Mini Mascot Preview Canvas
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = character.secondaryColor.copy(alpha = 0.2f),
                modifier = Modifier.size(90.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height * 0.72f

                    GameRenderer.drawPlayer(
                        drawScope = this,
                        character = character,
                        playerX = cx,
                        playerY = cy,
                        isSliding = false,
                        isJumping = false,
                        runFrame = idleFrame,
                        isInvulnerable = false,
                        hasCarrotBoost = false
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Character Info & Action
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = character.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepOrange
                    )
                    if (character.cost == 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GrassGreen
                        ) {
                            Text(
                                text = "FREE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "✨ ${character.perkName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepGrassGreen
                )

                Text(
                    text = character.perkDescription,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                when {
                    isSelected -> {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = GrassGreen,
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SELECTED", fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    isUnlocked -> {
                        Button(
                            onClick = onSelect,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepSkyBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("select_char_${character.id.lowercase()}")
                        ) {
                            Text("SELECT", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onUnlock,
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SunnyOrange,
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("unlock_char_${character.id.lowercase()}")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "UNLOCK (${character.cost} 🪙)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
