package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.components.StoneMasonryPanel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PohHouseTab(
    pohState: PohHouseState,
    petState: PetState? = null,
    inventoryItems: List<InventoryItem> = emptyList(),
    bankItems: List<InventoryItem> = emptyList(),
    constructionXp: Long,
    coinsGp: Long,
    unlockedPets: List<PetType>,
    isAfkCampfireActive: Boolean = false,
    isAfkCookingActive: Boolean = false,
    isAfkFishingActive: Boolean = false,
    isAfkMiningActive: Boolean = false,
    isAfkSmeltingActive: Boolean = false,
    isAfkSawmillActive: Boolean = false,
    isAfkWoodcuttingActive: Boolean = false,
    isAfkNailCraftingActive: Boolean = false,
    isAfkStickCraftingActive: Boolean = false,
    isAfkArrowtipCraftingActive: Boolean = false,
    isAfkFletchingActive: Boolean = false,
    isAfkBoneBuryingActive: Boolean = false,
    isAfkSailingActive: Boolean = false,
    isAfkRunecraftingActive: Boolean = false,
    selectedRuneId: String = "item_rune_air",
    isAfkTrapCraftingActive: Boolean = false,
    selectedCraftingTrapId: String = "item_bird_snare",
    selectedFishId: String? = null,
    selectedFoodId: String? = null,
    selectedOreId: String? = null,
    selectedBarId: String? = null,
    selectedTreeId: String? = null,
    onToggleAfkCampfire: () -> Unit = {},
    onToggleAfkCooking: () -> Unit = {},
    onToggleAfkFishing: () -> Unit = {},
    onToggleAfkMining: () -> Unit = {},
    onToggleAfkSmelting: () -> Unit = {},
    onToggleAfkSawmill: () -> Unit = {},
    onToggleAfkWoodcutting: () -> Unit = {},
    onToggleAfkNailCrafting: () -> Unit = {},
    onToggleAfkStickCrafting: () -> Unit = {},
    onToggleAfkArrowtipCrafting: () -> Unit = {},
    onToggleAfkFletching: () -> Unit = {},
    onToggleAfkBoneBurying: () -> Unit = {},
    onToggleAfkSailing: () -> Unit = {},
    onToggleAfkRunecrafting: (String?) -> Unit = {},
    onToggleAfkTrapCrafting: () -> Unit = {},
    onSelectCraftingTrapId: (String) -> Unit = {},
    onSelectFishId: (String?) -> Unit = {},
    onSelectFoodId: (String?) -> Unit = {},
    onSelectOreId: (String?) -> Unit = {},
    onSelectBarId: (String?) -> Unit = {},
    onSelectTreeId: (String?) -> Unit = {},
    onBurnLogsAtCampfire: () -> Unit = {},
    onCookAtRange: () -> Unit = {},
    onCookSpecificFood: (String) -> Unit = {},
    onFishAtPohPond: () -> Unit = {},
    onFishSpecificFish: (String) -> Unit = {},
    onMineAtQuarry: () -> Unit = {},
    onMineSpecificOre: (String) -> Unit = {},
    onSmeltAtFurnace: () -> Unit = {},
    onSmeltSpecificBar: (String) -> Unit = {},
    onConvertLogsAtSawmill: () -> Unit = {},
    onChopTrees: () -> Unit = {},
    onChopSpecificTree: (String) -> Unit = {},
    onCraftNailsAtAnvil: () -> Unit = {},
    onCraftSticks: () -> Unit = {},
    onCraftArrowtips: () -> Unit = {},
    onFletchArrows: () -> Unit = {},
    onCraftTrap: (String) -> Unit = {},
    onForgeArmor: (String) -> Unit = {},
    onForgeEquipment: (String) -> Unit = {},
    onBuryBones: () -> Unit = {},
    onBuildRoom: (PohRoomType, Int?) -> Unit,
    onDemolishRoom: (BuiltRoom) -> Unit = {},
    onBuildFurniture: (BuiltRoom, PohFurnitureItem) -> Unit,
    onDestroyFurniture: (BuiltRoom, String) -> Unit = { _, _ -> },
    onBuyGeMaterial: (GeMaterial, Int) -> Unit,
    onExpandGrid: (Long) -> Unit = {},
    onUpdateWallsAndFloor: (BuiltRoom, PohWallType, PohWallType, PohWallType, PohWallType, PohFloorType) -> Unit = { _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val constructionLevel = OsrsXpCalculator.getLevelForXp(constructionXp)
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = House Rooms, 1 = GE Material Shop, 2 = Menagerie Pets
    var showRuneSelectorModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // POH Construction Header Card
        StoneMasonryPanel(
            modifier = Modifier.fillMaxWidth(),
            accentIcon = "🏡",
            borderColor = OsrsGold,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Row 1: Title and Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🏡", fontSize = 18.sp)
                    Column {
                        Text(
                            text = "Player-Owned House",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Level $constructionLevel Construction • ${pohState.builtRooms.size} Rooms",
                            color = OsrsParchment,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 GP Balance: ${"%,d".format(coinsGp)} GP",
                    color = OsrsGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                )
                Text(
                    text = "Build rooms & habitats",
                    color = Color.LightGray,
                    fontSize = 10.5.sp
                )
            }
        }

        // Sub Navigation: [🏡 3x3 House] [🐾 Menagerie / Pets]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { selectedSubTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSubTab == 0) OsrsRedFrame else OsrsLeatherMedium
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).testTag("subtab_poh_house")
            ) {
                Text("🏡 3x3 House", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            Button(
                onClick = { selectedSubTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSubTab == 1) OsrsRedFrame else OsrsLeatherMedium
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).testTag("subtab_poh_menagerie")
            ) {
                Text("🐾 Pets & Menagerie", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        // SubTab Content
        when (selectedSubTab) {
            0 -> PohRoomsView(
                pohState = pohState,
                petState = petState,
                inventoryItems = inventoryItems,
                bankItems = bankItems,
                constructionLevel = constructionLevel,
                coinsGp = coinsGp,
                onBuildRoom = onBuildRoom,
                onDemolishRoom = onDemolishRoom,
                onBuildFurniture = onBuildFurniture,
                onDestroyFurniture = onDestroyFurniture,
                onExpandGrid = onExpandGrid,
                onUpdateWallsAndFloor = onUpdateWallsAndFloor
            )
            1 -> MenagerieView(
                unlockedPets = unlockedPets
            )
        }
    }
}

@Composable
private fun PohRoomsView(
    pohState: PohHouseState,
    petState: PetState? = null,
    inventoryItems: List<InventoryItem> = emptyList(),
    bankItems: List<InventoryItem> = emptyList(),
    constructionLevel: Int,
    coinsGp: Long,
    onBuildRoom: (PohRoomType, Int?) -> Unit,
    onDemolishRoom: (BuiltRoom) -> Unit,
    onBuildFurniture: (BuiltRoom, PohFurnitureItem) -> Unit,
    onDestroyFurniture: (BuiltRoom, String) -> Unit = { _, _ -> },
    onExpandGrid: (Long) -> Unit = {},
    onUpdateWallsAndFloor: (BuiltRoom, PohWallType, PohWallType, PohWallType, PohWallType, PohFloorType) -> Unit = { _, _, _, _, _, _ -> }
) {
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showExpandGridDialog by remember { mutableStateOf(false) }
    var roomPendingDelete by remember { mutableStateOf<BuiltRoom?>(null) }
    var targetSlotForBuild by remember { mutableStateOf<Int?>(null) }
    var inspectingRoom by remember { mutableStateOf<BuiltRoom?>(null) }
    val scrollState = rememberScrollState()
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    val haptic = LocalHapticFeedback.current

    val gridDimension = com.example.data.models.getPohGridDimension(constructionLevel, pohState.extraGridSize)
    val maxRooms = gridDimension * gridDimension
    val nextUpgradeLevel = ((gridDimension - pohState.extraGridSize - 2) * 20) + 1
    val expansionCostGp = 25000L * (pohState.extraGridSize + 1) * (pohState.extraGridSize + 1)

    // 60fps continuous infinite animation for companion pet in house
    val infiniteTransition = rememberInfiniteTransition(label = "poh_blueprint_pet_anim")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "house_pet_bounce"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 🏰 RUNESCAPE POH BLUEPRINT MAP (PROMINENT AT TOP)
        StoneMasonryPanel(
            modifier = Modifier.fillMaxWidth(),
            accentIcon = "🏰",
            borderColor = OsrsGold,
            contentPadding = PaddingValues(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val mapTitle = "🏰 ${gridDimension}x${gridDimension} POH HOUSE BLUEPRINT"
                        val mapSubtitle = if (pohState.extraGridSize > 0) {
                            "Grid ${gridDimension}x${gridDimension} (+${pohState.extraGridSize} GP Expansions) • (${pohState.builtRooms.size}/$maxRooms Rooms)"
                        } else if (gridDimension >= 7 || nextUpgradeLevel > 99) {
                            "Master Level 99 Grid • Max Capacity (${pohState.builtRooms.size}/$maxRooms Rooms)"
                        } else {
                            "Level $constructionLevel Grid • (${pohState.builtRooms.size}/$maxRooms Rooms) • Level $nextUpgradeLevel adds base grid"
                        }
                        Text(
                            text = mapTitle,
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = mapSubtitle,
                            color = OsrsParchment,
                            fontSize = 10.sp
                        )
                    }
                    Button(
                        onClick = {
                            targetSlotForBuild = null
                            showAddRoomDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("blueprint_add_room_button")
                    ) {
                        Text("➕ Add Room", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 🔍 ZOOM CONTROLS & ➕ EXPAND GRID ROW/COL FOR GP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔍 Zoom:", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                zoomLevel = (zoomLevel - 0.15f).coerceAtLeast(0.4f)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("zoom_out_button")
                        ) {
                            Text("➖", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                zoomLevel = 1.0f
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2218)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("zoom_reset_button")
                        ) {
                            Text("${(zoomLevel * 100).toInt()}%", color = OsrsParchment, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                zoomLevel = (zoomLevel + 0.15f).coerceAtMost(2.5f)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("zoom_in_button")
                        ) {
                            Text("➕", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // GP Expansion Button
                    val canAffordExpand = coinsGp >= expansionCostGp
                    Button(
                        onClick = {
                            showExpandGridDialog = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAffordExpand) Color(0xFF1B5E20) else Color(0xFF3E2D23)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.testTag("expand_grid_button")
                    ) {
                        Text(
                            text = "➕ +1 Row&Col (${NumberFormat.getNumberInstance(Locale.US).format(expansionCostGp)} GP)",
                            color = if (canAffordExpand) OsrsTextYellow else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Zoom Preset Chips & Touch Gesture Guidance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Pinch grid to zoom • Drag to pan",
                        color = Color(0xFF81D4FA),
                        fontSize = 9.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf(0.5f, 0.75f, 1.0f, 1.5f, 2.0f).forEach { preset ->
                            val isSelected = (zoomLevel - preset).let { if (it < 0) -it else it } < 0.08f
                            Surface(
                                modifier = Modifier.clickable {
                                    zoomLevel = preset
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                shape = RoundedCornerShape(3.dp),
                                color = if (isSelected) OsrsGold else Color(0xFF2C1F16),
                                border = BorderStroke(0.5.dp, if (isSelected) OsrsGoldBright else Color(0xFF4E3629))
                            ) {
                                Text(
                                    text = "${(preset * 100).toInt()}%",
                                    color = if (isSelected) Color.Black else OsrsParchment,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Dynamic Blueprint Grid Layout with Pinch-to-Zoom and 2D panning/scrolling
                val horizontalGridScroll = rememberScrollState()
                val verticalGridScroll = rememberScrollState()
                val baseTileDimension = if (gridDimension <= 3) 84.dp else if (gridDimension <= 5) 72.dp else 60.dp
                val scaledTileSize = (baseTileDimension * zoomLevel).coerceIn(36.dp, 180.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 260.dp, max = 400.dp)
                        .background(Color(0xFF0F0B07), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF3A2B1D), RoundedCornerShape(6.dp))
                        .padding(4.dp)
                        .pointerInput(Unit) {
                            detectTransformGestures(panZoomLock = false) { _, _, zoom, _ ->
                                if (zoom != 1f) {
                                    zoomLevel = (zoomLevel * zoom).coerceIn(0.4f, 2.5f)
                                }
                            }
                        }
                        .horizontalScroll(horizontalGridScroll)
                        .verticalScroll(verticalGridScroll)
                ) {
                    Column(
                        modifier = Modifier.wrapContentSize(),
                        verticalArrangement = Arrangement.spacedBy((4 * zoomLevel).dp.coerceAtLeast(2.dp))
                    ) {
                        for (rowIndex in 0 until gridDimension) {
                            Row(
                                modifier = Modifier.wrapContentSize(),
                                horizontalArrangement = Arrangement.spacedBy((4 * zoomLevel).dp.coerceAtLeast(2.dp))
                            ) {
                                for (colIndex in 0 until gridDimension) {
                                    val slotIndex = rowIndex * gridDimension + colIndex
                                    val roomAtSlot = pohState.builtRooms.find { it.gridPosition == slotIndex }

                                    Box(
                                        modifier = Modifier.size(scaledTileSize)
                                    ) {
                                        if (roomAtSlot != null) {
                                            VisualRoomTile(
                                                room = roomAtSlot,
                                                petState = petState,
                                                bounceOffset = bounceY,
                                                gridDimension = gridDimension,
                                                zoomScale = zoomLevel,
                                                isCentralPetRoom = roomAtSlot.gridPosition == (maxRooms / 2) || roomAtSlot.roomType == PohRoomType.PARLOUR || roomAtSlot.roomType == PohRoomType.MENAGERIE,
                                                onClick = { inspectingRoom = roomAtSlot }
                                            )
                                        } else {
                                            VisualEmptySlotTile(
                                                slotIndex = slotIndex,
                                                gridDimension = gridDimension,
                                                zoomScale = zoomLevel,
                                                onClick = {
                                                    targetSlotForBuild = slotIndex
                                                    showAddRoomDialog = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏠 HOUSE ROOMS & FURNITURE LIST",
                color = OsrsGold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Button(
                onClick = {
                    targetSlotForBuild = null
                    showAddRoomDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("add_room_button")
            ) {
                Text("➕ Add Room", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (pohState.builtRooms.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
                border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🏡 No Rooms Built Yet!", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Tap any slot in the blueprint map above or tap 'Add Room' to construct your first room.", color = OsrsParchment, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            // Built Rooms Cards with Scrollable Furniture & Delete Option
            pohState.builtRooms.forEach { room ->
                BuiltRoomCard(
                    room = room,
                    pohState = pohState,
                    constructionLevel = constructionLevel,
                    inventoryItems = inventoryItems,
                    bankItems = bankItems,
                    gridDimension = gridDimension,
                    onInspectRoom = { inspectingRoom = room },
                    onDeleteRoom = {
                        roomPendingDelete = room
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onBuildFurniture = { furn -> onBuildFurniture(room, furn) },
                    onDestroyFurniture = { furnId -> onDestroyFurniture(room, furnId) }
                )
            }
        }
    }

    // Room Inspector Dialog Overlay - resolves current room state dynamically
    val activeInspectingRoom = inspectingRoom?.let { initial ->
        pohState.builtRooms.find { it.id == initial.id }
    }

    activeInspectingRoom?.let { room ->
        RoomInspectorDialog(
            room = room,
            pohState = pohState,
            constructionLevel = constructionLevel,
            inventoryItems = inventoryItems,
            bankItems = bankItems,
            gridDimension = gridDimension,
            onBuildFurniture = { furn -> onBuildFurniture(room, furn) },
            onDestroyFurniture = { furnId -> onDestroyFurniture(room, furnId) },
            onApplyWallsAndFloor = { wn: PohWallType, we: PohWallType, ws: PohWallType, ww: PohWallType, fl: PohFloorType ->
                onUpdateWallsAndFloor.invoke(room, wn, we, ws, ww, fl)
            },
            onDemolishRoom = { roomToDemolish ->
                inspectingRoom = null
                roomPendingDelete = roomToDemolish
            },
            onDismiss = { inspectingRoom = null }
        )
    }

    // Dedicated Room Deletion Confirmation Dialog
    roomPendingDelete?.let { room ->
        DeleteRoomConfirmDialog(
            room = room,
            gridDimension = gridDimension,
            onConfirmDelete = {
                onDemolishRoom(room)
                roomPendingDelete = null
                if (inspectingRoom?.id == room.id) {
                    inspectingRoom = null
                }
            },
            onDismiss = { roomPendingDelete = null }
        )
    }

    if (showAddRoomDialog) {
        val dialogTitle = if (targetSlotForBuild != null) {
            val r = targetSlotForBuild!! / gridDimension + 1
            val c = targetSlotForBuild!! % gridDimension + 1
            "🏠 Build Room at Grid (Row $r, Col $c)"
        } else {
            "🏠 Build New OSRS Room"
        }

        AlertDialog(
            onDismissRequest = {
                showAddRoomDialog = false
                targetSlotForBuild = null
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddRoomDialog = false
                    targetSlotForBuild = null
                }) {
                    Text("Close", color = OsrsTextYellow)
                }
            },
            title = { Text(dialogTitle, color = OsrsTextYellow) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(PohRoomType.entries) { roomType ->
                        val canBuild = constructionLevel >= roomType.reqLevel
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canBuild) {
                                    onBuildRoom(roomType, targetSlotForBuild)
                                    showAddRoomDialog = false
                                    targetSlotForBuild = null
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (canBuild) Color(0xFF2B2018) else Color(0xFF1E1610)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (canBuild) OsrsGold else Color.Gray
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(roomType.iconEmoji, fontSize = 22.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${roomType.displayName} (Lvl ${roomType.reqLevel})",
                                        color = if (canBuild) OsrsTextYellow else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Cost: ${roomType.buildCostGp} GP",
                                        color = OsrsGold,
                                        fontSize = 11.sp
                                    )
                                }
                                if (canBuild) {
                                    Text("➕ Build", color = Color(0xFF70E000), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("🔒 Lvl ${roomType.reqLevel}", color = Color.Red, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            containerColor = OsrsLeatherDark
        )
    }

    if (showExpandGridDialog) {
        val canAfford = coinsGp >= expansionCostGp
        val nextDim = gridDimension + 1
        val nextMaxRooms = nextDim * nextDim

        AlertDialog(
            onDismissRequest = { showExpandGridDialog = false },
            title = {
                Text(
                    text = "🏰 Expand Estate Grid (+1 Row & Col)",
                    color = OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Expand your Player Owned House estate layout. Adding rows and columns increases maximum room capacity and provides more construction possibilities!",
                        color = OsrsTextWhite,
                        fontSize = 11.sp
                    )

                    Surface(
                        color = Color(0xFF22170E),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, OsrsGold)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Current Grid:", color = OsrsParchment, fontSize = 11.sp)
                                Text("${gridDimension}x${gridDimension} ($maxRooms Rooms Max)", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expanded Estate:", color = OsrsGoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${nextDim}x${nextDim} ($nextMaxRooms Rooms Max)", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("New Room Slots:", color = Color(0xFF70E000), fontSize = 11.sp)
                                Text("+${nextMaxRooms - maxRooms} Slots", color = Color(0xFF70E000), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Divider(color = Color(0xFF4A3828))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expansion Cost:", color = OsrsGold, fontSize = 11.sp)
                                Text("${NumberFormat.getNumberInstance(Locale.US).format(expansionCostGp)} GP", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Your Balance:", color = OsrsParchment, fontSize = 11.sp)
                                Text(
                                    "${NumberFormat.getNumberInstance(Locale.US).format(coinsGp)} GP",
                                    color = if (canAfford) Color(0xFF70E000) else Color(0xFFFF5252),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "✓ All existing built rooms and furniture are preserved at their exact coordinates.",
                        color = Color(0xFF81D4FA),
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onExpandGrid(expansionCostGp)
                        showExpandGridDialog = false
                    },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) Color(0xFF1B5E20) else Color(0xFF3E2D23)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.testTag("confirm_expand_grid_button")
                ) {
                    Text(
                        text = if (canAfford) "Confirm (${NumberFormat.getNumberInstance(Locale.US).format(expansionCostGp)} GP)" else "Not Enough GP",
                        color = if (canAfford) OsrsTextYellow else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpandGridDialog = false }) {
                    Text("Cancel", color = OsrsParchment, fontSize = 11.sp)
                }
            },
            containerColor = OsrsLeatherDark
        )
    }
}

@Composable
private fun PohAfkStationsView(
    pohState: com.example.data.models.PohHouseState,
    isAfkCampfireActive: Boolean,
    isAfkCookingActive: Boolean,
    isAfkFishingActive: Boolean,
    isAfkMiningActive: Boolean,
    isAfkSmeltingActive: Boolean,
    isAfkSawmillActive: Boolean,
    isAfkWoodcuttingActive: Boolean = false,
    isAfkNailCraftingActive: Boolean,
    isAfkStickCraftingActive: Boolean = false,
    isAfkArrowtipCraftingActive: Boolean = false,
    isAfkFletchingActive: Boolean = false,
    isAfkBoneBuryingActive: Boolean = false,
    isAfkSailingActive: Boolean = false,
    isAfkRunecraftingActive: Boolean = false,
    selectedRuneId: String = "item_rune_air",
    isAfkTrapCraftingActive: Boolean = false,
    selectedCraftingTrapId: String = "item_bird_snare",
    selectedFishId: String? = null,
    selectedFoodId: String? = null,
    selectedOreId: String? = null,
    selectedBarId: String? = null,
    selectedTreeId: String? = null,
    onToggleAfkCampfire: () -> Unit,
    onToggleAfkCooking: () -> Unit,
    onToggleAfkFishing: () -> Unit,
    onToggleAfkMining: () -> Unit,
    onToggleAfkSmelting: () -> Unit,
    onToggleAfkSawmill: () -> Unit,
    onToggleAfkWoodcutting: () -> Unit = {},
    onToggleAfkNailCrafting: () -> Unit,
    onToggleAfkStickCrafting: () -> Unit = {},
    onToggleAfkArrowtipCrafting: () -> Unit = {},
    onToggleAfkFletching: () -> Unit = {},
    onToggleAfkBoneBurying: () -> Unit = {},
    onToggleAfkSailing: () -> Unit = {},
    onToggleAfkRunecrafting: (String?) -> Unit = {},
    onToggleAfkTrapCrafting: () -> Unit = {},
    onSelectCraftingTrapId: (String) -> Unit = {},
    onSelectFishId: (String?) -> Unit = {},
    onSelectFoodId: (String?) -> Unit = {},
    onSelectOreId: (String?) -> Unit = {},
    onSelectBarId: (String?) -> Unit = {},
    onSelectTreeId: (String?) -> Unit = {},
    onBurnLogsAtCampfire: () -> Unit,
    onCookAtRange: () -> Unit,
    onCookSpecificFood: (String) -> Unit = {},
    onFishAtPohPond: () -> Unit,
    onFishSpecificFish: (String) -> Unit = {},
    onMineAtQuarry: () -> Unit,
    onMineSpecificOre: (String) -> Unit = {},
    onSmeltAtFurnace: () -> Unit,
    onSmeltSpecificBar: (String) -> Unit = {},
    onConvertLogsAtSawmill: () -> Unit,
    onChopTrees: () -> Unit = {},
    onChopSpecificTree: (String) -> Unit = {},
    onCraftNailsAtAnvil: () -> Unit,
    onCraftSticks: () -> Unit = {},
    onCraftArrowtips: () -> Unit = {},
    onFletchArrows: () -> Unit = {},
    onCraftTrap: (String) -> Unit = {},
    onForgeArmor: (String) -> Unit = {},
    onForgeEquipment: (String) -> Unit = {},
    onBuryBones: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showRuneSelectorModal by remember { mutableStateOf(false) }

    val builtFurnitureIdsAll = remember(pohState.builtRooms) {
        pohState.builtRooms.flatMap { it.builtFurnitureIds }
    }
    val hasCampfire = remember(builtFurnitureIdsAll) {
        builtFurnitureIdsAll.any { id ->
            id.contains("campfire", ignoreCase = true) ||
            id.contains("fireplace", ignoreCase = true) ||
            id.contains("hearth", ignoreCase = true) ||
            id.contains("fire", ignoreCase = true)
        }
    }
    val hasKitchenRange = remember(builtFurnitureIdsAll) {
        builtFurnitureIdsAll.any { id ->
            id.contains("range", ignoreCase = true) ||
            id.contains("cooking", ignoreCase = true) ||
            id.contains("stove", ignoreCase = true)
        }
    }
    val hasCraftingStation = remember(builtFurnitureIdsAll) {
        builtFurnitureIdsAll.any { id ->
            id.contains("workbench", ignoreCase = true) ||
            id.contains("crafting", ignoreCase = true) ||
            id.contains("tool_rack", ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // AFK House Facilities Card (Campfire, Cooking Range, Fishing Pond, Mining Quarry, Smelting Furnace, Sawmill)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🔥 🍳 🎣 ⛏️ 🪚 HOUSE AFK SKILL STATIONS",
                    color = OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "⚡ Single AFK Rule: Only 1 AFK activity can run at a time! Enabling one automatically pauses others.",
                    color = OsrsGold,
                    fontSize = 10.sp
                )

                // 1. Campfire Station (Firemaking XP)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🔥 Campfire (Burn Logs)", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        if (hasCampfire) {
                            Text("Burns inventory logs for Summoning XP while AFK!", color = OsrsParchment, fontSize = 10.sp)
                        } else {
                            Text("🔒 Locked: Build a Campfire/Fireplace in your POH (Parlour or Garden) to unlock!", color = Color(0xFFFF8A80), fontSize = 10.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = onBurnLogsAtCampfire,
                            enabled = hasCampfire,
                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Burn 1x", color = OsrsTextYellow, fontSize = 10.sp)
                        }
                        Button(
                            onClick = onToggleAfkCampfire,
                            enabled = hasCampfire,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAfkCampfireActive) Color(0xFF2E6B38) else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (isAfkCampfireActive) "⚡ AFK ON" else if (hasCampfire) "AFK OFF" else "🔒 Locked", color = OsrsTextWhite, fontSize = 10.sp)
                        }
                    }
                }

                Divider(color = Color(0xFF4A3828))

                // 2. Cooking Range (Cooking Raw Food with OSRS Burn Chance)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🍳 Kitchen Range (Cook Raw Food)", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            if (hasKitchenRange) {
                                Text("Cooks raw food! Select a target food for AFK cooking:", color = OsrsParchment, fontSize = 10.sp)
                            } else {
                                Text("🔒 Locked: Build a Cooking Range in your POH Kitchen to unlock!", color = Color(0xFFFF8A80), fontSize = 10.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = onCookAtRange,
                                enabled = hasKitchenRange,
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Cook 1x", color = OsrsTextYellow, fontSize = 10.sp)
                            }
                            Button(
                                onClick = onToggleAfkCooking,
                                enabled = hasKitchenRange,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAfkCookingActive) Color(0xFF2E6B38) else Color.DarkGray
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(if (isAfkCookingActive) "⚡ AFK ON" else if (hasKitchenRange) "AFK OFF" else "🔒 Locked", color = OsrsTextWhite, fontSize = 10.sp)
                            }
                        }
                    }

                    // Specific Cooking Selectors
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val foods = listOf(
                            "item_raw_shrimps" to "Shrimps 🦐",
                            "item_raw_trout" to "Trout 🐟",
                            "item_raw_salmon" to "Salmon 🐟",
                            "item_raw_lobster" to "Lobster 🦞",
                            "item_raw_swordfish" to "Swordfish 🗡️",
                            "item_raw_shark" to "Shark 🦈"
                        )
                        foods.forEach { (foodId, label) ->
                            val isSelected = selectedFoodId == foodId
                            Button(
                                onClick = {
                                    onSelectFoodId(foodId)
                                    onCookSpecificFood(foodId)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF2E6B38) else Color(0xFF2B2118)
                                ),
                                border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color(0xFF5A4433)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(label, fontSize = 9.sp, color = if (isSelected) Color.White else OsrsTextYellow, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                Divider(color = Color(0xFF4A3828))



                // 6. Sawmill Area (Convert Logs -> Planks + Crafting XP)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🪚 Sawmill Area (Crafting XP)", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Converts 1 log to 1 plank for Crafting XP (+120 / +250 XP). Stops automatically when out of logs!", color = OsrsParchment, fontSize = 9.5.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = onConvertLogsAtSawmill,
                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Saw 1x", color = OsrsTextYellow, fontSize = 10.sp)
                        }
                        Button(
                            onClick = onToggleAfkSawmill,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAfkSawmillActive) Color(0xFF2E6B38) else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (isAfkSawmillActive) "⚡ AFK ON" else "AFK OFF", color = OsrsTextWhite, fontSize = 10.sp)
                        }
                    }
                }

                Divider(color = Color(0xFF4A3828))

                // Woodcutting Grove (Chop Trees & Tree Selection)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🌳 Harvesting Grove (Chop Trees)", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Select tree type below to collect specific logs! Higher tier axes chop logs faster.", color = OsrsParchment, fontSize = 9.5.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = onChopTrees,
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Chop 1x", color = OsrsTextYellow, fontSize = 10.sp)
                            }
                            Button(
                                onClick = onToggleAfkWoodcutting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAfkWoodcuttingActive) Color(0xFF2E6B38) else Color.DarkGray
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(if (isAfkWoodcuttingActive) "⚡ AFK ON" else "AFK OFF", color = OsrsTextWhite, fontSize = 10.sp)
                            }
                        }
                    }

                    // Tree Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val treeList = listOf(
                            "item_logs" to "Normal 🌳",
                            "item_birch_logs" to "Birch (L10)",
                            "item_oak_logs" to "Oak (L15)",
                            "item_pine_logs" to "Pine (L25)",
                            "item_willow_logs" to "Willow (L30)",
                            "item_cedar_logs" to "Cedar (L40)",
                            "item_maple_logs" to "Maple (L45)",
                            "item_yew_logs" to "Yew (L60)",
                            "item_ironwood_logs" to "Ironwood (L75)",
                            "item_redwood_logs" to "Redwood (L90)"
                        )
                        treeList.forEach { (treeId, label) ->
                            val isSelected = selectedTreeId == treeId
                            Button(
                                onClick = {
                                    onSelectTreeId(treeId)
                                    onChopSpecificTree(treeId)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF2E6B38) else Color(0xFF1E1E1E)
                                ),
                                border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color(0xFF5A4433)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    label,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White else OsrsTextYellow,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Divider(color = Color(0xFF4A3828))

                // Workshop Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231C16)),
                    border = BorderStroke(1.dp, OsrsGold)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🛠️", fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Workshop & Crafting Stations", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Workshop Station, Hunting Traps, Sawmill, and Arrow Whittling are available for high-efficiency Crafting & POH training!", color = OsrsParchment, fontSize = 10.sp)
                        }
                    }
                }



                Divider(color = Color(0xFF4A3828))

                // Sacred Altar & AFK Bone Burying Station (Magic XP)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🦴 Magic & Bone Offering (AFK)", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Buries inventory bones for Magic XP! (2x XP if Sacred Altar built)", color = OsrsParchment, fontSize = 9.5.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = onBuryBones,
                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Bury 1x", color = OsrsTextYellow, fontSize = 10.sp)
                        }
                        Button(
                            onClick = onToggleAfkBoneBurying,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAfkBoneBuryingActive) Color(0xFF2E6B38) else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (isAfkBoneBuryingActive) "⚡ AFK ON" else "AFK OFF", color = OsrsTextWhite, fontSize = 10.sp)
                        }
                    }
                }

                Divider(color = Color(0xFF4A3828))

                // 🔮 AFK Runecrafting Station (Runecrafting XP)
                val currentRune = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == selectedRuneId }
                    ?: RunecraftData.CRAFTABLE_RUNES.first()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🔮 Runemaking Altar (AFK)", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Crafts ${currentRune.iconEmoji} ${currentRune.runeName}s from Essence! (Lvl ${currentRune.reqLevel}+)", color = OsrsParchment, fontSize = 9.5.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { showRuneSelectorModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B0764)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("${currentRune.iconEmoji} Target", color = OsrsTextYellow, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onToggleAfkRunecrafting(currentRune.runeItemId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAfkRunecraftingActive) Color(0xFF2E6B38) else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (isAfkRunecraftingActive) "⚡ AFK ON" else "AFK OFF", color = OsrsTextWhite, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Rune Selector Modal Dialog
        if (showRuneSelectorModal) {
            AlertDialog(
                onDismissRequest = { showRuneSelectorModal = false },
                title = {
                    Text("🔮 Select AFK Runemaking Target Rune", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.heightIn(max = 350.dp)
                    ) {
                        items(RunecraftData.CRAFTABLE_RUNES) { rune ->
                            val isSelected = rune.runeItemId == selectedRuneId
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF3B0764) else Color(0xFF1E1712)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OsrsGold else Color(0xFF4A3828)),
                                onClick = {
                                    onToggleAfkRunecrafting(rune.runeItemId)
                                    showRuneSelectorModal = false
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OsrsItemIcon(itemId = rune.runeItemId, itemName = rune.runeName, fallbackEmoji = rune.iconEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(rune.runeName, color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("+${rune.xpPerEssence} XP per essence", color = OsrsParchment, fontSize = 10.sp)
                                        }
                                    }
                                    Text("Lvl ${rune.reqLevel}", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRuneSelectorModal = false }) {
                        Text("Close", color = OsrsTextYellow)
                    }
                },
                containerColor = Color(0xFF1E1712)
            )
        }

        // ⛵ SEAFARING PORT (AFK SAILING STATION)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2027)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00B4D8))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("⛵ Seafaring Port & AFK Sailing", color = Color(0xFF90E0EF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Row across the ocean! Companion pet floats in a rowboat in overlay while active!", color = OsrsParchment, fontSize = 9.5.sp)
                }
                Button(
                    onClick = onToggleAfkSailing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAfkSailingActive) Color(0xFF0284C7) else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(if (isAfkSailingActive) "⛵ AFK ON" else "AFK OFF", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun GeMaterialShopView(
    coinsGp: Long,
    materialInventory: Map<GeMaterial, Int>,
    onBuyGeMaterial: (GeMaterial, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📈 GRAND EXCHANGE HUT-KEEPING MATERIALS",
            color = OsrsGold,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(GeMaterial.entries) { mat ->
                val qty = materialInventory[mat] ?: 0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsParchment)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(mat.iconEmoji, fontSize = 24.sp)
                        Text(
                            text = mat.displayName,
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "GE Price: ${mat.defaultPriceGp} GP",
                            color = OsrsGold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Owned: $qty",
                            color = OsrsTextWhite,
                            fontSize = 11.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onBuyGeMaterial(mat, 1) },
                                enabled = coinsGp >= mat.defaultPriceGp,
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Buy 1x", color = OsrsTextYellow, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { onBuyGeMaterial(mat, 10) },
                                enabled = coinsGp >= mat.defaultPriceGp * 10,
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Buy 10x", color = OsrsTextYellow, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenagerieView(
    unlockedPets: List<PetType>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🐾 POH MENAGERIE PET HABITAT (${unlockedPets.size} Pets Unlocked)",
            color = OsrsGold,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Text(
            text = "All your unlocked rare skilling and boss pets freely roam here in your house menagerie!",
            color = OsrsParchment,
            fontSize = 11.sp
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(unlockedPets) { pet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231A12)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(pet.iconSymbol, fontSize = 32.sp)
                        Text(
                            text = pet.displayName,
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "\"${pet.defaultQuote}\"",
                            color = OsrsParchment,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getAvailableMaterialCount(
    mat: GeMaterial,
    pohState: PohHouseState,
    inventoryItems: List<InventoryItem> = emptyList(),
    bankItems: List<InventoryItem> = emptyList()
): Int {
    val pohQty = pohState.materialInventory[mat] ?: 0
    val allItems = (inventoryItems + bankItems).distinctBy { it.id }
    val storageQty = allItems.find { it.id == mat.itemId }?.quantity ?: 0
    return pohQty + storageQty
}

private fun formatRequiredMaterials(
    materials: Map<GeMaterial, Int>,
    pohState: PohHouseState? = null,
    inventoryItems: List<InventoryItem> = emptyList(),
    bankItems: List<InventoryItem> = emptyList()
): String {
    if (materials.isEmpty()) return "No materials"
    return materials.entries.joinToString(", ") { (mat, qty) ->
        if (pohState != null) {
            val owned = getAvailableMaterialCount(mat, pohState, inventoryItems, bankItems)
            "${mat.iconEmoji} $qty ${mat.displayName} (Have $owned)"
        } else {
            "${mat.iconEmoji} $qty ${mat.displayName}"
        }
    }
}

private fun getPredefinedFurnitureForRoom(roomType: PohRoomType): List<PohFurnitureItem> {
    val items = when (roomType) {
        PohRoomType.PARLOUR -> listOf(
            PohFurnitureItem("wooden_chair_1", "Wooden Chair", 1, 60L, PohRoomType.PARLOUR, mapOf(GeMaterial.BIRCH_PLANK to 2, GeMaterial.NAILS to 2), "🪑", "Basic seat. +5% Pet Happiness gain"),
            PohFurnitureItem("stone_campfire_p", "Stone Parlour Hearth", 1, 60L, PohRoomType.PARLOUR, mapOf(GeMaterial.BIRCH_PLANK to 2, GeMaterial.NAILS to 2), "🔥", "Stone hearth fireplace for AFK log burning & Summoning XP"),
            PohFurnitureItem("wooden_bookcase_1", "Wooden Bookcase", 4, 90L, PohRoomType.PARLOUR, mapOf(GeMaterial.BIRCH_PLANK to 4, GeMaterial.NAILS to 4), "📚", "Study bookshelf. +2% XP boost to all Skills"),
            PohFurnitureItem("willow_rocking_chair", "Willow Rocking Chair", 10, 140L, PohRoomType.PARLOUR, mapOf(GeMaterial.WILLOW_PLANK to 2, GeMaterial.NAILS to 2), "🪑", "Relaxing rocking chair. +8% Pet Happiness gain"),
            PohFurnitureItem("stone_fireplace_1", "Stone Hearth Fireplace", 15, 240L, PohRoomType.PARLOUR, mapOf(GeMaterial.BIRCH_PLANK to 3, GeMaterial.NAILS to 2), "🔥", "Warm hearth. Passively regenerates Run Energy & AFK log burning"),
            PohFurnitureItem("oak_armchair_1", "Oak Armchair", 19, 120L, PohRoomType.PARLOUR, mapOf(GeMaterial.OAK_PLANK to 2), "🛋️", "Comfortable armchair. +10% Pet Happiness gain"),
            PohFurnitureItem("pine_grandfather_clock", "Pine Grandfather Clock", 28, 300L, PohRoomType.PARLOUR, mapOf(GeMaterial.PINE_PLANK to 3, GeMaterial.NAILS to 2), "🕰️", "Antique pine clock. +5% AFK task completion speed"),
            PohFurnitureItem("teak_armchair_1", "Teak Armchair", 35, 270L, PohRoomType.PARLOUR, mapOf(GeMaterial.TEAK_PLANK to 2), "🪑", "Lounge seating. +15% Pet Happiness gain"),
            PohFurnitureItem("maple_reading_desk", "Maple Reading Desk", 45, 450L, PohRoomType.PARLOUR, mapOf(GeMaterial.MAPLE_PLANK to 3), "📖", "Scholarly study desk. +4% XP to gathering & crafting"),
            PohFurnitureItem("mahogany_armchair_1", "Mahogany Armchair", 50, 420L, PohRoomType.PARLOUR, mapOf(GeMaterial.MAHOGANY_PLANK to 2), "👑", "Royal armchair. +25% Pet Happiness gain"),
            PohFurnitureItem("yew_display_cabinet", "Yew Display Cabinet", 58, 600L, PohRoomType.PARLOUR, mapOf(GeMaterial.YEW_PLANK to 3, GeMaterial.CLOTH to 1), "🏺", "Polished display case for rare artifacts & treasures"),
            PohFurnitureItem("mahogany_bookcase_1", "Grand Library", 65, 840L, PohRoomType.PARLOUR, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.CLOTH to 2), "🏛️", "Grand Library. +5% XP boost to all Skills"),
            PohFurnitureItem("magic_crystal_chandelier", "Magic Crystal Chandelier", 75, 1200L, PohRoomType.PARLOUR, mapOf(GeMaterial.MAGIC_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "✨", "Luminous crystal chandelier. +10% POH prestige & aura"),
            PohFurnitureItem("redwood_lounge_suite", "Redwood Lounge Suite", 85, 2000L, PohRoomType.PARLOUR, mapOf(GeMaterial.REDWOOD_PLANK to 4, GeMaterial.CLOTH to 3, GeMaterial.GOLD_LEAF to 1), "🛋️", "Master luxury estate lounge. +35% Pet Happiness gain"),
            PohFurnitureItem("celestial_starlight_sofa", "Celestial Starlight Sofa", 92, 3500L, PohRoomType.PARLOUR, mapOf(GeMaterial.CELESTIAL_PLANK to 3, GeMaterial.GOLD_LEAF to 2, GeMaterial.MARBLE_BLOCK to 1), "🌌", "Divine starlight sofa. +10% Pet XP & instant happiness")
        )
        PohRoomType.GRASS_PATCH -> listOf(
            PohFurnitureItem("stepping_stones_gp", "Stone Stepping Stones", 1, 40L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.BIRCH_PLANK to 1, GeMaterial.NAILS to 1), "🪨", "River pebble stepping stones across the grassy lawn"),
            PohFurnitureItem("wildflower_border_gp", "Wildflower Garden Border", 5, 80L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.BIRCH_PLANK to 2), "🌸", "Vibrant colorful wildflowers bordering the pathway"),
            PohFurnitureItem("willow_garden_arch_gp", "Willow Garden Archway", 15, 180L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.WILLOW_PLANK to 3), "🌿", "Natural arched trellis with trailing ivy and shaded vines"),
            PohFurnitureItem("pine_trail_sign_gp", "Pine Trail Signpost", 25, 240L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.PINE_PLANK to 2, GeMaterial.NAILS to 2), "🪧", "Rustic directional trail sign indicating nearby chambers"),
            PohFurnitureItem("oak_picnic_spot_gp", "Oak Picnic Spot", 35, 360L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.OAK_PLANK to 4, GeMaterial.CLOTH to 1), "🧺", "Scenic grass picnic table. +5% Energy recovery outdoors"),
            PohFurnitureItem("maple_leaf_hedge_gp", "Maple Topiary Hedge", 48, 500L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.MAPLE_PLANK to 3), "🍁", "Sculpted autumn maple hedge adding estate beauty"),
            PohFurnitureItem("fairy_lantern_gp", "Fairy Garden Lamp", 60, 800L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.YEW_PLANK to 2, GeMaterial.GOLD_LEAF to 1), "🏮", "Soft glowing fairy lantern illuminating the lawn at night"),
            PohFurnitureItem("magic_blossom_canopy_gp", "Magic Blossom Canopy", 75, 1400L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.MAGIC_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "🌺", "Magical luminous canopy with gentle falling petals"),
            PohFurnitureItem("celestial_zen_garden_gp", "Celestial Zen Garden", 88, 2600L, PohRoomType.GRASS_PATCH, mapOf(GeMaterial.CELESTIAL_PLANK to 3, GeMaterial.MARBLE_BLOCK to 1), "⛩️", "Transcendent outdoor sanctuary for companion pet meditation")
        )
        PohRoomType.GARDEN -> listOf(
            PohFurnitureItem("wooden_bench_g", "Wooden Garden Bench", 1, 60L, PohRoomType.GARDEN, mapOf(GeMaterial.BIRCH_PLANK to 2, GeMaterial.NAILS to 2), "🪑", "Garden seating. Rest recovers energy"),
            PohFurnitureItem("stone_campfire_g", "Outdoor Stone Campfire", 1, 60L, PohRoomType.GARDEN, mapOf(GeMaterial.BIRCH_PLANK to 2, GeMaterial.NAILS to 2), "🔥", "Outdoor stone campfire for AFK log burning & Summoning XP"),
            PohFurnitureItem("planted_sunflowers", "Planted Sunflowers", 1, 50L, PohRoomType.GARDEN, mapOf(GeMaterial.BIRCH_PLANK to 2, GeMaterial.NAILS to 1), "🌻", "Floral bed. Passively boosts Pet Mood"),
            PohFurnitureItem("poh_allotment_patch_g", "2x2 Allotment Crop Patch", 5, 120L, PohRoomType.GARDEN, mapOf(GeMaterial.BIRCH_PLANK to 3, GeMaterial.NAILS to 2), "🌱", "Unlocks 4 POH Allotment sub-plots (A1, A2, B1, B2) for farming herbs & veggies!"),
            PohFurnitureItem("willow_garden_swing", "Willow Garden Swing", 12, 140L, PohRoomType.GARDEN, mapOf(GeMaterial.WILLOW_PLANK to 3, GeMaterial.CLOTH to 1), "🎋", "Playful garden swing for pets. +10% Mood boost"),
            PohFurnitureItem("oak_bench_g", "Oak Garden Bench", 15, 120L, PohRoomType.GARDEN, mapOf(GeMaterial.OAK_PLANK to 2), "🪵", "Outdoor oak bench. +5% Run Energy recovery"),
            PohFurnitureItem("poh_tree_patch_g", "2x2 Tree Orchard Patch", 25, 240L, PohRoomType.GARDEN, mapOf(GeMaterial.OAK_PLANK to 4, GeMaterial.NAILS to 2), "🌳", "Unlocks 4 POH Tree Orchard sub-plots (Tree A1, A2, B1, B2) for trees & fruit trees!"),
            PohFurnitureItem("maple_tree_g", "Big Maple Tree", 25, 200L, PohRoomType.GARDEN, mapOf(GeMaterial.BIRCH_PLANK to 3, GeMaterial.NAILS to 1), "🍁", "Shady tree. +10% Harvesting XP nearby"),
            PohFurnitureItem("cedar_bird_bath", "Cedar Bird Bath & Fountain", 32, 280L, PohRoomType.GARDEN, mapOf(GeMaterial.CEDAR_PLANK to 3), "⛲", "Carved cedar fountain attracting songbirds to the garden"),
            PohFurnitureItem("garden_pond_g", "Ornamental Fishing Pond", 35, 300L, PohRoomType.GARDEN, mapOf(GeMaterial.BIRCH_PLANK to 4, GeMaterial.MARBLE_BLOCK to 1), "🌊", "Garden Fishing Pond for passive raw fish & XP"),
            PohFurnitureItem("yew_garden_trellis", "Yew Garden Trellis", 44, 450L, PohRoomType.GARDEN, mapOf(GeMaterial.YEW_PLANK to 4), "🌿", "Lush flowering yew trellis. +10% Farming growth rate"),
            PohFurnitureItem("formal_hedge_g", "Formal Topiary Hedge", 50, 480L, PohRoomType.GARDEN, mapOf(GeMaterial.OAK_PLANK to 4), "🌳", "Topiary hedge. +10% POH House Prestige"),
            PohFurnitureItem("magic_fountain_g", "Magic Mist Fountain", 68, 1000L, PohRoomType.GARDEN, mapOf(GeMaterial.MAGIC_PLANK to 3, GeMaterial.MARBLE_BLOCK to 1), "⛲", "Enchanted mist fountain that restores pet vitality"),
            PohFurnitureItem("marble_sundial_g", "Marble Sundial", 75, 1800L, PohRoomType.GARDEN, mapOf(GeMaterial.MARBLE_BLOCK to 1, GeMaterial.GOLD_LEAF to 1), "☀️", "Ancient sundial. +15% POH House Prestige"),
            PohFurnitureItem("redwood_gazebo_g", "Redwood Garden Gazebo", 82, 2200L, PohRoomType.GARDEN, mapOf(GeMaterial.REDWOOD_PLANK to 4, GeMaterial.CLOTH to 2), "🛖", "Shaded redwood pavilion for relaxing in all seasons"),
            PohFurnitureItem("spirit_tree_shrine_g", "Spirit Tree Shrine", 91, 3600L, PohRoomType.GARDEN, mapOf(GeMaterial.SPIRIT_PLANK to 3, GeMaterial.GOLD_LEAF to 2), "✨", "Sacred tree shrine blessing your estate with double nature harvest")
        )
        PohRoomType.KITCHEN -> listOf(
            PohFurnitureItem("clay_cooking_stove", "Clay Cooking Stove", 1, 60L, PohRoomType.KITCHEN, mapOf(GeMaterial.BIRCH_PLANK to 2, GeMaterial.NAILS to 2), "🍳", "Basic cooking stove for AFK cooking raw food"),
            PohFurnitureItem("wooden_pump_sink", "Wooden Pump & Sink", 7, 90L, PohRoomType.KITCHEN, mapOf(GeMaterial.BIRCH_PLANK to 2, GeMaterial.NAILS to 2), "🚰", "Fresh water source for cooking meals"),
            PohFurnitureItem("beer_barrel", "Wine & Beer Barrel", 7, 60L, PohRoomType.KITCHEN, mapOf(GeMaterial.BIRCH_PLANK to 3, GeMaterial.NAILS to 2), "🍺", "Brewing barrel. Boosts Strength & Combat stats"),
            PohFurnitureItem("willow_bread_box", "Willow Bread & Flour Bin", 14, 120L, PohRoomType.KITCHEN, mapOf(GeMaterial.WILLOW_PLANK to 2, GeMaterial.NAILS to 2), "🍞", "Keeps baked goods fresh and provides baking supplies"),
            PohFurnitureItem("pine_spice_cupboard", "Pine Spice Cupboard", 24, 220L, PohRoomType.KITCHEN, mapOf(GeMaterial.PINE_PLANK to 3, GeMaterial.NAILS to 2), "🧂", "Fragrant pine cupboard filled with cooking herbs & spices"),
            PohFurnitureItem("cooking_range", "Iron Cooking Range", 30, 300L, PohRoomType.KITCHEN, mapOf(GeMaterial.OAK_PLANK to 4, GeMaterial.NAILS to 4), "🍳", "Reduces food burn rate to 0%"),
            PohFurnitureItem("oak_larder", "Oak Larder", 33, 480L, PohRoomType.KITCHEN, mapOf(GeMaterial.OAK_PLANK to 8), "🗄️", "Unlimited basic cooking ingredients storage"),
            PohFurnitureItem("cedar_fermentation_cask", "Cedar Brewing Cask", 42, 420L, PohRoomType.KITCHEN, mapOf(GeMaterial.CEDAR_PLANK to 4), "🍷", "Aged cedar barrel for brewing potent chef ales"),
            PohFurnitureItem("teak_kitchen_table", "Teak Kitchen Table", 52, 540L, PohRoomType.KITCHEN, mapOf(GeMaterial.TEAK_PLANK to 6), "🍽️", "Gourmet prep station. +10% Cooking XP"),
            PohFurnitureItem("maple_pastry_counter", "Maple Pastry Counter", 58, 620L, PohRoomType.KITCHEN, mapOf(GeMaterial.MAPLE_PLANK to 4), "🥐", "Artisan bakery prep station. +15% Pastry & Pie XP"),
            PohFurnitureItem("mahogany_spice_rack", "Mahogany Spice Rack", 67, 960L, PohRoomType.KITCHEN, mapOf(GeMaterial.MAHOGANY_PLANK to 3), "📦", "Master Spice Rack. +15% Cooking XP & double food yield"),
            PohFurnitureItem("yew_smokehouse", "Yew Meat & Fish Smokehouse", 74, 1300L, PohRoomType.KITCHEN, mapOf(GeMaterial.YEW_PLANK to 4, GeMaterial.NAILS to 4), "🥓", "Smokes fish and meats for extra health restoration"),
            PohFurnitureItem("magic_icebox", "Magic Preserving Icebox", 83, 2200L, PohRoomType.KITCHEN, mapOf(GeMaterial.MAGIC_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "🧊", "Magical frost chest that preserves cooked food permanently"),
            PohFurnitureItem("sunfire_chef_range", "Sunfire Master Gourmet Range", 93, 3800L, PohRoomType.KITCHEN, mapOf(GeMaterial.SUNFIRE_PLANK to 3, GeMaterial.GOLD_LEAF to 2, GeMaterial.MARBLE_BLOCK to 1), "🔥", "Legendary chef range. +25% Cooking XP & master banquet cooking")
        )
        PohRoomType.DINING_ROOM -> listOf(
            PohFurnitureItem("wooden_dining_table", "Wooden Dining Table", 10, 180L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.BIRCH_PLANK to 4, GeMaterial.NAILS to 4), "🪵", "Dining table. +5% Pet Hunger reduction from meals"),
            PohFurnitureItem("willow_dining_chairs", "Willow Dining Chairs", 16, 160L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.WILLOW_PLANK to 3), "🪑", "Comfortable woven willow dining chair set"),
            PohFurnitureItem("oak_dining_table", "Oak Dining Table", 22, 360L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.OAK_PLANK to 6), "🍽️", "Spacious dining table. +10% Pet Hunger reduction"),
            PohFurnitureItem("pine_service_cart", "Pine Beverage Trolley", 27, 260L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.PINE_PLANK to 3, GeMaterial.NAILS to 2), "🛒", "Mobile drinks trolley for serving feast refreshments"),
            PohFurnitureItem("carved_oak_bench", "Carved Oak Bench", 31, 240L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.OAK_PLANK to 4), "🪑", "Crafted oak bench seating"),
            PohFurnitureItem("teak_dining_table", "Teak Dining Table", 38, 540L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.TEAK_PLANK to 6), "🍷", "Fine teak table. +15% Pet Hunger reduction"),
            PohFurnitureItem("cedar_wine_rack", "Cedar Wine & Ale Rack", 44, 420L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.CEDAR_PLANK to 3), "🍾", "Rustic cedar display holding fine vintage vintages"),
            PohFurnitureItem("mahogany_dining_table", "Mahogany Dining Table", 52, 840L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.MAHOGANY_PLANK to 6), "👑", "Banqueting table. +25% Pet Hunger reduction & +10% Pet XP"),
            PohFurnitureItem("maple_buffet_table", "Maple Banquet Buffet", 60, 750L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.MAPLE_PLANK to 5), "🍲", "Grand serving buffet for multi-course meals"),
            PohFurnitureItem("gilded_candelabra", "Gilded Candelabra", 68, 1200L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.MAHOGANY_PLANK to 2, GeMaterial.GOLD_LEAF to 1), "🕯️", "Gold dining illumination. +10% POH Prestige"),
            PohFurnitureItem("yew_royal_credenza", "Yew Royal Credenza", 76, 1500L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.YEW_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "🗄️", "Finely carved sideboard storing royal gold silverware"),
            PohFurnitureItem("redwood_grand_banquet", "Redwood Grand Banquet Hall", 86, 2600L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.REDWOOD_PLANK to 6, GeMaterial.GOLD_LEAF to 2, GeMaterial.CLOTH to 2), "🏰", "Massive redwood banquet table hosting legendary feasts"),
            PohFurnitureItem("celestial_feast_table", "Celestial Feast Table", 94, 4200L, PohRoomType.DINING_ROOM, mapOf(GeMaterial.CELESTIAL_PLANK to 4, GeMaterial.MARBLE_BLOCK to 2, GeMaterial.GOLD_LEAF to 2), "🌌", "Cosmic feast altar with everlasting ambrosia delights")
        )
        PohRoomType.WORKSHOP -> listOf(
            PohFurnitureItem("wooden_workbench", "Wooden Workbench", 1, 60L, PohRoomType.WORKSHOP, mapOf(GeMaterial.BIRCH_PLANK to 2, GeMaterial.NAILS to 2), "🔨", "Basic crafting workbench"),
            PohFurnitureItem("oak_workbench", "Oak Workbench", 15, 120L, PohRoomType.WORKSHOP, mapOf(GeMaterial.OAK_PLANK to 2), "🛠️", "Improved crafting station"),
            PohFurnitureItem("crafting_table", "Crafting Table", 16, 120L, PohRoomType.WORKSHOP, mapOf(GeMaterial.BIRCH_PLANK to 4, GeMaterial.NAILS to 4), "✂️", "Tool crafting table"),
            PohFurnitureItem("pine_fletching_jig", "Pine Fletching & Arrow Rig", 22, 200L, PohRoomType.WORKSHOP, mapOf(GeMaterial.PINE_PLANK to 3, GeMaterial.NAILS to 2), "🏹", "Precision fletching station. +10% Fletching speed"),
            PohFurnitureItem("clockwork_bench", "Clockwork Bench", 25, 240L, PohRoomType.WORKSHOP, mapOf(GeMaterial.OAK_PLANK to 4, GeMaterial.NAILS to 2), "⚙️", "Mechanical bench for toys & clockwork gadgets"),
            PohFurnitureItem("plumbed_sink", "Plumbed Workshop Sink", 35, 300L, PohRoomType.WORKSHOP, mapOf(GeMaterial.TEAK_PLANK to 4), "🚰", "Clean workspace. +5% Sawmill plank crafting speed"),
            PohFurnitureItem("sawmill_bench", "Integrated Sawmill Station", 40, 480L, PohRoomType.WORKSHOP, mapOf(GeMaterial.OAK_PLANK to 6, GeMaterial.NAILS to 4), "🪚", "Convert logs directly to planks inside your POH!"),
            PohFurnitureItem("cedar_tool_cabinet", "Cedar Tool Cabinet", 46, 460L, PohRoomType.WORKSHOP, mapOf(GeMaterial.CEDAR_PLANK to 4, GeMaterial.NAILS to 2), "🗄️", "Organized cabinet storing precision chisels, saws & hammers"),
            PohFurnitureItem("ironwood_anvil_stand", "Ironwood Anvil Stand", 54, 600L, PohRoomType.WORKSHOP, mapOf(GeMaterial.IRONWOOD_PLANK to 4), "⚒️", "Heavy ironwood forge base. +10% Smithing XP in POH"),
            PohFurnitureItem("mahogany_tool_rack", "Master Carpenter Tool Rack", 60, 960L, PohRoomType.WORKSHOP, mapOf(GeMaterial.MAHOGANY_PLANK to 4), "🧰", "Master Tools. -10% material cost on future furniture"),
            PohFurnitureItem("yew_crafting_bench", "Yew Crafting & Invention Rig", 68, 1100L, PohRoomType.WORKSHOP, mapOf(GeMaterial.YEW_PLANK to 4, GeMaterial.NAILS to 4), "🔬", "Advanced workshop station for gadgets and automata"),
            PohFurnitureItem("magic_runic_lathe", "Magic Runic Lathe", 76, 1600L, PohRoomType.WORKSHOP, mapOf(GeMaterial.MAGIC_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "🔮", "Rune-infused woodworking lathe. +15% Construction XP"),
            PohFurnitureItem("cosmic_forge_station", "Cosmic Crafting Forge", 88, 2800L, PohRoomType.WORKSHOP, mapOf(GeMaterial.COSMIC_PLANK to 3, GeMaterial.MARBLE_BLOCK to 1, GeMaterial.GOLD_LEAF to 1), "🌌", "Celestial workshop anvil for mythical metal shaping"),
            PohFurnitureItem("golden_spirit_workshop", "Golden Spirit Master Workshop", 95, 4500L, PohRoomType.WORKSHOP, mapOf(GeMaterial.GOLDEN_SPIRIT_PLANK to 3, GeMaterial.GOLD_LEAF to 3), "🏆", "Ultimate artisan sanctuary. -25% plank crafting cost & +20% XP")
        )
        PohRoomType.BEDROOM -> listOf(
            PohFurnitureItem("wooden_bed_1", "Wooden Bed", 20, 180L, PohRoomType.BEDROOM, mapOf(GeMaterial.BIRCH_PLANK to 3, GeMaterial.NAILS to 2), "🛏️", "Cozy bed. +10% Pet Energy recovery"),
            PohFurnitureItem("oak_dresser_1", "Oak Dresser", 27, 240L, PohRoomType.BEDROOM, mapOf(GeMaterial.OAK_PLANK to 2), "🗄️", "Dresser & bedroom accessory storage"),
            PohFurnitureItem("oak_bed_1", "Oak Bed", 30, 360L, PohRoomType.BEDROOM, mapOf(GeMaterial.OAK_PLANK to 3), "🛏️", "Comfortable oak bed. +20% Pet Energy recovery"),
            PohFurnitureItem("pine_wardrobe", "Pine Double Wardrobe", 34, 320L, PohRoomType.BEDROOM, mapOf(GeMaterial.PINE_PLANK to 4, GeMaterial.NAILS to 2), "🚪", "Spacious pine clothing wardrobe for costumes & gear"),
            PohFurnitureItem("teak_bed_1", "Teak Bed", 40, 540L, PohRoomType.BEDROOM, mapOf(GeMaterial.TEAK_PLANK to 3, GeMaterial.CLOTH to 2), "🛋️", "Luxury down bed. +30% Pet Energy recovery"),
            PohFurnitureItem("cedar_nightstand", "Cedar Nightstand & Lamp", 44, 380L, PohRoomType.BEDROOM, mapOf(GeMaterial.CEDAR_PLANK to 2), "🕯️", "Polished bedside table with gentle reading light"),
            PohFurnitureItem("servant_clock", "Automated Alarm Clock", 50, 600L, PohRoomType.BEDROOM, mapOf(GeMaterial.TEAK_PLANK to 2, GeMaterial.NAILS to 2), "⏰", "Doubles AFK reward production speed"),
            PohFurnitureItem("maple_canopy_bed", "Maple Canopy Bed", 62, 800L, PohRoomType.BEDROOM, mapOf(GeMaterial.MAPLE_PLANK to 4, GeMaterial.CLOTH to 3), "🛏️", "Four-poster draped maple canopy bed. +40% Energy regen"),
            PohFurnitureItem("yew_vanity_mirror", "Yew Vanity Mirror", 72, 1100L, PohRoomType.BEDROOM, mapOf(GeMaterial.YEW_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "🪞", "Gilded dressing mirror. Passively boosts Pet Charisma"),
            PohFurnitureItem("mahogany_bed_1", "Mahogany Bed", 80, 1400L, PohRoomType.BEDROOM, mapOf(GeMaterial.MAHOGANY_PLANK to 3, GeMaterial.CLOTH to 2), "🛌", "King velvet bed. +50% Pet Energy recovery & +100% HP heal"),
            PohFurnitureItem("magic_dream_catcher", "Magic Dream Sanctuary", 86, 2200L, PohRoomType.BEDROOM, mapOf(GeMaterial.MAGIC_PLANK to 3, GeMaterial.GOLD_LEAF to 1, GeMaterial.CLOTH to 2), "🔮", "Mystical slumber realm granting rested XP bonus upon waking"),
            PohFurnitureItem("cosmic_starlight_bedchamber", "Cosmic Starlight Bedchamber", 96, 4600L, PohRoomType.BEDROOM, mapOf(GeMaterial.COSMIC_PLANK to 3, GeMaterial.CELESTIAL_PLANK to 2, GeMaterial.GOLD_LEAF to 2), "🌌", "Astral bedding that fully recharges all pet energy instantly")
        )
        PohRoomType.SKILL_HALL -> listOf(
            PohFurnitureItem("oak_trophy_base", "Oak Trophy Base", 25, 240L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.OAK_PLANK to 4), "🏆", "Displays skill achievement trophies"),
            PohFurnitureItem("pine_hunting_rack", "Pine Hunter Trophy Plaque", 32, 280L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.PINE_PLANK to 3, GeMaterial.NAILS to 2), "🦌", "Mounted hunting trophies. +5% Hunter XP gains"),
            PohFurnitureItem("rune_armour_stand", "Rune Armour Stand", 38, 300L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.OAK_PLANK to 3, GeMaterial.NAILS to 2), "🛡️", "Displays Rune Armor set. +5% Combat Defense"),
            PohFurnitureItem("teak_trophy_base", "Teak Trophy Base", 41, 540L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.TEAK_PLANK to 4), "🥇", "Displays advanced trophies. +2% Overall XP"),
            PohFurnitureItem("cedar_fish_mount", "Cedar Giant Fish Mount", 44, 380L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.CEDAR_PLANK to 3), "🐟", "Trophy mount of prize catches. +5% Fishing XP"),
            PohFurnitureItem("head_trophy_stand", "Head Trophy Stand", 48, 480L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.TEAK_PLANK to 4), "🐲", "Displays Dragon Head trophy. +5% Bounty Hunter & Combat Damage"),
            PohFurnitureItem("ironwood_weapon_display", "Ironwood Arsenal Pedestal", 56, 680L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.IRONWOOD_PLANK to 4), "⚔️", "Showcases legendary weapons. +5% Combat accuracy"),
            PohFurnitureItem("maple_archery_stand", "Maple Master Bow Stand", 63, 800L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.MAPLE_PLANK to 4), "🏹", "Displays composite & long bows. +8% Ranged damage"),
            PohFurnitureItem("skill_cape_display", "Skill Cape Display Stand", 70, 1200L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "🧥", "Displays Skillcapes. +5% XP to chosen skill"),
            PohFurnitureItem("yew_relic_case", "Yew Ancient Relic Showcase", 78, 1500L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.YEW_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "🏺", "Glass display case for rare quest & dungeon artifacts"),
            PohFurnitureItem("mythical_banner", "Heroic Mythical Banner", 85, 2400L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.CLOTH to 2, GeMaterial.GOLD_LEAF to 1), "🚩", "Heroic banner. +10% POH Prestige & +5% All XP"),
            PohFurnitureItem("astral_mastery_totem", "Astral Skill Mastery Totem", 92, 3600L, PohRoomType.SKILL_HALL, mapOf(GeMaterial.ASTRAL_PLANK to 3, GeMaterial.GOLD_LEAF to 2, GeMaterial.MARBLE_BLOCK to 1), "🌌", "Shining astral pillar boosting all skill gains across Gielinor by +5%")
        )
        PohRoomType.GAMES_ROOM -> listOf(
            PohFurnitureItem("attack_stone", "Attack Stone Game", 30, 240L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.OAK_PLANK to 4), "🎯", "Target game. +5% Hand Combat & Blowdarts accuracy training"),
            PohFurnitureItem("drafts_board", "Drafts Game Board", 34, 300L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.OAK_PLANK to 4, GeMaterial.CLOTH to 1), "🎲", "Strategy game. +5% Trickery & Dexterity XP"),
            PohFurnitureItem("balance_chest", "Balance Chest", 40, 420L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.TEAK_PLANK to 4), "📦", "Dexterity challenge chest. +5% Dexterity XP"),
            PohFurnitureItem("cedar_dartboard", "Cedar Blowdart Target", 46, 440L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.CEDAR_PLANK to 3, GeMaterial.NAILS to 2), "🎯", "Precision bullseye board. +8% Dart throwing accuracy"),
            PohFurnitureItem("prize_chest", "Prize Chest", 54, 600L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.MAHOGANY_PLANK to 4), "🎁", "Lucky reward chest. Periodically grants GP & materials"),
            PohFurnitureItem("archery_target", "Archery Range Target", 62, 720L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.TEAK_PLANK to 4, GeMaterial.CLOTH to 2), "🏹", "Precision target. +10% Blowdarts XP"),
            PohFurnitureItem("maple_roulette_table", "Maple Fortune Wheel", 70, 1100L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.MAPLE_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "🎡", "Interactive fortune wheel awarding daily bonus tokens"),
            PohFurnitureItem("magic_chess_board", "Magic Animated Chess", 82, 1900L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.MAGIC_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "♟️", "Self-moving magical chess set that trains tactical mind"),
            PohFurnitureItem("cosmic_arcade_cabinet", "Cosmic Pinball Arcade", 91, 3400L, PohRoomType.GAMES_ROOM, mapOf(GeMaterial.COSMIC_PLANK to 3, GeMaterial.GOLD_LEAF to 2), "👾", "Retro cosmic arcade table offering grand high score minigames")
        )
        PohRoomType.COMBAT_ROOM -> listOf(
            PohFurnitureItem("dueling_ring_base", "Dueling Ring Base", 32, 240L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.OAK_PLANK to 4), "⚔️", "PvP Dueling arena ring"),
            PohFurnitureItem("pine_sparring_dummy", "Pine Sparring Dummy", 36, 320L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.PINE_PLANK to 3, GeMaterial.CLOTH to 1), "🥋", "Sturdy sparring dummy for unarmed and melee training"),
            PohFurnitureItem("weapons_display_rack", "Weapons Display Rack", 42, 300L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.TEAK_PLANK to 4), "🗡️", "Displays weaponry. +5% Melee Strength"),
            PohFurnitureItem("combat_dummy", "Undead Combat Dummy", 48, 360L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.OAK_PLANK to 3, GeMaterial.CLOTH to 2), "🎯", "Practice dummy. Test DPS & gain +5% Combat XP"),
            PohFurnitureItem("ironwood_shield_rack", "Ironwood Shield Barrier", 58, 640L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.IRONWOOD_PLANK to 4), "🛡️", "Reinforced defensive shield rack. +8% Armor rating"),
            PohFurnitureItem("dueling_ring_rope", "Dueling Ring Rope Stand", 64, 720L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.MAHOGANY_PLANK to 4), "🥊", "Champion ring. +10% Combat Accuracy & Defense"),
            PohFurnitureItem("subjugation_altar", "Subjugation Altar", 76, 1500L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "🔥", "Empowers weapon special attacks by +20%"),
            PohFurnitureItem("emberwood_battle_sigil", "Emberwood War Sigil", 84, 2300L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.EMBERWOOD_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "⚔️", "Flaming battle banner. +15% Special attack recharge rate"),
            PohFurnitureItem("obsidian_combat_monolith", "Obsidian Combat Monolith", 92, 3700L, PohRoomType.COMBAT_ROOM, mapOf(GeMaterial.OBSIDIAN_PLANK to 3, GeMaterial.MARBLE_BLOCK to 2, GeMaterial.GOLD_LEAF to 1), "🗿", "Ancient obsidian pillar granting +10% Melee, Ranged & Magic DPS")
        )
        PohRoomType.MENAGERIE -> listOf(
            PohFurnitureItem("oak_pet_house", "Oak Pet Habitat", 37, 240L, PohRoomType.MENAGERIE, mapOf(GeMaterial.OAK_PLANK to 4), "🏠", "Basic pet habitat. Houses up to 3 pets"),
            PohFurnitureItem("natural_arena", "Natural Pet Arena", 45, 360L, PohRoomType.MENAGERIE, mapOf(GeMaterial.OAK_PLANK to 4, GeMaterial.CLOTH to 2), "🏟️", "Pet play area. +15% Pet XP gains"),
            PohFurnitureItem("cedar_scratching_post", "Cedar Pet Climbing Tree", 49, 440L, PohRoomType.MENAGERIE, mapOf(GeMaterial.CEDAR_PLANK to 3, GeMaterial.CLOTH to 1), "🌲", "Climbing playground where feline and small pets exercise"),
            PohFurnitureItem("teak_pet_house", "Teak Pet Habitat", 52, 540L, PohRoomType.MENAGERIE, mapOf(GeMaterial.TEAK_PLANK to 4), "🏰", "Expanded pet habitat. Houses up to 6 pets"),
            PohFurnitureItem("pet_toy_box", "Pet Toy Box", 58, 720L, PohRoomType.MENAGERIE, mapOf(GeMaterial.TEAK_PLANK to 4, GeMaterial.CLOTH to 2), "🧸", "Interactive pet toys. Passively keeps pets happy & fed"),
            PohFurnitureItem("maple_pet_feeder", "Maple Automated Food Feeder", 64, 820L, PohRoomType.MENAGERIE, mapOf(GeMaterial.MAPLE_PLANK to 4), "🥣", "Dispenses favorite pet treats on schedule"),
            PohFurnitureItem("mahogany_pet_house", "Mahogany Pet Palace", 68, 1120L, PohRoomType.MENAGERIE, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "👑", "Royal pet mansion. Houses ALL unlocked pets freely roaming!"),
            PohFurnitureItem("spirit_pet_sanctuary", "Spirit Pet Sanctuary", 79, 1800L, PohRoomType.MENAGERIE, mapOf(GeMaterial.SPIRIT_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "✨", "Ethereal sanctuary accelerating companion pet evolution"),
            PohFurnitureItem("dragon_roost_perch", "Dragon Sky Roost", 89, 2900L, PohRoomType.MENAGERIE, mapOf(GeMaterial.REDWOOD_PLANK to 4, GeMaterial.MARBLE_BLOCK to 1, GeMaterial.GOLD_LEAF to 1), "🐉", "High mountain roost for avian and dragon companions"),
            PohFurnitureItem("celestial_pet_elysium", "Celestial Pet Elysium", 97, 4800L, PohRoomType.MENAGERIE, mapOf(GeMaterial.CELESTIAL_PLANK to 3, GeMaterial.GOLDEN_SPIRIT_PLANK to 2, GeMaterial.GOLD_LEAF to 2), "🌌", "Divine haven where all companion pets achieve transcendence")
        )
        PohRoomType.CHAPEL -> listOf(
            PohFurnitureItem("incense_burners", "Incense Burner Torches", 40, 180L, PohRoomType.CHAPEL, mapOf(GeMaterial.OAK_PLANK to 2, GeMaterial.CLOTH to 1), "🔥", "Adds +0.5x extra Magic XP multiplier on Altars!"),
            PohFurnitureItem("oak_altar", "Oak Altar", 45, 240L, PohRoomType.CHAPEL, mapOf(GeMaterial.OAK_PLANK to 4), "🕯️", "1.5x Magic XP when offering bones"),
            PohFurnitureItem("teak_altar", "Teak Altar", 60, 540L, PohRoomType.CHAPEL, mapOf(GeMaterial.TEAK_PLANK to 4, GeMaterial.CLOTH to 2), "🕯️", "1.75x Magic XP when offering bones"),
            PohFurnitureItem("cedar_organ", "Cedar Cathedral Pipe Organ", 66, 750L, PohRoomType.CHAPEL, mapOf(GeMaterial.CEDAR_PLANK to 4, GeMaterial.NAILS to 4), "🎹", "Magnificent musical pipe organ playing sacred hymns"),
            PohFurnitureItem("mahogany_altar", "Mahogany Altar", 70, 840L, PohRoomType.CHAPEL, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.CLOTH to 2), "🏛️", "2.0x Magic XP when offering bones"),
            PohFurnitureItem("gilded_altar", "Gilded Altar", 75, 2800L, PohRoomType.CHAPEL, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.GOLD_LEAF to 2), "🌟", "3.5x Magic XP when offering bones with burners!"),
            PohFurnitureItem("sacred_statue", "Sacred Statue", 82, 1800L, PohRoomType.CHAPEL, mapOf(GeMaterial.MARBLE_BLOCK to 1, GeMaterial.GOLD_LEAF to 1), "🗿", "Holy blessing. +10% Magic Power retention"),
            PohFurnitureItem("astral_holy_shrine", "Astral Celestial Shrine", 91, 3600L, PohRoomType.CHAPEL, mapOf(GeMaterial.ASTRAL_PLANK to 3, GeMaterial.MARBLE_BLOCK to 2, GeMaterial.GOLD_LEAF to 2), "✨", "4.0x Magic XP and automatic bone consecration")
        )
        PohRoomType.PORTAL_CHAMBER -> listOf(
            PohFurnitureItem("teak_portal_frame", "Teak Portal Frame", 50, 540L, PohRoomType.PORTAL_CHAMBER, mapOf(GeMaterial.TEAK_PLANK to 3), "🌀", "Basic teleport portal frame (Grand Capital & Riverbank)"),
            PohFurnitureItem("scrying_pool", "Scrying Pool", 50, 300L, PohRoomType.PORTAL_CHAMBER, mapOf(GeMaterial.OAK_PLANK to 4), "🪞", "Scrying pool. View distant regions & Boss locations"),
            PohFurnitureItem("mahogany_portal_frame", "Mahogany Portal Frame", 65, 840L, PohRoomType.PORTAL_CHAMBER, mapOf(GeMaterial.MAHOGANY_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "🔮", "Advanced portal frame (White Keep & High Citadel)"),
            PohFurnitureItem("focus_crystal", "Focus Crystal", 72, 1200L, PohRoomType.PORTAL_CHAMBER, mapOf(GeMaterial.MAHOGANY_PLANK to 3, GeMaterial.GOLD_LEAF to 1), "💎", "Focus crystal. Instant spellbook switching & teleport boost"),
            PohFurnitureItem("marble_portal_frame", "Marble Portal Frame", 80, 2100L, PohRoomType.PORTAL_CHAMBER, mapOf(GeMaterial.MARBLE_BLOCK to 1, GeMaterial.MAHOGANY_PLANK to 2), "🌌", "Master portal frame (Shadow Realm & Elven Spire)"),
            PohFurnitureItem("sunfire_portal_nexus", "Sunfire Dimension Portal", 87, 2800L, PohRoomType.PORTAL_CHAMBER, mapOf(GeMaterial.SUNFIRE_PLANK to 3, GeMaterial.GOLD_LEAF to 2), "☀️", "Dimensional rift linking to volcanic & desert territories"),
            PohFurnitureItem("cosmic_teleport_gate", "Cosmic Teleport Gateway", 94, 4200L, PohRoomType.PORTAL_CHAMBER, mapOf(GeMaterial.COSMIC_PLANK to 3, GeMaterial.MARBLE_BLOCK to 2, GeMaterial.GOLD_LEAF to 2), "🪐", "Instant teleportation gateway to any destination in Gielinor")
        )
        PohRoomType.SUPERIOR_GARDEN -> listOf(
            PohFurnitureItem("poh_allotment_patch_sg", "2x2 Master Crop Allotment", 65, 500L, PohRoomType.SUPERIOR_GARDEN, mapOf(GeMaterial.TEAK_PLANK to 4, GeMaterial.MARBLE_BLOCK to 1), "🌱", "Master 2x2 Allotment Patch (Sub-plots A1, A2, B1, B2). +15% Farming yield & XP!"),
            PohFurnitureItem("revitalisation_pool", "Revitalisation Pool", 65, 600L, PohRoomType.SUPERIOR_GARDEN, mapOf(GeMaterial.TEAK_PLANK to 4, GeMaterial.MARBLE_BLOCK to 1), "🧪", "Restores Special Attack & Run Energy"),
            PohFurnitureItem("spirit_tree_patch", "Spirit Tree Patch", 75, 1200L, PohRoomType.SUPERIOR_GARDEN, mapOf(GeMaterial.TEAK_PLANK to 4), "🌳", "Spirit Tree network. Instant travel to any wilderness/city tree"),
            PohFurnitureItem("poh_tree_patch_sg", "2x2 Superior Tree Orchard", 75, 1000L, PohRoomType.SUPERIOR_GARDEN, mapOf(GeMaterial.TEAK_PLANK to 4, GeMaterial.MARBLE_BLOCK to 1), "🍎", "Master 2x2 Tree Orchard (Sub-plots Tree A1, A2, B1, B2). +15% Tree yield & XP!"),
            PohFurnitureItem("theme_obelisk", "Theme Teleport Obelisk", 80, 1800L, PohRoomType.SUPERIOR_GARDEN, mapOf(GeMaterial.MARBLE_BLOCK to 1, GeMaterial.GOLD_LEAF to 1), "🗼", "Wilderness Obelisk. Instant wilderness teleports"),
            PohFurnitureItem("celestial_fairy_ring", "Celestial Fairy Ring", 85, 2400L, PohRoomType.SUPERIOR_GARDEN, mapOf(GeMaterial.MARBLE_BLOCK to 1, GeMaterial.MAHOGANY_PLANK to 4), "🧚", "Fairy Ring network. Instant access to all fairy codes"),
            PohFurnitureItem("ornate_rejuv_pool", "Ornate Pool of Rejuvenation", 90, 3500L, PohRoomType.SUPERIOR_GARDEN, mapOf(GeMaterial.MARBLE_BLOCK to 2, GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.GOLD_LEAF to 2), "✨", "Fully restores HP, Magic, Energy, Special Attack & Cures Poison!"),
            PohFurnitureItem("golden_spirit_sanctum", "Golden Spirit Nature Sanctum", 96, 4800L, PohRoomType.SUPERIOR_GARDEN, mapOf(GeMaterial.GOLDEN_SPIRIT_PLANK to 3, GeMaterial.MARBLE_BLOCK to 2, GeMaterial.GOLD_LEAF to 2), "🌸", "Legendary oasis that continuously blesses the player with divine health")
        )
        PohRoomType.ACHIEVEMENT_GALLERY -> listOf(
            PohFurnitureItem("mahogany_cape_stand", "Mahogany Cape Stand", 80, 1200L, PohRoomType.ACHIEVEMENT_GALLERY, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "🧥", "Displays Skillcapes & grants +1000 Daily Bonus XP"),
            PohFurnitureItem("display_case_achieve", "Quest Display Case", 82, 1400L, PohRoomType.ACHIEVEMENT_GALLERY, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.CLOTH to 1), "🖼️", "Displays Quest relics. +10% Quest reward bonuses"),
            PohFurnitureItem("boss_trophy_stand", "Boss Slayer Trophy Stand", 88, 2200L, PohRoomType.ACHIEVEMENT_GALLERY, mapOf(GeMaterial.MAHOGANY_PLANK to 4, GeMaterial.GOLD_LEAF to 1), "🏆", "Displays rare boss drops & pet slayer trophies. +10% Boss drop rate"),
            PohFurnitureItem("occult_altar", "Occult Altar", 90, 4000L, PohRoomType.ACHIEVEMENT_GALLERY, mapOf(GeMaterial.MARBLE_BLOCK to 2, GeMaterial.GOLD_LEAF to 2, GeMaterial.MAHOGANY_PLANK to 4), "🔮", "Ancient Altar. Switch between Standard, Ancient, and Lunar Magic"),
            PohFurnitureItem("eternity_mythic_pillar", "Eternity Mythic Champion Pillar", 98, 5500L, PohRoomType.ACHIEVEMENT_GALLERY, mapOf(GeMaterial.GOLDEN_SPIRIT_PLANK to 3, GeMaterial.MARBLE_BLOCK to 2, GeMaterial.GOLD_LEAF to 3), "👑", "The ultimate monument of prestige honoring 99 in all skills")
        )
    }
    return items.sortedBy { it.reqLevel }
}

@Composable
private fun VisualRoomTile(
    room: BuiltRoom,
    petState: PetState?,
    bounceOffset: Float,
    isCentralPetRoom: Boolean,
    gridDimension: Int = 3,
    zoomScale: Float = 1.0f,
    onClick: () -> Unit
) {
    val floorColor = Color(room.floorType.colorHex)

    val maxNameLen = if (gridDimension >= 5 && zoomScale < 1.0f) 4 else if (gridDimension >= 5) 6 else 8
    val baseFontSize = if (gridDimension >= 5) 7.5f else 9f
    val computedFontSize = (baseFontSize * zoomScale).coerceIn(6f, 13f).sp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(floorColor)
            .drawWithContent {
                drawContent()
                val wallStroke = (3.5f * zoomScale).coerceIn(2.5f, 5f).dp.toPx()
                // North Wall (Top)
                if (room.wallNorth != PohWallType.NONE) {
                    drawLine(
                        color = Color(room.wallNorth.colorHex),
                        start = Offset(0f, wallStroke / 2),
                        end = Offset(size.width, wallStroke / 2),
                        strokeWidth = wallStroke
                    )
                }
                // East Wall (Right)
                if (room.wallEast != PohWallType.NONE) {
                    drawLine(
                        color = Color(room.wallEast.colorHex),
                        start = Offset(size.width - wallStroke / 2, 0f),
                        end = Offset(size.width - wallStroke / 2, size.height),
                        strokeWidth = wallStroke
                    )
                }
                // South Wall (Bottom)
                if (room.wallSouth != PohWallType.NONE) {
                    drawLine(
                        color = Color(room.wallSouth.colorHex),
                        start = Offset(0f, size.height - wallStroke / 2),
                        end = Offset(size.width, size.height - wallStroke / 2),
                        strokeWidth = wallStroke
                    )
                }
                // West Wall (Left)
                if (room.wallWest != PohWallType.NONE) {
                    drawLine(
                        color = Color(room.wallWest.colorHex),
                        start = Offset(wallStroke / 2, 0f),
                        end = Offset(wallStroke / 2, size.height),
                        strokeWidth = wallStroke
                    )
                }
                // Subtle boundary border if all walls are none
                if (room.wallNorth == PohWallType.NONE &&
                    room.wallEast == PohWallType.NONE &&
                    room.wallSouth == PohWallType.NONE &&
                    room.wallWest == PohWallType.NONE
                ) {
                    drawRect(
                        color = Color(0x33D4AF37),
                        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )
                }
            }
            .clickable { onClick() }
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${room.roomType.iconEmoji} ${room.roomType.displayName.take(maxNameLen)}",
                color = OsrsTextYellow,
                fontWeight = FontWeight.Bold,
                fontSize = computedFontSize,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            val builtFurn = getPredefinedFurnitureForRoom(room.roomType)
                .filter { room.builtFurnitureIds.contains(it.id) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (builtFurn.isNotEmpty()) {
                    val maxIcons = if (gridDimension >= 5 && zoomScale < 1.0f) 1 else if (gridDimension >= 5) 2 else 3
                    builtFurn.take(maxIcons).forEach { f ->
                        Text(f.iconEmoji, fontSize = (10f * zoomScale).coerceIn(7f, 14f).sp)
                    }
                } else {
                    Text("🔨 Empty", color = Color.LightGray, fontSize = (computedFontSize.value * 0.85f).coerceIn(5f, 10f).sp)
                }
            }

            if (isCentralPetRoom && petState != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = (bounceOffset * zoomScale.coerceAtMost(1f)).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(OsrsParchment)
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${petState.petType.iconSymbol} ${petState.customName.take(if (gridDimension >= 5) 4 else 6)}",
                            color = OsrsTextDark,
                            fontSize = (7f * zoomScale).coerceIn(5.5f, 10f).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = "Inspect",
                    color = OsrsParchment.copy(alpha = 0.8f),
                    fontSize = (6.5f * zoomScale).coerceIn(5f, 9f).sp
                )
            }
        }
    }
}

@Composable
private fun VisualEmptySlotTile(
    slotIndex: Int,
    gridDimension: Int = 3,
    zoomScale: Float = 1.0f,
    onClick: () -> Unit
) {
    val row = slotIndex / gridDimension + 1
    val col = slotIndex % gridDimension + 1
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF140F0A))
            .border(
                border = androidx.compose.foundation.BorderStroke(1.2.dp, OsrsGold.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text("➕", fontSize = (12f * zoomScale).coerceIn(8f, 18f).sp)
            Text(
                "Build",
                color = OsrsGold,
                fontWeight = FontWeight.Bold,
                fontSize = (8f * zoomScale).coerceIn(6f, 11f).sp
            )
            Text(
                "R$row C$col",
                color = OsrsParchment,
                fontSize = (6.5f * zoomScale).coerceIn(5f, 9f).sp
            )
        }
    }
}

@Composable
private fun BuiltRoomCard(
    room: BuiltRoom,
    pohState: PohHouseState,
    constructionLevel: Int,
    inventoryItems: List<InventoryItem> = emptyList(),
    bankItems: List<InventoryItem> = emptyList(),
    gridDimension: Int,
    onInspectRoom: () -> Unit,
    onDeleteRoom: () -> Unit,
    onBuildFurniture: (PohFurnitureItem) -> Unit,
    onDestroyFurniture: (String) -> Unit
) {
    val r = room.gridPosition / gridDimension + 1
    val c = room.gridPosition % gridDimension + 1
    val allFurnitureOptions = getPredefinedFurnitureForRoom(room.roomType)
    val builtCount = allFurnitureOptions.count { room.builtFurnitureIds.contains(it.id) }
    var filterMode by remember { mutableStateOf("ALL") } // "ALL", "BUILT", "AVAILABLE"
    val furnScrollState = rememberScrollState()

    val filteredFurniture = remember(allFurnitureOptions, room.builtFurnitureIds, constructionLevel, filterMode, pohState.materialInventory, inventoryItems, bankItems) {
        when (filterMode) {
            "BUILT" -> allFurnitureOptions.filter { room.builtFurnitureIds.contains(it.id) }
            "AVAILABLE" -> allFurnitureOptions.filter { furn ->
                !room.builtFurnitureIds.contains(furn.id) &&
                    constructionLevel >= furn.reqLevel &&
                    furn.requiredMaterials.all { (mat, reqQty) ->
                        getAvailableMaterialCount(mat, pohState, inventoryItems, bankItems) >= reqQty
                    }
            }
            else -> allFurnitureOptions
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
        border = BorderStroke(1.dp, OsrsParchment),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Room Header with Location and Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Color(0xFF1B140D),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, OsrsGold)
                    ) {
                        Text(
                            text = room.roomType.iconEmoji,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = room.roomType.displayName,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                color = Color(0xFF1B140D),
                                shape = RoundedCornerShape(3.dp),
                                border = BorderStroke(0.5.dp, OsrsGold)
                            ) {
                                Text(
                                    text = "Lvl ${room.roomType.reqLevel}",
                                    color = OsrsGoldBright,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Grid Location: Row $r, Col $c • ($builtCount/${allFurnitureOptions.size} Built)",
                            color = OsrsParchment,
                            fontSize = 10.sp
                        )
                    }
                }

                // Delete Room Button
                Button(
                    onClick = onDeleteRoom,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    border = BorderStroke(1.dp, Color(0xFFFF5252)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.testTag("destroy_room_button_${room.gridPosition}")
                ) {
                    Text("🗑️ Delete Room", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = Color(0xFF4A3828))

            // Furniture Section Header with Quick Filter Chips & Scroll Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🪑 Furniture Options (${filteredFurniture.size}/${allFurnitureOptions.size}):",
                    color = OsrsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "ALL" to "All (${allFurnitureOptions.size})",
                        "BUILT" to "Built ($builtCount)",
                        "AVAILABLE" to "Can Build"
                    ).forEach { (modeKey, label) ->
                        val isSelected = filterMode == modeKey
                        Surface(
                            modifier = Modifier.clickable { filterMode = modeKey },
                            shape = RoundedCornerShape(3.dp),
                            color = if (isSelected) OsrsGold else Color(0xFF1B140D),
                            border = BorderStroke(0.5.dp, if (isSelected) OsrsGoldBright else Color(0xFF4E3629))
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else OsrsParchment,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "↕ Scroll below to view all different furniture options in this room",
                color = Color(0xFF81D4FA),
                fontSize = 9.sp
            )

            // Scrollable Container for Room Furniture Options
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 250.dp)
                    .background(Color(0xFF18110B), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF3E2D23), RoundedCornerShape(6.dp))
                    .padding(6.dp)
                    .verticalScroll(furnScrollState)
            ) {
                if (filteredFurniture.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterMode == "BUILT") "No furniture built in this room yet." else "No furniture matches current filter.",
                            color = OsrsParchment,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredFurniture.forEach { furn ->
                            val isBuilt = room.builtFurnitureIds.contains(furn.id)
                            val hasMaterials = furn.requiredMaterials.all { (mat, reqQty) ->
                                getAvailableMaterialCount(mat, pohState, inventoryItems, bankItems) >= reqQty
                            }
                            val canBuild = constructionLevel >= furn.reqLevel && hasMaterials

                            Surface(
                                color = if (isBuilt) Color(0xFF222A1E) else Color(0xFF241A13),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, if (isBuilt) Color(0xFF70E000) else Color(0xFF4A3828)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(furn.iconEmoji, fontSize = 14.sp)
                                            Text(
                                                text = "${furn.name} (Lvl ${furn.reqLevel})",
                                                color = if (isBuilt) Color(0xFF70E000) else OsrsTextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = if (isBuilt) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                        if (furn.effectDescription.isNotEmpty()) {
                                            Text(
                                                text = "⚡ ${furn.effectDescription}",
                                                color = Color(0xFF64B5F6),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Text(
                                            text = formatRequiredMaterials(furn.requiredMaterials, pohState, inventoryItems, bankItems),
                                            color = if (hasMaterials) OsrsGold else Color(0xFFE57373),
                                            fontSize = 9.sp
                                        )
                                    }

                                    if (isBuilt) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("✓ Built", color = Color(0xFF70E000), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Button(
                                                onClick = { onDestroyFurniture(furn.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)),
                                                shape = RoundedCornerShape(4.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("🗑️ Destroy", color = Color.White, fontSize = 9.sp)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { onBuildFurniture(furn) },
                                            enabled = canBuild,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (canBuild) OsrsRedFrame else Color(0xFF3E2D23)
                                            ),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            val btnText = when {
                                                constructionLevel < furn.reqLevel -> "🔒 Lvl ${furn.reqLevel}"
                                                !hasMaterials -> "🔨 Need Mats"
                                                else -> "Build (+${furn.xpGained} XP)"
                                            }
                                            Text(btnText, color = if (canBuild) OsrsTextYellow else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun DeleteRoomConfirmDialog(
    room: BuiltRoom,
    gridDimension: Int,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val row = room.gridPosition / gridDimension + 1
    val col = room.gridPosition % gridDimension + 1
    val builtCount = room.builtFurnitureIds.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("⚠️", fontSize = 20.sp)
                Text(
                    text = "Delete ${room.roomType.displayName}?",
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Are you sure you want to delete and demolish this room from your Player Owned House?",
                    color = OsrsTextWhite,
                    fontSize = 12.sp
                )

                Surface(
                    color = Color(0xFF22170E),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF8B0000))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Room Type:", color = OsrsParchment, fontSize = 11.sp)
                            Text("${room.roomType.iconEmoji} ${room.roomType.displayName}", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grid Location:", color = OsrsParchment, fontSize = 11.sp)
                            Text("Row $row, Column $col", color = OsrsGoldBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Built Furniture:", color = OsrsParchment, fontSize = 11.sp)
                            Text(
                                if (builtCount > 0) "$builtCount items will be removed" else "Empty (0 items)",
                                color = if (builtCount > 0) Color(0xFFFF8A80) else Color(0xFF70E000),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Text(
                    text = "✓ Grid Slot (Row $row, Col $col) will become available to build any other room immediately.",
                    color = Color(0xFF81D4FA),
                    fontSize = 10.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                border = BorderStroke(1.dp, Color(0xFFFF5252)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("confirm_delete_room_button")
            ) {
                Text("🗑️ Demolish & Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OsrsParchment, fontSize = 11.sp)
            }
        },
        containerColor = OsrsLeatherDark
    )
}

@Composable
private fun RoomInspectorDialog(
    room: BuiltRoom,
    pohState: PohHouseState,
    constructionLevel: Int,
    inventoryItems: List<InventoryItem> = emptyList(),
    bankItems: List<InventoryItem> = emptyList(),
    gridDimension: Int = 3,
    onBuildFurniture: (PohFurnitureItem) -> Unit,
    onDestroyFurniture: (String) -> Unit,
    onApplyWallsAndFloor: (PohWallType, PohWallType, PohWallType, PohWallType, PohFloorType) -> Unit,
    onDemolishRoom: (BuiltRoom) -> Unit,
    onDismiss: () -> Unit
) {
    val row = room.gridPosition / gridDimension + 1
    val col = room.gridPosition % gridDimension + 1
    val furnOptions = getPredefinedFurnitureForRoom(room.roomType)
    val builtCount = furnOptions.count { room.builtFurnitureIds.contains(it.id) }
    var activeDialogTab by remember { mutableIntStateOf(0) } // 0 = Furniture, 1 = Walls & Floor
    var filterMode by remember { mutableStateOf("ALL") }
    val inspectorScrollState = rememberScrollState()

    // Walls & Floor state
    var selectedNorthWall by remember(room) { mutableStateOf<PohWallType>(room.wallNorth) }
    var selectedEastWall by remember(room) { mutableStateOf<PohWallType>(room.wallEast) }
    var selectedSouthWall by remember(room) { mutableStateOf<PohWallType>(room.wallSouth) }
    var selectedWestWall by remember(room) { mutableStateOf<PohWallType>(room.wallWest) }
    var selectedFloor by remember(room) { mutableStateOf<PohFloorType>(room.floorType) }
    var activeWallMaterialForBatch by remember { mutableStateOf<PohWallType>(PohWallType.STONE_WALL) }

    val filteredOptions = remember(furnOptions, room.builtFurnitureIds, constructionLevel, filterMode, pohState.materialInventory, inventoryItems, bankItems) {
        when (filterMode) {
            "BUILT" -> furnOptions.filter { room.builtFurnitureIds.contains(it.id) }
            "AVAILABLE" -> furnOptions.filter { furn ->
                !room.builtFurnitureIds.contains(furn.id) &&
                    constructionLevel >= furn.reqLevel &&
                    furn.requiredMaterials.all { (mat, reqQty) ->
                        getAvailableMaterialCount(mat, pohState, inventoryItems, bankItems) >= reqQty
                    }
            }
            else -> furnOptions
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(room.roomType.iconEmoji, fontSize = 22.sp)
                Column {
                    Text("Inspect ${room.roomType.displayName}", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Grid Position: Row $row, Col $col • ($builtCount/${furnOptions.size} Built)", color = OsrsGold, fontSize = 10.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(room.roomType.description, color = OsrsParchment, fontSize = 11.sp)

                // Sub-tabs: 0 = Furniture, 1 = Walls & Floors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { activeDialogTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeDialogTab == 0) OsrsGold else Color(0xFF241A13)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (activeDialogTab == 0) OsrsGoldBright else Color(0xFF4A3828)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.weight(1f).height(30.dp)
                    ) {
                        Text("🪑 Furniture ($builtCount)", color = if (activeDialogTab == 0) Color.Black else OsrsParchment, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { activeDialogTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeDialogTab == 1) OsrsGold else Color(0xFF241A13)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (activeDialogTab == 1) OsrsGoldBright else Color(0xFF4A3828)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.weight(1f).height(30.dp)
                    ) {
                        Text("🧱 Walls & Floor", color = if (activeDialogTab == 1) Color.Black else OsrsParchment, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = OsrsGold)

                if (activeDialogTab == 0) {
                    // Furniture Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪑 Furniture Options:", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf("ALL" to "All (${furnOptions.size})", "BUILT" to "Built ($builtCount)", "AVAILABLE" to "Can Build").forEach { (key, lbl) ->
                                val isSel = filterMode == key
                                Surface(
                                    modifier = Modifier.clickable { filterMode = key },
                                    shape = RoundedCornerShape(3.dp),
                                    color = if (isSel) OsrsGold else Color(0xFF1B140D),
                                    border = BorderStroke(0.5.dp, if (isSel) OsrsGoldBright else Color(0xFF4E3629))
                                ) {
                                    Text(
                                        text = lbl,
                                        color = if (isSel) Color.Black else OsrsParchment,
                                        fontSize = 8.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text("↕ Scroll below to view all different furniture options", color = Color(0xFF81D4FA), fontSize = 9.sp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 320.dp)
                            .background(Color(0xFF18110B), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF3E2D23), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                            .verticalScroll(inspectorScrollState)
                    ) {
                        if (filteredOptions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No furniture matching filter.", color = OsrsParchment, fontSize = 11.sp)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                filteredOptions.forEach { furn ->
                                    val isBuilt = room.builtFurnitureIds.contains(furn.id)
                                    val hasMaterials = furn.requiredMaterials.all { (mat, reqQty) ->
                                        getAvailableMaterialCount(mat, pohState, inventoryItems, bankItems) >= reqQty
                                    }
                                    val canBuild = constructionLevel >= furn.reqLevel && hasMaterials

                                    Surface(
                                        color = if (isBuilt) Color(0xFF222A1E) else Color(0xFF241A13),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(0.5.dp, if (isBuilt) Color(0xFF70E000) else Color(0xFF4A3828)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${furn.iconEmoji} ${furn.name}", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                if (furn.effectDescription.isNotEmpty()) {
                                                    Text("⚡ Perk: ${furn.effectDescription}", color = Color(0xFF64B5F6), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                                }
                                                Text("Req Con Lvl ${furn.reqLevel} • +${furn.xpGained} XP", color = OsrsParchment, fontSize = 9.sp)
                                                Text(
                                                    text = formatRequiredMaterials(furn.requiredMaterials, pohState, inventoryItems, bankItems),
                                                    color = if (hasMaterials) OsrsGold else Color(0xFFE57373),
                                                    fontSize = 9.sp
                                                )
                                            }

                                            if (isBuilt) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text("✅ Built", color = Color(0xFF70E000), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    Button(
                                                        onClick = { onDestroyFurniture(furn.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)),
                                                        shape = RoundedCornerShape(4.dp),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("🗑️", color = Color.White, fontSize = 9.sp)
                                                    }
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        onBuildFurniture(furn)
                                                        onDismiss()
                                                    },
                                                    enabled = canBuild,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (canBuild) OsrsRedFrame else Color(0xFF3E2D23)
                                                    ),
                                                    shape = RoundedCornerShape(4.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    val btnText = when {
                                                        constructionLevel < furn.reqLevel -> "🔒 Lvl ${furn.reqLevel}"
                                                        !hasMaterials -> "🔨 Need Mats"
                                                        else -> "🔨 Build"
                                                    }
                                                    Text(btnText, color = if (canBuild) OsrsTextYellow else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Walls & Floor Customizer Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 420.dp)
                            .background(Color(0xFF18110B), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF3E2D23), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Live Preview Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🎨 Live Tile Preview", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Floor: ${selectedFloor.displayName}", color = OsrsTextWhite, fontSize = 10.sp)
                                    val wallsActive = listOf(
                                        if (selectedNorthWall != PohWallType.NONE) "N: ${selectedNorthWall.displayName}" else null,
                                        if (selectedEastWall != PohWallType.NONE) "E: ${selectedEastWall.displayName}" else null,
                                        if (selectedSouthWall != PohWallType.NONE) "S: ${selectedSouthWall.displayName}" else null,
                                        if (selectedWestWall != PohWallType.NONE) "W: ${selectedWestWall.displayName}" else null
                                    ).filterNotNull()
                                    Text(
                                        text = if (wallsActive.isNotEmpty()) wallsActive.joinToString(", ") else "Open (No Walls)",
                                        color = OsrsParchment,
                                        fontSize = 9.5.sp
                                    )
                                }

                                // Mini interactive preview box
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(selectedFloor.colorHex))
                                        .drawWithContent {
                                            drawContent()
                                            val stroke = 4.dp.toPx()
                                            if (selectedNorthWall != PohWallType.NONE) {
                                                drawLine(Color(selectedNorthWall.colorHex), Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
                                            }
                                            if (selectedEastWall != PohWallType.NONE) {
                                                drawLine(Color(selectedEastWall.colorHex), Offset(size.width - stroke / 2, 0f), Offset(size.width - stroke / 2, size.height), stroke)
                                            }
                                            if (selectedSouthWall != PohWallType.NONE) {
                                                drawLine(Color(selectedSouthWall.colorHex), Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
                                            }
                                            if (selectedWestWall != PohWallType.NONE) {
                                                drawLine(Color(selectedWestWall.colorHex), Offset(stroke / 2, 0f), Offset(stroke / 2, size.height), stroke)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(room.roomType.iconEmoji, fontSize = 20.sp)
                                }
                            }

                            // Quick Wall Presets
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("⚡ Quick Wall Presets:", color = OsrsGold, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedNorthWall = activeWallMaterialForBatch
                                            selectedEastWall = activeWallMaterialForBatch
                                            selectedSouthWall = activeWallMaterialForBatch
                                            selectedWestWall = activeWallMaterialForBatch
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(26.dp)
                                    ) {
                                        Text("🏰 4 Walls", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            selectedNorthWall = PohWallType.NONE
                                            selectedEastWall = PohWallType.NONE
                                            selectedSouthWall = PohWallType.NONE
                                            selectedWestWall = PohWallType.NONE
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(26.dp)
                                    ) {
                                        Text("🚪 0 Walls", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            selectedNorthWall = activeWallMaterialForBatch
                                            selectedSouthWall = activeWallMaterialForBatch
                                            selectedEastWall = PohWallType.NONE
                                            selectedWestWall = PohWallType.NONE
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B382B)),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(26.dp)
                                    ) {
                                        Text("↕️ N & S", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            selectedEastWall = activeWallMaterialForBatch
                                            selectedWestWall = activeWallMaterialForBatch
                                            selectedNorthWall = PohWallType.NONE
                                            selectedSouthWall = PohWallType.NONE
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B382B)),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(26.dp)
                                    ) {
                                        Text("↔️ E & W", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Active Wall Material Selector
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("🧱 Wall Material:", color = OsrsGold, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    PohWallType.entries.filter { it != PohWallType.NONE }.forEach { wallType ->
                                        val isSel = activeWallMaterialForBatch == wallType
                                        Surface(
                                            modifier = Modifier.clickable { activeWallMaterialForBatch = wallType },
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSel) OsrsGold else Color(0xFF241A13),
                                            border = BorderStroke(1.dp, if (isSel) OsrsGoldBright else Color(0xFF4A3828))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = wallType.displayName,
                                                    color = if (isSel) Color.Black else OsrsTextWhite,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (wallType.costGp > 0) "${wallType.costGp} GP" else "Free",
                                                    color = if (isSel) Color(0xFF1B5E20) else OsrsGold,
                                                    fontSize = 8.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Wall Direction Selectors (Vertically Stacked with Scrollable Horizontal Chips)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("📐 Individual Wall Configuration (N, E, S, W):", color = OsrsGold, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)

                                listOf(
                                    "⬆️ North Wall" to selectedNorthWall,
                                    "➡️ East Wall" to selectedEastWall,
                                    "⬇️ South Wall" to selectedSouthWall,
                                    "⬅️ West Wall" to selectedWestWall
                                ).forEachIndexed { idx, (label, currentWall) ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF22170F), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, Color(0xFF3E2D23), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(label, color = OsrsTextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (currentWall == PohWallType.NONE) "No Wall (Open)" else currentWall.displayName,
                                                color = if (currentWall == PohWallType.NONE) Color(0xFFFFCC80) else OsrsGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            PohWallType.entries.forEach { wallType ->
                                                val isSelected = currentWall == wallType
                                                Surface(
                                                    modifier = Modifier.clickable {
                                                        when (idx) {
                                                            0 -> selectedNorthWall = wallType
                                                            1 -> selectedEastWall = wallType
                                                            2 -> selectedSouthWall = wallType
                                                            3 -> selectedWestWall = wallType
                                                        }
                                                        if (wallType != PohWallType.NONE) {
                                                            activeWallMaterialForBatch = wallType
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = if (isSelected) OsrsGold else Color(0xFF1B140D),
                                                    border = BorderStroke(0.5.dp, if (isSelected) OsrsGoldBright else Color(0xFF4E3629))
                                                ) {
                                                    Text(
                                                        text = if (wallType == PohWallType.NONE) "🚪 None" else wallType.displayName,
                                                        color = if (isSelected) Color.Black else OsrsParchment,
                                                        fontSize = 8.5.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Floor Type Selector
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("🪵 Floor Type:", color = OsrsGold, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    PohFloorType.entries.forEach { floor ->
                                        val isSel = selectedFloor == floor
                                        Surface(
                                            modifier = Modifier.clickable { selectedFloor = floor },
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSel) OsrsGold else Color(0xFF241A13),
                                            border = BorderStroke(1.dp, if (isSel) OsrsGoldBright else Color(0xFF4A3828))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = floor.displayName,
                                                    color = if (isSel) Color.Black else OsrsTextWhite,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (floor.costGp > 0) "${floor.costGp} GP" else "Free",
                                                    color = if (isSel) Color(0xFF1B5E20) else OsrsGold,
                                                    fontSize = 8.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Apply Button
                            Button(
                                onClick = {
                                    onApplyWallsAndFloor(
                                        selectedNorthWall,
                                        selectedEastWall,
                                        selectedSouthWall,
                                        selectedWestWall,
                                        selectedFloor
                                    )
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                border = BorderStroke(1.dp, Color(0xFF81C784)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("🔨 Apply Walls & Flooring Configuration", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Divider(color = Color(0xFF4A3828))

                Button(
                    onClick = {
                        onDemolishRoom(room)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    border = BorderStroke(1.dp, Color(0xFFFF5252)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_destroy_room_button")
                ) {
                    Text("🗑️ Delete Room & Clear Grid Slot (Row $row, Col $col)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = OsrsGold, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = OsrsLeatherDark,
        shape = RoundedCornerShape(12.dp)
    )
}
