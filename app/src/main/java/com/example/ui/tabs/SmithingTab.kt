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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
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
import com.example.data.models.*
import com.example.ui.components.CooldownActionButton
import com.example.ui.components.BonusBreakdownDialog
import com.example.ui.components.BonusSourceDetail
import com.example.ui.components.dashedBorder
import com.example.ui.components.StoneMasonryPanel
import com.example.ui.components.StoneHeaderBanner
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

data class OreReserveEntry(
    val item: InventoryItem,
    val invQty: Int,
    val bankQty: Int,
    val totalQty: Int
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SmithingTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val isAfkSmeltingActive by viewModel.isAfkSmeltingActive.collectAsStateWithLifecycle()
    val isAfkSmithingAnvilActive by viewModel.isAfkSmithingAnvilActive.collectAsStateWithLifecycle()
    val isAfkMiningActive by viewModel.isAfkMiningActive.collectAsStateWithLifecycle()
    val activeSmeltRecipe by viewModel.activeSmeltRecipe.collectAsStateWithLifecycle()
    val activeSmithAnvilRecipe by viewModel.activeSmithAnvilRecipe.collectAsStateWithLifecycle()
    val selectedOreId by viewModel.selectedOreId.collectAsStateWithLifecycle()
    val selectedGemologyAreaId by viewModel.selectedGemologyAreaId.collectAsStateWithLifecycle()
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val adventuringMaxFloor by viewModel.adventuringMaxFloor.collectAsStateWithLifecycle()
    val npcFavorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    val arlgFavorLvl = npcFavorMap["arlg"]?.first ?: npcFavorMap["arig"]?.first ?: viewModel.getNpcFavorLevel("arlg")
    val completedQuestIds = petState.completedQuestIds

    val smithXp = skillXpMap[OsrsSkill.SMITHING] ?: 0L
    val smithLvl = OsrsXpCalculator.getLevelForXp(smithXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel((smithLvl + 1).coerceAtMost(99))
    val currentLevelBaseXp = OsrsXpCalculator.getXpForLevel(smithLvl)
    val progress = if (smithLvl >= 99) 1f else ((smithXp - currentLevelBaseXp).toFloat() / (nextLevelXp - currentLevelBaseXp).coerceAtLeast(1L)).coerceIn(0f, 1f)

    val currentGemologyArea = remember(selectedGemologyAreaId) {
        AdventuringStoryData.GEMOLOGY_AREAS.find { it.id == selectedGemologyAreaId }
            ?: AdventuringStoryData.GEMOLOGY_AREAS.first()
    }

    var selectedSectionIndex by remember { mutableStateOf(0) } // 0 = Gemology Quarry, 1 = Furnace Smelting, 2 = Anvil Smithing
    var selectedMetalTier by remember { mutableStateOf("Bronze") } // Bronze, Iron, Steel, Opalite, Amethyst, Aetherite

    fun getTotalCount(itemId: String): Int {
        return viewModel.getItemQuantityCombined(itemId)
    }

    val allMineralIds = remember {
        AdventuringStoryData.GEMOLOGY_AREAS.flatMap { it.minerals }.map { it.id }.toSet()
    }

    var inspectedQuarryForReqs by remember { mutableStateOf<GemologyArea?>(null) }
    var inspectedMineralForDetails by remember { mutableStateOf<com.example.data.models.GemologyMineral?>(null) }
    var showBonusBreakdownDialog by remember { mutableStateOf(false) }

    val onSelectOrInspectQuarry: (String) -> Unit = { areaId ->
        val area = AdventuringStoryData.GEMOLOGY_AREAS.find { it.id == areaId }
        val hasTotem = area?.reqTotemId == null || viewModel.isTotemUnlocked(area.reqTotemId)
        val isUnlocked = area != null && smithLvl >= area.reqLevel && hasTotem
        if (area != null && !isUnlocked) {
            inspectedQuarryForReqs = area
        } else {
            viewModel.selectGemologyArea(areaId)
        }
    }
    val minedOreItems = remember(inventoryItems, bankItems) {
        val itemsMap = mutableMapOf<String, Triple<InventoryItem, Int, Int>>() // normId -> (item, invQty, bankQty)
        inventoryItems.forEach { item ->
            if (item.quantity > 0 && (allMineralIds.contains(item.id) || item.id.contains("ore") || item.name.contains("Ore", true) || item.id == "item_rune_essence" || item.id == "item_coal_ore" || item.name.contains("Essence", true))) {
                val normId = com.example.data.models.DefaultItems.normalizeItemId(item.id)
                val existing = itemsMap[normId]
                if (existing != null) {
                    itemsMap[normId] = Triple(existing.first, existing.second + item.quantity, existing.third)
                } else {
                    itemsMap[normId] = Triple(item, item.quantity, 0)
                }
            }
        }
        bankItems.forEach { bItem ->
            if (bItem.quantity > 0 && (allMineralIds.contains(bItem.id) || bItem.id.contains("ore") || bItem.name.contains("Ore", true) || bItem.id == "item_rune_essence" || bItem.id == "item_coal_ore" || bItem.name.contains("Essence", true))) {
                val normId = com.example.data.models.DefaultItems.normalizeItemId(bItem.id)
                val existing = itemsMap[normId]
                if (existing != null) {
                    itemsMap[normId] = Triple(existing.first, existing.second, existing.third + bItem.quantity)
                } else {
                    itemsMap[normId] = Triple(bItem.copy(quantity = 0), 0, bItem.quantity)
                }
            }
        }
        itemsMap.values.map { (item, invQty, bankQty) ->
            OreReserveEntry(
                item = item,
                invQty = invQty,
                bankQty = bankQty,
                totalQty = invQty + bankQty
            )
        }.sortedByDescending { it.totalQty }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // --- COMPACT SMITHING & MINING HEADER CARD ---
        item {
            StoneMasonryPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("smithing_header_card"),
                accentIcon = "⚒️",
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
                            Text("⚒️", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "Gemology & Smithing",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OsrsTextYellow
                                )
                                Text(
                                    text = "Lv. $smithLvl Forging • ${"%,d".format(smithXp)} XP",
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
                                .testTag("badge_double_ore_chance")
                        ) {
                            Text(
                                text = "+${arlgFavorLvl}% Double Ore ⓘ",
                                color = Color(0xFFA5D6A7),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Row 2: Quick AFK Action Switcher / Status Button
                    val anyAfkActive = isAfkMiningActive || isAfkSmeltingActive || isAfkSmithingAnvilActive
                    val afkLabel = when {
                        isAfkMiningActive -> "⚡ STOP AFK MINING"
                        isAfkSmeltingActive -> "⚡ STOP AFK SMELTING"
                        isAfkSmithingAnvilActive -> "⚡ STOP AFK FORGING"
                        else -> "⛏️ START AFK MINING"
                    }

                    CooldownActionButton(
                        onClick = {
                            when {
                                isAfkMiningActive -> viewModel.toggleAfkGemologyMining(selectedGemologyAreaId)
                                isAfkSmeltingActive -> viewModel.toggleAfkSmelting(activeSmeltRecipe ?: SmithingData.SMELT_RECIPES.first())
                                isAfkSmithingAnvilActive -> viewModel.toggleAfkSmithingAnvil(activeSmithAnvilRecipe ?: SmithingData.getAnvilRecipesForTier("Bronze").first())
                                else -> viewModel.toggleAfkGemologyMining(selectedGemologyAreaId)
                            }
                        },
                        cooldownMs = 600L,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (anyAfkActive) Color(0xFF1B5E20) else Color(0xFF3E2723)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (anyAfkActive) Color(0xFF81C784) else OsrsGold),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        Text(
                            text = afkLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (anyAfkActive) Color.White else OsrsTextYellow,
                            maxLines = 1
                        )
                    }

                    // Level Progress Bar
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
                            color = OsrsTextGreen,
                            trackColor = Color(0xFF1E1510)
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

        // --- COMPACT ORE RESERVES STRIP (STYLE LIKE SHAMAN POOL FISH RESERVES) ---
        item {
            StoneMasonryPanel(
                borderColor = Color(0xFF5A3E25),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ore_reserve_section"),
                accentIcon = "⛏️",
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
                            Text("⛏️", fontSize = 12.sp)
                            Text(
                                "ORE RESERVE",
                                color = OsrsTextYellow,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val totalOreCount = minedOreItems.sumOf { it.totalQty }
                        Text(
                            "$totalOreCount Total",
                            color = Color(0xFF81C784),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (minedOreItems.isEmpty()) {
                        Text(
                            "No mined ores in inventory or bank yet. Mine ores in the quarry above!",
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
                            minedOreItems.forEach { oreEntry ->
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
                                        Text(oreEntry.item.iconEmoji, fontSize = 12.sp)
                                        Text(
                                            oreEntry.item.name,
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
                                                "x${oreEntry.totalQty}",
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

        // --- COMPACT SECTION SELECTION TABS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Button(
                    onClick = { selectedSectionIndex = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 0) OsrsRedFrame else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                        .border(1.dp, if (selectedSectionIndex == 0) OsrsGold else Color(0xFF4A3828), RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = "⛏️ Gemology Quarry",
                        color = if (selectedSectionIndex == 0) OsrsTextYellow else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = { selectedSectionIndex = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 1) OsrsRedFrame else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                        .border(1.dp, if (selectedSectionIndex == 1) OsrsGold else Color(0xFF4A3828), RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = "🔥 Smelting",
                        color = if (selectedSectionIndex == 1) OsrsTextYellow else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = { selectedSectionIndex = 2 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 2) OsrsRedFrame else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                        .border(1.dp, if (selectedSectionIndex == 2) OsrsGold else Color(0xFF4A3828), RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = "🔨 Anvil Forging",
                        color = if (selectedSectionIndex == 2) OsrsTextYellow else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }

        if (selectedSectionIndex == 0) {
            // --- ALWAYS VISIBLE INTERACTIVE GEMOLOGY QUARRY WORLD MAP ---
            item {
                GemologyQuarryWorldMapCard(
                    selectedAreaId = selectedGemologyAreaId,
                    userMiningLevel = smithLvl,
                    isTotemUnlocked = { viewModel.isTotemUnlocked(it) },
                    onSelectArea = onSelectOrInspectQuarry,
                    onLongPressArea = { inspectedQuarryForReqs = it }
                )
            }

            // --- GEMOLOGY QUARRY AREA SELECTOR CHIPS ---
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AdventuringStoryData.GEMOLOGY_AREAS, key = { it.id }) { area ->
                        val isSelected = area.id == selectedGemologyAreaId
                        val isMiningHere = isAfkMiningActive && isSelected
                        val hasTotem = viewModel.isTotemUnlocked(area.reqTotemId)
                        val hasLevel = smithLvl >= area.reqLevel
                        val isAccessible = hasLevel && hasTotem
                        val isObeliskLocked = !hasTotem
                        val isLevelLocked = !hasLevel && hasTotem

                        val chipBg = when {
                            isMiningHere -> Color(0xFF1B5E20)
                            isSelected -> Color(0xFF4E342E)
                            isAccessible -> Color(0xFF162B18)
                            isObeliskLocked || isLevelLocked -> Color(0xFF2C260D)
                            else -> Color(0xFF2B1212)
                        }

                        val chipBorder = when {
                            isMiningHere -> Color(0xFF81C784)
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
                                width = if (isMiningHere || isSelected) 1.dp else 0.5.dp,
                                color = chipBorder
                            ) else null,
                            modifier = Modifier
                                .alpha(chipAlpha)
                                .then(
                                    if (isDotted) Modifier.dashedBorder(
                                        width = if (isMiningHere || isSelected) 1.dp else 0.5.dp,
                                        color = chipBorder,
                                        shape = RoundedCornerShape(4.dp),
                                        dashLength = 3.dp,
                                        gapLength = 3.dp
                                    ) else Modifier
                                )
                                .clickable { onSelectOrInspectQuarry(area.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(area.emoji, fontSize = 11.sp)
                                Text(
                                    area.name,
                                    color = if (isMiningHere) Color.White else if (isSelected) OsrsTextYellow else if (isAccessible) OsrsTextWhite else if (isObeliskLocked || isLevelLocked) Color(0xFFFFF59D) else Color.LightGray,
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
                                } else if (isMiningHere) {
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

            // --- MINABLE MINERALS IN THIS GEMOLOGY AREA ---
            item {
                val areaHasTotem = viewModel.isTotemUnlocked(currentGemologyArea.reqTotemId)
                val areaHasLevel = smithLvl >= currentGemologyArea.reqLevel
                val areaUnlocked = areaHasLevel && areaHasTotem
                val isMiningThisArea = isAfkMiningActive && selectedGemologyAreaId == currentGemologyArea.id

                val areaBgColor = when {
                    isMiningThisArea -> Color(0xFF1B2E1B)
                    areaUnlocked -> Color(0xFF162B18)
                    !areaHasTotem || !areaHasLevel -> Color(0xFF2C260D)
                    else -> Color(0xFF2B1212)
                }
                val areaBorderColor = when {
                    isMiningThisArea -> Color(0xFF81C784)
                    areaUnlocked -> Color(0xFF81C784)
                    !areaHasTotem || !areaHasLevel -> Color(0xFFFFD54F)
                    else -> Color(0xFFE57373)
                }

                val isAreaObeliskLocked = !areaHasTotem
                val isAreaLevelLocked = !areaHasLevel && areaHasTotem
                val isAreaDotted = isAreaObeliskLocked || isAreaLevelLocked
                val areaCardAlpha = if (isAreaObeliskLocked) 0.30f else 1.0f

                Card(
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
                        ),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = areaBgColor
                    ),
                    border = if (!isAreaDotted) BorderStroke(1.dp, areaBorderColor) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Area Title & Compact AFK Mining Toggle Switch
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
                                    "${currentGemologyArea.emoji} ${currentGemologyArea.name}",
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
                                        if (areaUnlocked) "Lv.${currentGemologyArea.reqLevel} ✓" else if (!areaHasTotem) "🔒 ${currentGemologyArea.reqTotemEmoji ?: "🗿"} Needed" else "Lv.${currentGemologyArea.reqLevel}",
                                        color = if (areaUnlocked) Color(0xFF81C784) else if (!areaHasTotem || !areaHasLevel) Color(0xFFFFD54F) else Color(0xFFE57373),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                    )
                                }
                                if (isMiningThisArea) {
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
                                    viewModel.toggleAfkGemologyMining(currentGemologyArea.id)
                                },
                                cooldownMs = 600L,
                                enabled = areaUnlocked,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMiningThisArea) Color(0xFF1B5E20) else Color(0xFF3E2723),
                                    disabledContainerColor = Color(0xFF261912)
                                ),
                                shape = RoundedCornerShape(3.dp),
                                border = BorderStroke(0.5.dp, if (isMiningThisArea) Color(0xFF81C784) else OsrsGold),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(22.dp)
                            ) {
                                Text(
                                    text = if (isMiningThisArea) "⚡ STOP" else "⛏️ AFK MINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMiningThisArea) Color.White else OsrsTextYellow
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF3E2D1F), thickness = 0.5.dp)

                        currentGemologyArea.minerals.forEach { mineral ->
                            val canMine = smithLvl >= mineral.reqLevel && areaUnlocked
                            val isSelectedTarget = selectedOreId == mineral.id

                            val invQty = inventoryItems.find { it.id == mineral.id }?.quantity ?: 0
                            val bankQty = bankItems.find { it.id == mineral.id }?.quantity ?: 0
                            val totalOwned = invQty + bankQty

                            val dropBadgeColor = when {
                                mineral.dropChancePercent >= 40 -> Color(0xFF4CAF50)
                                mineral.dropChancePercent >= 20 -> Color(0xFF00ACC1)
                                mineral.dropChancePercent >= 10 -> Color(0xFFFFB300)
                                else -> Color(0xFFFF7043)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .background(
                                        color = when {
                                            isSelectedTarget -> Color(0xFF3D2A1B)
                                            canMine -> Color(0xFF281C13)
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
                                            if (canMine) {
                                                viewModel.setSelectedOreId(mineral.id)
                                            } else {
                                                inspectedMineralForDetails = mineral
                                            }
                                        },
                                        onLongClick = {
                                            inspectedMineralForDetails = mineral
                                        }
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mineral.emoji, fontSize = 13.sp)

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = mineral.name,
                                            color = if (canMine) OsrsTextYellow else Color(0xFF8D6E63),
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
                                                text = "${mineral.dropChancePercent}%",
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
                                            "Lv.${mineral.reqLevel} • +${mineral.xp}XP",
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
                                        if (canMine) {
                                            viewModel.setSelectedOreId(mineral.id)
                                            viewModel.mineAtPohQuarry(mineral.id, isAfk = false)
                                        } else {
                                            inspectedMineralForDetails = mineral
                                        }
                                    },
                                    cooldownMs = 1200L,
                                    enabled = canMine,
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
                                        text = if (canMine) "Mine ⛏️" else "🔒 Lv ${mineral.reqLevel}",
                                        color = if (canMine) Color.White else Color(0xFF6D4C41),
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
        }

        if (selectedSectionIndex == 1) {
            // --- FURNACE SMELTING RECIPES ---
            items(SmithingData.SMELT_RECIPES) { recipe ->
                val meetsLevel = smithLvl >= recipe.reqLevel

                val materialStatusList = recipe.inputOres.map { ore ->
                    val invQty = inventoryItems.filter { it.id == ore.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == com.example.data.models.DefaultItems.normalizeItemId(ore.itemId) }.sumOf { it.quantity }
                    val bankQty = bankItems.filter { it.id == ore.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == com.example.data.models.DefaultItems.normalizeItemId(ore.itemId) }.sumOf { it.quantity }
                    val total = invQty + bankQty
                    Triple(ore, invQty, total)
                }
                val hasMaterials = materialStatusList.all { (ore, _, total) -> total >= ore.quantity }

                val barCount = getTotalCount(recipe.barItemId)
                val isThisRecipeAfk = isAfkSmeltingActive && activeSmeltRecipe?.id == recipe.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smelt_card_${recipe.id}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isThisRecipeAfk) Color(0xFF1E351E) else Color(0xFF221810)
                    ),
                    border = BorderStroke(1.dp, if (isThisRecipeAfk) Color(0xFF81C784) else if (meetsLevel) Color(0xFF5A4532) else Color(0xFF3A2B1E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(recipe.iconEmoji, fontSize = 15.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = recipe.barName,
                                        color = if (meetsLevel) OsrsTextYellow else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                    Text(
                                        text = "• +${recipe.xpReward} XP",
                                        color = Color(0xFF81C784),
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Owned: $barCount",
                                    color = OsrsGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (meetsLevel) Color(0xFF1B5E20) else Color(0xFF3E2D1D))
                                        .border(0.5.dp, if (meetsLevel) Color(0xFF81C784) else OsrsGold.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Lv ${recipe.reqLevel}",
                                        color = if (meetsLevel) Color(0xFFA5D6A7) else OsrsParchment,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Compact Requirements and Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                materialStatusList.forEach { (ore, invQty, totalQty) ->
                                    val isEnough = totalQty >= ore.quantity
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isEnough) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color(0xFF381C10))
                                            .border(0.5.dp, if (isEnough) Color(0xFF81C784) else Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "${ore.itemName}: $totalQty/${ore.quantity}",
                                            color = if (isEnough) Color(0xFFA5D6A7) else Color(0xFFFCA5A5),
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.smeltRecipe(recipe, isAfk = false) },
                                    enabled = meetsLevel && hasMaterials,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OsrsRedFrame,
                                        disabledContainerColor = Color(0xFF2C2018)
                                    ),
                                    shape = RoundedCornerShape(3.dp),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("smelt_button_${recipe.id}"),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (!meetsLevel) "🔒 Lv ${recipe.reqLevel}" else if (!hasMaterials) "No Ore" else "Smelt 🔥",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = if (meetsLevel && hasMaterials) OsrsTextYellow else Color.Gray
                                    )
                                }

                                Button(
                                    onClick = { viewModel.toggleAfkSmelting(recipe) },
                                    enabled = meetsLevel && hasMaterials,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isThisRecipeAfk) Color(0xFF1B5E20) else OsrsLeatherMedium
                                    ),
                                    shape = RoundedCornerShape(3.dp),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("afk_smelt_button_${recipe.id}"),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (isThisRecipeAfk) "⚡ STOP" else "⚡ AFK",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = if (isThisRecipeAfk) Color(0xFFA5D6A7) else OsrsTextYellow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedSectionIndex == 2) {
            // --- ANVIL WEAPONS & ARMOR ---
            item {
                // Tier Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tiers = listOf("Bronze", "Iron", "Steel", "Opalite", "Amethyst", "Aetherite")
                    tiers.forEach { tier ->
                        val isSelected = selectedMetalTier == tier
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) OsrsRedFrame else OsrsLeatherMedium,
                            border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color(0xFF4A3828)),
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .testTag("metal_tier_tab_$tier")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Button(
                                    onClick = { selectedMetalTier = tier },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = tier,
                                        color = if (isSelected) OsrsTextYellow else Color.Gray,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val anvilRecipes = SmithingData.getAnvilRecipesForTier(selectedMetalTier)

            items(anvilRecipes) { recipe ->
                val meetsLevel = smithLvl >= recipe.reqLevel
                val invBars = inventoryItems.filter { it.id == recipe.barItemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == com.example.data.models.DefaultItems.normalizeItemId(recipe.barItemId) }.sumOf { it.quantity }
                val bankBars = bankItems.filter { it.id == recipe.barItemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == com.example.data.models.DefaultItems.normalizeItemId(recipe.barItemId) }.sumOf { it.quantity }
                val totalBars = invBars + bankBars
                val hasBars = totalBars >= recipe.barsRequired
                val createdCount = getTotalCount(recipe.outputItemId)
                val isThisRecipeAfk = isAfkSmithingAnvilActive && activeSmithAnvilRecipe?.id == recipe.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("anvil_card_${recipe.id}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isThisRecipeAfk) Color(0xFF1E351E) else Color(0xFF221810)
                    ),
                    border = BorderStroke(1.dp, if (isThisRecipeAfk) Color(0xFF81C784) else if (meetsLevel) Color(0xFF5A4532) else Color(0xFF3A2B1E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(recipe.iconEmoji, fontSize = 15.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = recipe.name,
                                        color = if (meetsLevel) OsrsTextYellow else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                    if (recipe.combatPower > 0) {
                                        Surface(color = Color(0xFF1E2818), shape = RoundedCornerShape(3.dp), border = BorderStroke(0.5.dp, Color(0xFF81C784))) {
                                            Text("+${recipe.combatPower}A", color = Color(0xFFA5D6A7), fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))
                                        }
                                    }
                                    if (recipe.defPower > 0) {
                                        Surface(color = Color(0xFF102838), shape = RoundedCornerShape(3.dp), border = BorderStroke(0.5.dp, Color(0xFF64B5F6))) {
                                            Text("+${recipe.defPower}D", color = Color(0xFF90CAF9), fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))
                                        }
                                    }
                                    Text(
                                        text = "• +${recipe.xpReward} XP",
                                        color = Color(0xFF81C784),
                                        fontSize = 8.5.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Owned: $createdCount", color = OsrsGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (meetsLevel) Color(0xFF1B5E20) else Color(0xFF3E2D1D))
                                        .border(0.5.dp, if (meetsLevel) Color(0xFF81C784) else OsrsGold.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("Lv ${recipe.reqLevel}", color = if (meetsLevel) Color(0xFFA5D6A7) else OsrsParchment, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Bars Needed and Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bars: ${recipe.barsRequired}x (Inv: $invBars / Total: $totalBars)",
                                color = if (hasBars) Color(0xFFA5D6A7) else Color(0xFFFCA5A5),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.smithAnvilRecipe(recipe, isAfk = false) },
                                    enabled = meetsLevel && hasBars,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OsrsRedFrame,
                                        disabledContainerColor = Color(0xFF2C2018)
                                    ),
                                    shape = RoundedCornerShape(3.dp),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("anvil_smith_button_${recipe.id}"),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (!meetsLevel) "🔒 Lv ${recipe.reqLevel}" else if (!hasBars) "No Bars" else "Forge 🔨",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = if (meetsLevel && hasBars) OsrsTextYellow else Color.Gray
                                    )
                                }

                                Button(
                                    onClick = { viewModel.toggleAfkSmithingAnvil(recipe) },
                                    enabled = meetsLevel && hasBars,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isThisRecipeAfk) Color(0xFF1B5E20) else OsrsLeatherMedium
                                    ),
                                    shape = RoundedCornerShape(3.dp),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("afk_anvil_button_${recipe.id}"),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (isThisRecipeAfk) "⚡ STOP" else "⚡ AFK",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = if (isThisRecipeAfk) Color(0xFFA5D6A7) else OsrsTextYellow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (inspectedQuarryForReqs != null) {
        val area = inspectedQuarryForReqs!!
        val hasLevel = smithLvl >= area.reqLevel
        val hasTotem = area.reqTotemId == null || viewModel.isTotemUnlocked(area.reqTotemId)
        val isFullUnlocked = hasLevel && hasTotem

        GemologyQuarryRequirementsDialog(
            area = area,
            userMiningLevel = smithLvl,
            hasTotem = hasTotem,
            isFullUnlocked = isFullUnlocked,
            onDismiss = { inspectedQuarryForReqs = null },
            onSelectArea = {
                viewModel.selectGemologyArea(area.id)
                inspectedQuarryForReqs = null
            }
        )
    }

    if (inspectedMineralForDetails != null) {
        val mineral = inspectedMineralForDetails!!
        val invQty = inventoryItems.find { it.id == mineral.id }?.quantity ?: 0
        val bankQty = bankItems.find { it.id == mineral.id }?.quantity ?: 0

        MineralOreDetailsDialog(
            mineral = mineral,
            userMiningLevel = smithLvl,
            invQty = invQty,
            bankQty = bankQty,
            onDismiss = { inspectedMineralForDetails = null },
            onMineNow = {
                viewModel.setSelectedOreId(mineral.id)
                viewModel.mineAtPohQuarry(mineral.id, isAfk = false)
                inspectedMineralForDetails = null
            }
        )
    }

    if (showBonusBreakdownDialog) {
        BonusBreakdownDialog(
            title = "Double Ore Mining Chance",
            categoryName = "Gemology Quarry & Mining",
            iconEmoji = "⚒️",
            sources = listOf(
                BonusSourceDetail(
                    title = "Arlg the Master Smith's Favor (Lv. $arlgFavorLvl)",
                    description = "Grants +1% chance per favor level to mine double ores and rare uncut gems from Gemology Quarry nodes (Up to +50%).",
                    bonusPercent = arlgFavorLvl,
                    emoji = "⚒️",
                    isUnlocked = true
                )
            ),
            note = "When triggered, an additional ore or mineral is extracted from the mining node without consuming extra tools!",
            onDismiss = { showBonusBreakdownDialog = false }
        )
    }
}

/**
 * Interactive OSRS-styled World Map showing Gemology Mining Quarry Areas.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GemologyQuarryWorldMapCard(
    selectedAreaId: String,
    userMiningLevel: Int,
    isTotemUnlocked: (String?) -> Boolean,
    onSelectArea: (String) -> Unit,
    onLongPressArea: (GemologyArea) -> Unit
) {
    val areas = AdventuringStoryData.GEMOLOGY_AREAS

    val infiniteTransition = rememberInfiniteTransition(label = "quarry_map_ping")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_quarry"
    )

    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E130D)),
        border = BorderStroke(1.dp, OsrsGold),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gemology_quarry_world_map")
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
                        "Gemology Realm Quarry Map",
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
                        color = Color(0xFFFFB74D),
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
                    .border(0.5.dp, Color(0xFF4E342E), RoundedCornerShape(6.dp))
                    .horizontalScroll(horizontalScrollState)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = mapWidthDp, height = mapHeightDp)
                        .background(Color(0xFF140D08))
                ) {
                    // Background Image Layer for Gemology Mining Quarry
                    Image(
                        painter = painterResource(id = R.drawable.img_gemology_map_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.38f)
                    )

                    // 1. Canvas layer for cavern grid & connecting minecart rails
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthPx = size.width
                        val heightPx = size.height

                        // Draw grid lines
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val gridColor = Color(0xFF2E1A11)

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

                        // Draw connecting mine track paths between quarry areas
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
                            color = Color(0xFFFFB74D).copy(alpha = 0.45f),
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
                            "⛏️ Subterranean Ore Quarries",
                            color = Color(0xFF5D4037),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Render Location Pins
                    areas.forEach { area ->
                        val isSelected = area.id == selectedAreaId
                        val hasTotem = isTotemUnlocked(area.reqTotemId)
                        val hasLevel = userMiningLevel >= area.reqLevel
                        val isAccessible = hasLevel && hasTotem
                        val isObeliskLocked = !hasTotem
                        val isLevelLocked = !hasLevel && hasTotem

                        val pinBgColor = when {
                            isSelected -> Color(0xFF4E342E)
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

                        val pinXDp = (mapWidthDp * area.posXRatio - 45.dp).coerceIn(4.dp, mapWidthDp - 115.dp)
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
                                                fontSize = 9.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
 * Requirements and details dialog shown when tapping/long-pressing an area in Gemology Quarry.
 */
@Composable
private fun GemologyQuarryRequirementsDialog(
    area: GemologyArea,
    userMiningLevel: Int,
    hasTotem: Boolean,
    isFullUnlocked: Boolean,
    onDismiss: () -> Unit,
    onSelectArea: () -> Unit
) {
    val hasLevel = userMiningLevel >= area.reqLevel

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E130D)),
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

                HorizontalDivider(color = Color(0xFF4E342E), thickness = 1.dp)

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
                        .background(Color(0xFF140D08), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFF4E342E), RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📜 Access Requirements:", color = Color(0xFFFFB74D), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // 1. Level Requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• Forging/Mining Level:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                        Text(
                            text = if (hasLevel) "✓ Lv. ${area.reqLevel} (Met)" else "✗ Lv. ${area.reqLevel} (Current: $userMiningLevel)",
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
                            Text("✓ None (Default Quarry)", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            text = "💡 Unlock Tip: Obtain the ${area.reqTotemName} to access this cavern!",
                            color = Color(0xFFFFCC80),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                // Mineable Minerals in this Area
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("💎 Mineable Minerals & Gems in this Quarry:", color = OsrsTextYellow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    area.minerals.forEach { mineral ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${mineral.emoji} ${mineral.name} (Lv. ${mineral.reqLevel})", color = Color(0xFFE0E0E0), fontSize = 10.5.sp)
                            Text("${mineral.dropChancePercent}% rate (+${mineral.xp} XP)", color = Color(0xFF81C784), fontSize = 10.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFullUnlocked) Color(0xFFE65100) else Color(0xFF3E2723))
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

/**
 * Mineral / Ore Details Dialog triggered when holding down (long-press) on any ore/mineral in the Forging/Gemology Quarry tab.
 */
@Composable
private fun MineralOreDetailsDialog(
    mineral: com.example.data.models.GemologyMineral,
    userMiningLevel: Int,
    invQty: Int,
    bankQty: Int,
    onDismiss: () -> Unit,
    onMineNow: () -> Unit
) {
    val canMine = userMiningLevel >= mineral.reqLevel

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E130D)),
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
                        Text(mineral.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = mineral.name,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (canMine) "✓ Unlocked (Lv. ${mineral.reqLevel})" else "🔒 Requires Lv. ${mineral.reqLevel} Forging/Mining",
                                color = if (canMine) Color(0xFF81C784) else Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Text("✕", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFF4E342E), thickness = 1.dp)

                // Lore / Description
                Text(
                    text = mineral.description,
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
                        color = Color(0xFF140D08),
                        border = BorderStroke(0.5.dp, Color(0xFF4E342E)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✨ XP Gain", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("+${mineral.xp} XP", color = OsrsTextYellow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF140D08),
                        border = BorderStroke(0.5.dp, Color(0xFF4E342E)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎯 Mining Yield", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${mineral.dropChancePercent}%", color = Color(0xFF4CAF50), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF140D08),
                        border = BorderStroke(0.5.dp, Color(0xFF4E342E)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⛏️ Tool Needed", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Pickaxe", color = Color(0xFF80DEEA), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Drops Table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF140D08), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFF4E342E), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("📦 Items Dropped When Mined:", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(mineral.emoji, fontSize = 13.sp)
                            Text("1x ${mineral.name}", color = Color.White, fontSize = 11.sp)
                        }
                        Text("100% (Guaranteed)", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    if (mineral.bonusSecondItemId != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(mineral.bonusSecondItemEmoji ?: "💎", fontSize = 13.sp)
                                Text("+${mineral.bonusSecondItemQty}x ${mineral.bonusSecondItemName ?: mineral.bonusSecondItemId}", color = Color(0xFFFFF59D), fontSize = 11.sp)
                            }
                            Text("Bonus Drop", color = Color(0xFFFFD54F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        "🔥 Metallurgical Use: Smelt into metal bars at the furnace, then forge into weapons and armor at the anvil.",
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
                        onClick = onMineNow,
                        enabled = canMine,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canMine) Color(0xFF1B5E20) else Color(0xFF37474F)
                        )
                    ) {
                        Text(
                            text = if (canMine) "Mine Now ⛏️" else "Locked 🔒",
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
