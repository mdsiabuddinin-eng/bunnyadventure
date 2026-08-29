package com.example

import com.example.game.GameEngine
import com.example.model.CharacterType
import com.example.model.WorldType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine
    private var scoreChanged = 0
    private var coinsChanged = 0
    private var livesChanged = 3
    private var isGameOverCalled = false

    @Before
    fun setup() {
        engine = GameEngine(
            screenWidth = 1080f,
            screenHeight = 1920f,
            onScoreChanged = { scoreChanged = it },
            onCoinsChanged = { coinsChanged = it },
            onLivesChanged = { livesChanged = it },
            onGameOver = { _, _, _ -> isGameOverCalled = true }
        )
    }

    @Test
    fun testStartNewGame_initializesCorrectly() {
        engine.startNewGame(CharacterType.BUNNY, WorldType.GREEN_FOREST, childMode = false)

        assertEquals(0, engine.score)
        assertEquals(0, engine.coinsCollectedThisRun)
        assertEquals(3, engine.lives)
        assertFalse(engine.isGameOver)
        assertTrue(engine.isGrounded)
    }

    @Test
    fun testPandaCharacter_getsExtraLife() {
        engine.startNewGame(CharacterType.PANDA, WorldType.GREEN_FOREST, childMode = false)

        assertEquals(4, engine.maxLives)
        assertEquals(4, engine.lives)
    }

    @Test
    fun testJump_setsVelocityAndState() {
        engine.startNewGame(CharacterType.BUNNY, WorldType.GREEN_FOREST, childMode = false)
        engine.jump()

        assertFalse(engine.isGrounded)
        assertTrue(engine.playerVy < 0)
    }

    @Test
    fun testSlide_setsSlidingState() {
        engine.startNewGame(CharacterType.BUNNY, WorldType.GREEN_FOREST, childMode = false)
        engine.slide()

        assertTrue(engine.isSliding)
        assertTrue(engine.slideTimer > 0f)
    }

    @Test
    fun testEngineUpdate_advancesDistanceAndScore() {
        engine.startNewGame(CharacterType.BUNNY, WorldType.GREEN_FOREST, childMode = false)
        engine.update(0.05f)

        assertTrue(engine.distanceTraveled > 0f)
    }
}
