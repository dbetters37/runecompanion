package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.DefaultItems
import com.example.data.models.EquipmentSlot
import com.example.data.models.EquipmentLoadout
import com.example.data.models.InventoryItem
import com.example.viewmodel.PetViewModel

@Composable
fun OsrsEquipmentTab(viewModel: PetViewModel) {
    val equippedItems by viewModel.equippedItems.collectAsStateWithLifecycle()
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val loadouts by viewModel.equipmentLoadouts.collectAsStateWithLifecycle()

    var selectedSlot by remember { mutableStateOf<EquipmentSlot?>(null) }
    var showLoadoutsDialog by remember { mutableStateOf(false) }

    // Calculate stats
    val totalCombatBonus = equippedItems.values.sumOf { it.combatPowerBonus }
    val totalDefBonus = equippedItems.values.sumOf { it.defPowerBonus }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Card with Stats & Action Buttons
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C241B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF8B7355), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🛡️ ${petState.customName}'s Equipment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("⚔️ Combat: +$totalCombatBonus", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("🛡️ Defence: +$totalDefBonus", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Action Buttons: Equip Strongest Gear & Loadouts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.equipStrongestGear() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD48806)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(38.dp)
                            .testTag("equip_strongest_gear_btn"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("⚡ Equip Strongest Gear", color = Color.Black, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showLoadoutsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("equipment_loadouts_btn"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("🎒 Loadouts (${loadouts.size}/5)", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Standard OSRS Equipment Grid (11 slots layout)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF382E21)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color(0xFF8B7355), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Row 1: Head
                EquipmentSlotBox(
                    slot = EquipmentSlot.HEAD,
                    equippedItem = equippedItems[EquipmentSlot.HEAD],
                    isSelected = selectedSlot == EquipmentSlot.HEAD,
                    onClick = { selectedSlot = EquipmentSlot.HEAD }
                )

                // Row 2: Cape, Amulet, Ammo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EquipmentSlotBox(
                        slot = EquipmentSlot.CAPE,
                        equippedItem = equippedItems[EquipmentSlot.CAPE],
                        isSelected = selectedSlot == EquipmentSlot.CAPE,
                        onClick = { selectedSlot = EquipmentSlot.CAPE }
                    )
                    EquipmentSlotBox(
                        slot = EquipmentSlot.AMULET,
                        equippedItem = equippedItems[EquipmentSlot.AMULET],
                        isSelected = selectedSlot == EquipmentSlot.AMULET,
                        onClick = { selectedSlot = EquipmentSlot.AMULET }
                    )
                    EquipmentSlotBox(
                        slot = EquipmentSlot.AMMO,
                        equippedItem = equippedItems[EquipmentSlot.AMMO],
                        isSelected = selectedSlot == EquipmentSlot.AMMO,
                        onClick = { selectedSlot = EquipmentSlot.AMMO }
                    )
                }

                // Row 3: Weapon, Body, Shield
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EquipmentSlotBox(
                        slot = EquipmentSlot.WEAPON,
                        equippedItem = equippedItems[EquipmentSlot.WEAPON],
                        isSelected = selectedSlot == EquipmentSlot.WEAPON,
                        onClick = { selectedSlot = EquipmentSlot.WEAPON }
                    )
                    EquipmentSlotBox(
                        slot = EquipmentSlot.BODY,
                        equippedItem = equippedItems[EquipmentSlot.BODY],
                        isSelected = selectedSlot == EquipmentSlot.BODY,
                        onClick = { selectedSlot = EquipmentSlot.BODY }
                    )
                    EquipmentSlotBox(
                        slot = EquipmentSlot.SHIELD,
                        equippedItem = equippedItems[EquipmentSlot.SHIELD],
                        isSelected = selectedSlot == EquipmentSlot.SHIELD,
                        onClick = { selectedSlot = EquipmentSlot.SHIELD }
                    )
                }

                // Row 4: Legs
                EquipmentSlotBox(
                    slot = EquipmentSlot.LEGS,
                    equippedItem = equippedItems[EquipmentSlot.LEGS],
                    isSelected = selectedSlot == EquipmentSlot.LEGS,
                    onClick = { selectedSlot = EquipmentSlot.LEGS }
                )

                // Row 5: Gloves, Boots, Ring
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EquipmentSlotBox(
                        slot = EquipmentSlot.GLOVES,
                        equippedItem = equippedItems[EquipmentSlot.GLOVES],
                        isSelected = selectedSlot == EquipmentSlot.GLOVES,
                        onClick = { selectedSlot = EquipmentSlot.GLOVES }
                    )
                    EquipmentSlotBox(
                        slot = EquipmentSlot.BOOTS,
                        equippedItem = equippedItems[EquipmentSlot.BOOTS],
                        isSelected = selectedSlot == EquipmentSlot.BOOTS,
                        onClick = { selectedSlot = EquipmentSlot.BOOTS }
                    )
                    EquipmentSlotBox(
                        slot = EquipmentSlot.RING,
                        equippedItem = equippedItems[EquipmentSlot.RING],
                        isSelected = selectedSlot == EquipmentSlot.RING,
                        onClick = { selectedSlot = EquipmentSlot.RING }
                    )
                }

                // Row 6: Woodcutting Axe Slot
                EquipmentSlotBox(
                    slot = EquipmentSlot.AXE,
                    equippedItem = equippedItems[EquipmentSlot.AXE],
                    isSelected = selectedSlot == EquipmentSlot.AXE,
                    onClick = { selectedSlot = EquipmentSlot.AXE }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Details / Action Section
        val slot = selectedSlot
        val item = if (slot != null) equippedItems[slot] else null

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C241B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF8B7355), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentSlot = selectedSlot
                if (currentSlot == null) {
                    Text(
                        text = "💡 Tap any equipment slot above to equip or unequip gear for your companion.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    if (item != null) {
                        // Currently Equipped Item Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val displayIcon = if ((item.id.contains("arrow") || item.equipmentSlot == EquipmentSlot.AMMO || item.name.contains("arrow", ignoreCase = true)) && item.iconEmoji == "🏹") "➹" else item.iconEmoji
                                Text(
                                    text = "$displayIcon ${item.name} (${currentSlot.displayName})",
                                    color = Color(0xFFFFD700),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "⚔️ Combat +${item.combatPowerBonus} | 🛡️ Def +${item.defPowerBonus}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                if (item.description.isNotBlank()) {
                                    Text(
                                        text = item.description,
                                        color = Color(0xFFCCCCCC),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.unequipItem(currentSlot)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Unequip 📦", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        // Empty Slot
                        Text(
                            text = "${currentSlot.iconSymbol} ${currentSlot.displayName} Slot: Empty",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Show Equippable Items in Backpack + Bank
                    val equippableItems = remember(inventoryItems, bankItems, currentSlot) {
                        (inventoryItems + bankItems)
                            .filter { it.equipmentSlot == currentSlot && it.quantity > 0 }
                            .groupBy { it.id }
                            .mapValues { entry ->
                                val first = entry.value.first()
                                val totalQty = entry.value.sumOf { it.quantity }
                                val inBag = inventoryItems.find { it.id == entry.key }?.quantity ?: 0
                                val inBank = bankItems.find { it.id == entry.key }?.quantity ?: 0
                                Triple(first.copy(quantity = totalQty), inBag, inBank)
                            }.values.toList()
                    }

                    if (equippableItems.isEmpty()) {
                        if (item == null) {
                            Text(
                                text = "No ${currentSlot.displayName} gear in your backpack or bank!\nBuy gear from the GE tab or earn gear from Quests.",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text(
                            text = if (item != null) "Swap with gear in Backpack / Bank:" else "Equippable items in Backpack / Bank:",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            equippableItems.forEach { (gearItem, inBag, inBank) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E1610), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF6B533E), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OsrsItemIcon(item = gearItem, fontSize = 20.sp)
                                        Column {
                                            Text(
                                                text = "${gearItem.name} (x${gearItem.quantity})",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val locationSummary = buildString {
                                                if (inBag > 0 && inBank > 0) append("🎒 $inBag in Bag | 🏦 $inBank in Bank")
                                                else if (inBag > 0) append("🎒 $inBag in Bag")
                                                else append("🏦 $inBank in Bank")
                                            }
                                            Text(
                                                text = "⚔️ +${gearItem.combatPowerBonus} | 🛡️ +${gearItem.defPowerBonus}  •  $locationSummary",
                                                color = Color(0xFFFF9800),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.equipItem(gearItem)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Equip 🛡️", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Equipment Loadouts Dialog (Up to 5 slots, save, equip, delete)
        if (showLoadoutsDialog) {
            EquipmentLoadoutsDialog(
                loadouts = loadouts,
                equippedItems = equippedItems,
                onSaveLoadout = { name -> viewModel.saveCurrentLoadout(name) },
                onEquipLoadout = { loadout -> viewModel.equipLoadout(loadout) },
                onDeleteLoadout = { id -> viewModel.deleteLoadout(id) },
                onDismiss = { showLoadoutsDialog = false }
            )
        }
    }
}

@Composable
fun EquipmentLoadoutsDialog(
    loadouts: List<EquipmentLoadout>,
    equippedItems: Map<EquipmentSlot, InventoryItem?>,
    onSaveLoadout: (String) -> Unit,
    onEquipLoadout: (EquipmentLoadout) -> Unit,
    onDeleteLoadout: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newLoadoutName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1711)),
            border = BorderStroke(2.dp, Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎒 Equipment Loadouts",
                            color = Color(0xFFFFD700),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${loadouts.size} / 5 Slots Saved",
                            color = if (loadouts.size >= 5) Color(0xFFFF6B6B) else Color(0xFF81C784),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                // Save Current Gear Form
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2C2219),
                    border = BorderStroke(1.dp, Color(0xFF8B7355)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💾 Save Current Equipped Gear (${equippedItems.values.count { it != null }} items)",
                            color = Color(0xFFFFE3A8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newLoadoutName,
                                onValueChange = { if (it.length <= 24) newLoadoutName = it },
                                placeholder = { Text("e.g. Boss Slaying Set", fontSize = 11.sp, color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("loadout_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF6B533E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1B140E),
                                    unfocusedContainerColor = Color(0xFF1B140E)
                                )
                            )

                            Button(
                                onClick = {
                                    val nameToSave = newLoadoutName.ifBlank { "Loadout ${loadouts.size + 1}" }
                                    onSaveLoadout(nameToSave)
                                    newLoadoutName = ""
                                },
                                enabled = loadouts.size < 5,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("save_loadout_btn"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text("Save 💾", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (loadouts.size >= 5) {
                            Text(
                                text = "⚠️ Maximum 5 loadouts reached. Delete an existing loadout below to save a new one.",
                                color = Color(0xFFFF8A80),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Scrollable List of Saved Loadouts
                Text(
                    text = "Saved Loadout Combinations (${loadouts.size}/5)",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (loadouts.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF241C15),
                        border = BorderStroke(1.dp, Color(0xFF4A3B2C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🛡️ No saved loadouts yet", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Equip your favorite weapons, armor, and jewelry, then type a name above to save your first loadout!",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        loadouts.forEachIndexed { index, loadout ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF2B2017),
                                border = BorderStroke(1.dp, Color(0xFF8B7355)),
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
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(Color(0xFFFFD700), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.Black,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = loadout.name,
                                                color = Color(0xFFFFD700),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    onEquipLoadout(loadout)
                                                    onDismiss()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .height(30.dp)
                                                    .testTag("equip_loadout_${loadout.id}"),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                            ) {
                                                Text("⚡ Equip", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            IconButton(
                                                onClick = { onDeleteLoadout(loadout.id) },
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .testTag("delete_loadout_${loadout.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete Loadout",
                                                    tint = Color(0xFFFF6B6B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Items preview row
                                    val itemNames = loadout.items.values.mapNotNull { itemId ->
                                        DefaultItems.getItemById(itemId)
                                    }
                                    if (itemNames.isNotEmpty()) {
                                        Text(
                                            text = itemNames.take(6).joinToString(" • ") { it.name } +
                                                    if (itemNames.size > 6) " (+${itemNames.size - 6} more)" else "",
                                            color = Color(0xFFD4A373),
                                            fontSize = 10.5.sp,
                                            maxLines = 1
                                        )
                                    } else {
                                        Text("Empty loadout", color = Color.Gray, fontSize = 10.sp)
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

@Composable
fun EquipmentSlotBox(
    slot: EquipmentSlot,
    equippedItem: InventoryItem?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (equippedItem != null) Color(0xFF4A3B2C) else Color(0xFF261E16))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF6B533E),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (equippedItem != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OsrsItemIcon(item = equippedItem, fontSize = 24.sp)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = slot.iconSymbol, fontSize = 18.sp, color = Color(0x66FFFFFF))
                Text(text = slot.displayName, fontSize = 8.sp, color = Color(0x88FFFFFF))
            }
        }
    }
}
