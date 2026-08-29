package com.example.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameRenderer
import com.example.model.AppScreen
import com.example.model.WorldType
import com.example.ui.GameViewModel
import com.example.ui.theme.CarrotOrange
import com.example.ui.theme.DeepGrassGreen
import com.example.ui.theme.DeepOrange
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyOrange

@Composable
fun WorldSelectScreen(viewModel: GameViewModel) {
    val userData by viewModel.userData.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE0F7FA),
                        Color(0xFFB2EBF2),
                        Color(0xFF80DEEA)
                    )
                )
            )
            .testTag("world_select_screen")
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
                        .testTag("worlds_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepOrange
                    )
                }

                Text(
                    text = "🌍 WORLDS 🌍",
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

            // Worlds List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(WorldType.entries) { world ->
                    val isUnlocked = userData.unlockedWorlds.contains(world.id)
                    val isSelected = userData.selectedWorldId == world.id
                    val canAffordUnlock = userData.totalCoins >= world.unlockCoins && userData.bestScore >= world.unlockScore

                    WorldCard(
                        world = world,
                        isUnlocked = isUnlocked,
                        isSelected = isSelected,
                        canAffordUnlock = canAffordUnlock,
                        bestScore = userData.bestScore,
                        totalCoins = userData.totalCoins,
                        onSelect = { viewModel.selectWorld(world.id) },
                        onUnlock = { viewModel.unlockWorld(world) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldCard(
    world: WorldType,
    isUnlocked: Boolean,
    isSelected: Boolean,
    canAffordUnlock: Boolean,
    bestScore: Int,
    totalCoins: Int,
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
            .testTag("world_card_${world.id.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Scenic World Preview Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    GameRenderer.drawWorldBackground(
                        drawScope = this,
                        world = world,
                        distanceTraveled = 200f,
                        groundY = size.height * 0.72f
                    )
                }

                // World name chip on top of banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = world.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = world.subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DeepOrange
            )

            Text(
                text = world.ambientDescription,
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
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
                            .height(40.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Active World", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ACTIVE WORLD", fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
                isUnlocked -> {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSkyBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("select_world_${world.id.lowercase()}")
                    ) {
                        Text("PLAY THIS WORLD", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
                else -> {
                    val scoreMet = bestScore >= world.unlockScore
                    val coinsMet = totalCoins >= world.unlockCoins

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Requires: ${world.unlockCoins} 🪙 ${if (coinsMet) "✓" else "✗"} & ${world.unlockScore} Score ${if (scoreMet) "✓" else "✗"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAffordUnlock) DeepGrassGreen else Color.Gray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Button(
                            onClick = onUnlock,
                            enabled = canAffordUnlock,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SunnyOrange,
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("unlock_world_${world.id.lowercase()}")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UNLOCK WORLD",
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
