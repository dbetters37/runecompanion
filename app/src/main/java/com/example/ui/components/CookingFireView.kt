package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CookingCategory
import com.example.data.models.CookingFireRecipe
import com.example.data.models.CookingRecipes
import com.example.data.models.InventoryItem
import com.example.ui.components.BonusBreakdownDialog
import com.example.ui.components.BonusSourceDetail
import com.example.ui.components.CooldownActionButton
import com.example.ui.theme.*

@Composable
fun CookingFireView(
    cookingLevel: Int,
    cookingXp: Long,
    isAfkCookingActive: Boolean,
    cookingQueue: List<String>,
    inventoryItems: List<InventoryItem>,
    bankItems: List<InventoryItem>,
    onToggleAfkCooking: () -> Unit,
    onAddToCookingQueue: (String) -> Unit,
    onRemoveFromCookingQueue: (String) -> Unit,
    onMoveCookingQueueItem: (String, Int) -> Unit,
    onClearCookingQueue: () -> Unit,
    onAutoPopulateCookingQueue: () -> Unit,
    onCookFood: (String, Int) -> Unit,
    onWithdrawFromBank: (InventoryItem, Int) -> Unit = { _, _ -> },
    onDepositToBank: (InventoryItem, Int) -> Unit = { _, _ -> },
    emberFavorLevel: Int = 1,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CookingCategory.ALL) }
    var showBonusBreakdownDialog by remember { mutableStateOf(false) }

    val maxQueueSlots = remember(cookingLevel) {
        CookingRecipes.getMaxQueueSlots(cookingLevel)
    }

    // Flame animation for active cooking fire
    val infiniteTransition = rememberInfiniteTransition(label = "fire_anim")
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire_scale"
    )

    // Gather all raw food items: all static recipes + any raw items owned in backpack or bank
    val rawReserveItems = remember(inventoryItems, bankItems, searchQuery, selectedCategory) {
        val query = searchQuery.trim().lowercase()

        // 1. Start with all static recipes
        val recipesMap = LinkedHashMap<String, CookingFireRecipe>()
        CookingRecipes.ALL_RECIPES.forEach { recipe ->
            recipesMap[recipe.rawId] = recipe
        }

        // 2. Discover any raw uncooked food items in bank that might not be in static recipes
        bankItems.forEach { item ->
            if (CookingRecipes.isRawFoodItem(item) && !recipesMap.containsKey(item.id)) {
                recipesMap[item.id] = CookingRecipes.getOrCreateRecipeForItem(item)
            }
        }

        // 3. Filter and map with storage counts
        recipesMap.values.map { recipe ->
            val normRaw = com.example.data.models.DefaultItems.normalizeItemId(recipe.rawId)
            val matchingItem = bankItems.find { it.id == recipe.rawId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normRaw || it.name.equals(recipe.rawName, true) }
            val totalAvail = matchingItem?.quantity ?: 0
            Pair(recipe, totalAvail)
        }.filter { (recipe, totalAvail) ->
            val matchesCategory = when (selectedCategory) {
                CookingCategory.ALL -> true
                CookingCategory.READY_TO_COOK -> totalAvail > 0
                else -> recipe.category == selectedCategory
            }
            val matchesSearch = query.isEmpty() ||
                    recipe.rawName.lowercase().contains(query) ||
                    recipe.cookedName.lowercase().contains(query)
            matchesCategory && matchesSearch
        }.distinctBy { it.first.rawId }
        .sortedWith(
            compareBy<Pair<CookingFireRecipe, Int>> { it.first.reqLevel }
                .thenBy { it.first.rawName }
        )
    }

    val totalRawInBank = remember(bankItems) {
        bankItems.filter { CookingRecipes.isRawFoodItem(it) }.sumOf { it.quantity }
    }

    val totalRawAvailable = remember(totalRawInBank) {
        totalRawInBank
    }

    val totalCookedInBag = remember(bankItems) {
        bankItems.filter { it.isCookedReadyToEatFood }.sumOf { it.quantity }
    }

        val cauldronRecipesToDisplay = remember(selectedCategory, searchQuery, bankItems) {
        val query = searchQuery.trim().lowercase()
        com.example.data.models.CauldronRecipes.ALL_RECIPES.filter { rec ->
            val q1 = bankItems.find { it.id == rec.requiredRawItemId }?.quantity ?: 0
            val q2 = bankItems.find { it.id == rec.requiredItem2Id }?.quantity ?: 0
            val totalAvail = minOf(q1, q2)
            val matchesCategory = when (selectedCategory) {
                CookingCategory.ALL -> true
                CookingCategory.READY_TO_COOK -> totalAvail > 0
                CookingCategory.RECIPES -> true
                else -> false
            }
            val matchesSearch = query.isEmpty() || rec.name.lowercase().contains(query) || rec.cookedItemName.lowercase().contains(query)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cooking_fire_view"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        // --- 1. HERO COOKING FIRE HEADER ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cooking_fire_hero_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAfkCookingActive) Color(0xFF26150C) else Color(0xFF1E1610)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isAfkCookingActive) Color(0xFFFF7043) else OsrsGold
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Row 1: Title, Badges & Level Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isAfkCookingActive) "🔥" else "🪵",
                                fontSize = if (isAfkCookingActive) (18 * fireScale).sp else 18.sp
                            )
                            Column {
                                Text(
                                    text = "Sacred Cooking Fire",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OsrsTextYellow
                                )
                                Text(
                                    text = "Cooking Lv.$cookingLevel • Queue: ${cookingQueue.size}/$maxQueueSlots Slots",
                                    fontSize = 10.5.sp,
                                    color = OsrsParchment
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = if (isAfkCookingActive) Color(0xFFD84315) else Color(0xFF3E2723),
                                border = BorderStroke(0.5.dp, if (isAfkCookingActive) Color(0xFFFFB74D) else Color.Gray)
                            ) {
                                Text(
                                    text = if (isAfkCookingActive) "🔥 ACTIVE" else "⏸️ IDLE",
                                    color = if (isAfkCookingActive) Color.White else Color.LightGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = Color(0xFFD84315).copy(alpha = 0.35f),
                                border = BorderStroke(0.8.dp, Color(0xFFFFB74D)),
                                modifier = Modifier
                                    .clickable { showBonusBreakdownDialog = true }
                                    .testTag("badge_extra_food_chance")
                            ) {
                                Text(
                                    text = "+${emberFavorLevel}% Extra ⓘ",
                                    color = Color(0xFFFFCCBC),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Row 2: AFK Toggle Action Button
                    CooldownActionButton(
                        onClick = onToggleAfkCooking,
                        cooldownMs = 600L,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAfkCookingActive) Color(0xFFBF360C) else Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (isAfkCookingActive) Color(0xFFFFB74D) else Color(0xFF81C784)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        Text(
                            text = if (isAfkCookingActive) "🛑 STOP COOKING FIRE" else "⚡ START COOKING FIRE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Unlock Progression Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF140D08),
                        border = BorderStroke(0.5.dp, Color(0xFF4E342E))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CookingRecipes.getNextUnlockDescription(cookingLevel),
                                fontSize = 8.5.sp,
                                color = if (cookingLevel >= 80) Color(0xFFFFD54F) else Color(0xFFFFCC80),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🪵 Raw Food: $totalRawAvailable in Bank",
                                fontSize = 8.5.sp,
                                color = if (totalRawAvailable > 0) Color(0xFF81C784) else Color(0xFFE57373)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. AFK COOKING QUEUE SECTION (Sequential Auto-Cooking) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cooking_queue_card"),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF201610)),
                border = BorderStroke(1.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Queue Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📋", fontSize = 12.sp)
                            Text(
                                text = "AFK Cooking Queue (${cookingQueue.size}/$maxQueueSlots)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OsrsTextYellow
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (totalRawAvailable > 0 && cookingQueue.size < maxQueueSlots) {
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = Color(0xFF2E7D32),
                                    border = BorderStroke(0.5.dp, Color(0xFF81C784)),
                                    modifier = Modifier.clickable { onAutoPopulateCookingQueue() }
                                ) {
                                    Text(
                                        text = "⚡ Auto-Fill",
                                        color = Color.White,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (cookingQueue.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = Color(0xFF3E2723),
                                    border = BorderStroke(0.5.dp, Color.Gray),
                                    modifier = Modifier.clickable { onClearCookingQueue() }
                                ) {
                                    Text(
                                        text = "🗑️ Clear",
                                        color = Color.LightGray,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Render only the active queued items in sleek, compact rows
                    if (cookingQueue.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF140E0A),
                            border = BorderStroke(0.5.dp, Color(0xFF3E2D20))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("➕", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = "Queue empty — Tap '➕ Add to AFK Queue' on any food below",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        val activeSlotIndex = cookingQueue.indexOfFirst { qId ->
                            val normId = com.example.data.models.DefaultItems.normalizeItemId(qId)
                            val bQty = bankItems.find { it.id == qId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normId }?.quantity ?: 0
                            bQty > 0
                        }
                        cookingQueue.forEachIndexed { slotIndex, queuedRawId ->
                            val cauldronRec = com.example.data.models.CauldronRecipes.ALL_RECIPES.find { it.id == queuedRawId }
                            val recipe = if (cauldronRec != null) null else CookingRecipes.findRecipe(queuedRawId)
                            val normId = com.example.data.models.DefaultItems.normalizeItemId(queuedRawId)
                            val matchingBank = if (cauldronRec != null) null else bankItems.find { it.id == queuedRawId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normId }
                            val totalAvail = if (cauldronRec != null) {
                                val q1 = bankItems.find { it.id == cauldronRec.requiredRawItemId }?.quantity ?: 0
                                val q2 = bankItems.find { it.id == cauldronRec.requiredItem2Id }?.quantity ?: 0
                                minOf(q1, q2)
                            } else {
                                matchingBank?.quantity ?: 0
                            }
                            val displayName = cauldronRec?.name ?: recipe?.rawName ?: queuedRawId.removePrefix("item_raw_").replace("_", " ").replaceFirstChar { it.uppercase() }
                            val displayEmoji = cauldronRec?.emoji ?: recipe?.emoji ?: "🍲"
                            val isActiveNow = isAfkCookingActive && slotIndex == activeSlotIndex
                            val isOutOfStock = totalAvail == 0

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cooking_queue_slot_$slotIndex"),
                                shape = RoundedCornerShape(4.dp),
                                color = when {
                                    isActiveNow -> Color(0xFF2B1910)
                                    isOutOfStock -> Color(0xFF1C120D)
                                    else -> Color(0xFF18110B)
                                },
                                border = BorderStroke(
                                    width = if (isActiveNow) 1.dp else 0.5.dp,
                                    color = when {
                                        isActiveNow -> Color(0xFFFF7043)
                                        isOutOfStock -> Color(0xFFD32F2F)
                                        else -> Color(0xFF4E342E)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 3.5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Slot badge, Emoji, Name, and compact quantity
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = if (isActiveNow) Color(0xFFD84315) else Color(0xFF3E2723)
                                        ) {
                                            Text(
                                                text = if (isActiveNow) "🔥 #${slotIndex + 1}" else "#${slotIndex + 1}",
                                                color = if (isActiveNow) Color.White else OsrsTextYellow,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }

                                        Text(displayEmoji, fontSize = 13.sp)

                                        Text(
                                            text = displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOutOfStock) Color(0xFFEF5350) else OsrsTextWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = if (totalAvail > 0) "• $totalAvail Left" else "• Out of Stock",
                                            fontSize = 9.sp,
                                            color = if (totalAvail > 0) Color(0xFF81C784) else Color(0xFFEF5350),
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }

                                    // Right: Compact Controls (Move Up, Move Down, Delete)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (slotIndex > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFF2B1F17),
                                                border = BorderStroke(0.5.dp, Color(0xFF5D4037)),
                                                modifier = Modifier.clickable { onMoveCookingQueueItem(queuedRawId, -1) }
                                            ) {
                                                Text("⬆️", fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.5.dp))
                                            }
                                        }

                                        if (slotIndex < cookingQueue.size - 1) {
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFF2B1F17),
                                                border = BorderStroke(0.5.dp, Color(0xFF5D4037)),
                                                modifier = Modifier.clickable { onMoveCookingQueueItem(queuedRawId, 1) }
                                            ) {
                                                Text("⬇️", fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.5.dp))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xFF4E342E),
                                            border = BorderStroke(0.5.dp, Color(0xFF795548)),
                                            modifier = Modifier.clickable { onRemoveFromCookingQueue(queuedRawId) }
                                        ) {
                                            Text("❌", fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Subtle footer when there are more unlocked slots remaining
                        if (cookingQueue.size < maxQueueSlots) {
                            Text(
                                text = "+ ${maxQueueSlots - cookingQueue.size} more queue slot(s) available",
                                fontSize = 8.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. RAW FOOD RESERVE / PANTRY ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Category Selector Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🥩 Raw Food Reserve (${rawReserveItems.size} Types)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OsrsTextYellow
                    )
                    Text(
                        text = "🎒 Ready Food: $totalCookedInBag",
                        fontSize = 10.sp,
                        color = OsrsParchment
                    )
                }

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CookingCategory.entries.forEach { cat ->
                        val isSel = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSel) Color(0xFFD84315) else Color(0xFF241A12),
                            border = BorderStroke(1.dp, if (isSel) OsrsGold else Color(0xFF4E342E)),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = "${cat.emoji} ${cat.label}",
                                color = if (isSel) Color.White else OsrsParchment,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reserve Items List: Clean, Spacious, Highly Legible Cards
        items(rawReserveItems, key = { it.first.rawId }) { (recipe, totalAvail) ->
            val isUnlocked = cookingLevel >= recipe.reqLevel
            val isQueued = cookingQueue.contains(recipe.rawId)
            val queuePosition = cookingQueue.indexOf(recipe.rawId)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("raw_reserve_${recipe.rawId}"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isQueued -> Color(0xFF2E1C12)
                        totalAvail > 0 -> Color(0xFF221710)
                        else -> Color(0xFF160F0A)
                    }
                ),
                border = BorderStroke(
                    width = if (isQueued) 1.5.dp else if (totalAvail > 0) 1.dp else 0.5.dp,
                    color = when {
                        isQueued -> OsrsGold
                        totalAvail > 0 -> Color(0xFF6D4C41)
                        else -> Color(0xFF3E2723)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Row 1: Food Name, Emoji, Level Requirement, XP, Queued Badge
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
                            Text(recipe.emoji, fontSize = 22.sp)

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = recipe.rawName,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) OsrsTextWhite else Color.LightGray
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isUnlocked) "Lv. ${recipe.reqLevel}" else "🔒 Req Lv. ${recipe.reqLevel}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFEF5350)
                                    )
                                    Text(
                                        text = "• +${recipe.xpEarned} XP",
                                        fontSize = 10.5.sp,
                                        color = OsrsGold
                                    )
                                    Text(
                                        text = "• Cooks to ${recipe.cookedName}",
                                        fontSize = 10.sp,
                                        color = OsrsParchment,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (isQueued) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD84315),
                                border = BorderStroke(1.dp, OsrsGold)
                            ) {
                                Text(
                                    text = "🔥 Slot #${queuePosition + 1} Queued",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Row 2: Prominent Stock Indicator
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        color = if (totalAvail > 0) Color(0xFF18100A) else Color(0xFF120B07),
                        border = BorderStroke(0.5.dp, if (totalAvail > 0) Color(0xFF4E342E) else Color(0xFF2C190F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (totalAvail > 0) "📦 Left to Cook:" else "📦 Out of Stock:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (totalAvail > 0) OsrsTextYellow else Color.Gray
                                )
                                Text(
                                    text = "$totalAvail available",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalAvail > 0) Color(0xFF81C784) else Color(0xFFEF5350)
                                )
                            }

                            Text(
                                text = "($totalAvail in Storage)",
                                fontSize = 10.sp,
                                color = if (totalAvail > 0) OsrsParchment else Color.DarkGray
                            )
                        }
                    }

                    // Row 3: Primary Action — Add/Remove from AFK Queue
                    if (isQueued) {
                        Button(
                            onClick = { onRemoveFromCookingQueue(recipe.rawId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = "❌ Remove from AFK Queue (Slot #${queuePosition + 1})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFCC80)
                            )
                        }
                    } else {
                        val canAdd = isUnlocked && cookingQueue.size < maxQueueSlots
                        Button(
                            onClick = { onAddToCookingQueue(recipe.rawId) },
                            enabled = canAdd,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                disabledContainerColor = Color(0xFF1E140E)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = when {
                                    !isUnlocked -> "🔒 Locked (Requires Cooking Lv.${recipe.reqLevel})"
                                    cookingQueue.size >= maxQueueSlots -> "➕ Add to Queue (Queue Full $maxQueueSlots/$maxQueueSlots)"
                                    else -> "➕ Add to AFK Queue"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canAdd) Color.White else Color.Gray
                            )
                        }
                    }

                    // Row 4: Manual Instant Cook Buttons (if player has raw stock and level)
                    if (totalAvail > 0 && isUnlocked) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CooldownActionButton(
                                onClick = { onCookFood(recipe.rawId, 1) },
                                cooldownMs = 400L,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Text(
                                    text = "🔥 Cook 1x",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            val batchQty = minOf(totalAvail, 28)
                            CooldownActionButton(
                                onClick = { onCookFood(recipe.rawId, batchQty) },
                                cooldownMs = 600L,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF360C)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(34.dp)
                            ) {
                                Text(
                                    text = if (totalAvail <= 28) "🔥 Cook All ($totalAvail)" else "🔥 Cook Batch (28)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CAULDRON RECIPES IN COOKING QUEUE ---

        if (cauldronRecipesToDisplay.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🍲 Spirit Cauldron Recipes (${cauldronRecipesToDisplay.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OsrsTextYellow
                    )
                    Text(
                        text = "Can be queued for AFK cooking",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }
            }

            items(cauldronRecipesToDisplay, key = { "cauldron_${it.id}" }) { recipe ->
                val q1 = bankItems.find { it.id == recipe.requiredRawItemId }?.quantity ?: 0
                val q2 = bankItems.find { it.id == recipe.requiredItem2Id }?.quantity ?: 0
                val totalAvail = minOf(q1, q2)
                val isUnlocked = cookingLevel >= recipe.reqLevel
                val isQueued = cookingQueue.contains(recipe.id)
                val queuePosition = cookingQueue.indexOf(recipe.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cauldron_recipe_${recipe.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isQueued -> Color(0xFF2E1C12)
                            totalAvail > 0 -> Color(0xFF221710)
                            else -> Color(0xFF160F0A)
                        }
                    ),
                    border = BorderStroke(
                        width = if (isQueued) 1.5.dp else if (totalAvail > 0) 1.dp else 0.5.dp,
                        color = when {
                            isQueued -> OsrsGold
                            totalAvail > 0 -> Color(0xFF8D6E63)
                            else -> Color(0xFF3E2723)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                Text(recipe.emoji, fontSize = 22.sp)
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = recipe.name,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) OsrsTextWhite else Color.LightGray
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isUnlocked) "Lv. ${recipe.reqLevel}" else "🔒 Req Lv. ${recipe.reqLevel}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFEF5350)
                                        )
                                        Text(
                                            text = "• +${recipe.cookingXp} XP",
                                            fontSize = 10.5.sp,
                                            color = OsrsGold
                                        )
                                        Text(
                                            text = "• ${recipe.buffEffect}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF80DEEA),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            if (isQueued) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFD84315),
                                    border = BorderStroke(1.dp, OsrsGold)
                                ) {
                                    Text(
                                        text = "🔥 Slot #${queuePosition + 1} Queued",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Ingredients row
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            color = if (totalAvail > 0) Color(0xFF18100A) else Color(0xFF120B07),
                            border = BorderStroke(0.5.dp, if (totalAvail > 0) Color(0xFF4E342E) else Color(0xFF2C190F))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Requires: 1x ${recipe.rawItemName} ($q1 in Bank) + 1x ${recipe.item2Name} ($q2 in Bank)",
                                    fontSize = 9.5.sp,
                                    color = if (totalAvail > 0) Color(0xFF81C784) else Color(0xFFEF5350),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (totalAvail > 0) "Can make $totalAvail batch(es)" else "Missing ingredients in Bank Storage",
                                    fontSize = 9.sp,
                                    color = if (totalAvail > 0) OsrsParchment else Color.Gray
                                )
                            }
                        }

                        // Action buttons
                        if (isQueued) {
                            Button(
                                onClick = { onRemoveFromCookingQueue(recipe.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = "❌ Remove from AFK Queue (Slot #${queuePosition + 1})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFCC80)
                                )
                            }
                        } else {
                            val canAdd = isUnlocked && cookingQueue.size < maxQueueSlots
                            Button(
                                onClick = { onAddToCookingQueue(recipe.id) },
                                enabled = canAdd,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32),
                                    disabledContainerColor = Color(0xFF1E140E)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = when {
                                        !isUnlocked -> "🔒 Locked (Requires Cooking Lv.${recipe.reqLevel})"
                                        cookingQueue.size >= maxQueueSlots -> "➕ Add to Queue (Queue Full $maxQueueSlots/$maxQueueSlots)"
                                        else -> "➕ Add Recipe to AFK Queue"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAdd) Color.White else Color.Gray
                                )
                            }
                        }

                        // Manual instant cook button
                        if (totalAvail > 0 && isUnlocked) {
                            CooldownActionButton(
                                onClick = { onCookFood(recipe.id, 1) },
                                cooldownMs = 400L,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                            ) {
                                Text(
                                    text = "🍲 Cook 1x ${recipe.name}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBonusBreakdownDialog) {
        BonusBreakdownDialog(
            title = "Extra Food Cooking Chance",
            categoryName = "Campfire Cooking & Culinary Arts",
            iconEmoji = "🔥",
            sources = listOf(
                BonusSourceDetail(
                    title = "Ember the Cook's Favor (Lv. $emberFavorLevel)",
                    description = "Grants +1% chance per favor level to cook extra dishes and bonus culinary treats from cooking fires (Up to +50%).",
                    bonusPercent = emberFavorLevel,
                    emoji = "👨‍🍳",
                    isUnlocked = true
                )
            ),
            note = "When triggered, an additional cooked dish is prepared in the same cooking tick without extra raw food!",
            onDismiss = { showBonusBreakdownDialog = false }
        )
    }
}