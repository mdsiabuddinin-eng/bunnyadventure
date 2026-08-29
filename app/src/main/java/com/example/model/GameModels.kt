package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Characters available in the game with their coin costs and unique perks.
 */
enum class CharacterType(
    val id: String,
    val displayName: String,
    val cost: Int,
    val perkName: String,
    val perkDescription: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val extraLives: Int = 0,
    val coinMagnetRadius: Float = 0f,
    val starScoreMultiplier: Int = 1
) {
    BUNNY(
        id = "BUNNY",
        displayName = "Bunny",
        cost = 0, // FREE
        perkName = "Nimble Hopper",
        perkDescription = "Quick recovery & super cute ear hops!",
        primaryColor = Color(0xFFFFFFFF),
        secondaryColor = Color(0xFFFF80AB),
        accentColor = Color(0xFFFF4081)
    ),
    PUPPY(
        id = "PUPPY",
        displayName = "Puppy",
        cost = 500,
        perkName = "Coin Magnet",
        perkDescription = "Pulls nearby shiny coins automatically!",
        primaryColor = Color(0xFFFFB74D),
        secondaryColor = Color(0xFF8D6E63),
        accentColor = Color(0xFFFF7043),
        coinMagnetRadius = 140f
    ),
    KITTEN(
        id = "KITTEN",
        displayName = "Kitten",
        cost = 1000,
        perkName = "Star Sparkle",
        perkDescription = "Stars give DOUBLE score (2x)!",
        primaryColor = Color(0xFFFFCC80),
        secondaryColor = Color(0xFFFFAB91),
        accentColor = Color(0xFFFF4081),
        starScoreMultiplier = 2
    ),
    PANDA(
        id = "PANDA",
        displayName = "Panda",
        cost = 1500,
        perkName = "Bamboo Shield",
        perkDescription = "Starts every run with 4 hearts (+1 Life)!",
        primaryColor = Color(0xFFFFFFFF),
        secondaryColor = Color(0xFF212121),
        accentColor = Color(0xFF4CAF50),
        extraLives = 1
    );

    companion object {
        fun fromId(id: String): CharacterType =
            entries.firstOrNull { it.id == id } ?: BUNNY
    }
}

/**
 * 5 Unique Worlds with distinct visual themes and obstacles.
 */
enum class WorldType(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val unlockScore: Int,
    val unlockCoins: Int,
    val skyColorTop: Color,
    val skyColorBottom: Color,
    val groundColor: Color,
    val subGroundColor: Color,
    val accentColor: Color,
    val ambientDescription: String
) {
    GREEN_FOREST(
        id = "GREEN_FOREST",
        displayName = "Green Forest",
        subtitle = "Sunny trees, flowers & breeze",
        unlockScore = 0,
        unlockCoins = 0,
        skyColorTop = Color(0xFF4FC3F7),
        skyColorBottom = Color(0xFFE1F5FE),
        groundColor = Color(0xFF66BB6A),
        subGroundColor = Color(0xFF388E3C),
        accentColor = Color(0xFFFFD54F),
        ambientDescription = "Lush green grass, flowers, floating clouds, and warm sunlight"
    ),
    CANDY_LAND(
        id = "CANDY_LAND",
        displayName = "Candy Land",
        subtitle = "Lollipops, sweet paths & gumdrops",
        unlockScore = 500,
        unlockCoins = 150,
        skyColorTop = Color(0xFFF48FB1),
        skyColorBottom = Color(0xFFFCE4EC),
        groundColor = Color(0xFFFF80AB),
        subGroundColor = Color(0xFFEC407A),
        accentColor = Color(0xFF80D8FF),
        ambientDescription = "Swirly lollipop trees, candy cane arches, and cotton candy clouds"
    ),
    SNOW_LAND(
        id = "SNOW_LAND",
        displayName = "Snow Land",
        subtitle = "Winter wonderland & crystal ice",
        unlockScore = 1200,
        unlockCoins = 350,
        skyColorTop = Color(0xFF80DEEA),
        skyColorBottom = Color(0xFFE0F7FA),
        groundColor = Color(0xFFECEFF1),
        subGroundColor = Color(0xFFB0BEC5),
        accentColor = Color(0xFF00E5FF),
        ambientDescription = "Crisp white snow, snowy pines, shimmering icicles and snowflakes"
    ),
    SUNNY_BEACH(
        id = "SUNNY_BEACH",
        displayName = "Sunny Beach",
        subtitle = "Golden sand, palm trees & waves",
        unlockScore = 2500,
        unlockCoins = 600,
        skyColorTop = Color(0xFF29B6F6),
        skyColorBottom = Color(0xFFFFF9C4),
        groundColor = Color(0xFFFFE082),
        subGroundColor = Color(0xFFFFB74D),
        accentColor = Color(0xFFFF5722),
        ambientDescription = "Tropical sands, swaying palms, starfish, and cheerful ocean ripples"
    ),
    SPACE_WORLD(
        id = "SPACE_WORLD",
        displayName = "Space World",
        subtitle = "Cosmic nebula, stars & planets",
        unlockScore = 5000,
        unlockCoins = 1000,
        skyColorTop = Color(0xFF1A237E),
        skyColorBottom = Color(0xFF4A148C),
        groundColor = Color(0xFF7E57C2),
        subGroundColor = Color(0xFF4527A0),
        accentColor = Color(0xFF00E676),
        ambientDescription = "Cosmic nebula, glowing star platforms, spinning planets and meteors"
    );

    companion object {
        fun fromId(id: String): WorldType =
            entries.firstOrNull { it.id == id } ?: GREEN_FOREST
    }
}

/**
 * Child-friendly Obstacles.
 * Overhead obstacles require sliding under (Swipe down / Slide button).
 * Ground obstacles require jumping over (Tap screen / Jump button).
 */
enum class ObstacleKind(
    val displayName: String,
    val isOverhead: Boolean,
    val width: Float,
    val height: Float,
    val color: Color
) {
    // Green Forest
    ROCK("Rock", false, 48f, 40f, Color(0xFF78909C)),
    WOODEN_BOX("Wooden Box", false, 46f, 46f, Color(0xFF8D6E63)),
    PUDDLE("Mud Puddle", false, 60f, 18f, Color(0xFF5D4037)),
    LOW_BRANCH("Tree Branch", true, 64f, 52f, Color(0xFF388E3C)),

    // Candy Land
    CANDY_ROCK("Candy Rock", false, 48f, 42f, Color(0xFFFF4081)),
    GUMMY_BLOCK("Gummy Block", false, 46f, 46f, Color(0xFF00E676)),
    LOLLIPOP_GATE("Lollipop Gate", true, 64f, 54f, Color(0xFFFF1744)),
    CHOCOLATE_PUDDLE("Choco Puddle", false, 58f, 18f, Color(0xFF4E342E)),

    // Snow Land
    SNOW_BLOCK("Snow Block", false, 46f, 44f, Color(0xFFCFD8DC)),
    ICE_MOUND("Ice Mound", false, 52f, 38f, Color(0xFF80DEEA)),
    ICICLE_GATE("Icicle Arch", true, 64f, 54f, Color(0xFF00BCD4)),
    ICE_PUDDLE("Slick Ice", false, 60f, 16f, Color(0xFFB2EBF2)),

    // Sunny Beach
    SAND_CASTLE("Sand Castle", false, 50f, 44f, Color(0xFFFFCA28)),
    BEACH_BALL("Beach Ball", false, 42f, 42f, Color(0xFFFF5252)),
    PALM_BRANCH("Palm Frond", true, 66f, 52f, Color(0xFF2E7D32)),
    TIDE_PUDDLE("Tide Pool", false, 58f, 18f, Color(0xFF0288D1)),

    // Space World
    METEOR("Meteor Rock", false, 48f, 44f, Color(0xFF7E57C2)),
    SPACE_CRATE("Space Crate", false, 46f, 46f, Color(0xFF26A69A)),
    LASER_GATE("Laser Beam", true, 64f, 50f, Color(0xFFFF1744)),
    MOON_CRATER("Moon Crater", false, 60f, 18f, Color(0xFF37474F))
}

data class Obstacle(
    val id: Long,
    val kind: ObstacleKind,
    var x: Float,
    val y: Float, // Calculated based on ground level or overhead height
    val width: Float,
    val height: Float,
    var isPassed: Boolean = false,
    var isHit: Boolean = false
)

/**
 * Collectible items with distinct rewards and behaviors.
 */
enum class CollectibleKind(
    val displayName: String,
    val pointValue: Int,
    val coinValue: Int,
    val size: Float
) {
    COIN("Coin", 10, 1, 30f),
    STAR("Star", 50, 0, 36f),
    CARROT("Carrot", 100, 2, 38f)
}

data class Collectible(
    val id: Long,
    val kind: CollectibleKind,
    var x: Float,
    var y: Float,
    val baseY: Float,
    var animPhase: Float = 0f,
    var isCollected: Boolean = false
)

enum class ParticleShape {
    CIRCLE, STAR, SPARKLE, DUST
}

data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var alpha: Float = 1f,
    var life: Float = 1f, // 1.0 down to 0.0
    val decayRate: Float = 0.03f,
    val shape: ParticleShape = ParticleShape.CIRCLE
)

data class FloatingText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var alpha: Float = 1f,
    var life: Float = 1f
)

/**
 * Navigation screen routes.
 */
enum class AppScreen {
    SPLASH,
    HOME,
    GAME,
    CHARACTERS,
    WORLDS,
    SETTINGS
}

/**
 * Reward Milestones that children can unlock and celebrate.
 */
data class RewardMilestone(
    val id: String,
    val title: String,
    val description: String,
    val requiredCoins: Int,
    val rewardIcon: String,
    val isCharacterUnlock: Boolean = false,
    val characterId: String? = null
)
