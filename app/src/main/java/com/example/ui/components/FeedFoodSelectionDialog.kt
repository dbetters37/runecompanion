package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.example.data.models.DefaultItems
import com.example.data.models.InventoryItem
import com.example.data.models.ItemCategory
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedFoodSelectionDialog(
    inventoryItems: List<InventoryItem>,
    bankItems: List<InventoryItem> = emptyList(),
    favoriteItemIds: Set<String> = emptySet(),
    onToggleFavorite: ((String) -> Unit)? = null,
    foodBagEatHighestFirst: Boolean = true,
    onToggleFoodBagEatOrder: () -> Unit = {},
    onFeedFromFoodBag: (InventoryItem?) -> Unit = {},
    onSelectFoodToFeed: (InventoryItem) -> Unit,
    onGetStarterBread: () -> Unit,
    onDismiss: () -> Unit
) {
    val foodItems = remember(inventoryItems, favoriteItemIds) {
        inventoryItems.filter { it.isCookedReadyToEatFood && it.quantity > 0 }
            .sortedWith { a, b ->
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
    val foodBagItems = remember(bankItems, favoriteItemIds) {
        bankItems.filter { it.isCookedReadyToEatFood && it.quantity > 0 }
            .sortedWith { a, b ->
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

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame)
            ) {
                Text("Close", color = OsrsTextYellow)
            }
        },
        title = {
            Text("🍗 FEED PET - FOOD BAG & INVENTORY", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 🎒 FOOD BAG SUMMARY CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2219)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
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
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎒", fontSize = 18.sp)
                                Column {
                                    Text("Food Bag Storage", color = OsrsGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("${foodBagItems.sumOf { it.quantity }} ready-to-eat meals", color = Color.LightGray, fontSize = 10.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (foodBagEatHighestFirst) Color(0xFF1B4D1B) else Color(0xFF4A3515),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                                modifier = Modifier.clickable { onToggleFoodBagEatOrder() }
                            ) {
                                Text(
                                    if (foodBagEatHighestFirst) "⬆️ Most HP First" else "⬇️ Least HP First",
                                    color = OsrsTextYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onFeedFromFoodBag(null)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .testTag("feed_from_food_bag_btn")
                        ) {
                            Text(
                                "🥩 Quick Feed From Food Bag (${if (foodBagEatHighestFirst) "Highest HP" else "Lowest HP"})",
                                color = OsrsTextYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "Inventory Food Items:",
                    color = OsrsParchment,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (foodItems.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🍞 No food in inventory!", color = Color.Red, fontWeight = FontWeight.Bold)
                            Text("Claim starter bread, cook raw fish, or deposit food to your Food Bag.", color = OsrsParchment, fontSize = 11.sp)

                            Button(
                                onClick = {
                                    onGetStarterBread()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("get_starter_bread_button")
                            ) {
                                Text("🍞 Get 3x Fresh Bread (Free)", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(foodItems) { food ->
                            val normFoodId = DefaultItems.normalizeItemId(food.id)
                            val isFav = favoriteItemIds.contains(normFoodId) || favoriteItemIds.contains(food.id)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            onSelectFoodToFeed(food)
                                            onDismiss()
                                        },
                                        onLongClick = {
                                            onToggleFavorite?.invoke(food.id)
                                        }
                                    )
                                    .testTag("feed_food_item_${food.id}"),
                                colors = CardDefaults.cardColors(containerColor = if (isFav) Color(0xFF382B1B) else Color(0xFF2B2018)),
                                border = androidx.compose.foundation.BorderStroke(if (isFav) 1.5.dp else 1.dp, if (isFav) Color(0xFFFFD700) else OsrsGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isFav) {
                                        Text("⭐", fontSize = 16.sp)
                                    }
                                    Text(food.iconEmoji, fontSize = 28.sp)

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${food.name} (x${food.quantity})",
                                            color = OsrsTextYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Restores +${food.restoreHunger} Hunger • +${food.healHp} HP",
                                            color = Color(0xFF70E000),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            onSelectFoodToFeed(food)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Feed", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = OsrsLeatherDark
    )
}
