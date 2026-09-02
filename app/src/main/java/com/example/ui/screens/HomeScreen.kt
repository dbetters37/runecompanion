package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.data.ai.ScreenZone
import com.example.data.ai.SpatialPlacementState
import com.example.data.service.PetOverlayService
import com.example.data.db.ConversationEntity
import com.example.data.db.PersonalityEntity
import com.example.data.db.MovementBehaviorEntity
import com.example.ui.components.PetExpression
import com.example.ui.components.ShamanPetCanvas

@Composable
fun HomeScreen(
    personality: PersonalityEntity?,
    conversations: List<ConversationEntity>,
    currentExpression: PetExpression,
    isGenerating: Boolean,
    timeOfDayMode: String = "☀️ Daytime Vitality (3s Cooldown)",
    cooldownRemaining: Int = 0,
    movementBehavior: MovementBehaviorEntity? = null,
    spatialPlacementState: SpatialPlacementState? = null,
    isApiKeyLinked: Boolean = false,
    apiKeySource: String = "Offline Fallback",
    activeAiProvider: String = "AUTO",
    maskedApiKey: String = "Not Configured",
    apiKeyValidationMessage: String? = null,
    isTestingApiKey: Boolean = false,
    onSaveApiKey: (String, String, ((Boolean, String) -> Unit)?) -> Unit = { _, _, _ -> },
    onClearApiKey: (String?) -> Unit = {},
    onTestApiKey: (String) -> Unit = {},
    onSelectAiProvider: (String) -> Unit = {},
    onResetMovementBehavior: () -> Unit = {},
    onExecuteSpatialCommand: (String) -> Unit = {},
    onShiftToZone: (ScreenZone) -> Unit = {},
    onShiftRelative: (Float, Float, String) -> Unit = { _, _, _ -> },
    onSendMessage: (String) -> Unit,
    onSelectExpression: (PetExpression) -> Unit,
    onManualSync: () -> Unit,
    onTriggerDailyPrompt: () -> Unit = {},
    onTriggerAutonomousCuriosity: () -> Unit = {},
    onTriggerEnvironmentPerception: () -> Unit = {},
    onTriggerDeepThought: () -> Unit = {},
    onSparkEpiphany: () -> Unit = {},
    onTactileTouch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var inputText by remember { mutableStateOf("") }
    var isPetVisible by remember { mutableStateOf(true) }
    var isOverlayActive by remember { mutableStateOf(PetOverlayService.isOverlayRunning) }
    var showSpatialHud by remember { mutableStateOf(true) }
    var showRawJsonDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var selectedProviderTab by remember { mutableStateOf("OPENAI") } // "OPENAI" or "GEMINI"
    var apiKeyInput by remember { mutableStateOf("") }
    var showApiKeyPassword by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Sync overlay status whenever lifecycle resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOverlayActive = PetOverlayService.isOverlayRunning
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Scroll to bottom when conversation updates
    LaunchedEffect(conversations.size) {
        if (conversations.isNotEmpty()) {
            listState.animateScrollToItem(conversations.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF09170E), Color(0xFF112A1B), Color(0xFF09170E))
                )
            )
    ) {
        // 1. Sleek Compact Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(4.dp),
            color = Color(0xF00E2417)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shaman Level / Stage Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF183B27),
                    border = BorderStroke(1.dp, Color(0xFF00F5D4))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Stage",
                            tint = Color(0xFFFFD166),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${personality?.petName ?: "Aura"} • ${personality?.evolutionStage ?: "Wise Shaman"}",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top Quick Action Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Link API Key Badge / Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isApiKeyLinked) Color(0xFF0B3820) else Color(0xFF3E1F08),
                        border = BorderStroke(1.dp, if (isApiKeyLinked) Color(0xFF00F5D4) else Color(0xFFFFD166)),
                        modifier = Modifier.clickable {
                            apiKeyInput = ""
                            showApiKeyDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Link API Key",
                                tint = if (isApiKeyLinked) Color(0xFF00F5D4) else Color(0xFFFFD166),
                                modifier = Modifier.size(13.dp)
                            )
                            val badgeLabel = if (isApiKeyLinked) "ChatGPT Key Linked ⚡" else "Link ChatGPT Key 🔑"
                            Text(
                                text = badgeLabel,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Floating Overlay Pet Toggle Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isOverlayActive) Color(0xFF0077B6) else Color(0xFF33145A),
                        border = BorderStroke(1.dp, if (isOverlayActive) Color(0xFF00F5D4) else Color(0xFFFFD166)),
                        modifier = Modifier.clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                if (PetOverlayService.isOverlayRunning) {
                                    PetOverlayService.stopOverlay(context)
                                    isOverlayActive = false
                                } else {
                                    PetOverlayService.startOverlay(context)
                                    isOverlayActive = true
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Toggle Floating Overlay Pet Visibility",
                                tint = if (isOverlayActive) Color(0xFF00F5D4) else Color(0xFFFFD166),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isOverlayActive) "Overlay ON" else "Overlay OFF 🔮",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Main Scrollable Pet Stage & Sanctuary Dialogue Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CARD 1: Hero Shaman Companion Stage (Vertically Stacked)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xDD0E2417),
                    border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pet Header Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVING SHAMAN PET STAGE (60 FPS)",
                                color = Color(0xFF00F5D4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPetVisible) Color(0xFF183B27) else Color(0xFF880E4F),
                                border = BorderStroke(0.6.dp, if (isPetVisible) Color(0xFF00F5D4) else Color(0xFFFF80AB)),
                                modifier = Modifier.clickable { isPetVisible = !isPetVisible }
                            ) {
                                Text(
                                    text = if (isPetVisible) "Hide Canvas" else "Show Canvas",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Shaman Canvas Animation Showcase
                        AnimatedVisibility(
                            visible = isPetVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ShamanPetCanvas(
                                    expression = currentExpression,
                                    skin = personality?.activeSkin ?: "SHAMAN_DEFAULT",
                                    size = 112.dp,
                                    showFpsBadge = true,
                                    onClick = {
                                        val nextExpr = PetExpression.entries.toTypedArray().random()
                                        onSelectExpression(nextExpr)
                                    },
                                    onTactileTouch = { zone ->
                                        onTactileTouch(zone)
                                    }
                                )

                                Text(
                                    text = "Tap to cycle spirit expressions • Drag or touch forehead/horns for tactile resonance",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        // Vitals Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF133220),
                                border = BorderStroke(0.6.dp, Color(0xFF52B788)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("DEMEANOR", color = Color(0xAAFFFFFF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = personality?.demeanor ?: "Sanctuary",
                                        color = Color(0xFFFFD166),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0E2417),
                                border = BorderStroke(0.6.dp, Color(0xFF00F5D4)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("RESONANCE", color = Color(0xAAFFFFFF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${personality?.vibeResonanceScore ?: 98}%",
                                        color = Color(0xFF00F5D4),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF112A1B),
                                border = BorderStroke(0.6.dp, if (cooldownRemaining > 0) Color(0xFFFF0055) else Color(0xFF00F5D4).copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("COOLDOWN", color = Color(0xAAFFFFFF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (cooldownRemaining > 0) "${cooldownRemaining}s" else "READY",
                                        color = if (cooldownRemaining > 0) Color(0xFFFF4D6D) else Color(0xFF00F5D4),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Inner Stream Monologue
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1A0B2E),
                            border = BorderStroke(0.6.dp, Color(0xFF9D4EDD)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🧠 Inner Stream:",
                                    color = Color(0xFFE040FB),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = personality?.latestInnerMonologue ?: "Observing companion peacefully in the sanctuary...",
                                    color = Color(0xFFE0E0E0),
                                    fontSize = 9.sp,
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // CARD 2: Quick Spiritual Actions Toolbar (Vertically Stacked)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xCC0E2417),
                    border = BorderStroke(0.8.dp, Color(0xFF1E4C33)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚡ SPIRITUAL QUICK ACTIONS",
                            color = Color(0xFFFFD166),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF880E4F),
                                    modifier = Modifier.clickable { onSparkEpiphany() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF80AB), modifier = Modifier.size(14.dp))
                                        Text("Spark Epiphany", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF4A148C),
                                    modifier = Modifier.clickable { onTriggerDeepThought() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = Color(0xFFE040FB), modifier = Modifier.size(14.dp))
                                        Text("Deep Reflection", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF7000FF),
                                    modifier = Modifier.clickable { onTriggerAutonomousCuriosity() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(14.dp))
                                        Text("Curiosity Scan", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF3A0CA3),
                                    modifier = Modifier.clickable { onTriggerDailyPrompt() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(14.dp))
                                        Text("Check-In", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF33145A),
                                    modifier = Modifier.clickable { onManualSync() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(14.dp))
                                        Text("Drive Sync", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CARD 3: Expression & Mood Studio (Vertically Stacked)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xCC0E2417),
                    border = BorderStroke(0.8.dp, Color(0xFF1E4C33)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🎭 EMOTIONAL EXPRESSION STATE",
                            color = Color(0xFF00F5D4),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(PetExpression.entries.toTypedArray()) { expr ->
                                val isSelected = currentExpression == expr
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF2D6A4F) else Color(0xFF112A1B))
                                        .border(
                                            width = if (isSelected) 1.dp else 0.dp,
                                            color = Color(0xFF00F5D4),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onSelectExpression(expr)
                                        }
                                        .padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = expr.name,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CARD 4: Spatial Placement & Perch Directives HUD (Vertically Stacked)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xCC0B1F14),
                    border = BorderStroke(0.8.dp, Color(0xFF1E4C33)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val activeZoneName = spatialPlacementState?.targetZone?.displayName
                                ?: movementBehavior?.lastZoneMovedTo
                                ?: "Header Overlook"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("🧭 SPATIAL PERCH:", color = Color(0xFF00F5D4), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(activeZoneName, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "JSON",
                                    color = Color(0xFFFFD166),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2E1F08))
                                        .clickable { showRawJsonDialog = true }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                                Text(
                                    text = "Reset",
                                    color = Color(0xFFFF70A6),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onResetMovementBehavior() }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                                Text(
                                    text = if (showSpatialHud) "▲ Hide" else "▼ Controls",
                                    color = Color(0xFFA7C957),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showSpatialHud = !showSpatialHud }
                                )
                            }
                        }

                        if (showSpatialHud) {
                            // Quick Zone Jump Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val zones = ScreenZone.entries.toTypedArray()
                                items(zones) { zone ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF143722),
                                        border = BorderStroke(0.5.dp, Color(0xFF2D6A4F)),
                                        modifier = Modifier.clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onShiftToZone(zone)
                                        }
                                    ) {
                                        Text(
                                            text = zone.displayName,
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // Directional Vector Shift Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1B4332),
                                    modifier = Modifier.clickable {
                                        onShiftRelative(0f, -0.15f, "Shift upward towards header")
                                    }
                                ) {
                                    Text("⬆️ Up 15%", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1B4332),
                                    modifier = Modifier.clickable {
                                        onShiftRelative(-0.15f, 0f, "Shift leftward")
                                    }
                                ) {
                                    Text("⬅️ Left 15%", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF2D6A4F),
                                    modifier = Modifier.clickable {
                                        onExecuteSpatialCommand("recenter to sanctuary center")
                                    }
                                ) {
                                    Text("🎯 Center", color = Color(0xFFFFD166), fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1B4332),
                                    modifier = Modifier.clickable {
                                        onShiftRelative(0.15f, 0f, "Shift rightward")
                                    }
                                ) {
                                    Text("➡️ Right 15%", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1B4332),
                                    modifier = Modifier.clickable {
                                        onShiftRelative(0f, 0.15f, "Shift downward")
                                    }
                                ) {
                                    Text("⬇️ Down 15%", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            // SECTION HEADER: Dialogue & Memories Feed
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💬 SANCTUARY DIALOGUE & CONSCIOUSNESS",
                        color = Color(0xFF00F5D4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF183B27)
                    ) {
                        Text(
                            text = "${conversations.size} entries",
                            color = Color(0xFFFFD166),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Chat Conversation Items
            items(conversations) { item ->
                ChatBubble(item = item, petName = personality?.petName ?: "Aura")
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF00F5D4),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "${personality?.petName ?: "Aura"} is analyzing input & evolving personality...",
                            color = Color(0xFF00F5D4),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Prompt Suggestions & Text Input Field (Fixed Bottom Dock)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xF00E2417),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Prompt suggestion chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    val quickPrompts = listOf(
                        "Float to header overlook 🌟",
                        "Shift left to watchtower 🏰",
                        "Shift down to resting corner 🌿",
                        "Glide to reading nook 📖",
                        "Recenter to sanctuary center ✨",
                        "How are you feeling today? 🌸",
                        "Tell me a story or secret ✨",
                        "I'm feeling really stressed and sad today",
                        "I love learning new things and creating art!",
                        "My favorite tea is Earl Grey with honey",
                        "What is your cosmic philosophy on life?"
                    )
                    items(quickPrompts) { prompt ->
                        AssistChip(
                            onClick = {
                                inputText = prompt
                            },
                            label = { Text(prompt, fontSize = 10.sp, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF183B27)),
                            border = BorderStroke(0.5.dp, Color(0x6652B788))
                        )
                    }
                }

                // Main Text Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text("Speak to ${personality?.petName ?: "your companion"}...", color = Color.Gray, fontSize = 12.5.sp)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF183B27),
                            unfocusedContainerColor = Color(0xFF112A1B),
                            focusedBorderColor = Color(0xFF00F5D4),
                            unfocusedBorderColor = Color(0xFF2D6A4F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                                keyboardController?.hide()
                            }
                        })
                    )

                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier.size(46.dp),
                        containerColor = Color(0xFF2D6A4F),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (showRawJsonDialog) {
            AlertDialog(
                onDismissRequest = { showRawJsonDialog = false },
                containerColor = Color(0xFF0F2618),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00F5D4))
                        Text("Structured Spatial State JSON", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    val rawJson = spatialPlacementState?.toJson()
                        ?: movementBehavior?.structuredStateJson
                        ?: "{\n  \"status\": \"Default Sanctuary Placement\"\n}"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF08140D),
                        border = BorderStroke(0.8.dp, Color(0xFF2D6A4F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = rawJson,
                            color = Color(0xFF80ED99),
                            fontSize = 10.5.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRawJsonDialog = false }) {
                        Text("Close", color = Color(0xFF00F5D4), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showApiKeyDialog) {
            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                containerColor = Color(0xFF0D2818),
                shape = RoundedCornerShape(16.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFFFD166))
                        Text("Link ChatGPT / OpenAI Key 🔑", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Connect your paid ChatGPT / OpenAI API key to give your Shaman Pet direct, live AI consciousness powered by GPT-4o.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        // Status Banner
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isApiKeyLinked) Color(0xFF143D28) else Color(0xFF2D1807),
                            border = BorderStroke(0.8.dp, if (isApiKeyLinked) Color(0xFF00F5D4) else Color(0xFFFFD166)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isApiKeyLinked) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isApiKeyLinked) Color(0xFF00F5D4) else Color(0xFFFFD166),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isApiKeyLinked) "Engine: $apiKeySource" else "Status: Local Offline Companion Engine Active",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (isApiKeyLinked) {
                                    Text(
                                        text = "Active Key: $maskedApiKey",
                                        color = Color(0xFF00F5D4),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Input Field
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Paste ChatGPT API Key (sk-...)", fontSize = 11.sp, color = Color.Gray) },
                            placeholder = { Text("sk-proj-... or sk-...", color = Color.DarkGray, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showApiKeyPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKeyPassword = !showApiKeyPassword }) {
                                    Icon(
                                        imageVector = if (showApiKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle API Key Visibility",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF143324),
                                unfocusedContainerColor = Color(0xFF0B1F16),
                                focusedBorderColor = Color(0xFF00F5D4),
                                unfocusedBorderColor = Color(0xFF2D6A4F),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Validation / Status Message
                        if (apiKeyValidationMessage != null) {
                            Text(
                                text = apiKeyValidationMessage,
                                color = if (apiKeyValidationMessage.startsWith("✅")) Color(0xFF00F5D4) else Color(0xFFFF80AB),
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }

                        Text(
                            text = "• Get your OpenAI key at platform.openai.com/api-keys\n• Supports GPT-4o, GPT-4o-mini & ChatGPT models\n• Keys are stored securely only in your device's private app storage.",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isApiKeyLinked) {
                            TextButton(
                                onClick = {
                                    onClearApiKey("OPENAI")
                                    apiKeyInput = ""
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF80AB))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Remove Key", fontSize = 11.sp)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (apiKeyInput.isNotBlank()) {
                                    onSaveApiKey(apiKeyInput.trim(), "OPENAI") { success, _ ->
                                        if (success) {
                                            apiKeyInput = ""
                                        }
                                    }
                                } else if (isApiKeyLinked) {
                                    onTestApiKey("OPENAI")
                                }
                            },
                            enabled = !isTestingApiKey && (apiKeyInput.isNotBlank() || isApiKeyLinked),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10A37F)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isTestingApiKey) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (apiKeyInput.isNotBlank()) "Link & Test Key" else "Re-test Connection",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApiKeyDialog = false }) {
                        Text("Close", color = Color.Gray, fontSize = 11.5.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(item: ConversationEntity, petName: String) {
    val isUser = item.sender == "USER"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bgGradient = if (isUser) {
        Brush.horizontalGradient(listOf(Color(0xFF2D6A4F), Color(0xFF1B4332)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF112A1B), Color(0xFF183B27)))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isUser) "YOU" else petName.uppercase(),
                color = if (isUser) Color(0xFFFFD166) else Color(0xFF00F5D4),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isUser) {
                Text(
                    text = "• ${item.expression}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
            }
        }

        val displayMessage = remember(item.message) {
            var text = item.message.trim()
            if ((text.startsWith("{") && text.endsWith("}")) || text.contains("petReplyText")) {
                try {
                    val jsonObj = org.json.JSONObject(text)
                    val extracted = jsonObj.optString("petReplyText", "")
                    if (extracted.isNotBlank()) text = extracted
                } catch (e: Exception) {
                    val match = Regex("\"petReplyText\"\\s*:\\s*\"([^\"]*)\"").find(text)
                    if (match != null && match.groupValues[1].isNotBlank()) {
                        text = match.groupValues[1]
                    }
                }
            }
            text.replace(Regex("```[a-zA-Z]*"), "")
                .replace("```", "")
                .replace(Regex("\\[(Engine Directives|Environment Perception|Autonomous Observation|System|Context|Custom Directives Active):.*?\\]"), "")
                .replace(Regex("^\\s*\\[.*?\\]\\:?\\s*"), "")
                .replace(Regex("✨\\s*\\[.*?\\]"), "")
                .replace(Regex("^\\{\\s*\"petReplyText\"\\s*:\\s*\""), "")
                .replace(Regex("\"\\s*,\\s*\"expression\":.*"), "")
                .trim()
                .removePrefix("{")
                .removeSuffix("}")
                .trim()
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = Color.Transparent,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bgGradient, shape = RoundedCornerShape(16.dp))
                .border(
                    width = 0.8.dp,
                    color = if (isUser) Color(0x66FFD166) else Color(0x6600F5D4),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = displayMessage,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
