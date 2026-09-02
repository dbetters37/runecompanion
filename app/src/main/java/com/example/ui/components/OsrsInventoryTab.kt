package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CauldronRecipe
import com.example.data.models.CauldronRecipes
import com.example.data.models.OsrsSkill
import com.example.data.models.DefaultItems
import com.example.data.models.InventoryItem
import com.example.data.models.ItemCategory
import com.example.ui.components.weatheredStoneBorder
import com.example.ui.theme.*

@Composable
fun OsrsInventoryTab(
    inventoryItems: List<InventoryItem>,
    bankItems: List<InventoryItem> = emptyList(),
    coinsGp: Long,
    onFeedPet: (InventoryItem) -> Unit,
    onPlayPet: (InventoryItem) -> Unit,
    onSellItem: (InventoryItem, Int) -> Unit = { _, _ -> },
    onBuyItem: (InventoryItem) -> Unit,
    onBuyCustomGeOffer: ((String, Long, Int) -> Unit)? = null,
    onEquipItem: ((InventoryItem) -> Unit)? = null,
    onDepositToBank: (InventoryItem, Int) -> Unit = { _, _ -> },
    onWithdrawFromBank: (InventoryItem, Int) -> Unit = { _, _ -> },
    onDepositAllToBank: () -> Unit = {},
    onOpenSeedPouch: ((InventoryItem) -> Unit)? = null,
    onOpenContractReward: ((com.example.data.models.OsrsSkill, Int) -> Unit)? = null,
    foodBagEatHighestFirst: Boolean = true,
    onToggleFoodBagEatOrder: () -> Unit = {},
    onFeedFromFoodBag: (InventoryItem?) -> Unit = {},
    cauldronFoodName: String? = "Raw Food",
    cauldronFoodEmoji: String = "🐟",
    cauldronUncookedCount: Int = 0,
    cauldronCookedCount: Int = 0,
    cauldronProgress: Float = 0f,
    isCauldronAfkActive: Boolean = false,
    activeCauldronRecipe: CauldronRecipe = CauldronRecipes.ALL_RECIPES.first(),
    onSelectCauldronRecipe: (CauldronRecipe) -> Unit = {},
    onCookRecipeFromBankAndInventory: (CauldronRecipe) -> Unit = {},
    adventuringLevel: Int = 1,
    cookingLevel: Int = 1,
    cookingXp: Long = 0L,
    cookingQueue: List<String> = emptyList(),
    isAfkCookingActive: Boolean = false,
    onToggleAfkCooking: () -> Unit = {},
    onAddToCookingQueue: (String) -> Unit = {},
    onRemoveFromCookingQueue: (String) -> Unit = {},
    onMoveCookingQueueItem: (String, Int) -> Unit = { _, _ -> },
    onClearCookingQueue: () -> Unit = {},
    onAutoPopulateCookingQueue: () -> Unit = {},
    onCookFood: (String, Int) -> Unit = { _, _ -> },
    activeCookingBuffs: List<com.example.data.models.ActiveCookingBuff> = emptyList(),
    maxBuffSlots: Int = 1,
    favoriteItemIds: Set<String> = emptySet(),
    onToggleFavoriteItem: ((String) -> Unit)? = null,
    onAddFoodToCauldron: (InventoryItem, Int) -> Unit = { _, _ -> },
    onClaimCauldronCookedFood: () -> Unit = {},
    onToggleCauldronAfk: () -> Unit = {},
    onOfferPouchItem: (InventoryItem, Int) -> Unit = { _, _ -> },
    onBatchOfferPouchCategory: (MysticalCategory) -> Unit = {},
    onBatchOfferPouchAll: () -> Unit = {},
    onTransmutePouchItem: (InventoryItem, Int) -> Unit = { _, _ -> },
    viewModel: com.example.viewmodel.PetViewModel? = null,
    modifier: Modifier = Modifier
) {
    // 0: Storage Vault, 1: Cauldron, 2: Recipe Book
    val vmSubTab = viewModel?.storageSelectedSubTab?.collectAsStateWithLifecycle()?.value ?: 0
    var selectedTabIndex by remember(vmSubTab) { mutableIntStateOf(vmSubTab) }
    var selectedStorageItemForDetail by remember { mutableStateOf<InventoryItem?>(null) }
    var storageSearchQuery by remember { mutableStateOf("") }
    var storageCategoryFilterKey by remember { mutableStateOf<String?>(null) }

    // Use combined bank/storage items as the single source of truth
    val allStorageItems = remember(bankItems, inventoryItems) {
        if (bankItems.isNotEmpty()) bankItems else inventoryItems
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(OsrsLeatherMedium)
            .weatheredStoneBorder(cornerRadius = 10.dp)
            .padding(12.dp)
    ) {
        // Tab Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
                    Text("📦", fontSize = 18.sp)
                    Text(
                        text = "Storage & Supplies",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = OsrsLeatherDark,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, OsrsGold)
                ) {
                    Text(
                        text = "🪙 ${"%,d".format(coinsGp)} GP",
                        color = OsrsGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TabButton(
                        title = "📦 Storage",
                        isSelected = selectedTabIndex == 0,
                        onClick = {
                            selectedTabIndex = 0
                            viewModel?.setStorageSubTab(0)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TabButton(
                        title = "🍲 Cauldron",
                        isSelected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                            viewModel?.setStorageSubTab(1)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TabButton(
                        title = "📖 Recipes",
                        isSelected = selectedTabIndex == 2,
                        onClick = {
                            selectedTabIndex = 2
                            viewModel?.setStorageSubTab(2)
                        }
                    )
                }
            }
        }

        if (selectedTabIndex == 0) {
            // --- 📦 UNIFIED STORAGE VAULT TAB ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Storage Stats Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OsrsLeatherDark, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦 Storage: ${allStorageItems.sumOf { it.quantity }} items (${allStorageItems.size} slots)",
                        color = OsrsTextYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "💡 Tap item to use / hold to ⭐ Favorite",
                        color = OsrsParchment,
                        fontSize = 10.sp
                    )
                }

                // Slideable Storage Category Filter Tabs
                val categories: List<Pair<String?, String>> = listOf(
                    null to "🌐 All",
                    "REWARDS" to "🎁 Reward Boxes",
                    "BONES" to "🦴 Bones",
                    "CONSTRUCTION" to "🪚 Hut-Keeping",
                    "SEEDS" to "🌱 Seeds",
                    "EQUIPMENT" to "⚔️ Gear",
                    "FOOD" to "🥩 Food",
                    "POTION" to "🧪 Potions",
                    "SKILL_TOOL" to "🪤 Traps & Tools",
                    "RUNES" to "🪄 Magic & Runes",
                    "LOGS" to "🪵 Logs & Wood",
                    "BARS" to "🧱 Bars & Ores",
                    "HERBS" to "🌿 Herbs & Farm",
                    "TOY" to "🧸 Toys",
                    "MISC" to "📦 Misc"
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { (catKey, label) ->
                        val isSelected = (storageCategoryFilterKey == catKey)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) OsrsRedFrame else OsrsLeatherDark,
                            border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color.Gray),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { storageCategoryFilterKey = catKey }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) OsrsTextYellow else OsrsParchment,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Storage Search Input
                OutlinedTextField(
                    value = storageSearchQuery,
                    onValueChange = { storageSearchQuery = it },
                    label = { Text("🔍 Search Storage Vault...", color = OsrsParchment, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OsrsGold,
                        unfocusedBorderColor = OsrsParchment,
                        focusedTextColor = OsrsTextYellow,
                        unfocusedTextColor = OsrsTextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                val filteredStorageItems = remember(allStorageItems, storageSearchQuery, storageCategoryFilterKey) {
                    allStorageItems.filter { item ->
                        val matchesCat = when (storageCategoryFilterKey) {
                            null -> true
                            "REWARDS" -> item.id.startsWith("item_contract_reward_") ||
                                    item.id.contains("reward") ||
                                    item.id.contains("casket") ||
                                    item.id.contains("box") ||
                                    item.id.contains("pouch") ||
                                    item.id.contains("crate") ||
                                    item.id.contains("chest") ||
                                    item.id.contains("loot") ||
                                    item.name.contains("Reward", ignoreCase = true) ||
                                    item.name.contains("Casket", ignoreCase = true) ||
                                    item.name.contains("Box", ignoreCase = true) ||
                                    item.name.contains("Pouch", ignoreCase = true) ||
                                    item.name.contains("Crate", ignoreCase = true) ||
                                    item.name.contains("Chest", ignoreCase = true)
                            "BONES" -> item.category == ItemCategory.BONES || item.id.contains("bone") || item.name.contains("bone", ignoreCase = true)
                            "CONSTRUCTION" -> item.category == ItemCategory.CONSTRUCTION || item.id.contains("plank") || item.id.contains("nail") || item.id.contains("cloth") || item.id.contains("leaf") || item.id.contains("marble") || item.name.contains("plank", ignoreCase = true) || item.name.contains("brick", ignoreCase = true)
                            "SEEDS" -> item.category == ItemCategory.SEEDS || item.id.contains("seed") || item.name.contains("seed", ignoreCase = true) || item.id.contains("spore")
                            "EQUIPMENT" -> item.category == ItemCategory.EQUIPMENT || item.equipmentSlot != null
                            "FOOD" -> item.category == ItemCategory.FOOD || item.restoreHunger > 0 || item.healHp > 0 || item.isRawUncookedFood || item.isCookedReadyToEatFood || item.id.contains("raw_") || item.id.contains("cooked_") || item.id.contains("fish") || item.id.contains("salad") || item.id.contains("stew") || item.id.contains("roast") || item.id.contains("meat")
                            "POTION" -> item.category == ItemCategory.POTION || item.name.contains("potion", ignoreCase = true) || item.name.contains("elixir", ignoreCase = true) || item.name.contains("tonic", ignoreCase = true) || item.name.contains("brew", ignoreCase = true)
                            "SKILL_TOOL" -> (item.category == ItemCategory.SKILL_TOOL && !item.id.contains("plank") && !item.name.contains("plank", ignoreCase = true) && !item.id.contains("bone") && !item.name.contains("bone", ignoreCase = true)) || ((item.id.contains("trap") || item.id.contains("snare") || item.id.contains("net") || item.id.contains("wand") || item.id.contains("rod") || item.id.contains("axe") || item.id.contains("pickaxe")) && !item.id.contains("bone") && !item.name.contains("bone", ignoreCase = true) && item.category != ItemCategory.EQUIPMENT)
                            "RUNES" -> item.category == ItemCategory.RUNES_MAGIC || item.id.contains("rune") || item.id.contains("essence") || item.id.contains("staff") || item.name.contains("rune", ignoreCase = true)
                            "LOGS" -> item.category == ItemCategory.LOGS_WOOD || item.id.contains("log") || item.id.contains("wood") || item.name.contains("log", ignoreCase = true) || item.name.contains("wood", ignoreCase = true)
                            "BARS" -> item.category == ItemCategory.BARS_ORES || item.id.contains("ore") || item.id.contains("bar") || item.name.contains("ore", ignoreCase = true) || item.name.contains("bar", ignoreCase = true)
                            "HERBS" -> item.category == ItemCategory.HERBS_FARMING || item.id.contains("herb") || item.id.contains("crushed") || item.id.contains("leaf") || item.id.contains("clean") || item.name.contains("herb", ignoreCase = true)
                            "TOY" -> item.category == ItemCategory.TOY
                            "MISC" -> item.category == ItemCategory.MISC
                            else -> true
                        }
                        val matchesSearch = storageSearchQuery.isBlank() ||
                                item.name.contains(storageSearchQuery, ignoreCase = true) ||
                                item.description.contains(storageSearchQuery, ignoreCase = true)
                        matchesCat && matchesSearch
                    }
                }

                val sortedStorageItems = remember(filteredStorageItems, favoriteItemIds) {
                    filteredStorageItems.sortedWith { a, b ->
                        val normA = DefaultItems.normalizeItemId(a.id)
                        val normB = DefaultItems.normalizeItemId(b.id)
                        val aFav = favoriteItemIds.contains(normA) || favoriteItemIds.contains(a.id)
                        val bFav = favoriteItemIds.contains(normB) || favoriteItemIds.contains(b.id)
                        when {
                            aFav && !bFav -> -1
                            !aFav && bFav -> 1
                            else -> 0
                        }
                    }
                }

                if (sortedStorageItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(OsrsLeatherDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (allStorageItems.isEmpty()) "Your Storage Vault is currently empty!\nEngage in skilling, adventuring, or complete favors to gain items." else "No items matched your Storage search/category filter.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp)
                    ) {
                        items(sortedStorageItems) { item ->
                            val normId = DefaultItems.normalizeItemId(item.id)
                            val isFav = favoriteItemIds.contains(normId) || favoriteItemIds.contains(item.id)
                            InventorySlot(
                                item = item,
                                isFavorite = isFav,
                                onClick = { selectedStorageItemForDetail = item },
                                onLongClick = { onToggleFavoriteItem?.invoke(item.id) }
                            )
                        }
                    }
                }
            }
        } else if (selectedTabIndex == 1) {
            // --- 🔥 COOKING FIRE & RAW FOOD RESERVE (Ember NPC Companion only in Fire subtab) ---
            Box(modifier = Modifier.fillMaxWidth()) {
                val emberFavorLevel = viewModel?.getNpcFavorLevel("ember") ?: 1
                CookingFireView(
                    emberFavorLevel = emberFavorLevel,
                    cookingLevel = cookingLevel,
                    cookingXp = cookingXp,
                    isAfkCookingActive = isAfkCookingActive,
                    cookingQueue = cookingQueue,
                    inventoryItems = allStorageItems,
                    bankItems = allStorageItems,
                    onToggleAfkCooking = onToggleAfkCooking,
                    onAddToCookingQueue = onAddToCookingQueue,
                    onRemoveFromCookingQueue = onRemoveFromCookingQueue,
                    onMoveCookingQueueItem = onMoveCookingQueueItem,
                    onClearCookingQueue = onClearCookingQueue,
                    onAutoPopulateCookingQueue = onAutoPopulateCookingQueue,
                    onCookFood = onCookFood,
                    onWithdrawFromBank = onWithdrawFromBank,
                    onDepositToBank = onDepositToBank,
                    modifier = Modifier.fillMaxWidth()
                )

                if (viewModel != null) {
                    EmberNpcCompanion(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else if (selectedTabIndex == 2) {
            // --- 📖 SPIRIT CAULDRON RECIPE BOOK TAB ---
            var recipeSearchQuery by remember { mutableStateOf("") }
            var recipeCategoryFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Unlocked, 2 = Can Cook

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Compact Recipe Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📖 Shaman Recipe Book (20 Recipes)",
                        color = Color(0xFF00FF9D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1F3A2B),
                        border = BorderStroke(1.dp, Color(0xFF00FF9D))
                    ) {
                        Text(
                            "Lv. $cookingLevel Cooking",
                            color = Color(0xFF00FF9D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                val now = System.currentTimeMillis()
                val nonExpiredBuffs = activeCookingBuffs.filter { it.expiryTimeMs > now }
                if (nonExpiredBuffs.isNotEmpty()) {
                    val activeBuff = nonExpiredBuffs.first()
                    val remainingSec = ((activeBuff.expiryTimeMs - now) / 1000).coerceAtLeast(0)
                    val minutes = remainingSec / 60
                    val seconds = remainingSec % 60
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1B3B26),
                        border = BorderStroke(1.dp, Color(0xFF81C784)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🌟 Active Buff: ${activeBuff.recipeName}",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                            Text(
                                "⏱️ %02d:%02d".format(minutes, seconds),
                                color = OsrsGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Compact Search & Filter Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = recipeSearchQuery,
                        onValueChange = { recipeSearchQuery = it },
                        placeholder = { Text("🔍 Search recipes...", color = Color.Gray, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF9D),
                            unfocusedBorderColor = Color(0xFF2C4A36),
                            focusedTextColor = OsrsTextYellow,
                            unfocusedTextColor = OsrsTextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recipe_search_input")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val filterTabs = listOf(
                            "All" to "📖 All",
                            "Unlocked" to "🔓 Unlocked",
                            "Can Cook" to "🍳 Can Cook",
                            "Buffs" to "⚔️ Buffs",
                            "Boosts" to "⚡ Boosts"
                        )
                        itemsIndexed(filterTabs) { index, (_, label) ->
                            FilterChip(
                                selected = recipeCategoryFilter == index,
                                onClick = { recipeCategoryFilter = index },
                                label = { Text(label, fontSize = 10.sp, fontWeight = if (recipeCategoryFilter == index) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.height(32.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E3828),
                                    selectedLabelColor = Color(0xFF00FF9D),
                                    containerColor = Color(0xFF141F18),
                                    labelColor = OsrsParchment
                                )
                            )
                        }
                    }
                }

                // Recipes Grid/List
                val filteredRecipes = remember(recipeSearchQuery, recipeCategoryFilter, cookingLevel, allStorageItems) {
                    CauldronRecipes.ALL_RECIPES.filter { recipe ->
                        val matchesSearch = recipeSearchQuery.isBlank() ||
                                recipe.name.contains(recipeSearchQuery, ignoreCase = true) ||
                                recipe.rawItemName.contains(recipeSearchQuery, ignoreCase = true) ||
                                recipe.item2Name.contains(recipeSearchQuery, ignoreCase = true) ||
                                recipe.buffEffect.contains(recipeSearchQuery, ignoreCase = true)

                        val isUnlocked = cookingLevel >= recipe.reqLevel

                        val isCombatBuff = recipe.boostedSkill != null && (
                            recipe.boostedSkill == OsrsSkill.ATTACK ||
                            recipe.boostedSkill == OsrsSkill.DEFENCE ||
                            recipe.boostedSkill == OsrsSkill.RANGED ||
                                                        recipe.boostedSkill == OsrsSkill.MAGIC ||
                            recipe.boostedSkill == OsrsSkill.HITPOINTS ||
                            recipe.boostedSkill == OsrsSkill.SLAYER ||
                            recipe.boostedSkill == OsrsSkill.ADVENTURING
                        ) || recipe.buffEffect.contains("Attack", ignoreCase = true) ||
                             recipe.buffEffect.contains("Slayer", ignoreCase = true) ||
                             recipe.buffEffect.contains("Hitpoints", ignoreCase = true) ||
                             recipe.buffEffect.contains("Adventuring", ignoreCase = true)

                        val isXpBoost = (recipe.boostedSkill != null && !isCombatBuff) ||
                            recipe.buffEffect.contains("Global", ignoreCase = true) ||
                            recipe.buffEffect.contains("ALL Skills", ignoreCase = true) ||
                            recipe.xpBoostPercent > 0

                        val matchesCategory = when (recipeCategoryFilter) {
                            1 -> isUnlocked
                            3 -> isCombatBuff // "Buffs"
                            4 -> isXpBoost   // "Boosts"
                            else -> true
                        }

                        matchesSearch && matchesCategory
                    }.sortedByDescending { recipe ->
                        if (recipeCategoryFilter == 2) {
                            val target1 = DefaultItems.normalizeItemId(recipe.requiredRawItemId)
                            val total1 = allStorageItems.find { DefaultItems.normalizeItemId(it.id) == target1 }?.quantity ?: 0

                            val target2 = DefaultItems.normalizeItemId(recipe.requiredItem2Id)
                            val total2 = allStorageItems.find { DefaultItems.normalizeItemId(it.id) == target2 }?.quantity ?: 0

                            if (cookingLevel >= recipe.reqLevel && total1 > 0 && total2 > 0) 1 else 0
                        } else 0
                    }
                }

                if (filteredRecipes.isEmpty()) {
                    Text(
                        "No Cauldron Recipes match your search/filter.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 580.dp)
                    ) {
                        items(filteredRecipes) { recipe ->
                            val isUnlocked = cookingLevel >= recipe.reqLevel

                            val target1 = DefaultItems.normalizeItemId(recipe.requiredRawItemId)
                            val total1 = allStorageItems.find { DefaultItems.normalizeItemId(it.id) == target1 }?.quantity ?: 0

                            val target2 = DefaultItems.normalizeItemId(recipe.requiredItem2Id)
                            val total2 = allStorageItems.find { DefaultItems.normalizeItemId(it.id) == target2 }?.quantity ?: 0

                            val hasIngredients = total1 > 0 && total2 > 0

                            Surface(
                                color = if (isUnlocked) Color(0xFF1C2720) else Color(0xFF1A1A1A),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isUnlocked) Color(0xFF2C4A36) else Color(0xFF333333)
                                ),
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(recipe.emoji, fontSize = 22.sp)
                                            Column {
                                                Text(
                                                    recipe.name,
                                                    color = if (isUnlocked) OsrsTextYellow else Color.Gray,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    "Req: Level ${recipe.reqLevel} Cooking",
                                                    color = if (isUnlocked) Color(0xFF81C784) else Color.Red.copy(alpha = 0.7f),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (isUnlocked) {
                                                Button(
                                                    onClick = { onCookRecipeFromBankAndInventory(recipe) },
                                                    enabled = hasIngredients,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF2E6B38),
                                                        disabledContainerColor = Color(0xFF2A2A2A)
                                                    ),
                                                    shape = RoundedCornerShape(4.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier
                                                        .height(28.dp)
                                                        .testTag("cook_recipe_${recipe.id}")
                                                ) {
                                                    Text(
                                                        if (hasIngredients) "Cook 🍲" else "Need Items",
                                                        color = if (hasIngredients) Color.White else Color.Gray,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                val isQueuedInCooking = cookingQueue.contains(recipe.id)
                                                if (isQueuedInCooking) {
                                                    Button(
                                                        onClick = { onRemoveFromCookingQueue(recipe.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                                        shape = RoundedCornerShape(4.dp),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .height(28.dp)
                                                            .testTag("unqueue_recipe_${recipe.id}")
                                                    ) {
                                                        Text("In Queue ✕", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = { onAddToCookingQueue(recipe.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400)),
                                                        shape = RoundedCornerShape(4.dp),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .height(28.dp)
                                                            .testTag("queue_recipe_${recipe.id}")
                                                    ) {
                                                        Text("➕ Queue", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Text("🔒 Lv. ${recipe.reqLevel}", color = Color.Gray, fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFF2C4A36).copy(alpha = 0.5f))

                                    // Ingredients & Buff row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                "🍗 1x ${recipe.rawItemName} ($total1 in storage)",
                                                color = if (total1 > 0) Color(0xFFA5D6A7) else Color.Red.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                "🌿 1x ${recipe.item2Name} ($total2 in storage)",
                                                color = if (total2 > 0) Color(0xFFA5D6A7) else Color.Red.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "Buff Effect:",
                                                color = OsrsGold,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                recipe.buffEffect,
                                                color = OsrsParchment,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Medium
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

    // Storage Item Action Detail Dialog
    selectedStorageItemForDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedStorageItemForDetail = null },
            containerColor = OsrsLeatherMedium,
            title = {
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
                        OsrsItemIcon(item = item, fontSize = 24.sp)
                        Text(text = item.name, color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            },
            text = {
                val unitSellPrice = (item.costGp * 0.75).toLong().coerceAtLeast(1L)
                val totalSellPrice = unitSellPrice * item.quantity

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = item.description, color = OsrsParchment, fontSize = 12.sp)
                    Text(text = "Stored in Vault: ${item.quantity}x", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "GE Estimated Value: $unitSellPrice GP each ($totalSellPrice GP total)", color = OsrsTextYellow, fontSize = 11.sp)
                    if (item.restoreHunger > 0) Text(text = "• Restores ${item.restoreHunger} Hunger & ${item.healHp} HP", color = OsrsTextWhite, fontSize = 11.sp)
                    if (item.addHappiness > 0) Text(text = "• Adds +${item.addHappiness} Happiness", color = OsrsTextWhite, fontSize = 11.sp)
                    if (item.bonusXpSkill != null) Text(text = "• Grants +${item.bonusXpAmount}% ${item.bonusXpSkill.displayName} XP Bonus", color = OsrsTextYellow, fontSize = 11.sp)
                    if (item.combatPowerBonus > 0) Text(text = "• +${item.combatPowerBonus} Attack Bonus", color = Color(0xFFFF8A80), fontSize = 11.sp)
                    if (item.defPowerBonus > 0) Text(text = "• +${item.defPowerBonus} Defence Bonus", color = Color(0xFF80D8FF), fontSize = 11.sp)

                    HorizontalDivider(color = OsrsGold.copy(alpha = 0.5f), thickness = 1.dp)

                    // Primary Item Action (Open / Equip / Feed / Play)
                    if (item.id.startsWith("item_contract_reward_")) {
                        val skillName = item.id.removePrefix("item_contract_reward_").uppercase()
                        val skill = com.example.data.models.OsrsSkill.entries.find { it.name == skillName }
                        if (skill != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onOpenContractReward?.invoke(skill, 1)
                                        selectedStorageItemForDetail = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🎁 Open 1 Box", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                if (item.quantity > 1) {
                                    Button(
                                        onClick = {
                                            onOpenContractReward?.invoke(skill, item.quantity)
                                            selectedStorageItemForDetail = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🎁 Open All (${item.quantity})", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else if (item.id.contains("seed_pouch") || item.name.contains("Seed Pouch")) {
                        Button(
                            onClick = {
                                onOpenSeedPouch?.invoke(item)
                                selectedStorageItemForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B38)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🎁 Open Seed Pouch", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (item.equipmentSlot != null || item.category == ItemCategory.EQUIPMENT) {
                        Button(
                            onClick = {
                                if (onEquipItem != null) {
                                    onEquipItem(item)
                                }
                                selectedStorageItemForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Equip to Companion 🛡️", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (item.category == ItemCategory.FOOD || item.category == ItemCategory.POTION || item.healHp > 0 || item.restoreHunger > 0 || item.isCookedReadyToEatFood || item.isRawUncookedFood) {
                        if (item.isRawUncookedFood) {
                            Button(
                                onClick = {
                                    selectedTabIndex = 1 // Cauldron
                                    viewModel?.setStorageSubTab(1)
                                    selectedStorageItemForDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🍲 Cook in Cauldron", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (item.isCookedReadyToEatFood || item.category == ItemCategory.POTION) {
                            Button(
                                onClick = {
                                    onFeedFromFoodBag(item)
                                    selectedStorageItemForDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🥩 Feed to Companion", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                onPlayPet(item)
                                selectedStorageItemForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B38)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🧸 Play with Companion", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Favorite Toggle
                    val normDetailId = DefaultItems.normalizeItemId(item.id)
                    val isItemFav = favoriteItemIds.contains(normDetailId) || favoriteItemIds.contains(item.id)
                    Button(
                        onClick = {
                            onToggleFavoriteItem?.invoke(item.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isItemFav) Color(0xFF4A3B12) else Color(0xFF2C2219)),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isItemFav) "⭐ Favorited in Vault" else "☆ Set as Favorite", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Sell Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                onSellItem(item, 1)
                                selectedStorageItemForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B6B23)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sell_1_item_button")
                        ) {
                            Text("Sell 1x (+${unitSellPrice} GP)", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (item.quantity > 1) {
                            Button(
                                onClick = {
                                    onSellItem(item, item.quantity)
                                    selectedStorageItemForDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A3D1E)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sell_all_items_button")
                            ) {
                                Text("Sell All (+${totalSellPrice} GP)", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedStorageItemForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                    border = BorderStroke(1.dp, OsrsGold),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("close_storage_detail_button")
                ) {
                    Text("Close", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tab_button_scale"
    )

    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF1E3828) else OsrsLeatherDark)
            .weatheredStoneBorder(
                cornerRadius = 6.dp,
                borderColor = if (isSelected) Color(0xFF00FF9D) else Color(0xFF4A3E31),
                runeGlowAlpha = if (isSelected) 0.9f else 0.3f,
                showRunes = isSelected
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) OsrsTextYellow else OsrsParchment,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun formatOsrsQuantity(qty: Int): String {
    return when {
        qty <= 1 -> ""
        qty < 100_000 -> "$qty"
        qty < 10_000_000 -> "${qty / 1000}K"
        else -> "${qty / 1_000_000}M"
    }
}

@Composable
private fun getOsrsStackColor(qty: Int): Color {
    return when {
        qty < 100_000 -> OsrsTextYellow
        qty < 10_000_000 -> Color.White
        else -> Color(0xFF00FFFF)
    }
}

@Composable
private fun getResolvedItemIcon(item: InventoryItem): String {
    val isArrowOrAmmo = item.equipmentSlot == com.example.data.models.EquipmentSlot.AMMO ||
            item.id.contains("arrow") ||
            item.name.contains("arrow", ignoreCase = true)
    return if (isArrowOrAmmo && item.iconEmoji == "🏹") {
        "➹"
    } else {
        item.iconEmoji
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InventorySlot(
    item: InventoryItem,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val displayIcon = getResolvedItemIcon(item)
    val isAmmo = item.equipmentSlot == com.example.data.models.EquipmentSlot.AMMO || item.id.contains("arrow") || item.name.contains("arrow", ignoreCase = true)
    val isWeapon = item.equipmentSlot == com.example.data.models.EquipmentSlot.WEAPON
    val isArmor = item.equipmentSlot == com.example.data.models.EquipmentSlot.BODY ||
            item.equipmentSlot == com.example.data.models.EquipmentSlot.HEAD ||
            item.equipmentSlot == com.example.data.models.EquipmentSlot.LEGS ||
            item.equipmentSlot == com.example.data.models.EquipmentSlot.SHIELD ||
            item.equipmentSlot == com.example.data.models.EquipmentSlot.BOOTS ||
            item.equipmentSlot == com.example.data.models.EquipmentSlot.GLOVES
    val isRawFood = item.isRawUncookedFood
    val isCookedFood = item.isCookedReadyToEatFood
    val isFood = item.category == ItemCategory.FOOD || isRawFood || isCookedFood

    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        if (isFavorite) Color(0xFF3B2E10) else Color(0xFF2A1F17),
                        if (isFavorite) Color(0xFF2B200A) else Color(0xFF1B120C)
                    )
                )
            )
            .border(if (isFavorite) 1.5.dp else 1.dp, if (isFavorite) Color(0xFFFFD700) else OsrsGold, RoundedCornerShape(6.dp))
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                onLongClick = onLongClick?.let { lc ->
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        lc()
                    }
                }
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        // Authentic 3D Inset OSRS Slot
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF140E0A))
                .border(0.5.dp, Color(0xFF38291D), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Main Item Icon with Category Color Halos & Badges
            OsrsItemIcon(item = item, fontSize = 24.sp)

            // Favorite Star Badge (Top-End)
            if (isFavorite) {
                Text(
                    text = "⭐",
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 1.dp, end = 1.dp)
                )
            }

            // Category Corner Badge
            val badgeSymbol = when {
                isAmmo -> "➹"
                isWeapon -> "⚔️"
                isArmor -> "🛡️"
                isRawFood -> "🟤"
                isCookedFood -> "🍖"
                isFood -> "🍖"
                item.category == ItemCategory.POTION -> "🧪"
                else -> null
            }

            badgeSymbol?.let {
                Text(
                    text = it,
                    fontSize = 9.sp,
                    color = if (isAmmo) OsrsGold else if (isRawFood) Color(0xFF8D6E63) else if (isCookedFood) Color(0xFFFF9800) else Color.LightGray,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 1.dp, end = 2.dp)
                )
            }

            // OSRS Style Stack Quantity Overlay (Top-Start)
            if (item.quantity > 1) {
                Text(
                    text = formatOsrsQuantity(item.quantity),
                    color = getOsrsStackColor(item.quantity),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 2.dp, top = 1.dp)
                )
            }
        }
    }
}
