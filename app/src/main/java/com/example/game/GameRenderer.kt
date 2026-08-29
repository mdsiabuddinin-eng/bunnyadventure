package com.example.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.model.CharacterType
import com.example.model.Collectible
import com.example.model.CollectibleKind
import com.example.model.GameParticle
import com.example.model.Obstacle
import com.example.model.ObstacleKind
import com.example.model.ParticleShape
import com.example.model.WorldType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {

    fun drawWorldBackground(
        drawScope: DrawScope,
        world: WorldType,
        distanceTraveled: Float,
        groundY: Float
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // 1. Sky Gradient
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(world.skyColorTop, world.skyColorBottom),
                startY = 0f,
                endY = groundY
            ),
            size = Size(width, groundY)
        )

        // 2. Far Celestial / Decorative elements (Sun / Moon / Giant Planets)
        drawCelestial(drawScope, world, width, groundY)

        // 3. Far Parallax Layer (Distant Hills / Clouds)
        drawFarParallax(drawScope, world, distanceTraveled * 0.15f, width, groundY)

        // 4. Mid Parallax Layer (Trees / Candy Structures / Palms / Space crystals)
        drawMidParallax(drawScope, world, distanceTraveled * 0.45f, width, groundY)

        // 5. Foreground Ground
        drawGround(drawScope, world, distanceTraveled, width, height, groundY)
    }

    private fun drawCelestial(drawScope: DrawScope, world: WorldType, width: Float, groundY: Float) {
        when (world) {
            WorldType.GREEN_FOREST, WorldType.SUNNY_BEACH -> {
                // Bright warm sun with rays
                val sunCenter = Offset(width * 0.82f, groundY * 0.22f)
                drawScope.drawCircle(
                    color = Color(0x33FFD54F),
                    radius = 54f,
                    center = sunCenter
                )
                drawScope.drawCircle(
                    color = Color(0xFFFFEE58),
                    radius = 34f,
                    center = sunCenter
                )
                drawScope.drawCircle(
                    color = Color(0xFFFFF59D),
                    radius = 24f,
                    center = sunCenter
                )
            }
            WorldType.CANDY_LAND -> {
                // Soft pink pastel sun & donut cloud
                val sunCenter = Offset(width * 0.8f, groundY * 0.2f)
                drawScope.drawCircle(
                    color = Color(0x44FF80AB),
                    radius = 48f,
                    center = sunCenter
                )
                drawScope.drawCircle(
                    color = Color(0xFFFF80AB),
                    radius = 30f,
                    center = sunCenter
                )
            }
            WorldType.SNOW_LAND -> {
                // Pale cold winter sun
                val sunCenter = Offset(width * 0.82f, groundY * 0.2f)
                drawScope.drawCircle(
                    color = Color(0x44E0F7FA),
                    radius = 50f,
                    center = sunCenter
                )
                drawScope.drawCircle(
                    color = Color(0xFFFFFFFF),
                    radius = 28f,
                    center = sunCenter
                )
            }
            WorldType.SPACE_WORLD -> {
                // Ringed planet + distant glowing stars
                val planetCenter = Offset(width * 0.78f, groundY * 0.28f)
                drawScope.drawCircle(
                    color = Color(0xFFFF4081),
                    radius = 32f,
                    center = planetCenter
                )
                drawScope.drawOval(
                    color = Color(0xAA80D8FF),
                    topLeft = Offset(planetCenter.x - 48f, planetCenter.y - 10f),
                    size = Size(96f, 20f),
                    style = Stroke(width = 4f)
                )
                // Small twinkling stars in background
                val starPositions = listOf(
                    Offset(width * 0.15f, groundY * 0.15f),
                    Offset(width * 0.35f, groundY * 0.3f),
                    Offset(width * 0.55f, groundY * 0.12f),
                    Offset(width * 0.9f, groundY * 0.45f)
                )
                starPositions.forEach { pos ->
                    drawScope.drawCircle(Color.White, radius = 3f, center = pos)
                }
            }
        }
    }

    private fun drawFarParallax(
        drawScope: DrawScope,
        world: WorldType,
        offset: Float,
        width: Float,
        groundY: Float
    ) {
        val segmentWidth = 320f
        val startX = -(offset % segmentWidth) - segmentWidth

        var currentX = startX
        while (currentX < width + segmentWidth) {
            when (world) {
                WorldType.SPACE_WORLD -> {
                    // Cosmic Nebula Blobs
                    drawScope.drawCircle(
                        color = Color(0x228E24AA),
                        radius = 80f,
                        center = Offset(currentX + 120f, groundY * 0.45f)
                    )
                }
                else -> {
                    // Fluffy Clouds
                    drawCloud(drawScope, currentX + 80f, groundY * 0.22f, 1.1f)
                    drawCloud(drawScope, currentX + 220f, groundY * 0.38f, 0.85f)
                    // Distant Mountain Hills
                    val hillPath = Path().apply {
                        moveTo(currentX, groundY)
                        quadraticTo(
                            currentX + segmentWidth * 0.5f,
                            groundY - 110f,
                            currentX + segmentWidth,
                            groundY
                        )
                        close()
                    }
                    val hillColor = when (world) {
                        WorldType.GREEN_FOREST -> Color(0x4481C784)
                        WorldType.CANDY_LAND -> Color(0x44F8BBD0)
                        WorldType.SNOW_LAND -> Color(0x66B2EBF2)
                        WorldType.SUNNY_BEACH -> Color(0x4480DEEA)
                        WorldType.SPACE_WORLD -> Color(0x44512DA8)
                    }
                    drawScope.drawPath(hillPath, color = hillColor)
                }
            }
            currentX += segmentWidth
        }
    }

    private fun drawCloud(drawScope: DrawScope, x: Float, y: Float, scaleFactor: Float) {
        val cloudColor = Color(0xCCFFFFFF)
        val r = 24f * scaleFactor
        drawScope.drawCircle(cloudColor, radius = r, center = Offset(x, y))
        drawScope.drawCircle(cloudColor, radius = r * 1.25f, center = Offset(x + r * 1.1f, y - r * 0.2f))
        drawScope.drawCircle(cloudColor, radius = r * 0.95f, center = Offset(x + r * 2.2f, y))
        drawScope.drawRoundRect(
            color = cloudColor,
            topLeft = Offset(x - r * 0.5f, y),
            size = Size(r * 3.2f, r * 1.1f),
            cornerRadius = CornerRadius(r, r)
        )
    }

    private fun drawMidParallax(
        drawScope: DrawScope,
        world: WorldType,
        offset: Float,
        width: Float,
        groundY: Float
    ) {
        val step = 180f
        val startX = -(offset % step) - step

        var currentX = startX
        while (currentX < width + step) {
            when (world) {
                WorldType.GREEN_FOREST -> {
                    // Tree Trunk & Canopy
                    drawScope.drawRoundRect(
                        color = Color(0xFF6D4C41),
                        topLeft = Offset(currentX + 50f, groundY - 80f),
                        size = Size(14f, 80f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFF43A047),
                        radius = 38f,
                        center = Offset(currentX + 57f, groundY - 95f)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFF66BB6A),
                        radius = 26f,
                        center = Offset(currentX + 57f, groundY - 105f)
                    )
                }
                WorldType.CANDY_LAND -> {
                    // Giant Swirly Lollipop
                    drawScope.drawRoundRect(
                        color = Color(0xFFFFFFFF),
                        topLeft = Offset(currentX + 45f, groundY - 85f),
                        size = Size(10f, 85f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFFF4081),
                        radius = 32f,
                        center = Offset(currentX + 50f, groundY - 95f)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 20f,
                        center = Offset(currentX + 50f, groundY - 95f),
                        style = Stroke(width = 4f)
                    )
                }
                WorldType.SNOW_LAND -> {
                    // Snowy Pine Tree
                    val pinePath = Path().apply {
                        moveTo(currentX + 50f, groundY - 110f)
                        lineTo(currentX + 25f, groundY - 50f)
                        lineTo(currentX + 40f, groundY - 50f)
                        lineTo(currentX + 20f, groundY - 15f)
                        lineTo(currentX + 80f, groundY - 15f)
                        lineTo(currentX + 60f, groundY - 50f)
                        lineTo(currentX + 75f, groundY - 50f)
                        close()
                    }
                    drawScope.drawPath(pinePath, color = Color(0xFF26A69A))
                    drawScope.drawCircle(Color.White, radius = 8f, center = Offset(currentX + 50f, groundY - 110f))
                }
                WorldType.SUNNY_BEACH -> {
                    // Palm Tree
                    drawScope.drawRoundRect(
                        color = Color(0xFF795548),
                        topLeft = Offset(currentX + 48f, groundY - 85f),
                        size = Size(12f, 85f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawScope.drawOval(
                        color = Color(0xFF2E7D32),
                        topLeft = Offset(currentX + 15f, groundY - 105f),
                        size = Size(78f, 28f)
                    )
                }
                WorldType.SPACE_WORLD -> {
                    // Floating Cosmic Crystal
                    drawScope.drawRoundRect(
                        color = Color(0xFF00E5FF),
                        topLeft = Offset(currentX + 45f, groundY - 80f),
                        size = Size(16f, 50f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
            currentX += step
        }
    }

    private fun drawGround(
        drawScope: DrawScope,
        world: WorldType,
        distanceTraveled: Float,
        width: Float,
        height: Float,
        groundY: Float
    ) {
        val groundHeight = height - groundY

        // Main ground surface
        drawScope.drawRect(
            color = world.groundColor,
            topLeft = Offset(0f, groundY),
            size = Size(width, groundHeight)
        )

        // Sub-ground dirt/bedrock layer
        drawScope.drawRect(
            color = world.subGroundColor,
            topLeft = Offset(0f, groundY + 28f),
            size = Size(width, groundHeight - 28f)
        )

        // Animated Ground Stripes/Dots for high-speed sensation
        val stripeStep = 60f
        val offset = distanceTraveled % stripeStep
        var startX = -offset

        while (startX < width + stripeStep) {
            when (world) {
                WorldType.GREEN_FOREST -> {
                    // Cute little flowers & grass blades
                    drawScope.drawCircle(
                        color = Color(0xFFFFF176),
                        radius = 4f,
                        center = Offset(startX + 20f, groundY + 12f)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFFF80AB),
                        radius = 4f,
                        center = Offset(startX + 45f, groundY + 14f)
                    )
                }
                WorldType.CANDY_LAND -> {
                    // Sprinkles
                    drawScope.drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(startX + 15f, groundY + 10f),
                        size = Size(12f, 5f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
                WorldType.SNOW_LAND -> {
                    // Snow sparkles
                    drawScope.drawCircle(
                        color = Color(0xFF80DEEA),
                        radius = 3f,
                        center = Offset(startX + 30f, groundY + 12f)
                    )
                }
                WorldType.SUNNY_BEACH -> {
                    // Beach pebbles & shells
                    drawScope.drawCircle(
                        color = Color(0xFFFFCC80),
                        radius = 4f,
                        center = Offset(startX + 25f, groundY + 12f)
                    )
                }
                WorldType.SPACE_WORLD -> {
                    // Neon Grid Lines
                    drawScope.drawLine(
                        color = Color(0xFF00E676),
                        start = Offset(startX, groundY + 4f),
                        end = Offset(startX + 35f, groundY + 4f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
            startX += stripeStep
        }
    }

    /**
     * Draw the player character (Bunny, Puppy, Kitten, Panda)
     */
    fun drawPlayer(
        drawScope: DrawScope,
        character: CharacterType,
        playerX: Float,
        playerY: Float,
        isSliding: Boolean,
        isJumping: Boolean,
        runFrame: Float,
        isInvulnerable: Boolean,
        hasCarrotBoost: Boolean
    ) {
        // If invulnerable, blink
        if (isInvulnerable && (runFrame.toInt() % 4 < 2)) {
            return
        }

        drawScope.translate(left = playerX, top = playerY) {
            // Carrot Boost Rainbow Glow
            if (hasCarrotBoost) {
                drawCircle(
                    color = Color(0x66FFD700),
                    radius = 48f,
                    center = Offset(0f, if (isSliding) 10f else -10f)
                )
                drawCircle(
                    color = Color(0x44FF4081),
                    radius = 56f,
                    center = Offset(0f, if (isSliding) 10f else -10f)
                )
            }

            // Shadow on ground
            drawOval(
                color = Color(0x33000000),
                topLeft = Offset(-28f, if (isSliding) 24f else 28f),
                size = Size(56f, 14f)
            )

            val bobY = if (isJumping) 0f else sin(runFrame * PI.toFloat() * 2f) * 4f
            val legSwing = if (isJumping) 0.5f else sin(runFrame * PI.toFloat() * 2f)

            when (character) {
                CharacterType.BUNNY -> drawBunny(drawScope, isSliding, isJumping, bobY, legSwing)
                CharacterType.PUPPY -> drawPuppy(drawScope, isSliding, isJumping, bobY, legSwing)
                CharacterType.KITTEN -> drawKitten(drawScope, isSliding, isJumping, bobY, legSwing)
                CharacterType.PANDA -> drawPanda(drawScope, isSliding, isJumping, bobY, legSwing)
            }
        }
    }

    private fun drawBunny(
        drawScope: DrawScope,
        isSliding: Boolean,
        isJumping: Boolean,
        bobY: Float,
        legSwing: Float
    ) {
        val white = Color(0xFFFFFFFF)
        val pink = Color(0xFFFF80AB)
        val deepPink = Color(0xFFFF4081)
        val eyeColor = Color(0xFF263238)

        if (isSliding) {
            // Squashed, sliding bunny
            // Body
            drawScope.drawOval(
                color = white,
                topLeft = Offset(-34f, 2f),
                size = Size(68f, 28f)
            )
            // Ears pinned back
            drawScope.drawRoundRect(
                color = white,
                topLeft = Offset(-46f, 4f),
                size = Size(28f, 10f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawScope.drawRoundRect(
                color = pink,
                topLeft = Offset(-44f, 6f),
                size = Size(20f, 6f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Head
            drawScope.drawCircle(white, radius = 18f, center = Offset(18f, 10f))
            // Eye & Cheek
            drawScope.drawCircle(eyeColor, radius = 3.5f, center = Offset(24f, 8f))
            drawScope.drawCircle(pink, radius = 4f, center = Offset(22f, 16f))
            // Nose
            drawScope.drawCircle(deepPink, radius = 2.5f, center = Offset(30f, 10f))
            // Tail
            drawScope.drawCircle(white, radius = 8f, center = Offset(-32f, 10f))
        } else {
            // Running / Jumping upright Bunny
            val headCenter = Offset(4f, -14f + bobY)

            // Ears
            val earTilt = if (isJumping) -8f else legSwing * 12f
            // Left Ear
            drawScope.rotate(degrees = -10f + earTilt, pivot = Offset(headCenter.x - 6f, headCenter.y - 16f)) {
                drawRoundRect(
                    color = white,
                    topLeft = Offset(headCenter.x - 12f, headCenter.y - 48f),
                    size = Size(14f, 34f),
                    cornerRadius = CornerRadius(7f, 7f)
                )
                drawRoundRect(
                    color = pink,
                    topLeft = Offset(headCenter.x - 10f, headCenter.y - 44f),
                    size = Size(10f, 26f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
            }
            // Right Ear
            drawScope.rotate(degrees = 15f - earTilt, pivot = Offset(headCenter.x + 8f, headCenter.y - 16f)) {
                drawRoundRect(
                    color = white,
                    topLeft = Offset(headCenter.x + 2f, headCenter.y - 48f),
                    size = Size(14f, 34f),
                    cornerRadius = CornerRadius(7f, 7f)
                )
                drawRoundRect(
                    color = pink,
                    topLeft = Offset(headCenter.x + 4f, headCenter.y - 44f),
                    size = Size(10f, 26f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
            }

            // Body
            drawScope.drawOval(
                color = white,
                topLeft = Offset(-20f, -4f + bobY),
                size = Size(40f, 32f)
            )

            // Fluffy Tail
            drawScope.drawCircle(white, radius = 9f, center = Offset(-22f, 10f + bobY))

            // Head
            drawScope.drawCircle(white, radius = 22f, center = headCenter)

            // Rosy Cheeks
            drawScope.drawCircle(pink, radius = 5f, center = Offset(headCenter.x + 12f, headCenter.y + 6f))
            drawScope.drawCircle(pink, radius = 4f, center = Offset(headCenter.x - 4f, headCenter.y + 6f))

            // Sparkly Eye
            drawScope.drawCircle(eyeColor, radius = 4.5f, center = Offset(headCenter.x + 10f, headCenter.y - 2f))
            drawScope.drawCircle(Color.White, radius = 1.5f, center = Offset(headCenter.x + 9f, headCenter.y - 4f))

            // Cute Nose
            drawScope.drawCircle(deepPink, radius = 3f, center = Offset(headCenter.x + 18f, headCenter.y + 2f))

            // Running Feet
            val foot1Y = 22f + legSwing * 6f
            val foot2Y = 22f - legSwing * 6f
            drawScope.drawOval(
                color = white,
                topLeft = Offset(-12f, foot1Y),
                size = Size(18f, 10f)
            )
            drawScope.drawOval(
                color = white,
                topLeft = Offset(4f, foot2Y),
                size = Size(20f, 10f)
            )
        }
    }

    private fun drawPuppy(
        drawScope: DrawScope,
        isSliding: Boolean,
        isJumping: Boolean,
        bobY: Float,
        legSwing: Float
    ) {
        val brown = Color(0xFFFFB74D)
        val darkBrown = Color(0xFF8D6E63)
        val noseColor = Color(0xFF263238)
        val tongueColor = Color(0xFFFF5252)

        if (isSliding) {
            drawScope.drawOval(brown, topLeft = Offset(-34f, 2f), size = Size(68f, 28f))
            drawScope.drawCircle(brown, radius = 18f, center = Offset(18f, 10f))
            drawScope.drawOval(darkBrown, topLeft = Offset(-10f, 4f), size = Size(20f, 12f))
            drawScope.drawCircle(noseColor, radius = 3.5f, center = Offset(26f, 8f))
            drawScope.drawCircle(noseColor, radius = 3f, center = Offset(32f, 10f))
        } else {
            val headCenter = Offset(4f, -14f + bobY)

            // Floppy puppy ears
            val earBob = legSwing * 10f
            drawScope.drawOval(
                darkBrown,
                topLeft = Offset(headCenter.x - 22f, headCenter.y - 12f + earBob),
                size = Size(16f, 28f)
            )
            drawScope.drawOval(
                darkBrown,
                topLeft = Offset(headCenter.x + 10f, headCenter.y - 12f - earBob),
                size = Size(16f, 28f)
            )

            // Body
            drawScope.drawOval(brown, topLeft = Offset(-20f, -4f + bobY), size = Size(40f, 32f))

            // Wagging tail
            val tailTilt = sin(legSwing * PI.toFloat()) * 18f
            drawScope.rotate(degrees = 30f + tailTilt, pivot = Offset(-22f, 4f + bobY)) {
                drawRoundRect(
                    darkBrown,
                    topLeft = Offset(-30f, 0f + bobY),
                    size = Size(12f, 8f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Head
            drawScope.drawCircle(brown, radius = 22f, center = headCenter)

            // Cute Eye patch
            drawScope.drawCircle(darkBrown, radius = 8f, center = Offset(headCenter.x + 8f, headCenter.y - 2f))
            drawScope.drawCircle(Color.White, radius = 3.5f, center = Offset(headCenter.x + 8f, headCenter.y - 2f))
            drawScope.drawCircle(noseColor, radius = 2f, center = Offset(headCenter.x + 8f, headCenter.y - 2f))

            // Snout & Tongue
            drawScope.drawCircle(Color(0xFFFFE0B2), radius = 10f, center = Offset(headCenter.x + 14f, headCenter.y + 4f))
            drawScope.drawCircle(noseColor, radius = 3.5f, center = Offset(headCenter.x + 20f, headCenter.y + 2f))
            drawScope.drawOval(tongueColor, topLeft = Offset(headCenter.x + 14f, headCenter.y + 9f), size = Size(8f, 6f))

            // Paws
            drawScope.drawOval(darkBrown, topLeft = Offset(-12f, 22f + legSwing * 6f), size = Size(18f, 10f))
            drawScope.drawOval(darkBrown, topLeft = Offset(4f, 22f - legSwing * 6f), size = Size(20f, 10f))
        }
    }

    private fun drawKitten(
        drawScope: DrawScope,
        isSliding: Boolean,
        isJumping: Boolean,
        bobY: Float,
        legSwing: Float
    ) {
        val peach = Color(0xFFFFCC80)
        val innerPink = Color(0xFFFF80AB)
        val eyeGreen = Color(0xFF00E676)
        val nosePink = Color(0xFFFF4081)

        if (isSliding) {
            drawScope.drawOval(peach, topLeft = Offset(-34f, 2f), size = Size(68f, 28f))
            drawScope.drawCircle(peach, radius = 18f, center = Offset(18f, 10f))
            drawScope.drawCircle(eyeGreen, radius = 3.5f, center = Offset(24f, 8f))
            drawScope.drawCircle(nosePink, radius = 2.5f, center = Offset(30f, 10f))
        } else {
            val headCenter = Offset(4f, -14f + bobY)

            // Pointy Cat Ears
            val earPath1 = Path().apply {
                moveTo(headCenter.x - 14f, headCenter.y - 12f)
                lineTo(headCenter.x - 10f, headCenter.y - 36f)
                lineTo(headCenter.x - 2f, headCenter.y - 16f)
                close()
            }
            val earPath2 = Path().apply {
                moveTo(headCenter.x + 4f, headCenter.y - 16f)
                lineTo(headCenter.x + 12f, headCenter.y - 36f)
                lineTo(headCenter.x + 18f, headCenter.y - 12f)
                close()
            }
            drawScope.drawPath(earPath1, peach)
            drawScope.drawPath(earPath2, peach)

            // Inner Ear Pink
            val innerPath1 = Path().apply {
                moveTo(headCenter.x - 12f, headCenter.y - 14f)
                lineTo(headCenter.x - 9f, headCenter.y - 30f)
                lineTo(headCenter.x - 4f, headCenter.y - 16f)
                close()
            }
            drawScope.drawPath(innerPath1, innerPink)

            // Body
            drawScope.drawOval(peach, topLeft = Offset(-20f, -4f + bobY), size = Size(40f, 32f))

            // Curling Cat Tail
            val tailPath = Path().apply {
                moveTo(-18f, 6f + bobY)
                quadraticTo(-36f, 0f + bobY, -30f, -18f + bobY)
            }
            drawScope.drawPath(tailPath, peach, style = Stroke(width = 6f, cap = StrokeCap.Round))

            // Head
            drawScope.drawCircle(peach, radius = 22f, center = headCenter)

            // Sparkling Emerald Eye
            drawScope.drawCircle(eyeGreen, radius = 5f, center = Offset(headCenter.x + 10f, headCenter.y - 2f))
            drawScope.drawCircle(Color(0xFF212121), radius = 2.5f, center = Offset(headCenter.x + 11f, headCenter.y - 2f))
            drawScope.drawCircle(Color.White, radius = 1.2f, center = Offset(headCenter.x + 9f, headCenter.y - 4f))

            // Nose & Cheeks
            drawScope.drawCircle(nosePink, radius = 3f, center = Offset(headCenter.x + 18f, headCenter.y + 2f))

            // Whiskers
            drawScope.drawLine(
                Color.White,
                start = Offset(headCenter.x + 14f, headCenter.y + 4f),
                end = Offset(headCenter.x + 30f, headCenter.y + 1f),
                strokeWidth = 2f
            )
            drawScope.drawLine(
                Color.White,
                start = Offset(headCenter.x + 14f, headCenter.y + 6f),
                end = Offset(headCenter.x + 29f, headCenter.y + 9f),
                strokeWidth = 2f
            )

            // Paws
            drawScope.drawOval(peach, topLeft = Offset(-12f, 22f + legSwing * 6f), size = Size(18f, 10f))
            drawScope.drawOval(peach, topLeft = Offset(4f, 22f - legSwing * 6f), size = Size(20f, 10f))
        }
    }

    private fun drawPanda(
        drawScope: DrawScope,
        isSliding: Boolean,
        isJumping: Boolean,
        bobY: Float,
        legSwing: Float
    ) {
        val white = Color(0xFFFFFFFF)
        val black = Color(0xFF263238)
        val bambooGreen = Color(0xFF4CAF50)

        if (isSliding) {
            drawScope.drawOval(white, topLeft = Offset(-34f, 2f), size = Size(68f, 28f))
            drawScope.drawCircle(black, radius = 9f, center = Offset(-24f, 10f))
            drawScope.drawCircle(white, radius = 18f, center = Offset(18f, 10f))
            drawScope.drawCircle(black, radius = 5f, center = Offset(24f, 8f))
        } else {
            val headCenter = Offset(4f, -14f + bobY)

            // Round black ears
            drawScope.drawCircle(black, radius = 8f, center = Offset(headCenter.x - 14f, headCenter.y - 18f))
            drawScope.drawCircle(black, radius = 8f, center = Offset(headCenter.x + 14f, headCenter.y - 18f))

            // Body
            drawScope.drawOval(white, topLeft = Offset(-20f, -4f + bobY), size = Size(40f, 32f))
            // Black vest pattern
            drawScope.drawOval(black, topLeft = Offset(-16f, 0f + bobY), size = Size(32f, 18f))

            // Head
            drawScope.drawCircle(white, radius = 22f, center = headCenter)

            // Black eye patch
            drawScope.drawOval(black, topLeft = Offset(headCenter.x + 4f, headCenter.y - 8f), size = Size(14f, 12f))
            drawScope.drawCircle(Color.White, radius = 3.5f, center = Offset(headCenter.x + 11f, headCenter.y - 2f))
            drawScope.drawCircle(black, radius = 1.8f, center = Offset(headCenter.x + 11f, headCenter.y - 2f))

            // Cute nose
            drawScope.drawCircle(black, radius = 3.5f, center = Offset(headCenter.x + 18f, headCenter.y + 2f))

            // Little bamboo stalk
            drawScope.drawRoundRect(
                bambooGreen,
                topLeft = Offset(headCenter.x + 14f, headCenter.y + 8f),
                size = Size(14f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )

            // Black paws
            drawScope.drawOval(black, topLeft = Offset(-12f, 22f + legSwing * 6f), size = Size(18f, 10f))
            drawScope.drawOval(black, topLeft = Offset(4f, 22f - legSwing * 6f), size = Size(20f, 10f))
        }
    }

    /**
     * Draw Obstacles
     */
    fun drawObstacle(drawScope: DrawScope, obstacle: Obstacle) {
        val x = obstacle.x
        val y = obstacle.y
        val w = obstacle.width
        val h = obstacle.height

        when (obstacle.kind) {
            ObstacleKind.ROCK, ObstacleKind.CANDY_ROCK, ObstacleKind.METEOR, ObstacleKind.ICE_MOUND -> {
                // 3D Shaded Boulder
                drawScope.drawRoundRect(
                    color = obstacle.kind.color,
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(w * 0.4f, h * 0.4f)
                )
                // Highlight glint
                drawScope.drawRoundRect(
                    color = Color(0x55FFFFFF),
                    topLeft = Offset(x + 6f, y + 4f),
                    size = Size(w * 0.4f, h * 0.35f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
            ObstacleKind.WOODEN_BOX, ObstacleKind.SPACE_CRATE, ObstacleKind.SNOW_BLOCK, ObstacleKind.GUMMY_BLOCK -> {
                // Square Crate
                drawScope.drawRoundRect(
                    color = obstacle.kind.color,
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Border & Cross Brace
                drawScope.drawRoundRect(
                    color = Color(0x44000000),
                    topLeft = Offset(x + 3f, y + 3f),
                    size = Size(w - 6f, h - 6f),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 3f)
                )
                drawScope.drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(x + 4f, y + 4f),
                    end = Offset(x + w - 4f, y + h - 4f),
                    strokeWidth = 3f
                )
            }
            ObstacleKind.SAND_CASTLE -> {
                // Sand Castle with turrets
                drawScope.drawRoundRect(
                    color = obstacle.kind.color,
                    topLeft = Offset(x, y + 10f),
                    size = Size(w, h - 10f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawScope.drawRoundRect(
                    color = obstacle.kind.color,
                    topLeft = Offset(x + 10f, y),
                    size = Size(w - 20f, 16f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawScope.drawCircle(Color(0xFFFF5722), radius = 4f, center = Offset(x + w / 2f, y - 2f))
            }
            ObstacleKind.PUDDLE, ObstacleKind.CHOCOLATE_PUDDLE, ObstacleKind.ICE_PUDDLE, ObstacleKind.TIDE_PUDDLE, ObstacleKind.MOON_CRATER -> {
                // Puddle on ground
                drawScope.drawOval(
                    color = obstacle.kind.color,
                    topLeft = Offset(x, y),
                    size = Size(w, h)
                )
                drawScope.drawOval(
                    color = Color(0x66FFFFFF),
                    topLeft = Offset(x + 8f, y + 3f),
                    size = Size(w * 0.45f, h * 0.4f)
                )
            }
            ObstacleKind.BEACH_BALL -> {
                // Cheerful striped beach ball
                val center = Offset(x + w / 2f, y + h / 2f)
                val radius = w / 2f
                drawScope.drawCircle(Color.White, radius = radius, center = center)
                drawScope.drawArc(
                    color = Color(0xFFFF5252),
                    startAngle = 0f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(x, y),
                    size = Size(w, h)
                )
                drawScope.drawArc(
                    color = Color(0xFF29B6F6),
                    startAngle = 120f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(x, y),
                    size = Size(w, h)
                )
                drawScope.drawArc(
                    color = Color(0xFFFFD600),
                    startAngle = 240f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(x, y),
                    size = Size(w, h)
                )
                drawScope.drawCircle(Color.White, radius = 6f, center = center)
            }
            // Overhead Obstacles (Slide under)
            ObstacleKind.LOW_BRANCH, ObstacleKind.PALM_BRANCH -> {
                // Branch extending downwards
                drawScope.drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(x + 10f, y),
                    size = Size(w - 20f, 14f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Hanging lush leaves
                drawScope.drawOval(
                    color = obstacle.kind.color,
                    topLeft = Offset(x, y + 8f),
                    size = Size(w, h - 8f)
                )
                drawScope.drawCircle(Color(0xFF81C784), radius = 8f, center = Offset(x + w * 0.3f, y + h * 0.6f))
            }
            ObstacleKind.LOLLIPOP_GATE -> {
                // Candy Overhead Bar
                drawScope.drawRoundRect(
                    color = Color(0xFFFF4081),
                    topLeft = Offset(x, y),
                    size = Size(w, 18f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawScope.drawCircle(Color(0xFFFF1744), radius = 14f, center = Offset(x + w / 2f, y + 24f))
                drawScope.drawCircle(Color.White, radius = 7f, center = Offset(x + w / 2f, y + 24f))
            }
            ObstacleKind.ICICLE_GATE -> {
                // Hanging Icicles
                drawScope.drawRoundRect(
                    color = Color(0xFFB2EBF2),
                    topLeft = Offset(x, y),
                    size = Size(w, 12f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // 3 Sharp icicles
                val path = Path().apply {
                    moveTo(x + 4f, y + 12f); lineTo(x + 14f, y + h); lineTo(x + 24f, y + 12f)
                    moveTo(x + 24f, y + 12f); lineTo(x + 36f, y + h - 6f); lineTo(x + 48f, y + 12f)
                    moveTo(x + 48f, y + 12f); lineTo(x + 56f, y + h); lineTo(x + w - 4f, y + 12f)
                    close()
                }
                drawScope.drawPath(path, color = Color(0xFF80DEEA))
            }
            ObstacleKind.LASER_GATE -> {
                // Glowing Neon Sci-Fi Barrier
                drawScope.drawRoundRect(
                    color = Color(0xFF311B92),
                    topLeft = Offset(x, y),
                    size = Size(w, 14f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFFF1744),
                    topLeft = Offset(x + 4f, y + 16f),
                    size = Size(w - 8f, h - 16f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 3f)
                )
                drawScope.drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(x + 6f, y + h / 2f),
                    end = Offset(x + w - 6f, y + h / 2f),
                    strokeWidth = 5f
                )
            }
        }
    }

    /**
     * Draw Collectibles (Coins, Stars, Carrots)
     */
    fun drawCollectible(drawScope: DrawScope, collectible: Collectible) {
        val x = collectible.x
        val y = collectible.y
        val size = collectible.kind.size
        val anim = collectible.animPhase

        when (collectible.kind) {
            CollectibleKind.COIN -> {
                // 3D Spinning Gold Coin
                val spinWidth = (cos(anim * PI.toFloat() * 2f).coerceIn(-1f, 1f)) * (size * 0.9f)
                val coinW = kotlin.math.abs(spinWidth).coerceAtLeast(4f)
                val coinX = x - coinW / 2f
                val coinY = y - size / 2f

                // Outer Gold Rim
                drawScope.drawOval(
                    color = Color(0xFFFFB300),
                    topLeft = Offset(coinX, coinY),
                    size = Size(coinW, size)
                )
                // Inner Bright Yellow
                drawScope.drawOval(
                    color = Color(0xFFFFEA00),
                    topLeft = Offset(coinX + coinW * 0.15f, coinY + size * 0.15f),
                    size = Size(coinW * 0.7f, size * 0.7f)
                )
                // Shimmer glint
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(coinX + coinW * 0.4f, coinY + size * 0.35f)
                )
            }
            CollectibleKind.STAR -> {
                // Sparkling 5-Pointed Gold Star
                val starScale = 1f + sin(anim * PI.toFloat() * 2f) * 0.12f
                drawScope.scale(scale = starScale, pivot = Offset(x, y)) {
                    // Outer Soft Halo
                    drawCircle(
                        color = Color(0x55FFD600),
                        radius = size * 0.75f,
                        center = Offset(x, y)
                    )
                    // Star Polygon
                    val starPath = createStarPath(x, y, 5, size * 0.55f, size * 0.26f)
                    drawPath(starPath, color = Color(0xFFFFD600))
                    val innerStarPath = createStarPath(x, y, 5, size * 0.38f, size * 0.18f)
                    drawPath(innerStarPath, color = Color(0xFFFFF9C4))
                }
            }
            CollectibleKind.CARROT -> {
                // Juicy Carrot with Bushy Green Top
                val floatBob = sin(anim * PI.toFloat() * 2f) * 4f
                val carrotY = y + floatBob

                drawScope.rotate(degrees = 15f + sin(anim * 4f) * 8f, pivot = Offset(x, carrotY)) {
                    // Orange Carrot Body
                    val carrotBody = Path().apply {
                        moveTo(x - 8f, carrotY - 6f)
                        lineTo(x + 8f, carrotY - 6f)
                        lineTo(x, carrotY + 18f)
                        close()
                    }
                    drawPath(carrotBody, color = Color(0xFFFF6D00))

                    // Carrot texture ridges
                    drawLine(Color(0xFFE65100), Offset(x - 4f, carrotY), Offset(x + 2f, carrotY), strokeWidth = 2f)
                    drawLine(Color(0xFFE65100), Offset(x - 2f, carrotY + 7f), Offset(x + 3f, carrotY + 7f), strokeWidth = 2f)

                    // Green bushy leaves
                    drawCircle(Color(0xFF4CAF50), radius = 6f, center = Offset(x - 4f, carrotY - 11f))
                    drawCircle(Color(0xFF66BB6A), radius = 7f, center = Offset(x, carrotY - 13f))
                    drawCircle(Color(0xFF4CAF50), radius = 6f, center = Offset(x + 4f, carrotY - 11f))
                }
            }
        }
    }

    private fun createStarPath(cx: Float, cy: Float, points: Int, outerRadius: Float, innerRadius: Float): Path {
        val path = Path()
        val step = PI / points
        var angle = -PI / 2
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            angle += step
        }
        path.close()
        return path
    }

    /**
     * Draw particle burst and sparkle effects
     */
    fun drawParticle(drawScope: DrawScope, particle: GameParticle) {
        val alphaColor = particle.color.copy(alpha = particle.alpha.coerceIn(0f, 1f))
        when (particle.shape) {
            ParticleShape.CIRCLE -> {
                drawScope.drawCircle(
                    color = alphaColor,
                    radius = particle.size * particle.life,
                    center = Offset(particle.x, particle.y)
                )
            }
            ParticleShape.STAR -> {
                val starPath = createStarPath(
                    particle.x,
                    particle.y,
                    4,
                    particle.size * particle.life,
                    particle.size * 0.4f * particle.life
                )
                drawScope.drawPath(starPath, color = alphaColor)
            }
            ParticleShape.SPARKLE -> {
                val s = particle.size * particle.life
                drawScope.drawLine(
                    color = alphaColor,
                    start = Offset(particle.x - s, particle.y),
                    end = Offset(particle.x + s, particle.y),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = alphaColor,
                    start = Offset(particle.x, particle.y - s),
                    end = Offset(particle.x, particle.y + s),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
            ParticleShape.DUST -> {
                drawScope.drawCircle(
                    color = alphaColor,
                    radius = particle.size * particle.life * 1.2f,
                    center = Offset(particle.x, particle.y)
                )
            }
        }
    }
}
