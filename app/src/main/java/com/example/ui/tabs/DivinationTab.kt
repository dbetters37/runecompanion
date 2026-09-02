package com.example.ui.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivinationTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val elementalEnergyMap by viewModel.elementalEnergyMap.collectAsStateWithLifecycle()

    val divXp = skillXpMap[OsrsSkill.DIVINATION] ?: 0L
    val divLvl = OsrsXpCalculator.getLevelForXp(divXp)
    val divProgress = OsrsXpCalculator.getXpProgressToNextLevel(divXp)

    val totalEnergySum = remember(elementalEnergyMap) {
        EnergyType.entries.sumOf { elementalEnergyMap[it.name] ?: 0L }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Transmute Materials, 1 = Effigy Workshop
    var selectedEffigyTierFilter by remember { mutableIntStateOf(0) } // 0 = All Strengths, 1..6 = T1..T6
    var selectedItemForTransmute by remember { mutableStateOf<InventoryItem?>(null) }
    var transmuteQty by remember { mutableIntStateOf(1) }

    val allTransmutables = remember(inventoryItems, bankItems) {
        val list = mutableListOf<InventoryItem>()
        inventoryItems.filter { it.quantity > 0 && !it.id.startsWith("item_effigy_") }.forEach { list.add(it) }
        bankItems.filter { it.quantity > 0 && !it.id.startsWith("item_effigy_") }.forEach { bankItem ->
            if (list.none { it.id == bankItem.id }) {
                list.add(bankItem)
            }
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- HEADER CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("divination_header_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142433)),
                border = BorderStroke(1.5.dp, Color(0xFF00B4D8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Row 1: Title, Icon & Outfit Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📱", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Divination Nexus",
                                    color = Color(0xFF90E0EF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Level $divLvl • Spirit Energy Conversion",
                                    color = OsrsParchment,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // XP Bar
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        LinearProgressIndicator(
                            progress = { divProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF00B4D8),
                            trackColor = Color(0xFF0A192F)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${"%,d".format(divXp)} XP", fontSize = 9.5.sp, color = OsrsParchment)
                            Text("Next Lvl: ${"%,d".format(OsrsXpCalculator.getXpRemainingForNextLevel(divXp))} XP", fontSize = 9.5.sp, color = OsrsParchment)
                        }
                    }
                }
            }
        }

        // --- ELEMENTAL ENERGIES DISPLAY CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTab = 2 }
                    .testTag("divination_energy_summary_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1326)),
                border = BorderStroke(1.dp, Color(0xFFAB47BC))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔮 Elemental Energies Storage",
                            color = Color(0xFFE1BEE7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "View Breakdown 📊 ➔",
                            color = Color(0xFFCE93D8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EnergyType.entries.forEach { type ->
                            val count = elementalEnergyMap[type.name] ?: 0L
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(type.colorHex).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(type.colorHex))
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text(type.emoji, fontSize = 14.sp)
                                    }
                                }
                                Text(
                                    text = "$count",
                                    color = if (count > 0) Color(type.colorHex) else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = type.displayName.take(5),
                                    color = OsrsParchment,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- MODE TABS SELECTOR ---
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2B1D13),
                border = BorderStroke(1.dp, Color(0xFF5A3E25)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf(
                        Triple(0, "♻️", "Transmute Items"),
                        Triple(1, "✨", "Craft Effigies"),
                        Triple(2, "📊", "Energy Breakdown")
                    ).forEach { (index, icon, label) ->
                        val isSel = selectedTab == index
                        Surface(
                            onClick = { selectedTab = index },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) Color(0xFF5A3E25) else Color.Transparent,
                            border = BorderStroke(
                                width = if (isSel) 1.5.dp else 0.dp,
                                color = if (isSel) Color(0xFFFFD700) else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("divination_tab_selector_$index")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(icon, fontSize = 11.sp)
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSel) Color(0xFFFFD700) else Color(0xFFA1887F),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- TAB 0: TRANSMUTE MATERIALS TO ELEMENTAL ENERGY ---
        if (selectedTab == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
                    border = BorderStroke(1.dp, OsrsGold)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "♻️ Feed Spare Materials for Elemental Energy",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Select any item in your inventory or bank below to break it down into Air 💨, Water 💧, Fire 🔥, Nature 🌱, Light 🌟 or Dark 🌑 energy!",
                            color = OsrsParchment,
                            fontSize = 10.sp
                        )

                        if (allTransmutables.isEmpty()) {
                            Text(
                                text = "No items in inventory or bank to transmute!",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            // Selected Item Control Box
                            selectedItemForTransmute?.let { selected ->
                                val (energyType, perItemAmount) = DivinationTransmutation.getEnergyForItem(
                                    selected.id, selected.name, selected.category.name, selected.costGp
                                )
                                val totalQty = (inventoryItems.find { it.id == selected.id }?.quantity ?: 0) +
                                        (bankItems.find { it.id == selected.id }?.quantity ?: 0)
                                val safeQty = transmuteQty.coerceAtMost(totalQty).coerceAtLeast(1)
                                val yieldTotal = perItemAmount * safeQty

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2212)),
                                    border = BorderStroke(1.dp, Color(0xFF70E000))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
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
                                                Text(selected.iconEmoji, fontSize = 20.sp)
                                                Column {
                                                    Text(selected.name, color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("Have: $totalQty • Base Value: ${selected.costGp} GP", color = OsrsParchment, fontSize = 9.sp)
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF101B0C),
                                            border = BorderStroke(1.dp, Color(energyType.colorHex))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Yield Preview:", color = OsrsParchment, fontSize = 10.sp)
                                                Text(
                                                    text = "+$yieldTotal ${energyType.emoji} ${energyType.displayName}",
                                                    color = Color(energyType.colorHex),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        // Qty buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf(1, 5, 10, totalQty).forEach { qtyChoice ->
                                                val qLabel = if (qtyChoice == totalQty) "All ($totalQty)" else "$qtyChoice"
                                                Button(
                                                    onClick = { transmuteQty = qtyChoice },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (safeQty == qtyChoice) Color(0xFF388E3C) else Color(0xFF2C3523)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                                                ) {
                                                    Text(qLabel, fontSize = 9.sp, color = OsrsTextWhite)
                                                }
                                            }
                                        }

                                        // Transmute Action Button
                                        Button(
                                            onClick = {
                                                viewModel.transmuteItemToEnergy(selected.id, safeQty)
                                                transmuteQty = 1
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("transmute_action_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                        ) {
                                            Text(
                                                text = "⚡ Transmute $safeQty x ${selected.name} ➔ +$yieldTotal ${energyType.displayName}",
                                                color = OsrsTextYellow,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Items Selection Grid
                            Text("Select Item to Transmute:", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                allTransmutables.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        rowItems.forEach { item ->
                                            val isSel = selectedItemForTransmute?.id == item.id
                                            val invCount = inventoryItems.find { it.id == item.id }?.quantity ?: 0
                                            val bankCount = bankItems.find { it.id == item.id }?.quantity ?: 0
                                            val totalCount = invCount + bankCount
                                            val (energyType, energyYield) = DivinationTransmutation.getEnergyForItem(
                                                item.id, item.name, item.category.name, item.costGp
                                            )

                                            Surface(
                                                onClick = {
                                                    selectedItemForTransmute = item
                                                    transmuteQty = 1
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("transmute_item_${item.id}"),
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSel) Color(0xFF3B2F1B) else Color(0xFF1E1712),
                                                border = BorderStroke(
                                                    width = if (isSel) 1.5.dp else 1.dp,
                                                    color = if (isSel) OsrsGold else Color(0xFF423325)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("${item.iconEmoji} ${item.name}", color = OsrsTextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("x$totalCount", color = OsrsGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(
                                                        text = "✨ +$energyYield ${energyType.emoji} ${energyType.displayName}",
                                                        color = Color(energyType.colorHex),
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- TAB 1: EFFIGY WORKSHOP ---
        if (selectedTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
                    border = BorderStroke(1.dp, OsrsGold)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✨ Craft Skill Effigies for Direct Skill XP",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "As your Divination level increases, unlock up to 6 tiers of stronger effigies with massive XP yields!",
                            color = OsrsParchment,
                            fontSize = 10.sp
                        )

                        // Level Strength Overview Banner
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1F1A2A),
                            border = BorderStroke(1.dp, Color(0xFF5E4575)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ Divination Lvl: $divLvl",
                                    color = OsrsTextYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                val unlockedTiersCount = DivinationTransmutation.EFFIGY_TIERS.count { divLvl >= it.reqDivLevel }
                                Text(
                                    text = "Unlocked Strengths: $unlockedTiersCount/6",
                                    color = Color(0xFF81D4FA),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Tier Selection / Filter Tabs
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Filter Strength Level:",
                                color = OsrsParchment,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (selectedEffigyTierFilter == 0) OsrsGold else Color(0xFF2C2235),
                                    border = BorderStroke(1.dp, if (selectedEffigyTierFilter == 0) OsrsGold else Color(0xFF4A3A5A)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedEffigyTierFilter = 0 }
                                ) {
                                    Text(
                                        text = "All Tiers",
                                        color = if (selectedEffigyTierFilter == 0) Color.Black else OsrsTextWhite,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }

                                DivinationTransmutation.EFFIGY_TIERS.forEach { tier ->
                                    val isUnlocked = divLvl >= tier.reqDivLevel
                                    val isSelected = selectedEffigyTierFilter == tier.level
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isSelected) Color(tier.colorHex) else Color(0xFF1E1727),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) Color.White else if (isUnlocked) Color(tier.colorHex).copy(alpha = 0.5f) else Color(0xFF3E2C42)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedEffigyTierFilter = tier.level }
                                    ) {
                                        Text(
                                            text = "T${tier.level}\nLvl ${tier.reqDivLevel}",
                                            color = if (isSelected) Color.Black else if (isUnlocked) OsrsTextWhite else Color.Gray,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Filter Recipes
                        val displayRecipes = remember(selectedEffigyTierFilter) {
                            if (selectedEffigyTierFilter == 0) {
                                DivinationTransmutation.EFFIGY_RECIPES
                            } else {
                                DivinationTransmutation.EFFIGY_RECIPES.filter { it.tierLevel == selectedEffigyTierFilter }
                            }
                        }

                        displayRecipes.forEach { recipe ->
                            val levelUnlocked = divLvl >= recipe.reqDivLevel
                            val canCraft = levelUnlocked && recipe.requiredEnergies.all { cost ->
                                (elementalEnergyMap[cost.type.name] ?: 0L) >= cost.amount
                            }
                            val effigyInInv = inventoryItems.find { it.id == recipe.effigyItemId }?.quantity ?: 0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("effigy_card_${recipe.skill.name.lowercase()}_t${recipe.tierLevel}"),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1522)),
                                border = BorderStroke(
                                    1.dp,
                                    if (!levelUnlocked) Color(0xFF4A3859) else if (canCraft) OsrsGold else Color(recipe.badgeColorHex)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                            Text(recipe.emoji, fontSize = 18.sp)
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(3.dp),
                                                        color = Color(recipe.badgeColorHex).copy(alpha = 0.2f),
                                                        border = BorderStroke(1.dp, Color(recipe.badgeColorHex))
                                                    ) {
                                                        Text(
                                                            text = "T${recipe.tierLevel} ${recipe.tierName}",
                                                            color = Color(recipe.badgeColorHex),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = recipe.effigyName,
                                                        color = OsrsTextYellow,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.5.sp
                                                    )
                                                }
                                                Text(
                                                    text = "Grants +${recipe.xpReward} ${recipe.skill.displayName} XP (+${recipe.craftDivXp} Div XP on Craft)",
                                                    color = Color(0xFF81D4FA),
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }

                                        if (effigyInInv > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF4A148C),
                                                border = BorderStroke(1.dp, Color(0xFFE1BEE7))
                                            ) {
                                                Text(
                                                    text = "Owned: $effigyInInv",
                                                    color = OsrsTextWhite,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Energy Recipe Ingredients Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Recipe:", color = OsrsParchment, fontSize = 9.sp)
                                        recipe.requiredEnergies.forEach { cost ->
                                            val owned = elementalEnergyMap[cost.type.name] ?: 0L
                                            val meets = owned >= cost.amount
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(cost.type.colorHex).copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, if (meets) Color(cost.type.colorHex) else Color.Red)
                                            ) {
                                                Text(
                                                    text = "${cost.type.emoji} $owned/${cost.amount}",
                                                    color = if (meets) Color(cost.type.colorHex) else Color(0xFFFF8A80),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Action Buttons Row (Craft & Consume)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.craftSkillEffigy(recipe) },
                                            enabled = canCraft,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("craft_effigy_${recipe.skill.name.lowercase()}_t${recipe.tierLevel}"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF6A1B9A),
                                                disabledContainerColor = Color(0xFF2C1E36)
                                            ),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when {
                                                    !levelUnlocked -> "🔒 Div Lvl ${recipe.reqDivLevel} Required"
                                                    canCraft -> "✨ Craft (+${recipe.craftDivXp} Div XP)"
                                                    else -> "🔒 Need Energies"
                                                },
                                                color = if (canCraft) OsrsTextYellow else Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp
                                            )
                                        }

                                        if (effigyInInv > 0) {
                                            Button(
                                                onClick = { viewModel.consumeSkillEffigy(recipe) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("consume_effigy_${recipe.skill.name.lowercase()}_t${recipe.tierLevel}"),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "🔥 Consume (+${recipe.xpReward} XP)",
                                                    color = OsrsTextYellow,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.5.sp
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

        // --- TAB 2: DEDICATED ELEMENTAL ENERGIES BREAKDOWN ---
        if (selectedTab == 2) {
            // Summary Breakdown Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("energy_breakdown_summary_card"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1828)),
                    border = BorderStroke(1.dp, Color(0xFFAB47BC))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📊 Elemental Energy Reserve Breakdown",
                                    color = Color(0xFFE1BEE7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Total Energy Stored: $totalEnergySum Units",
                                    color = OsrsParchment,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = { selectedTab = 0 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("♻️ Transmute More", fontSize = 10.sp, color = OsrsTextYellow)
                            }
                        }

                        // Proportion Bar
                        if (totalEnergySum > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF0F0A14))
                            ) {
                                EnergyType.entries.forEach { type ->
                                    val count = elementalEnergyMap[type.name] ?: 0L
                                    if (count > 0) {
                                        val weight = count.toFloat() / totalEnergySum.toFloat()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(weight)
                                                .background(Color(type.colorHex))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Detailed Cards per Energy Type
            EnergyType.entries.forEach { type ->
                item {
                    val count = elementalEnergyMap[type.name] ?: 0L
                    val percentage = if (totalEnergySum > 0) (count * 100f / totalEnergySum) else 0f

                    val (sourcesList, usesList) = when (type) {
                        EnergyType.AIR -> Pair(
                            "Ranged ammo (arrows, darts, bows), feathers, agility gear, wind runes",
                            DivinationTransmutation.EFFIGY_RECIPES.filter { r -> r.requiredEnergies.any { it.type == EnergyType.AIR } }
                        )
                        EnergyType.WATER -> Pair(
                            "Fish (raw & cooked), potions, water buckets, sailing timber",
                            DivinationTransmutation.EFFIGY_RECIPES.filter { r -> r.requiredEnergies.any { it.type == EnergyType.WATER } }
                        )
                        EnergyType.FIRE -> Pair(
                            "Ores, metal bars, coal, cooked food, ashes, tinder, forge scraps",
                            DivinationTransmutation.EFFIGY_RECIPES.filter { r -> r.requiredEnergies.any { it.type == EnergyType.FIRE } }
                        )
                        EnergyType.NATURE -> Pair(
                            "Logs, planks, seeds, herbs, farming crops, woodcraft items",
                            DivinationTransmutation.EFFIGY_RECIPES.filter { r -> r.requiredEnergies.any { it.type == EnergyType.NATURE } }
                        )
                        EnergyType.LIGHT -> Pair(
                            "Gems (diamonds, rubies, sapphires), jewelry, gold & silver trinkets, blessings",
                            DivinationTransmutation.EFFIGY_RECIPES.filter { r -> r.requiredEnergies.any { it.type == EnergyType.LIGHT } }
                        )
                        EnergyType.DARK -> Pair(
                            "Bones, slayer monster trophies, dark weapons, armor, corrupt relics",
                            DivinationTransmutation.EFFIGY_RECIPES.filter { r -> r.requiredEnergies.any { it.type == EnergyType.DARK } }
                        )
                    }

                    // Matching inventory and bank items available to transmute into this energy
                    val matchingItems = remember(inventoryItems, bankItems) {
                        val all = mutableListOf<InventoryItem>()
                        (inventoryItems + bankItems).forEach { item ->
                            if (item.quantity > 0 && !item.id.startsWith("item_effigy_")) {
                                val (eType, _) = DivinationTransmutation.getEnergyForItem(item.id, item.name, item.category.name, item.costGp)
                                if (eType == type && all.none { it.id == item.id }) {
                                    all.add(item)
                                }
                            }
                        }
                        all
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("energy_breakdown_card_${type.name.lowercase()}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF191322)),
                        border = BorderStroke(1.5.dp, Color(type.colorHex))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Header Row
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
                                        color = Color(type.colorHex).copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, Color(type.colorHex))
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Text(type.emoji, fontSize = 18.sp)
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = type.displayName,
                                            color = Color(type.colorHex),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Storage: $count units (${"%.1f".format(percentage)}% of total)",
                                            color = OsrsParchment,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(type.colorHex).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(type.colorHex))
                                ) {
                                    Text(
                                        text = "$count",
                                        color = Color(type.colorHex),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFF332640), thickness = 0.5.dp)

                            // Salvaged Sources
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("♻️ Primary Salvage Sources:", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text(sourcesList, color = OsrsParchment, fontSize = 9.5.sp)
                            }

                            // Items currently in bag/bank yielding this energy
                            if (matchingItems.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("📦 Items Available to Divine (${matchingItems.size}):", color = Color(0xFF81D4FA), fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        matchingItems.take(4).forEach { item ->
                                            val invCount = inventoryItems.find { it.id == item.id }?.quantity ?: 0
                                            val bankCount = bankItems.find { it.id == item.id }?.quantity ?: 0
                                            val total = invCount + bankCount
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF221A2D),
                                                border = BorderStroke(1.dp, Color(type.colorHex).copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = "${item.iconEmoji} ${item.name} ($total)",
                                                    color = OsrsTextWhite,
                                                    fontSize = 8.5.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (matchingItems.size > 4) {
                                            Text("+${matchingItems.size - 4} more", color = Color.Gray, fontSize = 8.5.sp, modifier = Modifier.align(Alignment.CenterVertically))
                                        }
                                    }
                                }
                            }

                            // Effigies using this energy
                            if (usesList.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("✨ Used in Crafting Effigies:", color = Color(0xFFCE93D8), fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        usesList.take(3).forEach { recipe ->
                                            val reqCost = recipe.requiredEnergies.firstOrNull { it.type == type }?.amount ?: 0
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF2A1C3A),
                                                border = BorderStroke(1.dp, Color(0xFF7B1FA2))
                                            ) {
                                                Text(
                                                    text = "${recipe.emoji} ${recipe.effigyName} (${reqCost}x)",
                                                    color = OsrsTextYellow,
                                                    fontSize = 8.5.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (usesList.size > 3) {
                                            Text("+${usesList.size - 3} more", color = Color.Gray, fontSize = 8.5.sp, modifier = Modifier.align(Alignment.CenterVertically))
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
}
