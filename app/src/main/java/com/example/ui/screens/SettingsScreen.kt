package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.CarrotOrange
import com.example.ui.theme.DeepGrassGreen
import com.example.ui.theme.DeepOrange
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.LavenderPurple
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyOrange

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val userData by viewModel.userData.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFE1BEE7),
                        Color(0xFFCE93D8)
                    )
                )
            )
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
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
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepPurple
                    )
                }

                Text(
                    text = "⚙️ SETTINGS ⚙️",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepPurple
                )

                // Spacer for symmetry
                Spacer(modifier = Modifier.size(46.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio & Play Options Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "AUDIO & CONTROLS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepPurple
                    )

                    // Music Toggle
                    SettingToggleRow(
                        title = "Background Music",
                        subtitle = "Cheerful melody during runs",
                        icon = Icons.Default.MusicNote,
                        iconTint = SunnyOrange,
                        isChecked = userData.musicEnabled,
                        onCheckedChange = { viewModel.toggleMusic() },
                        tag = "toggle_music"
                    )

                    // SFX Toggle
                    SettingToggleRow(
                        title = "Sound Effects",
                        subtitle = "Coins, jumps, carrots & pops",
                        icon = Icons.Default.VolumeUp,
                        iconTint = SkyBlue,
                        isChecked = userData.sfxEnabled,
                        onCheckedChange = { viewModel.toggleSfx() },
                        tag = "toggle_sfx"
                    )

                    // Haptics Toggle
                    SettingToggleRow(
                        title = "Soft Vibration",
                        subtitle = "Gentle taps on jumps & bonuses",
                        icon = Icons.Default.Vibration,
                        iconTint = GrassGreen,
                        isChecked = userData.hapticsEnabled,
                        onCheckedChange = { viewModel.toggleHaptics() },
                        tag = "toggle_haptics"
                    )

                    // Child Friendly Mode
                    SettingToggleRow(
                        title = "Child Friendly Mode",
                        subtitle = "Gentler speed ramp for little runners",
                        icon = Icons.Default.ChildCare,
                        iconTint = CarrotOrange,
                        isChecked = userData.childModeEnabled,
                        onCheckedChange = { viewModel.toggleChildMode() },
                        tag = "toggle_child_mode"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Player Career Stats Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🏆 PLAYER CAREER STATS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepPurple
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Best Score:", color = Color.Gray, fontSize = 14.sp)
                        Text("${userData.bestScore} ⭐", fontWeight = FontWeight.Bold, color = DeepOrange, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Coins Bank:", color = Color.Gray, fontSize = 14.sp)
                        Text("${userData.totalCoins} 🪙", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Carrots Munched:", color = Color.Gray, fontSize = 14.sp)
                        Text("${userData.totalCarrotsCollected} 🥕", fontWeight = FontWeight.Bold, color = CarrotOrange, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Adventures Run:", color = Color.Gray, fontSize = 14.sp)
                        Text("${userData.totalRuns} 🏃", fontWeight = FontWeight.Bold, color = DeepGrassGreen, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Progress Button
            Button(
                onClick = { showResetConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reset_progress_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Reset", tint = HeartRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESET ALL PROGRESS", color = HeartRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bunny Adventure: Kids Run • v1.0\nMade with ❤️ for young adventurers",
                fontSize = 12.sp,
                color = DeepPurple.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Reset Confirmation Dialog
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = {
                    Text(
                        text = "Reset Progress?",
                        fontWeight = FontWeight.Black,
                        color = DeepOrange
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to reset all scores, coins, and unlocked characters? This cannot be undone.",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetProgress()
                            showResetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeartRed)
                    ) {
                        Text("Yes, Reset", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GrassGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
