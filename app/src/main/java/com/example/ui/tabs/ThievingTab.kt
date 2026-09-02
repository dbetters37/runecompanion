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
import com.example.data.models.*
import com.example.ui.components.dashedBorder
import com.example.ui.components.CooldownActionButton
import com.example.ui.components.BonusBreakdownDialog
import com.example.ui.components.BonusSourceDetail
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThievingTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val isAfkThieving by viewModel.isAfkThievingActive.collectAsStateWithLifecycle()
    val isAfkSepulchre by viewModel.isAfkSepulchreActive.collectAsStateWithLifecycle()
    val selectedNpcId by viewModel.selectedThievingNpcId.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val npcFavorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    val renFavorLvl = npcFavorMap["ren"]?.first ?: viewModel.getNpcFavorLevel("ren")

    val thievingXp = skillXpMap[OsrsSkill.THIEVING] ?: 0L
    val thievingLvl = OsrsXpCalculator.getLevelForXp(thievingXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel((thievingLvl + 1).coerceAtMost(99))
    val currentLevelBaseXp = OsrsXpCalculator.getXpForLevel(thievingLvl)
    val progress = if (thievingLvl >= 99) 1f else ((thievingXp - currentLevelBaseXp).toFloat() / (nextLevelXp - currentLevelBaseXp).coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

    val agilityXp = skillXpMap[OsrsSkill.AGILITY] ?: 0L
    val agilityLvl = OsrsXpCalculator.getLevelForXp(agilityXp)
    val isDesertTreasureCompleted = petState.completedQuestIds.contains("quest_desert_treasure")

    var selectedDistrictId by remember { mutableStateOf("dist_town") }
    var inspectedDistrictForReqs by remember { mutableStateOf<TrickeryDistrict?>(null) }
    var showBonusBreakdownDialog by remember { mutableStateOf(false) }

    val isDistrictUnlocked: (TrickeryDistrict) -> Boolean = { dist ->
        val levelMet = thievingLvl >= dist.reqLevel
        val totemMet = dist.reqTotemId == null || viewModel.isTotemUnlocked(dist.reqTotemId)
        levelMet && totemMet
    }

    val onSelectOrInspectDistrict: (String) -> Unit = { districtId ->
        val dist = TRICKERY_DISTRICTS.find { it.id == districtId }
        if (dist != null && !isDistrictUnlocked(dist)) {
            inspectedDistrictForReqs = dist
        } else {
            selectedDistrictId = districtId
        }
    }

    val currentDistrict = remember(selectedDistrictId) {
        TRICKERY_DISTRICTS.find { it.id == selectedDistrictId } ?: TRICKERY_DISTRICTS.first()
    }

    val stolenLootItems = remember(inventoryItems, bankItems) {
        val itemsMap = mutableMapOf<String, Triple<com.example.data.models.InventoryItem, Int, Int>>()
        inventoryItems.forEach { item ->
            if (item.quantity > 0 && isStolenOrThievingItem(item)) {
                itemsMap[item.id] = Triple(item, item.quantity, 0)
            }
        }
        bankItems.forEach { bItem ->
            if (bItem.quantity > 0 && isStolenOrThievingItem(bItem)) {
                val existing = itemsMap[bItem.id]
                if (existing != null) {
                    itemsMap[bItem.id] = Triple(existing.first, existing.second, bItem.quantity)
                } else {
                    itemsMap[bItem.id] = Triple(bItem.copy(quantity = 0), 0, bItem.quantity)
                }
            }
        }
        itemsMap.values.map { (item, invQty, bankQty) ->
            TrickeryLootEntry(item, invQty + bankQty)
        }.sortedByDescending { it.totalQty }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .padding(6.dp)
            .testTag("tab_trickery"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // --- COMPACT TRICKERY HEADER CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trickery_header_card"),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
                border = BorderStroke(1.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            Text("🥷", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "Trickery & Thieving",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OsrsTextYellow
                                )
                                Text(
                                    text = "Lv. $thievingLvl Thieving • ${"%,d".format(thievingXp)} XP",
                                    fontSize = 10.sp,
                                    color = OsrsParchment
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF6A1B9A).copy(alpha = 0.4f),
                            border = BorderStroke(0.8.dp, Color(0xFFBA68C8)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clickable { showBonusBreakdownDialog = true }
                                .testTag("badge_extra_loot_chance")
                        ) {
                            Text(
                                text = "+${renFavorLvl}% Extra Loot ⓘ",
                                color = Color(0xFFE1BEE7),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Row 2: AFK Steal Toggle Button
                    CooldownActionButton(
                        onClick = { viewModel.toggleAfkThieving() },
                        cooldownMs = 600L,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAfkThieving) Color(0xFF1B5E20) else Color(0xFF3E2723)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (isAfkThieving) Color(0xFF81C784) else OsrsGold),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        Text(
                            text = if (isAfkThieving) "⚡ STOP AFK STEALING" else "🥷 START AFK STEALING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAfkThieving) Color.White else OsrsTextYellow,
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
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = OsrsTextYellow
                        )
                    }
                }
            }
        }

        // --- ALWAYS VISIBLE INTERACTIVE ROGUE REALM WORLD MAP ---
        item {
            TrickeryWorldMapCard(
                selectedDistrictId = selectedDistrictId,
                userThievingLevel = thievingLvl,
                isDistrictUnlocked = isDistrictUnlocked,
                isTotemUnlocked = { viewModel.isTotemUnlocked(it) },
                onSelectDistrict = onSelectOrInspectDistrict,
                onLongPressDistrict = { inspectedDistrictForReqs = it }
            )
        }

        // --- COMPACT LOCATION SELECTOR CHIPS ---
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TRICKERY_DISTRICTS, key = { it.id }) { district ->
                    val isSelected = district.id == selectedDistrictId
                    val hasLevel = thievingLvl >= district.reqLevel
                    val hasTotem = if (district.reqTotemId != null) viewModel.isTotemUnlocked(district.reqTotemId) else true
                    val requiresObelisk = district.reqTotemId != null
                    val isAccessible = hasLevel && hasTotem
                    val isObeliskLocked = requiresObelisk && !hasTotem
                    val isLevelLocked = !hasLevel && hasTotem

                    val chipBg = when {
                        isSelected -> Color(0xFF3B2A1D)
                        isAccessible -> Color(0xFF162B18)
                        isObeliskLocked || isLevelLocked -> Color(0xFF2C260D)
                        else -> Color(0xFF1E140D)
                    }

                    val chipBorder = when {
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
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = chipBorder
                        ) else null,
                        modifier = Modifier
                            .alpha(chipAlpha)
                            .then(
                                if (isDotted) Modifier.dashedBorder(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = chipBorder,
                                    shape = RoundedCornerShape(4.dp),
                                    dashLength = 3.dp,
                                    gapLength = 3.dp
                                ) else Modifier
                            )
                            .clickable { onSelectOrInspectDistrict(district.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(district.emoji, fontSize = 11.sp)
                            Text(
                                district.name,
                                color = if (isSelected) OsrsTextYellow else if (isAccessible) OsrsTextWhite else if (isObeliskLocked || isLevelLocked) Color(0xFFFFF59D) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            if (requiresObelisk) {
                                Text(
                                    text = district.reqTotemEmoji ?: "🏆",
                                    fontSize = 9.sp
                                )
                            }
                            Text(
                                "Lv.${district.reqLevel}",
                                color = if (isAccessible) Color(0xFF81C784) else if (isLevelLocked) Color(0xFFFFD54F) else Color(0xFFE57373),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }
        }

        // --- PICKPOCKET TARGETS IN THIS DISTRICT (SLAYER TASK STYLE STACKED ROWS) ---
        item {
            val hasAreaLevel = thievingLvl >= currentDistrict.reqLevel
            val hasAreaTotem = if (currentDistrict.reqTotemId != null) viewModel.isTotemUnlocked(currentDistrict.reqTotemId) else true
            val requiresAreaObelisk = currentDistrict.reqTotemId != null
            val isAreaObeliskLocked = requiresAreaObelisk && !hasAreaTotem
            val isAreaLevelLocked = !hasAreaLevel && hasAreaTotem
            val isAreaDotted = isAreaObeliskLocked || isAreaLevelLocked
            val areaCardAlpha = if (isAreaObeliskLocked) 0.30f else 1.0f

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E140D)),
                border = if (!isAreaDotted) BorderStroke(1.dp, Color(0xFF5A3E25)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(areaCardAlpha)
                    .then(
                        if (isAreaDotted) Modifier.dashedBorder(
                            width = 1.dp,
                            color = Color(0xFFFFD54F),
                            shape = RoundedCornerShape(8.dp),
                            dashLength = 4.dp,
                            gapLength = 4.dp
                        ) else Modifier
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${currentDistrict.emoji} ${currentDistrict.name} (Lv. ${currentDistrict.reqLevel}+)",
                            color = OsrsTextYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap row or button to target",
                            color = Color(0xFFA1887F),
                            fontSize = 11.sp
                        )
                    }

                    currentDistrict.npcs.forEach { npc ->
                        val canPickpocket = thievingLvl >= npc.levelReq
                        val isSelectedTarget = selectedNpcId == npc.id && isAfkThieving

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 82.dp)
                                .background(
                                    color = when {
                                        isSelectedTarget -> Color(0xFF3D2A1B)
                                        canPickpocket -> Color(0xFF281C13)
                                        else -> Color(0xFF19110B)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = if (isSelectedTarget) 1.5.dp else 1.dp,
                                    color = when {
                                        isSelectedTarget -> OsrsGold
                                        canPickpocket -> Color(0xFF5A3E25)
                                        else -> Color(0xFF3E2D1F)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(enabled = canPickpocket) {
                                    viewModel.startPickpocketingNpc(npc.id)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Badge Container
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (canPickpocket) Color(0xFF19110B) else Color(0xFF120C08),
                                border = BorderStroke(1.dp, if (isSelectedTarget) OsrsGold else Color(0xFF4A3423)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(npc.iconEmoji, fontSize = 26.sp)
                                }
                            }

                            // Info Column
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = npc.name,
                                        color = if (canPickpocket) OsrsTextYellow else Color(0xFF8D6E63),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "Lv.${npc.levelReq}",
                                        color = if (canPickpocket) Color(0xFFFFB74D) else Color(0xFF8D6E63),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isSelectedTarget) {
                                        Text("🎯", fontSize = 12.sp)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "+${npc.thievingXp} XP",
                                        color = Color(0xFF81C784),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "• ${npc.description}",
                                        color = Color(0xFFB0BEC5),
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    "💰 Loot: ${npc.lootSummary}",
                                    color = Color(0xFFFFE082),
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp
                                )
                            }

                            // Action Button
                            CooldownActionButton(
                                onClick = {
                                    if (canPickpocket) {
                                        if (isSelectedTarget) {
                                            viewModel.stopPickpocketing()
                                        } else {
                                            viewModel.startPickpocketingNpc(npc.id)
                                        }
                                    }
                                },
                                cooldownMs = 1200L,
                                enabled = canPickpocket,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        isSelectedTarget -> Color(0xFFB71C1C)
                                        canPickpocket -> Color(0xFF1B5E20)
                                        else -> Color(0xFF3E2723)
                                    },
                                    disabledContainerColor = Color(0xFF261912)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .width(86.dp)
                                    .height(38.dp)
                            ) {
                                Text(
                                    when {
                                        isSelectedTarget -> "Stop 🛑"
                                        canPickpocket -> "Steal 🥷"
                                        else -> "🔒 Lv.${npc.levelReq}"
                                    },
                                    color = if (canPickpocket) Color.White else Color(0xFF8D6E63),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- STOLEN GOODS INVENTORY SECTION ---
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221710)),
                border = BorderStroke(1.dp, Color(0xFF5A3E25)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧺", fontSize = 20.sp)
                            Text(
                                "STOLEN TREASURES INVENTORY",
                                color = OsrsTextYellow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val totalStolenCount = stolenLootItems.sumOf { it.totalQty }
                        Text(
                            "$totalStolenCount Stolen Goods (Inv + Bank)",
                            color = Color(0xFF81C784),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (stolenLootItems.isEmpty()) {
                        Text(
                            "No stolen goods or coins in inventory or bank yet. Select a district and pickpocket targets above!",
                            color = Color(0xFF8D6E63),
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            stolenLootItems.forEach { lootEntry ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF19110B),
                                    border = BorderStroke(1.dp, Color(0xFF4D3726))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(lootEntry.item.iconEmoji, fontSize = 18.sp)
                                        Column {
                                            Text(
                                                lootEntry.item.name,
                                                color = OsrsTextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "🏦 ${lootEntry.totalQty} in Bank",
                                                color = Color(0xFF90CAF9),
                                                fontSize = 9.sp
                                            )
                                        }
                                        Surface(
                                            color = OsrsGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(0.5.dp, OsrsGold)
                                        ) {
                                            Text(
                                                "x${lootEntry.totalQty}",
                                                color = OsrsTextYellow,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
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

        // --- SHAMANIC CATACOMBS CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1D13)),
                border = BorderStroke(1.5.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = OsrsLeatherMedium,
                            border = BorderStroke(1.dp, OsrsGold),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🗿", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "🗿 Shamanic Catacombs",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OsrsTextYellow
                            )
                            Text(
                                text = "Ancestral spirit crypts, elemental totems & sacred relics",
                                fontSize = 10.5.sp,
                                color = OsrsParchment
                            )
                        }
                    }

                    if (!isDesertTreasureCompleted) {
                        // Locked Quest Banner
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF3A1212),
                            border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🔒 QUEST REQUIREMENT LOCKED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8A80)
                                )
                                Text(
                                    text = "Complete 'Pyramid of the Four Elements' (Desert Treasure) quest to unlock the Sacred Shamanic Catacombs!",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFFCDD2)
                                )
                                Button(
                                    onClick = { viewModel.addChatMessage("📜 Navigate to Quests tab to complete Pyramid of the Four Elements!") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("📜 Open Quests Tab", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        val isAgilReqMet = agilityLvl >= 52
                        val isThievReqMet = thievingLvl >= 50

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• Dexterity Level 52 Requirement",
                                    fontSize = 11.sp,
                                    color = if (isAgilReqMet) Color(0xFF81C784) else Color(0xFFFF8A80)
                                )
                                Text(
                                    text = if (isAgilReqMet) "✅ Lvl $agilityLvl" else "❌ Lvl $agilityLvl / 52",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAgilReqMet) Color(0xFF81C784) else Color(0xFFFF8A80)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• Trickery Level 50 Requirement",
                                    fontSize = 11.sp,
                                    color = if (isThievReqMet) Color(0xFF81C784) else Color(0xFFFF8A80)
                                )
                                Text(
                                    text = if (isThievReqMet) "✅ Lvl $thievingLvl" else "❌ Lvl $thievingLvl / 50",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isThievReqMet) Color(0xFF81C784) else Color(0xFFFF8A80)
                                )
                            }

                            Text(
                                text = "⚡ Rewards: +180 Dexterity XP, +150 Trickery XP & Rare Seeds, Runes, Armor & Relics",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OsrsTextYellow
                            )

                            Button(
                                onClick = { viewModel.toggleAfkCatacombs() },
                                enabled = isAgilReqMet && isThievReqMet,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAfkSepulchre) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = if (isAfkSepulchre) "Stop Shamanic Catacombs AFK" else "🗿 Start Shamanic Catacombs AFK",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (inspectedDistrictForReqs != null) {
        val district = inspectedDistrictForReqs!!
        val isUnlocked = isDistrictUnlocked(district)
        val hasTotem = district.reqTotemId == null || viewModel.isTotemUnlocked(district.reqTotemId)
        val hasLevel = thievingLvl >= district.reqLevel

        TrickeryDistrictRequirementsDialog(
            district = district,
            userThievingLevel = thievingLvl,
            isUnlocked = isUnlocked,
            hasTotem = hasTotem,
            hasLevel = hasLevel,
            onDismiss = { inspectedDistrictForReqs = null },
            onSelectDistrict = {
                selectedDistrictId = district.id
                inspectedDistrictForReqs = null
            }
        )
    }

    if (showBonusBreakdownDialog) {
        BonusBreakdownDialog(
            title = "Extra Loot Thieving Chance",
            categoryName = "Trickery & Pickpocketing",
            iconEmoji = "🗡️",
            sources = listOf(
                BonusSourceDetail(
                    title = "Ren the Shadow Thief's Favor (Lv. $renFavorLvl)",
                    description = "Grants +1% chance per favor level to double stolen coins and rare loot drops while pickpocketing NPCs (Up to +50%).",
                    bonusPercent = renFavorLvl,
                    emoji = "🗡️",
                    isUnlocked = true
                )
            ),
            note = "When triggered, you receive double loot from the target without getting caught!",
            onDismiss = { showBonusBreakdownDialog = false }
        )
    }
}

/**
 * Interactive OSRS-styled World Map showing Rogue & Thief District Locations.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrickeryWorldMapCard(
    selectedDistrictId: String,
    userThievingLevel: Int,
    isDistrictUnlocked: (TrickeryDistrict) -> Boolean,
    isTotemUnlocked: (String) -> Boolean,
    onSelectDistrict: (String) -> Unit,
    onLongPressDistrict: (TrickeryDistrict) -> Unit
) {
    val districts = TRICKERY_DISTRICTS

    val infiniteTransition = rememberInfiniteTransition(label = "map_ping_trickery")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_trickery"
    )

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18120D)),
        border = BorderStroke(1.5.dp, OsrsGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        "Rogue & Thief Hideouts Map",
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
                    .border(0.5.dp, Color(0xFF3E2B1E), RoundedCornerShape(6.dp))
                    .horizontalScroll(horizontalScrollState)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = mapWidthDp, height = mapHeightDp)
                        .background(Color(0xFF110B07))
                ) {
                    // Background Image Layer for Trickery Map
                    Image(
                        painter = painterResource(id = R.drawable.img_trickery_map_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.42f)
                    )

                    // 1. Canvas layer for grid lines & connecting paths
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthPx = size.width
                        val heightPx = size.height
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val gridColor = Color(0xFF3B2515).copy(alpha = 0.5f)

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
                        districts.forEachIndexed { index, district ->
                            val px = district.posXRatio * widthPx
                            val py = district.posYRatio * heightPx
                            if (index == 0) {
                                path.moveTo(px, py)
                            } else {
                                path.lineTo(px, py)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFFFFB74D).copy(alpha = 0.5f),
                            style = Stroke(width = 3.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
                        )
                    }

                    // 2. Map Title Watermark
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            "🧭 Rogue Realm & Thief Territories",
                            color = Color(0xFFFFB74D).copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Render Location Pins
                    districts.forEach { district ->
                        val isSelected = district.id == selectedDistrictId
                        val hasLevel = userThievingLevel >= district.reqLevel
                        val hasTotem = if (district.reqTotemId != null) isTotemUnlocked(district.reqTotemId) else true
                        val requiresObelisk = district.reqTotemId != null
                        val isAccessible = hasLevel && hasTotem
                        val isObeliskLocked = requiresObelisk && !hasTotem
                        val isLevelLocked = !hasLevel && hasTotem

                        val pinBgColor = when {
                            isSelected -> Color(0xFF3B2A1D)
                            isAccessible -> Color(0xFF18241D)
                            isObeliskLocked || isLevelLocked -> Color(0xFF2C260D)
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

                        val pinXDp = (mapWidthDp * district.posXRatio - 45.dp).coerceIn(4.dp, mapWidthDp - 110.dp)
                        val pinYDp = (mapHeightDp * district.posYRatio - 22.dp).coerceIn(4.dp, mapHeightDp - 50.dp)

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
                                        onClick = { onSelectDistrict(district.id) },
                                        onLongClick = { onLongPressDistrict(district) }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(district.emoji, fontSize = 13.sp)

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = district.name,
                                                color = if (isSelected) OsrsTextYellow else if (isAccessible) OsrsTextWhite else if (isObeliskLocked || isLevelLocked) Color(0xFFFFF59D) else Color.LightGray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp
                                            )
                                            if (requiresObelisk) {
                                                Text(
                                                    district.reqTotemEmoji ?: "🏆",
                                                    fontSize = 8.5.sp
                                                )
                                            }
                                            if (isSelected) {
                                                Text(
                                                    "📍",
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }

                                        val badgeText = when {
                                            isAccessible -> "Lv.${district.reqLevel}"
                                            isObeliskLocked -> "🔒 Need Obelisk"
                                            isLevelLocked -> "🔒 Lv.${district.reqLevel}"
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
                        }
                    }
                }
            }
        }
    }
}

/**
 * Requirements and details dialog shown when tapping/long-pressing an area in Trickery.
 */
@Composable
private fun TrickeryDistrictRequirementsDialog(
    district: TrickeryDistrict,
    userThievingLevel: Int,
    isUnlocked: Boolean,
    hasTotem: Boolean,
    hasLevel: Boolean,
    onDismiss: () -> Unit,
    onSelectDistrict: () -> Unit
) {
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
                        Text(district.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = district.name,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            val statusBadge = if (isUnlocked) "✓ Full Access Granted" else "🔒 Territory Locked"
                            val statusColor = if (isUnlocked) Color(0xFF81C784) else Color(0xFFE57373)
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
                    text = district.description,
                    color = OsrsTextWhite,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )

                district.specialPerkDesc?.let { perk ->
                    Surface(
                        color = Color(0xFF2E1C0F),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF8D6E63)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✨", fontSize = 16.sp)
                            Column {
                                Text(
                                    "Special Territory Perk",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    perk,
                                    color = Color(0xFFFFF8E1),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

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

                    // 1. Totem Requirement if required
                    if (district.reqTotemId != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "• ${district.reqTotemEmoji ?: "🏆"} ${district.reqTotemName ?: "Shaman Obelisk"}:",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (hasTotem) "✓ Attained & Active" else "✗ Not Yet Claimed",
                                color = if (hasTotem) Color(0xFF81C784) else Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!hasTotem) {
                            Text(
                                text = "Defeat the Shaman Path Gym Trial to earn the ${district.reqTotemName ?: "Obelisk"}.",
                                color = Color(0xFFFFCC80),
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    // 2. Level Requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• Trickery (Thieving) Level:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                        Text(
                            text = if (hasLevel) "✓ Lv. ${district.reqLevel} (Met)" else "✗ Lv. ${district.reqLevel} (Current: $userThievingLevel)",
                            color = if (hasLevel) Color(0xFF81C784) else Color(0xFFE57373),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Targets in this District
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🥷 Targets in this Territory:", color = OsrsTextYellow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    district.npcs.forEach { npc ->
                        val meetsNpcLevel = userThievingLevel >= npc.levelReq
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(npc.iconEmoji, fontSize = 11.sp)
                                Text(
                                    text = "${npc.name} (Lv. ${npc.levelReq})",
                                    color = if (meetsNpcLevel) Color(0xFFE0E0E0) else Color.Gray,
                                    fontSize = 10.5.sp
                                )
                            }
                            Text(
                                text = "+${npc.thievingXp} XP",
                                color = if (meetsNpcLevel) Color(0xFF81C784) else Color(0xFFE57373),
                                fontSize = 10.sp
                            )
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
                        onClick = onSelectDistrict,
                        modifier = Modifier.weight(1.2f),
                        enabled = isUnlocked,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isUnlocked) Color(0xFF6A1B9A) else Color(0xFF3E2723))
                    ) {
                        Text(
                            text = if (isUnlocked) "Travel Here 🧭" else "Locked 🔒",
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

private fun isStolenOrThievingItem(item: com.example.data.models.InventoryItem): Boolean {
    val id = item.id.lowercase()
    val name = item.name.lowercase()
    return id.contains("coin") || id.contains("gp") || id.contains("gem") || id.contains("ruby") ||
           id.contains("sapphire") || id.contains("emerald") || id.contains("diamond") ||
           id.contains("rune") || id.contains("seed") || id.contains("silk") || id.contains("bar") ||
           id.contains("plank") || id.contains("lockpick") || id.contains("relic") || id.contains("ore") ||
           id.contains("talisman") || id.contains("chalice") || id.contains("stolen") ||
           name.contains("coin") || name.contains("rune") || name.contains("seed") || name.contains("bar") ||
           name.contains("gem") || name.contains("silk") || name.contains("lockpick") || name.contains("pouch")
}
