package com.example.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var botMoveJob: Job? = null
    private var matchmakingJob: Job? = null
    private var introAnimationJob: Job? = null

    // Names for bot players to make matchmaking feel extremely real!
    private val botNamesVi = listOf("BánhNgọt99", "KẹoDẻoMút", "SữaChuaDâu", "KemBơBéo", "SuKemSữa", "GấuBôngCandy", "ThỏPastel", "CookieNướng")
    private val botNamesEn = listOf("SweetTooth", "CandyCush", "ChocoChip", "BerryGlaze", "CreamPuff", "HoneyBun", "SugarPlum", "Marshmallow")

    private val prefs = application.getSharedPreferences("ludo_prefs", Context.MODE_PRIVATE)

    init {
        // Play click on start
        LudoSoundSynth.playClick()
        loadPersistentState()
    }

    private fun loadPersistentState() {
        val gold = prefs.getInt("user_gold", 3000)
        val diamonds = prefs.getInt("user_diamonds", 10)
        val lastClaim = prefs.getLong("last_claimed_timestamp", 0L)
        val selectedChar = prefs.getString("selected_character", "char1") ?: "char1"
        val unlockedCharsString = prefs.getString("unlocked_characters", "char1") ?: "char1"
        val unlockedCharsSet = unlockedCharsString.split(",").filter { it.isNotEmpty() }.toSet()
        val savedName = prefs.getString("player_name", "") ?: ""

        _uiState.update {
            it.copy(
                userGold = gold,
                userDiamonds = diamonds,
                lastClaimedRewardTimestamp = lastClaim,
                selectedCharacter = selectedChar,
                unlockedCharacters = unlockedCharsSet,
                playerName = savedName
            )
        }
    }

    private fun savePersistentState() {
        val state = _uiState.value
        prefs.edit()
            .putInt("user_gold", state.userGold)
            .putInt("user_diamonds", state.userDiamonds)
            .putLong("last_claimed_timestamp", state.lastClaimedRewardTimestamp)
            .putString("selected_character", state.selectedCharacter)
            .putString("unlocked_characters", state.unlockedCharacters.joinToString(","))
            .putString("player_name", state.playerName)
            .apply()
    }

    fun updatePlayerName(newName: String) {
        _uiState.update {
            it.copy(playerName = newName)
        }
        savePersistentState()
    }

    fun canClaimDailyReward(): Boolean {
        val lastClaimTime = _uiState.value.lastClaimedRewardTimestamp
        if (lastClaimTime == 0L) return true
        
        val now = java.util.Calendar.getInstance()
        val last = java.util.Calendar.getInstance().apply { timeInMillis = lastClaimTime }
        
        val nowYear = now.get(java.util.Calendar.YEAR)
        val nowMonth = now.get(java.util.Calendar.MONTH)
        val nowDay = now.get(java.util.Calendar.DAY_OF_MONTH)
        
        val lastYear = last.get(java.util.Calendar.YEAR)
        val lastMonth = last.get(java.util.Calendar.MONTH)
        val lastDay = last.get(java.util.Calendar.DAY_OF_MONTH)
        
        return nowYear != lastYear || nowMonth != lastMonth || nowDay != lastDay
    }

    fun claimDailyReward(): Boolean {
        if (!canClaimDailyReward()) return false
        
        val newGold = _uiState.value.userGold + 100
        val newDiamonds = _uiState.value.userDiamonds + 5
        val nowTime = System.currentTimeMillis()
        
        _uiState.update {
            it.copy(
                userGold = newGold,
                userDiamonds = newDiamonds,
                lastClaimedRewardTimestamp = nowTime
            )
        }
        savePersistentState()
        return true
    }

    fun purchaseCharacter(charId: String, price: Int): Boolean {
        val state = _uiState.value
        if (state.unlockedCharacters.contains(charId)) return false
        if (state.userGold < price) return false

        _uiState.update {
            it.copy(
                userGold = it.userGold - price,
                unlockedCharacters = it.unlockedCharacters + charId
            )
        }
        savePersistentState()
        return true
    }

    fun selectCharacter(charId: String) {
        val state = _uiState.value
        if (state.unlockedCharacters.contains(charId)) {
            _uiState.update {
                it.copy(selectedCharacter = charId)
            }
            savePersistentState()
        }
    }

    fun toggleMusic() {
        val next = !_uiState.value.isMusicOn
        _uiState.update { it.copy(isMusicOn = next) }
        LudoSoundSynth.isMusicEnabled = next
        if (next) {
            LudoSoundSynth.startMusic(_uiState.value.status == GameStateStatus.MAIN_MENU)
        } else {
            LudoSoundSynth.stopMusic()
        }
    }

    fun toggleSfx() {
        val next = !_uiState.value.isSfxOn
        _uiState.update { it.copy(isSfxOn = next) }
        LudoSoundSynth.isSfxEnabled = next
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(language = lang) }
    }

    fun startNewGame(mode: GameMode, context: Context? = null, customNames: List<String>? = null) {
        // Cancel existing jobs
        timerJob?.cancel()
        botMoveJob?.cancel()

        _uiState.update {
            it.copy(
                mode = mode,
                status = if (mode == GameMode.ONLINE) GameStateStatus.MATCHMAKING else GameStateStatus.INTRO_CAMERA,
                bannerText = if (mode == GameMode.ONLINE) "Đang tìm đối thủ..." else "SWEETY LUDO",
                consecutiveSixes = 0,
                activePlayerIndex = 0,
                diceValue = 1,
                isDiceRolling = false,
                selectedPawnId = null,
                showProfileStatsPlayer = null,
                activeEmotes = emptyList(),
                logs = listOf("Trận đấu mới bắt đầu!")
            )
        }

        if (mode == GameMode.ONLINE) {
            simulateMatchmaking()
        } else {
            setupPlayers(mode, customNames)
            startIntroAnimation()
        }
    }

    private fun simulateMatchmaking() {
        matchmakingJob = viewModelScope.launch {
            delay(1500) // Simulate finding players
            if (_uiState.value.status != GameStateStatus.MATCHMAKING) return@launch
            val isVi = _uiState.value.language == "vi"
            val names = if (isVi) botNamesVi.shuffled() else botNamesEn.shuffled()
            
            val humanSkin = _uiState.value.selectedCharacter
            val displayName = if (_uiState.value.playerName.isNotEmpty()) _uiState.value.playerName else (if (isVi) "Bạn" else "You")
            val human = LudoPlayer(
                color = PlayerColor.RED,
                name = displayName,
                isBot = false,
                avatarId = 0,
                totalGames = 145,
                wins = 78,
                winRate = 53,
                gold = 12000,
                diamonds = 120,
                characterSkin = humanSkin
            )

            val bot1 = LudoPlayer(
                color = PlayerColor.GREEN,
                name = names[0],
                isBot = true,
                avatarId = Random.nextInt(1, 6),
                totalGames = Random.nextInt(50, 200),
                wins = Random.nextInt(25, 100),
                winRate = Random.nextInt(40, 60),
                gold = Random.nextInt(1000, 15000),
                diamonds = Random.nextInt(10, 150),
                characterSkin = "char${Random.nextInt(1, 9)}"
            )

            val bot2 = LudoPlayer(
                color = PlayerColor.YELLOW,
                name = names[1],
                isBot = true,
                avatarId = Random.nextInt(1, 6),
                totalGames = Random.nextInt(50, 200),
                wins = Random.nextInt(25, 100),
                winRate = Random.nextInt(40, 60),
                gold = Random.nextInt(1000, 15000),
                diamonds = Random.nextInt(10, 150),
                characterSkin = "char${Random.nextInt(1, 9)}"
            )

            val bot3 = LudoPlayer(
                color = PlayerColor.BLUE,
                name = names[2],
                isBot = true,
                avatarId = Random.nextInt(1, 6),
                totalGames = Random.nextInt(50, 200),
                wins = Random.nextInt(25, 100),
                winRate = Random.nextInt(40, 60),
                gold = Random.nextInt(1000, 15000),
                diamonds = Random.nextInt(10, 150),
                characterSkin = "char${Random.nextInt(1, 9)}"
            )

            _uiState.update {
                it.copy(
                    players = listOf(human, bot1, bot2, bot3),
                    pawns = createInitialPawns(listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE))
                )
            }

            delay(1000)
            if (_uiState.value.status != GameStateStatus.MATCHMAKING) return@launch
            _uiState.update { it.copy(status = GameStateStatus.INTRO_CAMERA) }
            startIntroAnimation()
        }
    }

    private fun setupPlayers(mode: GameMode, customNames: List<String>? = null) {
        val isVi = _uiState.value.language == "vi"
        val names = if (isVi) botNamesVi.shuffled() else botNamesEn.shuffled()

        val playersList = mutableListOf<LudoPlayer>()
        val colors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)

        when (mode) {
            GameMode.WITH_FRIENDS -> {
                // 4 Local Human Players
                val name1 = if (!customNames.isNullOrEmpty() && customNames.getOrNull(0)?.isNotBlank() == true) customNames[0] else (if (_uiState.value.playerName.isNotEmpty()) _uiState.value.playerName else "Player 1 (Đỏ)")
                val name2 = if (!customNames.isNullOrEmpty() && customNames.getOrNull(1)?.isNotBlank() == true) customNames[1] else "Player 2 (Lá)"
                val name3 = if (!customNames.isNullOrEmpty() && customNames.getOrNull(2)?.isNotBlank() == true) customNames[2] else "Player 3 (Vàng)"
                val name4 = if (!customNames.isNullOrEmpty() && customNames.getOrNull(3)?.isNotBlank() == true) customNames[3] else "Player 4 (Dương)"

                playersList.add(LudoPlayer(PlayerColor.RED, name1, false, 0, characterSkin = _uiState.value.selectedCharacter))
                playersList.add(LudoPlayer(PlayerColor.GREEN, name2, false, 1, characterSkin = "char2"))
                playersList.add(LudoPlayer(PlayerColor.YELLOW, name3, false, 2, characterSkin = "char3"))
                playersList.add(LudoPlayer(PlayerColor.BLUE, name4, false, 3, characterSkin = "char4"))
            }
            GameMode.OFFLINE, GameMode.ARENA -> {
                // Red is Human, others are Bots
                val displayName = if (_uiState.value.playerName.isNotEmpty()) _uiState.value.playerName else (if (isVi) "Bạn" else "You")
                playersList.add(LudoPlayer(PlayerColor.RED, displayName, false, 0, gold = if (mode == GameMode.ARENA) 8000 else 2500, characterSkin = _uiState.value.selectedCharacter))
                playersList.add(LudoPlayer(PlayerColor.GREEN, names[0], true, Random.nextInt(1, 6), characterSkin = "char${Random.nextInt(1, 9)}"))
                playersList.add(LudoPlayer(PlayerColor.YELLOW, names[1], true, Random.nextInt(1, 6), characterSkin = "char${Random.nextInt(1, 9)}"))
                playersList.add(LudoPlayer(PlayerColor.BLUE, names[2], true, Random.nextInt(1, 6), characterSkin = "char${Random.nextInt(1, 9)}"))
            }
            else -> {}
        }

        _uiState.update {
            it.copy(
                players = playersList,
                pawns = createInitialPawns(colors)
            )
        }
    }

    private fun createInitialPawns(colors: List<PlayerColor>): List<Pawn> {
        val list = mutableListOf<Pawn>()
        for (color in colors) {
            for (id in 0..3) {
                list.add(Pawn(id = id, color = color, stepCount = -1)) // All in Base
            }
        }
        return list
    }

    private fun startIntroAnimation() {
        introAnimationJob = viewModelScope.launch {
            _uiState.update { it.copy(bannerText = "SWEETY LUDO") }
            delay(1800) // Give time for isometric zoom transition
            if (_uiState.value.status != GameStateStatus.INTRO_CAMERA) return@launch
            _uiState.update { it.copy(status = GameStateStatus.WAITING_FOR_ROLL) }
            startTurn()
        }
    }

    private fun startTurn() {
        val state = _uiState.value
        if (state.status == GameStateStatus.MAIN_MENU) return
        val activePlayer = state.players[state.activePlayerIndex]
        val isVi = state.language == "vi"
        
        val bannerMsg = if (state.mode == GameMode.WITH_FRIENDS) {
            if (isVi) "Đến lượt ${activePlayer.name}" else "${activePlayer.name}'s turn"
        } else if (activePlayer.isBot) {
            if (isVi) "Đến lượt ${activePlayer.name}" else "${activePlayer.name}'s turn"
        } else {
            if (isVi) "ĐẾN LƯỢT BẠN!" else "YOUR TURN!"
        }

        _uiState.update {
            it.copy(
                status = GameStateStatus.WAITING_FOR_ROLL,
                bannerText = bannerMsg,
                turnTimeLeft = 5,
                selectedPawnId = null
            )
        }

        if (activePlayer.isBot) {
            // Turn off sound effects when it is another player's (bot/opponent) turn to go
        } else {
            // Only play turn-alert sound if it is the local human user (PlayerColor.RED) or when playing locally with friends on the same device
            if (state.mode == GameMode.WITH_FRIENDS || activePlayer.color == PlayerColor.RED) {
                LudoSoundSynth.playYourTurnAlert()
            }
        }
        startTimer()

        // If it's a bot, trigger rolling after a short artistic delay
        if (activePlayer.isBot) {
            triggerBotRoll()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value.turnTimeLeft
                if (current > 0) {
                    _uiState.update { it.copy(turnTimeLeft = current - 1) }
                } else {
                    // Time out!
                    handleTimeout()
                    break
                }
            }
        }
    }

    private fun handleTimeout() {
        viewModelScope.launch {
            val state = _uiState.value
            val activePlayer = state.players[state.activePlayerIndex]
            if (activePlayer.isBot) {
                // If bot timed out (unlikely, but safe), just switch turn
                switchTurn()
            } else {
                // If human player timed out, automatically roll and auto-move or switch turn
                if (state.status == GameStateStatus.WAITING_FOR_ROLL) {
                    rollDice()
                } else if (state.status == GameStateStatus.WAITING_FOR_MOVE) {
                    val legalPawns = getLegalMoves(activePlayer.color, state.diceValue)
                    if (legalPawns.isNotEmpty()) {
                        movePawn(legalPawns.random().id)
                    } else {
                        switchTurn()
                    }
                }
            }
        }
    }

    fun rollDice() {
        val state = _uiState.value
        if (state.status == GameStateStatus.MAIN_MENU) return
        if (state.isDiceRolling || state.status != GameStateStatus.WAITING_FOR_ROLL) return

        timerJob?.cancel() // Stop timer during dice roll
        _uiState.update {
            it.copy(
                status = GameStateStatus.ROLLING_DICE,
                isDiceRolling = true
            )
        }

        LudoSoundSynth.playDiceRoll()

        viewModelScope.launch {
            // Animate rolling values
            for (i in 1..8) {
                if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                val tempVal = Random.nextInt(1, 7)
                _uiState.update { it.copy(diceValue = tempVal) }
                delay(100)
            }

            if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
            // Final value
            val finalDice = Random.nextInt(1, 7)
            
            _uiState.update {
                it.copy(
                    diceValue = finalDice,
                    isDiceRolling = false
                )
            }

            evaluateRollResult(finalDice)
        }
    }

    private fun evaluateRollResult(dice: Int) {
        val state = _uiState.value
        if (state.status == GameStateStatus.MAIN_MENU) return
        val activePlayer = state.players[state.activePlayerIndex]
        val isVi = state.language == "vi"

        // Handle consecutive 6s rule!
        if (dice == 6) {
            val nextConsecutive = state.consecutiveSixes + 1
            if (nextConsecutive == 3) {
                // Rolled 6 three times! Turn voided.
                _uiState.update {
                    it.copy(
                        consecutiveSixes = 0,
                        bannerText = if (isVi) "Mất lượt (3 lần đổ 6)!" else "Turn Voided (3 Sixes!)",
                        logs = state.logs + "${activePlayer.name} đổ 3 lần số 6 liên tiếp!"
                    )
                }
                LudoSoundSynth.playBumpScream()
                viewModelScope.launch {
                    delay(1800)
                    if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                    switchTurn()
                }
                return
            } else {
                _uiState.update { it.copy(consecutiveSixes = nextConsecutive) }
            }
        } else {
            // Reset consecutive 6s on non-6 roll
            _uiState.update { it.copy(consecutiveSixes = 0) }
        }

        // Get list of legal moves
        val legalPawns = getLegalMoves(activePlayer.color, dice)

        if (legalPawns.isEmpty()) {
            _uiState.update {
                it.copy(
                    bannerText = if (isVi) "Không có nước đi hợp lệ!" else "No valid moves!",
                    logs = state.logs + "${activePlayer.name} đổ $dice nhưng không thể di chuyển."
                )
            }
            viewModelScope.launch {
                delay(1500)
                if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                switchTurn()
            }
        } else {
            _uiState.update {
                it.copy(
                    status = GameStateStatus.WAITING_FOR_MOVE,
                    bannerText = if (activePlayer.isBot) "" else (if (isVi) "CHỌN QUÂN CỜ ĐỂ ĐI!" else "TAP A PAWN TO MOVE!"),
                    logs = state.logs + "${activePlayer.name} đổ được $dice."
                )
            }

            // Intelligent Auto-Move:
            // "If a player has only one legal, valid move available after rolling, the game must automatically move that pawn to ensure fast-paced gameplay."
            if (legalPawns.size == 1) {
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(bannerText = if (isVi) "Tự động di chuyển..." else "Auto moving...")
                    }
                    delay(1000)
                    if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                    movePawn(legalPawns.first().id)
                }
            } else {
                _uiState.update { it.copy(turnTimeLeft = 15) }
                startTimer() // Restart timer for move selection
                if (activePlayer.isBot) {
                    triggerBotMove(legalPawns)
                }
            }
        }
    }

    private fun getLegalMoves(color: PlayerColor, dice: Int): List<Pawn> {
        val state = _uiState.value
        val playerPawns = state.pawns.filter { it.color == color }
        val legalList = mutableListOf<Pawn>()

        for (pawn in playerPawns) {
            if (pawn.stepCount == -1) {
                // To deploy from Base, must roll exactly 6
                if (dice == 6) {
                    // Check if starting tile has double block of ANOTHER color
                    val startCoord = LudoBoardConfig.outerTrack[LudoBoardConfig.playerStartTrackIndex[color]!!]
                    if (!isOpponentDoubleBlockedAt(startCoord, color)) {
                        legalList.add(pawn)
                    }
                }
            } else if (pawn.stepCount < 56) {
                // Must enter center home (56) with exact roll
                val targetStep = pawn.stepCount + dice
                if (targetStep <= 56) {
                    // Check if path is blocked by opponent double blocks (standard Ludo rules: double block prevents passing/landing)
                    if (!isPathBlockedByDoubleBlock(color, pawn.stepCount, targetStep)) {
                        legalList.add(pawn)
                    }
                }
            }
        }
        return legalList
    }

    private fun isPathBlockedByDoubleBlock(color: PlayerColor, startStep: Int, targetStep: Int): Boolean {
        // Evaluate each tile along the path
        for (step in (startStep + 1)..targetStep) {
            val coord = getCoordinateForStep(color, step) ?: continue
            if (isOpponentDoubleBlockedAt(coord, color)) {
                return true
            }
        }
        return false
    }

    private fun isOpponentDoubleBlockedAt(coord: Pair<Int, Int>, myColor: PlayerColor): Boolean {
        // Filter pawns on this tile
        val pawnsOnTile = _uiState.value.pawns.filter {
            it.stepCount != -1 && getCoordinateForStep(it.color, it.stepCount) == coord
        }
        if (pawnsOnTile.size >= 2) {
            val blockColor = pawnsOnTile.first().color
            if (blockColor != myColor) {
                return true
            }
        }
        return false
    }

    private fun getCoordinateForStep(color: PlayerColor, step: Int): Pair<Int, Int>? {
        if (step == -1) return null
        if (step == 56) return LudoBoardConfig.centerHomes[color]
        if (step in 0..50) {
            val startIndex = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
            val idx = (startIndex + step) % 52
            return LudoBoardConfig.outerTrack[idx]
        }
        if (step in 51..55) {
            val path = LudoBoardConfig.homePaths[color] ?: return null
            return path[step - 51]
        }
        return null
    }

    fun movePawn(pawnId: Int) {
        val state = _uiState.value
        if (state.status != GameStateStatus.WAITING_FOR_MOVE) return

        val activePlayer = state.players[state.activePlayerIndex]
        val dice = state.diceValue
        val targetPawn = state.pawns.find { it.color == activePlayer.color && it.id == pawnId } ?: return

        timerJob?.cancel() // Cancel timer during movement

        _uiState.update {
            it.copy(
                status = GameStateStatus.MOVING_PAWN,
                selectedPawnId = pawnId
            )
        }

        viewModelScope.launch {
            val isDeployment = targetPawn.stepCount == -1
            val stepsToTake = if (isDeployment) 1 else dice
            var currentStep = targetPawn.stepCount

            // Smooth stepwise animation
            for (step in 1..stepsToTake) {
                if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
                currentStep = if (isDeployment) 0 else currentStep + 1
                
                // Animate hopping state
                updatePawnStep(activePlayer.color, pawnId, currentStep, isHopping = true)
                LudoSoundSynth.playPawnHop()
                
                // Jump time interpolation delay for rendering squash-and-stretch
                for (frame in 1..5) {
                    updatePawnHopProgress(activePlayer.color, pawnId, frame / 5f)
                    delay(40)
                }
                
                updatePawnStep(activePlayer.color, pawnId, currentStep, isHopping = false)
                delay(60)
            }

            if (_uiState.value.status == GameStateStatus.MAIN_MENU) return@launch
            // Movement ended, check conditions
            handleMovementLanding(activePlayer.color, pawnId, currentStep, dice)
        }
    }

    private fun updatePawnStep(color: PlayerColor, id: Int, step: Int, isHopping: Boolean) {
        _uiState.update { state ->
            val updatedPawns = state.pawns.map {
                if (it.color == color && it.id == id) {
                    it.copy(stepCount = step, isHopping = isHopping)
                } else it
            }
            state.copy(pawns = updatedPawns)
        }
    }

    private fun updatePawnHopProgress(color: PlayerColor, id: Int, progress: Float) {
        _uiState.update { state ->
            val updatedPawns = state.pawns.map {
                if (it.color == color && it.id == id) {
                    it.copy(hopProgress = progress)
                } else it
            }
            state.copy(pawns = updatedPawns)
        }
    }

    private suspend fun handleMovementLanding(color: PlayerColor, pawnId: Int, finalStep: Int, rolledDice: Int) {
        val state = _uiState.value
        val activePlayer = state.players[state.activePlayerIndex]
        val landingCoord = getCoordinateForStep(color, finalStep)
        val isVi = state.language == "vi"

        var gotBonusRoll = false
        var isGoal = false

        // Check if pawn reached final Home (56)
        if (finalStep == 56) {
            isGoal = true
            gotBonusRoll = true // Goal grants bonus roll
            _uiState.update {
                it.copy(
                    bannerText = if (isVi) "MỤC TIÊU! +1 LƯỢT ĐỒ" else "GOAL! BONUS ROLL",
                    logs = it.logs + "${activePlayer.name} đưa một quân cờ về đích!",
                    portalPawnId = pawnId,
                    portalPawnColor = color,
                    portalProgress = 0f
                )
            }
            LudoSoundSynth.playGoalCelebration()
            LudoSoundSynth.playPortalTeleport()

            // Animate portalProgress from 0f to 1f over 1500ms
            val duration = 1500L
            val steps = 30
            val delayPerStep = duration / steps
            for (step in 1..steps) {
                delay(delayPerStep)
                _uiState.update {
                    it.copy(portalProgress = step.toFloat() / steps)
                }
            }

            // Reset portal state after completion
            _uiState.update {
                it.copy(
                    portalPawnId = null,
                    portalPawnColor = null,
                    portalProgress = 0f
                )
            }
            delay(300)

            // Check if player has won!
            val allHome = _uiState.value.pawns.filter { it.color == color }.all { it.stepCount == 56 }
            if (allHome) {
                // Determine rankings of players based on total step counts
                val sortedPlayersByProgress = _uiState.value.players.map { p ->
                    val totalSteps = _uiState.value.pawns.filter { it.color == p.color }.sumOf { it.stepCount }
                    p to totalSteps
                }.sortedByDescending { it.second }

                // Find where our human player is in the ranking
                val humanPlayerColor = PlayerColor.RED
                val humanRankIndex = sortedPlayersByProgress.indexOfFirst { it.first.color == humanPlayerColor }

                val mode = _uiState.value.mode
                val isArena = mode == GameMode.ARENA

                val goldReward = when (humanRankIndex) {
                    0 -> if (isArena) 2000 else 500
                    1 -> if (isArena) 1000 else 200
                    else -> 0
                }

                val rewardMsg = if (isVi) {
                    when (humanRankIndex) {
                        0 -> "Bạn về Nhất! Nhận +$goldReward Vàng 🍭"
                        1 -> "Bạn về Nhì! Nhận +$goldReward Vàng 🍭"
                        else -> "Bạn về thứ ${humanRankIndex + 1}! Hãy cố gắng hơn nhé!"
                    }
                } else {
                    when (humanRankIndex) {
                        0 -> "You got 1st Place! +$goldReward Gold 🍭"
                        1 -> "You got 2nd Place! +$goldReward Gold 🍭"
                        else -> "You got Place #${humanRankIndex + 1}! Try again!"
                    }
                }

                _uiState.update {
                    val updatedGold = it.userGold + goldReward
                    it.copy(
                        status = GameStateStatus.MATCH_ENDED,
                        userGold = updatedGold,
                        matchRewardText = rewardMsg,
                        bannerText = if (isVi) {
                            if (activePlayer.color == PlayerColor.RED) "BẠN CHIẾN THẮNG!" else "${activePlayer.name} CHIẾN THẮNG!"
                        } else {
                            if (activePlayer.color == PlayerColor.RED) "YOU WIN!" else "${activePlayer.name} WINS!"
                        },
                        logs = it.logs + "${activePlayer.name} đã giành chiến thắng chung cuộc!"
                    )
                }
                savePersistentState()
                return
            }
        } else if (landingCoord != null) {
            // Check Safe Spot immunity
            val startIndex = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
            val isSafeSpot = finalStep in 0..50 && LudoBoardConfig.safeSpotIndices.contains((startIndex + finalStep) % 52)

            if (!isSafeSpot) {
                // Find opponents to bump (excluding Safe spots and double blocks of same-color opponents)
                val opponentsOnTile = _uiState.value.pawns.filter {
                    it.color != color && it.stepCount != -1 && getCoordinateForStep(it.color, it.stepCount) == landingCoord
                }

                if (opponentsOnTile.isNotEmpty()) {
                    // Bump opponent! (Bumping double blocks was already prevented as a legal move, so this must be single opponents)
                    gotBonusRoll = true
                    _uiState.update {
                        it.copy(
                            bannerText = if (isVi) "ĐÁ BAY ĐỐI THỦ! +1 LƯỢT ĐỒ" else "BUMPED OPPONENT! BONUS ROLL",
                            logs = it.logs + "${activePlayer.name} đã đá bay quân cờ của ${opponentsOnTile.first().color.displayName}!"
                        )
                    }

                    // Play scream and trigger fly back animation
                    LudoSoundSynth.playBumpScream()

                    // Update opponent pawns to go back to base
                    opponentsOnTile.forEach { oppPawn ->
                        viewModelScope.launch {
                            // Fly animation
                            _uiState.update { s ->
                                val updated = s.pawns.map {
                                    if (it.color == oppPawn.color && it.id == oppPawn.id) {
                                        it.copy(isBumping = true)
                                    } else it
                                }
                                s.copy(pawns = updated)
                            }
                            delay(500)
                            _uiState.update { s ->
                                val updated = s.pawns.map {
                                    if (it.color == oppPawn.color && it.id == oppPawn.id) {
                                        it.copy(stepCount = -1, isBumping = false)
                                    } else it
                                }
                                s.copy(pawns = updated)
                            }
                        }
                    }

                    delay(1000)

                    // Emote triggers for the bumped bot! Extremely realistic bot behaviors!
                    opponentsOnTile.firstOrNull()?.let { bumped ->
                        triggerBotEmoteReaction(bumped.color, EmoteType.ANGRY)
                        triggerBotEmoteReaction(color, EmoteType.LAUGH)
                    }
                }
            }
        }

        // Did we roll a 6?
        if (rolledDice == 6 && !gotBonusRoll) {
            gotBonusRoll = true
            _uiState.update {
                it.copy(
                    bannerText = if (isVi) "THÊM LƯỢT! (ĐỔ 6)" else "BONUS ROLL! (ROLLED 6)",
                    logs = it.logs + "${activePlayer.name} nhận thêm lượt đổ do được 6."
                )
            }
            delay(1000)
        }

        if (gotBonusRoll) {
            // Keep turn with same active player
            _uiState.update {
                it.copy(
                    status = GameStateStatus.WAITING_FOR_ROLL,
                    selectedPawnId = null
                )
            }
            startTurn()
        } else {
            switchTurn()
        }
    }

    private fun switchTurn() {
        if (_uiState.value.status == GameStateStatus.MAIN_MENU) return
        _uiState.update {
            val nextIndex = (it.activePlayerIndex + 1) % it.players.size
            it.copy(
                activePlayerIndex = nextIndex,
                consecutiveSixes = 0,
                selectedPawnId = null
            )
        }
        startTurn()
    }

    // AI bot behavior logic
    private fun triggerBotRoll() {
        botMoveJob = viewModelScope.launch {
            val state = _uiState.value
            val activePlayer = state.players.getOrNull(state.activePlayerIndex)
            if (activePlayer != null && Random.nextFloat() < 0.15f) {
                val emotes = listOf(EmoteType.LAUGH, EmoteType.LOVE, EmoteType.SLEEPY)
                triggerBotEmoteReaction(activePlayer.color, emotes.random())
                delay(1500)
            } else {
                delay(1200 + Random.nextLong(0, 1000)) // Human-like reaction delay
            }
            rollDice()
        }
    }

    private fun triggerBotMove(legalPawns: List<Pawn>) {
        botMoveJob = viewModelScope.launch {
            delay(1000 + Random.nextLong(0, 800)) // Decision-making delay

            // Select the best strategic move using scoring algorithm!
            var bestPawn = legalPawns.first()
            var maxScore = -9999

            for (pawn in legalPawns) {
                val score = calculateMoveScore(pawn, _uiState.value.diceValue)
                if (score > maxScore) {
                    maxScore = score
                    bestPawn = pawn
                }
            }

            // Bot throws emotes sometimes based on stakes!
            if (maxScore >= 100 && Random.nextFloat() < 0.6f) {
                // Going to bump someone! Show excited or laughing emote
                triggerBotEmoteReaction(bestPawn.color, EmoteType.LAUGH)
            }

            movePawn(bestPawn.id)
        }
    }

    private fun calculateMoveScore(pawn: Pawn, dice: Int): Int {
        var score = 10 // baseline
        val targetStep = pawn.stepCount + dice

        // Reaching Home is huge
        if (targetStep == 56) {
            score += 150
        }

        // Deploying from base
        if (pawn.stepCount == -1 && dice == 6) {
            score += 80
        }

        // Check landing coordinate
        val landingCoord = getCoordinateForStep(pawn.color, targetStep)
        if (landingCoord != null) {
            // Can we bump opponent?
            val opponentsOnTile = _uiState.value.pawns.filter {
                it.color != pawn.color && it.stepCount != -1 && getCoordinateForStep(it.color, it.stepCount) == landingCoord
            }
            if (opponentsOnTile.isNotEmpty()) {
                score += 200 // Maximum priority!
            }

            // Moving into Safe Spot
            val startIndex = LudoBoardConfig.playerStartTrackIndex[pawn.color] ?: 0
            val isSafe = targetStep in 0..50 && LudoBoardConfig.safeSpotIndices.contains((startIndex + targetStep) % 52)
            if (isSafe) {
                score += 45
            }
        }

        // Progress bonus (prefer moving pawns closer to home)
        score += pawn.stepCount / 2

        // Avoid leaving safe spots if an opponent is trailing behind us
        if (pawn.stepCount != -1 && pawn.stepCount < 51) {
            val startIndex = LudoBoardConfig.playerStartTrackIndex[pawn.color] ?: 0
            val currentlySafe = LudoBoardConfig.safeSpotIndices.contains((startIndex + pawn.stepCount) % 52)
            if (currentlySafe) {
                score -= 20 // minor penalty to discourage leaving safe spots needlessly
            }
        }

        return score
    }

    // Emotes triggering
    fun throwEmote(emote: EmoteType, targetColor: PlayerColor? = null) {
        throwEmoteWithChat(emote, null, targetColor)
    }

    fun throwEmoteWithChat(emote: EmoteType, chatText: String? = null, targetColor: PlayerColor? = null) {
        val state = _uiState.value
        val finalColor = PlayerColor.RED
        
        val activeEmote = ActiveEmote(
            playerColor = finalColor,
            emote = emote,
            timestamp = System.currentTimeMillis(),
            targetColor = targetColor,
            chatText = chatText
        )

        LudoSoundSynth.playEmoteSound(emote)

        _uiState.update {
            it.copy(activeEmotes = it.activeEmotes + activeEmote)
        }

        // Clear after 4 seconds
        viewModelScope.launch {
            delay(4000)
            _uiState.update { s ->
                s.copy(activeEmotes = s.activeEmotes.filter { it != activeEmote })
            }
        }
    }

    private fun getRandomBotQuote(color: PlayerColor, emote: EmoteType): String {
        return when (emote) {
            EmoteType.ANGRY -> listOf(
                "Ơ kìa, chơi ác thế! 😡",
                "Chờ đấy, tớ sẽ phục thù! ⚔️",
                "Đừng có đùa với tớ nhé! 😤",
                "Sao lại đá tớ về chuồng dợ? 😭",
                "Hic, ác như thú vậy! 😡"
            ).random()
            EmoteType.CRY -> listOf(
                "Bánh ngọt của tớ... bay màu rồi! 😭",
                "Hu hu, sao ai cũng bắt nạt tớ vậy? 🥺",
                "Đen đủi quá đi mất! 😭",
                "Mẹ ơi tớ muốn về nhà! 😭",
                "Trời ơi là trời! 🌧️"
            ).random()
            EmoteType.LOVE -> listOf(
                "Hehe, yêu quá cơ! ❤️",
                "Cảm ơn vì đã nhường đường nha! 😘",
                "Bạn tốt nhất hệ mặt trời! 🥰",
                "Moa moa chụt chụt! 💕",
                "Thả tim nhẹ nhàng! ❤️"
            ).random()
            EmoteType.LAUGH -> listOf(
                "Ahihi đồ ngốc, cho về chuồng nhé! 😂",
                "Kaka, số đỏ không đỡ được! 🎲",
                "Hehe ngọt xỉu luôn! 🍩",
                "Haha, né xa ta ra nha! 🏃‍♂️",
                "Cười ẻ, quá dễ dàng! 😂"
            ).random()
            EmoteType.SLEEPY -> listOf(
                "Đang ngủ gật đây này... 😴",
                "Nhanh lên bạn ơi, sốt ruột quá! 🥱",
                "Zzz... buồn ngủ ghê á! 😴",
                "Ủa tới lượt ai dợ? 🧐",
                "Lâu quá đi mất, ngủ một giấc đã! 💤"
            ).random()
            else -> listOf(
                "Ném trái táo nè! 🍎",
                "Ăn táo không bạn? 🍎",
                "Trúng đầu nè! Hehe 🎯"
            ).random()
        }
    }

    private fun triggerBotEmoteReaction(color: PlayerColor, emote: EmoteType) {
        viewModelScope.launch {
            delay(500)
            val quote = getRandomBotQuote(color, emote)
            val activeEmote = ActiveEmote(
                playerColor = color,
                emote = emote,
                timestamp = System.currentTimeMillis(),
                chatText = quote
            )
            LudoSoundSynth.playEmoteSound(emote)
            _uiState.update {
                it.copy(activeEmotes = it.activeEmotes + activeEmote)
            }
            delay(4000)
            _uiState.update { s ->
                s.copy(activeEmotes = s.activeEmotes.filter { it != activeEmote })
            }
        }
    }

    fun showProfileStats(player: LudoPlayer?) {
        LudoSoundSynth.playClick()
        _uiState.update { it.copy(showProfileStatsPlayer = player) }
    }

    fun exitToMainMenu() {
        LudoSoundSynth.playClick()
        timerJob?.cancel()
        botMoveJob?.cancel()
        matchmakingJob?.cancel()
        introAnimationJob?.cancel()
        _uiState.update {
            it.copy(
                status = GameStateStatus.MAIN_MENU,
                bannerText = "SWEETY LUDO"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        botMoveJob?.cancel()
        matchmakingJob?.cancel()
        introAnimationJob?.cancel()
    }
}
