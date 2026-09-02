package com.example.ui.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.components.BramNpcCompanion
import com.example.ui.components.OsrsItemIcon
import com.example.ui.components.WoodPlankPanel
import com.example.ui.components.WoodHeaderBanner
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FletchingTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val isAfkFletchingActive by viewModel.isAfkFletchingActive.collectAsStateWithLifecycle()
    val isAfkShaftCraftingActive by viewModel.isAfkShaftCraftingActive.collectAsStateWithLifecycle()
    val isAfkFeatherCraftingActive by viewModel.isAfkFeatherCraftingActive.collectAsStateWithLifecycle()
    val isAfkBowstringCraftingActive by viewModel.isAfkBowstringCraftingActive.collectAsStateWithLifecycle()
    val isAfkArrowtipCraftingActive by viewModel.isAfkArrowtipCraftingActive.collectAsStateWithLifecycle()
    val selectedShaftLogId by viewModel.selectedShaftLogId.collectAsStateWithLifecycle()
    val selectedArrowtipBarId by viewModel.selectedArrowtipBarId.collectAsStateWithLifecycle()
    val isAfkTrapCraftingActive by viewModel.isAfkTrapCraftingActive.collectAsStateWithLifecycle()
    val selectedCraftingTrapId by viewModel.selectedCraftingTrapId.collectAsStateWithLifecycle()

    val fletchXp = skillXpMap[OsrsSkill.FLETCHING] ?: 0L
    val fletchLvl = OsrsXpCalculator.getLevelForXp(fletchXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel((fletchLvl + 1).coerceAtMost(99))
    val currentLevelBaseXp = OsrsXpCalculator.getXpForLevel(fletchLvl)
    val progress = if (fletchLvl >= 99) 1f else ((fletchXp - currentLevelBaseXp).toFloat() / (nextLevelXp - currentLevelBaseXp).coerceAtLeast(1L)).coerceIn(0f, 1f)

    var selectedSectionIndex by remember { mutableStateOf(0) } // 0 = Arrows, 1 = Bows, 2 = Whittling Bench, 3 = Traps

    // Helper to get total owned count of an item in inventory + bank
    fun getTotalCount(itemId: String): Int {
        val norm = com.example.data.models.DefaultItems.normalizeItemId(itemId)
        val inv = inventoryItems.filter { it.id == itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == norm }.sumOf { it.quantity }
        val bank = bankItems.filter { it.id == itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == norm }.sumOf { it.quantity }
        return inv + bank
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- FLETCHING HEADER CARD ---
        item {
            WoodPlankPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fletching_header_card"),
                accentIcon = "🎯",
                borderColor = OsrsGold,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Row 1: Title, Icon & XP Badge
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
                            shape = CircleShape,
                            color = OsrsLeatherMedium,
                            border = BorderStroke(1.dp, OsrsGold),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎯", fontSize = 18.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Whittling & Fletching",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Level $fletchLvl Fletching",
                                color = OsrsParchment,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    Surface(
                        color = OsrsLeatherMedium,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, OsrsGold)
                    ) {
                        Text(
                            text = "${String.format("%,d", fletchXp)} XP",
                            color = OsrsGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Level Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level $fletchLvl Progress",
                            color = OsrsParchment,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = OsrsGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = OsrsTextGreen,
                        trackColor = Color(0xFF1E1510)
                    )
                }
            }
        }

        // --- MATERIAL INVENTORY & BANK QUICK COUNTER ---
        item {
            WoodPlankPanel(
                modifier = Modifier.fillMaxWidth(),
                borderColor = OsrsGold.copy(alpha = 0.5f),
                accentIcon = "🪵",
                contentPadding = PaddingValues(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦 Material Reserves (Inventory + Bank)",
                        color = OsrsGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Auto-Synced",
                        color = OsrsParchment,
                        fontSize = 9.5.sp
                    )
                }

                    val keyMaterials = listOf(
                        Triple("item_arrow_shaft", "Shafts", "🪵"),
                        Triple("item_feather", "Feathers", "🪶"),
                        Triple("item_headless_arrow", "Headless", "🏹"),
                        Triple("item_bowstring", "Bowstring", "🧵"),
                        Triple("item_bronze_arrowtip", "Bronze Tips", "🗡️"),
                        Triple("item_iron_arrowtip", "Iron Tips", "🗡️"),
                        Triple("item_steel_arrowtip", "Steel Tips", "🗡️"),
                        Triple("item_mithril_arrowtip", "Opalite Tips", "🗡️"),
                        Triple("item_adamant_arrowtip", "Amethyst Tips", "🗡️"),
                        Triple("item_rune_arrowtip", "Aetherite Tips", "🗡️"),
                        Triple("item_dragon_arrowtip", "Dragon Tips", "🐉")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        keyMaterials.forEach { (itemId, label, emoji) ->
                            val invCount = inventoryItems.find { it.id == itemId }?.quantity ?: 0
                            val totalCount = getTotalCount(itemId)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF221810),
                                border = BorderStroke(1.dp, if (totalCount > 0) OsrsGold.copy(alpha = 0.6f) else Color(0xFF4A3828)),
                                modifier = Modifier.combinedClickable(
                                    onClick = { viewModel.inspectItemObtain(itemId) },
                                    onLongClick = { viewModel.inspectItemObtain(itemId) }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(emoji, fontSize = 11.sp)
                                    Text(
                                        text = "$label: $invCount",
                                        color = if (invCount > 0) OsrsTextYellow else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (totalCount > invCount) {
                                        Text(
                                            text = "(Bank: ${totalCount - invCount})",
                                            color = OsrsGold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // --- ARROW TIER DAMAGE SHOWCASE BANNER ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2818)),
                border = BorderStroke(1.dp, Color(0xFF81C784))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "Arrow Tier Combat Damage Scale",
                            color = Color(0xFFA5D6A7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Bronze (+2) • Iron (+5) • Steel (+9) • Opalite (+16) • Amethyst (+24) • Aetherite (+35) • Dragon (+48 Boost)",
                            color = OsrsParchment,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // --- CATEGORY SELECTOR TABS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { selectedSectionIndex = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 0) OsrsRedFrame else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .border(1.dp, if (selectedSectionIndex == 0) OsrsGold else Color(0xFF4A3828), RoundedCornerShape(6.dp)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🏹 Arrows",
                        color = if (selectedSectionIndex == 0) OsrsTextYellow else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { selectedSectionIndex = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 1) OsrsRedFrame else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .border(1.dp, if (selectedSectionIndex == 1) OsrsGold else Color(0xFF4A3828), RoundedCornerShape(6.dp)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🪵 Bows",
                        color = if (selectedSectionIndex == 1) OsrsTextYellow else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { selectedSectionIndex = 2 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 2) OsrsRedFrame else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(36.dp)
                        .border(1.dp, if (selectedSectionIndex == 2) OsrsGold else Color(0xFF4A3828), RoundedCornerShape(6.dp)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🪓 Whittling",
                        color = if (selectedSectionIndex == 2) OsrsTextYellow else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { selectedSectionIndex = 3 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 3) OsrsRedFrame else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(36.dp)
                        .border(1.dp, if (selectedSectionIndex == 3) OsrsGold else Color(0xFF4A3828), RoundedCornerShape(6.dp)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🪤 Traps",
                        color = if (selectedSectionIndex == 3) OsrsTextYellow else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- SECTION 0 & 1: ARROWS & BOWS RECIPES ---
        if (selectedSectionIndex == 0 || selectedSectionIndex == 1) {
            val recipes = if (selectedSectionIndex == 0) FletchingData.ARROW_RECIPES else FletchingData.BOW_RECIPES

            items(recipes) { recipe ->
                val meetsLevel = fletchLvl >= recipe.reqLevel

                // Check if player has all input materials in inventory/bank
                var hasMaterialsInInventory = true
                val materialStatusList = recipe.inputMaterials.map { mat ->
                    val invQty = viewModel.getItemQuantityCombined(mat.itemId)
                    val total = invQty
                    val currentInInv = inventoryItems.find { it.id == mat.itemId }?.quantity ?: 0
                    if (currentInInv < mat.quantity) {
                        hasMaterialsInInventory = false
                    }
                    Triple(mat, currentInInv, total)
                }

                val createdCount = getTotalCount(recipe.outputItemId)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fletch_card_${recipe.id}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF221810)),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (meetsLevel) Color(0xFF5A4532) else Color(0xFF3A2B1E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .combinedClickable(
                                        onClick = { viewModel.inspectItemObtain(recipe.outputItemId) },
                                        onLongClick = { viewModel.inspectItemObtain(recipe.outputItemId) }
                                    )
                            ) {
                                Text(recipe.iconEmoji, fontSize = 22.sp)
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = recipe.name,
                                            color = if (meetsLevel) OsrsTextYellow else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (recipe.rangedPowerBonus > 0) {
                                            Surface(
                                                color = Color(0xFF1E2818),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, Color(0xFF81C784))
                                            ) {
                                                Text(
                                                    text = "+${recipe.rangedPowerBonus} Dmg",
                                                    color = Color(0xFFA5D6A7),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = recipe.description,
                                        color = OsrsParchment,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                // Level Requirement Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (meetsLevel) Color(0xFF1B5E20) else Color(0xFF3E2D1D))
                                        .border(1.dp, if (meetsLevel) Color(0xFF81C784) else OsrsGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Lvl ${recipe.reqLevel}",
                                        color = if (meetsLevel) Color(0xFFA5D6A7) else OsrsParchment,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Owned Count Badge
                                Text(
                                    text = "Owned: $createdCount",
                                    color = OsrsGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Required Materials Section
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Required Materials:",
                                color = OsrsGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                materialStatusList.forEach { (mat, invQty, totalQty) ->
                                    val isEnoughInInv = invQty >= mat.quantity
                                    val isEnoughTotal = totalQty >= mat.quantity
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isEnoughInInv) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color(0xFF381C10))
                                            .border(
                                                width = 1.dp,
                                                color = if (isEnoughInInv) Color(0xFF81C784) else Color(0xFFEF4444).copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .combinedClickable(
                                                onClick = { viewModel.inspectItemObtain(mat.itemId) },
                                                onLongClick = { viewModel.inspectItemObtain(mat.itemId) }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(mat.emoji, fontSize = 11.sp)
                                            Text(
                                                text = "${mat.itemName}: $invQty/${mat.quantity}",
                                                color = if (isEnoughInInv) Color(0xFFA5D6A7) else Color(0xFFFCA5A5),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (!isEnoughInInv && isEnoughTotal) {
                                                Text(
                                                    text = "($totalQty in Bank)",
                                                    color = OsrsGold,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.fletchRecipe(recipe, isAfk = false) },
                                enabled = meetsLevel && hasMaterialsInInventory,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OsrsRedFrame,
                                    disabledContainerColor = Color(0xFF2C2018)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("fletch_button_${recipe.id}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (!meetsLevel) "Requires Lvl ${recipe.reqLevel}" else if (!hasMaterialsInInventory) "Missing Materials in Inv" else "CRAFT ${recipe.outputQuantity}x (+${recipe.xpReward} XP)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = if (meetsLevel && hasMaterialsInInventory) OsrsTextYellow else Color.Gray
                                )
                            }

                            Button(
                                onClick = { viewModel.toggleAfkFletching(recipe) },
                                enabled = meetsLevel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAfkFletchingActive) Color(0xFF1B5E20) else OsrsLeatherMedium
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("afk_fletch_button_${recipe.id}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAfkFletchingActive) "🛑 STOP AFK" else "⚡ AFK",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = if (isAfkFletchingActive) Color(0xFFA5D6A7) else OsrsTextYellow
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: WHITTLING & MATERIAL PREPARATION BENCH ---
        if (selectedSectionIndex == 2) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whittling_bench_card"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
                    border = BorderStroke(1.dp, OsrsGold)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🪓 Whittling & Material Bench",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Whittle raw logs into arrow shafts, spin bowstrings, trim feathers, and forge metal arrowtips.",
                            color = OsrsParchment,
                            fontSize = 11.sp
                        )

                        HorizontalDivider(color = Color(0xFF4A3828))

                        // 1. ARROW SHAFT WHITTLING
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF221810)),
                            border = BorderStroke(1.dp, Color(0xFF5A4532))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🪵 Arrow Shaft Whittling", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("1 Log ➔ 15 Shafts (+25 XP)", color = OsrsGold, fontSize = 10.sp)
                                }

                                val availableLogs = listOf(
                                    Pair("item_logs", "Normal Logs"),
                                    Pair("item_birch_logs", "Birch Logs"),
                                    Pair("item_oak_logs", "Oak Logs"),
                                    Pair("item_pine_logs", "Pine Logs"),
                                    Pair("item_willow_logs", "Willow Logs"),
                                    Pair("item_cedar_logs", "Cedar Logs"),
                                    Pair("item_maple_logs", "Maple Logs"),
                                    Pair("item_yew_logs", "Yew Logs"),
                                    Pair("item_ironwood_logs", "Ironwood Logs"),
                                    Pair("item_magic_logs", "Magic Logs"),
                                    Pair("item_redwood_logs", "Redwood Logs")
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(availableLogs) { (logId, label) ->
                                        val count = getTotalCount(logId)
                                        val isSel = selectedShaftLogId == logId
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSel) Color(0xFF3B2A1C) else Color(0xFF1A120C),
                                            border = BorderStroke(1.dp, if (isSel) OsrsGold else if (count > 0) Color(0xFF6B533E) else Color.DarkGray),
                                            modifier = Modifier.clickable { viewModel.setSelectedShaftLogId(logId) }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(label, color = if (isSel) OsrsTextYellow else OsrsParchment, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                                Text("x$count", color = if (count > 0) Color(0xFFA5D6A7) else Color.Gray, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.craftLogsToShafts(isAfk = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text("Whittle Shafts (15x)", fontSize = 10.sp, color = OsrsTextYellow, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.toggleAfkShaftCrafting() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAfkShaftCraftingActive) Color(0xFF1B5E20) else OsrsLeatherMedium
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = if (isAfkShaftCraftingActive) "🛑 Stop AFK" else "⚡ AFK Shafts",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAfkShaftCraftingActive) Color(0xFFA5D6A7) else OsrsTextYellow
                                        )
                                    }
                                }
                            }
                        }

                        // 2. FEATHER TRIMMING & BOWSTRING SPINNING
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF221810)),
                                border = BorderStroke(1.dp, Color(0xFF5A4532))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("🪶 Trim Feathers", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Owned: ${getTotalCount("item_feather")}", color = OsrsGold, fontSize = 9.5.sp)
                                    Button(
                                        onClick = { viewModel.craftFeathers() },
                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth().height(28.dp),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("Trim Feathers", fontSize = 9.5.sp, color = OsrsTextYellow, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.toggleAfkFeatherCrafting() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAfkFeatherCraftingActive) Color(0xFF1B5E20) else OsrsLeatherMedium
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth().height(28.dp),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text(if (isAfkFeatherCraftingActive) "🛑 Stop" else "⚡ AFK Feathers", fontSize = 9.5.sp, color = if (isAfkFeatherCraftingActive) Color(0xFFA5D6A7) else OsrsTextYellow)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF221810)),
                                border = BorderStroke(1.dp, Color(0xFF5A4532))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("🧵 Spin Bowstrings", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Owned: ${getTotalCount("item_bowstring")}", color = OsrsGold, fontSize = 9.5.sp)
                                    Button(
                                        onClick = { viewModel.craftBowstrings() },
                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth().height(28.dp),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("Spin Bowstring", fontSize = 9.5.sp, color = OsrsTextYellow, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.toggleAfkBowstringCrafting() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAfkBowstringCraftingActive) Color(0xFF1B5E20) else OsrsLeatherMedium
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth().height(28.dp),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text(if (isAfkBowstringCraftingActive) "🛑 Stop" else "⚡ AFK Bowstring", fontSize = 9.5.sp, color = if (isAfkBowstringCraftingActive) Color(0xFFA5D6A7) else OsrsTextYellow)
                                    }
                                }
                            }
                        }

                        // 3. ARROWTIP FORGING BENCH
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF221810)),
                            border = BorderStroke(1.dp, Color(0xFF5A4532))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🗡️ Metal Arrowtip Anvil", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("1 Bar ➔ 15 Arrowtips (+40 XP)", color = OsrsGold, fontSize = 10.sp)
                                }

                                val availableBars = listOf(
                                    Pair("item_bronze_bar", "Bronze"),
                                    Pair("item_iron_bar", "Iron"),
                                    Pair("item_steel_bar", "Steel"),
                                    Pair("item_mithril_bar", "Opalite"),
                                    Pair("item_adamant_bar", "Amethyst"),
                                    Pair("item_rune_bar", "Aetherite"),
                                    Pair("item_dragon_bar", "Dragon")
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(availableBars) { (barId, label) ->
                                        val count = getTotalCount(barId)
                                        val isSel = selectedArrowtipBarId == barId
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSel) Color(0xFF3B2A1C) else Color(0xFF1A120C),
                                            border = BorderStroke(1.dp, if (isSel) OsrsGold else if (count > 0) Color(0xFF6B533E) else Color.DarkGray),
                                            modifier = Modifier.clickable { viewModel.setSelectedArrowtipBarId(barId) }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(label, color = if (isSel) OsrsTextYellow else OsrsParchment, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                                Text("x$count", color = if (count > 0) Color(0xFFA5D6A7) else Color.Gray, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.craftBarsToArrowtips(isAfk = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text("Forge Arrowtips (15x)", fontSize = 10.sp, color = OsrsTextYellow, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.toggleAfkArrowtipCrafting() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAfkArrowtipCraftingActive) Color(0xFF1B5E20) else OsrsLeatherMedium
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = if (isAfkArrowtipCraftingActive) "🛑 Stop AFK" else "⚡ AFK Tips",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAfkArrowtipCraftingActive) Color(0xFFA5D6A7) else OsrsTextYellow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 3: HUNTER TRAP CRAFTING BENCH ---
        if (selectedSectionIndex == 3) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trap_crafting_header_card"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
                    border = BorderStroke(1.dp, OsrsGold)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("🪤 Hunter Trap Crafting Bench", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Craft snares, box traps & nets for Hunter rumours.", color = OsrsParchment, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.toggleAfkTrapCrafting() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAfkTrapCraftingActive) Color(0xFF1B5E20) else OsrsRedFrame
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (isAfkTrapCraftingActive) "🛑 Stop AFK" else "⚡ AFK Craft",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAfkTrapCraftingActive) Color(0xFFA5D6A7) else OsrsTextYellow
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF4A3828))

                        val trapList = listOf(
                            Triple("item_bird_snare", "🪤 Bird Snare (Lvl 1 Whittling)", "1 Log ➔ 2 Snares"),
                            Triple("item_box_trap", "📦 Box Trap (Lvl 27 Whittling)", "2 Logs ➔ 1 Box Trap"),
                            Triple("item_net_trap", "🕸️ Net Trap Gear (Lvl 29 Whittling)", "1 Log + 1 Stick ➔ 1 Net Trap"),
                            Triple("item_noose_wand", "🪓 Noose Wand (Lvl 1 Whittling)", "1 Log ➔ 1 Noose Wand"),
                            Triple("item_impling_net", "🦋 Impling Net (Lvl 48 Whittling)", "2 Wooden Sticks ➔ 1 Net")
                        )

                        trapList.forEach { (trapId, title, reqText) ->
                            val isSelected = selectedCraftingTrapId == trapId
                            val owned = getTotalCount(trapId)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("trap_item_$trapId")
                                    .combinedClickable(
                                        onClick = { viewModel.setSelectedCraftingTrapId(trapId) },
                                        onLongClick = { viewModel.inspectItemObtain(trapId) }
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF2B2018) else Color(0xFF1E1610)
                                ),
                                border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color.DarkGray)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .combinedClickable(
                                                onClick = { viewModel.setSelectedCraftingTrapId(trapId) },
                                                onLongClick = { viewModel.inspectItemObtain(trapId) }
                                            ),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(title, color = if (isSelected) OsrsTextYellow else OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(reqText, color = OsrsParchment, fontSize = 10.sp)
                                        Text("Owned (Inv + Bank): $owned", color = OsrsGold, fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.setSelectedCraftingTrapId(trapId)
                                            viewModel.craftHunterTrap(trapId)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Craft Trap", fontSize = 10.sp, color = OsrsTextYellow, fontWeight = FontWeight.Bold)
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
