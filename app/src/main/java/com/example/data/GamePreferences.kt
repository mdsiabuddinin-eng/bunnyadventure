package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CharacterType
import com.example.model.RewardMilestone
import com.example.model.WorldType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameUserData(
    val bestScore: Int = 0,
    val totalCoins: Int = 0,
    val unlockedCharacters: Set<String> = setOf(CharacterType.BUNNY.id),
    val selectedCharacterId: String = CharacterType.BUNNY.id,
    val unlockedWorlds: Set<String> = setOf(WorldType.GREEN_FOREST.id),
    val selectedWorldId: String = WorldType.GREEN_FOREST.id,
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val childModeEnabled: Boolean = false,
    val claimedMilestones: Set<String> = emptySet(),
    val totalRuns: Int = 0,
    val totalCarrotsCollected: Int = 0
)

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bunny_adventure_prefs", Context.MODE_PRIVATE)

    private val _userData = MutableStateFlow(loadData())
    val userData: StateFlow<GameUserData> = _userData.asStateFlow()

    val allMilestones = listOf(
        RewardMilestone("milestone_100_coins", "100 Coins Master", "Collected 100 shiny gold coins! Earned Golden Ribbon badge!", 100, "🪙"),
        RewardMilestone("milestone_puppy", "Puppy Unlocked", "Earned 500 coins to adopt the playful Puppy!", 500, "🐶", true, CharacterType.PUPPY.id),
        RewardMilestone("milestone_kitten", "Kitten Unlocked", "Earned 1000 coins to adopt the adorable Kitten!", 1000, "🐱", true, CharacterType.KITTEN.id),
        RewardMilestone("milestone_panda", "Panda Champion", "Earned 1500 coins to adopt the mighty Panda!", 1500, "🐼", true, CharacterType.PANDA.id),
        RewardMilestone("milestone_5000_score", "Super Runner", "Scored over 5000 points in a single run!", 0, "🏆")
    )

    private fun loadData(): GameUserData {
        val bestScore = prefs.getInt("best_score", 0)
        val totalCoins = prefs.getInt("total_coins", 0)
        val unlockedChars = prefs.getStringSet("unlocked_chars", setOf(CharacterType.BUNNY.id))
            ?: setOf(CharacterType.BUNNY.id)
        val selectedChar = prefs.getString("selected_char", CharacterType.BUNNY.id)
            ?: CharacterType.BUNNY.id
        val unlockedWorlds = prefs.getStringSet("unlocked_worlds", setOf(WorldType.GREEN_FOREST.id))
            ?: setOf(WorldType.GREEN_FOREST.id)
        val selectedWorld = prefs.getString("selected_world", WorldType.GREEN_FOREST.id)
            ?: WorldType.GREEN_FOREST.id
        val music = prefs.getBoolean("music_enabled", true)
        val sfx = prefs.getBoolean("sfx_enabled", true)
        val haptics = prefs.getBoolean("haptics_enabled", true)
        val childMode = prefs.getBoolean("child_mode_enabled", false)
        val milestones = prefs.getStringSet("claimed_milestones", emptySet()) ?: emptySet()
        val totalRuns = prefs.getInt("total_runs", 0)
        val totalCarrots = prefs.getInt("total_carrots", 0)

        return GameUserData(
            bestScore = bestScore,
            totalCoins = totalCoins,
            unlockedCharacters = unlockedChars,
            selectedCharacterId = selectedChar,
            unlockedWorlds = unlockedWorlds,
            selectedWorldId = selectedWorld,
            musicEnabled = music,
            sfxEnabled = sfx,
            hapticsEnabled = haptics,
            childModeEnabled = childMode,
            claimedMilestones = milestones,
            totalRuns = totalRuns,
            totalCarrotsCollected = totalCarrots
        )
    }

    fun recordRunResults(score: Int, coinsEarned: Int, carrotsEarned: Int): Pair<Boolean, List<RewardMilestone>> {
        val current = _userData.value
        val newBest = maxOf(current.bestScore, score)
        val isNewBest = score > current.bestScore
        val newTotalCoins = current.totalCoins + coinsEarned
        val newTotalCarrots = current.totalCarrotsCollected + carrotsEarned
        val newTotalRuns = current.totalRuns + 1

        // Check for new milestone unlocks
        val newlyClaimed = current.claimedMilestones.toMutableSet()
        val unlockedCharacters = current.unlockedCharacters.toMutableSet()
        val unlockedWorlds = current.unlockedWorlds.toMutableSet()
        val newlyUnlockedMilestones = mutableListOf<RewardMilestone>()

        // Check world unlocks based on cumulative coins or high score
        WorldType.entries.forEach { world ->
            if (!unlockedWorlds.contains(world.id)) {
                if (newTotalCoins >= world.unlockCoins && newBest >= world.unlockScore) {
                    unlockedWorlds.add(world.id)
                }
            }
        }

        allMilestones.forEach { milestone ->
            if (!newlyClaimed.contains(milestone.id)) {
                val shouldUnlock = when (milestone.id) {
                    "milestone_100_coins" -> newTotalCoins >= 100
                    "milestone_puppy" -> newTotalCoins >= 500
                    "milestone_kitten" -> newTotalCoins >= 1000
                    "milestone_panda" -> newTotalCoins >= 1500
                    "milestone_5000_score" -> newBest >= 5000
                    else -> false
                }
                if (shouldUnlock) {
                    newlyClaimed.add(milestone.id)
                    newlyUnlockedMilestones.add(milestone)
                    milestone.characterId?.let { charId ->
                        unlockedCharacters.add(charId)
                    }
                }
            }
        }

        prefs.edit()
            .putInt("best_score", newBest)
            .putInt("total_coins", newTotalCoins)
            .putInt("total_carrots", newTotalCarrots)
            .putInt("total_runs", newTotalRuns)
            .putStringSet("claimed_milestones", newlyClaimed)
            .putStringSet("unlocked_chars", unlockedCharacters)
            .putStringSet("unlocked_worlds", unlockedWorlds)
            .apply()

        _userData.value = current.copy(
            bestScore = newBest,
            totalCoins = newTotalCoins,
            totalCarrotsCollected = newTotalCarrots,
            totalRuns = newTotalRuns,
            claimedMilestones = newlyClaimed,
            unlockedCharacters = unlockedCharacters,
            unlockedWorlds = unlockedWorlds
        )

        return Pair(isNewBest, newlyUnlockedMilestones)
    }

    fun unlockCharacter(character: CharacterType): Boolean {
        val current = _userData.value
        if (current.unlockedCharacters.contains(character.id)) return true
        if (current.totalCoins < character.cost) return false

        val newTotalCoins = current.totalCoins - character.cost
        val newUnlocked = current.unlockedCharacters + character.id

        prefs.edit()
            .putInt("total_coins", newTotalCoins)
            .putStringSet("unlocked_chars", newUnlocked)
            .putString("selected_char", character.id)
            .apply()

        _userData.value = current.copy(
            totalCoins = newTotalCoins,
            unlockedCharacters = newUnlocked,
            selectedCharacterId = character.id
        )
        return true
    }

    fun selectCharacter(characterId: String) {
        val current = _userData.value
        if (!current.unlockedCharacters.contains(characterId)) return
        prefs.edit().putString("selected_char", characterId).apply()
        _userData.value = current.copy(selectedCharacterId = characterId)
    }

    fun unlockWorld(world: WorldType): Boolean {
        val current = _userData.value
        if (current.unlockedWorlds.contains(world.id)) return true
        if (current.totalCoins < world.unlockCoins || current.bestScore < world.unlockScore) return false

        val newUnlocked = current.unlockedWorlds + world.id
        prefs.edit()
            .putStringSet("unlocked_worlds", newUnlocked)
            .putString("selected_world", world.id)
            .apply()

        _userData.value = current.copy(
            unlockedWorlds = newUnlocked,
            selectedWorldId = world.id
        )
        return true
    }

    fun selectWorld(worldId: String) {
        val current = _userData.value
        if (!current.unlockedWorlds.contains(worldId)) return
        prefs.edit().putString("selected_world", worldId).apply()
        _userData.value = current.copy(selectedWorldId = worldId)
    }

    fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("music_enabled", enabled).apply()
        _userData.value = _userData.value.copy(musicEnabled = enabled)
    }

    fun setSfxEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sfx_enabled", enabled).apply()
        _userData.value = _userData.value.copy(sfxEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
        _userData.value = _userData.value.copy(hapticsEnabled = enabled)
    }

    fun setChildModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("child_mode_enabled", enabled).apply()
        _userData.value = _userData.value.copy(childModeEnabled = enabled)
    }

    fun addDebugCoins(amount: Int) {
        val current = _userData.value
        val newCoins = current.totalCoins + amount
        prefs.edit().putInt("total_coins", newCoins).apply()
        _userData.value = current.copy(totalCoins = newCoins)
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
        _userData.value = GameUserData()
    }
}
