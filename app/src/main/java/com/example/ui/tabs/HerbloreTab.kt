package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun HerbloreTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val isAfkHerbCrushingActive by viewModel.isAfkHerbCrushingActive.collectAsStateWithLifecycle()
    val isAfkPotionBrewingActive by viewModel.isAfkPotionBrewingActive.collectAsStateWithLifecycle()

    val herbloreXp = skillXpMap[OsrsSkill.HERBLORE] ?: 0L
    val herbloreLvl = OsrsXpCalculator.getLevelForXp(herbloreXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel((herbloreLvl + 1).coerceAtMost(99))
    val currentLevelBaseXp = OsrsXpCalculator.getXpForLevel(herbloreLvl)
    val progress = if (herbloreLvl >= 99) 1f else ((herbloreXp - currentLevelBaseXp).toFloat() / (nextLevelXp - currentLevelBaseXp).coerceAtLeast(1L)).coerceIn(0f, 1f)

    var selectedSectionIndex by remember { mutableStateOf(0) } // 0 = Herb Grinding, 1 = Potion Brewing, 2 = Botanical Matrix

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
        // --- HERBLORE HEADER CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("herblore_header_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B281B)),
                border = BorderStroke(1.5.dp, Color(0xFF81C784))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                color = Color(0xFF112011),
                                border = BorderStroke(1.dp, Color(0xFF81C784)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🥣", fontSize = 18.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = "Herblore & Alchemy",
                                    color = Color(0xFFA5D6A7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Level $herbloreLvl Herbalism",
                                    color = OsrsParchment,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF112011),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF81C784))
                        ) {
                            Text(
                                text = "${String.format("%,d", herbloreXp)} XP",
                                color = Color(0xFFA5D6A7),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Level Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Level $herbloreLvl Progress", color = OsrsParchment, fontSize = 10.sp)
                            Text("${(progress * 100).toInt()}%", color = Color(0xFFA5D6A7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF81C784),
                            trackColor = Color(0xFF0D1B0D)
                        )
                    }

                    // Tip banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF122012), RoundedCornerShape(4.dp))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("💡", fontSize = 12.sp)
                        Text(
                            text = "Herbs harvest normal (no grimy herbs). Grind fresh herbs into crushed form for brewing! Hold any item to see obtainment methods.",
                            color = OsrsParchment,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
        }

        // --- HERB & POTION RESERVES CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
                border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌿 Herb & Potion Storage (Inventory + Bank)",
                            color = Color(0xFFA5D6A7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hold item for obtain info",
                            color = OsrsGold,
                            fontSize = 9.5.sp
                        )
                    }

                    var storageFilter by remember { mutableStateOf(0) } // 0 = All, 1 = Normal Herbs, 2 = Crushed, 3 = Potions
                    var showOwnedOnly by remember { mutableStateOf(false) }

                    val allStorageItems = remember {
                        val herbs = HerbloreData.CRUSH_HERB_RECIPES.map { recipe ->
                            Triple(recipe.herbId, recipe.herbName, recipe.iconEmoji)
                        }
                        val crushed = HerbloreData.CRUSH_HERB_RECIPES.map { recipe ->
                            Triple(recipe.crushedHerbId, recipe.crushedHerbName, "🥣")
                        }
                        val potions = HerbloreData.POTION_RECIPES.map { recipe ->
                            Triple(recipe.outputPotionId, recipe.outputPotionName, recipe.iconEmoji)
                        }
                        mapOf(
                            1 to herbs,
                            2 to crushed,
                            3 to potions
                        )
                    }

                    val filteredStorageItems = remember(storageFilter, showOwnedOnly, inventoryItems, bankItems) {
                        val baseList = when (storageFilter) {
                            1 -> allStorageItems[1] ?: emptyList()
                            2 -> allStorageItems[2] ?: emptyList()
                            3 -> allStorageItems[3] ?: emptyList()
                            else -> (allStorageItems[1] ?: emptyList()) + (allStorageItems[2] ?: emptyList()) + (allStorageItems[3] ?: emptyList())
                        }
                        if (showOwnedOnly) {
                            baseList.filter { (itemId, _, _) -> getTotalCount(itemId) > 0 }
                        } else {
                            baseList
                        }
                    }

                    // Filter selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("All", "Herbs", "Crushed", "Potions").forEachIndexed { index, label ->
                                FilterChip(
                                    selected = storageFilter == index,
                                    onClick = { storageFilter = index },
                                    label = { Text(label, fontSize = 10.sp) },
                                    modifier = Modifier.height(26.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2E5B2E),
                                        selectedLabelColor = Color(0xFFA5D6A7),
                                        containerColor = Color(0xFF1B281B),
                                        labelColor = OsrsParchment
                                    )
                                )
                            }
                        }
                        FilterChip(
                            selected = showOwnedOnly,
                            onClick = { showOwnedOnly = !showOwnedOnly },
                            label = { Text(if (showOwnedOnly) "Owned" else "All", fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF81C784),
                                selectedLabelColor = Color(0xFF112011),
                                containerColor = Color(0xFF1B281B),
                                labelColor = OsrsParchment
                            )
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (filteredStorageItems.isEmpty()) {
                            Text(
                                text = "No items matching filter in Inventory or Bank.",
                                color = OsrsParchment,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            filteredStorageItems.forEach { (itemId, label, emoji) ->
                                val invCount = inventoryItems.find { it.id == itemId }?.quantity ?: 0
                                val totalCount = getTotalCount(itemId)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1B281B),
                                    border = BorderStroke(1.dp, if (totalCount > 0) Color(0xFF81C784) else Color(0xFF2E482E)),
                                    modifier = Modifier.combinedClickable(
                                        onClick = { /* quick tap */ },
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
                                            color = if (invCount > 0) Color(0xFFA5D6A7) else Color.Gray,
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
            }
        }

        // --- SECTION SELECTOR BUTTONS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedSectionIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 0) Color(0xFF2E5B2E) else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .border(1.dp, if (selectedSectionIndex == 0) Color(0xFF81C784) else Color(0xFF4A3828), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = "🥣 Grind Herbs",
                        color = if (selectedSectionIndex == 0) Color(0xFFA5D6A7) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedSectionIndex = 1
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 1) Color(0xFF2E5B2E) else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .border(1.dp, if (selectedSectionIndex == 1) Color(0xFF81C784) else Color(0xFF4A3828), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = "🧪 Brew Potions",
                        color = if (selectedSectionIndex == 1) Color(0xFFA5D6A7) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedSectionIndex = 2
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSectionIndex == 2) Color(0xFF2E5B2E) else OsrsLeatherMedium
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(36.dp)
                        .border(1.dp, if (selectedSectionIndex == 2) Color(0xFF81C784) else Color(0xFF4A3828), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = "🌱 Botanical Matrix",
                        color = if (selectedSectionIndex == 2) Color(0xFFA5D6A7) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (selectedSectionIndex == 0) {
            // --- HERB CRUSHING / GRINDING (PESTLE & MORTAR) ---
            items(HerbloreData.CRUSH_HERB_RECIPES) { recipe ->
                val meetsLevel = herbloreLvl >= recipe.reqLevel
                val herbInv = inventoryItems.find { it.id == recipe.herbId }?.quantity ?: 0
                val herbBank = bankItems.find { it.id == recipe.herbId }?.quantity ?: 0
                val totalHerb = herbInv + herbBank
                val crushedCount = getTotalCount(recipe.crushedHerbId)
                val hasHerbInInv = herbInv > 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("crush_herb_card_${recipe.id}")
                        .combinedClickable(
                            onClick = { /* expand or focus */ },
                            onLongClick = { viewModel.inspectItemObtain(recipe.herbId) }
                        ),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B281B)),
                    border = BorderStroke(1.dp, if (meetsLevel) Color(0xFF385E38) else Color(0xFF223822))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                Text(recipe.iconEmoji, fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = "${recipe.herbName} ➔ ${recipe.crushedHerbName}",
                                        color = if (meetsLevel) Color(0xFFA5D6A7) else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Grind with Pestle & Mortar for +${recipe.xpReward} Herblore XP.",
                                        color = OsrsParchment,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (meetsLevel) Color(0xFF1B5E20) else Color(0xFF3E2D1D))
                                    .border(1.dp, if (meetsLevel) Color(0xFF81C784) else OsrsGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Lvl ${recipe.reqLevel}", color = if (meetsLevel) Color(0xFFA5D6A7) else OsrsParchment, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${recipe.herbName} in Inv: $herbInv (Total: $totalHerb)",
                                color = if (hasHerbInInv) Color(0xFFA5D6A7) else Color(0xFFFCA5A5),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Crushed Owned: $crushedCount", color = Color(0xFFA5D6A7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.crushHerbRecipe(recipe, isAfk = false)
                                },
                                enabled = meetsLevel && hasHerbInInv,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E5B2E),
                                    disabledContainerColor = Color(0xFF182818)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("crush_button_${recipe.id}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (!meetsLevel) "Requires Lvl ${recipe.reqLevel}" else if (!hasHerbInInv) "No ${recipe.herbName} in Inv" else "GRIND 1 HERB (+${recipe.xpReward} XP)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = if (meetsLevel && hasHerbInInv) Color(0xFFA5D6A7) else Color.Gray
                                )
                            }

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleAfkHerbCrushing(recipe)
                                },
                                enabled = meetsLevel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAfkHerbCrushingActive) Color(0xFF1B5E20) else OsrsLeatherMedium
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("afk_crush_button_${recipe.id}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAfkHerbCrushingActive) "🛑 STOP AFK" else "⚡ AFK GRIND",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = if (isAfkHerbCrushingActive) Color(0xFFA5D6A7) else OsrsTextYellow
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedSectionIndex == 1) {
            // --- POTION BREWING ---
            items(HerbloreData.POTION_RECIPES) { recipe ->
                val meetsLevel = herbloreLvl >= recipe.reqLevel

                val crushedHerbInv = inventoryItems.find { it.id == recipe.crushedHerbId }?.quantity ?: 0
                val cleanHerbInv = inventoryItems.find { it.id == recipe.cleanHerbId }?.quantity ?: 0
                val herbInv = if (crushedHerbInv > 0) crushedHerbInv else cleanHerbInv
                val totalHerb = getTotalCount(recipe.crushedHerbId) + getTotalCount(recipe.cleanHerbId)

                val secInv = inventoryItems.find { it.id == recipe.secondaryItemId }?.quantity ?: 0
                val secBank = bankItems.find { it.id == recipe.secondaryItemId }?.quantity ?: 0
                val totalSec = secInv + secBank

                val hasItemsInInv = (crushedHerbInv > 0 || cleanHerbInv > 0) && secInv > 0
                val createdCount = getTotalCount(recipe.outputPotionId)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("potion_card_${recipe.id}")
                        .combinedClickable(
                            onClick = { /* details */ },
                            onLongClick = { viewModel.inspectItemObtain(recipe.outputPotionId) }
                        ),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B281B)),
                    border = BorderStroke(1.dp, if (meetsLevel) Color(0xFF385E38) else Color(0xFF223822))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                Text(recipe.iconEmoji, fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = recipe.name,
                                        color = if (meetsLevel) Color(0xFFA5D6A7) else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(recipe.effectDescription, color = OsrsParchment, fontSize = 10.sp)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (meetsLevel) Color(0xFF1B5E20) else Color(0xFF3E2D1D))
                                        .border(1.dp, if (meetsLevel) Color(0xFF81C784) else OsrsGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Lvl ${recipe.reqLevel}", color = if (meetsLevel) Color(0xFFA5D6A7) else OsrsParchment, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Owned: $createdCount", color = Color(0xFFA5D6A7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Required Materials Section
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Required Ingredients (Hold to Inspect Obtainment):", color = Color(0xFFA5D6A7), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Crushed Herb pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (herbInv > 0) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color(0xFF381C10))
                                        .border(1.dp, if (herbInv > 0) Color(0xFF81C784) else Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .combinedClickable(
                                            onClick = { /* inspect */ },
                                            onLongClick = { viewModel.inspectItemObtain(recipe.crushedHerbId) }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text("🥣", fontSize = 11.sp)
                                        Text("${recipe.crushedHerbName}: $herbInv/1", color = if (herbInv > 0) Color(0xFFA5D6A7) else Color(0xFFFCA5A5), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                                        if (herbInv < 1 && totalHerb >= 1) {
                                            Text("($totalHerb in Bank)", color = OsrsGold, fontSize = 8.5.sp)
                                        }
                                    }
                                }

                                // Secondary pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (secInv > 0) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color(0xFF381C10))
                                        .border(1.dp, if (secInv > 0) Color(0xFF81C784) else Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .combinedClickable(
                                            onClick = { /* inspect */ },
                                            onLongClick = { viewModel.inspectItemObtain(recipe.secondaryItemId) }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(recipe.secondaryItemEmoji, fontSize = 11.sp)
                                        Text("${recipe.secondaryItemName}: $secInv/1", color = if (secInv > 0) Color(0xFFA5D6A7) else Color(0xFFFCA5A5), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                                        if (secInv < 1 && totalSec >= 1) {
                                            Text("($totalSec in Bank)", color = OsrsGold, fontSize = 8.5.sp)
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
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.brewPotionRecipe(recipe, isAfk = false)
                                },
                                enabled = meetsLevel && hasItemsInInv,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E5B2E),
                                    disabledContainerColor = Color(0xFF182818)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("brew_button_${recipe.id}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (!meetsLevel) "Requires Lvl ${recipe.reqLevel}" else if (!hasItemsInInv) "Missing Items in Inv" else "BREW POTION (+${recipe.xpReward} XP)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = if (meetsLevel && hasItemsInInv) Color(0xFFA5D6A7) else Color.Gray
                                )
                            }

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleAfkPotionBrewing(recipe)
                                },
                                enabled = meetsLevel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAfkPotionBrewingActive) Color(0xFF1B5E20) else OsrsLeatherMedium
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("afk_brew_button_${recipe.id}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAfkPotionBrewingActive) "🛑 STOP AFK" else "⚡ AFK BREW",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = if (isAfkPotionBrewingActive) Color(0xFFA5D6A7) else OsrsTextYellow
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- BOTANICAL MATRIX (SEED -> CROP -> CRUSHED -> POTIONS) ---
            items(HerbloreData.BOTANICAL_CHAINS) { chain ->
                val seedCount = getTotalCount(chain.seedId)
                val herbCount = getTotalCount(chain.herbId)
                val crushedCount = getTotalCount(chain.crushedHerbId)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("botanical_chain_${chain.seedId}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B281B)),
                    border = BorderStroke(1.dp, Color(0xFF385E38))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌱 ${chain.herbName} Botanical Lifecycle",
                                color = Color(0xFFA5D6A7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Farm Lv. ${chain.reqFarmingLevel} • Herb Lv. ${chain.reqHerbloreLevel}",
                                color = OsrsParchment,
                                fontSize = 10.sp
                            )
                        }

                        // Chain visual progression
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Seed
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF142414),
                                border = BorderStroke(1.dp, if (seedCount > 0) Color(0xFF81C784) else Color(0xFF2E482E)),
                                modifier = Modifier
                                    .weight(1f)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = { viewModel.inspectItemObtain(chain.seedId) }
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(chain.seedEmoji, fontSize = 14.sp)
                                    Text(chain.seedName, color = OsrsParchment, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("Owned: $seedCount", color = if (seedCount > 0) Color(0xFFA5D6A7) else Color.Gray, fontSize = 8.5.sp)
                                }
                            }

                            Text("➔", color = OsrsGold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))

                            // Fresh Herb
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF142414),
                                border = BorderStroke(1.dp, if (herbCount > 0) Color(0xFF81C784) else Color(0xFF2E482E)),
                                modifier = Modifier
                                    .weight(1f)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = { viewModel.inspectItemObtain(chain.herbId) }
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(chain.herbEmoji, fontSize = 14.sp)
                                    Text(chain.herbName, color = OsrsParchment, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("Owned: $herbCount", color = if (herbCount > 0) Color(0xFFA5D6A7) else Color.Gray, fontSize = 8.5.sp)
                                }
                            }

                            Text("➔", color = OsrsGold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))

                            // Crushed Herb
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF142414),
                                border = BorderStroke(1.dp, if (crushedCount > 0) Color(0xFF81C784) else Color(0xFF2E482E)),
                                modifier = Modifier
                                    .weight(1f)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = { viewModel.inspectItemObtain(chain.crushedHerbId) }
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🥣", fontSize = 14.sp)
                                    Text(chain.crushedHerbName, color = OsrsParchment, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("Owned: $crushedCount", color = if (crushedCount > 0) Color(0xFFA5D6A7) else Color.Gray, fontSize = 8.5.sp)
                                }
                            }
                        }

                        // Resulting Potions from this herb
                        if (chain.potionRecipes.isNotEmpty()) {
                            Text("Brews into:", color = Color(0xFFA5D6A7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            chain.potionRecipes.forEach { pot ->
                                val potCount = getTotalCount(pot.outputPotionId)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF142414),
                                    border = BorderStroke(1.dp, Color(0xFF2E482E)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {},
                                            onLongClick = { viewModel.inspectItemObtain(pot.outputPotionId) }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(pot.iconEmoji, fontSize = 13.sp)
                                            Text(
                                                text = "${pot.name} (Req. Herblore ${pot.reqLevel})",
                                                color = Color(0xFFA5D6A7),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Text("Owned: $potCount", color = OsrsGold, fontSize = 9.5.sp)
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
