package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.data.models.ActiveSummoningCompanion
import com.example.data.models.InventoryItem
import com.example.data.models.SummonableAnimal
import com.example.data.models.SummoningData
import com.example.ui.theme.*

/**
 * CompanionQuickPanels displays two square panels side-by-side:
 * - Left Panel: Next food in queue for the Food button, with instant change dialog and feed action.
 * - Right Panel: Last used spirit totem with status, instant change dialog, and a quick "Use Totem" button underneath.
 */
@Composable
fun CompanionQuickPanels(
    queuedFood: InventoryItem?,
    allCookedFoods: List<InventoryItem>,
    lastUsedTotem: SummonableAnimal,
    totemStockCount: Int,
    activeSummon: ActiveSummoningCompanion?,
    allTotems: List<SummonableAnimal> = SummoningData.ALL_ANIMALS,
    getTotemCount: (String) -> Int,
    onFeedQueuedFood: (InventoryItem?) -> Unit,
    onSelectQueuedFood: (InventoryItem?) -> Unit,
    onUseTotem: (String) -> Unit,
    onSelectTotem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showFoodQueueDialog by remember { mutableStateOf(false) }
    var showTotemSelectDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- LEFT SQUARE PANEL: FOOD QUEUE ---
        QueuedFoodSquarePanel(
            queuedFood = queuedFood,
            onOpenFoodSelector = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showFoodQueueDialog = true
            },
            onFeed = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onFeedQueuedFood(queuedFood)
            },
            modifier = Modifier.weight(1f)
        )

        // --- RIGHT SQUARE PANEL: LAST USED TOTEM ---
        LastTotemSquarePanel(
            totem = lastUsedTotem,
            totemStockCount = totemStockCount,
            activeSummon = activeSummon,
            onOpenTotemSelector = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showTotemSelectDialog = true
            },
            onUseTotem = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onUseTotem(lastUsedTotem.id)
            },
            modifier = Modifier.weight(1f)
        )
    }

    // Food Selector Dialog
    if (showFoodQueueDialog) {
        SelectQueuedFoodDialog(
            currentQueuedFood = queuedFood,
            availableCookedFoods = allCookedFoods,
            onSelectFood = { item ->
                onSelectQueuedFood(item)
                showFoodQueueDialog = false
            },
            onFeedImmediate = { item ->
                onSelectQueuedFood(item)
                onFeedQueuedFood(item)
                showFoodQueueDialog = false
            },
            onDismiss = { showFoodQueueDialog = false }
        )
    }

    // Totem Selector Dialog
    if (showTotemSelectDialog) {
        SelectSpiritTotemDialog(
            currentTotem = lastUsedTotem,
            allTotems = allTotems,
            activeSummon = activeSummon,
            getTotemCount = getTotemCount,
            onSelectTotem = { totem ->
                onSelectTotem(totem.id)
                showTotemSelectDialog = false
            },
            onUseTotemImmediate = { totem ->
                onSelectTotem(totem.id)
                onUseTotem(totem.id)
                showTotemSelectDialog = false
            },
            onDismiss = { showTotemSelectDialog = false }
        )
    }
}

/**
 * Left Square Panel: Queued Food
 */
@Composable
private fun QueuedFoodSquarePanel(
    queuedFood: InventoryItem?,
    onOpenFoodSelector: () -> Unit,
    onFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .testTag("food_queue_panel"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF241A12)),
        border = BorderStroke(1.5.dp, OsrsGold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF332216), Color(0xFF1E140D))
                    )
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFoodSelector() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("🍗", fontSize = 11.sp)
                    Text(
                        text = "FOOD QUEUE",
                        color = OsrsTextYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF422C1A), RoundedCornerShape(4.dp))
                        .border(0.8.dp, OsrsGoldBright, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Change",
                        color = OsrsGoldBright,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Food Display Center
            if (queuedFood != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFoodSelector() }
                        .padding(horizontal = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3E2D1D))
                            .border(1.dp, OsrsGoldBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(queuedFood.iconEmoji, fontSize = 20.sp)
                    }

                    Text(
                        text = queuedFood.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (queuedFood.quantity > 0) "x${queuedFood.quantity} in Stock" else "0 in Stock",
                        color = if (queuedFood.quantity > 0) Color(0xFF81C784) else Color(0xFFEF9A9A),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "+${queuedFood.healHp} HP • +${queuedFood.restoreHunger} Food",
                        color = Color(0xFFFFCC80),
                        fontSize = 8.5.sp,
                        maxLines = 1
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFoodSelector() }
                        .padding(horizontal = 4.dp)
                ) {
                    Text("🍽️", fontSize = 26.sp)
                    Text(
                        text = "No Food Queued",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tap to choose meal",
                        color = OsrsGoldBright,
                        fontSize = 8.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            val isFoodAvailable = queuedFood != null && queuedFood.quantity > 0

            // Bottom Feed Action Button
            Button(
                onClick = onFeed,
                enabled = isFoodAvailable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OsrsRedFrame,
                    disabledContainerColor = Color(0xFF2C1D13),
                    contentColor = OsrsTextYellow,
                    disabledContentColor = Color(0xFF757575)
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("feed_pet_button")
            ) {
                Text(
                    text = if (isFoodAvailable) "🍗 Feed 1x" else "🍗 No Food",
                    color = if (isFoodAvailable) OsrsTextYellow else Color(0xFF9E9E9E),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Right Square Panel: Last Used Totem & Quick Use
 */
@Composable
private fun LastTotemSquarePanel(
    totem: SummonableAnimal,
    totemStockCount: Int,
    activeSummon: ActiveSummoningCompanion?,
    onOpenTotemSelector: () -> Unit,
    onUseTotem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = activeSummon?.animalId == totem.id
    val remainingSec = activeSummon?.remainingSeconds ?: 0
    val remainingMin = (remainingSec + 59) / 60

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onOpenTotemSelector() }
            .testTag("totem_panel"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1528)),
        border = BorderStroke(1.5.dp, if (isActive) Color(0xFFB388FF) else OsrsGold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF281A38), Color(0xFF140D20))
                    )
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("🗿", fontSize = 11.sp)
                    Text(
                        text = "LAST TOTEM",
                        color = Color(0xFFE1BEE7),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF381F4B), RoundedCornerShape(4.dp))
                        .border(0.8.dp, Color(0xFFCE93D8), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Change",
                        color = Color(0xFFE1BEE7),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Totem Display Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B2553))
                        .border(1.dp, if (isActive) Color(0xFF80CBC4) else Color(0xFFCE93D8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(totem.iconEmoji, fontSize = 20.sp)
                }

                Text(
                    text = "${totem.name} Totem",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                if (isActive) {
                    Text(
                        text = "✨ ACTIVE (${remainingMin}m)",
                        color = Color(0xFF80CBC4),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Text(
                        text = if (totemStockCount > 0) "x$totemStockCount in Stock" else "0 in Stock",
                        color = if (totemStockCount > 0) Color(0xFF81C784) else Color(0xFFEF9A9A),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = totem.benefitText,
                    color = Color(0xFFCE93D8),
                    fontSize = 8.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Use Totem Action Button
            Button(
                onClick = onUseTotem,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFF1B5E20) else Color(0xFF4A148C)
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("use_last_totem_button")
            ) {
                Text(
                    text = if (isActive) "✨ Renew ($totemStockCount)" else "✨ Use Totem ($totemStockCount)",
                    color = OsrsTextYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Dialog to change and select the queued food item for the food button
 */
@Composable
fun SelectQueuedFoodDialog(
    currentQueuedFood: InventoryItem?,
    availableCookedFoods: List<InventoryItem>,
    onSelectFood: (InventoryItem?) -> Unit,
    onFeedImmediate: (InventoryItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredFoods = remember(availableCookedFoods, searchQuery, selectedCategory) {
        availableCookedFoods.filter { food ->
            val matchesSearch = searchQuery.isBlank() || 
                food.name.contains(searchQuery, ignoreCase = true) ||
                food.description.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = when (selectedCategory) {
                "IN_STOCK" -> food.quantity > 0
                "FISH" -> food.name.contains("fish", true) || food.name.contains("shrimp", true) ||
                          food.name.contains("trout", true) || food.name.contains("salmon", true) ||
                          food.name.contains("lobster", true) || food.name.contains("swordfish", true) ||
                          food.name.contains("shark", true) || food.name.contains("ray", true) ||
                          food.name.contains("eel", true) || food.name.contains("crab", true) ||
                          food.name.contains("turtle", true) || food.name.contains("angler", true) ||
                          food.name.contains("anchov", true) || food.name.contains("sardine", true) ||
                          food.name.contains("herring", true) || food.name.contains("mackerel", true) ||
                          food.name.contains("cod", true) || food.name.contains("pike", true) ||
                          food.name.contains("tuna", true) || food.name.contains("bass", true) ||
                          food.name.contains("karambwan", true)
                "MEAT" -> food.name.contains("meat", true) || food.name.contains("chicken", true) ||
                          food.name.contains("beef", true) || food.name.contains("steak", true) ||
                          food.name.contains("bear", true) || food.name.contains("roast", true)
                "BAKERY" -> food.name.contains("bread", true) || food.name.contains("pie", true) ||
                            food.name.contains("pizza", true) || food.name.contains("cake", true) ||
                            food.name.contains("stew", true) || food.name.contains("curry", true)
                "SPIRIT" -> food.name.contains("spirit", true) || food.name.contains("astral", true) ||
                            food.name.contains("ethereal", true) || food.name.contains("magma", true) ||
                            food.name.contains("cosmic", true) || food.name.contains("dragonfish", true) ||
                            food.name.contains("shaman", true) || food.name.contains("ember", true) ||
                            food.name.contains("obsidian", true)
                else -> true
            }
            matchesSearch && matchesCategory
        }.sortedWith(
            compareByDescending<InventoryItem> { it.quantity > 0 }
                .thenByDescending { it.healHp }
                .thenBy { it.name }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF241A12)),
            border = BorderStroke(2.dp, OsrsGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🍗", fontSize = 18.sp)
                        Text(
                            text = "SELECT QUEUED FOOD",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Text(
                    text = "Pick which cooked dish your companion eats when tapping the Feed button:",
                    color = Color.LightGray,
                    fontSize = 10.5.sp
                )

                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val categories = listOf(
                        "ALL" to "🍱 All",
                        "IN_STOCK" to "⭐ In Stock",
                        "FISH" to "🐟 Fish",
                        "MEAT" to "🍗 Meat",
                        "BAKERY" to "🥧 Bakery",
                        "SPIRIT" to "✨ Spirit"
                    )
                    categories.forEach { (catKey, catLabel) ->
                        val isCatSelected = selectedCategory == catKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCatSelected) OsrsGold else Color(0xFF332216))
                                .border(0.8.dp, if (isCatSelected) OsrsGoldBright else Color(0xFF4A3423), RoundedCornerShape(4.dp))
                                .clickable { selectedCategory = catKey }
                                .padding(horizontal = 5.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = catLabel,
                                color = if (isCatSelected) Color.Black else Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Auto Select Option Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFood(null) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentQueuedFood == null) Color(0xFF4E3725) else Color(0xFF1E150F)
                    ),
                    border = BorderStroke(
                        if (currentQueuedFood == null) 1.5.dp else 1.dp,
                        if (currentQueuedFood == null) OsrsGoldBright else Color(0xFF3E2D1D)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⭐", fontSize = 18.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Select Best Food",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = "Automatically feeds highest healing food from backpack & bank",
                                color = Color.LightGray,
                                fontSize = 9.5.sp
                            )
                        }
                        if (currentQueuedFood == null) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = OsrsGoldBright, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // List of available cooked foods
                if (filteredFoods.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFF1B130D), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF332216), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🍳", fontSize = 24.sp)
                            Text(
                                text = "No Cooked Foods Found",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = "Catch fish or hunt meat, then cook at the Campfire to prepare meals for your companion!",
                                color = Color.LightGray,
                                fontSize = 9.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(filteredFoods, key = { it.id }) { item ->
                            val isSelected = currentQueuedFood?.id == item.id
                            val hasStock = item.quantity > 0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectFood(item) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF4A3423) else Color(0xFF1D140E)
                                ),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) OsrsGoldBright else Color(0xFF332216)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E1F14))
                                            .border(1.dp, if (isSelected) OsrsGoldBright else Color(0xFF4A3423), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.iconEmoji, fontSize = 17.sp)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = item.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (hasStock) "x${item.quantity}" else "x0",
                                                color = if (hasStock) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                        Text(
                                            text = "+${item.healHp} HP • +${item.restoreHunger} Food",
                                            color = Color(0xFFFFCC80),
                                            fontSize = 9.sp
                                        )
                                    }

                                    if (hasStock) {
                                        Button(
                                            onClick = { onFeedImmediate(item) },
                                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("Feed 1x", color = OsrsTextYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = { onSelectFood(item) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332216)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("Queue", color = Color.LightGray, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2D1D)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = OsrsTextYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Dialog to change and select the active spirit totem
 */
@Composable
fun SelectSpiritTotemDialog(
    currentTotem: SummonableAnimal,
    allTotems: List<SummonableAnimal>,
    activeSummon: ActiveSummoningCompanion?,
    getTotemCount: (String) -> Int,
    onSelectTotem: (SummonableAnimal) -> Unit,
    onUseTotemImmediate: (SummonableAnimal) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 540.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1528)),
            border = BorderStroke(2.dp, Color(0xFFB388FF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🗿", fontSize = 18.sp)
                        Text(
                            text = "SELECT SPIRIT TOTEM",
                            color = Color(0xFFE1BEE7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Text(
                    text = "Select which Spirit Totem appears on your companion screen for quick invocation:",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                // Totem list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allTotems, key = { it.id }) { totem ->
                        val isSelected = currentTotem.id == totem.id
                        val count = getTotemCount(totem.id)
                        val isThisActive = activeSummon?.animalId == totem.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTotem(totem) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF3B2553) else Color(0xFF181022)
                            ),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) Color(0xFFE1BEE7) else Color(0xFF2C1C3D)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2D1B40))
                                        .border(1.dp, if (isThisActive) Color(0xFF80CBC4) else Color(0xFFB388FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(totem.iconEmoji, fontSize = 18.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${totem.name} Totem",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        if (isThisActive) {
                                            Text(
                                                text = "✨ ACTIVE",
                                                color = Color(0xFF80CBC4),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp
                                            )
                                        } else {
                                            Text(
                                                text = "x$count",
                                                color = if (count > 0) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = totem.benefitText,
                                        color = Color(0xFFCE93D8),
                                        fontSize = 9.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Quick Use Button if user has stock
                                if (count > 0) {
                                    Button(
                                        onClick = { onUseTotemImmediate(totem) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Use", color = OsrsTextYellow, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { onSelectTotem(totem) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1C3D)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Select", color = Color.LightGray, fontSize = 9.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381F4B)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = Color(0xFFE1BEE7), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
