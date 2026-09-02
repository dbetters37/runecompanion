package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.InventoryItem
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.ui.theme.*

data class ColorUnlock(
    val name: String,
    val colorInt: Long,
    val reqLevel: Int
)

data class AnimUnlock(
    val name: String,
    val emoji: String,
    val desc: String,
    val reqLevel: Int
)

val COLOR_PROGRESSION_UNLOCKS = listOf(
    ColorUnlock("Orange Flame", 0xFFFF5722, 1),
    ColorUnlock("Amber Gold", 0xFFFFC107, 3),
    ColorUnlock("Azure Flame", 0xFF00BCD4, 6),
    ColorUnlock("Deep Sea Blue", 0xFF2196F3, 9),
    ColorUnlock("Jade Emerald", 0xFF4CAF50, 12),
    ColorUnlock("Amethyst Purple", 0xFF9C27B0, 15),
    ColorUnlock("Magenta Rose", 0xFFE91E63, 18),
    ColorUnlock("Divine Gold", 0xFFFFD700, 21),
    ColorUnlock("Cosmic Cyan", 0xFF00E5FF, 24),
    ColorUnlock("Dragon Fire", 0xFFD50000, 27),
    ColorUnlock("Obsidian Spark", 0xFF37474F, 30),
    ColorUnlock("Pure White Flame", 0xFFFFFFFF, 33),
    ColorUnlock("Violet Nebula", 0xFF7C4DFF, 36),
    ColorUnlock("Solar Flare", 0xFFFF6E40, 39),
    ColorUnlock("Toxic Lime", 0xFF76FF03, 42),
    ColorUnlock("Electric Indigo", 0xFF304FFE, 45),
    ColorUnlock("Ruby Blood", 0xFFC62828, 48),
    ColorUnlock("Frost Blue", 0xFF80D8FF, 51),
    ColorUnlock("Plasma Pink", 0xFFFF4081, 54),
    ColorUnlock("Bronze Copper", 0xFFA1887F, 57),
    ColorUnlock("Silver Steel", 0xFFB0BEC5, 60),
    ColorUnlock("Golden Sun", 0xFFFFD54F, 63),
    ColorUnlock("Emerald Spark", 0xFF00E676, 66),
    ColorUnlock("Sapphire Wave", 0xFF2979FF, 69),
    ColorUnlock("Diamond Shine", 0xFFEA80FC, 72),
    ColorUnlock("Fire Opal", 0xFFFFAB40, 75),
    ColorUnlock("Dragonstone Glow", 0xFFAA00FF, 78),
    ColorUnlock("Onyx Void", 0xFF212121, 81),
    ColorUnlock("Zenyte Orange", 0xFFFF6D00, 84),
    ColorUnlock("Shadow Purple", 0xFF4A148C, 87),
    ColorUnlock("Prismatic Rainbow", 0xFFE040FB, 90),
    ColorUnlock("Sacred White", 0xFFFAFAFA, 93),
    ColorUnlock("Nether Flame", 0xFFDD2C00, 96),
    ColorUnlock("Eternal Master Flame", 0xFFFFD700, 99)
)

val ANIMATION_PROGRESSION_UNLOCKS = listOf(
    AnimUnlock("Flame Blast", "🔥", "Expanding burst of fiery embers", 1),
    AnimUnlock("Phoenix Rise", "🦅", "Rising fiery wings with trailing sparks", 10),
    AnimUnlock("Cosmic Ember Ring", "💫", "Spinning dual-color flame halo", 20),
    AnimUnlock("Inferno Vortex", "🌪️", "Swirling fiery vortex tornado", 30),
    AnimUnlock("Dragon Breath Ring", "🐉", "Pulsing diamond dragonfire shockwave", 40),
    AnimUnlock("Supernova Nova", "💥", "Radial explosion with star points", 50),
    AnimUnlock("Meteor Shower", "🌠", "Cascading streak of burning meteors", 60),
    AnimUnlock("Lightning Spark", "⚡", "Electric lightning bolt ember sparks", 70),
    AnimUnlock("Tidal Wave Fire", "🌊", "Rolling surge of fiery crests", 80),
    AnimUnlock("Astral Comet", "☄️", "Deep space cosmic comet blast", 90),
    AnimUnlock("Master Divine Flare", "👑", "Golden crown divine flare eruption", 99)
)

@Composable
fun FiremakingGuildTab(
    skillXpMap: Map<OsrsSkill, Long>,
    inventoryItems: List<InventoryItem>,
    bankItems: List<InventoryItem> = emptyList(),
    fmColor1: Int,
    fmColor2: Int,
    selectedFmAnimations: List<String>,
    onSetFmColors: (c1: Int, c2: Int) -> Unit,
    onToggleFmAnimation: (animName: String) -> Unit,
    onBurnLogs: (logItemId: String) -> Unit,
    onTapFiremakingXp: (xpAmount: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val fmXp = skillXpMap[OsrsSkill.FIREMAKING] ?: 0L
    val fmLevel = OsrsXpCalculator.getLevelForXp(fmXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel(fmLevel + 1)
    val currentLevelXp = OsrsXpCalculator.getXpForLevel(fmLevel)
    val progress = if (fmLevel >= 99) 1f else {
        ((fmXp - currentLevelXp).toFloat() / (nextLevelXp - currentLevelXp).toFloat()).coerceIn(0f, 1f)
    }

    var activeColorWheelSlot by remember { mutableIntStateOf(1) } // 1: Color 1, 2: Color 2
    var selectedProgressionSubTab by remember { mutableIntStateOf(0) } // 0: Colors, 1: Animations

    // 60fps campfire flame pulsation animation
    val infiniteTransition = rememberInfiniteTransition(label = "guild_campfire_60fps")
    val campfirePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "campfire_scale"
    )

    val unlockedColorsCount = COLOR_PROGRESSION_UNLOCKS.count { fmLevel >= it.reqLevel }
    val unlockedAnimsCount = ANIMATION_PROGRESSION_UNLOCKS.count { fmLevel >= it.reqLevel }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Header Banner ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = OsrsLeatherMedium,
            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔥", fontSize = 18.sp)
                        Column {
                            Text(
                                "Firemaking Guild",
                                color = OsrsGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Level $fmLevel Firemaking",
                                color = OsrsParchment,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    Surface(
                        color = Color(0xFF331100),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF5722))
                    ) {
                        Text(
                            "${String.format("%,d", fmXp)} XP",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF5722),
                    trackColor = Color(0xFF331100)
                )
            }
        }

        // --- Interactive Campfire Tap Arena ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1E120B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5722)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🔥 Sacred Campfire Tap Arena",
                    color = OsrsGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Tap campfire to kindle sacred fire! Tap size & effects scale with your Firemaking Level ($fmLevel/99).",
                    color = OsrsParchment,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                // Animated Campfire visual
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(fmColor1).copy(alpha = 0.8f),
                                    Color(fmColor2).copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(2.dp, Color(0xFFFF9800), CircleShape)
                        .clickable {
                            // Tapping triggers active unlocked flame colors & animation effects
                        }
                        .testTag("guild_campfire_tap_zone"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔥",
                        fontSize = (42 * campfirePulse).sp,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    "Tap Campfire to test your Flame Colors & Animations! (Burn logs below for XP)",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- PROGRESSION GUIDE: COLORS (1 per 3 lvls) & ANIMATIONS (1 per 10 lvls) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = OsrsLeatherMedium,
            border = androidx.compose.foundation.BorderStroke(2.dp, OsrsGold),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📖", fontSize = 18.sp)
                        Text(
                            "Firemaking Progression Guide",
                            color = OsrsGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = Color(0xFF382D1E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Colors: $unlockedColorsCount/34 • Anims: $unlockedAnimsCount/11",
                            color = Color(0xFFFFD700),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Sub-tab toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { selectedProgressionSubTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedProgressionSubTab == 0) OsrsRedFrame else Color(0xFF2C2018)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text("🎨 Colors (3 Lvls)", color = if (selectedProgressionSubTab == 0) OsrsTextYellow else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedProgressionSubTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedProgressionSubTab == 1) OsrsRedFrame else Color(0xFF2C2018)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text("✨ Animations (10 Lvls)", color = if (selectedProgressionSubTab == 1) OsrsTextYellow else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (selectedProgressionSubTab == 0) {
                    // COLOR SELECTION & PROGRESSION GUIDE
                    Text(
                        "Unlock 1 Flame Color every 3 Firemaking Levels! Choose Color 1 or Color 2 to equip:",
                        color = OsrsParchment,
                        fontSize = 10.5.sp
                    )

                    // Color 1 / Color 2 Slot Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { activeColorWheelSlot = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeColorWheelSlot == 1) Color(0xFF4E342E) else Color(0xFF2C1D18)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (activeColorWheelSlot == 1) Color(0xFFFFD700) else Color.Gray
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(fmColor1))
                                        .border(1.dp, Color.White, CircleShape)
                                )
                                Text("Equipping Color 1", fontSize = 11.sp, color = OsrsTextYellow)
                            }
                        }

                        Button(
                            onClick = { activeColorWheelSlot = 2 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeColorWheelSlot == 2) Color(0xFF4E342E) else Color(0xFF2C1D18)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (activeColorWheelSlot == 2) Color(0xFFFFD700) else Color.Gray
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(fmColor2))
                                        .border(1.dp, Color.White, CircleShape)
                                )
                                Text("Equipping Color 2", fontSize = 11.sp, color = OsrsTextYellow)
                            }
                        }
                    }

                    // Color Unlocks List
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        COLOR_PROGRESSION_UNLOCKS.chunked(3).forEach { rowUnlocks ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowUnlocks.forEach { unlock ->
                                    val isUnlocked = fmLevel >= unlock.reqLevel
                                    val isEquipped = (activeColorWheelSlot == 1 && fmColor1.toLong() == unlock.colorInt) ||
                                            (activeColorWheelSlot == 2 && fmColor2.toLong() == unlock.colorInt)

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(enabled = isUnlocked) {
                                                if (activeColorWheelSlot == 1) {
                                                    onSetFmColors(unlock.colorInt.toInt(), fmColor2)
                                                } else {
                                                    onSetFmColors(fmColor1, unlock.colorInt.toInt())
                                                }
                                            },
                                        color = when {
                                            isEquipped -> Color(0xFF3E2723)
                                            isUnlocked -> Color(0xFF1E120B)
                                            else -> Color(0xFF18100C)
                                        },
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            when {
                                                isEquipped -> Color(0xFFFFD700)
                                                isUnlocked -> Color(unlock.colorInt)
                                                else -> Color(0xFF332211)
                                            }
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isUnlocked) Color(unlock.colorInt) else Color.DarkGray)
                                                    .border(1.dp, if (isUnlocked) Color.White else Color.Gray, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!isUnlocked) {
                                                    Text("🔒", fontSize = 10.sp)
                                                }
                                            }

                                            Text(
                                                text = unlock.name,
                                                color = if (isUnlocked) OsrsTextYellow else Color.Gray,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )

                                            Text(
                                                text = if (isUnlocked) "Lvl ${unlock.reqLevel} ✓" else "Lvl ${unlock.reqLevel}",
                                                color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFFF8A80),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ANIMATION UNLOCKS (Every 10 levels)
                    Text(
                        "Unlock 1 Tap Particle Animation every 10 Firemaking Levels! Check to equip active tap animations:",
                        color = OsrsParchment,
                        fontSize = 10.5.sp
                    )

                    ANIMATION_PROGRESSION_UNLOCKS.forEach { anim ->
                        val isUnlocked = fmLevel >= anim.reqLevel
                        val isSelected = selectedFmAnimations.contains(anim.name)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isUnlocked) {
                                    onToggleFmAnimation(anim.name)
                                },
                            color = when {
                                !isUnlocked -> Color(0xFF140D0A)
                                isSelected -> Color(0xFF3E2723)
                                else -> Color(0xFF1E120B)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    !isUnlocked -> Color(0xFF332211)
                                    isSelected -> Color(0xFFFFD700)
                                    else -> Color(0xFF553311)
                                }
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isUnlocked) anim.emoji else "🔒", fontSize = 18.sp)
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = anim.name,
                                                color = if (isUnlocked) OsrsTextYellow else Color.Gray,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                color = if (isUnlocked) Color(0xFF1B5E20) else Color(0xFF3E2723),
                                                shape = RoundedCornerShape(3.dp)
                                            ) {
                                                Text(
                                                    text = "Lvl ${anim.reqLevel}",
                                                    color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFFF8A80),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(anim.desc, color = if (isUnlocked) OsrsParchment else Color.Gray, fontSize = 9.5.sp)
                                    }
                                }

                                if (isUnlocked) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onToggleFmAnimation(anim.name) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFFFF5722),
                                            uncheckedColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Pyre Log Altar & Wood Burning ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = OsrsLeatherMedium,
            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🪵 Pyre Log Altar (Burn Timber Logs)",
                    color = OsrsGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Burn timber logs directly from your backpack or bank for Firemaking XP & Sacred Ashes!",
                    color = OsrsParchment,
                    fontSize = 11.sp
                )

                val logItems = remember(inventoryItems, bankItems) {
                    (inventoryItems + bankItems)
                        .filter {
                            it.id.contains("logs") || it.id.contains("pyre") || it.name.contains("Logs", ignoreCase = true)
                        }
                        .filter { it.quantity > 0 }
                        .groupBy { it.id }
                        .mapValues { entry ->
                            val first = entry.value.first()
                            first.copy(quantity = entry.value.sumOf { it.quantity })
                        }.values.toList()
                }

                if (logItems.isEmpty()) {
                    Text(
                        "No timber logs found in inventory or bank! Chop logs in Woodcutting or buy from GE.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    logItems.forEach { item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1E120B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF553311)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(item.iconEmoji, fontSize = 20.sp)
                                    Column {
                                        Text("${item.name} (x${item.quantity})", color = OsrsTextYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Burn for Firemaking XP", color = OsrsParchment, fontSize = 10.sp)
                                    }
                                }

                                CooldownActionButton(
                                    onClick = { onBurnLogs(item.id) },
                                    cooldownMs = 1500L,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.testTag("burn_log_${item.id}")
                                ) {
                                    Text("Burn 🔥", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
