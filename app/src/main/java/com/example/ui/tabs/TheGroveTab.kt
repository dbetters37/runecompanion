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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.models.AdventuringStoryData
import com.example.ui.components.dashedBorder
import com.example.data.models.GroveForestArea
import com.example.data.models.GroveTree
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.ui.components.BramNpcCompanion
import com.example.ui.components.CooldownActionButton
import com.example.ui.components.BonusBreakdownDialog
import com.example.ui.components.BonusSourceDetail
import com.example.ui.components.WoodPlankPanel
import com.example.ui.components.WoodHeaderBanner
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

data class GroveRawLogEntry(
    val item: com.example.data.models.InventoryItem,
    val totalQty: Int
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun TheGroveTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val selectedGroveForestId by viewModel.selectedGroveForestId.collectAsStateWithLifecycle()
    val isAfkWoodcuttingActive by viewModel.isAfkWoodcuttingActive.collectAsStateWithLifecycle()
    val isAfkSawmillActive by viewModel.isAfkSawmillActive.collectAsStateWithLifecycle()
    val selectedSawmillPlankId by viewModel.selectedSawmillPlankId.collectAsStateWithLifecycle()
    val selectedTreeId by viewModel.selectedTreeId.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var inspectedTreeForInfo by remember { mutableStateOf<GroveTree?>(null) }
    var inspectedForestForReqs by remember { mutableStateOf<GroveForestArea?>(null) }
    var showBonusBreakdownDialog by remember { mutableStateOf(false) }

    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val adventuringMaxFloor by viewModel.adventuringMaxFloor.collectAsStateWithLifecycle()
    val npcFavorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    val bramFavorLvl = npcFavorMap["bram"]?.first ?: viewModel.getNpcFavorLevel("bram")
    val completedQuestIds = petState.completedQuestIds

    val constructionXp = skillXpMap[OsrsSkill.CONSTRUCTION] ?: 0L
    val constructionLvl = OsrsXpCalculator.getLevelForXp(constructionXp)

    val wcXp = skillXpMap[OsrsSkill.WOODCUTTING] ?: 0L
    val wcLvl = OsrsXpCalculator.getLevelForXp(wcXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel((wcLvl + 1).coerceAtMost(99))
    val currentLevelBaseXp = OsrsXpCalculator.getXpForLevel(wcLvl)
    val progress = if (wcLvl >= 99) 1f else ((wcXp - currentLevelBaseXp).toFloat() / (nextLevelXp - currentLevelBaseXp).coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

    val onSelectOrInspectForest: (String) -> Unit = { forestId ->
        val forest = AdventuringStoryData.GROVE_FOREST_AREAS.find { it.id == forestId }
        val hasLevel = forest?.let { wcLvl >= it.reqLevel } ?: true
        val hasTotem = forest?.reqTotemId?.let { viewModel.isTotemUnlocked(it) } ?: true
        if (forest != null && (!hasLevel || !hasTotem)) {
            inspectedForestForReqs = forest
        } else {
            viewModel.selectGroveForestArea(forestId)
        }
    }

    val allTreeIds = remember {
        AdventuringStoryData.GROVE_FOREST_AREAS.flatMap { it.choppableTrees }.map { it.id }.toSet()
    }

    val rawLogItems = remember(bankItems) {
        val itemsMap = mutableMapOf<String, Pair<com.example.data.models.InventoryItem, Int>>()
        bankItems.forEach { bItem ->
            if (bItem.quantity > 0 && (allTreeIds.contains(bItem.id) || bItem.id.contains("log") || bItem.id.contains("timber") || bItem.id.contains("bark") || bItem.id.contains("trunk") || bItem.name.contains("Log", true))) {
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
            GroveRawLogEntry(
                item = item,
                totalQty = totalQty
            )
        }.sortedByDescending { it.totalQty }
    }

    val currentForest = remember(selectedGroveForestId) {
        AdventuringStoryData.GROVE_FOREST_AREAS.find { it.id == selectedGroveForestId }
            ?: AdventuringStoryData.GROVE_FOREST_AREAS.first()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OsrsLeatherDark)
                .padding(6.dp)
                .testTag("tab_the_grove"),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
        // --- COMPACT WOODCUTTING HEADER CARD ---
        item {
            WoodPlankPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("the_grove_header_card"),
                accentIcon = "🌲",
                borderColor = OsrsGold,
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
                        Text("🌲", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "The Grove",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OsrsTextYellow
                            )
                            Text(
                                text = "Lv. $wcLvl Woodcutting • ${"%,d".format(wcXp)} XP",
                                fontSize = 10.sp,
                                color = OsrsParchment
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.4f),
                        border = BorderStroke(0.8.dp, Color(0xFF81C784)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .clickable { showBonusBreakdownDialog = true }
                            .testTag("badge_extra_logs_chance")
                    ) {
                        Text(
                            text = "+${bramFavorLvl}% Extra Logs ⓘ",
                            color = Color(0xFFA5D6A7),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2: AFK Woodcutting Toggle Button
                CooldownActionButton(
                    onClick = { viewModel.toggleAfkWoodcutting() },
                    cooldownMs = 600L,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAfkWoodcuttingActive) Color(0xFF1B5E20) else Color(0xFF3E2723)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (isAfkWoodcuttingActive) Color(0xFF81C784) else OsrsGold),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    Text(
                        text = if (isAfkWoodcuttingActive) "⚡ STOP AFK WOODCUTTING" else "🪓 START AFK WOODCUTTING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAfkWoodcuttingActive) Color.White else OsrsTextYellow,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                        color = Color(0xFF81C784),
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

        // --- ALWAYS VISIBLE INTERACTIVE GROVE FOREST WORLD MAP ---
        item {
            TheGroveWorldMapCard(
                selectedForestId = selectedGroveForestId,
                userWoodcuttingLevel = wcLvl,
                completedQuestIds = completedQuestIds,
                adventuringMaxFloor = adventuringMaxFloor,
                isTotemUnlocked = { viewModel.isTotemUnlocked(it) },
                onSelectForest = onSelectOrInspectForest,
                onLongPressForest = { forest ->
                    inspectedForestForReqs = forest
                }
            )
        }

        // --- COMPACT LOCATION SELECTOR CHIPS ---
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AdventuringStoryData.GROVE_FOREST_AREAS, key = { it.id }) { forest ->
                    val isSelected = forest.id == selectedGroveForestId
                    val isHarvestingHere = isAfkWoodcuttingActive && isSelected
                    val hasLevel = wcLvl >= forest.reqLevel
                    val hasTotem = if (forest.reqTotemId != null) viewModel.isTotemUnlocked(forest.reqTotemId) else true
                    val requiresObelisk = forest.reqTotemId != null
                    val isFullUnlocked = hasLevel && hasTotem
                    val isObeliskLocked = requiresObelisk && !hasTotem
                    val isLevelLocked = !hasLevel && hasTotem

                    val chipBorderColor = when {
                        isHarvestingHere -> Color(0xFF81C784)
                        isSelected -> OsrsGold
                        isFullUnlocked -> Color(0xFF4CAF50)
                        isObeliskLocked || isLevelLocked -> Color(0xFFFFD54F)
                        else -> Color(0xFFE57373)
                    }

                    val chipBgColor = when {
                        isHarvestingHere -> Color(0xFF1B5E20)
                        isSelected -> Color(0xFF2E7D32)
                        isFullUnlocked -> Color(0xFF1C2C1B)
                        isObeliskLocked || isLevelLocked -> Color(0xFF2E260D)
                        else -> Color(0xFF241515)
                    }

                    val isDotted = isObeliskLocked || isLevelLocked
                    val chipAlpha = if (isObeliskLocked) 0.30f else 1.0f

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = chipBgColor,
                        border = if (!isDotted) BorderStroke(
                            width = if (isHarvestingHere || isSelected) 1.dp else 0.5.dp,
                            color = chipBorderColor
                        ) else null,
                        modifier = Modifier
                            .alpha(chipAlpha)
                            .then(
                                if (isDotted) Modifier.dashedBorder(
                                    width = if (isHarvestingHere || isSelected) 1.dp else 0.5.dp,
                                    color = chipBorderColor,
                                    shape = RoundedCornerShape(4.dp),
                                    dashLength = 3.dp,
                                    gapLength = 3.dp
                                ) else Modifier
                            )
                            .combinedClickable(
                                onClick = { onSelectOrInspectForest(forest.id) },
                                onLongClick = { inspectedForestForReqs = forest }
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(forest.emoji, fontSize = 11.sp)
                            Text(
                                forest.name,
                                color = if (isHarvestingHere || isSelected) Color.White else if (isFullUnlocked) Color(0xFFC8E6C9) else if (isObeliskLocked || isLevelLocked) Color(0xFFFFF59D) else Color(0xFFEF9A9A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            if (forest.reqTotemId != null && !hasTotem) {
                                Text(
                                    "🔒${forest.reqTotemEmoji ?: "🗿"}",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else if (isHarvestingHere) {
                                Text("⚡AFK", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                            } else {
                                Text(
                                    "Lv.${forest.reqLevel}",
                                    color = if (isFullUnlocked) Color(0xFF81C784) else if (isLevelLocked) Color(0xFFFFD54F) else Color(0xFFE57373),
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CHOPPABLE TREES IN THIS AREA (COMPACT AREA CARD) ---
        item {
            val hasAreaLevel = wcLvl >= currentForest.reqLevel
            val hasAreaTotem = if (currentForest.reqTotemId != null) viewModel.isTotemUnlocked(currentForest.reqTotemId) else true
            val isAreaFullUnlocked = hasAreaLevel && hasAreaTotem
            val requiresAreaObelisk = currentForest.reqTotemId != null
            val isAreaObeliskLocked = requiresAreaObelisk && !hasAreaTotem
            val isAreaLevelLocked = !hasAreaLevel && hasAreaTotem
            val isAreaDotted = isAreaObeliskLocked || isAreaLevelLocked
            val areaCardAlpha = if (isAreaObeliskLocked) 0.30f else 1.0f

            val isHarvestingThisForest = isAfkWoodcuttingActive && selectedGroveForestId == currentForest.id

            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isHarvestingThisForest) Color(0xFF1B2E1B) else Color(0xFF192419)
                ),
                border = if (!isAreaDotted) BorderStroke(1.dp, if (isHarvestingThisForest) Color(0xFF81C784) else Color(0xFF385E33)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(areaCardAlpha)
                    .then(
                        if (isAreaDotted) Modifier.dashedBorder(
                            width = 1.dp,
                            color = if (isAreaObeliskLocked) Color(0xFFFFD54F) else Color(0xFFFFD54F),
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
                    // Area Title & Compact AFK Harvest Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.combinedClickable(
                                onClick = { },
                                onLongClick = { inspectedForestForReqs = currentForest }
                            )
                        ) {
                            Text(
                                "${currentForest.emoji} ${currentForest.name}",
                                color = OsrsTextYellow,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = Color(0xFF2E4D29),
                                border = BorderStroke(0.5.dp, Color(0xFF81C784))
                            ) {
                                Text(
                                    "Lv.${currentForest.reqLevel}",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                )
                            }
                            if (isHarvestingThisForest) {
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
                                viewModel.toggleAfkGroveHarvest(currentForest.id)
                            },
                            cooldownMs = 600L,
                            enabled = isAreaFullUnlocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHarvestingThisForest) Color(0xFF1B5E20) else Color(0xFF3E2723),
                                disabledContainerColor = Color(0xFF261912)
                            ),
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(0.5.dp, if (isHarvestingThisForest) Color(0xFF81C784) else OsrsGold),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = if (isHarvestingThisForest) "⚡ STOP" else "🪓 AFK HARVEST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHarvestingThisForest) Color.White else OsrsTextYellow
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2D422B), thickness = 0.5.dp)

                    currentForest.choppableTrees.forEach { tree ->
                        val canChop = wcLvl >= tree.reqLevel && isAreaFullUnlocked
                        val isSelectedTarget = selectedTreeId == tree.id

                        val (invQty, bankQty) = remember(inventoryItems, bankItems, tree.id) {
                            val iQ = inventoryItems.find { it.id == tree.id }?.quantity ?: 0
                            val bQ = bankItems.find { it.id == tree.id }?.quantity ?: 0
                            Pair(iQ, bQ)
                        }
                        val totalOwned = invQty + bankQty

                        val dropBadgeColor = when {
                            tree.dropChancePercent >= 40 -> Color(0xFF4CAF50)
                            tree.dropChancePercent >= 20 -> Color(0xFF00ACC1)
                            tree.dropChancePercent >= 10 -> Color(0xFFFFB300)
                            else -> Color(0xFFFF7043)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(
                                    color = when {
                                        isSelectedTarget -> Color(0xFF284424)
                                        canChop -> Color(0xFF162415)
                                        else -> Color(0xFF121812)
                                    },
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .border(
                                    width = if (isSelectedTarget) 1.dp else 0.5.dp,
                                    color = if (isSelectedTarget) OsrsGold else Color(0xFF2D422B),
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (canChop) {
                                            viewModel.setSelectedTreeId(tree.id)
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        inspectedTreeForInfo = tree
                                        val logProducedName = when (tree.id) {
                                            "item_logs" -> "Logs"
                                            "item_oak_logs" -> "Oak Logs"
                                            "item_birch_timber" -> "Birch Timber"
                                            "item_willow_logs" -> "Willow Logs"
                                            "item_teak_logs" -> "Teak Logs"
                                            "item_maple_logs" -> "Maple Logs"
                                            "item_mahogany_logs" -> "Mahogany Logs"
                                            "item_yew_logs" -> "Yew Logs"
                                            "item_magic_logs" -> "Magic Logs"
                                            "item_redwood_logs" -> "Redwood Logs"
                                            else -> tree.name.replace(" Tree", "").replace(" Grove", "").replace(" Riverbank", "") + " Logs"
                                        }
                                        viewModel.addChatMessage("🪵 Tree Info: ${tree.name} yields $logProducedName (${tree.emoji}) when chopped!")
                                    }
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tree.emoji, fontSize = 13.sp)

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = tree.name,
                                        color = if (canChop) OsrsTextYellow else Color(0xFF8D6E63),
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
                                            text = "${tree.dropChancePercent}%",
                                            color = dropBadgeColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                                        )
                                    }
                                    if (isSelectedTarget) {
                                        Text("🎯", fontSize = 8.sp)
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Lv.${tree.reqLevel} • +${tree.xp}XP",
                                        color = Color(0xFF81C784),
                                        fontSize = 8.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        "• Bag: $invQty | Bank: $bankQty",
                                        color = if (totalOwned > 0) Color(0xFF80DEEA) else Color.Gray,
                                        fontWeight = if (totalOwned > 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 8.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            CooldownActionButton(
                                onClick = {
                                    if (canChop) {
                                        viewModel.setSelectedTreeId(tree.id)
                                        viewModel.chopTrees(targetTreeId = tree.id)
                                    }
                                },
                                cooldownMs = 1200L,
                                enabled = canChop,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelectedTarget) Color(0xFF1B5E20) else Color(0xFF2E3D2C),
                                    disabledContainerColor = Color(0xFF1F261E)
                                ),
                                shape = RoundedCornerShape(2.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .width(58.dp)
                                    .height(20.dp)
                            ) {
                                Text(
                                    text = if (canChop) "Chop 🪓" else "🔒 Lv ${tree.reqLevel}",
                                    color = if (canChop) Color.White else Color(0xFF6D4C41),
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

        // --- COMPACT TIMBER LOGS INVENTORY STRIP ---
        item {
            WoodPlankPanel(
                borderColor = Color(0xFF5D4037),
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
                            "TIMBER RESERVES",
                            color = OsrsTextYellow,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val totalLogsCount = rawLogItems.sumOf { it.totalQty }
                    Text(
                        "$totalLogsCount Total",
                        color = Color(0xFF81C784),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (rawLogItems.isEmpty()) {
                    Text(
                        "No timber logs in Bank yet. Chop trees above!",
                        color = Color(0xFFD7CCC8),
                        fontSize = 9.5.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rawLogItems.forEach { logEntry ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E130B).copy(alpha = 0.8f),
                                border = BorderStroke(0.5.dp, Color(0xFF8D6E63))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(logEntry.item.iconEmoji, fontSize = 12.sp)
                                    Text(
                                        logEntry.item.name,
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
                                            "x${logEntry.totalQty}",
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

        // --- AFK SAWMILL STATION (Milling Timber Logs -> Specialized Planks) ---
        item {
            WoodPlankPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("afk_sawmill_station_card"),
                accentIcon = "🪚",
                borderColor = if (isAfkSawmillActive) Color(0xFF81C784) else OsrsGold,
                contentPadding = PaddingValues(6.dp)
            ) {
                // 1. Compact Header with AFK Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🪚", fontSize = 12.sp)
                            Text(
                                text = "AFK SAWMILL",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OsrsTextYellow
                            )
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = if (isAfkSawmillActive) Color(0xFF1B5E20) else Color(0xFF3E2723),
                                border = BorderStroke(0.5.dp, if (isAfkSawmillActive) Color(0xFF81C784) else OsrsGold)
                            ) {
                                Text(
                                    text = if (isAfkSawmillActive) "⚡ RUNNING (4s)" else "⏸️ IDLE",
                                    color = if (isAfkSawmillActive) Color(0xFFFFD54F) else Color.LightGray,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                )
                            }
                        }

                        // Toggle AFK Sawmill Button
                        CooldownActionButton(
                            onClick = { viewModel.toggleAfkSawmill() },
                            cooldownMs = 500L,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAfkSawmillActive) Color(0xFF1B5E20) else Color(0xFF4E342E)
                            ),
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(0.8.dp, if (isAfkSawmillActive) Color(0xFF81C784) else OsrsGold),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = if (isAfkSawmillActive) "⚡ STOP AFK" else "🪚 START AFK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAfkSawmillActive) Color.White else OsrsTextYellow
                            )
                        }
                    }

                    // 2. Focused Active Target & Batch Action Panel
                    val activeTargetRecipe = com.example.data.models.SawmillRecipes.ALL_RECIPES.find { it.plankId == selectedSawmillPlankId }
                        ?: com.example.data.models.SawmillRecipes.ALL_RECIPES.first()

                    val (targetLogInv, targetLogBank) = remember(inventoryItems, bankItems, activeTargetRecipe.acceptedLogIds) {
                        val inv = inventoryItems.filter { activeTargetRecipe.acceptedLogIds.contains(it.id) }.sumOf { it.quantity }
                        val bnk = bankItems.filter { activeTargetRecipe.acceptedLogIds.contains(it.id) }.sumOf { it.quantity }
                        Pair(inv, bnk)
                    }
                    val totalTargetLogs = targetLogInv + targetLogBank

                    val (targetPlankInv, targetPlankBank) = remember(inventoryItems, bankItems, activeTargetRecipe.plankId) {
                        val inv = inventoryItems.find { it.id == activeTargetRecipe.plankId }?.quantity ?: 0
                        val bnk = bankItems.find { it.id == activeTargetRecipe.plankId }?.quantity ?: 0
                        Pair(inv, bnk)
                    }
                    val totalTargetPlanks = targetPlankInv + targetPlankBank

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF131A12),
                        border = BorderStroke(0.8.dp, if (isAfkSawmillActive) Color(0xFF81C784) else Color(0xFF382C20)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(activeTargetRecipe.emoji, fontSize = 13.sp)
                                    Text(
                                        text = "Target: ${activeTargetRecipe.plankName}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OsrsTextYellow
                                    )
                                    Text(
                                        text = "(Lv.${activeTargetRecipe.reqConstructionLevel} • +${activeTargetRecipe.constructionXp}XP)",
                                        fontSize = 8.sp,
                                        color = Color(0xFF81C784)
                                    )
                                }
                                Text(
                                    text = "🪵 $totalTargetLogs logs | 🪚 $totalTargetPlanks planks",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalTargetLogs > 0) Color(0xFFFFB74D) else Color(0xFFE57373)
                                )
                            }

                            // Quick Batch Action Buttons for Selected Target
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val canSaw1 = totalTargetLogs > 0
                                val batch10 = minOf(10, totalTargetLogs)
                                val canSaw10 = totalTargetLogs >= 2
                                val canSawAll = totalTargetLogs > 0

                                // Saw 1
                                CooldownActionButton(
                                    onClick = {
                                        viewModel.convertLogsToPlanksAtSawmill(
                                            isAfk = false,
                                            targetPlankId = activeTargetRecipe.plankId,
                                            quantityToSaw = 1
                                        )
                                    },
                                    cooldownMs = 300L,
                                    enabled = canSaw1,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E6B38),
                                        disabledContainerColor = Color(0xFF1E1712)
                                    ),
                                    shape = RoundedCornerShape(3.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(21.dp)
                                ) {
                                    Text(
                                        text = "Saw 1 🪚",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canSaw1) Color.White else Color(0xFF616161)
                                    )
                                }

                                // Saw 10
                                CooldownActionButton(
                                    onClick = {
                                        viewModel.convertLogsToPlanksAtSawmill(
                                            isAfk = false,
                                            targetPlankId = activeTargetRecipe.plankId,
                                            quantityToSaw = batch10
                                        )
                                    },
                                    cooldownMs = 350L,
                                    enabled = canSaw10,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1B5E20),
                                        disabledContainerColor = Color(0xFF1E1712)
                                    ),
                                    shape = RoundedCornerShape(3.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(21.dp)
                                ) {
                                    Text(
                                        text = if (canSaw10) "Saw $batch10 🪚" else "Saw 10 🪚",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canSaw10) OsrsTextYellow else Color(0xFF616161)
                                    )
                                }

                                // Saw All
                                CooldownActionButton(
                                    onClick = {
                                        viewModel.convertLogsToPlanksAtSawmill(
                                            isAfk = false,
                                            targetPlankId = activeTargetRecipe.plankId,
                                            quantityToSaw = totalTargetLogs
                                        )
                                    },
                                    cooldownMs = 400L,
                                    enabled = canSawAll,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF33691E),
                                        disabledContainerColor = Color(0xFF1E1712)
                                    ),
                                    shape = RoundedCornerShape(3.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(21.dp)
                                ) {
                                    Text(
                                        text = if (canSawAll) "Saw All ($totalTargetLogs)" else "Saw All",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canSawAll) Color(0xFFFFD54F) else Color(0xFF616161)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Ultra-Compact Single-Column Plank Recipes
                    Text(
                        text = "Plank Recipes (tap row to select milling target):",
                        fontSize = 8.sp,
                        color = Color(0xFFB0BEC5),
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    com.example.data.models.SawmillRecipes.ALL_RECIPES.forEach { recipe ->
                        val isSelected = selectedSawmillPlankId == recipe.plankId
                        val isUnlocked = constructionLvl >= recipe.reqConstructionLevel

                        val (logInvQty, logBankQty) = remember(inventoryItems, bankItems, recipe.acceptedLogIds) {
                            val inv = inventoryItems.filter { recipe.acceptedLogIds.contains(it.id) }.sumOf { it.quantity }
                            val bnk = bankItems.filter { recipe.acceptedLogIds.contains(it.id) }.sumOf { it.quantity }
                            Pair(inv, bnk)
                        }
                        val totalLogs = logInvQty + logBankQty

                        val (plankInvQty, plankBankQty) = remember(inventoryItems, bankItems, recipe.plankId) {
                            val inv = inventoryItems.find { it.id == recipe.plankId }?.quantity ?: 0
                            val bnk = bankItems.find { it.id == recipe.plankId }?.quantity ?: 0
                            Pair(inv, bnk)
                        }
                        val totalPlanks = plankInvQty + plankBankQty

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .clickable {
                                    viewModel.setSelectedSawmillPlankId(recipe.plankId)
                                },
                            shape = RoundedCornerShape(3.dp),
                            color = when {
                                isSelected -> Color(0xFF223618)
                                isUnlocked -> Color(0xFF17110B)
                                else -> Color(0xFF120E0C)
                            },
                            border = BorderStroke(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) OsrsGold else if (isUnlocked) Color(0xFF38291B) else Color(0xFF241C16)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp, vertical = 0.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Emoji, Name, Level & Selected Tag
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(recipe.emoji, fontSize = 11.sp)
                                    Text(
                                        text = recipe.plankName,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) OsrsTextYellow else if (isUnlocked) OsrsTextWhite else Color.Gray,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isUnlocked) "Lv.${recipe.reqConstructionLevel}" else "🔒 Lv.${recipe.reqConstructionLevel}",
                                        fontSize = 7.5.sp,
                                        color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFE57373)
                                    )
                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = OsrsGold.copy(alpha = 0.25f),
                                            border = BorderStroke(0.5.dp, OsrsGold)
                                        ) {
                                            Text(
                                                "🎯 TARGET",
                                                color = OsrsTextYellow,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 2.5.dp, vertical = 0.dp)
                                            )
                                        }
                                    }
                                }

                                // Right: Stock Counts (Logs & Planks)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "🪵 $totalLogs",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (totalLogs > 0) Color(0xFFFFB74D) else Color(0xFF757575)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(2.dp),
                                        color = if (isSelected) Color(0xFF1B4D20) else Color(0xFF140F0A),
                                        border = BorderStroke(0.5.dp, if (isSelected) OsrsGold else Color(0xFF38291B))
                                    ) {
                                        Text(
                                            text = "🪚 $totalPlanks",
                                            color = if (totalPlanks > 0) Color(0xFF81C784) else Color.Gray,
                                            fontSize = 7.5.sp,
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

    // --- POPUP DIALOGS FOR LONG-PRESS DETAILS ---
    inspectedTreeForInfo?.let { tree ->
        val totalQ = bankItems.filter { it.id == tree.id || com.example.data.models.DefaultItems.normalizeItemId(it.id) == com.example.data.models.DefaultItems.normalizeItemId(tree.id) }.sumOf { it.quantity }
        TreeDetailsDialog(
            tree = tree,
            userWoodcuttingLevel = wcLvl,
            totalQty = totalQ,
            onDismiss = { inspectedTreeForInfo = null },
            onChopNow = {
                viewModel.setSelectedTreeId(tree.id)
                viewModel.chopTrees(targetTreeId = tree.id)
                inspectedTreeForInfo = null
            }
        )
    }

    inspectedForestForReqs?.let { forest ->
        ForestAreaRequirementsDialog(
            forest = forest,
            userWoodcuttingLevel = wcLvl,
            isTotemUnlocked = { viewModel.isTotemUnlocked(it) },
            onDismiss = { inspectedForestForReqs = null },
            onSelectForest = {
                viewModel.selectGroveForestArea(forest.id)
                inspectedForestForReqs = null
            }
        )
    }

    BramNpcCompanion(
        viewModel = viewModel,
        modifier = Modifier.fillMaxSize()
    )

    if (showBonusBreakdownDialog) {
        BonusBreakdownDialog(
            title = "Extra Log Bonus Chance",
            categoryName = "The Grove & Forestry",
            iconEmoji = "🪓",
            sources = listOf(
                BonusSourceDetail(
                    title = "Bram the Woodcutter's Favor (Lv. $bramFavorLvl)",
                    description = "Grants +1% chance per favor level to chop an extra log and bonus bird nests / tree spirits from Grove trees (Up to +50%).",
                    bonusPercent = bramFavorLvl,
                    emoji = "🪓",
                    isUnlocked = true
                )
            ),
            note = "When triggered, an additional log is chopped in the same attempt!",
            onDismiss = { showBonusBreakdownDialog = false }
        )
    }
    }
}

/**
 * Interactive OSRS-styled World Map showing Grove Forest Areas.
 * Formatted identically to ShamanPoolWorldMapCard for visual parity.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TheGroveWorldMapCard(
    selectedForestId: String,
    userWoodcuttingLevel: Int,
    completedQuestIds: List<String>,
    adventuringMaxFloor: Int,
    isTotemUnlocked: (String?) -> Boolean,
    onSelectForest: (String) -> Unit,
    onLongPressForest: (GroveForestArea) -> Unit
) {
    val forests = AdventuringStoryData.GROVE_FOREST_AREAS

    val infiniteTransition = rememberInfiniteTransition(label = "grove_map_ping")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "grove_pulse_alpha"
    )

    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF122014)),
        border = BorderStroke(1.dp, OsrsGold),
        modifier = Modifier.fillMaxWidth().testTag("grove_map_card")
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
                        "Sylvan Canopy Map",
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
                        "Hold pin for info",
                        color = Color(0xFF81C784),
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
                    .border(0.5.dp, Color(0xFF2C4A2E), RoundedCornerShape(6.dp))
                    .horizontalScroll(horizontalScrollState)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = mapWidthDp, height = mapHeightDp)
                        .background(Color(0xFF0D180E))
                ) {
                    // Background Image Layer for The Grove
                    Image(
                        painter = painterResource(id = R.drawable.img_grove_map_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.38f)
                    )

                    // 1. Canvas layer for forest grid & connecting woodland pathways
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthPx = size.width
                        val heightPx = size.height
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val gridColor = Color(0xFF1B331D)

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

                        val path = Path()
                        forests.forEachIndexed { index, forest ->
                            val px = forest.posXRatio * widthPx
                            val py = forest.posYRatio * heightPx
                            if (index == 0) {
                                path.moveTo(px, py)
                            } else {
                                path.lineTo(px, py)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF81C784).copy(alpha = 0.4f),
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
                            "🧭 Sylvan Forest Canopy",
                            color = Color(0xFF2E4E2F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Render Location Pins
                    forests.forEach { forest ->
                        val isSelected = forest.id == selectedForestId
                        val hasLevel = userWoodcuttingLevel >= forest.reqLevel
                        val hasTotem = if (forest.reqTotemId != null) isTotemUnlocked(forest.reqTotemId) else true
                        val requiresObelisk = forest.reqTotemId != null
                        val isAccessible = hasLevel && hasTotem
                        val isObeliskLocked = requiresObelisk && !hasTotem
                        val isLevelLocked = !hasLevel && hasTotem

                        val pinBgColor = when {
                            isSelected -> Color(0xFF2E7D32)
                            isAccessible -> Color(0xFF18241D)
                            isObeliskLocked || isLevelLocked -> Color(0xFF2E260D)
                            else -> Color(0xFF1E1515)
                        }

                        val pinBorderColor = when {
                            isSelected -> OsrsGold.copy(alpha = pulseAlpha)
                            isAccessible -> Color(0xFF81C784)
                            isObeliskLocked || isLevelLocked -> Color(0xFFFFD54F)
                            else -> Color(0xFFE57373)
                        }

                        val isDotted = isObeliskLocked || isLevelLocked
                        val pinAlpha = if (isObeliskLocked) 0.30f else 1.0f

                        val pinXDp = (mapWidthDp * forest.posXRatio - 45.dp).coerceIn(4.dp, mapWidthDp - 110.dp)
                        val pinYDp = (mapHeightDp * forest.posYRatio - 22.dp).coerceIn(4.dp, mapHeightDp - 50.dp)

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
                                        onClick = { onSelectForest(forest.id) },
                                        onLongClick = { onLongPressForest(forest) }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(forest.emoji, fontSize = 13.sp)

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = forest.name,
                                                color = if (isSelected) OsrsTextYellow else if (isAccessible) OsrsTextWhite else if (isObeliskLocked || isLevelLocked) Color(0xFFFFF59D) else Color.LightGray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp
                                            )
                                            if (isSelected) {
                                                Text("📍", fontSize = 9.sp)
                                            }
                                        }

                                        val badgeText = when {
                                            isAccessible -> "Lv.${forest.reqLevel}"
                                            isObeliskLocked -> "🔒 ${forest.reqTotemEmoji ?: "🗿"} Obelisk"
                                            isLevelLocked -> "🔒 Lv.${forest.reqLevel}"
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

                            // Corner Obelisk symbol if inaccessible due to totem/obelisk
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
                                            text = forest.reqTotemEmoji ?: "🗿",
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
 * Tree Details Dialog triggered when holding down (long-press) on a tree in The Grove.
 */
@Composable
private fun TreeDetailsDialog(
    tree: GroveTree,
    userWoodcuttingLevel: Int,
    totalQty: Int,
    onDismiss: () -> Unit,
    onChopNow: () -> Unit
) {
    val canChop = userWoodcuttingLevel >= tree.reqLevel
    val logName = when (tree.id) {
        "item_logs" -> "Logs"
        "item_oak_logs" -> "Oak Logs"
        "item_birch_timber" -> "Birch Timber"
        "item_willow_logs" -> "Willow Logs"
        "item_teak_logs" -> "Teak Logs"
        "item_maple_logs" -> "Maple Logs"
        "item_mahogany_logs" -> "Mahogany Logs"
        "item_yew_logs" -> "Yew Logs"
        "item_magic_logs" -> "Magic Logs"
        "item_redwood_logs" -> "Redwood Logs"
        else -> tree.name.replace(" Tree", "").replace(" Grove", "").replace(" Riverbank", "") + " Logs"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162417)),
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
                        Text(tree.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = tree.name,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (canChop) "✓ Unlocked (Lv. ${tree.reqLevel})" else "🔒 Requires Lv. ${tree.reqLevel} Woodcutting",
                                color = if (canChop) Color(0xFF81C784) else Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Text("✕", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFF2C4A2E), thickness = 1.dp)

                // Log Info Card
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF101B11),
                    border = BorderStroke(0.5.dp, Color(0xFF385E33)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪵 Primary Harvest:", color = Color(0xFFC8E6C9), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("1x $logName", color = OsrsTextYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡ Woodcutting XP:", color = Color(0xFFB0BEC5), fontSize = 11.5.sp)
                            Text("+${tree.xp} XP / chop", color = Color(0xFF81C784), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎲 Canopy Frequency:", color = Color(0xFFB0BEC5), fontSize = 11.5.sp)
                            Text("${tree.dropChancePercent}% chance", color = Color(0xFF00E676), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎒 Currently Owned:", color = Color(0xFFB0BEC5), fontSize = 11.5.sp)
                            Text("In Bank: $totalQty", color = Color(0xFF80DEEA), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Secondary Drops & Lore
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🕊️ Secondary Forest Yields:", color = OsrsTextYellow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "• Birds' Nests (Tree Seeds, Gold/Gem Rings, Spirit Eggs)\n• Tree Resin & Acorns for Herbalist & Summoning Infusion\n• Usable for Firemaking, Fletching shafts & Sawmill Planks",
                        color = Color(0xFFE0E0E0),
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )
                }

                if (tree.description.isNotBlank()) {
                    Text(
                        text = tree.description,
                        color = Color(0xFFB0BEC5),
                        fontSize = 10.5.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
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
                        onClick = onChopNow,
                        enabled = canChop,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            disabledContainerColor = Color(0xFF222B22)
                        )
                    ) {
                        Text(
                            text = if (canChop) "Chop Now 🪓" else "Locked",
                            color = if (canChop) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Forest Area Requirements Dialog triggered when holding down (long-press) on a Forest Area in The Grove.
 */
@Composable
private fun ForestAreaRequirementsDialog(
    forest: GroveForestArea,
    userWoodcuttingLevel: Int,
    isTotemUnlocked: (String?) -> Boolean,
    onDismiss: () -> Unit,
    onSelectForest: () -> Unit
) {
    val hasLevel = userWoodcuttingLevel >= forest.reqLevel
    val hasTotem = if (forest.reqTotemId != null) isTotemUnlocked(forest.reqTotemId) else true
    val isFullUnlocked = hasLevel && hasTotem
    val isPartialUnlocked = hasLevel != hasTotem

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162417)),
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
                        Text(forest.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = forest.name,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            val statusBadge = when {
                                isFullUnlocked -> "🟢 Full Access Granted"
                                isPartialUnlocked -> "🟡 Partial Access (Missing Requirement)"
                                else -> "🔴 Area Locked"
                            }
                            val statusColor = when {
                                isFullUnlocked -> Color(0xFF81C784)
                                isPartialUnlocked -> Color(0xFFFFD54F)
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

                HorizontalDivider(color = Color(0xFF2C4A2E), thickness = 1.dp)

                // Requirements Checklist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF101B11), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFF385E33), RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📜 Access Requirements:", color = Color(0xFFC8E6C9), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // 1. Level Requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• Woodcutting Level:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                        Text(
                            text = if (hasLevel) "✓ Lv. ${forest.reqLevel} (Met)" else "✗ Lv. ${forest.reqLevel} (Current: $userWoodcuttingLevel)",
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
                        if (forest.reqTotemId == null && forest.reqTotemName == null) {
                            Text("✓ None (Default Area)", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            val obeliskName = forest.reqTotemName ?: "Obelisk"
                            val obeliskEmoji = forest.reqTotemEmoji ?: "🗿"
                            Text(
                                text = if (hasTotem) "✓ $obeliskEmoji $obeliskName (Claimed)" else "🔒 $obeliskEmoji $obeliskName (Required)",
                                color = if (hasTotem) Color(0xFF81C784) else Color(0xFFFFB74D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (forest.reqTotemId != null && !hasTotem) {
                        Text(
                            text = "💡 Unlock Tip: Obtain the ${forest.reqTotemName} to unlock access to this forest area!",
                            color = Color(0xFFFFCC80),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                // Choppable Trees in this Forest
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🌲 Choppable Trees in this Canopy:", color = OsrsTextYellow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    forest.choppableTrees.forEach { tree ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${tree.emoji} ${tree.name} (Lv. ${tree.reqLevel})", color = Color(0xFFE0E0E0), fontSize = 10.5.sp)
                            Text("${tree.dropChancePercent}% rate (+${tree.xp} XP)", color = Color(0xFF81C784), fontSize = 10.sp)
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
                        onClick = onSelectForest,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text(
                            text = "Travel Here 🧭",
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
