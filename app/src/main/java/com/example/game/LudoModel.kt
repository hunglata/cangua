package com.example.game

import androidx.compose.ui.graphics.Color

enum class PlayerColor(val displayName: String, val baseColor: Color, val accentColor: Color) {
    RED("Đỏ", Color(0xFFFF5252), Color(0xFFFF8A80)),
    GREEN("Xanh Lá", Color(0xFF4CAF50), Color(0xFFB9F6CA)),
    YELLOW("Vàng", Color(0xFFFFEB3B), Color(0xFFFFFF8D)),
    BLUE("Xanh Dương", Color(0xFF2196F3), Color(0xFF82B1FF))
}

enum class GameMode {
    ONLINE,      // Simulated online matchmaking with custom profiles and bots
    WITH_FRIENDS, // Local pass and play with friends
    OFFLINE,     // Play against offline AI bots
    ARENA        // Competitive tournament mode with high gold stakes and smart bots
}

data class PlayerStats(
    val guestId: String,
    val name: String,
    val avatarId: Int, // index to a preselected avatar
    val gold: Int,
    val diamonds: Int,
    val totalGames: Int,
    val wins: Int,
    val winRate: Int // percentage
)

data class Pawn(
    val id: Int, // 0..3
    val color: PlayerColor,
    // -1 means in Base, 0..50 on main track, 51..55 on home path, 56 at center home
    val stepCount: Int = -1,
    val isBumping: Boolean = false,
    val isHopping: Boolean = false,
    val hopProgress: Float = 0f // for jump animation interpolation
)

data class LudoPlayer(
    val color: PlayerColor,
    val name: String,
    val isBot: Boolean,
    val avatarId: Int,
    val totalGames: Int = 120,
    val wins: Int = 65,
    val winRate: Int = 54,
    val gold: Int = 2500,
    val diamonds: Int = 50,
    val isOnline: Boolean = true,
    val characterSkin: String = "char1"
)

enum class EmoteType(val symbol: String, val soundName: String) {
    ANGRY("😡", "angry"),
    CRY("😭", "cry"),
    LOVE("❤️", "love"),
    LAUGH("😂", "laugh"),
    SLEEPY("😴", "sleepy"),
    APPLE("🍎", "throw")
}

data class ActiveEmote(
    val playerColor: PlayerColor,
    val emote: EmoteType,
    val timestamp: Long,
    val targetColor: PlayerColor? = null,
    val chatText: String? = null
)

enum class GameStateStatus {
    MAIN_MENU,
    MATCHMAKING,
    INTRO_CAMERA,
    WAITING_FOR_ROLL,
    ROLLING_DICE,
    WAITING_FOR_MOVE,
    MOVING_PAWN,
    GOAL_EFFECT,
    MATCH_ENDED
}

data class GameState(
    val mode: GameMode = GameMode.OFFLINE,
    val status: GameStateStatus = GameStateStatus.MAIN_MENU,
    val players: List<LudoPlayer> = emptyList(),
    val activePlayerIndex: Int = 0,
    val diceValue: Int = 1,
    val isDiceRolling: Boolean = false,
    val consecutiveSixes: Int = 0,
    val pawns: List<Pawn> = emptyList(),
    val selectedPawnId: Int? = null,
    val bannerText: String = "",
    val turnTimeLeft: Int = 30, // seconds
    val showProfileStatsPlayer: LudoPlayer? = null,
    val activeEmotes: List<ActiveEmote> = emptyList(),
    val logs: List<String> = emptyList(),
    val isMusicOn: Boolean = true,
    val isSfxOn: Boolean = true,
    val language: String = "vi", // "en" or "vi"
    val userGold: Int = 3000,
    val userDiamonds: Int = 10,
    val unlockedCharacters: Set<String> = setOf("char1"),
    val selectedCharacter: String = "char1",
    val lastClaimedRewardTimestamp: Long = 0L,
    val portalPawnId: Int? = null,
    val portalPawnColor: PlayerColor? = null,
    val portalProgress: Float = 0f,
    val matchRewardText: String = "",
    val playerName: String = ""
)

// Board Coordinates
object LudoBoardConfig {
    // 52-tile outer track coordinates on the 15x15 grid
    val outerTrack = listOf(
        Pair(0, 6), Pair(1, 6), Pair(2, 6), Pair(3, 6), Pair(4, 6), Pair(5, 6), // Left arm top row
        Pair(6, 5), Pair(6, 4), Pair(6, 3), Pair(6, 2), Pair(6, 1), Pair(6, 0), // Top arm left column
        Pair(7, 0), // Connection
        Pair(8, 0), Pair(8, 1), Pair(8, 2), Pair(8, 3), Pair(8, 4), Pair(8, 5), // Top arm right column
        Pair(9, 6), Pair(10, 6), Pair(11, 6), Pair(12, 6), Pair(13, 6), Pair(14, 6), // Right arm top row
        Pair(14, 7), // Connection
        Pair(14, 8), Pair(13, 8), Pair(12, 8), Pair(11, 8), Pair(10, 8), Pair(9, 8), // Right arm bottom row
        Pair(8, 9), Pair(8, 10), Pair(8, 11), Pair(8, 12), Pair(8, 13), Pair(8, 14), // Bottom arm right column
        Pair(7, 14), // Connection
        Pair(6, 14), Pair(6, 13), Pair(6, 12), Pair(6, 11), Pair(6, 10), Pair(6, 9), // Bottom arm left column
        Pair(5, 8), Pair(4, 8), Pair(3, 8), Pair(2, 8), Pair(1, 8), Pair(0, 8), // Left arm bottom row
        Pair(0, 7) // Connection
    )

    // Starting indices of players in the outer track
    val playerStartTrackIndex = mapOf(
        PlayerColor.RED to 40,      // (6, 13)
        PlayerColor.GREEN to 1,     // (1, 6)
        PlayerColor.YELLOW to 14,    // (8, 1)
        PlayerColor.BLUE to 27      // (13, 8)
    )

    // 5-tile home path coordinates for each player leading up to the center
    val homePaths = mapOf(
        PlayerColor.RED to listOf(Pair(7, 13), Pair(7, 12), Pair(7, 11), Pair(7, 10), Pair(7, 9)),
        PlayerColor.GREEN to listOf(Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7)),
        PlayerColor.YELLOW to listOf(Pair(7, 1), Pair(7, 2), Pair(7, 3), Pair(7, 4), Pair(7, 5)),
        PlayerColor.BLUE to listOf(Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7))
    )

    // Center Home coordinates
    val centerHomes = mapOf(
        PlayerColor.RED to Pair(7, 8),
        PlayerColor.GREEN to Pair(6, 7),
        PlayerColor.YELLOW to Pair(7, 6),
        PlayerColor.BLUE to Pair(8, 7)
    )

    // Safe Spot Indices on the 52-tile outer track (8 total)
    val safeSpotIndices = setOf(1, 8, 14, 21, 27, 34, 40, 47)

    // Mini-grid positions of pawns in base (4 pawns per player)
    val basePawnPositions = mapOf(
        PlayerColor.RED to listOf(Pair(10.7f, 10.7f), Pair(11.7f, 10.7f), Pair(10.7f, 11.7f), Pair(11.7f, 11.7f)),
        PlayerColor.GREEN to listOf(Pair(2.3f, 10.7f), Pair(3.3f, 10.7f), Pair(2.3f, 11.7f), Pair(3.3f, 11.7f)),
        PlayerColor.YELLOW to listOf(Pair(2.3f, 2.3f), Pair(3.3f, 2.3f), Pair(2.3f, 3.3f), Pair(3.3f, 3.3f)),
        PlayerColor.BLUE to listOf(Pair(10.7f, 2.3f), Pair(11.7f, 2.3f), Pair(10.7f, 3.3f), Pair(11.7f, 3.3f))
    )
}
