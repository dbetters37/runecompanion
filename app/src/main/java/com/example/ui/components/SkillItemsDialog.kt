package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.EquipmentSlot
import com.example.data.models.InventoryItem
import com.example.data.models.OsrsSkill
import com.example.data.models.SkillItemRegistry
import com.example.ui.theme.*

private enum class ItemFilter { ALL, OWNED, MISSING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillItemsDialog(
    skill: OsrsSkill,
    inventoryItems: List<InventoryItem>,
    bankItems: List<InventoryItem>,
    equippedItems: Map<EquipmentSlot, InventoryItem>,
    onDismissRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ItemFilter.ALL) }

    val allSkillEntries = remember(skill, inventoryItems, bankItems, equippedItems) {
        SkillItemRegistry.getItemsForSkill(
            skill = skill,
            inventoryItems = inventoryItems,
            bankItems = bankItems,
            equippedItems = equippedItems
        )
    }

    val ownedCount = remember(allSkillEntries) { allSkillEntries.count { it.isOwned } }
    val totalCount = remember(allSkillEntries) { allSkillEntries.size }
    val totalQuantitySum = remember(allSkillEntries) { allSkillEntries.sumOf { it.totalOwned } }

    val filteredEntries = remember(allSkillEntries, searchQuery, selectedFilter) {
        allSkillEntries.filter { entry ->
            val matchesSearch = searchQuery.isBlank() ||
                    entry.item.name.contains(searchQuery, ignoreCase = true) ||
                    entry.item.description.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                ItemFilter.ALL -> true
                ItemFilter.OWNED -> entry.isOwned
                ItemFilter.MISSING -> !entry.isOwned
            }

            matchesSearch && matchesFilter
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp)
                .testTag("dialog_skill_items_${skill.name.lowercase()}"),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E140D),
            border = BorderStroke(2.dp, OsrsGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // --- HEADER ---
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
                            color = skill.accentColor.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, skill.accentColor),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(skill.iconSymbol, fontSize = 20.sp)
                            }
                        }

                        Column {
                            Text(
                                text = "${skill.displayName} Collection",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OsrsTextYellow
                            )
                            Text(
                                text = "Item Inventory & Possession Tracker",
                                fontSize = 11.sp,
                                color = OsrsParchment
                            )
                        }
                    }

                    // Close Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF3E1A1A),
                        border = BorderStroke(1.dp, Color(0xFFE57373)),
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { onDismissRequest() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✕", color = Color(0xFFEF5350), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- SUMMARY CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1F17)),
                    border = BorderStroke(1.dp, Color(0xFF5A412A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Items Collected: $ownedCount / $totalCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OsrsTextYellow
                            )
                            Text(
                                text = "Total Items in Possession: ${"%,d".format(totalQuantitySum)}",
                                fontSize = 11.sp,
                                color = OsrsParchment
                            )
                        }

                        val percent = if (totalCount > 0) (ownedCount * 100) / totalCount else 0
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (percent == 100) Color(0xFF1B5E20) else Color(0xFF382A1D),
                            border = BorderStroke(1.dp, if (percent == 100) Color(0xFF81C784) else OsrsGold)
                        ) {
                            Text(
                                text = "$percent% Complete",
                                color = if (percent == 100) Color(0xFF81C784) else OsrsGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // --- SEARCH & FILTER BAR ---
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search ${skill.displayName} items...", color = Color.Gray, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF140D08),
                            unfocusedContainerColor = Color(0xFF140D08),
                            focusedBorderColor = OsrsGold,
                            unfocusedBorderColor = Color(0xFF5A412A),
                            focusedTextColor = OsrsTextWhite,
                            unfocusedTextColor = OsrsTextWhite
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    // Filter Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ItemFilter.entries.forEach { filter ->
                            val isSelected = filter == selectedFilter
                            val count = when (filter) {
                                ItemFilter.ALL -> totalCount
                                ItemFilter.OWNED -> ownedCount
                                ItemFilter.MISSING -> totalCount - ownedCount
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFF3B2A1D) else Color(0xFF19100A),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) OsrsGold else Color(0xFF3E2B1E)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFilter = filter }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${filter.name.lowercase().replaceFirstChar { it.uppercase() }} ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) OsrsTextYellow else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // --- SCROLLABLE ITEM LIST ---
                if (filteredEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items match your filter.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredEntries, key = { it.item.id }) { entry ->
                            SkillItemRow(entry = entry)
                        }
                    }
                }

                // --- FOOTER DONE BUTTON ---
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                    border = BorderStroke(1.dp, OsrsGold)
                ) {
                    Text(
                        text = "CLOSE TRACKER",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillItemRow(entry: SkillItemRegistry.SkillItemEntry) {
    val isOwned = entry.isOwned

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isOwned) Color(0xFF231A12) else Color(0xFF1A120B),
        border = BorderStroke(
            width = if (isOwned) 1.dp else 0.5.dp,
            color = if (isOwned) OsrsGold.copy(alpha = 0.8f) else Color(0xFF3E2B1E)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon + Badge
            Box {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isOwned) Color(0xFF3B2A1D) else Color(0xFF140D08),
                    border = BorderStroke(1.dp, if (isOwned) OsrsGold else Color(0xFF3A281B)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = entry.item.iconEmoji,
                            fontSize = 20.sp,
                            color = if (isOwned) Color.Unspecified else Color.Gray.copy(alpha = 0.4f)
                        )
                    }
                }

                // Owned quantity badge on bottom-right of icon
                if (isOwned) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1B5E20),
                        border = BorderStroke(0.5.dp, Color(0xFF81C784)),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = if (entry.totalOwned > 999) "${entry.totalOwned / 1000}k" else "${entry.totalOwned}",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Info details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = entry.item.name,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOwned) OsrsTextYellow else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = entry.item.description,
                    fontSize = 10.sp,
                    color = if (isOwned) OsrsParchment else Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Inventory / Bank / Equipment breakdown pill
                if (isOwned) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (entry.inventoryQty > 0) {
                            Text("🎒 Inv: ${entry.inventoryQty}", fontSize = 9.sp, color = Color(0xFF81C784))
                        }
                        if (entry.bankQty > 0) {
                            Text("🏦 Bank: ${entry.bankQty}", fontSize = 9.sp, color = Color(0xFF64B5F6))
                        }
                        if (entry.equippedQty > 0) {
                            Text("⚔️ Eqp: ${entry.equippedQty}", fontSize = 9.sp, color = Color(0xFFFFB74D))
                        }
                    }
                }
            }

            // Status Badge on Right
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isOwned) Color(0xFF1B5E20) else Color(0xFF2A1C1A),
                border = BorderStroke(1.dp, if (isOwned) Color(0xFF81C784) else Color(0xFF5A3E3E))
            ) {
                Text(
                    text = if (isOwned) "x${"%,d".format(entry.totalOwned)}" else "0 Owned",
                    color = if (isOwned) Color(0xFF81C784) else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
