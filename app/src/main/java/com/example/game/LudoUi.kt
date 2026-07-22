package com.example.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Avatars lists
val AvatarEmojis = listOf("🧁", "🍓", "🍩", "🐼", "🦊", "🐯", "🦁")

@Composable
fun rememberAssetImagePainter(assetPath: String): Painter? {
    val context = LocalContext.current
    var painter by remember(assetPath) { mutableStateOf<Painter?>(null) }
    LaunchedEffect(assetPath) {
        try {
            context.assets.open(assetPath).use { stream ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    painter = androidx.compose.ui.graphics.painter.BitmapPainter(
                        bitmap.asImageBitmap()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return painter
}

@Composable
fun PlayerAvatar(
    color: PlayerColor,
    avatarId: Int,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    customEmoji: String? = null
) {
    val context = LocalContext.current
    var painter by remember(color) { mutableStateOf<Painter?>(null) }
    
    LaunchedEffect(color) {
        try {
            val possiblePaths = listOf(
                "IMG/${color.name.lowercase()}.png",
                "IMG/${color.name.lowercase()}.jpg",
                "IMG/${color.name.lowercase()}.jpeg",
                "IMG/${color.name.lowercase()}.webp",
                "img/${color.name.lowercase()}.png",
                "img/${color.name.lowercase()}.jpg",
                "img/${color.name.lowercase()}.jpeg",
                "img/${color.name.lowercase()}.webp",
                "${color.name.lowercase()}.png",
                "${color.name.lowercase()}.jpg",
                "${color.name.lowercase()}.jpeg",
                "${color.name.lowercase()}.webp",
                "IMG/${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.png",
                "img/${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.png",
                "${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.png",
                "IMG/${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.jpg",
                "img/${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.jpg",
                "${when(color) {
                    PlayerColor.GREEN -> "1"
                    PlayerColor.RED -> "2"
                    PlayerColor.BLUE -> "3"
                    PlayerColor.YELLOW -> "4"
                }}.jpg"
            )
            
            var foundPath: String? = null
            
            // Try matching via dynamic file list first (checking "img" then "IMG" directories)
            var list = emptyArray<String>()
            var dirPrefix = "img"
            try {
                list = context.assets.list("img") ?: emptyArray()
            } catch (e: Exception) {
                // ignore
            }
            if (list.isEmpty()) {
                try {
                    list = context.assets.list("IMG") ?: emptyArray()
                    dirPrefix = "IMG"
                } catch (e: Exception) {
                    // ignore
                }
            }
            
            if (list.isNotEmpty()) {
                val match = list.firstOrNull { filename ->
                    val lower = filename.lowercase()
                    lower.contains(color.name.lowercase()) || (
                        lower.startsWith(when(color) {
                            PlayerColor.GREEN -> "1"
                            PlayerColor.RED -> "2"
                            PlayerColor.BLUE -> "3"
                            PlayerColor.YELLOW -> "4"
                        })
                    )
                }
                if (match != null) {
                    foundPath = "$dirPrefix/$match"
                }
            }
            
            if (foundPath == null) {
                // Try matching root list
                val rootList = context.assets.list("") ?: emptyArray()
                if (rootList.isNotEmpty()) {
                    val match = rootList.firstOrNull { filename ->
                        val lower = filename.lowercase()
                        lower.contains(color.name.lowercase()) || (
                            lower.startsWith(when(color) {
                                PlayerColor.GREEN -> "1"
                                PlayerColor.RED -> "2"
                                PlayerColor.BLUE -> "3"
                                PlayerColor.YELLOW -> "4"
                            })
                        )
                    }
                    if (match != null) {
                        foundPath = match
                    }
                }
            }
            
            if (foundPath == null) {
                for (path in possiblePaths) {
                    try {
                        context.assets.open(path).close()
                        foundPath = path
                        break
                    } catch (e: Exception) {
                        // ignore and try next
                    }
                }
            }
            
            if (foundPath != null) {
                context.assets.open(foundPath).use { stream ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        painter = androidx.compose.ui.graphics.painter.BitmapPainter(
                            bitmap.asImageBitmap()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    if (painter != null) {
        Image(
            painter = painter!!,
            contentDescription = "Avatar ${color.name}",
            modifier = modifier.size(size).clip(shape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        // Fallback to emoji
        val emojiStr = when (color) {
            PlayerColor.GREEN -> "🐴"
            PlayerColor.BLUE -> "🐂"
            PlayerColor.YELLOW -> "🐥"
            PlayerColor.RED -> customEmoji ?: "🦈"
        }
        val fallbackEmoji = if (avatarId >= 0) {
            if (color == PlayerColor.RED) customEmoji ?: "🦈"
            else AvatarEmojis[avatarId % AvatarEmojis.size]
        } else {
            emojiStr
        }
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Text(fallbackEmoji, fontSize = fontSize)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LudoGameApp(viewModel: LudoViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Screen-shake offset when bump occurs
    var shakeOffset by remember { mutableStateOf(Offset.Zero) }

    val characterBitmaps = remember { mutableStateMapOf<String, ImageBitmap>() }
    LaunchedEffect(Unit) {
        for (i in 1..8) {
            val paths = listOf(
                "IMG/Character$i.png",
                "IMG/character$i.png",
                "img/Character$i.png",
                "img/character$i.png",
                "IMG/Character$i.PNG",
                "IMG/character$i.PNG",
                "IMG/$i.png",
                "img/$i.png",
                "$i.png"
            )
            var loaded = false
            for (path in paths) {
                try {
                    context.assets.open(path).use { stream ->
                        val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                        if (bitmap != null) {
                            characterBitmaps["char$i"] = bitmap.asImageBitmap()
                            loaded = true
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
                if (loaded) break
            }
        }
    }

    // Start background music loop if ON
    LaunchedEffect(state.isMusicOn, state.status) {
        if (state.isMusicOn) {
            LudoSoundSynth.isMusicEnabled = true
            LudoSoundSynth.startMusic(state.status == GameStateStatus.MAIN_MENU || state.status == GameStateStatus.MATCHMAKING)
        } else {
            LudoSoundSynth.stopMusic()
        }
    }

    // Capture bumps for screenshake animation
    LaunchedEffect(state.pawns) {
        val anyBumping = state.pawns.any { it.isBumping }
        if (anyBumping) {
            // Trigger 5-frame shake
            launch {
                for (i in 1..8) {
                    shakeOffset = Offset(
                        (kotlin.random.Random.nextFloat() - 0.5f) * 20f,
                        (kotlin.random.Random.nextFloat() - 0.5f) * 20f
                    )
                    delay(40)
                }
                shakeOffset = Offset.Zero
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFCE4EC) // Warm pastel strawberry pink background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shakeOffset.x.dp, y = shakeOffset.y.dp)
        ) {
            when (state.status) {
                GameStateStatus.MAIN_MENU -> {
                    MainMenuScreen(
                        state = state,
                        viewModel = viewModel,
                        characterBitmaps = characterBitmaps,
                        onStartGame = { mode -> viewModel.startNewGame(mode, context) },
                        onToggleMusic = { viewModel.toggleMusic() },
                        onToggleSfx = { viewModel.toggleSfx() },
                        onSetLanguage = { lang -> viewModel.setLanguage(lang) }
                    )
                }
                GameStateStatus.MATCHMAKING -> {
                    MatchmakingScreen(state = state, onCancel = { viewModel.exitToMainMenu() })
                }
                else -> {
                    // Actual In-Game screen with 2.5D Isometric Board
                    GamePlayScreen(
                        state = state,
                        viewModel = viewModel,
                        characterBitmaps = characterBitmaps,
                        onExit = { viewModel.exitToMainMenu() }
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    state: GameState,
    viewModel: LudoViewModel,
    characterBitmaps: Map<String, ImageBitmap>,
    onStartGame: (GameMode) -> Unit,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    onSetLanguage: (String) -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    val isVi = state.language == "vi"
    val context = LocalContext.current

    var showRewardsDialog by remember { mutableStateOf(false) }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var showArenaClosedDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showWithFriendsNamesDialog by remember { mutableStateOf(false) }
    var friendName1 by remember { mutableStateOf("") }
    var friendName2 by remember { mutableStateOf("") }
    var friendName3 by remember { mutableStateOf("") }
    var friendName4 by remember { mutableStateOf("") }

    val banbePainter = rememberAssetImagePainter("IMG/banbe.png")
    val onlinePainter = rememberAssetImagePainter("IMG/online.png")
    val offlinePainter = rememberAssetImagePainter("IMG/offline.png")
    val backgroundPainter = rememberAssetImagePainter("IMG/background.png")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCBE2B0)) // Light green pasture background
    ) {
        if (backgroundPainter != null) {
            androidx.compose.foundation.Image(
                painter = backgroundPainter,
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            // SCENERY: Fields, pathways, winding water stream, trees, fences
            Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Water stream at the top right
            val streamPath = Path().apply {
                moveTo(w * 0.62f, 0f)
                cubicTo(
                    w * 0.65f, h * 0.1f,
                    w * 0.72f, h * 0.08f,
                    w * 0.76f, h * 0.22f
                )
                cubicTo(
                    w * 0.82f, h * 0.38f,
                    w * 0.92f, h * 0.35f,
                    w * 1.0f, h * 0.45f
                )
                lineTo(w, 0f)
                close()
            }
            drawPath(streamPath, SolidColor(Color(0xFF81D4FA)))

            // Subtle waves in river
            drawCircle(Color.White.copy(alpha = 0.35f), radius = 6f, center = Offset(w * 0.78f, h * 0.06f))
            drawCircle(Color.White.copy(alpha = 0.35f), radius = 8f, center = Offset(w * 0.88f, h * 0.18f))
            drawCircle(Color.White.copy(alpha = 0.35f), radius = 5f, center = Offset(w * 0.94f, h * 0.32f))

            // 2. Main winding biscuit pathway
            // A warm golden cream path cutting through the pasture
            val pathFill = Path().apply {
                moveTo(w * 0.18f, h * 0.38f)
                quadraticBezierTo(w * 0.35f, h * 0.48f, w * 0.52f, h * 0.45f)
                quadraticBezierTo(w * 0.68f, h * 0.42f, w * 0.76f, h * 0.58f)
                quadraticBezierTo(w * 0.82f, h * 0.72f, w * 0.85f, h * 1.0f)
                lineTo(w * 0.65f, h * 1.0f)
                quadraticBezierTo(w * 0.62f, h * 0.75f, w * 0.54f, h * 0.68f)
                quadraticBezierTo(w * 0.42f, h * 0.62f, w * 0.32f, h * 0.72f)
                quadraticBezierTo(w * 0.25f, h * 0.8f, w * 0.2f, h * 1.0f)
                lineTo(0f, h * 1.0f)
                lineTo(0f, h * 0.68f)
                quadraticBezierTo(w * 0.1f, h * 0.55f, w * 0.18f, h * 0.38f)
                close()
            }
            drawPath(pathFill, SolidColor(Color(0xFFEEDBB2)))

            // Chocolate dashed borders along the pathway
            val points = listOf(
                Offset(w * 0.18f, h * 0.38f), Offset(w * 0.26f, h * 0.43f), Offset(w * 0.35f, h * 0.46f),
                Offset(w * 0.44f, h * 0.46f), Offset(w * 0.52f, h * 0.45f), Offset(w * 0.60f, h * 0.44f),
                Offset(w * 0.68f, h * 0.46f), Offset(w * 0.73f, h * 0.52f), Offset(w * 0.76f, h * 0.58f),
                Offset(w * 0.79f, h * 0.65f), Offset(w * 0.82f, h * 0.72f), Offset(w * 0.83f, h * 0.85f), Offset(w * 0.85f, h * 1.0f)
            )
            for (p in points) {
                drawCircle(Color(0xFF795548), radius = 3.5f, center = p)
            }

            // 3. Cute Standee Gingerbread Men scattered around the field (🍪)
            fun drawGingerbreadMan(x: Float, y: Float, scale: Float = 1f) {
                val r = 10f * scale
                val bodyColor = Color(0xFFBCAAA4) // Biscuit brown
                val frostColor = Color.White
                // Head
                drawCircle(bodyColor, radius = r, center = Offset(x, y - r * 1.2f))
                // Head frosting eyes and smile
                drawCircle(frostColor, radius = r * 0.18f, center = Offset(x - r * 0.35f, y - r * 1.35f))
                drawCircle(frostColor, radius = r * 0.18f, center = Offset(x + r * 0.35f, y - r * 1.35f))
                drawCircle(Color(0xFFE51C23), radius = r * 0.22f, center = Offset(x, y - r * 0.85f)) // Red nose/mouth
                
                // Body
                drawOval(bodyColor, topLeft = Offset(x - r * 0.8f, y - r * 0.4f), size = Size(r * 1.6f, r * 2.2f))
                // Buttons
                drawCircle(Color(0xFFE51C23), radius = r * 0.15f, center = Offset(x, y + r * 0.1f))
                drawCircle(Color(0xFF4CAF50), radius = r * 0.15f, center = Offset(x, y + r * 0.6f))
                // Arms
                drawOval(bodyColor, topLeft = Offset(x - r * 1.4f, y - r * 0.2f), size = Size(r * 0.7f, r * 1.1f))
                drawOval(bodyColor, topLeft = Offset(x + r * 0.7f, y - r * 0.2f), size = Size(r * 0.7f, r * 1.1f))
                // Feet
                drawCircle(bodyColor, radius = r * 0.45f, center = Offset(x - r * 0.4f, y + r * 1.8f))
                drawCircle(bodyColor, radius = r * 0.4f, center = Offset(x + r * 0.4f, y + r * 1.8f))
            }

            drawGingerbreadMan(w * 0.32f, h * 0.16f, 1.1f)
            drawGingerbreadMan(w * 0.84f, h * 0.38f, 0.9f)
            drawGingerbreadMan(w * 0.41f, h * 0.84f, 1.2f)
            drawGingerbreadMan(w * 0.66f, h * 0.90f, 1.0f)

            // 4. Little fences / logs along paths
            fun drawFencePost(x: Float, y: Float) {
                drawRoundRect(
                    color = Color(0xFF8D6E63),
                    topLeft = Offset(x, y),
                    size = Size(10f, 32f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(x + 2f, y + 2f),
                    size = Size(6f, 28f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
            fun drawFenceConnection(x1: Float, y1: Float, x2: Float, y2: Float) {
                drawLine(Color(0xFF8D6E63), Offset(x1 + 5f, y1 + 10f), Offset(x2 + 5f, y2 + 10f), strokeWidth = 5f)
                drawLine(Color(0xFF8D6E63), Offset(x1 + 5f, y1 + 22f), Offset(x2 + 5f, y2 + 22f), strokeWidth = 5f)
            }

            // Group of fence posts on the left
            drawFencePost(w * 0.09f, h * 0.3f)
            drawFencePost(w * 0.12f, h * 0.27f)
            drawFencePost(w * 0.15f, h * 0.24f)
            drawFenceConnection(w * 0.09f, h * 0.3f, w * 0.12f, h * 0.27f)
            drawFenceConnection(w * 0.12f, h * 0.27f, w * 0.15f, h * 0.24f)

            // Group of fences in center
            drawFencePost(w * 0.37f, h * 0.29f)
            drawFencePost(w * 0.40f, h * 0.26f)
            drawFenceConnection(w * 0.37f, h * 0.29f, w * 0.40f, h * 0.26f)

            // Fences on the right path
            drawFencePost(w * 0.84f, h * 0.52f)
            drawFencePost(w * 0.88f, h * 0.48f)
            drawFenceConnection(w * 0.84f, h * 0.52f, w * 0.88f, h * 0.48f)

            // 5. Beautiful lollipop trees / wafer trees
            fun drawLollipopTree(cx: Float, cy: Float, primaryColor: Color, sizeFactor: Float = 1f) {
                drawRect(
                    color = Color(0xFFD7CCC8),
                    topLeft = Offset(cx - 3.5f * sizeFactor, cy),
                    size = Size(7f * sizeFactor, 42f * sizeFactor)
                )
                drawCircle(primaryColor, radius = 22f * sizeFactor, center = Offset(cx, cy))
                drawCircle(Color.White, radius = 17f * sizeFactor, center = Offset(cx, cy))
                drawCircle(primaryColor, radius = 12f * sizeFactor, center = Offset(cx, cy))
                drawCircle(Color.White, radius = 7f * sizeFactor, center = Offset(cx, cy))
            }
            drawLollipopTree(w * 0.22f, h * 0.22f, Color(0xFFE91E63), 1.1f)
            drawLollipopTree(w * 0.25f, h * 0.18f, Color(0xFFFF9800), 0.9f)
            drawLollipopTree(w * 0.92f, h * 0.15f, Color(0xFF4CAF50), 1.2f)
            drawLollipopTree(w * 0.96f, h * 0.20f, Color(0xFF00BCD4), 0.8f)

            // Golden biscuit star in sky/pasture
            fun drawBiscuitStar(x: Float, y: Float, radius: Float) {
                val starPath = Path().apply {
                    val angle = Math.PI / 5
                    for (i in 0..9) {
                        val r = if (i % 2 == 0) radius else radius * 0.5f
                        val currAngle = i * angle - Math.PI / 2
                        val px = (x + r * cos(currAngle)).toFloat()
                        val py = (y + r * sin(currAngle)).toFloat()
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(starPath, SolidColor(Color(0xFFFFF59D)))
                drawPath(starPath, SolidColor(Color(0xFFFBC02D)), style = Stroke(width = 3f))
            }
            drawBiscuitStar(w * 0.05f, h * 0.50f, 22f)
            drawBiscuitStar(w * 0.75f, h * 0.31f, 18f)
            drawBiscuitStar(w * 0.78f, h * 0.25f, 24f)
        }
        }

        // ==============================================
        // TOP HEADER HUD ROW
        // ==============================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile & Balances Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Unified Profile & Name Block
                val currentMascotEmoji = when (state.selectedCharacter) {
                    "char1" -> "🧁"
                    "char2" -> "🍓"
                    "char3" -> "🍩"
                    "char4" -> "🐼"
                    "char5" -> "🦊"
                    "char6" -> "🐯"
                    "char7" -> "🦁"
                    "char8" -> "🦄"
                    else -> "🧁"
                }
                val currentMascotName = when (state.selectedCharacter) {
                    "char1" -> if (isVi) "Nhân vật 1" else "Character 1"
                    "char2" -> if (isVi) "Nhân vật 2" else "Character 2"
                    "char3" -> if (isVi) "Nhân vật 3" else "Character 3"
                    "char4" -> if (isVi) "Nhân vật 4" else "Character 4"
                    "char5" -> if (isVi) "Nhân vật 5" else "Character 5"
                    "char6" -> if (isVi) "Nhân vật 6" else "Character 6"
                    "char7" -> if (isVi) "Nhân vật 7" else "Character 7"
                    "char8" -> if (isVi) "Nhân vật 8" else "Character 8"
                    else -> if (isVi) "Nhân vật 1" else "Character 1"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFBEBE1), RoundedCornerShape(16.dp))
                        .border(2.5.dp, Color(0xFF8D6E63), RoundedCornerShape(16.dp))
                        .clickable { showRenameDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    // Selected mascot square box
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF81D4FA), RoundedCornerShape(10.dp))
                            .border(2.2.dp, Color(0xFF8D6E63), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = characterBitmaps[state.selectedCharacter]
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap,
                                contentDescription = currentMascotName,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Text(currentMascotEmoji, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.playerName.isNotEmpty()) state.playerName else (if (isVi) "Bạn" else "You"),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color(0xFF4E342E)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("✏️", fontSize = 12.sp)
                }

                // 2. Lollipop Balance Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(50.dp))
                        .background(Color(0xFFFBEBE1), RoundedCornerShape(50.dp))
                        .border(2.2.dp, Color(0xFF8D6E63), RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("🍭", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("%,d", state.userGold),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFF4E342E)
                    )
                }

                // 3. Diamond Balance Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(50.dp))
                        .background(Color(0xFFFBEBE1), RoundedCornerShape(50.dp))
                        .border(2.2.dp, Color(0xFF8D6E63), RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("💎", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("%,d", state.userDiamonds),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFF4E342E)
                    )
                }
            }

            // Top-right action group (Arena, Rewards, Settings)
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Đấu Trường (Arena Mode)
                val isArenaOpen = remember {
                    val calendar = java.util.Calendar.getInstance()
                    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                    hour in 20..21
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        if (isArenaOpen) {
                            onStartGame(GameMode.ARENA)
                        } else {
                            showArenaClosedDialog = true
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                            .border(2.5.dp, Color(0xFF8D6E63), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 24.sp)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 4.dp)
                                .background(if (isArenaOpen) Color(0xFF4CAF50) else Color(0xFF757575), RoundedCornerShape(6.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (isArenaOpen) (if (isVi) "MỞ" else "OPEN") else "20-22h",
                                color = Color.White,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isVi) "Đấu trường" else "Arena",
                        color = Color(0xFFFFFDF0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF3E2723),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 2f
                            )
                        )
                    )
                }

                // 2. Nhận Thưởng
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showRewardsDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp))
                            .background(Color(0xFFBA68C8), RoundedCornerShape(12.dp))
                            .border(2.5.dp, Color(0xFF8D6E63), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📺", fontSize = 24.sp)

                        if (viewModel.canClaimDailyReward()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(16.dp)
                                    .background(Color(0xFFE51C23), CircleShape)
                                    .border(1.2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "1",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isVi) "Nhận quà" else "Rewards",
                        color = Color(0xFFFFFDF0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF3E2723),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 2f
                            )
                        )
                    )
                }

                // 3. Settings Gear
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showSettings = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(3.dp, CircleShape)
                            .background(Color(0xFFFFD54F), CircleShape)
                            .border(2.5.dp, Color(0xFF8D6E63), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF5D4037),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isVi) "Cài đặt" else "Settings",
                        color = Color(0xFFFFFDF0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF3E2723),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }
        }

        // ==============================================
        // THREE MAIN GAME-MODE BUILDINGS (CENTER PLACEMENT)
        // ==============================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 45.dp)
        ) {
            // 1. Chơi Online (Lower-Left Building)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 32.dp, bottom = 2.dp)
                    .size(210.dp)
                    .clickable { onStartGame(GameMode.ONLINE) }
            ) {
                if (onlinePainter != null) {
                    Image(
                        painter = onlinePainter,
                        contentDescription = "Online Play",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Ground Shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.16f),
                            topLeft = Offset(w * 0.08f, h * 0.72f),
                            size = Size(w * 0.84f, h * 0.22f)
                        )

                        // 1. Double gold base platform
                        drawOval(
                            color = Color(0xFFE0C28B),
                            topLeft = Offset(w * 0.1f, h * 0.65f),
                            size = Size(w * 0.8f, h * 0.25f)
                        )
                        drawOval(
                            color = Color(0xFFF1DAB1),
                            topLeft = Offset(w * 0.1f, h * 0.63f),
                            size = Size(w * 0.8f, h * 0.24f)
                        )

                        // 2. Carousel dome pillars & striped cake rotunda body
                        val rotundaLeft = w * 0.18f
                        val rotundaRight = w * 0.82f
                        val rotundaTop = h * 0.40f
                        val rotundaBottom = h * 0.75f

                        // Rotunda base body
                        drawRect(
                            color = Color(0xFFF06292),
                            topLeft = Offset(rotundaLeft, rotundaTop),
                            size = Size(rotundaRight - rotundaLeft, rotundaBottom - rotundaTop)
                        )

                        // Vertical cream pillars / cake slices
                        val numPillars = 5
                        val step = (rotundaRight - rotundaLeft) / (numPillars - 1)
                        for (i in 0 until numPillars) {
                            val px = rotundaLeft + i * step
                            // Cream pillars
                            drawRect(
                                color = Color(0xFFFFFDF2),
                                topLeft = Offset(px - 6f, rotundaTop - 4f),
                                size = Size(12f, rotundaBottom - rotundaTop + 4f)
                            )
                            // White meringue dollop peaks on top of pillars
                            drawCircle(
                                color = Color.White,
                                radius = 10f,
                                center = Offset(px, rotundaTop - 8f)
                            )
                        }

                        // Golden door in center
                        drawRoundRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(w * 0.42f, h * 0.52f),
                            size = Size(w * 0.16f, h * 0.23f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = Color(0xFF8D6E63),
                            topLeft = Offset(w * 0.44f, h * 0.54f),
                            size = Size(w * 0.12f, h * 0.21f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 3f)
                        )

                        // 3. Pink velvet dome roof
                        val roofPath = Path().apply {
                            moveTo(rotundaLeft - 10f, rotundaTop)
                            quadraticBezierTo(w * 0.5f, h * 0.12f, rotundaRight + 10f, rotundaTop)
                            close()
                        }
                        drawPath(roofPath, SolidColor(Color(0xFFD81B60)))

                        // Scalloped frosting drip at roof lower rim
                        val scallCount = 8
                        val scallStep = (rotundaRight + 20f - (rotundaLeft - 10f)) / scallCount
                        for (i in 0..scallCount) {
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = Offset(rotundaLeft - 10f + i * scallStep, rotundaTop)
                            )
                        }

                        // 4. Cherry Syrup dripping from top of dome
                        drawCircle(Color(0xFFC2185B), radius = 15f, center = Offset(w * 0.5f, h * 0.24f))

                        // 5. Huge 3D white dice cookie sitting on top
                        val diceSize = 34f
                        val dx = w * 0.5f - diceSize / 2f
                        val dy = h * 0.16f - diceSize / 2f
                        // Draw die body (rounded rect)
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(dx, dy),
                            size = Size(diceSize, diceSize),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFF795548),
                            topLeft = Offset(dx, dy),
                            size = Size(diceSize, diceSize),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 3.5f)
                        )
                        // Dice spots (One big red spot in center for value 1)
                        drawCircle(Color(0xFFD81B60), radius = 5.5f, center = Offset(w * 0.5f, h * 0.16f))
                    }
                }

                // Semi-transparent label pill overlay exactly matching the mockup style
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .background(Color(0x994E342E), RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(20.dp))
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isVi) "Chơi Online" else "Online Play",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // 2. Chơi Với Bạn (Middle-Top Donut Cottage)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .size(190.dp)
                    .clickable { showWithFriendsNamesDialog = true }
            ) {
                if (banbePainter != null) {
                    Image(
                        painter = banbePainter,
                        contentDescription = "With Friends",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Ground shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.16f),
                            topLeft = Offset(w * 0.15f, h * 0.72f),
                            size = Size(w * 0.7f, h * 0.18f)
                        )

                        // Walls (gingerbread golden-brown rectangle)
                        drawRoundRect(
                            color = Color(0xFF8D6E63),
                            topLeft = Offset(w * 0.22f, h * 0.44f),
                            size = Size(w * 0.56f, h * 0.32f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        // Yellow framing lines
                        drawRoundRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(w * 0.22f, h * 0.44f),
                            size = Size(w * 0.56f, h * 0.32f),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 3.5f)
                        )

                        // Door (chocolate round arch)
                        drawRoundRect(
                            color = Color(0xFF4E342E),
                            topLeft = Offset(w * 0.42f, h * 0.54f),
                            size = Size(w * 0.16f, h * 0.22f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )

                        // Purple grape biscuit tiled roof
                        val roofPath = Path().apply {
                            moveTo(w * 0.16f, h * 0.44f)
                            lineTo(w * 0.5f, h * 0.14f)
                            lineTo(w * 0.84f, h * 0.44f)
                            close()
                        }
                        drawPath(roofPath, SolidColor(Color(0xFFAB47BC)))
                        drawPath(roofPath, SolidColor(Color(0xFF7B1FA2)), style = Stroke(width = 3.5f))

                        // Huge beautiful glazed donut sitting on top
                        val donutCx = w * 0.5f
                        val donutCy = h * 0.20f
                        // Ring body
                        drawCircle(Color(0xFFE2C488), radius = 28f, center = Offset(donutCx, donutCy))
                        // Glazing pink frosting on donut
                        drawCircle(Color(0xFFFF8A80), radius = 23f, center = Offset(donutCx, donutCy))
                        // Hole in donut
                        drawCircle(Color(0xFFCBE2B0), radius = 10f, center = Offset(donutCx, donutCy)) // matches background color
                        drawCircle(Color(0xFF8D6E63), radius = 10f, center = Offset(donutCx, donutCy), style = Stroke(width = 2.5f))

                        // Red heart candies on the donut
                        fun drawMiniHeart(hx: Float, hy: Float) {
                            val hpath = Path().apply {
                                moveTo(hx, hy)
                                cubicTo(hx - 5f, hy - 6f, hx - 10f, hy, hx, hy + 8f)
                                cubicTo(hx + 10f, hy, hx + 5f, hy - 6f, hx, hy)
                            }
                            drawPath(hpath, SolidColor(Color(0xFFE51C23)))
                        }
                        drawMiniHeart(donutCx - 8f, donutCy - 4f)
                        drawMiniHeart(donutCx + 8f, donutCy - 4f)
                    }
                }

                // Semi-transparent label pill overlay exactly matching the mockup style
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .background(Color(0x994E342E), RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(20.dp))
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isVi) "Chơi Với Bạn" else "With Friends",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // 3. Chơi Offline (Lower-Right Wafer Cabin)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 32.dp, bottom = 2.dp)
                    .size(200.dp)
                    .clickable { onStartGame(GameMode.OFFLINE) }
            ) {
                if (offlinePainter != null) {
                    Image(
                        painter = offlinePainter,
                        contentDescription = "Play Offline",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Ground shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.16f),
                            topLeft = Offset(w * 0.1f, h * 0.72f),
                            size = Size(w * 0.8f, h * 0.2f)
                        )

                        // Wafer walls (light tan rectangle with horizontal log divisions)
                        drawRoundRect(
                            color = Color(0xFFF5E6D3),
                            topLeft = Offset(w * 0.22f, h * 0.44f),
                            size = Size(w * 0.56f, h * 0.32f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        // Log lines
                        drawLine(Color(0xFF8D6E63), Offset(w * 0.22f, h * 0.52f), Offset(w * 0.78f, h * 0.52f), strokeWidth = 3f)
                        drawLine(Color(0xFF8D6E63), Offset(w * 0.22f, h * 0.60f), Offset(w * 0.78f, h * 0.60f), strokeWidth = 3f)
                        drawLine(Color(0xFF8D6E63), Offset(w * 0.22f, h * 0.68f), Offset(w * 0.78f, h * 0.68f), strokeWidth = 3f)

                        // Brown wafer framing
                        drawRoundRect(
                            color = Color(0xFF8D6E63),
                            topLeft = Offset(w * 0.22f, h * 0.44f),
                            size = Size(w * 0.56f, h * 0.32f),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 3.5f)
                        )

                        // Green ring donut window
                        val winCx = w * 0.5f
                        val winCy = h * 0.58f
                        drawCircle(Color(0xFF4CAF50), radius = 16f, center = Offset(winCx, winCy))
                        drawCircle(Color(0xFFF5E6D3), radius = 7f, center = Offset(winCx, winCy))
                        drawCircle(Color(0xFF8D6E63), radius = 16f, center = Offset(winCx, winCy), style = Stroke(width = 2.5f))

                        // Wafer curved roof canopy
                        val roofPath = Path().apply {
                            moveTo(w * 0.16f, h * 0.44f)
                            quadraticBezierTo(w * 0.5f, h * 0.18f, w * 0.84f, h * 0.44f)
                            quadraticBezierTo(w * 0.5f, h * 0.36f, w * 0.16f, h * 0.44f)
                            close()
                        }
                        drawPath(roofPath, SolidColor(Color(0xFFD7CCC8)))
                        drawPath(roofPath, SolidColor(Color(0xFF8D6E63)), style = Stroke(width = 3.5f))

                        // Left chimney (Chocolate roll stick)
                        drawRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(w * 0.28f, h * 0.22f),
                            size = Size(12f, 26f)
                        )
                        // Spiral lines on roll
                        drawLine(Color.White, Offset(w * 0.28f, h * 0.24f), Offset(w * 0.28f + 12f, h * 0.27f), strokeWidth = 2.5f)
                        drawLine(Color.White, Offset(w * 0.28f, h * 0.30f), Offset(w * 0.28f + 12f, h * 0.33f), strokeWidth = 2.5f)
                    }
                }

                // Semi-transparent label pill overlay exactly matching the mockup style
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .background(Color(0x994E342E), RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(20.dp))
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isVi) "Chơi Offline" else "Play Offline",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // ==============================================
        // BOTTOM FOOTER NAVIGATION (GUIDE, SHOP)
        // ==============================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Right: "Hướng Dẫn" & "Cửa Hàng"
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hướng Dẫn Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(16.dp))
                        .border(2.5.dp, Color(0xFF8D6E63), RoundedCornerShape(16.dp))
                        .clickable { showTutorial = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("📖", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isVi) "Hướng Dẫn" else "Tutorial",
                        color = Color(0xFF4E342E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Cửa Hàng Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFCE4EC), RoundedCornerShape(16.dp))
                        .border(2.5.dp, Color(0xFF8D6E63), RoundedCornerShape(16.dp))
                        .clickable { showInventoryDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("🏪", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isVi) "Cửa Hàng" else "Shop",
                        color = Color(0xFF4E342E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }

    // Special Sweet reward dialog
    if (showRewardsDialog) {
        val canClaim = viewModel.canClaimDailyReward()
        Dialog(onDismissRequest = { showRewardsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎁 " + (if (isVi) "Nhận Quà Hàng Ngày" else "Daily Reward"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (canClaim) {
                            if (isVi) "Chúc mừng! Bạn nhận được quà tặng ngọt ngào từ Candy Kingdom:\n\n🍭 +100 Kẹo mút vàng\n💎 +5 Kim cương lấp lánh!"
                            else "Congratulations! You received a sweet gift from Candy Kingdom:\n\n🍭 +100 Golden Lollipops\n💎 +5 Sparkling Diamonds!"
                        } else {
                            if (isVi) "Bạn đã nhận quà hôm nay rồi!\n\nThời gian nhận quà sẽ reset sau 00:00 hàng ngày. Hãy quay lại vào ngày mai để tiếp tục nhận quà nhé! 💖"
                            else "You have already claimed your daily reward today!\n\nThe claim timer resets at 00:00 daily. Please come back tomorrow for more sweet treats! 💖"
                        },
                        fontSize = 15.sp,
                        color = Color(0xFF5D4037),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (canClaim) {
                                val success = viewModel.claimDailyReward()
                                if (success) {
                                    LudoSoundSynth.playGoalCelebration()
                                    android.widget.Toast.makeText(
                                        context,
                                        if (isVi) "Nhận quà thành công! +100 Kẹo mút vàng, +5 Kim cương lấp lánh!"
                                        else "Reward claimed! +100 Golden Lollipops, +5 Sparkling Diamonds!",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            showRewardsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canClaim) Color(0xFFD81B60) else Color.LightGray,
                            contentColor = Color.White
                        ),
                        enabled = canClaim,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (canClaim) {
                                if (isVi) "NHẬN QUÀ" else "CLAIM REWARD"
                            } else {
                                if (isVi) "ĐÃ NHẬN HÔM NAY" else "CLAIMED TODAY"
                            }
                        )
                    }
                    if (!canClaim) {
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(
                            onClick = { showRewardsDialog = false }
                        ) {
                            Text(if (isVi) "Đóng" else "Close", color = Color(0xFFD81B60))
                        }
                    }
                }
            }
        }
    }

    if (showArenaClosedDialog) {
        Dialog(onDismissRequest = { showArenaClosedDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 64.sp)
                    Text(
                        text = if (isVi) "Đấu Trường Ludo" else "Ludo Arena",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isVi) {
                            "Đấu trường chỉ mở cửa tranh tài vào lúc 20:00 - 22:00 hàng ngày!\n\nỞ đây, bạn sẽ đấu với các đối thủ thông minh và có cơ hội kiếm được rất nhiều Kẹo mút vàng 🍭 khi chiến thắng về nhất hoặc về nhì!\n\nHiện tại đấu trường đang đóng cửa."
                        } else {
                            "The Arena is only open for competition from 20:00 to 22:00 daily!\n\nHere, you will play against highly intelligent bots and earn lots of Golden Lollipops 🍭 when finishing 1st or 2nd place!\n\nCurrently, the Arena is closed."
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF5D4037),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showArenaClosedDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isVi) "QUAY LẠI SAU" else "COME BACK LATER")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.material3.TextButton(
                        onClick = {
                            showArenaClosedDialog = false
                            onStartGame(GameMode.ARENA)
                        }
                    ) {
                        Text(
                            text = if (isVi) "CHƠI THỬ NGHIỆM (ADMIN BYPASS)" else "PLAY TEST MODE (ADMIN BYPASS)",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        var tempName by remember { mutableStateOf(state.playerName) }
        Dialog(onDismissRequest = { showRenameDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📝", fontSize = 48.sp)
                    Text(
                        text = if (isVi) "Đổi Tên Người Chơi" else "Change Player Name",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value = tempName,
                        onValueChange = { if (it.length <= 15) tempName = it },
                        placeholder = { Text(if (isVi) "Nhập tên mới..." else "Enter new name...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD81B60),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showRenameDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isVi) "HỦY" else "CANCEL", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                if (tempName.trim().isNotEmpty()) {
                                    viewModel.updatePlayerName(tempName.trim())
                                    LudoSoundSynth.playClick()
                                    showRenameDialog = false
                                    android.widget.Toast.makeText(
                                        context,
                                        if (isVi) "Đã đổi tên thành: ${tempName.trim()}!" else "Name changed to: ${tempName.trim()}!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            enabled = tempName.trim().isNotEmpty()
                        ) {
                            Text(if (isVi) "LƯU" else "SAVE")
                        }
                    }
                }
            }
        }
    }

    if (showWithFriendsNamesDialog) {
        Dialog(onDismissRequest = { showWithFriendsNamesDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF8D6E63))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVi) "👥 NHẬP TÊN NGƯỜI CHƠI" else "👥 ENTER PLAYER NAMES",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD81B60),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val defaultP1 = if (state.playerName.isNotEmpty()) state.playerName else (if (isVi) "Bạn" else "You")

                    // Name 1 (Red)
                    androidx.compose.material3.OutlinedTextField(
                        value = friendName1,
                        onValueChange = { friendName1 = it },
                        label = { Text(if (isVi) "Người chơi 1 (Đỏ)" else "Player 1 (Red)", fontSize = 12.sp) },
                        placeholder = { Text(defaultP1) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Name 2 (Green)
                    androidx.compose.material3.OutlinedTextField(
                        value = friendName2,
                        onValueChange = { friendName2 = it },
                        label = { Text(if (isVi) "Người chơi 2 (Lá)" else "Player 2 (Green)", fontSize = 12.sp) },
                        placeholder = { Text("Player 2 (Lá)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Name 3 (Yellow)
                    androidx.compose.material3.OutlinedTextField(
                        value = friendName3,
                        onValueChange = { friendName3 = it },
                        label = { Text(if (isVi) "Người chơi 3 (Vàng)" else "Player 3 (Yellow)", fontSize = 12.sp) },
                        placeholder = { Text("Player 3 (Vàng)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Name 4 (Blue)
                    androidx.compose.material3.OutlinedTextField(
                        value = friendName4,
                        onValueChange = { friendName4 = it },
                        label = { Text(if (isVi) "Người chơi 4 (Dương)" else "Player 4 (Blue)", fontSize = 12.sp) },
                        placeholder = { Text("Player 4 (Dương)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showWithFriendsNamesDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isVi) "HỦY" else "CANCEL", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val p1 = if (friendName1.isNotBlank()) friendName1 else defaultP1
                                val p2 = if (friendName2.isNotBlank()) friendName2 else "Player 2 (Lá)"
                                val p3 = if (friendName3.isNotBlank()) friendName3 else "Player 3 (Vàng)"
                                val p4 = if (friendName4.isNotBlank()) friendName4 else "Player 4 (Dương)"

                                showWithFriendsNamesDialog = false
                                viewModel.startNewGame(GameMode.WITH_FRIENDS, context, listOf(p1, p2, p3, p4))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isVi) "CHƠI NGAY" else "PLAY", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Special Sweet Shop/Cửa Hàng dialog
    if (showInventoryDialog) {
        Dialog(onDismissRequest = { showInventoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏪 " + (if (isVi) "Cửa Hàng Candy" else "Candy Shop"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isVi) "Sử dụng Kẹo mút vàng để mua các nhân vật siêu dễ thương!" else "Use Golden Lollipops to purchase adorable sweet characters!",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Player Balances Row in Shop
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🍭", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%,d", state.userGold),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF4E342E)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%,d", state.userDiamonds),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF4E342E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val shopCharactersList = listOf(
                        Triple("char1", if (isVi) "Bánh Kem Creamy 🧁" else "Cupcake Creamy 🧁", 0),
                        Triple("char2", if (isVi) "Dâu Tây Ngọt Ngào 🍓" else "Strawberry Sweety 🍓", 200),
                        Triple("char3", if (isVi) "Bánh Vòng Choco 🍩" else "Donut Choco 🍩", 400),
                        Triple("char4", if (isVi) "Gấu Trúc Trân Châu 🐼" else "Boba Panda 🐼", 600),
                        Triple("char5", if (isVi) "Cáo Mật Ong 🦊" else "Honey Fox 🦊", 800),
                        Triple("char6", if (isVi) "Hổ Ca Cao 🐯" else "Cocoa Tiger 🐯", 1000),
                        Triple("char7", if (isVi) "Sư Tử Bơ 🦁" else "Butter Lion 🦁", 1200),
                        Triple("char8", if (isVi) "Kỳ Lân Kẹo Ngọt 🦄" else "Candy Unicorn 🦄", 1500)
                    )

                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(shopCharactersList.size) { index ->
                            val item = shopCharactersList[index]
                            val id = item.first
                            val name = item.second
                            val price = item.third

                            val isUnlocked = state.unlockedCharacters.contains(id)
                            val isEquipped = state.selectedCharacter == id

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isEquipped) Color(0xFFE8F5E9) else Color(0xFFF9F9F9),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = if (isEquipped) 2.dp else 1.dp,
                                        color = if (isEquipped) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Emoji Extraction
                                val emoji = name.split(" ").last()
                                val labelText = name.substring(0, name.length - emoji.length).trim()

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFFFF3E0), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = characterBitmaps[id]
                                    if (bitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = bitmap,
                                            contentDescription = labelText,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Text(emoji, fontSize = 24.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = labelText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4E342E)
                                    )
                                    if (id == "char1") {
                                        Text(
                                            text = if (isVi) "Mặc định (Miễn phí)" else "Default (Free)",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🍭 ", fontSize = 11.sp)
                                            Text(
                                                text = "$price",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }

                                // Button Action
                                Button(
                                    onClick = {
                                        if (isUnlocked) {
                                            viewModel.selectCharacter(id)
                                            LudoSoundSynth.playClick()
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isVi) "Đã chọn nhân vật: $labelText!" else "Equipped: $labelText!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            val success = viewModel.purchaseCharacter(id, price)
                                            if (success) {
                                                LudoSoundSynth.playGoalCelebration()
                                                viewModel.selectCharacter(id) // auto equip
                                                android.widget.Toast.makeText(
                                                    context,
                                                    if (isVi) "Mua thành công & trang bị $labelText! 🎉" else "Purchased & equipped $labelText! 🎉",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                LudoSoundSynth.playClick()
                                                android.widget.Toast.makeText(
                                                    context,
                                                    if (isVi) "Không đủ Kẹo mút vàng! 🍭" else "Not enough Golden Lollipops! 🍭",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            isEquipped -> Color(0xFF4CAF50)
                                            isUnlocked -> Color(0xFF8D6E63)
                                            else -> Color(0xFFD81B60)
                                        }
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = when {
                                            isEquipped -> if (isVi) "ĐANG DÙNG" else "EQUIPPED"
                                            isUnlocked -> if (isVi) "CHỌN" else "EQUIP"
                                            else -> if (isVi) "MUA" else "BUY"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showInventoryDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isVi) "ĐÓNG" else "CLOSE")
                    }
                }
            }
        }
    }

    // Settings popup Dialog
    if (showSettings) {
        Dialog(onDismissRequest = { showSettings = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVi) "Cài Đặt Game" else "Settings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Music toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isVi) "Nhạc Nền (BGM)" else "Background Music",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5D4037)
                        )
                        Switch(
                            checked = state.isMusicOn,
                            onCheckedChange = { onToggleMusic() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD81B60))
                        )
                    }

                    // SFX toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isVi) "Âm Thanh Hiệu Ứng" else "Sound Effects (SFX)",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5D4037)
                        )
                        Switch(
                            checked = state.isSfxOn,
                            onCheckedChange = { onToggleSfx() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD81B60))
                        )
                    }

                    // Language toggler
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isVi) "Ngôn Ngữ" else "Language",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5D4037)
                        )
                        Row {
                            Button(
                                onClick = { onSetLanguage("vi") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isVi) Color(0xFFD81B60) else Color.LightGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text("VI", color = Color.White)
                            }
                            Button(
                                onClick = { onSetLanguage("en") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isVi) Color(0xFFD81B60) else Color.LightGray
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("EN", color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showSettings = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isVi) "ĐÓNG" else "CLOSE")
                    }
                }
            }
        }
    }

    // Tutorial dialog
    if (showTutorial) {
        Dialog(onDismissRequest = { showTutorial = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVi) "Cách Chơi Sweety Ludo" else "How To Play",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val tutText = if (isVi) {
                        "1. **Đổ xúc xắc**: Nhấn vào khối chocolate ở góc phải khi đến lượt.\n" +
                        "2. **Xuất quân**: Đổ được số 6 để đưa quân từ Căn Cứ Bánh Kem ra vạch xuất phát.\n" +
                        "3. **Đá quân**: Di chuyển trùng ô với đối thủ để đá bay họ về căn cứ (Trừ ô Hoa An Toàn 🌸).\n" +
                        "4. **Tạo khối chắn**: 2 quân cùng màu đứng chung ô sẽ tạo khối chắn đối thủ không thể đá.\n" +
                        "5. **Thêm lượt**: Nhận thêm lượt đổ khi đổ được 6, đá đối thủ hoặc đưa quân về đích thành công (Tối đa 3 lần đổ 6 sẽ mất lượt).\n" +
                        "6. **Chiến thắng**: Đưa toàn bộ 4 quân cờ nhảy lên đỉnh tháp bánh ở trung tâm bằng điểm đổ chính xác!"
                    } else {
                        "1. **Roll Dice**: Tap the chocolate dice block on bottom right when it is your turn.\n" +
                        "2. **Deployment**: Roll a 6 to deploy a cute bird from your high Cage to the track.\n" +
                        "3. **Bump (Kill)**: Land on an opponent's tile to bump them back to base (except on safe Green Flowers 🌸).\n" +
                        "4. **Blocks**: 2 same-colored birds on a tile form a double-block that cannot be bumped.\n" +
                        "5. **Bonus Rolls**: Get a bonus roll by rolling a 6, bumping an opponent, or reaching Home. Rolling a 6 thrice voids turn.\n" +
                        "6. **Goal**: Land all 4 pawns at the center cake top with exact rolls to win!"
                    }

                    Text(
                        text = tutText,
                        fontSize = 14.sp,
                        color = Color(0xFF5D4037),
                        lineHeight = 20.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showTutorial = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isVi) "ĐÃ HIỂU" else "GOT IT")
                    }
                }
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    desc: String,
    icon: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() }
            .testTag("mode_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF4E342E)
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FooterButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFFD81B60))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color(0xFF4E342E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun MatchmakingScreen(state: GameState, onCancel: () -> Unit) {
    val isVi = state.language == "vi"
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF1F1)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .rotate(rotation)
                .border(6.dp, Color(0xFFD81B60), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🧁", fontSize = 54.sp, modifier = Modifier.rotate(-rotation))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (isVi) "Đang Tìm Đối Thủ Trực Tuyến..." else "Finding Online Opponents...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD81B60)
        )
        Text(
            text = if (isVi) "Đang tìm kiếm bánh ngọt ngọt ngào nhất..." else "Searching for the sweetest players...",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isVi) "HỦY TÌM" else "CANCEL", color = Color(0xFF4E342E))
        }
    }
}

@Composable
fun GamePlayScreen(
    state: GameState,
    viewModel: LudoViewModel,
    characterBitmaps: Map<String, ImageBitmap>,
    onExit: () -> Unit
) {
    val isVi = state.language == "vi"
    val activePlayer = state.players.getOrNull(state.activePlayerIndex) ?: return

    val context = LocalContext.current
    var backgroundPlayBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(Unit) {
        val paths = listOf("IMG/background_play.png", "img/background_play.png", "background_play.png")
        for (path in paths) {
            try {
                context.assets.open(path).use { stream ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        backgroundPlayBitmap = bitmap.asImageBitmap()
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
            if (backgroundPlayBitmap != null) break
        }
    }

    // Camera zoom intro state animation!
    var boardZoom by remember { mutableStateOf(0.4f) }
    var showGameplaySettings by remember { mutableStateOf(false) }
    var showChatEmoteDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            boardZoom = value
        }
    }

    // Glow shader pulse around dice block for active player's turn to roll
    val dicePulseTransition = rememberInfiniteTransition(label = "dicePulse")
    val diceGlowScale by dicePulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF261912)) // Deep Warm Chocolate Cocoa Backdrop for ultimate character contrast
    ) {
        if (backgroundPlayBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = backgroundPlayBitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            // BEAUTIFUL DESSERT BACKDROP DECORATIONS
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Wafer Pillars in the background (as dark brown columns)
                // Left wafer column holding red lollipop
                drawRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(40f.dp.toPx(), h - 350f.dp.toPx()),
                    size = Size(24f.dp.toPx(), 350f.dp.toPx())
                )
                // Left chocolate dripping top
                drawCircle(
                    color = Color(0xFF3E2723),
                    radius = 16f.dp.toPx(),
                    center = Offset(52f.dp.toPx(), h - 350f.dp.toPx())
                )

                // Right wafer column holding green lollipop
                drawRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(w - 70f.dp.toPx(), h - 450f.dp.toPx()),
                    size = Size(20f.dp.toPx(), 450f.dp.toPx())
                )
                // Right chocolate dripping top
                drawCircle(
                    color = Color(0xFF3E2723),
                    radius = 14f.dp.toPx(),
                    center = Offset(w - 60f.dp.toPx(), h - 450f.dp.toPx())
                )

                // Draw spiral red/white lollipop on the left
                val leftLollipopCenter = Offset(52f.dp.toPx(), h - 360f.dp.toPx())
                val leftLollipopRadius = 50f.dp.toPx()
                drawCircle(Color.White, radius = leftLollipopRadius, center = leftLollipopCenter)
                // Draw spiral red lines
                for (i in 0..5) {
                    drawArc(
                        color = Color(0xFFE57373),
                        startAngle = (i * 60).toFloat(),
                        sweepAngle = 30f,
                        useCenter = true,
                        topLeft = Offset(leftLollipopCenter.x - leftLollipopRadius, leftLollipopCenter.y - leftLollipopRadius),
                        size = Size(leftLollipopRadius * 2, leftLollipopRadius * 2)
                    )
                }
                // Inner white swirl overlay
                drawCircle(Color.White, radius = leftLollipopRadius * 0.4f, center = leftLollipopCenter)
                drawCircle(Color(0xFFE57373), radius = leftLollipopRadius * 0.2f, center = leftLollipopCenter)

                // Draw spiral green/white lollipop on the top-right
                val rightLollipopCenter = Offset(w - 60f.dp.toPx(), h - 460f.dp.toPx())
                val rightLollipopRadius = 45f.dp.toPx()
                drawCircle(Color.White, radius = rightLollipopRadius, center = rightLollipopCenter)
                // Draw spiral green lines
                for (i in 0..5) {
                    drawArc(
                        color = Color(0xFF81C784),
                        startAngle = (i * 60 + 30).toFloat(),
                        sweepAngle = 30f,
                        useCenter = true,
                        topLeft = Offset(rightLollipopCenter.x - rightLollipopRadius, rightLollipopCenter.y - rightLollipopRadius),
                        size = Size(rightLollipopRadius * 2, rightLollipopRadius * 2)
                    )
                }
                // Inner white swirl overlay
                drawCircle(Color.White, radius = rightLollipopRadius * 0.4f, center = rightLollipopCenter)
                drawCircle(Color(0xFF81C784), radius = rightLollipopRadius * 0.2f, center = rightLollipopCenter)

                // Floating star cookies!
                drawPastelStarCookie(Offset(80f.dp.toPx(), h - 220f.dp.toPx()), 12f.dp.toPx())
                drawPastelStarCookie(Offset(100f.dp.toPx(), 60f.dp.toPx()), 15f.dp.toPx())
                drawPastelStarCookie(Offset(w - 50f.dp.toPx(), 180f.dp.toPx()), 14f.dp.toPx())
                drawPastelStarCookie(Offset(w - 140f.dp.toPx(), h - 180f.dp.toPx()), 18f.dp.toPx())
            }
        }

        // CENTER: ISOMETRIC BOARD AREA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 96.dp, vertical = 6.dp), // More vertical space to avoid clipping of bases
            contentAlignment = Alignment.Center
        ) {
            // Rendering 2.5D Isometric Canvas
            LudoIsometricBoardCanvas(
                state = state,
                zoom = boardZoom,
                characterBitmaps = characterBitmaps,
                onPawnClicked = { pawnId ->
                    if (state.status == GameStateStatus.WAITING_FOR_MOVE && activePlayer.color == state.pawns.find { it.id == pawnId }?.color) {
                        viewModel.movePawn(pawnId)
                    }
                }
            )
        }

        // ==========================================
        // LANDSCAPE HUD OVERLAYS (MATCHING REFERENCE MOCKUP)
        // ==========================================

        // 1. TOP-LEFT CORNER: Green Player Card ("Suzanna" style with dinosaur mascot)
        state.players.find { it.color == PlayerColor.GREEN }?.let { player ->
            CutePlayerCard(
                player = player,
                isActive = activePlayer.color == PlayerColor.GREEN,
                timeLeft = state.turnTimeLeft,
                align = Alignment.TopStart,
                isGreenMascotStyle = true,
                characterBitmaps = characterBitmaps,
                onClick = { viewModel.showProfileStats(player) }
            )
        }

        // 2. BOTTOM-LEFT CORNER: Red Player Card (Strawberry Red style)
        state.players.find { it.color == PlayerColor.RED }?.let { player ->
            CutePlayerCard(
                player = player,
                isActive = activePlayer.color == PlayerColor.RED,
                timeLeft = state.turnTimeLeft,
                align = Alignment.BottomStart,
                characterBitmaps = characterBitmaps,
                onClick = { viewModel.showProfileStats(player) }
            )
        }

        // 3. TOP-RIGHT CORNER: Yellow Player Card (Custard Yellow style)
        state.players.find { it.color == PlayerColor.YELLOW }?.let { player ->
            CutePlayerCard(
                player = player,
                isActive = activePlayer.color == PlayerColor.YELLOW,
                timeLeft = state.turnTimeLeft,
                align = Alignment.TopEnd,
                characterBitmaps = characterBitmaps,
                onClick = { viewModel.showProfileStats(player) }
            )
        }

        // 4. BOTTOM-RIGHT CORNER: Blue Player Card ("Guest5132" style with chicken mascot and badge)
        state.players.find { it.color == PlayerColor.BLUE }?.let { player ->
            CutePlayerCard(
                player = player,
                isActive = activePlayer.color == PlayerColor.BLUE,
                timeLeft = state.turnTimeLeft,
                align = Alignment.BottomEnd,
                isBlueGuestStyle = true,
                characterBitmaps = characterBitmaps,
                onClick = { viewModel.showProfileStats(player) }
            )
        }

        // 5. LEFT EDGE (MIDDLE): Cute wooden scalloped Back Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, top = 40.dp) // Offset down from green player
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .size(54.dp)
                    .shadow(6.dp, CircleShape)
                    .background(Color(0xFFE2C488), CircleShape) // Biscuit color
                    .border(3.dp, Color(0xFF8D6E63), CircleShape) // Chocolate border
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Exit", tint = Color(0xFF5D4037), modifier = Modifier.size(26.dp))
            }
        }

        // 6. RIGHT EDGE (TOP): Gear Settings Icon ⚙️
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 76.dp) // Positioned nicely below the Yellow player card
        ) {
            IconButton(
                onClick = { showGameplaySettings = true }, // Opens actual Settings dialog
                modifier = Modifier
                    .size(46.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFFFD93D), CircleShape) // Scallop yellow
                    .border(2.5.dp, Color(0xFFEBC02D), CircleShape)
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Color(0xFF795548), modifier = Modifier.size(24.dp))
            }
        }

        // 7. RIGHT EDGE (MIDDLE): Beautiful Glowing Orange Scalloped Dice Badge with horizontal glowing line
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glowing yellow horizontal bar across the dice as in mockup
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(24.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFFFFEB3B).copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
            )

            // Pulse aura on active turn
            if (state.status == GameStateStatus.WAITING_FOR_ROLL && !activePlayer.isBot) {
                Box(
                    modifier = Modifier
                        .size((90 * diceGlowScale).dp)
                        .background(Color(0xFFFFEB3B).copy(alpha = 0.35f), CircleShape)
                )
            }

            // Outer Scalloped Orange Badge
            Box(
                modifier = Modifier
                    .size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.width / 2f
                    drawCircle(Color(0xFFE67E22), radius = r) // Outer orange scallop
                    drawCircle(Color(0xFFF39C12), radius = r * 0.9f) // Mid orange
                    drawCircle(Color(0xFFFFEB3B).copy(alpha = 0.25f), radius = r * 0.8f) // Inner light
                }

                // Interactive Cream Dice Cookie
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFFDF0), RoundedCornerShape(10.dp)) // Cream White Dice body
                        .border(2.5.dp, Color(0xFFE2C488), RoundedCornerShape(10.dp))
                        .clickable {
                            if (state.status == GameStateStatus.WAITING_FOR_ROLL && !activePlayer.isBot) {
                                viewModel.rollDice()
                            }
                        }
                        .testTag("dice_roll_block"),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isDiceRolling) {
                        CircularProgressIndicator(color = Color(0xFFE67E22), strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                    } else {
                        // Draw Chocolate Dots
                        DrawDiceFace(value = state.diceValue)
                    }
                }
            }
        }

        // 8. RIGHT EDGE (BOTTOM): Beautiful Chat & Emote Trigger Button
        IconButton(
            onClick = { showChatEmoteDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp) // Placed nicely above the Blue Card
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .background(Color(0xFF8D6E63), CircleShape)
                .border(2.dp, Color(0xFF5D4037), CircleShape)
        ) {
            Text("💬", fontSize = 24.sp)
        }



        // 9. BOTTOM CENTER: Cute Dialog Speech Bubble displaying latest logs
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .width(320.dp)
                .height(44.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFFF5D1D8), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.logs.lastOrNull() ?: (if (isVi) "Chào mừng bạn đến với Thế giới Bánh Ngọt!" else "Welcome to the Dessert World!"),
                fontSize = 11.sp,
                color = Color(0xFF4E342E),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }

        // 10. ACTIVE TURN POPUP BANNER ("NEXT TURN" / "LƯỢT TIẾP THEO")
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = state.bannerText.isNotEmpty() && state.status == GameStateStatus.WAITING_FOR_ROLL,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFD93D), RoundedCornerShape(16.dp))
                        .border(3.dp, Color(0xFFEBC02D), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isVi) "LƯỢT TIẾP THEO" else "NEXT TURN",
                            color = Color(0xFF795548),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = state.bannerText,
                            color = Color(0xFF3E2723), // Dark cocoa chocolate for solid, high-contrast readability
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = Color.White.copy(alpha = 0.5f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 1f
                                )
                            )
                        )
                    }
                }
            }
        }

        // EMOTE OVERLAYS floating on top
        state.activeEmotes.forEach { activeEmote ->
            FloatingEmoteOverlay(activeEmote = activeEmote)
        }

        // PROFILE VIEW POPUP WITH STATS AND EMOTE GRID
        state.showProfileStatsPlayer?.let { profilePlayer ->
            Dialog(onDismissRequest = { viewModel.showProfileStats(null) }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(profilePlayer.color.baseColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                PlayerAvatar(
                                    color = profilePlayer.color,
                                    avatarId = profilePlayer.avatarId,
                                    size = 50.dp,
                                    fontSize = 32.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = profilePlayer.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E342E)
                                )
                                Text(
                                    text = if (profilePlayer.isBot) "MÁY (SMART BOT)" else "NGƯỜI CHƠI (USER)",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        // Player Profile Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MetricItem(label = if (isVi) "Tổng số trận" else "Games Played", value = profilePlayer.totalGames.toString())
                            MetricItem(label = if (isVi) "Số trận thắng" else "Wins", value = profilePlayer.wins.toString())
                            MetricItem(label = if (isVi) "Tỉ lệ thắng" else "Win Rate", value = "${profilePlayer.winRate}%")
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        // EMOTE INTERACTIVE GRID UNDERNEATH METRICS
                        Text(
                            text = if (isVi) "Ném Biểu Cảm Cho Đối Thủ:" else "Throw Emote at Opponent:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D6E63),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EmoteType.values().forEach { emo ->
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .shadow(1.dp, CircleShape)
                                        .background(Color(0xFFFFF1F1), CircleShape)
                                        .clickable {
                                            viewModel.throwEmote(emo, profilePlayer.color)
                                            viewModel.showProfileStats(null)
                                        }
                                        .testTag("emote_${emo.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emo.symbol, fontSize = 24.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.showProfileStats(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isVi) "ĐÓNG" else "CLOSE")
                        }
                    }
                }
            }
        }

        // CUSTOM EMOTE & QUICK CHAT SELECTION DIALOG (CONSOLIDATED & SMART DESIGN)
        if (showChatEmoteDialog) {
            Dialog(onDismissRequest = { showChatEmoteDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFD81B60))
                ) {
                    var customChatText by remember { mutableStateOf("") }

                    val dialogScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth()
                            .heightIn(max = 290.dp)
                            .verticalScroll(dialogScrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header with Close Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "💬 BIỂU CẢM & TRÒ CHUYỆN" else "💬 EMOTES & CHAT",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD81B60)
                            )
                            IconButton(
                                onClick = { showChatEmoteDialog = false },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF5F5F5), CircleShape)
                            ) {
                                Text("❌", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Emotes Section (Single elegant row of all 6 interactive emojis with background glow)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "🎭 BIỂU CẢM NHANH:" else "🎭 QUICK EMOTE:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8D6E63),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val emotes = EmoteType.values()
                            val emoteColors = mapOf(
                                EmoteType.LAUGH to Pair(Color(0xFFFFF9C4), Color(0xFFFBC02D)), // Light yellow, yellow border
                                EmoteType.ANGRY to Pair(Color(0xFFFFCDD2), Color(0xFFE53935)), // Light red, red border
                                EmoteType.CRY to Pair(Color(0xFFE1F5FE), Color(0xFF1E88E5)),   // Light blue, blue border
                                EmoteType.LOVE to Pair(Color(0xFFFCE4EC), Color(0xFFD81B60)),  // Light pink, pink border
                                EmoteType.SLEEPY to Pair(Color(0xFFEDE7F6), Color(0xFF8E24AA)),// Light violet, purple border
                                EmoteType.APPLE to Pair(Color(0xFFE8F5E9), Color(0xFF43A047))  // Light green, green border
                            )

                            emotes.forEach { emo ->
                                val colors = emoteColors[emo] ?: Pair(Color(0xFFFFF1F1), Color(0xFFD81B60))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.throwEmote(emo)
                                            showChatEmoteDialog = false
                                        }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(3.dp, CircleShape)
                                            .background(colors.first, CircleShape)
                                            .border(1.5.dp, colors.second, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emo.symbol, fontSize = 26.sp)
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                        // 2. Custom Input Bar (The Smart Field with Send action and instant feedback)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.OutlinedTextField(
                                value = customChatText,
                                onValueChange = { customChatText = it },
                                placeholder = {
                                    Text(
                                        text = if (isVi) "Nhập tin nhắn tùy chỉnh..." else "Type custom message...",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (customChatText.isNotEmpty()) {
                                        IconButton(onClick = { customChatText = "" }) {
                                            Text("❌", fontSize = 10.sp)
                                        }
                                    }
                                },
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD81B60),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (customChatText.isNotBlank()) {
                                        viewModel.throwEmoteWithChat(emote = EmoteType.LOVE, chatText = customChatText)
                                        customChatText = ""
                                        showChatEmoteDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(
                                    text = if (isVi) "Gửi 🚀" else "Send 🚀",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                        // 3. Quick Chats List (Much taller, organized list of beautifully colored speech bubble cards)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "💬 MẪU CHAT NHANH (BẤM ĐỂ GỬI):" else "💬 QUICK CHAT TEMPLATES (TAP TO SEND):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8D6E63),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val chatCategories = listOf(
                            Triple(
                                if (isVi) "Vui vẻ 😄" else "Joy 😄",
                                EmoteType.LAUGH,
                                listOf(
                                    if (isVi) "Ha ha, quá tuyệt vời! 🎉" else "Haha, awesome! 🎉",
                                    if (isVi) "Đỉnh quá đi! 😎" else "So cool! 😎",
                                    if (isVi) "May mắn ghê nha! 🍀" else "So lucky! 🍀"
                                )
                            ),
                            Triple(
                                if (isVi) "Buồn bã 😭" else "Sad 😭",
                                EmoteType.CRY,
                                listOf(
                                    if (isVi) "Huhu, xui quá đi mất! 😭" else "Huhu, so unlucky! 😭",
                                    if (isVi) "Sao tớ đen thế nhỉ? 😢" else "Why am I so unfortunate? 😢",
                                    if (isVi) "Không công bằng tí nào! 💔" else "This is not fair! 💔"
                                )
                            ),
                            Triple(
                                if (isVi) "Tức giận 😡" else "Angry 😡",
                                EmoteType.ANGRY,
                                listOf(
                                    if (isVi) "Chờ đấy, tớ sẽ phục thù! 😡" else "Wait, I will get revenge! 😡",
                                    if (isVi) "Đừng có đùa với tớ! 😤" else "Don't play with me! 😤",
                                    if (isVi) "Chơi thế mà chơi à! 👿" else "Is that how you play?! 👿"
                                )
                            ),
                            Triple(
                                if (isVi) "Hài hước 🤣" else "Funny 🤣",
                                EmoteType.LAUGH,
                                listOf(
                                    if (isVi) "Chạy đi đâu con sâu! 🐛" else "Where are you running, little bug! 🐛",
                                    if (isVi) "Lêu lêu, bắt tớ đi nè! 😜" else "Teehee, catch me if you can! 😜",
                                    if (isVi) "Một bước lên mây luôn! 🚀" else "One step to the sky! 🚀"
                                )
                            )
                        )

                        // Styling for quick chat category bubbles
                        val categoryStyles = mapOf(
                            "Vui vẻ 😄" to Pair(Color(0xFFFFFDE7), Color(0xFFFFF59D)), // Yellow
                            "Joy 😄" to Pair(Color(0xFFFFFDE7), Color(0xFFFFF59D)),
                            "Buồn bã 😭" to Pair(Color(0xFFE3F2FD), Color(0xFF90CAF9)), // Blue
                            "Sad 😭" to Pair(Color(0xFFE3F2FD), Color(0xFF90CAF9)),
                            "Tức giận 😡" to Pair(Color(0xFFFFEBEE), Color(0xFFEF9A9A)), // Red
                            "Angry 😡" to Pair(Color(0xFFFFEBEE), Color(0xFFEF9A9A)),
                            "Hài hước 🤣" to Pair(Color(0xFFE8F5E9), Color(0xFFA5D6A7)), // Green
                            "Funny 🤣" to Pair(Color(0xFFE8F5E9), Color(0xFFA5D6A7))
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chatCategories.forEach { category ->
                                val catTitle = category.first
                                val catEmote = category.second
                                val phrases = category.third

                                val bubbleColors = categoryStyles[catTitle] ?: Pair(Color(0xFFF9F9F9), Color(0xFFE0E0E0))

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = catTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD81B60),
                                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp, top = 2.dp)
                                    )
                                    phrases.forEach { phrase ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                                .shadow(1.dp, RoundedCornerShape(10.dp))
                                                .background(bubbleColors.first, RoundedCornerShape(10.dp))
                                                .border(1.dp, bubbleColors.second, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    viewModel.throwEmoteWithChat(emote = catEmote, chatText = phrase)
                                                    showChatEmoteDialog = false
                                                }
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = catEmote.symbol,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = phrase,
                                                fontSize = 13.sp,
                                                color = Color(0xFF4E342E),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showChatEmoteDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text(
                                text = if (isVi) "ĐÓNG CHAT" else "CLOSE CHAT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // GAMEPLAY SETTINGS DIALOG (BGM & SFX TOGGLES)
        if (showGameplaySettings) {
            Dialog(onDismissRequest = { showGameplaySettings = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isVi) "Cài Đặt Game" else "Settings",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E342E)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Music toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "Nhạc Nền (BGM)" else "Background Music",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5D4037)
                            )
                            Switch(
                                checked = state.isMusicOn,
                                onCheckedChange = { viewModel.toggleMusic() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD81B60))
                            )
                        }

                        // SFX toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "Âm Thanh Hiệu Ứng" else "Sound Effects (SFX)",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5D4037)
                            )
                            Switch(
                                checked = state.isSfxOn,
                                onCheckedChange = { viewModel.toggleSfx() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD81B60))
                            )
                        }

                        // Language toggler
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVi) "Ngôn Ngữ" else "Language",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5D4037)
                            )
                            Row {
                                Button(
                                    onClick = { viewModel.setLanguage("vi") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isVi) Color(0xFFD81B60) else Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text("VI", color = Color.White)
                                }
                                Button(
                                    onClick = { viewModel.setLanguage("en") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isVi) Color(0xFFD81B60) else Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("EN", color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showGameplaySettings = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isVi) "ĐÓNG" else "CLOSE")
                        }
                    }
                }
            }
        }

        // MATCH COMPLETED WIN DIALOG
        if (state.status == GameStateStatus.MATCH_ENDED) {
            Dialog(onDismissRequest = { viewModel.exitToMainMenu() }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👑", fontSize = 64.sp)
                        Text(
                            text = state.bannerText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD81B60),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.matchRewardText.isNotEmpty()) state.matchRewardText else (if (isVi) "Phần thưởng của bạn: +500 Vàng 🍭" else "Reward: +500 Gold 🍭"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.exitToMainMenu() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isVi) "QUAY LẠI SẢNH CHỜ" else "BACK TO MAIN MENU")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerHudItem(
    player: LudoPlayer,
    activePlayer: LudoPlayer,
    timeLeft: Int,
    onClick: () -> Unit
) {
    val isActive = activePlayer.color == player.color
    val baseColor = player.color.sweetBaseColor()
    val accentColor = player.color.sweetAccentColor()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(if (isActive) 8.dp else 3.dp, RoundedCornerShape(16.dp))
            .background(accentColor, RoundedCornerShape(16.dp))
            .border(
                width = if (isActive) 3.dp else 2.dp,
                color = baseColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                CircularProgressIndicator(
                    progress = { timeLeft / 30f },
                    modifier = Modifier.fillMaxSize(),
                    color = baseColor,
                    strokeWidth = 3.dp
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.5.dp, baseColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                PlayerAvatar(
                    color = player.color,
                    avatarId = player.avatarId,
                    size = 28.dp,
                    fontSize = 18.sp,
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = player.name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = baseColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CutePlayerCard(
    player: LudoPlayer,
    isActive: Boolean,
    timeLeft: Int,
    align: Alignment,
    isGreenMascotStyle: Boolean = false,
    isBlueGuestStyle: Boolean = false,
    characterBitmaps: Map<String, ImageBitmap> = emptyMap(),
    onClick: () -> Unit
) {
    // High-contrast rich dark sweet-fudge backgrounds for perfect white text contrast!
    val cardBg = when (player.color) {
        PlayerColor.RED -> Color(0xFF6B1D20) // Rich dark cherry/strawberry
        PlayerColor.GREEN -> Color(0xFF1E4620) // Rich dark matcha green
        PlayerColor.YELLOW -> Color(0xFF5F4504) // Rich dark honey/caramel
        PlayerColor.BLUE -> Color(0xFF13325B) // Rich dark blueberry syrup
    }
    val cardBorder = when (player.color) {
        PlayerColor.RED -> Color(0xFFFF8A80)
        PlayerColor.GREEN -> Color(0xFF81C784)
        PlayerColor.YELLOW -> Color(0xFFFFD54F)
        PlayerColor.BLUE -> Color(0xFF64B5F6)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = align
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .shadow(if (isActive) 10.dp else 3.dp, RoundedCornerShape(20.dp))
                .background(cardBg, RoundedCornerShape(20.dp))
                .border(
                    width = if (isActive) 3.5.dp else 2.5.dp,
                    color = if (isActive) Color.White else cardBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
            val avatarScale by if (isActive) {
                infiniteTransition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.14f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "avatarScale"
                )
            } else {
                remember { androidx.compose.runtime.mutableStateOf(1.0f) }
            }

            val glowAlpha by if (isActive) {
                infiniteTransition.animateFloat(
                    initialValue = 0.25f,
                    targetValue = 0.75f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowAlpha"
                )
            } else {
                remember { androidx.compose.runtime.mutableStateOf(0f) }
            }

            // Square avatar box
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    // Pulsating glowing halo matching the player's primary border color!
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = avatarScale * 1.25f
                                scaleY = avatarScale * 1.25f
                                alpha = glowAlpha
                            }
                            .size(38.dp)
                            .background(cardBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    )
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = avatarScale
                            scaleY = avatarScale
                        }
                        .size(42.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(
                            if (isActive) 3.dp else 2.dp,
                            if (isActive) Color.White else cardBorder,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Circular active turn indicator wrapping around the avatar
                    if (isActive) {
                        val maxTime = if (timeLeft <= 5) 5f else 15f
                        CircularProgressIndicator(
                            progress = { (timeLeft / maxTime).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxSize().padding(2.dp),
                            color = cardBorder,
                            strokeWidth = 3.dp
                        )
                    }

                    // Load custom character images with safe fallback to emojis
                    val bitmap = characterBitmaps[player.characterSkin]
                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap,
                            contentDescription = player.name,
                            modifier = Modifier.size(34.dp)
                        )
                    } else {
                        PlayerAvatar(
                            color = player.color,
                            avatarId = -1, // Use color-specific themed default if custom image fails
                            size = 36.dp,
                            fontSize = 24.sp,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // White Player Name
            Text(
                text = player.name,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 4.dp),
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(1.5f, 1.5f),
                        blurRadius = 1f
                    )
                )
            )

            // Dynamic "1" badge for the Blue Guest player matching the mockup exactly
            if (isBlueGuestStyle) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFFFD93D), CircleShape)
                        .border(1.2.dp, Color(0xFFEBC02D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        color = Color(0xFF795548),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DrawDiceFace(value: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val dotRadius = size.width * 0.11f
        val offset = size.width * 0.24f

        // Helper to draw a high-end 3D glossy spherical dot
        fun drawGlossyDot(x: Float, y: Float) {
            // 1. Drop shadow under the dot
            drawCircle(
                color = Color(0x3D000000),
                radius = dotRadius * 1.15f,
                center = Offset(x + 1f, y + 1.5f)
            )
            // 2. Spherical dark core
            drawCircle(
                color = Color(0xFF261204),
                radius = dotRadius,
                center = Offset(x, y)
            )
            // 3. 3D Volume inner lighting (subtle gradient overlay simulation)
            drawCircle(
                color = Color(0xFF4A2A14),
                radius = dotRadius * 0.8f,
                center = Offset(x + dotRadius * 0.1f, y + dotRadius * 0.1f)
            )
            // 4. Strong glossy highlight reflection (white spot on top-left)
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = dotRadius * 0.28f,
                center = Offset(x - dotRadius * 0.35f, y - dotRadius * 0.35f)
            )
        }

        when (value) {
            1 -> drawGlossyDot(cx, cy)
            2 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx + offset, cy + offset)
            }
            3 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx, cy)
                drawGlossyDot(cx + offset, cy + offset)
            }
            4 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx + offset, cy - offset)
                drawGlossyDot(cx - offset, cy + offset)
                drawGlossyDot(cx + offset, cy + offset)
            }
            5 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx + offset, cy - offset)
                drawGlossyDot(cx, cy)
                drawGlossyDot(cx - offset, cy + offset)
                drawGlossyDot(cx + offset, cy + offset)
            }
            6 -> {
                drawGlossyDot(cx - offset, cy - offset)
                drawGlossyDot(cx + offset, cy - offset)
                drawGlossyDot(cx - offset, cy)
                drawGlossyDot(cx + offset, cy)
                drawGlossyDot(cx - offset, cy + offset)
                drawGlossyDot(cx + offset, cy + offset)
            }
        }
    }
}

@Composable
fun FloatingEmoteOverlay(activeEmote: ActiveEmote) {
    val align = when (activeEmote.playerColor) {
        PlayerColor.GREEN -> Alignment.TopStart
        PlayerColor.RED -> Alignment.BottomStart
        PlayerColor.YELLOW -> Alignment.TopEnd
        PlayerColor.BLUE -> Alignment.BottomEnd
    }

    val offsetX = when (activeEmote.playerColor) {
        PlayerColor.GREEN -> 160.dp
        PlayerColor.RED -> 160.dp
        PlayerColor.YELLOW -> (-160).dp
        PlayerColor.BLUE -> (-160).dp
    }
    
    val offsetY = when (activeEmote.playerColor) {
        PlayerColor.GREEN -> 24.dp
        PlayerColor.RED -> (-24).dp
        PlayerColor.YELLOW -> 24.dp
        PlayerColor.BLUE -> (-24).dp
    }

    val animatedY = remember { Animatable(0f) }
    val animatedAlpha = remember { Animatable(1.0f) }

    LaunchedEffect(Unit) {
        launch {
            animatedY.animateTo(
                targetValue = -40f,
                animationSpec = tween(2500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(1500)
            animatedAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(1000)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = align
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetX, y = offsetY + animatedY.value.dp)
                .graphicsLayer(alpha = animatedAlpha.value)
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.5.dp, activeEmote.playerColor.baseColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(activeEmote.emote.symbol, fontSize = 28.sp)
                if (!activeEmote.chatText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeEmote.chatText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4E342E),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFD81B60))
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

// ==========================================
// CANVASES & MATH FOR 2.5D ISOMETRIC GAME BOARD
// ==========================================

fun getIsometricCoords(
    x: Float,
    y: Float,
    z: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    zoom: Float
): Offset {
    val centerX = canvasWidth / 2f
    val centerY = canvasHeight / 2f
    
    // Constraint board size by height in landscape mode to prevent clipping at the top/bottom
    val boardDim = minOf(canvasWidth, canvasHeight * 1.55f)
    val tileWidth = (boardDim / 27.5f) * zoom
    val tileHeight = tileWidth * 0.58f

    // Center coordinates at index (7, 7)
    val cx = x - 7.5f
    val cy = y - 7.5f

    // Isometric math projection:
    // Screen X = center + (cx - cy) * tileWidth
    // Screen Y = center + (cx + cy) * tileHeight - z * heightOffset
    val screenX = centerX + (cx - cy) * tileWidth
    val screenY = centerY + (cx + cy) * tileHeight - z * tileHeight * 1.3f

    return Offset(screenX, screenY)
}

@Composable
fun LudoIsometricBoardCanvas(
    state: GameState,
    zoom: Float,
    characterBitmaps: Map<String, ImageBitmap>,
    onPawnClicked: (Int) -> Unit
) {
    val pawns = state.pawns
    val activePlayerIndex = state.activePlayerIndex
    val activePlayer = state.players.getOrNull(activePlayerIndex)

    val infiniteTransition = rememberInfiniteTransition(label = "pawnArrowBob")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Interactive clicking overlay detection
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(pawns, state.status) {
                    detectTapGestures { offset ->
                        // Detect if a pawn of active player was clicked
                        if (state.status == GameStateStatus.WAITING_FOR_MOVE && activePlayer != null) {
                            var clickedPawnId: Int? = null
                            var minDistance = 1500f // max touch radius in pixels sq

                            pawns.filter { it.color == activePlayer.color }.forEach { p ->
                                val coord = getPawnVisualCoords(p)
                                if (coord != null) {
                                    val scrCoord = getIsometricCoords(coord.first, coord.second, coord.third, width, height, zoom)
                                    val dist = (offset - scrCoord).getDistanceSquared()
                                    if (dist < minDistance) {
                                        minDistance = dist
                                        clickedPawnId = p.id
                                    }
                                }
                             }

                            clickedPawnId?.let { id ->
                                onPawnClicked(id)
                            }
                        }
                    }
                }
        ) {
            // 1. DRAW GIANT CAKE BOARD LAYERS (BACKGROUND 3D STRUCTURE)
            drawGiantCakeBoard(width, height, zoom)

            // 2. DRAW TRACK TILES (BISCUIT PATH AND SAFE FLOWERS)
            drawTrackTilesAndCages(width, height, zoom)

            // 3. DRAW SPECIAL GOAL PORTAL IN THE CENTER
            drawGoalPortalCenter(state, width, height, zoom)

            // 4. DRAW THE PAWNS (CUTE HOPPING RETRO CHARACTERS)
            drawCutePawns(pawns, state, width, height, zoom, characterBitmaps, bobOffset)
        }
    }
}

private fun getPawnVisualCoords(pawn: Pawn): Triple<Float, Float, Float>? {
    val color = pawn.color
    if (pawn.stepCount == -1) {
        val basePos = LudoBoardConfig.basePawnPositions[color]?.getOrNull(pawn.id) ?: return null
        return Triple(basePos.first, basePos.second, 1.1f)
    }

    if (pawn.stepCount == 56) {
        val c = LudoBoardConfig.centerHomes[color] ?: return null
        return Triple(c.first.toFloat(), c.second.toFloat(), 1.0f)
    }

    if (pawn.stepCount in 0..50) {
        val startIndex = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
        val idx = (startIndex + pawn.stepCount) % 52
        val c = LudoBoardConfig.outerTrack[idx]
        return Triple(c.first.toFloat(), c.second.toFloat(), 0.12f)
    }

    if (pawn.stepCount in 51..55) {
        val c = LudoBoardConfig.homePaths[color]?.getOrNull(pawn.stepCount - 51) ?: return null
        val progressHeight = 0.12f + (pawn.stepCount - 50) * 0.15f // ramp slope
        return Triple(c.first.toFloat(), c.second.toFloat(), progressHeight)
    }

    return null
}

// ==========================================
// COLOR AND SHAPE HELPERS FOR DESSERT THEME
// ==========================================

fun PlayerColor.sweetBaseColor(): Color = when (this) {
    PlayerColor.RED -> Color(0xFFE53935) // High-contrast richer cherry red
    PlayerColor.GREEN -> Color(0xFF2E7D32) // High-contrast richer matcha green
    PlayerColor.YELLOW -> Color(0xFFFBC02D) // High-contrast richer custard honey
    PlayerColor.BLUE -> Color(0xFF1565C0) // High-contrast richer blueberry jam
}

fun PlayerColor.sweetAccentColor(): Color = when (this) {
    PlayerColor.RED -> Color(0xFFFFCDD2) // Soft pink cream
    PlayerColor.GREEN -> Color(0xFFC8E6C9) // Soft matcha cream
    PlayerColor.YELLOW -> Color(0xFFFFF9C4) // Soft vanilla cream
    PlayerColor.BLUE -> Color(0xFFBBDEFB) // Soft blueberry cream
}

fun DrawScope.drawPastelStarCookie(center: Offset, radius: Float) {
    val path = Path()
    for (i in 0..9) {
        val r = if (i % 2 == 0) radius else radius * 0.45f
        val angle = i * 36 - 90
        val x = center.x + r * cos(angle * Math.PI / 180.0).toFloat()
        val y = center.y + r * sin(angle * Math.PI / 180.0).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    drawPath(path, SolidColor(Color(0xFFFFF0B5)))
    drawPath(path, SolidColor(Color(0xFFEBC02D)), style = Stroke(width = 2.5f.dp.toPx()))
}

fun DrawScope.drawCakeBasePedestal(
    centerX: Float,
    centerY: Float,
    w: Float,
    h: Float,
    color: PlayerColor
) {
    val baseColor = color.sweetBaseColor()
    val accentColor = color.sweetAccentColor()

    // Layer 1: Bottom Chocolate/Wafer Biscuit layer
    drawIsometricSlab(
        centerX = centerX,
        centerY = centerY + 14f,
        w = w,
        h = h,
        thickness = 10f,
        topBrush = SolidColor(Color(0xFF8D6E63)),
        leftBrush = SolidColor(Color(0xFF5D4037)),
        rightBrush = SolidColor(Color(0xFF4E342E))
    )

    // Layer 2: Dripping white cream layer
    drawIsometricSlab(
        centerX = centerX,
        centerY = centerY + 6f,
        w = w * 0.96f,
        h = h * 0.96f,
        thickness = 8f,
        topBrush = SolidColor(Color.White),
        leftBrush = SolidColor(Color(0xFFEEEEEE)),
        rightBrush = SolidColor(Color(0xFFDDDDDD))
    )
    
    // Draw 3D dripping circular drops on the left/right front sides
    val leftDropCount = 4
    for (i in 0..leftDropCount) {
        val t = i.toFloat() / leftDropCount
        val p3 = Offset(centerX - w * 0.48f, centerY + 6f)
        val p2 = Offset(centerX, centerY + h * 0.48f + 6f)
        val dx = p3.x + (p2.x - p3.x) * t
        val dy = p3.y + (p2.y - p3.y) * t + 8f
        drawCircle(Color(0xFFEEEEEE), radius = w * 0.05f, center = Offset(dx, dy))
    }
    val rightDropCount = 4
    for (i in 0..rightDropCount) {
        val t = i.toFloat() / rightDropCount
        val p2 = Offset(centerX, centerY + h * 0.48f + 6f)
        val p1 = Offset(centerX + w * 0.48f, centerY + 6f)
        val dx = p2.x + (p1.x - p2.x) * t
        val dy = p2.y + (p1.y - p2.y) * t + 8f
        drawCircle(Color(0xFFDDDDDD), radius = w * 0.05f, center = Offset(dx, dy))
    }

    // Layer 3: Top icing layer (colored)
    drawIsometricSlab(
        centerX = centerX,
        centerY = centerY,
        w = w * 0.92f,
        h = h * 0.92f,
        thickness = 6f,
        topBrush = SolidColor(accentColor),
        leftBrush = SolidColor(baseColor.darken(0.1f)),
        rightBrush = SolidColor(baseColor.darken(0.2f))
    )

    // Draw grid pattern decoration on top of the base icing
    drawOval(
        color = baseColor.copy(alpha = 0.3f),
        topLeft = Offset(centerX - w * 0.35f, centerY - h * 0.35f),
        size = Size(w * 0.7f, h * 0.7f),
        style = Stroke(width = 3f)
    )
}

fun DrawScope.drawGiantCakeBoard(width: Float, height: Float, zoom: Float) {
    val boardDim = minOf(width, height * 1.55f)
    val tw = (boardDim / 27.5f) * zoom
    val th = tw * 0.58f

    // Bottom Layer: giant base biscuit slab (covers the entire 15x15 tile grid comfortably with safety margin)
    val bottomSlabBrush = Brush.verticalGradient(listOf(Color(0xFF8D6E63), Color(0xFF5D4037)))
    val bottomSlabSideLeft = Brush.verticalGradient(listOf(Color(0xFF5D4037), Color(0xFF4E342E)))
    val bottomSlabSideRight = Brush.verticalGradient(listOf(Color(0xFF4E342E), Color(0xFF3E2723)))

    drawIsometricSlab(
        centerX = width / 2f,
        centerY = height / 2f + 25f, // Centered slightly better vertically
        w = tw * 32.5f,
        h = th * 32.5f,
        thickness = 26f,
        topBrush = bottomSlabBrush,
        leftBrush = bottomSlabSideLeft,
        rightBrush = bottomSlabSideRight
    )

    // Middle Layer: cream cake slab
    val midSlabBrush = Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFFFF0D0)))
    val midSlabSideLeft = Brush.verticalGradient(listOf(Color(0xFFE2C488), Color(0xFFC2A468)))
    val midSlabSideRight = Brush.verticalGradient(listOf(Color(0xFFC2A468), Color(0xFFA28448)))

    drawIsometricSlab(
        centerX = width / 2f,
        centerY = height / 2f + 5f, // Centered slightly better vertically
        w = tw * 30.5f,
        h = th * 30.5f,
        thickness = 22f,
        topBrush = midSlabBrush,
        leftBrush = midSlabSideLeft,
        rightBrush = midSlabSideRight
    )
}

fun DrawScope.drawTrackTilesAndCages(width: Float, height: Float, zoom: Float) {
    val boardDim = minOf(width, height * 1.55f)
    val tw = (boardDim / 27.5f) * zoom
    val th = tw * 0.58f

    // 1. DRAW TILE TRACK BISCUITS
    LudoBoardConfig.outerTrack.forEachIndexed { index, coord ->
        val isSafe = LudoBoardConfig.safeSpotIndices.contains(index)
        val center = getIsometricCoords(coord.first.toFloat(), coord.second.toFloat(), 0.08f, width, height, zoom)

        if (isSafe) {
            // Safe spots match adjacent player colors as cute flower cookies!
            val flowerColor = when (index) {
                1, 47 -> PlayerColor.GREEN.sweetBaseColor()
                8, 14 -> PlayerColor.YELLOW.sweetBaseColor()
                21, 27 -> PlayerColor.BLUE.sweetBaseColor()
                34, 40 -> PlayerColor.RED.sweetBaseColor()
                else -> Color(0xFF6BCB77)
            }
            drawIsometricFlower(center.x, center.y, tw * 1.1f, th * 1.1f, flowerColor)
        } else {
            // Neutral delicious cream biscuit tile 🍪
            drawIsometricBiscuit(center.x, center.y, tw * 1.1f, th * 1.1f, Color(0xFFFFF9C4))
        }
    }

    // 2. DRAW HIGH CORNER BASES (ELEVATED CAGES AS MULTI-LAYER DRIPPING CAKES)
    PlayerColor.values().forEach { color ->
        val baseArea = when (color) {
            PlayerColor.RED -> Pair(11.2f, 11.2f)
            PlayerColor.GREEN -> Pair(2.8f, 11.2f)
            PlayerColor.YELLOW -> Pair(2.8f, 2.8f)
            PlayerColor.BLUE -> Pair(11.2f, 2.8f)
        }

        val baseCenter = getIsometricCoords(baseArea.first, baseArea.second, 0.4f, width, height, zoom)
        
        // Multi-layered dripping dessert cake pedestal!
        drawCakeBasePedestal(
            centerX = baseCenter.x,
            centerY = baseCenter.y,
            w = tw * 5.2f,
            h = th * 5.2f,
            color = color
        )

        // Draw Prominent Arrow tile next to base
        val startIdx = LudoBoardConfig.playerStartTrackIndex[color] ?: 0
        val deployCoord = LudoBoardConfig.outerTrack[startIdx]
        val arrowCenter = getIsometricCoords(deployCoord.first.toFloat(), deployCoord.second.toFloat(), 0.1f, width, height, zoom)
        
        // Draw ordinary biscuit underneath, then curved deployment arrow on top
        drawIsometricBiscuit(arrowCenter.x, arrowCenter.y, tw * 1.1f, th * 1.1f, Color(0xFFFFF9C4))
        drawIsometricArrow(arrowCenter.x, arrowCenter.y, tw * 0.7f, th * 0.7f, color.sweetBaseColor())
    }

    // 3. DRAW RAMP HOME PATHS Winding Up to Central Pedestal
    PlayerColor.values().forEach { color ->
        val path = LudoBoardConfig.homePaths[color] ?: return@forEach
        path.forEachIndexed { index, coord ->
            val heightLevel = 0.12f + (index + 1) * 0.14f
            val center = getIsometricCoords(coord.first.toFloat(), coord.second.toFloat(), heightLevel, width, height, zoom)
            // Color coordinated home path tiles
            drawIsometricBiscuit(center.x, center.y, tw * 1.1f, th * 1.1f, color.sweetAccentColor())
        }
    }
}

fun DrawScope.drawGoalPortalCenter(state: GameState, width: Float, height: Float, zoom: Float) {
    val boardDim = minOf(width, height * 1.55f)
    val tw = (boardDim / 27.5f) * zoom
    val th = tw * 0.58f
    val center = getIsometricCoords(7.5f, 7.5f, 0.7f, width, height, zoom)

    // Giant central cake crown (Cream cake Victory pedestal!)
    drawIsometricSlab(
        centerX = center.x,
        centerY = center.y,
        w = tw * 3.4f,
        h = th * 3.4f,
        thickness = 14f,
        topBrush = SolidColor(Color(0xFFFFFDF0)),
        leftBrush = SolidColor(Color(0xFFF1948A)),
        rightBrush = SolidColor(Color(0xFFEC7063))
    )

    // Draw the 4 colored path entrance circles
    val r = tw * 0.18f
    drawCircle(PlayerColor.GREEN.sweetBaseColor(), radius = r, center = Offset(center.x - tw * 0.6f, center.y))
    drawCircle(PlayerColor.YELLOW.sweetBaseColor(), radius = r, center = Offset(center.x, center.y - th * 0.6f))
    drawCircle(PlayerColor.BLUE.sweetBaseColor(), radius = r, center = Offset(center.x + tw * 0.6f, center.y))
    drawCircle(PlayerColor.RED.sweetBaseColor(), radius = r, center = Offset(center.x, center.y + th * 0.6f))

    // DRAW THE DYNAMIC GLOWING SPACE PORTAL DOOR IF ANIMATING!
    val portalPawnColor = state.portalPawnColor
    if (portalPawnColor != null) {
        val pCoord = LudoBoardConfig.centerHomes[portalPawnColor]
        if (pCoord != null) {
            val portalCenter = getIsometricCoords(pCoord.first.toFloat(), pCoord.second.toFloat(), 1.1f, width, height, zoom)
            val progress = state.portalProgress
            val scale = if (progress < 0.3f) (progress / 0.3f) else if (progress > 0.8f) ((1.0f - progress) / 0.2f) else 1.0f
            val portalW = tw * 1.6f * scale
            val portalH = th * 2.8f * scale

            if (scale > 0.05f) {
                // Upright capsule space door
                drawRoundRect(
                    color = Color(0xFF1A0033).copy(alpha = 0.88f * scale.coerceIn(0f, 1f)),
                    topLeft = Offset(portalCenter.x - portalW / 2, portalCenter.y - portalH * 0.8f),
                    size = Size(portalW, portalH),
                    cornerRadius = CornerRadius(portalW / 2, portalW / 2)
                )
                // Glowing neon magenta border
                drawRoundRect(
                    color = Color(0xFFFF007F).copy(alpha = scale.coerceIn(0f, 1f)),
                    topLeft = Offset(portalCenter.x - portalW / 2, portalCenter.y - portalH * 0.8f),
                    size = Size(portalW, portalH),
                    cornerRadius = CornerRadius(portalW / 2, portalW / 2),
                    style = Stroke(width = 6f * scale)
                )
                // Innermost swirling cyan ring
                drawRoundRect(
                    color = Color(0xFF00FFFF).copy(alpha = scale.coerceIn(0f, 1f)),
                    topLeft = Offset(portalCenter.x - portalW * 0.35f, portalCenter.y - portalH * 0.65f),
                    size = Size(portalW * 0.7f, portalH * 0.7f),
                    cornerRadius = CornerRadius(portalW * 0.35f, portalW * 0.35f),
                    style = Stroke(width = 3f * scale)
                )
            }
        }
    }
}

sealed class LudoTileKey {
    data class Outer(val index: Int) : LudoTileKey()
    data class Home(val color: PlayerColor, val step: Int) : LudoTileKey()
    data class Base(val color: PlayerColor, val id: Int) : LudoTileKey()
}

fun getPawnTileKey(pawn: Pawn): LudoTileKey? {
    if (pawn.stepCount == -1) {
        return LudoTileKey.Base(pawn.color, pawn.id)
    }
    if (pawn.stepCount in 0..50) {
        val startIndex = LudoBoardConfig.playerStartTrackIndex[pawn.color] ?: 0
        val idx = (startIndex + pawn.stepCount) % 52
        return LudoTileKey.Outer(idx)
    }
    if (pawn.stepCount in 51..55) {
        return LudoTileKey.Home(pawn.color, pawn.stepCount)
    }
    return null
}

fun DrawScope.drawMyPawnArrow(cx: Float, cy: Float, scaleX: Float, scaleY: Float, hasBadge: Boolean, bobOffset: Float) {
    val tipY = if (hasBadge) {
        cy - 82f * scaleY + bobOffset
    } else {
        cy - 62f * scaleY + bobOffset
    }
    
    val arrowWidth = 12f * scaleX
    val arrowHeight = 14f * scaleY
    
    val path = Path().apply {
        moveTo(cx, tipY)
        lineTo(cx - arrowWidth / 2f, tipY - arrowHeight * 0.4f)
        lineTo(cx - arrowWidth * 0.2f, tipY - arrowHeight * 0.4f)
        lineTo(cx - arrowWidth * 0.2f, tipY - arrowHeight)
        lineTo(cx + arrowWidth * 0.2f, tipY - arrowHeight)
        lineTo(cx + arrowWidth * 0.2f, tipY - arrowHeight * 0.4f)
        lineTo(cx + arrowWidth / 2f, tipY - arrowHeight * 0.4f)
        close()
    }
    
    drawPath(
        path = path,
        color = Color(0xFF261912),
        style = Stroke(width = 2.5f)
    )
    
    drawPath(
        path = path,
        color = Color(0xFFFFEB3B)
    )
}

fun DrawScope.drawPawnGroupCountBadge(cx: Float, cy: Float, scaleX: Float, scaleY: Float, count: Int) {
    val badgeRadius = 9f * scaleX
    val badgeY = cy - 64f * scaleY
    
    drawCircle(
        color = Color(0xFF261912),
        radius = badgeRadius + 1.8f,
        center = Offset(cx, badgeY)
    )
    
    drawCircle(
        color = Color(0xFFE53935),
        radius = badgeRadius,
        center = Offset(cx, badgeY)
    )
    
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 10f * scaleX
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
        val textHeightOffset = (paint.descent() + paint.ascent()) / 2f
        canvas.nativeCanvas.drawText(
            count.toString(),
            cx,
            badgeY - textHeightOffset,
            paint
        )
    }
}

fun DrawScope.drawCutePawns(
    pawns: List<Pawn>,
    state: GameState,
    width: Float,
    height: Float,
    zoom: Float,
    characterBitmaps: Map<String, ImageBitmap> = emptyMap(),
    bobOffset: Float = 0f
) {
    val visiblePawns = pawns.filter { it.stepCount != 56 || (state.portalPawnId == it.id && state.portalPawnColor == it.color && state.portalProgress <= 0.8f) }
    val pawnGroupCounts = visiblePawns.mapNotNull { p ->
        val key = getPawnTileKey(p)
        if (key != null) p to key else null
    }.groupBy({ it.second }, { it.first })

    pawns.forEach { pawn ->
        var charScale = 1.0f
        var px = 0f
        var py = 0f
        var pz = 0f

        if (pawn.stepCount == 56) {
            if (state.portalPawnId == pawn.id && state.portalPawnColor == pawn.color) {
                // Currently jumping into the space portal!
                val progress = state.portalProgress
                val color = pawn.color
                val p55Coord = LudoBoardConfig.homePaths[color]?.getOrNull(4) ?: Pair(7, 7)
                val p56Coord = LudoBoardConfig.centerHomes[color] ?: Pair(7, 7)

                if (progress < 0.2f) {
                    px = p55Coord.first.toFloat()
                    py = p55Coord.second.toFloat()
                    pz = 0.85f
                    charScale = 1.0f
                } else if (progress > 0.8f) {
                    return@forEach // Hidden completely!
                } else {
                    val jumpProgress = (progress - 0.2f) / 0.6f
                    px = p55Coord.first.toFloat() + (p56Coord.first.toFloat() - p55Coord.first.toFloat()) * jumpProgress
                    py = p55Coord.second.toFloat() + (p56Coord.second.toFloat() - p55Coord.second.toFloat()) * jumpProgress
                    pz = 0.85f + (1.2f - 0.85f) * jumpProgress + 3.0f * sin(jumpProgress * Math.PI).toFloat()
                    charScale = 1.0f - jumpProgress
                }
            } else {
                return@forEach // Hidden!
            }
        } else {
            val visualCoords = getPawnVisualCoords(pawn) ?: return@forEach
            px = visualCoords.first
            py = visualCoords.second
            pz = visualCoords.third

            // Apply Hop animation offset
            if (pawn.isHopping) {
                val hopHeight = sin(pawn.hopProgress * Math.PI).toFloat() * 1.2f
                pz += hopHeight
            }

            // Apply bump fly-away high Y offset animation
            if (pawn.isBumping) {
                pz += 5.5f // fly high along Z-axis
            }
        }

        val center = getIsometricCoords(px, py, pz, width, height, zoom)

        // Draw Custom Cute Bird Pawn Character with responsive squash-and-stretch
        var scaleX = if (pawn.isHopping) (1f - sin(pawn.hopProgress * Math.PI).toFloat() * 0.15f) else 1f
        var scaleY = if (pawn.isHopping) (1f + sin(pawn.hopProgress * Math.PI).toFloat() * 0.15f) else 1f

        scaleX *= charScale
        scaleY *= charScale

        // Determine the character type skin
        val player = state.players.find { it.color == pawn.color }
        val activeCharType = player?.characterSkin ?: "char1"

        drawCuteBirdCharacter(
            cx = center.x,
            cy = center.y,
            color = pawn.color,
            scaleX = scaleX,
            scaleY = scaleY,
            isSelected = state.selectedPawnId == pawn.id && state.players[state.activePlayerIndex].color == pawn.color,
            charType = activeCharType,
            characterBitmaps = characterBitmaps
        )

        // Find grouping details for counts
        val groupKey = getPawnTileKey(pawn)
        val groupPawns = if (groupKey != null) pawnGroupCounts[groupKey] ?: emptyList() else emptyList()
        val groupSize = groupPawns.size
        val hasBadge = groupSize >= 2

        if (hasBadge) {
            drawPawnGroupCountBadge(cx = center.x, cy = center.y, scaleX = scaleX, scaleY = scaleY, count = groupSize)
        }

        // Add visual pointing down arrow on top of user's pawn (PlayerColor.RED is human)
        if (pawn.color == PlayerColor.RED) {
            drawMyPawnArrow(cx = center.x, cy = center.y, scaleX = scaleX, scaleY = scaleY, hasBadge = hasBadge, bobOffset = bobOffset)
        }
    }
}

fun DrawScope.drawIsometricSlab(
    centerX: Float,
    centerY: Float,
    w: Float,
    h: Float,
    thickness: Float,
    topBrush: Brush,
    leftBrush: Brush,
    rightBrush: Brush
) {
    // 4 corners of top face
    val p0 = Offset(centerX, centerY - h / 2) // top
    val p1 = Offset(centerX + w / 2, centerY)   // right
    val p2 = Offset(centerX, centerY + h / 2) // bottom
    val p3 = Offset(centerX - w / 2, centerY)   // left

    // 1. Top face
    val topPath = Path().apply {
        moveTo(p0.x, p0.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(topPath, topBrush)

    // 2. Left side face
    val leftPath = Path().apply {
        moveTo(p3.x, p3.y)
        lineTo(p2.x, p2.y)
        lineTo(p2.x, p2.y + thickness)
        lineTo(p3.x, p3.y + thickness)
        close()
    }
    drawPath(leftPath, leftBrush)

    // 3. Right side face
    val rightPath = Path().apply {
        moveTo(p2.x, p2.y)
        lineTo(p1.x, p1.y)
        lineTo(p1.x, p1.y + thickness)
        lineTo(p2.x, p2.y + thickness)
        close()
    }
    drawPath(rightPath, rightBrush)

    // 4. Elegant high-contrast dark outline around the top face for clean comic-styled board visual pop
    drawPath(topPath, SolidColor(Color(0xFF331B10)), style = Stroke(width = 1.8f.dp.toPx()))
}

fun DrawScope.drawIsometricBiscuit(cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    // Draw 3D isometric biscuit top
    drawIsometricSlab(
        centerX = cx,
        centerY = cy,
        w = w,
        h = h,
        thickness = 8f,
        topBrush = SolidColor(color),
        leftBrush = SolidColor(color.darken(0.15f)),
        rightBrush = SolidColor(color.darken(0.3f))
    )

    // Draw little biscuit indentations/dots (representing a sweet butter cracker!)
    val dx = w * 0.18f
    val dy = h * 0.18f
    val dotColor = color.darken(0.12f)
    val r = w * 0.04f
    drawCircle(dotColor, radius = r, center = Offset(cx - dx, cy - dy))
    drawCircle(dotColor, radius = r, center = Offset(cx + dx, cy - dy))
    drawCircle(dotColor, radius = r, center = Offset(cx - dx, cy + dy))
    drawCircle(dotColor, radius = r, center = Offset(cx + dx, cy + dy))
}

fun DrawScope.drawIsometricFlower(cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    // Draw standard biscuit underneath first
    drawIsometricBiscuit(cx, cy, w, h, Color(0xFFFFF9C4))
    
    // Draw flower petals on top
    val petalColor = color
    val r = w * 0.16f
    val offsetW = w * 0.15f
    val offsetH = h * 0.15f
    drawCircle(petalColor, radius = r, center = Offset(cx - offsetW, cy))
    drawCircle(petalColor, radius = r, center = Offset(cx + offsetW, cy))
    drawCircle(petalColor, radius = r, center = Offset(cx, cy - offsetH))
    drawCircle(petalColor, radius = r, center = Offset(cx, cy + offsetH))
    
    // Draw flower center cream
    drawCircle(Color(0xFFFFF9C4), radius = r * 0.8f, center = Offset(cx, cy))
}

fun DrawScope.drawIsometricArrow(cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    // Draw curved curved flow entry arrow on the biscuit
    val path = Path().apply {
        moveTo(cx, cy - h / 3f)
        quadraticBezierTo(cx + w / 3f, cy - h / 3f, cx + w / 3f, cy + h / 6f)
        lineTo(cx + w / 2f, cy + h / 6f)
        lineTo(cx + w / 4f, cy + h / 2f)
        lineTo(cx, cy + h / 6f)
        lineTo(cx + w / 6f, cy + h / 6f)
        quadraticBezierTo(cx + w / 6f, cy - h / 12f, cx, cy - h / 12f)
        close()
    }
    drawPath(path, SolidColor(color))
}

fun DrawScope.drawCuteBirdCharacter(
    cx: Float,
    cy: Float,
    color: PlayerColor,
    scaleX: Float,
    scaleY: Float,
    isSelected: Boolean,
    charType: String = "default",
    characterBitmaps: Map<String, ImageBitmap> = emptyMap()
) {
    // 35% increase in base size so characters look bold, cute, and large! Plus 20% larger as requested by the user.
    val radius = 28.8f * scaleX
    val heightFactor = 38.4f * scaleY

    // If selected, draw glowing ring underneath
    if (isSelected) {
        drawOval(
            color = Color(0xFFFFEB3B),
            topLeft = Offset(cx - radius * 1.6f, cy - radius * 0.8f),
            size = Size(radius * 3.2f, radius * 1.6f),
            style = Stroke(width = 5f)
        )
    }

    // Draw darker, larger shadow under the character for enhanced visual pop on dark backgrounds
    drawOval(
        color = Color.Black.copy(alpha = 0.28f),
        topLeft = Offset(cx - radius * 1.1f, cy - radius * 0.4f),
        size = Size(radius * 2.2f, radius * 0.8f)
    )

    val bitmap = characterBitmaps[charType]
    if (bitmap != null) {
        // Draw the lovely PNG character skin!
        val dstW = radius * 2.6f
        val aspectRatio = if (bitmap.width > 0 && bitmap.height > 0) {
            bitmap.width.toFloat() / bitmap.height.toFloat()
        } else {
            1.0f
        }
        val dstH = dstW / aspectRatio
        val left = cx - dstW / 2f
        val top = cy - heightFactor * 0.72f - dstH / 2f
        drawImage(
            image = bitmap,
            dstOffset = androidx.compose.ui.unit.IntOffset(left.toInt(), top.toInt()),
            dstSize = androidx.compose.ui.unit.IntSize(dstW.toInt(), dstH.toInt())
        )
        return
    }

    val activeCharType = if (color == PlayerColor.RED) charType else "default"

    if (activeCharType != "default" && color == PlayerColor.RED) {
        // DRAW CUSTOM PURCHASED CHARACTER!
        val bodyColor = when (activeCharType) {
            "bird" -> Color(0xFFFF7043) // Bright orange-red bird
            "pig" -> Color(0xFFF48FB1)  // Pink pig
            "cat" -> Color(0xFFFFCC80)  // Orange tabby cat
            "otter" -> Color(0xFF8D6E63) // Brown otter
            "monkey" -> Color(0xFF9E7860) // Brown monkey
            else -> Color(0xFF78909C)   // Grey-blue shark
        }

        // Body outline
        drawCircle(
            color = Color(0xFF1E0C06),
            radius = radius * 1.25f,
            center = Offset(cx, cy - heightFactor * 0.5f)
        )
        // Body
        drawCircle(
            color = bodyColor,
            radius = radius * 1.1f,
            center = Offset(cx, cy - heightFactor * 0.5f)
        )

        // CHARACTER SPECIFIC EXTRAS (Ears, Fins, combs)
        when (activeCharType) {
            "bird" -> {
                // Red comb/feathers on top
                drawCircle(Color(0xFFE51C23), radius = radius * 0.35f, center = Offset(cx, cy - heightFactor * 1.1f))
            }
            "pig" -> {
                // Floppy pig ears on top
                val leftEar = Path().apply {
                    moveTo(cx - radius * 0.9f, cy - heightFactor * 0.9f)
                    lineTo(cx - radius * 0.4f, cy - heightFactor * 1.1f)
                    lineTo(cx - radius * 0.8f, cy - heightFactor * 1.2f)
                    close()
                }
                val rightEar = Path().apply {
                    moveTo(cx + radius * 0.9f, cy - heightFactor * 0.9f)
                    lineTo(cx + radius * 0.4f, cy - heightFactor * 1.1f)
                    lineTo(cx + radius * 0.8f, cy - heightFactor * 1.2f)
                    close()
                }
                drawPath(leftEar, SolidColor(Color(0xFFF06292)))
                drawPath(rightEar, SolidColor(Color(0xFFF06292)))
            }
            "cat" -> {
                // Pointy cat ears on top
                val leftEar = Path().apply {
                    moveTo(cx - radius * 0.9f, cy - heightFactor * 0.8f)
                    lineTo(cx - radius * 0.3f, cy - heightFactor * 1.1f)
                    lineTo(cx - radius * 0.7f, cy - heightFactor * 1.3f)
                    close()
                }
                val rightEar = Path().apply {
                    moveTo(cx + radius * 0.9f, cy - heightFactor * 0.8f)
                    lineTo(cx + radius * 0.3f, cy - heightFactor * 1.1f)
                    lineTo(cx + radius * 0.7f, cy - heightFactor * 1.3f)
                    close()
                }
                drawPath(leftEar, SolidColor(Color(0xFFFB8C00)))
                drawPath(rightEar, SolidColor(Color(0xFFFB8C00)))
            }
            "otter" -> {
                // Small round ears
                drawCircle(Color(0xFF5D4037), radius = radius * 0.28f, center = Offset(cx - radius * 0.7f, cy - heightFactor * 1.0f))
                drawCircle(Color(0xFF5D4037), radius = radius * 0.28f, center = Offset(cx + radius * 0.7f, cy - heightFactor * 1.0f))
            }
            "monkey" -> {
                // Big round monkey ears on sides
                drawCircle(Color(0xFF8D6E63), radius = radius * 0.38f, center = Offset(cx - radius * 1.1f, cy - heightFactor * 0.5f))
                drawCircle(Color(0xFFF5E6D3), radius = radius * 0.22f, center = Offset(cx - radius * 1.1f, cy - heightFactor * 0.5f))
                drawCircle(Color(0xFF8D6E63), radius = radius * 0.38f, center = Offset(cx + radius * 1.1f, cy - heightFactor * 0.5f))
                drawCircle(Color(0xFFF5E6D3), radius = radius * 0.22f, center = Offset(cx + radius * 1.1f, cy - heightFactor * 0.5f))
            }
            "shark" -> {
                // Shark fin on top
                val fin = Path().apply {
                    moveTo(cx - radius * 0.3f, cy - heightFactor * 1.0f)
                    lineTo(cx + radius * 0.3f, cy - heightFactor * 1.0f)
                    lineTo(cx, cy - heightFactor * 1.4f)
                    close()
                }
                drawPath(fin, SolidColor(Color(0xFF546E7A)))
            }
        }

        // Eyes (Common cute style)
        val eyeY = cy - heightFactor * 0.55f
        val eyeXOffset = radius * 0.35f
        val eyeRadius = radius * 0.32f

        // Left Eye
        drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
        drawCircle(Color(0xFF212121), radius = eyeRadius * 0.55f, center = Offset(cx - eyeXOffset, eyeY))
        drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

        // Right Eye
        drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
        drawCircle(Color(0xFF212121), radius = eyeRadius * 0.55f, center = Offset(cx + eyeXOffset, eyeY))
        drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx + eyeXOffset - 1f, eyeY - 1f))

        // CHARACTER FACIAL FEATURES
        when (activeCharType) {
            "bird" -> {
                // Cute orange beak
                val beakPath = Path().apply {
                    moveTo(cx - radius * 0.2f, cy - heightFactor * 0.42f)
                    lineTo(cx + radius * 0.2f, cy - heightFactor * 0.42f)
                    lineTo(cx, cy - heightFactor * 0.26f)
                    close()
                }
                drawPath(beakPath, SolidColor(Color(0xFFFFA726)))
            }
            "pig" -> {
                // Pink snout with nostrils
                drawOval(
                    color = Color(0xFFF8BBD0),
                    topLeft = Offset(cx - radius * 0.35f, cy - heightFactor * 0.45f),
                    size = Size(radius * 0.7f, radius * 0.45f)
                )
                drawCircle(Color(0xFFC2185B), radius = radius * 0.06f, center = Offset(cx - radius * 0.12f, cy - heightFactor * 0.33f))
                drawCircle(Color(0xFFC2185B), radius = radius * 0.06f, center = Offset(cx + radius * 0.12f, cy - heightFactor * 0.33f))
            }
            "cat" -> {
                // Cat nose, mouth, whiskers
                drawCircle(Color(0xFFF48FB1), radius = radius * 0.1f, center = Offset(cx, cy - heightFactor * 0.42f))
                // Whiskers
                drawLine(Color(0xFF4E342E), Offset(cx - radius * 0.3f, cy - heightFactor * 0.4f), Offset(cx - radius * 0.8f, cy - heightFactor * 0.45f), strokeWidth = 2.5f)
                drawLine(Color(0xFF4E342E), Offset(cx - radius * 0.3f, cy - heightFactor * 0.35f), Offset(cx - radius * 0.8f, cy - heightFactor * 0.35f), strokeWidth = 2.5f)
                drawLine(Color(0xFF4E342E), Offset(cx + radius * 0.3f, cy - heightFactor * 0.4f), Offset(cx + radius * 0.8f, cy - heightFactor * 0.45f), strokeWidth = 2.5f)
                drawLine(Color(0xFF4E342E), Offset(cx + radius * 0.3f, cy - heightFactor * 0.35f), Offset(cx + radius * 0.8f, cy - heightFactor * 0.35f), strokeWidth = 2.5f)
            }
            "otter" -> {
                // Otter muzzle and whiskers
                drawCircle(Color(0xFFEEEEEE), radius = radius * 0.25f, center = Offset(cx - radius * 0.12f, cy - heightFactor * 0.38f))
                drawCircle(Color(0xFFEEEEEE), radius = radius * 0.25f, center = Offset(cx + radius * 0.12f, cy - heightFactor * 0.38f))
                drawCircle(Color.Black, radius = radius * 0.08f, center = Offset(cx, cy - heightFactor * 0.44f))
            }
            "monkey" -> {
                // Monkey mouth/muzzle
                drawOval(
                    color = Color(0xFFF5E6D3),
                    topLeft = Offset(cx - radius * 0.45f, cy - heightFactor * 0.45f),
                    size = Size(radius * 0.9f, radius * 0.42f)
                )
                // Happy curved smile line
                val smilePath = Path().apply {
                    moveTo(cx - radius * 0.22f, cy - heightFactor * 0.33f)
                    quadraticBezierTo(cx, cy - heightFactor * 0.20f, cx + radius * 0.22f, cy - heightFactor * 0.33f)
                }
                drawPath(smilePath, SolidColor(Color(0xFF4E342E)), style = Stroke(width = 3f))
            }
            "shark" -> {
                // Sharp white teeth and dangerous mouth
                val mouthPath = Path().apply {
                    moveTo(cx - radius * 0.35f, cy - heightFactor * 0.35f)
                    lineTo(cx + radius * 0.35f, cy - heightFactor * 0.35f)
                    lineTo(cx, cy - heightFactor * 0.2f)
                    close()
                }
                drawPath(mouthPath, SolidColor(Color(0xFF263238)))
                // Teeth
                drawCircle(Color.White, radius = radius * 0.08f, center = Offset(cx - radius * 0.15f, cy - heightFactor * 0.32f))
                drawCircle(Color.White, radius = radius * 0.08f, center = Offset(cx + radius * 0.15f, cy - heightFactor * 0.32f))
            }
        }
    } else {
        when (color) {
            PlayerColor.RED -> {
                // ==============================================
                // 1. RED TEAM: Brown/Grey Hen with Red Sash
                // ==============================================
                val mainBrown = Color(0xFF8D6E63) // Brown/grey body color
                val lightBeige = Color(0xFFF5E6D3) // Face zone color

                // Bold outline for rich comic visual pop and absolute legibility
                drawCircle(
                    color = Color(0xFF1E0C06),
                    radius = radius * 1.25f,
                    center = Offset(cx, cy - heightFactor * 0.5f)
                )

                // Body
                drawCircle(
                    color = mainBrown,
                    radius = radius * 1.1f,
                    center = Offset(cx, cy - heightFactor * 0.5f)
                )

                // Red comb on top (Two rounded lobes)
                drawCircle(Color(0xFFE51C23), radius = radius * 0.45f, center = Offset(cx - radius * 0.25f, cy - heightFactor * 1.1f))
                drawCircle(Color(0xFFE51C23), radius = radius * 0.45f, center = Offset(cx + radius * 0.25f, cy - heightFactor * 1.15f))

                // White/Light Beige Face mask area
                drawCircle(
                    color = lightBeige,
                    radius = radius * 0.85f,
                    center = Offset(cx, cy - heightFactor * 0.5f)
                )

                // Big white eyes with lashes
                val eyeY = cy - heightFactor * 0.58f
                val eyeXOffset = radius * 0.35f
                val eyeRadius = radius * 0.35f

                // Left Eye
                drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
                drawCircle(Color(0xFF4E342E), radius = eyeRadius * 0.5f, center = Offset(cx - eyeXOffset + 1f, eyeY + 1f))
                drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

                // Right Eye
                drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
                drawCircle(Color(0xFF4E342E), radius = eyeRadius * 0.5f, center = Offset(cx + eyeXOffset - 1f, eyeY + 1f))
                drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(cx + eyeXOffset - 2f, eyeY - 1f))

                // Lashes/liner
                drawLine(Color.Black, Offset(cx - eyeXOffset * 1.8f, eyeY - eyeRadius), Offset(cx - eyeXOffset * 1.2f, eyeY - eyeRadius * 1.3f), strokeWidth = 2.5f)
                drawLine(Color.Black, Offset(cx + eyeXOffset * 1.8f, eyeY - eyeRadius), Offset(cx + eyeXOffset * 1.2f, eyeY - eyeRadius * 1.3f), strokeWidth = 2.5f)

                // Orange beak
                val beakPath = Path().apply {
                    moveTo(cx - radius * 0.18f, cy - heightFactor * 0.45f)
                    lineTo(cx + radius * 0.18f, cy - heightFactor * 0.45f)
                    lineTo(cx, cy - heightFactor * 0.32f)
                    close()
                }
                drawPath(beakPath, SolidColor(Color(0xFFFF9800)))

                // Red wattles under the beak
                drawCircle(Color(0xFFE51C23), radius = radius * 0.18f, center = Offset(cx - radius * 0.08f, cy - heightFactor * 0.28f))
                drawCircle(Color(0xFFE51C23), radius = radius * 0.18f, center = Offset(cx + radius * 0.08f, cy - heightFactor * 0.28f))

                // Red sash diagonally draped
                val sashPath = Path().apply {
                    moveTo(cx - radius * 0.8f, cy - heightFactor * 0.35f)
                    lineTo(cx - radius * 0.5f, cy - heightFactor * 0.22f)
                    lineTo(cx + radius * 0.6f, cy - heightFactor * 0.55f)
                    lineTo(cx + radius * 0.8f, cy - heightFactor * 0.65f)
                    close()
                }
                drawPath(sashPath, SolidColor(Color(0xFFFF4081)))

                // Little Brown Wings
                drawOval(
                    color = mainBrown,
                    topLeft = Offset(cx - radius * 1.15f, cy - heightFactor * 0.58f),
                    size = Size(radius * 0.4f, radius * 0.75f)
                )
                drawOval(
                    color = mainBrown,
                    topLeft = Offset(cx + radius * 0.75f, cy - heightFactor * 0.58f),
                    size = Size(radius * 0.4f, radius * 0.75f)
                )
            }
        PlayerColor.GREEN -> {
            // ==============================================
            // 2. GREEN TEAM: Beige Pony with dark brown mane
            // ==============================================
            val bodyBeige = Color(0xFFF9E7D0) // Warm beige
            val maneBrown = Color(0xFF5D4037) // Dark chocolate mane
            val muzzlePink = Color(0xFFFFC0CB) // Pink muzzle

            // Bold outline for rich comic visual pop and absolute legibility
            drawCircle(
                color = Color(0xFF1E0C06),
                radius = radius * 1.25f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Body
            drawCircle(
                color = bodyBeige,
                radius = radius * 1.1f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Ears
            val leftEarPath = Path().apply {
                moveTo(cx - radius * 0.9f, cy - heightFactor * 0.8f)
                lineTo(cx - radius * 0.4f, cy - heightFactor * 0.85f)
                lineTo(cx - radius * 0.7f, cy - heightFactor * 1.25f)
                close()
            }
            drawPath(leftEarPath, SolidColor(bodyBeige))
            val leftEarInnerPath = Path().apply {
                moveTo(cx - radius * 0.8f, cy - heightFactor * 0.82f)
                lineTo(cx - radius * 0.5f, cy - heightFactor * 0.85f)
                lineTo(cx - radius * 0.65f, cy - heightFactor * 1.15f)
                close()
            }
            drawPath(leftEarInnerPath, SolidColor(muzzlePink))

            val rightEarPath = Path().apply {
                moveTo(cx + radius * 0.9f, cy - heightFactor * 0.8f)
                lineTo(cx + radius * 0.4f, cy - heightFactor * 0.85f)
                lineTo(cx + radius * 0.7f, cy - heightFactor * 1.25f)
                close()
            }
            drawPath(rightEarPath, SolidColor(bodyBeige))
            val rightEarInnerPath = Path().apply {
                moveTo(cx + radius * 0.8f, cy - heightFactor * 0.82f)
                lineTo(cx + radius * 0.5f, cy - heightFactor * 0.85f)
                lineTo(cx + radius * 0.65f, cy - heightFactor * 1.15f)
                close()
            }
            drawPath(rightEarInnerPath, SolidColor(muzzlePink))

            // Dark brown mane / hair (fringe on forehead and neck)
            drawCircle(maneBrown, radius = radius * 0.45f, center = Offset(cx, cy - heightFactor * 0.95f))
            drawCircle(maneBrown, radius = radius * 0.35f, center = Offset(cx - radius * 0.3f, cy - heightFactor * 0.92f))
            drawCircle(maneBrown, radius = radius * 0.35f, center = Offset(cx + radius * 0.3f, cy - heightFactor * 0.92f))

            // Big eyes
            val eyeY = cy - heightFactor * 0.55f
            val eyeXOffset = radius * 0.35f
            val eyeRadius = radius * 0.32f

            // Left Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(Color(0xFF4E342E), radius = eyeRadius * 0.55f, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

            // Right Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(Color(0xFF4E342E), radius = eyeRadius * 0.55f, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx + eyeXOffset - 1f, eyeY - 1f))

            // Prominent pink snout muzzle (oval at the bottom of face)
            drawOval(
                color = muzzlePink,
                topLeft = Offset(cx - radius * 0.65f, cy - heightFactor * 0.36f),
                size = Size(radius * 1.3f, radius * 0.6f)
            )
            // Smile line and 2 nostrils on muzzle
            drawCircle(Color(0xFFE51C23).copy(alpha = 0.5f), radius = radius * 0.05f, center = Offset(cx - radius * 0.18f, cy - heightFactor * 0.22f))
            drawCircle(Color(0xFFE51C23).copy(alpha = 0.5f), radius = radius * 0.05f, center = Offset(cx + radius * 0.18f, cy - heightFactor * 0.22f))
        }
        PlayerColor.YELLOW -> {
            // ==============================================
            // 3. YELLOW TEAM: Yellow Baby Chicken with brown sash
            // ==============================================
            val brightYellow = Color(0xFFFFD93D)
            val darkBrown = Color(0xFF4E342E)

            // Bold outline for rich comic visual pop and absolute legibility
            drawCircle(
                color = Color(0xFF1E0C06),
                radius = radius * 1.25f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Body
            drawCircle(
                color = brightYellow,
                radius = radius * 1.1f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Red comb/tuft on head (heart-like design)
            drawCircle(Color(0xFFE51C23), radius = radius * 0.38f, center = Offset(cx - radius * 0.1f, cy - heightFactor * 1.05f))
            drawCircle(Color(0xFFE51C23), radius = radius * 0.38f, center = Offset(cx + radius * 0.1f, cy - heightFactor * 1.05f))

            // Big cartoon eyes
            val eyeY = cy - heightFactor * 0.58f
            val eyeXOffset = radius * 0.35f
            val eyeRadius = radius * 0.35f

            // Left Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(darkBrown, radius = eyeRadius * 0.5f, center = Offset(cx - eyeXOffset + 1f, eyeY + 1f))
            drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

            // Right Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(darkBrown, radius = eyeRadius * 0.5f, center = Offset(cx + eyeXOffset - 1f, eyeY + 1f))
            drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(cx + eyeXOffset - 2f, eyeY - 1f))

            // Lashes
            drawLine(Color.Black, Offset(cx - eyeXOffset * 1.8f, eyeY - eyeRadius), Offset(cx - eyeXOffset * 1.2f, eyeY - eyeRadius * 1.3f), strokeWidth = 2.5f)
            drawLine(Color.Black, Offset(cx + eyeXOffset * 1.8f, eyeY - eyeRadius), Offset(cx + eyeXOffset * 1.2f, eyeY - eyeRadius * 1.3f), strokeWidth = 2.5f)

            // Orange beak
            val beakPath = Path().apply {
                moveTo(cx - radius * 0.18f, cy - heightFactor * 0.45f)
                lineTo(cx + radius * 0.18f, cy - heightFactor * 0.45f)
                lineTo(cx, cy - heightFactor * 0.32f)
                close()
            }
            drawPath(beakPath, SolidColor(Color(0xFFFF9800)))

            // Red wattles
            drawCircle(Color(0xFFE51C23), radius = radius * 0.18f, center = Offset(cx - radius * 0.08f, cy - heightFactor * 0.28f))
            drawCircle(Color(0xFFE51C23), radius = radius * 0.18f, center = Offset(cx + radius * 0.08f, cy - heightFactor * 0.28f))

            // Dark brown sash diagonally draped
            val sashPath = Path().apply {
                moveTo(cx - radius * 0.8f, cy - heightFactor * 0.35f)
                lineTo(cx - radius * 0.5f, cy - heightFactor * 0.22f)
                lineTo(cx + radius * 0.6f, cy - heightFactor * 0.55f)
                lineTo(cx + radius * 0.8f, cy - heightFactor * 0.65f)
                close()
            }
            drawPath(sashPath, SolidColor(darkBrown))

            // Little yellow wings
            drawOval(
                color = brightYellow,
                topLeft = Offset(cx - radius * 1.15f, cy - heightFactor * 0.58f),
                size = Size(radius * 0.4f, radius * 0.75f)
            )
            drawOval(
                color = brightYellow,
                topLeft = Offset(cx + radius * 0.75f, cy - heightFactor * 0.58f),
                size = Size(radius * 0.4f, radius * 0.75f)
            )
        }
        PlayerColor.BLUE -> {
            // ==============================================
            // 4. BLUE TEAM: Dark Brown Bull with horns
            // ==============================================
            val bullBrown = Color(0xFF4A342E) // Dark brown
            val snoutTan = Color(0xFF8D6E63) // Lighter brown/tan snout
            val hornCream = Color(0xFFFFF1C1) // Cream color for horns

            // Bold outline for rich comic visual pop and absolute legibility
            drawCircle(
                color = Color(0xFF150A05),
                radius = radius * 1.25f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Body
            drawCircle(
                color = bullBrown,
                radius = radius * 1.1f,
                center = Offset(cx, cy - heightFactor * 0.5f)
            )

            // Curved Horns on top of head
            val leftHornPath = Path().apply {
                moveTo(cx - radius * 0.5f, cy - heightFactor * 0.8f)
                quadraticBezierTo(cx - radius * 1.3f, cy - heightFactor * 1.1f, cx - radius * 1.0f, cy - heightFactor * 1.35f)
                quadraticBezierTo(cx - radius * 0.8f, cy - heightFactor * 1.1f, cx - radius * 0.2f, cy - heightFactor * 0.85f)
                close()
            }
            drawPath(leftHornPath, SolidColor(hornCream))

            val rightHornPath = Path().apply {
                moveTo(cx + radius * 0.5f, cy - heightFactor * 0.8f)
                quadraticBezierTo(cx + radius * 1.3f, cy - heightFactor * 1.1f, cx + radius * 1.0f, cy - heightFactor * 1.35f)
                quadraticBezierTo(cx + radius * 0.8f, cy - heightFactor * 1.1f, cx + radius * 0.2f, cy - heightFactor * 0.85f)
                close()
            }
            drawPath(rightHornPath, SolidColor(hornCream))

            // Big cartoon eyes
            val eyeY = cy - heightFactor * 0.55f
            val eyeXOffset = radius * 0.35f
            val eyeRadius = radius * 0.32f

            // Left Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(Color.Black, radius = eyeRadius * 0.55f, center = Offset(cx - eyeXOffset, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx - eyeXOffset - 1f, eyeY - 1f))

            // Right Eye
            drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(Color.Black, radius = eyeRadius * 0.55f, center = Offset(cx + eyeXOffset, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.22f, center = Offset(cx + eyeXOffset - 1f, eyeY - 1f))

            // Tan muzzle snout
            drawOval(
                color = snoutTan,
                topLeft = Offset(cx - radius * 0.65f, cy - heightFactor * 0.36f),
                size = Size(radius * 1.3f, radius * 0.6f)
            )
            // Nostrils
            drawCircle(Color(0xFF2D1500), radius = radius * 0.05f, center = Offset(cx - radius * 0.15f, cy - heightFactor * 0.22f))
            drawCircle(Color(0xFF2D1500), radius = radius * 0.05f, center = Offset(cx + radius * 0.15f, cy - heightFactor * 0.22f))
        }
    }
}
}

// Color darkening extension helper for 3D faces
fun Color.darken(factor: Float): Color {
    return Color(
        red = (this.red * (1f - factor)).coerceIn(0f, 1f),
        green = (this.green * (1f - factor)).coerceIn(0f, 1f),
        blue = (this.blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}
