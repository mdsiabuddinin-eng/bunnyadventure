package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ads.AdMobBanner
import com.example.ads.AdMobManager
import com.example.model.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.screens.CharacterSelectScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RewardMilestoneDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WorldSelectScreen
import com.example.ui.theme.BunnyAdventureTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK and preload Interstitial / Rewarded ads
        AdMobManager.initialize(this)

        setContent {
            BunnyAdventureTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BunnyAdventureApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.soundManager.stopMusic()
        if (viewModel.currentScreen.value == AppScreen.GAME) {
            viewModel.pauseGame()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.userData.value.musicEnabled && viewModel.currentScreen.value != AppScreen.SPLASH) {
            viewModel.soundManager.startMusic()
        }
    }
}

@Composable
fun BunnyAdventureApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeReward by viewModel.activeMilestoneReward.collectAsState()

    // Handle Hardware Back Press
    BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.SPLASH) {
        when (currentScreen) {
            AppScreen.GAME -> viewModel.pauseGame()
            AppScreen.CHARACTERS, AppScreen.WORLDS, AppScreen.SETTINGS -> viewModel.navigateTo(AppScreen.HOME)
            else -> {}
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(300),
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.SPLASH -> SplashScreen()
                    AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                    AppScreen.GAME -> GamePlayScreen(viewModel = viewModel)
                    AppScreen.CHARACTERS -> CharacterSelectScreen(viewModel = viewModel)
                    AppScreen.WORLDS -> WorldSelectScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }

            // Global Milestone Celebration Popup
            activeReward?.let { milestone ->
                RewardMilestoneDialog(
                    milestone = milestone,
                    onDismiss = { viewModel.dismissMilestonePopup() }
                )
            }
        }

        // Sticky Banner Ad at the bottom of the screen (visible on main menu & game screens)
        if (currentScreen != AppScreen.SPLASH) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                AdMobBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
