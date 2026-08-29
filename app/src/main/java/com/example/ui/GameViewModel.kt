package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameSoundManager
import com.example.data.GamePreferences
import com.example.data.GameUserData
import com.example.game.GameEngine
import com.example.model.AppScreen
import com.example.model.CharacterType
import com.example.model.RewardMilestone
import com.example.model.WorldType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameOverData(
    val score: Int,
    val bestScore: Int,
    val isNewBestScore: Boolean,
    val coinsEarned: Int,
    val totalCoins: Int,
    val carrotsEarned: Int,
    val newlyUnlockedMilestones: List<RewardMilestone>
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = GamePreferences(application)
    val soundManager = GameSoundManager(application)

    val userData: StateFlow<GameUserData> = preferences.userData

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _gameOverData = MutableStateFlow<GameOverData?>(null)
    val gameOverData: StateFlow<GameOverData?> = _gameOverData.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _activeMilestoneReward = MutableStateFlow<RewardMilestone?>(null)
    val activeMilestoneReward: StateFlow<RewardMilestone?> = _activeMilestoneReward.asStateFlow()

    private val _isReviveAvailable = MutableStateFlow(true)
    val isReviveAvailable: StateFlow<Boolean> = _isReviveAvailable.asStateFlow()

    private val _hasDoubledCoins = MutableStateFlow(false)
    val hasDoubledCoins: StateFlow<Boolean> = _hasDoubledCoins.asStateFlow()

    // Real-time HUD Observable States
    val liveScore = MutableStateFlow(0)
    val liveCoins = MutableStateFlow(0)
    val liveLives = MutableStateFlow(3)

    val gameEngine: GameEngine = GameEngine(
        onScoreChanged = { newScore -> liveScore.value = newScore },
        onCoinsChanged = { newCoins -> liveCoins.value = newCoins },
        onLivesChanged = { newLives -> liveLives.value = newLives },
        onGameOver = { finalScore, coinsEarned, carrotsEarned ->
            viewModelScope.launch {
                val (isNewBest, newMilestones) = preferences.recordRunResults(finalScore, coinsEarned, carrotsEarned)
                _gameOverData.value = GameOverData(
                    score = finalScore,
                    bestScore = userData.value.bestScore,
                    isNewBestScore = isNewBest,
                    coinsEarned = coinsEarned,
                    totalCoins = userData.value.totalCoins,
                    carrotsEarned = carrotsEarned,
                    newlyUnlockedMilestones = newMilestones
                )
                if (newMilestones.isNotEmpty()) {
                    _activeMilestoneReward.value = newMilestones.first()
                }
            }
        },
        onSoundEvent = { event ->
            when (event) {
                GameEngine.SoundEvent.JUMP -> soundManager.playJump()
                GameEngine.SoundEvent.SLIDE -> soundManager.playSlide()
                GameEngine.SoundEvent.COIN -> soundManager.playCoin()
                GameEngine.SoundEvent.STAR -> soundManager.playStar()
                GameEngine.SoundEvent.CARROT -> soundManager.playCarrot()
                GameEngine.SoundEvent.BUMP -> soundManager.playBump()
                GameEngine.SoundEvent.GAME_OVER -> soundManager.playGameOver()
            }
        }
    )

    init {
        // Sync audio manager settings with saved preferences
        val user = userData.value
        soundManager.isMusicEnabled = user.musicEnabled
        soundManager.isSfxEnabled = user.sfxEnabled
        soundManager.isHapticsEnabled = user.hapticsEnabled

        // Splash screen auto transition
        viewModelScope.launch {
            delay(1400)
            if (_currentScreen.value == AppScreen.SPLASH) {
                _currentScreen.value = AppScreen.HOME
                soundManager.startMusic()
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundManager.playClick()
        if (screen == AppScreen.GAME) {
            startGame()
        } else {
            _currentScreen.value = screen
        }
    }

    fun startGame() {
        val user = userData.value
        val character = CharacterType.fromId(user.selectedCharacterId)
        val world = WorldType.fromId(user.selectedWorldId)

        _gameOverData.value = null
        _isPaused.value = false
        _isReviveAvailable.value = true
        _hasDoubledCoins.value = false
        liveScore.value = 0
        liveCoins.value = 0
        liveLives.value = 3 + character.extraLives

        gameEngine.startNewGame(character, world, user.childModeEnabled)
        _currentScreen.value = AppScreen.GAME
        soundManager.startMusic()
    }

    fun reviveRun() {
        _isReviveAvailable.value = false
        _gameOverData.value = null
        _isPaused.value = false
        gameEngine.revive(extraLives = 1)
        soundManager.playStar()
        if (userData.value.musicEnabled) {
            soundManager.startMusic()
        }
    }

    fun doubleGameOverCoins() {
        val currentData = _gameOverData.value ?: return
        if (_hasDoubledCoins.value) return
        _hasDoubledCoins.value = true
        val bonus = currentData.coinsEarned
        if (bonus > 0) {
            preferences.addDebugCoins(bonus)
            _gameOverData.value = currentData.copy(
                coinsEarned = currentData.coinsEarned * 2,
                totalCoins = userData.value.totalCoins
            )
            soundManager.playCoin()
            soundManager.playUnlock()
        }
    }

    fun claimAdBonusCoins(amount: Int = 50) {
        preferences.addDebugCoins(amount)
        soundManager.playCoin()
        soundManager.playUnlock()
    }

    fun jump() {
        gameEngine.jump()
    }

    fun slide() {
        gameEngine.slide()
    }

    fun pauseGame() {
        soundManager.playClick()
        _isPaused.value = true
        gameEngine.isPaused = true
    }

    fun resumeGame() {
        soundManager.playClick()
        _isPaused.value = false
        gameEngine.isPaused = false
    }

    fun restartGame() {
        soundManager.playClick()
        startGame()
    }

    fun returnToHome() {
        soundManager.playClick()
        _gameOverData.value = null
        _isPaused.value = false
        gameEngine.isGameOver = true
        _currentScreen.value = AppScreen.HOME
    }

    fun unlockCharacter(character: CharacterType) {
        val success = preferences.unlockCharacter(character)
        if (success) {
            soundManager.playUnlock()
        } else {
            soundManager.playBump()
        }
    }

    fun selectCharacter(characterId: String) {
        soundManager.playClick()
        preferences.selectCharacter(characterId)
    }

    fun unlockWorld(world: WorldType) {
        val success = preferences.unlockWorld(world)
        if (success) {
            soundManager.playUnlock()
        } else {
            soundManager.playBump()
        }
    }

    fun selectWorld(worldId: String) {
        soundManager.playClick()
        preferences.selectWorld(worldId)
    }

    fun toggleMusic() {
        val next = !userData.value.musicEnabled
        preferences.setMusicEnabled(next)
        soundManager.isMusicEnabled = next
        soundManager.playClick()
    }

    fun toggleSfx() {
        val next = !userData.value.sfxEnabled
        preferences.setSfxEnabled(next)
        soundManager.isSfxEnabled = next
        soundManager.playClick()
    }

    fun toggleHaptics() {
        val next = !userData.value.hapticsEnabled
        preferences.setHapticsEnabled(next)
        soundManager.isHapticsEnabled = next
        soundManager.playClick()
    }

    fun toggleChildMode() {
        val next = !userData.value.childModeEnabled
        preferences.setChildModeEnabled(next)
        soundManager.playClick()
    }

    fun resetProgress() {
        preferences.resetAllProgress()
        soundManager.playBump()
    }

    fun addTestCoins(amount: Int) {
        preferences.addDebugCoins(amount)
        soundManager.playCoin()
    }

    fun dismissMilestonePopup() {
        soundManager.playClick()
        _activeMilestoneReward.value = null
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
