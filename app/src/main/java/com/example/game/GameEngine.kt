package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.model.CharacterType
import com.example.model.Collectible
import com.example.model.CollectibleKind
import com.example.model.FloatingText
import com.example.model.GameParticle
import com.example.model.Obstacle
import com.example.model.ObstacleKind
import com.example.model.ParticleShape
import com.example.model.WorldType
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    var screenWidth: Float = 1080f,
    var screenHeight: Float = 1920f,
    var onScoreChanged: (Int) -> Unit = {},
    var onCoinsChanged: (Int) -> Unit = {},
    var onLivesChanged: (Int) -> Unit = {},
    var onGameOver: (score: Int, coins: Int, carrots: Int) -> Unit = { _, _, _ -> },
    var onSoundEvent: (SoundEvent) -> Unit = {}
) {
    enum class SoundEvent {
        JUMP, SLIDE, COIN, STAR, CARROT, BUMP, GAME_OVER
    }

    // World & Character settings
    var currentCharacter: CharacterType = CharacterType.BUNNY
    var currentWorld: WorldType = WorldType.GREEN_FOREST
    var isChildMode: Boolean = false

    // Player State
    var playerX: Float = 220f
    var playerY: Float = 0f
    var playerVy: Float = 0f
    val groundY: Float get() = screenHeight * 0.76f

    var isGrounded: Boolean = true
    var isSliding: Boolean = false
    var slideTimer: Float = 0f
    val slideDuration: Float = 0.65f

    var isInvulnerable: Boolean = false
    var invulnerableTimer: Float = 0f
    val invulnerableDuration: Float = 1.8f

    var hasCarrotBoost: Boolean = false
    var carrotBoostTimeRemaining: Float = 0f
    val carrotBoostMaxDuration: Float = 6.0f

    // Physics constants
    private val gravity: Float = 2100f
    private val jumpVelocity: Float = -850f

    // Gameplay progression
    var score: Int = 0
    var coinsCollectedThisRun: Int = 0
    var carrotsCollectedThisRun: Int = 0
    var lives: Int = 3
    var maxLives: Int = 3
    var isGameOver: Boolean = false
    var isPaused: Boolean = false

    var currentSpeed: Float = 340f
    var distanceTraveled: Float = 0f
    var runAnimFrame: Float = 0f

    // Entities
    val obstacles = mutableListOf<Obstacle>()
    val collectibles = mutableListOf<Collectible>()
    val particles = mutableListOf<GameParticle>()
    val floatingTexts = mutableListOf<FloatingText>()

    // Spawning timers & trackers
    private var nextSpawnDistance: Float = 400f
    private var entityIdCounter: Long = 1L
    private var dustParticleTimer: Float = 0f

    fun startNewGame(
        character: CharacterType,
        world: WorldType,
        childMode: Boolean
    ) {
        currentCharacter = character
        currentWorld = world
        isChildMode = childMode

        score = 0
        coinsCollectedThisRun = 0
        carrotsCollectedThisRun = 0
        maxLives = 3 + character.extraLives
        lives = maxLives
        isGameOver = false
        isPaused = false

        playerX = screenWidth * 0.22f
        playerY = groundY
        playerVy = 0f
        isGrounded = true
        isSliding = false
        slideTimer = 0f
        isInvulnerable = false
        invulnerableTimer = 0f
        hasCarrotBoost = false
        carrotBoostTimeRemaining = 0f

        currentSpeed = if (childMode) 300f else 350f
        distanceTraveled = 0f
        runAnimFrame = 0f
        nextSpawnDistance = 350f

        obstacles.clear()
        collectibles.clear()
        particles.clear()
        floatingTexts.clear()

        onScoreChanged(score)
        onCoinsChanged(coinsCollectedThisRun)
        onLivesChanged(lives)
    }

    fun revive(extraLives: Int = 1) {
        lives = extraLives
        isGameOver = false
        isPaused = false
        isInvulnerable = true
        invulnerableTimer = 2.5f
        // Clear all obstacles within 400px ahead to give player a safe landing
        obstacles.removeAll { it.x < playerX + 450f }
        onLivesChanged(lives)
        onSoundEvent(SoundEvent.STAR)
        spawnSparkleParticles(playerX, playerY - 30f, Color(0xFFFFD600))
        addFloatingText("✨ REVIVED! ✨", playerX, playerY - 60f, Color(0xFFFFD600))
    }

    fun jump() {
        if (isGameOver || isPaused) return
        if (isGrounded || isSliding) {
            // Cancel slide if jumping
            isSliding = false
            slideTimer = 0f
            playerVy = jumpVelocity
            isGrounded = false
            onSoundEvent(SoundEvent.JUMP)
            spawnJumpParticles()
        }
    }

    fun slide() {
        if (isGameOver || isPaused) return
        if (isGrounded && !isSliding) {
            isSliding = true
            slideTimer = slideDuration
            onSoundEvent(SoundEvent.SLIDE)
            spawnSlideParticles()
        } else if (!isGrounded) {
            // Fast dive down to ground
            playerVy = 900f
        }
    }

    fun update(dt: Float) {
        if (isGameOver || isPaused) return

        val safeDt = dt.coerceIn(0.001f, 0.05f)

        // 1. Difficulty Speed Ramp
        val speedIncrement = if (isChildMode) 3f else 6f
        val maxSpeed = if (isChildMode) 520f else 680f
        val effectiveSpeed = if (hasCarrotBoost) (currentSpeed * 1.35f) else currentSpeed

        currentSpeed = (currentSpeed + safeDt * speedIncrement).coerceAtMost(maxSpeed)
        val frameDistance = effectiveSpeed * safeDt
        distanceTraveled += frameDistance

        // Continuous distance score
        val oldScore = score
        score += (frameDistance * 0.08f).toInt()
        if (score != oldScore) {
            onScoreChanged(score)
        }

        // Run animation frame
        runAnimFrame = (runAnimFrame + safeDt * (effectiveSpeed / 45f))

        // 2. Player Physics
        if (!isGrounded) {
            playerVy += gravity * safeDt
            playerY += playerVy * safeDt

            if (playerY >= groundY) {
                playerY = groundY
                playerVy = 0f
                isGrounded = true
                spawnLandingDust()
            }
        }

        // Slide timer
        if (isSliding) {
            slideTimer -= safeDt
            if (slideTimer <= 0f) {
                isSliding = false
                slideTimer = 0f
            }
        }

        // Invulnerability iframe
        if (isInvulnerable) {
            invulnerableTimer -= safeDt
            if (invulnerableTimer <= 0f) {
                isInvulnerable = false
                invulnerableTimer = 0f
            }
        }

        // Carrot Boost Powerup timer
        if (hasCarrotBoost) {
            carrotBoostTimeRemaining -= safeDt
            // Spawn continuous rainbow sparkle trail
            if (Random.nextFloat() < 0.4f) {
                spawnRainbowSparkle()
            }
            if (carrotBoostTimeRemaining <= 0f) {
                hasCarrotBoost = false
                carrotBoostTimeRemaining = 0f
            }
        }

        // Ground running dust trail
        if (isGrounded && !isSliding) {
            dustParticleTimer += safeDt
            if (dustParticleTimer > 0.12f) {
                dustParticleTimer = 0f
                spawnRunDust()
            }
        }

        // 3. Update & Move Obstacles
        val obstacleIter = obstacles.iterator()
        while (obstacleIter.hasNext()) {
            val obs = obstacleIter.next()
            obs.x -= frameDistance

            // Collision check
            if (!obs.isHit && !obs.isPassed && !isInvulnerable) {
                if (checkObstacleCollision(obs)) {
                    if (hasCarrotBoost) {
                        // Smash obstacle!
                        obs.isHit = true
                        spawnSmashParticles(obs.x + obs.width / 2f, obs.y + obs.height / 2f)
                        addFloatingText("+25 SMASH!", obs.x, obs.y, Color(0xFFFFD600))
                        score += 25
                        onScoreChanged(score)
                        onSoundEvent(SoundEvent.STAR)
                    } else {
                        // Player hit
                        obs.isHit = true
                        handleObstacleHit(obs)
                    }
                }
            }

            if (obs.x + obs.width < -100f) {
                obstacleIter.remove()
            }
        }

        // 4. Update & Move Collectibles
        val magnetRadius = if (hasCarrotBoost) 280f else currentCharacter.coinMagnetRadius
        val collectIter = collectibles.iterator()
        while (collectIter.hasNext()) {
            val item = collectIter.next()
            item.x -= frameDistance
            item.animPhase = (item.animPhase + safeDt * 3f) % 1f

            // Coin Magnet Perk / Powerup pull
            if (magnetRadius > 0f && (item.kind == CollectibleKind.COIN || hasCarrotBoost)) {
                val dx = playerX - item.x
                val dy = (playerY - 20f) - item.y
                val dist = hypot(dx, dy)
                if (dist < magnetRadius && dist > 10f) {
                    val pullSpeed = 650f * safeDt
                    item.x += (dx / dist) * pullSpeed
                    item.y += (dy / dist) * pullSpeed
                }
            }

            // Collection Check
            val pHeadY = if (isSliding) playerY - 10f else playerY - 30f
            val itemDist = hypot(playerX - item.x, pHeadY - item.y)
            val collectThreshold = if (isSliding) 48f else 54f

            if (itemDist < collectThreshold && !item.isCollected) {
                item.isCollected = true
                handleCollectItem(item)
                collectIter.remove()
            } else if (item.x < -60f) {
                collectIter.remove()
            }
        }

        // 5. Update Particles & Text
        updateParticlesAndText(safeDt)

        // 6. Spawn Next Obstacles & Collectibles
        if (distanceTraveled >= nextSpawnDistance) {
            spawnNextPattern()
            val minGap = if (isChildMode) 380f else 320f
            val randomGap = Random.nextFloat() * 220f
            nextSpawnDistance = distanceTraveled + minGap + randomGap
        }
    }

    private fun checkObstacleCollision(obs: Obstacle): Boolean {
        // Player Bounding Box
        val playerLeft = playerX - 22f
        val playerRight = playerX + 22f
        val playerTop = if (isSliding) (playerY - 24f) else (playerY - 60f)
        val playerBottom = playerY + 4f

        val obsLeft = obs.x + 8f
        val obsRight = obs.x + obs.width - 8f
        val obsTop = obs.y + 6f
        val obsBottom = obs.y + obs.height - 4f

        // Bounding Box Overlap
        val xOverlap = playerRight > obsLeft && playerLeft < obsRight
        val yOverlap = playerBottom > obsTop && playerTop < obsBottom

        return xOverlap && yOverlap
    }

    private fun handleObstacleHit(obs: Obstacle) {
        lives--
        onLivesChanged(lives)
        onSoundEvent(SoundEvent.BUMP)
        spawnHitParticles(playerX, playerY - 20f)
        addFloatingText("OUCH!", playerX, playerY - 50f, Color(0xFFFF5252))

        if (lives <= 0) {
            isGameOver = true
            onSoundEvent(SoundEvent.GAME_OVER)
            onGameOver(score, coinsCollectedThisRun, carrotsCollectedThisRun)
        } else {
            isInvulnerable = true
            invulnerableTimer = invulnerableDuration
        }
    }

    private fun handleCollectItem(item: Collectible) {
        when (item.kind) {
            CollectibleKind.COIN -> {
                coinsCollectedThisRun += item.kind.coinValue
                val points = item.kind.pointValue
                score += points
                onCoinsChanged(coinsCollectedThisRun)
                onScoreChanged(score)
                onSoundEvent(SoundEvent.COIN)
                spawnSparkleParticles(item.x, item.y, Color(0xFFFFD600))
                addFloatingText("+1 🪙", item.x, item.y - 15f, Color(0xFFFFD600))
            }
            CollectibleKind.STAR -> {
                val multiplier = currentCharacter.starScoreMultiplier
                val points = item.kind.pointValue * multiplier
                score += points
                onScoreChanged(score)
                onSoundEvent(SoundEvent.STAR)
                spawnStarBurstParticles(item.x, item.y)
                val label = if (multiplier > 1) "+$points 🌟 (2x!)" else "+$points ⭐"
                addFloatingText(label, item.x, item.y - 15f, Color(0xFFFFEB3B))
            }
            CollectibleKind.CARROT -> {
                carrotsCollectedThisRun++
                coinsCollectedThisRun += item.kind.coinValue
                val points = item.kind.pointValue
                score += points
                hasCarrotBoost = true
                carrotBoostTimeRemaining = carrotBoostMaxDuration
                onCoinsChanged(coinsCollectedThisRun)
                onScoreChanged(score)
                onSoundEvent(SoundEvent.CARROT)
                spawnCarrotFanfareParticles(item.x, item.y)
                addFloatingText("CARROT BOOST! 🥕", item.x, item.y - 25f, Color(0xFFFF6D00))
            }
        }
    }

    private fun spawnNextPattern() {
        val spawnX = screenWidth + 80f
        val patternChoice = Random.nextInt(100)

        // Select world-appropriate obstacle kinds
        val availableObstacles = when (currentWorld) {
            WorldType.GREEN_FOREST -> listOf(ObstacleKind.ROCK, ObstacleKind.WOODEN_BOX, ObstacleKind.PUDDLE, ObstacleKind.LOW_BRANCH)
            WorldType.CANDY_LAND -> listOf(ObstacleKind.CANDY_ROCK, ObstacleKind.GUMMY_BLOCK, ObstacleKind.CHOCOLATE_PUDDLE, ObstacleKind.LOLLIPOP_GATE)
            WorldType.SNOW_LAND -> listOf(ObstacleKind.SNOW_BLOCK, ObstacleKind.ICE_MOUND, ObstacleKind.ICE_PUDDLE, ObstacleKind.ICICLE_GATE)
            WorldType.SUNNY_BEACH -> listOf(ObstacleKind.SAND_CASTLE, ObstacleKind.BEACH_BALL, ObstacleKind.TIDE_PUDDLE, ObstacleKind.PALM_BRANCH)
            WorldType.SPACE_WORLD -> listOf(ObstacleKind.METEOR, ObstacleKind.SPACE_CRATE, ObstacleKind.MOON_CRATER, ObstacleKind.LASER_GATE)
        }

        when {
            // Pattern 1: Single Ground Obstacle + Arc of Coins to jump over
            patternChoice < 35 -> {
                val groundObs = availableObstacles.filter { !it.isOverhead }.random()
                val obsY = groundY - groundObs.height + 4f
                obstacles.add(
                    Obstacle(
                        id = entityIdCounter++,
                        kind = groundObs,
                        x = spawnX,
                        y = obsY,
                        width = groundObs.width,
                        height = groundObs.height
                    )
                )
                // Jump arc coins
                for (i in 0..4) {
                    val coinX = spawnX - 80f + (i * 45f)
                    val arcY = groundY - 50f - sin(i / 4f * Math.PI.toFloat()) * 90f
                    collectibles.add(
                        Collectible(
                            id = entityIdCounter++,
                            kind = CollectibleKind.COIN,
                            x = coinX,
                            y = arcY,
                            baseY = arcY
                        )
                    )
                }
            }

            // Pattern 2: Overhead Obstacle (Slide under) + Slide Line Coins
            patternChoice < 60 -> {
                val overheadObs = availableObstacles.filter { it.isOverhead }.randomOrNull()
                    ?: availableObstacles.random()

                if (overheadObs.isOverhead) {
                    val obsY = groundY - 110f
                    obstacles.add(
                        Obstacle(
                            id = entityIdCounter++,
                            kind = overheadObs,
                            x = spawnX,
                            y = obsY,
                            width = overheadObs.width,
                            height = overheadObs.height
                        )
                    )
                    // Low sliding coins
                    for (i in 0..3) {
                        val coinX = spawnX - 60f + (i * 40f)
                        val coinY = groundY - 18f
                        collectibles.add(
                            Collectible(
                                id = entityIdCounter++,
                                kind = CollectibleKind.COIN,
                                x = coinX,
                                y = coinY,
                                baseY = coinY
                            )
                        )
                    }
                } else {
                    val obsY = groundY - overheadObs.height + 4f
                    obstacles.add(
                        Obstacle(
                            id = entityIdCounter++,
                            kind = overheadObs,
                            x = spawnX,
                            y = obsY,
                            width = overheadObs.width,
                            height = overheadObs.height
                        )
                    )
                }
            }

            // Pattern 3: High Star Reward + Coins
            patternChoice < 85 -> {
                val starY = groundY - 140f
                collectibles.add(
                    Collectible(
                        id = entityIdCounter++,
                        kind = CollectibleKind.STAR,
                        x = spawnX + 60f,
                        y = starY,
                        baseY = starY
                    )
                )
                for (i in 0..3) {
                    val coinX = spawnX + (i * 40f)
                    val coinY = groundY - 26f
                    collectibles.add(
                        Collectible(
                            id = entityIdCounter++,
                            kind = CollectibleKind.COIN,
                            x = coinX,
                            y = coinY,
                            baseY = coinY
                        )
                    )
                }
            }

            // Pattern 4: Delicious Carrot Powerup Spurt!
            else -> {
                val carrotY = groundY - 70f
                collectibles.add(
                    Collectible(
                        id = entityIdCounter++,
                        kind = CollectibleKind.CARROT,
                        x = spawnX + 50f,
                        y = carrotY,
                        baseY = carrotY
                    )
                )
                // Flanking stars and coins
                collectibles.add(
                    Collectible(
                        id = entityIdCounter++,
                        kind = CollectibleKind.COIN,
                        x = spawnX,
                        y = groundY - 26f,
                        baseY = groundY - 26f
                    )
                )
                collectibles.add(
                    Collectible(
                        id = entityIdCounter++,
                        kind = CollectibleKind.COIN,
                        x = spawnX + 100f,
                        y = groundY - 26f,
                        baseY = groundY - 26f
                    )
                )
            }
        }
    }

    private fun updateParticlesAndText(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= p.decayRate
            p.alpha = p.life.coerceIn(0f, 1f)
            if (p.life <= 0f) {
                pIter.remove()
            }
        }

        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val t = tIter.next()
            t.y -= 45f * dt
            t.life -= dt * 1.2f
            t.alpha = t.life.coerceIn(0f, 1f)
            if (t.life <= 0f) {
                tIter.remove()
            }
        }
    }

    fun addFloatingText(text: String, x: Float, y: Float, color: Color) {
        floatingTexts.add(
            FloatingText(
                id = entityIdCounter++,
                text = text,
                x = x,
                y = y,
                color = color
            )
        )
    }

    // --- Particle Spawners ---

    private fun spawnRunDust() {
        particles.add(
            GameParticle(
                x = playerX - 16f,
                y = groundY + 4f,
                vx = -120f + Random.nextFloat() * 40f,
                vy = -30f - Random.nextFloat() * 30f,
                color = currentWorld.groundColor.copy(alpha = 0.5f),
                size = 6f,
                decayRate = 0.05f,
                shape = ParticleShape.DUST
            )
        )
    }

    private fun spawnJumpParticles() {
        for (i in 0..5) {
            particles.add(
                GameParticle(
                    x = playerX + (Random.nextFloat() - 0.5f) * 24f,
                    y = groundY + 4f,
                    vx = (Random.nextFloat() - 0.5f) * 180f,
                    vy = -60f - Random.nextFloat() * 60f,
                    color = Color.White,
                    size = 7f,
                    decayRate = 0.04f,
                    shape = ParticleShape.DUST
                )
            )
        }
    }

    private fun spawnSlideParticles() {
        for (i in 0..4) {
            particles.add(
                GameParticle(
                    x = playerX - 20f,
                    y = groundY + 4f,
                    vx = -180f - Random.nextFloat() * 60f,
                    vy = -40f - Random.nextFloat() * 40f,
                    color = Color(0x99FFFFFF),
                    size = 8f,
                    decayRate = 0.05f,
                    shape = ParticleShape.DUST
                )
            )
        }
    }

    private fun spawnLandingDust() {
        for (i in 0..6) {
            particles.add(
                GameParticle(
                    x = playerX + (Random.nextFloat() - 0.5f) * 30f,
                    y = groundY + 2f,
                    vx = (Random.nextFloat() - 0.5f) * 200f,
                    vy = -40f - Random.nextFloat() * 40f,
                    color = Color(0x88FFFFFF),
                    size = 7f,
                    decayRate = 0.05f,
                    shape = ParticleShape.DUST
                )
            )
        }
    }

    private fun spawnHitParticles(x: Float, y: Float) {
        for (i in 0..8) {
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 260f,
                    vy = (Random.nextFloat() - 0.5f) * 260f,
                    color = Color(0xFFFF5252),
                    size = 9f,
                    decayRate = 0.035f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    private fun spawnSparkleParticles(x: Float, y: Float, color: Color) {
        for (i in 0..5) {
            particles.add(
                GameParticle(
                    x = x + (Random.nextFloat() - 0.5f) * 16f,
                    y = y + (Random.nextFloat() - 0.5f) * 16f,
                    vx = (Random.nextFloat() - 0.5f) * 160f,
                    vy = (Random.nextFloat() - 0.5f) * 160f,
                    color = color,
                    size = 8f,
                    decayRate = 0.04f,
                    shape = ParticleShape.SPARKLE
                )
            )
        }
    }

    private fun spawnStarBurstParticles(x: Float, y: Float) {
        for (i in 0..8) {
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 240f,
                    vy = (Random.nextFloat() - 0.5f) * 240f,
                    color = Color(0xFFFFD600),
                    size = 11f,
                    decayRate = 0.03f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    private fun spawnCarrotFanfareParticles(x: Float, y: Float) {
        val colors = listOf(Color(0xFFFF6D00), Color(0xFF66BB6A), Color(0xFFFFD600), Color(0xFFFF4081))
        for (i in 0..12) {
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 320f,
                    vy = (Random.nextFloat() - 0.5f) * 320f,
                    color = colors.random(),
                    size = 10f,
                    decayRate = 0.025f,
                    shape = ParticleShape.SPARKLE
                )
            )
        }
    }

    private fun spawnRainbowSparkle() {
        val rainbow = listOf(
            Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFFEE58),
            Color(0xFF66BB6A), Color(0xFF29B6F6), Color(0xFFAB47BC)
        )
        particles.add(
            GameParticle(
                x = playerX - 25f + Random.nextFloat() * 10f,
                y = playerY - 20f + (Random.nextFloat() - 0.5f) * 40f,
                vx = -160f,
                vy = (Random.nextFloat() - 0.5f) * 80f,
                color = rainbow.random(),
                size = 7f,
                decayRate = 0.04f,
                shape = ParticleShape.STAR
            )
        )
    }

    private fun spawnSmashParticles(x: Float, y: Float) {
        for (i in 0..10) {
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 340f,
                    vy = (Random.nextFloat() - 0.5f) * 340f,
                    color = Color(0xFFFFEB3B),
                    size = 10f,
                    decayRate = 0.035f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }
}
