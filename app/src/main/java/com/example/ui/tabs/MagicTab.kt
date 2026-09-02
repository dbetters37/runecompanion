package com.example.ui.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel
import kotlinx.coroutines.delay

@Composable
fun IncantationsTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    MagicTab(viewModel = viewModel, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val activeIncantationIds by viewModel.activeIncantationIds.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()

    val incantXp = skillXpMap[OsrsSkill.MAGIC] ?: 0L
    val incantLvl = OsrsXpCalculator.getLevelForXp(incantXp)

    // Periodic ticker to keep countdowns accurate
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val maxSlots = viewModel.getMaxIncantationSlots()
    val activeCount = activeIncantationIds.size
    var showSlotGuideDialog by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf<IncantationCategory?>(null) }

    val filteredIncantations = remember(selectedCategory) {
        val list = if (selectedCategory == null) {
            IncantationsData.ALL_INCANTATIONS
        } else {
            IncantationsData.ALL_INCANTATIONS.filter { it.category == selectedCategory }
        }
        list.sortedBy { it.reqLevel }
    }

    val runeGlyphs = listOf("ᚦ", "ᛖ", "ᚱ", "ᛟ", "ᚹ", "ᛋ", "ᚲ", "ᚷ", "ᚨ", "ᛁ", "ᛚ", "ᛗ")

    // Slot Unlock Guide Dialog
    if (showSlotGuideDialog) {
        IncantationSlotsGuideDialog(
            viewModel = viewModel,
            currentTimeMs = currentTimeMs,
            onDismiss = { showSlotGuideDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF12151A))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- ANCIENT ALTAR HEADER STONE SLAB ---
        item {
            val progress = OsrsXpCalculator.getXpProgressToNextLevel(incantXp)
            val xpRemaining = OsrsXpCalculator.getXpRemainingForNextLevel(incantXp)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incantations_header_card"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1A2027),
                border = BorderStroke(2.dp, Color(0xFF3B4856)),
                shadowElevation = 6.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0xFF64748B))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Top Row: Altar Icon & Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🏛️", fontSize = 18.sp)
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Ancient Mystic Inscriptions",
                                        color = OsrsTextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                    Text(
                                        text = "Magic Skill Level $incantLvl",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Active Chants Status Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (activeCount > 0) Color(0xFF0A2E1E) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (activeCount > 0) Color(0xFF00FF9D) else Color(0xFF64748B))
                            ) {
                                Text(
                                    text = if (activeCount > 0) "✨ $activeCount/$maxSlots ACTIVE" else "✨ READY TO CHANT",
                                    color = if (activeCount > 0) Color(0xFF00FF9D) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // --- ACTIVE INCANTATION SLOTS STATUS & UNLOCK BANNER (CLICKABLE) ---
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showSlotGuideDialog = true
                                }
                                .testTag("incantation_slots_capacity_banner"),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF131922),
                            border = BorderStroke(1.dp, Color(0xFF0284C7))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🪄", fontSize = 14.sp)
                                        Text(
                                            text = "Active Incantation Slots: $activeCount / $maxSlots",
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF0369A1),
                                        border = BorderStroke(0.5.dp, Color(0xFF38BDF8))
                                    ) {
                                        Text(
                                            text = "ℹ️ How to Unlock",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Visual Slot Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val activeList = activeIncantationIds.toList()
                                    for (slotIdx in 0 until maxSlots) {
                                        val incantId = activeList.getOrNull(slotIdx)
                                        val incant = incantId?.let { id -> IncantationsData.ALL_INCANTATIONS.find { it.id == id } }
                                        val remainingMs = if (incantId != null) viewModel.getIncantationRemainingMs(incantId) else 0L
                                        val remMin = remainingMs / 60_000L
                                        val remSec = (remainingMs % 60_000L) / 1000L

                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (incant != null) Color(0xFF0F291E) else Color(0xFF1E2632),
                                            border = BorderStroke(1.dp, if (incant != null) Color(0xFF00FF9D) else Color(0xFF334155))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = incant?.iconEmoji ?: "✨",
                                                    fontSize = 11.sp
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = incant?.name ?: "Slot ${slotIdx + 1}: Empty",
                                                        color = if (incant != null) Color(0xFF86EFAC) else Color(0xFF94A3B8),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (incant != null) {
                                                        Text(
                                                            text = "⏳ ${remMin}m ${remSec}s left",
                                                            color = Color(0xFF00FF9D),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // XP Progress Bar & Remaining Details
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Magic XP Progress (${(progress * 100).toInt()}%)",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (incantLvl >= 99) "${incantXp} XP (MAX)" else "${incantXp} XP • ${xpRemaining} to Lvl ${incantLvl + 1}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress.coerceIn(0.001f, 1f))
                                        .fillMaxHeight()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF00FF9D))
                                            )
                                        )
                                )
                            }
                        }

                        Text(
                            text = "💡 Incantations consume Runes and last for 1 hour once chanted. You can run up to $maxSlots chants simultaneously (Tap banner above to see how to unlock more).",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // --- CATEGORY FILTER BAR ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF181D23),
                border = BorderStroke(1.dp, Color(0xFF2D3748))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedCategory = null
                        },
                        label = { Text("All (${IncantationsData.ALL_INCANTATIONS.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF12161D),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == null,
                            borderColor = Color(0xFF3B4856),
                            selectedBorderColor = Color(0xFF38BDF8)
                        )
                    )

                    IncantationCategory.entries.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val count = IncantationsData.ALL_INCANTATIONS.count { it.category == cat }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedCategory = if (isSelected) null else cat
                            },
                            label = { Text("${cat.iconEmoji} ${cat.displayName} ($count)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.colorHex),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF12161D),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF2D3748),
                                selectedBorderColor = Color(cat.colorHex)
                            )
                        )
                    }
                }
            }
        }

        // --- COMPACT INCANTATION CARDS WITH RUNE COSTS ---
        items(filteredIncantations, key = { it.id }) { incantation ->
            val meetsLevel = incantLvl >= incantation.reqLevel
            val isCurrentlyActive = activeIncantationIds.contains(incantation.id)
            val glyph = runeGlyphs[incantation.id.hashCode().mod(runeGlyphs.size)]
            val remainingMs = if (isCurrentlyActive) viewModel.getIncantationRemainingMs(incantation.id) else 0L
            val remMin = remainingMs / 60_000L
            val remSec = (remainingMs % 60_000L) / 1000L

            // Check Rune availability
            val hasAllRunes = viewModel.hasRunesForIncantation(incantation)
            val areSlotsFull = activeCount >= maxSlots && !isCurrentlyActive

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incantation_card_${incantation.id}"),
                shape = RoundedCornerShape(8.dp),
                color = if (isCurrentlyActive) Color(0xFF102820) else Color(0xFF181D23),
                border = BorderStroke(
                    width = if (isCurrentlyActive) 1.5.dp else 1.dp,
                    color = when {
                        isCurrentlyActive -> Color(0xFF00FF9D)
                        meetsLevel && hasAllRunes -> Color(0xFF3B4856)
                        else -> Color(0xFF2A1C18)
                    }
                ),
                shadowElevation = if (isCurrentlyActive) 4.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Header Row: Emoji, Name, Tier, Category, Level Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(incantation.iconEmoji, fontSize = 20.sp)

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = incantation.name,
                                        color = if (isCurrentlyActive) Color(0xFF00FF9D) else OsrsTextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )

                                    // Tier Badge
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = when (incantation.tier) {
                                            3 -> Color(0xFF4C1D95)
                                            2 -> Color(0xFF1E3A8A)
                                            else -> Color(0xFF1E293B)
                                        },
                                        border = BorderStroke(
                                            0.5.dp,
                                            when (incantation.tier) {
                                                3 -> Color(0xFFC084FC)
                                                2 -> Color(0xFF60A5FA)
                                                else -> Color(0xFF94A3B8)
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = "T${incantation.tier}",
                                            color = when (incantation.tier) {
                                                3 -> Color(0xFFE9D5FF)
                                                2 -> Color(0xFF93C5FD)
                                                else -> Color(0xFFCBD5E1)
                                            },
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = incantation.category.displayName,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.5.sp
                                )
                            }
                        }

                        // Level Requirement Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (meetsLevel) Color(0xFF0F2E1E) else Color(0xFF261D1A),
                            border = BorderStroke(1.dp, if (meetsLevel) Color(0xFF00FF9D) else Color(0xFFEF4444))
                        ) {
                            Text(
                                text = "Lv.${incantation.reqLevel}",
                                color = if (meetsLevel) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // --- RUNE COST ROW (2 types for T1/T2, 3 types for T3) ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10141B),
                        border = BorderStroke(0.5.dp, Color(0xFF2B3746))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Runes (${incantation.runes.size} types):",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                incantation.runes.forEach { req ->
                                    val count = viewModel.getRuneCount(req.runeItemId)
                                    val hasEnough = count >= req.quantity

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (hasEnough) Color(0xFF0D251A) else Color(0xFF2D1616),
                                        border = BorderStroke(0.5.dp, if (hasEnough) Color(0xFF059669) else Color(0xFFDC2626))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(req.runeEmoji, fontSize = 9.5.sp)
                                            Text(
                                                text = "${req.quantity} ${req.runeName.removeSuffix(" Rune")} (${count})",
                                                color = if (hasEnough) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Benefit / Stat Inscription Strip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        color = if (isCurrentlyActive) Color(0xFF0D251A) else Color(0xFF13171D),
                        border = BorderStroke(0.5.dp, if (isCurrentlyActive) Color(0xFF00FF9D).copy(alpha = 0.5f) else Color(0xFF2D3748))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(glyph, color = if (isCurrentlyActive) Color(0xFF00FF9D) else OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = incantation.benefitSummary,
                                color = if (isCurrentlyActive) Color(0xFF6EE7B7) else Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Action Bar: Invoke Button or Active/Locked Badge
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (areSlotsFull) {
                                showSlotGuideDialog = true
                            } else {
                                viewModel.toggleIncantation(incantation.id)
                            }
                        },
                        enabled = isCurrentlyActive || (meetsLevel && hasAllRunes && !areSlotsFull) || areSlotsFull,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .testTag("incantation_action_${incantation.id}"),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isCurrentlyActive -> Color(0xFF047857)
                                areSlotsFull -> Color(0xFF334155)
                                !meetsLevel -> Color(0xFF1E293B)
                                !hasAllRunes -> Color(0xFF451A03)
                                else -> Color(0xFF0284C7)
                            },
                            contentColor = when {
                                isCurrentlyActive -> Color(0xFFD1FAE5)
                                areSlotsFull -> Color(0xFF94A3B8)
                                !meetsLevel -> Color(0xFF64748B)
                                !hasAllRunes -> Color(0xFFFCA5A5)
                                else -> Color.White
                            },
                            disabledContainerColor = when {
                                areSlotsFull -> Color(0xFF1E293B)
                                !meetsLevel -> Color(0xFF181E24)
                                else -> Color(0xFF2A1C18)
                            },
                            disabledContentColor = when {
                                areSlotsFull -> Color(0xFF94A3B8)
                                !meetsLevel -> Color(0xFF64748B)
                                else -> Color(0xFFFCA5A5)
                            }
                        )
                    ) {
                        Text(
                            text = when {
                                isCurrentlyActive -> "✨ ACTIVE (${remMin}m ${remSec}s left)"
                                areSlotsFull -> "⏳ ALL $maxSlots SLOTS OCCUPIED (Tap for Guide)"
                                !meetsLevel -> "🔒 REQUIRES MAGIC LEVEL ${incantation.reqLevel}"
                                !hasAllRunes -> "🔮 MISSING RUNES (Craft or buy runes)"
                                else -> "🪄 INVOKE CHANT (1 HOUR DURATION)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncantationSlotsGuideDialog(
    viewModel: PetViewModel,
    currentTimeMs: Long,
    onDismiss: () -> Unit
) {
    val activeIncantationIds by viewModel.activeIncantationIds.collectAsStateWithLifecycle()
    val maxSlots = viewModel.getMaxIncantationSlots()
    val slotSources = viewModel.getIncantationSlotSources()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF151921),
            border = BorderStroke(2.dp, Color(0xFF38BDF8)),
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("incantation_slots_guide_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🪄", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Incantation Capacity",
                                color = OsrsTextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Active Slots: ${activeIncantationIds.size} / $maxSlots Chants",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF00FF9D))
                    ) {
                        Text(
                            text = "$maxSlots SLOTS",
                            color = Color(0xFF00FF9D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Divider(color = Color(0xFF2D3748), thickness = 1.dp)

                Text(
                    text = "Incantations are powerful mystic auras that run for 1 hour per invocation. By expanding your channeling capacity, you can have multiple incantations active at the same time!",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Currently Active Chants Section
                if (activeIncantationIds.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0D211A),
                        border = BorderStroke(1.dp, Color(0xFF059669))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Currently Active Chants (${activeIncantationIds.size}/$maxSlots):",
                                color = Color(0xFF86EFAC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            activeIncantationIds.forEach { id ->
                                val incant = IncantationsData.ALL_INCANTATIONS.find { it.id == id }
                                val remMs = viewModel.getIncantationRemainingMs(id)
                                val m = remMs / 60_000L
                                val s = (remMs % 60_000L) / 1000L

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${incant?.iconEmoji ?: "✨"} ${incant?.name ?: id}",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "⏳ ${m}m ${s}s",
                                        color = Color(0xFF00FF9D),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Unlock Guide Breakdown List
                Text(
                    text = "How to Unlock More Active Chant Slots:",
                    color = OsrsGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(slotSources, key = { it.id }) { source ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (source.isUnlocked) Color(0xFF0E241A) else Color(0xFF1B2028),
                            border = BorderStroke(
                                1.dp,
                                if (source.isUnlocked) Color(0xFF00FF9D) else Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(source.iconEmoji, fontSize = 18.sp)

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = source.title,
                                            color = if (source.isUnlocked) Color(0xFF86EFAC) else OsrsTextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = if (source.isUnlocked) Color(0xFF065F46) else Color(0xFF334155)
                                        ) {
                                            Text(
                                                text = if (source.isUnlocked) "✅ UNLOCKED" else "🔒 LOCKED",
                                                color = if (source.isUnlocked) Color(0xFFD1FAE5) else Color(0xFFCBD5E1),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = source.description,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.5.sp,
                                        lineHeight = 12.5.sp
                                    )

                                    Text(
                                        text = "🎯 ${source.requirementHint}",
                                        color = if (source.isUnlocked) Color(0xFF34D399) else Color(0xFFFBBF24),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("close_incantation_slots_guide"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Close Guide", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}
