package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MarioDao
import com.example.data.MarioHighScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Tile Types
const val TILE_EMPTY = 0
const val TILE_GROUND = 1
const val TILE_BRICK = 2
const val TILE_QUESTION_COIN = 3
const val TILE_QUESTION_MUSHROOM = 4
const val TILE_QUESTION_FLOWER = 5
const val TILE_SOLID = 6
const val TILE_PIPE_TL = 7
const val TILE_PIPE_TR = 8
const val TILE_PIPE_BL = 9
const val TILE_PIPE_BR = 10
const val TILE_COIN = 11
const val TILE_FLAGPOLE = 12
const val TILE_FLAG_TOP = 13
const val TILE_CASTLE_BRICK = 14
const val TILE_CASTLE_DOOR = 15
const val TILE_HITSOLID = 16 // Mystery block after being hit
const val TILE_QUESTION_STAR = 17 // Glowing Star Mystery Block

// Game Statuses
enum class GameStatus {
    START_SCREEN,
    PLAYING,
    PAUSED,
    VICTORY_SLIDE,
    VICTORY_WALK,
    VICTORY_SCORING,
    GAME_OVER,
    DEATH_ANIMATION,
    LEADERBOARD
}

// Powerup Type
enum class PowerupType {
    MUSHROOM,
    FLOWER,
    STAR
}

// Enemy Type
enum class EnemyType {
    GOOMBA,
    KOOPA
}

// Data structures
data class Enemy(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val type: EnemyType,
    var isSquashed: Boolean = false,
    var isShell: Boolean = false,
    var shellSpeed: Float = 0f,
    var squashedTimer: Int = 0,
    val width: Float = 28f,
    val height: Float = 28f
)

data class Powerup(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val type: PowerupType,
    val width: Float = 28f,
    val height: Float = 28f
)

data class Fireball(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var bounces: Int = 0
)

data class ScorePopup(
    val text: String,
    var x: Float,
    var y: Float,
    var alpha: Float = 1f,
    var timer: Int = 30
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float = 0f,
    var timer: Int = 40
)

// Enemy Factory Pattern interface
interface EnemyFactory {
    fun createGoomba(id: Int, x: Float, y: Float, vx: Float): Enemy
    fun createKoopa(id: Int, x: Float, y: Float, vx: Float): Enemy
}

class DefaultEnemyFactory : EnemyFactory {
    override fun createGoomba(id: Int, x: Float, y: Float, vx: Float): Enemy {
        return Enemy(id = id, x = x, y = y, vx = vx, vy = 0f, type = EnemyType.GOOMBA)
    }

    override fun createKoopa(id: Int, x: Float, y: Float, vx: Float): Enemy {
        return Enemy(id = id, x = x, y = y, vx = vx, vy = 0f, type = EnemyType.KOOPA, width = 28f, height = 36f) // Koopa is slightly taller!
    }
}

class MarioViewModel(application: Application) : AndroidViewModel(application) {

    private val marioDao: MarioDao = AppDatabase.getDatabase(application).marioDao()
    private val enemyFactory: EnemyFactory = DefaultEnemyFactory()

    // Leaderboard state
    val topScores: StateFlow<List<MarioHighScore>> = marioDao.getTopTenScores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Game Status
    private val _gameStatus = MutableStateFlow(GameStatus.START_SCREEN)
    val gameStatus: StateFlow<GameStatus> = _gameStatus.asStateFlow()

    // Stats
    val score = mutableStateOf(0)
    val coins = mutableStateOf(0)
    val lives = mutableStateOf(3)
    val timeRemaining = mutableStateOf(400)
    val selectedWorld = mutableStateOf(1) // 1, 2, or 3
    val playerNameInput = mutableStateOf("MAR")

    // Mario State
    val marioX = mutableStateOf(100f)
    val marioY = mutableStateOf(100f)
    val marioVx = mutableStateOf(0f)
    val marioVy = mutableStateOf(0f)
    val isGrounded = mutableStateOf(false)
    val marioSize = mutableStateOf("SMALL") // SMALL, BIG, FIRE
    val invincibilityFrames = mutableStateOf(0)
    val flagSlideProgress = mutableStateOf(0f)
    val runFrameIndex = mutableStateOf(0f)
    val facingRight = mutableStateOf(true)

    // Starman & P-Meter states for creative features
    val isStarman = mutableStateOf(false)
    val starmanTimer = mutableStateOf(0)
    val pMeter = mutableStateOf(0f)

    // Camera offset (horizontal scroll)
    val cameraX = mutableStateOf(0f)

    // Game objects
    val enemies = mutableListOf<Enemy>()
    val powerups = mutableListOf<Powerup>()
    val fireballs = mutableListOf<Fireball>()
    val popups = mutableListOf<ScorePopup>()
    val particles = mutableListOf<Particle>()

    // Level map grid dimensions: 14 rows, 200 columns (each tile 32x32 units)
    val tileRows = 14
    val tileCols = 220
    val tileSize = 32f
    val levelGrid = Array(tileRows) { IntArray(tileCols) { TILE_EMPTY } }

    private var gameJob: Job? = null
    private var enemyIdSource = 1
    private var powerupIdSource = 1
    private var fireballIdSource = 1

    // Controls
    var isLeftPressed = false
    var isRightPressed = false
    var isJumpPressed = false
    var isSprintPressed = false

    val gameLock = Any()

    fun getEnemiesCopy(): List<Enemy> = synchronized(gameLock) { ArrayList(enemies) }
    fun getPowerupsCopy(): List<Powerup> = synchronized(gameLock) { ArrayList(powerups) }
    fun getFireballsCopy(): List<Fireball> = synchronized(gameLock) { ArrayList(fireballs) }
    fun getPopupsCopy(): List<ScorePopup> = synchronized(gameLock) { ArrayList(popups) }
    fun getParticlesCopy(): List<Particle> = synchronized(gameLock) { ArrayList(particles) }

    init {
        // Create initial level grid
        generateLevel(1)
    }

    fun startGame(worldNum: Int) {
        synchronized(gameLock) {
            selectedWorld.value = worldNum
            score.value = 0
            coins.value = 0
            lives.value = 3
            resetLevel()
            _gameStatus.value = GameStatus.PLAYING
        }
        startPhysicsLoop()
    }

    private fun resetLevel() {
        synchronized(gameLock) {
            marioX.value = 80f
            marioY.value = 250f
            marioVx.value = 0f
            marioVy.value = 0f
            isGrounded.value = false
            marioSize.value = "SMALL"
            invincibilityFrames.value = 0
            isStarman.value = false
            starmanTimer.value = 0
            pMeter.value = 0f
            timeRemaining.value = when (selectedWorld.value) {
                1 -> 400
                2 -> 360
                else -> 300
            }
            cameraX.value = 0f
            isLeftPressed = false
            isRightPressed = false
            isJumpPressed = false
            isSprintPressed = false

            enemies.clear()
            powerups.clear()
            fireballs.clear()
            popups.clear()
            particles.clear()

            generateLevel(selectedWorld.value)
            spawnEnemiesForWorld(selectedWorld.value)
        }
    }

    fun nextWorld() {
        if (selectedWorld.value < 3) {
            selectedWorld.value += 1
            resetLevel()
            _gameStatus.value = GameStatus.PLAYING
        } else {
            // Beat all worlds! Record score.
            _gameStatus.value = GameStatus.LEADERBOARD
        }
    }

    fun exitToMain() {
        stopPhysicsLoop()
        _gameStatus.value = GameStatus.START_SCREEN
    }

    fun enterLeaderboardDirect() {
        stopPhysicsLoop()
        _gameStatus.value = GameStatus.LEADERBOARD
    }

    fun togglePause() {
        if (_gameStatus.value == GameStatus.PLAYING) {
            _gameStatus.value = GameStatus.PAUSED
        } else if (_gameStatus.value == GameStatus.PAUSED) {
            _gameStatus.value = GameStatus.PLAYING
        }
    }

    fun saveScoreAndEnterLeaderboard() {
        val finalScore = score.value
        val finalCoins = coins.value
        val initials = playerNameInput.value.trim().uppercase().take(3).ifBlank { "MAR" }
        viewModelScope.launch {
            val levelLabel = "World 1-${selectedWorld.value}"
            marioDao.insertScore(
                MarioHighScore(
                    playerName = initials,
                    score = finalScore,
                    coins = finalCoins,
                    levelName = levelLabel
                )
            )
            _gameStatus.value = GameStatus.LEADERBOARD
        }
    }

    fun purgeLeaderboard() {
        viewModelScope.launch {
            marioDao.clearLeaderboard()
        }
    }

    private fun startPhysicsLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch(Dispatchers.Main) {
            var lastTime = System.nanoTime()
            val targetFps = 70 // Upgraded from 60 for ultra-smooth buttery fast action gaming!
            val optimalTime = 1000000000 / targetFps // ~14.28ms
            var timeAccumulator = 0

            while (isActive) {
                try {
                    val currentTime = System.nanoTime()
                    var elapsed = (currentTime - lastTime).toDouble()
                    if (elapsed > 250000000.0) { // Limit max frame step to 250ms
                        elapsed = 250000000.0
                    }
                    lastTime = currentTime

                    val statusValue = _gameStatus.value
                    if (statusValue == GameStatus.PLAYING) {
                        synchronized(gameLock) {
                            gameTick()
                        }
                        timeAccumulator++
                        if (timeAccumulator >= 60) {
                            timeAccumulator = 0
                            if (timeRemaining.value > 0) {
                                timeRemaining.value--
                                if (timeRemaining.value == 0) {
                                    synchronized(gameLock) {
                                        triggerPlayerDeath()
                                    }
                                }
                            }
                        }
                    } else if (statusValue == GameStatus.DEATH_ANIMATION) {
                        synchronized(gameLock) {
                            marioVy.value += 0.45f
                            marioY.value += marioVy.value
                            if (marioY.value > tileRows * tileSize + 100) {
                                lives.value--
                                if (lives.value > 0) {
                                    resetLevel()
                                    _gameStatus.value = GameStatus.PLAYING
                                } else {
                                    _gameStatus.value = GameStatus.GAME_OVER
                                }
                            }
                        }
                    } else if (statusValue == GameStatus.VICTORY_SLIDE) {
                        synchronized(gameLock) {
                            marioVy.value = 2f
                            marioY.value += marioVy.value
                            flagSlideProgress.value += 2f
                            
                            val groundYIndex = (marioY.value + getMarioHeight()) / tileSize
                            if (groundYIndex >= tileRows - 2) {
                                marioVy.value = 0f
                                _gameStatus.value = GameStatus.VICTORY_WALK
                                marioVx.value = 1.8f
                            }
                        }
                    } else if (statusValue == GameStatus.VICTORY_WALK) {
                        synchronized(gameLock) {
                            marioX.value += marioVx.value
                            val marioRightCol = ((marioX.value + getMarioWidth()) / tileSize).toInt()
                            
                            val currentCollisionTile = levelGrid[11].getOrElse(marioRightCol) { TILE_EMPTY }
                            if (currentCollisionTile == TILE_CASTLE_DOOR || marioX.value > (tileCols - 4) * tileSize) {
                                marioVx.value = 0f
                                _gameStatus.value = GameStatus.VICTORY_SCORING
                            }
                        }
                    } else if (statusValue == GameStatus.VICTORY_SCORING) {
                        synchronized(gameLock) {
                            if (timeRemaining.value > 0) {
                                val tick = min(10, timeRemaining.value)
                                timeRemaining.value -= tick
                                score.value += tick * 10
                            } else {
                                saveScoreAndEnterLeaderboard()
                            }
                        }
                    }

                    // Throttle frame timing to target 60FPS
                    val sleepTime = (lastTime - System.nanoTime() + optimalTime) / 1000000
                    if (sleepTime > 0) {
                        delay(sleepTime)
                    } else {
                        yield()
                    }
                } catch (e: Exception) {
                    delay(16)
                    lastTime = System.nanoTime()
                }
            }
        }
    }

    private fun stopPhysicsLoop() {
        gameJob?.cancel()
        gameJob = null
    }

    fun getMarioWidth(): Float = 22f
    fun getMarioHeight(): Float = if (marioSize.value != "SMALL") 38f else 22f

    private fun gameTick() {
        // Decrease invincibility frames
        if (invincibilityFrames.value > 0) {
            invincibilityFrames.value--
        }

        // Decrease starman timer
        if (isStarman.value) {
            starmanTimer.value--
            if (starmanTimer.value <= 0) {
                isStarman.value = false
            }
        }

        updateMarioMovement()
        updateEnemies()
        updatePowerups()
        updateFireballs()
        updateParticlesAndPopups()
    }

    private fun updateMarioMovement() {
        val width = getMarioWidth()
        val height = getMarioHeight()

        // P-Meter mechanics: if sprinting and speed is high, fill meter
        if (isSprintPressed && abs(marioVx.value) > 2.0f && isGrounded.value) {
            pMeter.value = min(100f, pMeter.value + 2.5f)
        } else {
            pMeter.value = max(0f, pMeter.value - 1.5f)
        }
        val isSuperCharged = pMeter.value >= 100f

        // Apply input forces (with boost from Starman and P-Meter) - significantly accelerated for swift action gameplay!
        val baseSpeedLimit = if (isSprintPressed) 6.4f else 4.0f
        val currentSpeedLimit = if (isStarman.value) 8.4f else if (isSuperCharged) 7.4f else baseSpeedLimit

        val accel = if (isSuperCharged || isStarman.value) 0.45f else 0.32f
        val friction = 0.90f

        if (isLeftPressed) {
            // Apply extra traction/skidding force when turning around
            val skidModifier = if (marioVx.value > 0.1f) 2.5f else 1.0f
            marioVx.value = max(marioVx.value - accel * skidModifier, -currentSpeedLimit)
            facingRight.value = false
            if (isGrounded.value) {
                val animationSpeed = if (isStarman.value) 0.35f else if (isSuperCharged) 0.28f else 0.15f
                runFrameIndex.value = (runFrameIndex.value + animationSpeed) % 3f
            }
        } else if (isRightPressed) {
            // Apply extra traction/skidding force when turning around
            val skidModifier = if (marioVx.value < -0.1f) 2.5f else 1.0f
            marioVx.value = min(marioVx.value + accel * skidModifier, currentSpeedLimit)
            facingRight.value = true
            if (isGrounded.value) {
                val animationSpeed = if (isStarman.value) 0.35f else if (isSuperCharged) 0.28f else 0.15f
                runFrameIndex.value = (runFrameIndex.value + animationSpeed) % 3f
            }
        } else {
            // High-precision momentum gliding: much less friction when in mid-air
            val activeFriction = if (isGrounded.value) friction else 0.98f
            marioVx.value *= activeFriction
            if (abs(marioVx.value) < 0.05f) {
                marioVx.value = 0f
                runFrameIndex.value = 0f
            }
        }

        // Apply variable gravity based on whether jump button is held down
        val gravityForce = if (isJumpPressed && marioVy.value < 0f) {
            if (isSuperCharged || isStarman.value) 0.26f else 0.36f
        } else {
            0.56f
        }
        marioVy.value += gravityForce
        if (marioVy.value > 14f) {
            marioVy.value = 14f // Terminal velocity fall speed
        }

        // Variable jumping height: cut jump height short if jump button released early
        if (!isJumpPressed && marioVy.value < -2.0f) {
            marioVy.value = -2.0f
        }

        // Apply Jump (with visual and physical power injection from stats)
        if (isJumpPressed && isGrounded.value) {
            val jumpImpulse = if (isSuperCharged) -12.5f else if (isStarman.value) -11.5f else -10.5f
            marioVy.value = jumpImpulse
            isGrounded.value = false
            if (isSuperCharged) {
                popups.add(ScorePopup("PMETER SPRINT!", marioX.value, marioY.value - 12f))
            }
        }

        // Apply X movement and horizontal collisions
        marioX.value += marioVx.value

        // Stay within screen bounds: cannot walk off the left of the current screen view!
        val screenMinX = cameraX.value
        if (marioX.value < screenMinX) {
            marioX.value = screenMinX
            marioVx.value = 0f
        }

        checkHorizontalCollisions(width, height)

        // Apply Y movement and vertical collisions
        marioY.value += marioVy.value
        checkVerticalCollisions(width, height)

        // Fall into pit check
        if (marioY.value > (tileRows + 1) * tileSize) {
            triggerPlayerDeath()
        }

        // Classic Super Mario scroll: only scroll right, never scroll left!
        val targetCam = marioX.value - 180f
        val maxCam = (tileCols * tileSize) - 400f
        val idealCam = max(0f, min(targetCam, maxCam))
        if (idealCam > cameraX.value) {
            cameraX.value = idealCam
        }
    }

    private fun triggerPlayerDeath() {
        _gameStatus.value = GameStatus.DEATH_ANIMATION
        marioVy.value = -8f // fly up animation
        isGrounded.value = false
        isLeftPressed = false
        isRightPressed = false
        isJumpPressed = false
        isSprintPressed = false
    }

    private fun checkHorizontalCollisions(mw: Float, mh: Float) {
        val mx = marioX.value
        val my = marioY.value

        val startCol = (mx / tileSize).toInt()
        val endCol = ((mx + mw) / tileSize).toInt()
        // Shrink row scanning slightly to prevent getting stuck on floors/ceilings
        val startRow = ((my + 2.0f) / tileSize).toInt()
        val endRow = ((my + mh - 2.0f) / tileSize).toInt()

        for (r in max(0, startRow)..min(tileRows - 1, endRow)) {
            for (c in max(0, startCol)..min(tileCols - 1, endCol)) {
                val tile = levelGrid[r][c]
                if (isSolidTile(tile)) {
                    // We have horizontal collision
                    if (marioVx.value > 0) { // moving right
                        marioX.value = c * tileSize - mw - 0.1f
                        marioVx.value = 0f
                    } else if (marioVx.value < 0) { // moving left
                        marioX.value = (c + 1) * tileSize + 0.1f
                        marioVx.value = 0f
                    }
                } else if (tile == TILE_FLAGPOLE) {
                    // Winner! Start the flag slide sequence
                    triggerVictory()
                    return
                }
            }
        }
    }

    private fun checkVerticalCollisions(mw: Float, mh: Float) {
        val mx = marioX.value
        val my = marioY.value

        // Shrink col scanning slightly to prevent getting stuck on side walls
        val startCol = ((mx + 2.0f) / tileSize).toInt()
        val endCol = ((mx + mw - 2.0f) / tileSize).toInt()
        val startRow = (my / tileSize).toInt()
        val endRow = ((my + mh + 1.5f) / tileSize).toInt()

        var groundedThisFrame = false

        for (r in max(0, startRow)..min(tileRows - 1, endRow)) {
            for (c in max(0, startCol)..min(tileCols - 1, endCol)) {
                val tile = levelGrid[r][c]
                if (isSolidTile(tile)) {
                    if (marioVy.value > 0) { // falling down
                        marioY.value = r * tileSize - mh - 0.1f
                        marioVy.value = 0f
                        groundedThisFrame = true
                    } else if (marioVy.value < 0) { // moving up / hit ceiling
                        marioY.value = (r + 1) * tileSize + 0.1f
                        marioVy.value = 0.5f // rebound
                        handleCeilingHit(r, c)
                    }
                } else if (tile == TILE_FLAGPOLE) {
                    triggerVictory()
                    return
                }
            }
        }
        isGrounded.value = groundedThisFrame
    }

    private fun handleCeilingHit(row: Int, col: Int) {
        val tile = levelGrid[row][col]
        if (tile == TILE_BRICK) {
            if (marioSize.value != "SMALL") {
                // Break Brick!
                levelGrid[row][col] = TILE_EMPTY
                score.value += 50
                // Spawn debris particles
                spawnDebris(col * tileSize + 16, row * tileSize + 16)
            } else {
                // Just bump brick (visual effect can be simple point update)
                score.value += 10
            }
        } else if (tile == TILE_QUESTION_COIN) {
            levelGrid[row][col] = TILE_HITSOLID
            coins.value++
            score.value += 200
            // Spawn pop text
            popups.add(ScorePopup("+200 COIN", col * tileSize, row * tileSize - 16f))
        } else if (tile == TILE_QUESTION_MUSHROOM) {
            levelGrid[row][col] = TILE_HITSOLID
            // Spawn mushroom sliding out
            spawnPowerup(col * tileSize, (row - 1) * tileSize, PowerupType.MUSHROOM)
            score.value += 100
        } else if (tile == TILE_QUESTION_FLOWER) {
            levelGrid[row][col] = TILE_HITSOLID
            spawnPowerup(col * tileSize, (row - 1) * tileSize, PowerupType.FLOWER)
            score.value += 150
        } else if (tile == TILE_QUESTION_STAR) {
            levelGrid[row][col] = TILE_HITSOLID
            spawnPowerup(col * tileSize, (row - 1) * tileSize, PowerupType.STAR)
            score.value += 200
            popups.add(ScorePopup("INVINCIBLE STAR!", col * tileSize, row * tileSize - 16f))
        }
    }

    private fun triggerVictory() {
        _gameStatus.value = GameStatus.VICTORY_SLIDE
        marioVx.value = 0f
        marioVy.value = 0f
        flagSlideProgress.value = 0f
        score.value += 1000 // Flag bonus!
        popups.add(ScorePopup("+1000 VICTORY", marioX.value, marioY.value - 24f))
    }

    fun fireAction() {
        if (marioSize.value == "FIRE" && fireballs.size < 3) {
            val fVx = if (facingRight.value) 5f else -5f
            val fVy = 2f
            fireballs.add(
                Fireball(
                    id = fireballIdSource++,
                    x = marioX.value + (if (facingRight.value) getMarioWidth() else -10f),
                    y = marioY.value + 10f,
                    vx = fVx,
                    vy = fVy
                )
            )
        }
        // Also triggers a speed dash if moving, handled by `currentSpeedLimit` in movement
    }

    private fun isSolidTile(tile: Int): Boolean {
        return tile == TILE_GROUND ||
                tile == TILE_BRICK ||
                tile == TILE_QUESTION_COIN ||
                tile == TILE_QUESTION_MUSHROOM ||
                tile == TILE_QUESTION_FLOWER ||
                tile == TILE_QUESTION_STAR ||
                tile == TILE_SOLID ||
                tile == TILE_PIPE_TL ||
                tile == TILE_PIPE_TR ||
                tile == TILE_PIPE_BL ||
                tile == TILE_PIPE_BR ||
                tile == TILE_CASTLE_BRICK ||
                tile == TILE_HITSOLID
    }

    private fun updateEnemies() {
        val mx = marioX.value
        val my = marioY.value
        val mw = getMarioWidth()
        val mh = getMarioHeight()

        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()

            if (enemy.isSquashed && !enemy.isShell) {
                enemy.squashedTimer++
                if (enemy.squashedTimer > 25) {
                    iterator.remove()
                }
                continue
            }

            // Normal or shell movement physics
            if (enemy.isShell) {
                enemy.vy += 0.45f
                if (enemy.vy > 8f) enemy.vy = 8f
                enemy.vx = enemy.shellSpeed
                enemy.x += enemy.vx
            } else {
                enemy.vy += 0.4f
                if (enemy.vy > 8f) enemy.vy = 8f
                enemy.x += enemy.vx
            }

            // Ground & Wall patrol collisions
            val eColStart = (enemy.x / tileSize).toInt()
            val eColEnd = ((enemy.x + enemy.width) / tileSize).toInt()
            val eRowStart = (enemy.y / tileSize).toInt()
            val eRowEnd = ((enemy.y + enemy.height + 1f) / tileSize).toInt()

            for (r in max(0, eRowStart)..min(tileRows - 1, eRowEnd)) {
                for (c in max(0, eColStart)..min(tileCols - 1, eColEnd)) {
                    val tile = levelGrid[r][c]
                    if (isSolidTile(tile)) {
                        // Horizontal deflect
                        if (enemy.vx > 0 && enemy.x + enemy.width > c * tileSize) {
                            if (enemy.isShell) {
                                enemy.shellSpeed = -enemy.shellSpeed
                                enemy.vx = enemy.shellSpeed
                            } else {
                                enemy.vx = -enemy.vx
                            }
                            enemy.x = c * tileSize - enemy.width - 0.1f
                        } else if (enemy.vx < 0 && enemy.x < (c + 1) * tileSize) {
                            if (enemy.isShell) {
                                enemy.shellSpeed = -enemy.shellSpeed
                                enemy.vx = enemy.shellSpeed
                            } else {
                                enemy.vx = -enemy.vx
                            }
                            enemy.x = (c + 1) * tileSize + 0.1f
                        }
                    }
                }
            }

            // Move Y & Vertical collisions
            enemy.y += enemy.vy
            val eyColStart = (enemy.x / tileSize).toInt()
            val eyColEnd = ((enemy.x + enemy.width) / tileSize).toInt()
            val eyRowStart = (enemy.y / tileSize).toInt()
            val eyRowEnd = ((enemy.y + enemy.height) / tileSize).toInt()

            for (r in max(0, eyRowStart)..min(tileRows - 1, eyRowEnd)) {
                for (c in max(0, eyColStart)..min(tileCols - 1, eyColEnd)) {
                    val tile = levelGrid[r][c]
                    if (isSolidTile(tile)) {
                        if (enemy.vy > 0) { // falling
                            enemy.y = r * tileSize - enemy.height - 0.1f
                            enemy.vy = 0f
                        }
                    }
                }
            }

            // Fall into pit? Remove
            if (enemy.y > tileRows * tileSize) {
                iterator.remove()
                continue
            }

            // High-speed sliding shell destroys other enemies in it's way!
            if (enemy.isShell && abs(enemy.shellSpeed) > 1.5f) {
                for (i in 0 until enemies.size) {
                    val other = enemies.getOrNull(i) ?: continue
                    if (other.id != enemy.id && !other.isSquashed) {
                        val hit = enemy.x < other.x + other.width && enemy.x + enemy.width > other.x &&
                                enemy.y < other.y + other.height && enemy.y + enemy.height > other.y
                        if (hit) {
                            other.isSquashed = true
                            other.vx = 0f
                            other.vy = 0f
                            score.value += 500
                            popups.add(ScorePopup("+500 CHAIN", other.x, other.y - 12f))
                        }
                    }
                }
            }

            // Check Collision with Mario
            val hOverlap = mx < enemy.x + enemy.width && mx + mw > enemy.x
            val vOverlap = my < enemy.y + enemy.height && my + mh > enemy.y

            if (hOverlap && vOverlap) {
                if (isStarman.value) {
                    // Starman destroys enemy instantly on contact
                    enemy.isSquashed = true
                    enemy.vx = 0f
                    enemy.vy = 0f
                    score.value += 200
                    popups.add(ScorePopup("+200 STAR!", enemy.x, enemy.y - 12f))
                } else if (marioVy.value >= -2.0f && (my + mh * 0.75f) <= enemy.y) {
                    // Stomp!
                    if (enemy.type == EnemyType.KOOPA && !enemy.isShell) {
                        // Put Koopa into Shell mode
                        enemy.isShell = true
                        enemy.shellSpeed = 0f
                        enemy.vx = 0f
                        marioVy.value = -6.5f // Bounce Mario
                        score.value += 100
                        popups.add(ScorePopup("SHELL STATE", enemy.x, enemy.y - 12f))
                    } else if (enemy.type == EnemyType.KOOPA && enemy.isShell) {
                        if (abs(enemy.shellSpeed) > 0.5f) {
                            // Stop moving shell
                            enemy.shellSpeed = 0f
                            enemy.vx = 0f
                            marioVy.value = -6f
                            popups.add(ScorePopup("SHELL STOP", enemy.x, enemy.y - 12f))
                        } else {
                            // Kick moving shell
                            val kickDir = if (mx + mw / 2f < enemy.x + enemy.width / 2f) 6.5f else -6.5f
                            enemy.shellSpeed = kickDir
                            enemy.vx = kickDir
                            marioVy.value = -6.5f
                            score.value += 200
                            popups.add(ScorePopup("+200 KICK", enemy.x, enemy.y - 12f))
                        }
                    } else {
                        // Goomba squash
                        enemy.isSquashed = true
                        enemy.vx = 0f
                        enemy.vy = 0f
                        marioVy.value = -6f // Bounce Mario
                        score.value += 100
                        popups.add(ScorePopup("+100", enemy.x, enemy.y - 12f))
                    }
                } else {
                    // Sideways contact
                    if (enemy.isShell && abs(enemy.shellSpeed) < 0.5f) {
                        // Kick stationary shell
                        val kickDir = if (mx + mw / 2f < enemy.x + enemy.width / 2f) 6.5f else -6.5f
                        enemy.shellSpeed = kickDir
                        enemy.vx = kickDir
                        score.value += 100
                        popups.add(ScorePopup("KICK!", enemy.x, enemy.y - 12f))
                        // Brief shield to prevent self-damage instant frame collision
                        invincibilityFrames.value = 25
                    } else {
                        // Takes Damage!
                        if (invincibilityFrames.value == 0) {
                            if (marioSize.value == "FIRE") {
                                marioSize.value = "BIG"
                                invincibilityFrames.value = 60 // 1 second flash
                            } else if (marioSize.value == "BIG") {
                                marioSize.value = "SMALL"
                                invincibilityFrames.value = 60
                            } else {
                                // Dead
                                triggerPlayerDeath()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updatePowerups() {
        val mx = marioX.value
        val my = marioY.value
        val mw = getMarioWidth()
        val mh = getMarioHeight()

        val iterator = powerups.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()

            // Mushrooms move, flowers stay stationary
            if (item.type == PowerupType.MUSHROOM) {
                item.vy += 0.4f
                if (item.vy > 8f) item.vy = 8f

                item.x += item.vx
                val colStart = (item.x / tileSize).toInt()
                val colEnd = ((item.x + item.width) / tileSize).toInt()
                val rowStart = (item.y / tileSize).toInt()
                val rowEnd = ((item.y + item.height) / tileSize).toInt()

                // Deflect side blocks
                for (r in max(0, rowStart)..min(tileRows - 1, rowEnd)) {
                    for (c in max(0, colStart)..min(tileCols - 1, colEnd)) {
                        val tile = levelGrid[r][c]
                        if (isSolidTile(tile)) {
                            if (item.vx > 0 && item.x + item.width > c * tileSize) {
                                item.vx = -item.vx
                                item.x = c * tileSize - item.width - 0.1f
                            } else if (item.vx < 0 && item.x < (c + 1) * tileSize) {
                                item.vx = -item.vx
                                item.x = (c + 1) * tileSize + 0.1f
                            }
                        }
                    }
                }

                item.y += item.vy
                val ycolStart = (item.x / tileSize).toInt()
                val ycolEnd = ((item.x + item.width) / tileSize).toInt()
                val yrowStart = (item.y / tileSize).toInt()
                val yrowEnd = ((item.y + item.height) / tileSize).toInt()

                for (r in max(0, yrowStart)..min(tileRows - 1, yrowEnd)) {
                    for (c in max(0, ycolStart)..min(tileCols - 1, ycolEnd)) {
                        val tile = levelGrid[r][c]
                        if (isSolidTile(tile)) {
                            if (item.vy > 0) {
                                item.y = r * tileSize - item.height - 0.1f
                                item.vy = 0f
                            }
                        }
                    }
                }
            }

            // Remove if off bounds
            if (item.y > tileRows * tileSize) {
                iterator.remove()
                continue
            }

            // Hit Mario?
            val hOverlap = mx < item.x + item.width && mx + mw > item.x
            val vOverlap = my < item.y + item.height && my + mh > item.y

            if (hOverlap && vOverlap) {
                // Collect powerup!
                if (item.type == PowerupType.MUSHROOM) {
                    if (marioSize.value == "SMALL") {
                        marioSize.value = "BIG"
                    }
                    score.value += 1000
                    popups.add(ScorePopup("+1000 POWERUP", item.x, item.y - 16f))
                } else if (item.type == PowerupType.FLOWER) {
                    marioSize.value = "FIRE"
                    score.value += 1000
                    popups.add(ScorePopup("+1000 FIREBALL", item.x, item.y - 16f))
                }
                iterator.remove()
            }
        }
    }

    private fun updateFireballs() {
        val iterator = fireballs.iterator()
        while (iterator.hasNext()) {
            val fb = iterator.next()

            fb.vy += 0.45f
            fb.x += fb.vx
            fb.y += fb.vy

            // Grid collision
            val col = (fb.x / tileSize).toInt()
            val row = (fb.y / tileSize).toInt()

            if (row < 0 || row >= tileRows || col < 0 || col >= tileCols) {
                iterator.remove()
                continue
            }

            val tile = levelGrid[row][col]
            if (isSolidTile(tile)) {
                // Bounce UP!
                fb.y = row * tileSize - 10f
                fb.vy = -4.5f
                fb.bounces++
                if (fb.bounces > 4) {
                    iterator.remove()
                    continue
                }
            }

            // Hit enemies?
            var hitEnemy = false
            val enemyIter = enemies.iterator()
            while (enemyIter.hasNext()) {
                val enemy = enemyIter.next()
                if (!enemy.isSquashed) {
                    val hOverlap = fb.x < enemy.x + enemy.width && fb.x + 8f > enemy.x
                    val vOverlap = fb.y < enemy.y + enemy.height && fb.y + 8f > enemy.y
                    if (hOverlap && vOverlap) {
                        // Hit Goomba!
                        enemy.isSquashed = true
                        enemy.vx = 0f
                        enemy.vy = 0f
                        score.value += 200
                        popups.add(ScorePopup("+200 FIRE", enemy.x, enemy.y - 10f))
                        hitEnemy = true
                        break
                    }
                }
            }

            if (hitEnemy) {
                iterator.remove()
            }
        }
    }

    private fun updateParticlesAndPopups() {
        // Popups fade
        val pIter = popups.iterator()
        while (pIter.hasNext()) {
            val pop = pIter.next()
            pop.y -= 0.6f
            pop.timer--
            pop.alpha = pop.timer / 30f
            if (pop.timer <= 0) {
                pIter.remove()
            }
        }

        // Particles fall
        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val part = partIter.next()
            part.vy += 0.38f
            part.x += part.vx
            part.y += part.vy
            part.rotation += part.vx * 2f
            part.timer--
            if (part.timer <= 0) {
                partIter.remove()
            }
        }
    }

    private fun spawnPowerup(xPx: Float, yPx: Float, type: PowerupType) {
        val vxMod = if (facingRight.value) 1.5f else -1.5f
        powerups.add(
            Powerup(
                id = powerupIdSource++,
                x = xPx,
                y = yPx,
                vx = vxMod,
                vy = -3f,
                type = type
            )
        )
    }

    private fun spawnDebris(xPx: Float, yPx: Float) {
        // Break brick debris particle effect (4 shards traveling outwards)
        particles.add(Particle(xPx, yPx, -2.5f, -6f))
        particles.add(Particle(xPx, yPx, 2.5f, -6f))
        particles.add(Particle(xPx, yPx, -1.5f, -3f))
        particles.add(Particle(xPx, yPx, 1.5f, -3f))
    }

    private fun spawnEnemiesForWorld(worldNum: Int) {
        enemies.clear()
        enemyIdSource = 1
        when (worldNum) {
            1 -> {
                // World 1-1 simple Goomba and Koopa placements
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 320f, 250f, -1f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 540f, 250f, -1.1f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 800f, 250f, -1f))
                enemies.add(enemyFactory.createKoopa(enemyIdSource++, 1150f, 240f, -0.9f)) // Koopa Troopa!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 1500f, 250f, -1f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 2000f, 250f, -1.1f))
                enemies.add(enemyFactory.createKoopa(enemyIdSource++, 2300f, 240f, -0.9f)) // Another Koopa!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 2600f, 250f, -1.2f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 3100f, 250f, -1f))
            }
            2 -> {
                // World 1-2 Goombas & faster Koopas
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 280f, 250f, -1.4f))
                enemies.add(enemyFactory.createKoopa(enemyIdSource++, 460f, 240f, -1.2f))  // Koopa Troopa!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 780f, 250f, -1.4f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 1200f, 250f, -1.5f))
                enemies.add(enemyFactory.createKoopa(enemyIdSource++, 1450f, 240f, -1.3f)) // Fast Koopa!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 1950f, 250f, -1.4f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 2400f, 250f, -1.5f))
                enemies.add(enemyFactory.createKoopa(enemyIdSource++, 2700f, 240f, -1.2f))  // Koopa!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 2900f, 250f, -1.4f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 3300f, 250f, -1.5f))
            }
            3 -> {
                // World 1-3 Fiery castle gate! Extreme Goombas and heavily armored Koopas!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 300f, 150f, -1.6f))
                enemies.add(enemyFactory.createKoopa(enemyIdSource++, 500f, 140f, -1.4f))   // Castle Koopa!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 900f, 150f, -1.6f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 1300f, 150f, -1.8f))
                enemies.add(enemyFactory.createKoopa(enemyIdSource++, 1500f, 140f, -1.5f))  // Koopa on platform!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 1700f, 150f, -1.7f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 2100f, 150f, -1.8f))
                enemies.add(enemyFactory.createKoopa(enemyIdSource++, 2300f, 140f, -1.6f))  // Patrol Koopa!
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 2500f, 150f, -1.9f))
                enemies.add(enemyFactory.createGoomba(enemyIdSource++, 3000f, 150f, -1.7f))
            }
        }
    }

    private fun generateLevel(worldNum: Int) {
        // Clear entire grid
        for (r in 0 until tileRows) {
            for (c in 0 until tileCols) {
                levelGrid[r][c] = TILE_EMPTY
            }
        }

        // Fill ground level for the bottom rows (rows 12 and 13)
        for (c in 0 until tileCols) {
            levelGrid[12][c] = TILE_GROUND
            levelGrid[13][c] = TILE_GROUND
        }

        // Apply environment customization bases
        when (worldNum) {
            1 -> {
                // World 1-1 Grassland Heights Pits
                // Pits at col 40-42, and col 95-97
                for (c in 40..42) {
                    levelGrid[12][c] = TILE_EMPTY
                    levelGrid[13][c] = TILE_EMPTY
                }
                for (c in 95..97) {
                    levelGrid[12][c] = TILE_EMPTY
                    levelGrid[13][c] = TILE_EMPTY
                }
                for (c in 140..142) {
                    levelGrid[12][c] = TILE_EMPTY
                    levelGrid[13][c] = TILE_EMPTY
                }

                // Add pipes
                addPipe(24, 4) // row height 4
                addPipe(48, 5) // row height 5
                addPipe(72, 6) // row height 6
                addPipe(110, 4)

                // Bricks & Mystery Blocks
                // Basic brick combo at row 8 (height level)
                levelGrid[8][15] = TILE_BRICK
                levelGrid[8][16] = TILE_QUESTION_COIN
                levelGrid[8][17] = TILE_BRICK
                levelGrid[8][18] = TILE_QUESTION_MUSHROOM
                levelGrid[8][19] = TILE_BRICK

                // Higher combo
                levelGrid[4][17] = TILE_QUESTION_FLOWER
                levelGrid[4][18] = TILE_COIN
                levelGrid[4][19] = TILE_COIN

                // Brick combo 2
                for (col in 55..62) {
                    levelGrid[8][col] = if (col % 2 == 0) TILE_QUESTION_COIN else TILE_BRICK
                }
                // Star Reward block high up!
                levelGrid[4][60] = TILE_QUESTION_STAR

                // Pyramid staircase of solid blocks before flagpole
                for (h in 1..4) {
                    for (c in (165 + h)..169) {
                        levelGrid[12 - h][c] = TILE_SOLID
                    }
                }

                // Flagpole column at col 185
                for (r in 2..11) {
                    levelGrid[r][185] = TILE_FLAGPOLE
                }
                levelGrid[1][185] = TILE_FLAG_TOP

                // Castle at the end col 192 to 198
                for (r in 7..11) {
                    for (c in 192..198) {
                        levelGrid[r][c] = TILE_CASTLE_BRICK
                    }
                }
                // Castle battlements
                for (c in 192..198 step 2) {
                    levelGrid[6][c] = TILE_CASTLE_BRICK
                }
                // Castle door
                levelGrid[10][195] = TILE_CASTLE_DOOR
                levelGrid[11][195] = TILE_CASTLE_DOOR

                // Fill coins scattering layout
                for (col in listOf(30, 31, 32, 75, 76, 77, 130, 131)) {
                    levelGrid[9][col] = TILE_COIN
                }
            }
            2 -> {
                // World 1-2 Underworld pits & platforms (more challenging)
                // Bottomless pits
                for (c in listOf(32, 33, 34, 65, 66, 67, 100, 101, 102, 135, 136, 137, 170, 171)) {
                    levelGrid[12][c] = TILE_EMPTY
                    levelGrid[13][c] = TILE_EMPTY
                }

                // Add pipe obstacles
                addPipe(20, 5)
                addPipe(50, 6)
                addPipe(80, 5)
                addPipe(120, 6)

                // Ceiling bricks (mimic underworld layout)
                for (c in 10..150) {
                    levelGrid[1][c] = TILE_BRICK
                    if (c % 15 == 0) levelGrid[2][c] = TILE_QUESTION_COIN
                }

                // Block bridges over pits with premium Star and Mushroom rewards!
                levelGrid[8][32] = TILE_BRICK
                levelGrid[8][33] = TILE_QUESTION_STAR
                levelGrid[8][34] = TILE_BRICK

                levelGrid[8][100] = TILE_SOLID
                levelGrid[8][101] = TILE_QUESTION_MUSHROOM
                levelGrid[8][102] = TILE_SOLID

                // Coins floating
                for (c in listOf(25, 26, 42, 43, 60, 61, 88, 89, 112, 113, 145, 146)) {
                    levelGrid[9][c] = TILE_COIN
                    levelGrid[5][c] = TILE_COIN
                }

                // Final stretch staircase
                for (h in 1..5) {
                    for (c in (163 + h)..168) {
                        levelGrid[12 - h][c] = TILE_SOLID
                    }
                }

                // Flagpole
                for (r in 2..11) {
                    levelGrid[r][182] = TILE_FLAGPOLE
                }
                levelGrid[1][182] = TILE_FLAG_TOP

                // Castle End
                for (r in 7..11) {
                    for (c in 189..195) {
                        levelGrid[r][c] = TILE_CASTLE_BRICK
                    }
                }
                levelGrid[10][192] = TILE_CASTLE_DOOR
                levelGrid[11][192] = TILE_CASTLE_DOOR
            }
            3 -> {
                // World 1-3 Bowser's Castle Fireworld Theme
                // Optimized, continuous, and fully navigable lava pits (never exceeding comfortable 3-tile gaps)
                val pits = (15..20) + (42..47) + (75..80) + (110..115) + (143..148)
                for (c in 0 until tileCols) {
                    if (c in pits) {
                        levelGrid[12][c] = TILE_EMPTY
                        levelGrid[13][c] = TILE_EMPTY
                    }
                }

                // Comfortable single platform bricks over first pit (15..20)
                for (c in 17..19) levelGrid[8][c] = TILE_BRICK
                levelGrid[8][18] = TILE_QUESTION_MUSHROOM

                // Stepping platforms over second pit (42..47)
                for (c in 44..45) levelGrid[7][c] = TILE_SOLID
                levelGrid[6][45] = TILE_COIN

                // Stepping stones over third pit (75..80) with a premium GLOWING STAR block in the middle!
                levelGrid[8][76] = TILE_SOLID
                levelGrid[6][78] = TILE_QUESTION_STAR // Starman bypass logic!
                levelGrid[8][79] = TILE_SOLID

                // Stepping stones over fourth pit (110..115)
                for (c in 111..114 step 2) {
                    levelGrid[8][c] = TILE_BRICK
                    levelGrid[5][c + 1] = TILE_COIN
                }

                // Stepping stones for final pit (143..148)
                for (c in 144..147 step 2) {
                    levelGrid[8][c] = TILE_SOLID
                    levelGrid[5][c] = TILE_COIN
                }

                // Castle style brick rows
                for (c in 0..110) {
                    if (c % 12 == 0) {
                        levelGrid[3][c] = TILE_BRICK
                        levelGrid[3][c + 1] = TILE_QUESTION_COIN
                        levelGrid[3][c + 2] = TILE_BRICK
                    }
                }

                // Staircase before finish line
                for (h in 1..6) {
                    for (c in (160 + h)..166) {
                        levelGrid[12 - h][c] = TILE_SOLID
                    }
                }

                // Flagpole at 178
                for (r in 2..11) {
                    levelGrid[r][178] = TILE_FLAGPOLE
                }
                levelGrid[1][178] = TILE_FLAG_TOP

                // Massive Final Castle col 186 to 196
                for (r in 6..11) {
                    for (c in 184..194) {
                        levelGrid[r][c] = TILE_CASTLE_BRICK
                    }
                }
                levelGrid[10][189] = TILE_CASTLE_DOOR
                levelGrid[11][189] = TILE_CASTLE_DOOR
            }
        }
    }

    private fun addPipe(col: Int, height: Int) {
        val baseRow = 12
        val pipeTopRow = baseRow - height

        levelGrid[pipeTopRow][col] = TILE_PIPE_TL
        levelGrid[pipeTopRow][col + 1] = TILE_PIPE_TR

        for (r in (pipeTopRow + 1) until baseRow) {
            levelGrid[r][col] = TILE_PIPE_BL
            levelGrid[r][col + 1] = TILE_PIPE_BR
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPhysicsLoop()
    }
}
