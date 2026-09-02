@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.ui.tabs

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.models.AdventuringStoryData
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.data.models.SpiritPoolArea
import com.example.ui.components.CooldownActionButton
import com.example.ui.components.FinbarNpcBadge
import com.example.ui.components.BonusBreakdownDialog
import com.example.ui.components.BonusSourceDetail
import com.example.ui.components.dashedBorder
import com.example.ui.components.NauticalOceanicPanel
import com.example.ui.components.NauticalHeaderBanner
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

data class ShamanRawFishEntry(
    val item: com.example.data.models.InventoryItem,
    val totalQty: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShamanPoolTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val selectedSpiritPoolAreaId by viewModel.selectedSpiritPoolAreaId.collectAsStateWithLifecycle()
    val isAfkFishingActive by viewModel.isAfkFishingActive.collectAsStateWithLifecycle()
    val selectedFishId by viewModel.selectedFishId.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()

    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val adventuringMaxFloor by viewModel.adventuringMaxFloor.collectAsStateWithLifecycle()
    val npcFavorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    val finbarFavorLvl = npcFavorMap["finbar"]?.first ?: viewModel.getNpcFavorLevel("finbar")
    val completedQuestIds = petState.completedQuestIds

    val allFishIds = remember {
        AdventuringStoryData.SPIRIT_POOL_AREAS.flatMap { it.catchableFish }.map { it.id }.toSet()
    }
    val rawFishItems = remember(bankItems) {
        val itemsMap = mutableMapOf<String, Pair<com.example.data.models.InventoryItem, Int>>()
        bankItems.forEach { bItem ->
            if (bItem.quantity > 0 && (allFishIds.contains(bItem.id) || bItem.isRawUncookedFood || bItem.id.startsWith("item_raw_") || bItem.name.contains("Raw", true) || bItem.name.contains("Trout", true) || bItem.name.contains("Salmon", true) || bItem.name.contains("Shrimp", true))) {
                val normId = com.example.data.models.DefaultItems.normalizeItemId(bItem.id)
                val existing = itemsMap[normId]
                if (existing != null) {
                    itemsMap[normId] = Pair(existing.first, existing.second + bItem.quantity)
                } else {
                    itemsMap[normId] = Pair(bItem, bItem.quantity)
                }
            }
        }
        itemsMap.values.map { (item, totalQty) ->
            ShamanRawFishEntry(
                item = item,
                totalQty = totalQty
            )
        }.sortedByDescending { it.totalQty }
    }

    val fishingXp = skillXpMap[OsrsSkill.FISHING] ?: 0L
    val fishingLvl = OsrsXpCalculator.getLevelForXp(fishingXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel((fishingLvl + 1).coerceAtMost(99))
    val currentLevelBaseXp = OsrsXpCalculator.getXpForLevel(fishingLvl)
    val progress = if (fishingLvl >= 99) 1f else ((fishingXp - currentLevelBaseXp).toFloat() / (nextLevelXp - currentLevelBaseXp).coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

    val currentArea = remember(selectedSpiritPoolAreaId) {
        AdventuringStoryData.SPIRIT_POOL_AREAS.find { it.id == selectedSpiritPoolAreaId }
            ?: AdventuringStoryData.SPIRIT_POOL_AREAS.first()
    }

    var inspectedAreaForReqs by remember { mutableStateOf<SpiritPoolArea?>(null) }
    var inspectedFishForDetails by remember { mutableStateOf<com.example.data.models.SpiritFish?>(null) }
    var showBonusBreakdownDialog by remember { mutableStateOf(false) }

    val onSelectOrInspectArea: (String) -> Unit = { areaId ->
        val area = AdventuringStoryData.SPIRIT_POOL_AREAS.find { it.id == areaId }
        val hasTotem = area?.reqTotemId == null || viewModel.isTotemUnlocked(area.reqTotemId)
        val isUnlocked = area != null && fishingLvl >= area.reqLevel && hasTotem
        if (area != null && !isUnlocked) {
            inspectedAreaForReqs = area
        } else {
            viewModel.setSpiritPoolArea(areaId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OsrsLeatherDark)
                .padding(6.dp)
                .testTag("tab_shaman_pool"),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
        // --- COMPACT FISHING HEADER CARD ---
        item {
            NauticalOceanicPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shaman_pool_header_card"),
                accentIcon = "🌊",
                borderColor = Color(0xFF64B5F6),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Row 1: Title, Level & Bonus Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🌊", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "Shaman Pool",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OsrsTextYellow
                                )
                                Text(
                                    text = "Lv. $fishingLvl Fishing • ${"%,d".format(fishingXp)} XP",
                                    fontSize = 10.sp,
                                    color = OsrsParchment
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF1976D2).copy(alpha = 0.4f),
                            border = BorderStroke(0.8.dp, Color(0xFF64B5F6)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clickable { showBonusBreakdownDialog = true }
                                .testTag("badge_extra_fish_chance")
                        ) {
                            Text(
                                text = "+${finbarFavorLvl}% Extra Fish ⓘ",
                                color = Color(0xFFBBDEFB),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Row 2: AFK Fishing Toggle Button
                    CooldownActionButton(
                        onClick = { viewModel.toggleAfkFishing() },
                        cooldownMs = 600L,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAfkFishingActive) Color(0xFF1B5E20) else Color(0xFF3E2723)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (isAfkFishingActive) Color(0xFF81C784) else OsrsGold),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        Text(
                            text = if (isAfkFishingActive) "⚡ STOP AFK FISHING" else "🎣 START AFK FISHING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAfkFishingActive) Color.White else OsrsTextYellow,
                            maxLines = 1
                        )
                    }

                    // Compact Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = OsrsGold,
                            trackColor = OsrsLeatherDark
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OsrsTextYellow
                        )
                    }
                }
            }

        // --- ALWAYS VISIBLE INTERACTIVE SPIRIT REALM WORLD MAP ---
        item {
            ShamanPoolWorldMapCard(
                selectedAreaId = selectedSpiritPoolAreaId,
                userFishingLevel = fishingLvl,
                isTotemUnlocked = { viewModel.isTotemUnlocked(it) },
                onSelectArea = onSelectOrInspectArea,
                onLongPressArea = { inspectedAreaForReqs = it }
            )
        }

        // --- COMPACT LOCATION SELECTOR CHIPS ---
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AdventuringStoryData.SPIRIT_POOL_AREAS, key = { it.id }) { area ->
                    val isSelected = area.id == selectedSpiritPoolAreaId
                    val isFishingHere = isAfkFishingActive && isSelected
                    val hasTotem = viewModel.isTotemUnlocked(area.reqTotemId)
                    val hasLevel = fishingLvl >= area.reqLevel
                    val isAccessible = hasLevel && hasTotem
                    val isObeliskLocked = !hasTotem
                    val isLevelLocked = !hasLevel && hasTotem

                    val chipBg = when {
                        isFishingHere -> Color(0xFF1B5E20)
                        isSelected -> Color(0xFF3B2A1D)
                        isAccessible -> Color(0xFF162B18)
                        isObeliskLocked || isLevelLocked -> Color(0xFF2C260D)
                        else -> Color(0xFF2B1212)
                    }

                    val chipBorder = when {
                        isFishingHere -> Color(0xFF81C784)
                        isSelected -> OsrsGold
                        isAccessible -> Color(0xFF81C784)
                        isObeliskLocked || isLevelLocked -> Color(0xFFFFD54F)
                        else -> Color(0xFFE57373)
                    }

                    val isDotted = isObeliskLocked || isLevelLocked
                    val chipAlpha = if (isObeliskLocked) 0.30f else 1.0f

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = chipBg,
                        border = if (!isDotted) BorderStroke(
                            width = if (isFishingHere || isSelected) 1.dp else 0.5.dp,
                            color = chipBorder
                        ) else null,
                        modifier = Modifier
                            .alpha(chipAlpha)
                            .then(
                                if (isDotted) Modifier.dashedBorder(
                                    width = if (isFishingHere || isSelected) 1.dp else 0.5.dp,
                                    color = chipBorder,
                                    shape = RoundedCornerShape(4.dp),
                                    dashLength = 3.dp,
                                    gapLength = 3.dp
                                ) else Modifier
                            )
                            .clickable { onSelectOrInspectArea(area.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(area.emoji, fontSize = 11.sp)
                            Text(
                                area.name,
                                color = if (isFishingHere) Color.White else if (isSelected) OsrsTextYellow else if (isAccessible) OsrsTextWhite else if (isObeliskLocked || isLevelLocked) Color(0xFFFFF59D) else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            if (isObeliskLocked) {
                                Text(
                                    "🔒${area.reqTotemEmoji ?: "🗿"}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else if (isFishingHere) {
                                Text("⚡AFK", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                            } else {
                                Text(
                                    "Lv.${area.reqLevel}",
                                    color = if (isAccessible) Color(0xFF81C784) else if (isLevelLocked) Color(0xFFFFD54F) else Color(0xFFE57373),
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CATCHABLE FISH SPECIES IN THIS AREA (SLAYER TASK STYLE STACKED ROWS) ---
        item {
            val areaHasTotem = viewModel.isTotemUnlocked(currentArea.reqTotemId)
            val areaHasLevel = fishingLvl >= currentArea.reqLevel
            val areaUnlocked = areaHasLevel && areaHasTotem
            val isFishingThisArea = isAfkFishingActive && selectedSpiritPoolAreaId == currentArea.id

            val areaBgColor = when {
                isFishingThisArea -> Color(0xFF1B2E1B)
                areaUnlocked -> Color(0xFF162B18)
                !areaHasTotem || !areaHasLevel -> Color(0xFF2C260D)
                else -> Color(0xFF2B1212)
            }
            val areaBorderColor = when {
                isFishingThisArea -> Color(0xFF81C784)
                areaUnlocked -> Color(0xFF81C784)
                !areaHasTotem || !areaHasLevel -> Color(0xFFFFD54F)
                else -> Color(0xFFE57373)
            }

            val isAreaObeliskLocked = !areaHasTotem
            val isAreaLevelLocked = !areaHasLevel && areaHasTotem
            val isAreaDotted = isAreaObeliskLocked || isAreaLevelLocked
            val areaCardAlpha = if (isAreaObeliskLocked) 0.30f else 1.0f

            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = areaBgColor
                ),
                border = if (!isAreaDotted) BorderStroke(1.dp, areaBorderColor) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(areaCardAlpha)
                    .then(
                        if (isAreaDotted) Modifier.dashedBorder(
                            width = 1.dp,
                            color = areaBorderColor,
                            shape = RoundedCornerShape(6.dp),
                            dashLength = 4.dp,
                            gapLength = 4.dp
                        ) else Modifier
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Area Title & Compact AFK Fishing Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "${currentArea.emoji} ${currentArea.name}",
                                color = OsrsTextYellow,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = Color(0xFF3E2723),
                                border = BorderStroke(0.5.dp, if (areaUnlocked) Color(0xFF81C784) else if (!areaHasTotem || !areaHasLevel) Color(0xFFFFD54F) else Color(0xFFE57373))
                            ) {
                                Text(
                                    if (areaUnlocked) "Lv.${currentArea.reqLevel} ✓" else if (!areaHasTotem) "🔒 ${currentArea.reqTotemEmoji ?: "🗿"} Needed" else "Lv.${currentArea.reqLevel}",
                                    color = if (areaUnlocked) Color(0xFF81C784) else if (!areaHasTotem || !areaHasLevel) Color(0xFFFFD54F) else Color(0xFFE57373),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                )
                            }
                            if (isFishingThisArea) {
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = Color(0xFF1B5E20),
                                    border = BorderStroke(0.5.dp, Color(0xFFFFD54F))
                                ) {
                                    Text(
                                        "⚡ ACTIVE",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                    )
                                }
                            }
                        }

                        // Toggle switch for this specific area
                        CooldownActionButton(
                            onClick = {
                                viewModel.toggleAfkShamanPoolFishing(currentArea.id)
                            },
                            cooldownMs = 600L,
                            enabled = areaUnlocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFishingThisArea) Color(0xFF1B5E20) else Color(0xFF3E2723),
                                disabledContainerColor = Color(0xFF261912)
                            ),
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(0.5.dp, if (isFishingThisArea) Color(0xFF81C784) else OsrsGold),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = if (isFishingThisArea) "⚡ STOP" else "🎣 AFK FISH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFishingThisArea) Color.White else OsrsTextYellow
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF3E2D1F), thickness = 0.5.dp)

                    currentArea.catchableFish.forEach { fish ->
                        val canCatch = fishingLvl >= fish.reqLevel && areaUnlocked
                        val isSelectedTarget = selectedFishId == fish.id
                        val cookedHp = getCookedHp(fish.id)
                        val ownedQty = remember(inventoryItems, bankItems, fish.id) {
                            (inventoryItems.find { it.id == fish.id }?.quantity ?: 0) + 
                            (bankItems.find { it.id == fish.id }?.quantity ?: 0)
                        }

                        val dropBadgeColor = when {
                            fish.dropChancePercent >= 40 -> Color(0xFF4CAF50)
                            fish.dropChancePercent >= 20 -> Color(0xFF00ACC1)
                            fish.dropChancePercent >= 10 -> Color(0xFFFFB300)
                            else -> Color(0xFFFF7043)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(
                                    color = when {
                                        isSelectedTarget -> Color(0xFF3D2A1B)
                                        canCatch -> Color(0xFF24180F)
                                        else -> Color(0xFF19110B)
                                    },
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .border(
                                    width = if (isSelectedTarget) 1.dp else 0.5.dp,
                                    color = if (isSelectedTarget) OsrsGold else Color(0xFF3E2D1F),
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (canCatch) {
                                            viewModel.setSelectedFishId(fish.id)
                                        } else {
                                            inspectedFishForDetails = fish
                                        }
                                    },
                                    onLongClick = {
                                        inspectedFishForDetails = fish
                                    }
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(fish.emoji, fontSize = 13.sp)

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = fish.name,
                                        color = if (canCatch) OsrsTextYellow else Color(0xFF8D6E63),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Drop Chance Badge
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = dropBadgeColor.copy(alpha = 0.2f),
                                        border = BorderStroke(0.5.dp, dropBadgeColor)
                                    ) {
                                        Text(
                                            text = "${fish.dropChancePercent}%",
                                            color = dropBadgeColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                                        )
                                    }
                                    if (isSelectedTarget) {
                                        Text("🎯", fontSize = 8.sp)
                                    }
                                    Text("ℹ️", fontSize = 7.5.sp, color = Color.Gray)
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Lv.${fish.reqLevel} • +${fish.xp}XP • +${cookedHp}HP",
                                        color = Color(0xFFA1887F),
                                        fontSize = 8.sp,
                                        maxLines = 1
                                    )
                                    if (ownedQty > 0) {
                                        Text(
                                            "• Owned: $ownedQty",
                                            color = Color(0xFF00B4D8),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            CooldownActionButton(
                                onClick = {
                                    if (canCatch) {
                                        viewModel.setSelectedFishId(fish.id)
                                        viewModel.fishAtPohPond(targetFishId = fish.id)
                                    } else {
                                        inspectedFishForDetails = fish
                                    }
                                },
                                cooldownMs = 1500L,
                                enabled = canCatch,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelectedTarget) Color(0xFF1B5E20) else Color(0xFF3E2723),
                                    disabledContainerColor = Color(0xFF261912)
                                ),
                                shape = RoundedCornerShape(2.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .width(58.dp)
                                    .height(20.dp)
                            ) {
                                Text(
                                    text = if (canCatch) "Catch 🎣" else "🔒 Lv ${fish.reqLevel}",
                                    color = if (canCatch) Color.White else Color(0xFF6D4C41),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- COMPACT RAW FISH RESERVES STRIP ---
        item {
            NauticalOceanicPanel(
                borderColor = Color(0xFF1E88E5),
                modifier = Modifier.fillMaxWidth(),
                accentIcon = "🧺",
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧺", fontSize = 12.sp)
                            Text(
                                "FISH RESERVES",
                                color = OsrsTextYellow,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val totalFishCount = rawFishItems.sumOf { it.totalQty }
                        Text(
                            "$totalFishCount Total",
                            color = Color(0xFF81C784),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (rawFishItems.isEmpty()) {
                        Text(
                            "No raw fish in inventory or bank yet. Catch fish above!",
                            color = Color(0xFF8D6E63),
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rawFishItems.forEach { fishEntry ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF150E09),
                                    border = BorderStroke(0.5.dp, Color(0xFF4D3726))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(fishEntry.item.iconEmoji, fontSize = 12.sp)
                                        Text(
                                            fishEntry.item.name,
                                            color = OsrsTextWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Surface(
                                            color = OsrsGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(3.dp),
                                            border = BorderStroke(0.5.dp, OsrsGold)
                                        ) {
                                            Text(
                                                "x${fishEntry.totalQty}",
                                                color = OsrsTextYellow,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Finbar NPC Companion with 4-corner move & minimize
        FinbarNpcBadge(
            viewModel = viewModel,
            currentTabContext = "Shaman Pool",
            modifier = Modifier.fillMaxSize()
        )
    }

    if (inspectedAreaForReqs != null) {
        val area = inspectedAreaForReqs!!
        val hasLevel = fishingLvl >= area.reqLevel
        val hasTotem = area.reqTotemId == null || viewModel.isTotemUnlocked(area.reqTotemId)
        val isFullUnlocked = hasLevel && hasTotem

        SpiritPoolAreaRequirementsDialog(
            area = area,
            userFishingLevel = fishingLvl,
            hasTotem = hasTotem,
            isFullUnlocked = isFullUnlocked,
            onDismiss = { inspectedAreaForReqs = null },
            onSelectArea = {
                viewModel.setSpiritPoolArea(area.id)
                inspectedAreaForReqs = null
            }
        )
    }

    if (inspectedFishForDetails != null) {
        val fish = inspectedFishForDetails!!
        val invQty = inventoryItems.find { it.id == fish.id }?.quantity ?: 0
        val bankQty = bankItems.find { it.id == fish.id }?.quantity ?: 0

        FishDetailsDialog(
            fish = fish,
            userFishingLevel = fishingLvl,
            invQty = invQty,
            bankQty = bankQty,
            onDismiss = { inspectedFishForDetails = null },
            onCatchNow = {
                viewModel.setSelectedFishId(fish.id)
                viewModel.fishAtPohPond(targetFishId = fish.id)
                inspectedFishForDetails = null
            }
        )
    }

    if (showBonusBreakdownDialog) {
        BonusBreakdownDialog(
            title = "Extra Fish Bonus Chance",
            categoryName = "Shaman Pool & Spirit Waters",
            iconEmoji = "🐟",
            sources = listOf(
                BonusSourceDetail(
                    title = "Finbar the Spirit Angler's Favor (Lv. $finbarFavorLvl)",
                    description = "Grants +1% chance per favor level to catch an extra bonus fish and spirit ingredients from Shaman Pool waters (Up to +50%).",
                    bonusPercent = finbarFavorLvl,
                    emoji = "👻",
                    isUnlocked = true
                )
            ),
            note = "When triggered, an additional fish is caught in the same fishing attempt without consuming extra bait!",
            onDismiss = { showBonusBreakdownDialog = false }
        )
    }
}

/**
 * Interactive OSRS-styled World Map showing Spirit Pool Fishing Areas.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShamanPoolWorldMapCard(
    selectedAreaId: String,
    userFishingLevel: Int,
    isTotemUnlocked: (String?) -> Boolean,
    onSelectArea: (String) -> Unit,
    onLongPressArea: (SpiritPoolArea) -> Unit
) {
    val areas = AdventuringStoryData.SPIRIT_POOL_AREAS

    val infiniteTransition = rememberInfiniteTransition(label = "map_ping")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121B24)),
        border = BorderStroke(1.dp, OsrsGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Map Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🗺️", fontSize = 14.sp)
                    Text(
                        "Spirit Realm Waters Map",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("↔️ Scrollable", color = Color(0xFFB0BEC5), fontSize = 9.sp)
                    Text("•", color = Color.Gray, fontSize = 9.sp)
                    Text(
                        "Tap pin to travel",
                        color = Color(0xFF00B4D8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Map Horizontal Scroll Container
            val horizontalScrollState = rememberScrollState()
            val mapWidthDp = 760.dp
            val mapHeightDp = 560.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color(0xFF2C3E50), RoundedCornerShape(6.dp))
                    .horizontalScroll(horizontalScrollState)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = mapWidthDp, height = mapHeightDp)
                        .background(Color(0xFF0A131C))
                ) {
                    // Background Image Layer for Spirit Realm Waters
                    Image(
                        painter = painterResource(id = R.drawable.img_shaman_pool_map_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.38f)
                    )

                    // 1. Canvas layer for water texture grid & connecting paths
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthPx = size.width
                        val heightPx = size.height

                        // Draw grid lines
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val gridColor = Color(0xFF182A3A)

                        var gx = 50f
                        while (gx < widthPx) {
                            drawLine(gridColor, Offset(gx, 0f), Offset(gx, heightPx), pathEffect = dashEffect)
                            gx += 60f
                        }
                        var gy = 50f
                        while (gy < heightPx) {
                            drawLine(gridColor, Offset(0f, gy), Offset(widthPx, gy), pathEffect = dashEffect)
                            gy += 60f
                        }

                        // Draw connecting spirit path lines between areas
                        val path = Path()
                        areas.forEachIndexed { index, area ->
                            val px = area.posXRatio * widthPx
                            val py = area.posYRatio * heightPx
                            if (index == 0) {
                                path.moveTo(px, py)
                            } else {
                                path.lineTo(px, py)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF00B4D8).copy(alpha = 0.4f),
                            style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
                        )
                    }

                    // 2. Map Title Watermark
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            "🧭 Spirit Realm Archipelago",
                            color = Color(0xFF2C4A63),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Render Location Pins
                    areas.forEach { area ->
                        val isSelected = area.id == selectedAreaId
                        val hasTotem = isTotemUnlocked(area.reqTotemId)
                        val hasLevel = userFishingLevel >= area.reqLevel
                        val isAccessible = hasLevel && hasTotem
                        val isObeliskLocked = !hasTotem
                        val isLevelLocked = !hasLevel && hasTotem

                        val pinBgColor = when {
                            isSelected -> Color(0xFF1B4332)
                            isAccessible -> Color(0xFF162B18)
                            isObeliskLocked || isLevelLocked -> Color(0xFF2C260D)
                            else -> Color(0xFF2B1212)
                        }

                        val pinBorderColor = when {
                            isSelected -> OsrsGold.copy(alpha = pulseAlpha)
                            isAccessible -> Color(0xFF81C784)
                            isObeliskLocked || isLevelLocked -> Color(0xFFFFD54F)
                            else -> Color(0xFFE57373)
                        }

                        val isDotted = isObeliskLocked || isLevelLocked
                        val pinAlpha = if (isObeliskLocked) 0.30f else 1.0f

                        val pinXDp = (mapWidthDp * area.posXRatio - 45.dp).coerceIn(4.dp, mapWidthDp - 110.dp)
                        val pinYDp = (mapHeightDp * area.posYRatio - 22.dp).coerceIn(4.dp, mapHeightDp - 50.dp)

                        Box(
                            modifier = Modifier
                                .offset(x = pinXDp, y = pinYDp)
                                .alpha(pinAlpha)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = pinBgColor,
                                border = if (!isDotted) BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = pinBorderColor
                                ) else null,
                                shadowElevation = if (isSelected) 6.dp else 2.dp,
                                modifier = Modifier
                                    .then(
                                        if (isDotted) Modifier.dashedBorder(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = pinBorderColor,
                                            shape = RoundedCornerShape(8.dp),
                                            dashLength = 3.dp,
                                            gapLength = 3.dp
                                        ) else Modifier
                                    )
                                    .combinedClickable(
                                        onClick = { onSelectArea(area.id) },
                                        onLongClick = { onLongPressArea(area) }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(area.emoji, fontSize = 13.sp)

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = area.name,
                                                color = if (isSelected) OsrsTextYellow else if (isAccessible) OsrsTextWhite else if (isObeliskLocked || isLevelLocked) Color(0xFFFFF59D) else Color.LightGray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp
                                            )
                                            if (isSelected) {
                                                Text(
                                                    "📍",
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }

                                        val badgeText = when {
                                            isAccessible -> "Lv.${area.reqLevel}"
                                            isObeliskLocked -> "🔒 ${area.reqTotemEmoji ?: "🗿"} Obelisk"
                                            isLevelLocked -> "🔒 Lv.${area.reqLevel}"
                                            else -> "🔒 Locked"
                                        }

                                        Text(
                                            text = badgeText,
                                            color = when {
                                                isAccessible -> Color(0xFF81C784)
                                                isObeliskLocked || isLevelLocked -> Color(0xFFFFD54F)
                                                else -> Color(0xFFE57373)
                                            },
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Corner Obelisk Symbol if inaccessible because of totem/obelisk
                            if (isObeliskLocked) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-6).dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF2A1C0A),
                                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                                        shadowElevation = 3.dp
                                    ) {
                                        Text(
                                            text = area.reqTotemEmoji ?: "🗿",
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Requirements and details dialog shown when tapping/long-pressing an area in the Shaman Pool.
 */
@Composable
private fun SpiritPoolAreaRequirementsDialog(
    area: SpiritPoolArea,
    userFishingLevel: Int,
    hasTotem: Boolean,
    isFullUnlocked: Boolean,
    onDismiss: () -> Unit,
    onSelectArea: () -> Unit
) {
    val hasLevel = userFishingLevel >= area.reqLevel

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121B24)),
            border = BorderStroke(1.5.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(area.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = area.name,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            val statusBadge = when {
                                isFullUnlocked -> "✓ Full Access Granted"
                                !hasLevel && !hasTotem -> "🔒 Level & Obelisk Locked"
                                !hasLevel -> "🔒 Level Requirement Not Met"
                                else -> "🔒 Obelisk Locked"
                            }
                            val statusColor = when {
                                isFullUnlocked -> Color(0xFF81C784)
                                hasTotem -> Color(0xFFFFB74D)
                                else -> Color(0xFFE57373)
                            }
                            Text(
                                text = statusBadge,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Text("✕", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFF2C3E50), thickness = 1.dp)

                Text(
                    text = area.description,
                    color = OsrsTextWhite,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )

                // Requirements Checklist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D151E), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFF2C4A63), RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📜 Access Requirements:", color = Color(0xFF81D4FA), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // 1. Level Requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• Fishing Level:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                        Text(
                            text = if (hasLevel) "✓ Lv. ${area.reqLevel} (Met)" else "✗ Lv. ${area.reqLevel} (Current: $userFishingLevel)",
                            color = if (hasLevel) Color(0xFF81C784) else Color(0xFFE57373),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Obelisk Requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• Obelisk Requirement:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                        if (area.reqTotemId == null && area.reqTotemName == null) {
                            Text("✓ None (Default Waters)", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            val obeliskName = area.reqTotemName ?: "Obelisk"
                            val obeliskEmoji = area.reqTotemEmoji ?: "🗿"
                            Text(
                                text = if (hasTotem) "✓ $obeliskEmoji $obeliskName (Claimed)" else "🔒 $obeliskEmoji $obeliskName (Required)",
                                color = if (hasTotem) Color(0xFF81C784) else Color(0xFFFFB74D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (area.reqTotemId != null && !hasTotem) {
                        Text(
                            text = "💡 Unlock Tip: Obtain the ${area.reqTotemName} to unlock access to these waters!",
                            color = Color(0xFFFFCC80),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                // Catchable Fish in this Area
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🐟 Catchable Fish Species in this Pool:", color = OsrsTextYellow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    area.catchableFish.forEach { fish ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${fish.emoji} ${fish.name} (Lv. ${fish.reqLevel})", color = Color(0xFFE0E0E0), fontSize = 10.5.sp)
                            Text("${fish.dropChancePercent}% rate (+${fish.xp} XP)", color = Color(0xFF81C784), fontSize = 10.sp)
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("Close", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onSelectArea,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFullUnlocked) Color(0xFF0277BD) else Color(0xFF37474F))
                    ) {
                        Text(
                            text = if (isFullUnlocked) "Travel Here 🧭" else "Locked 🔒",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun getCookedHp(fishId: String): Int {
    return when (fishId) {
        "item_raw_shrimps" -> 10
        "item_raw_anchovies" -> 12
        "item_raw_sardine" -> 15
        "item_raw_trout" -> 20
        "item_raw_pike" -> 25
        "item_raw_salmon" -> 30
        "item_raw_lobster" -> 60
        "item_raw_tuna" -> 75
        "item_raw_swordfish" -> 90
        "item_raw_shark" -> 120
        "item_raw_sea_turtle" -> 150
        "item_raw_manta_ray" -> 180
        "item_spirit_koi" -> 250
        "item_astral_angler" -> 300
        "item_ethereal_ray" -> 350
        "item_magma_eel" -> 420
        "item_ember_trout" -> 500
        "item_obsidian_crab" -> 600
        "item_sacred_shaman_fish" -> 750
        "item_cosmic_whale" -> 950
        "item_golden_dragonfish" -> 1200
        else -> 15
    }
}

/**
 * Fish Details Dialog triggered when holding down (long-press) on any fish in the Shaman Pool.
 */
@Composable
private fun FishDetailsDialog(
    fish: com.example.data.models.SpiritFish,
    userFishingLevel: Int,
    invQty: Int,
    bankQty: Int,
    onDismiss: () -> Unit,
    onCatchNow: () -> Unit
) {
    val canCatch = userFishingLevel >= fish.reqLevel
    val cookedHp = getCookedHp(fish.id)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121B24)),
            border = BorderStroke(1.5.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fish.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = fish.name,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (canCatch) "✓ Unlocked (Lv. ${fish.reqLevel})" else "🔒 Requires Lv. ${fish.reqLevel} Fishing",
                                color = if (canCatch) Color(0xFF81C784) else Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Text("✕", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFF2C3E50), thickness = 1.dp)

                // Lore / Description
                Text(
                    text = fish.description,
                    color = OsrsTextWhite,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0D151E),
                        border = BorderStroke(0.5.dp, Color(0xFF2C4A63)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✨ XP Gain", color = Color(0xFF81D4FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("+${fish.xp} XP", color = OsrsTextYellow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0D151E),
                        border = BorderStroke(0.5.dp, Color(0xFF2C4A63)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎯 Bite Rate", color = Color(0xFF81D4FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${fish.dropChancePercent}%", color = Color(0xFF4CAF50), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0D151E),
                        border = BorderStroke(0.5.dp, Color(0xFF2C4A63)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🍖 Heals (Cooked)", color = Color(0xFF81D4FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("+$cookedHp HP", color = Color(0xFFFF8A80), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Drops Table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D151E), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFF2C4A63), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("📦 Items Dropped When Caught:", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(fish.emoji, fontSize = 13.sp)
                            Text("1x Raw ${fish.name.replace("Raw ", "")}", color = Color.White, fontSize = 11.sp)
                        }
                        Text("100% (Guaranteed)", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    if (fish.bonusSecondItemId != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(fish.bonusSecondItemEmoji ?: "✨", fontSize = 13.sp)
                                Text("+${fish.bonusSecondItemQty}x ${fish.bonusSecondItemName ?: fish.bonusSecondItemId}", color = Color(0xFFFFF59D), fontSize = 11.sp)
                            }
                            Text("Bonus Drop", color = Color(0xFFFFD54F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        "🔥 Culinary Use: Cook on campfire or stove to prepare nourishing food for dungeon runs and HP recovery.",
                        color = Color(0xFFB0BEC5),
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp
                    )
                }

                // Inventory Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎒 Owned in Backpack: $invQty", color = Color(0xFFB0BEC5), fontSize = 10.5.sp)
                    Text("🏦 In Bank: $bankQty", color = Color(0xFFB0BEC5), fontSize = 10.5.sp)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("Close", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onCatchNow,
                        enabled = canCatch,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canCatch) Color(0xFF1B5E20) else Color(0xFF37474F)
                        )
                    ) {
                        Text(
                            text = if (canCatch) "Catch Now 🎣" else "Locked 🔒",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
