package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@Composable
fun PofFarmTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val pofState by viewModel.pofState.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val isAfkFarmingActive by viewModel.isAfkFarmingActive.collectAsStateWithLifecycle()
    val afkSeedCategory by viewModel.afkSeedCategory.collectAsStateWithLifecycle()
    val npcFavorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    val bryanFavorLvl = npcFavorMap["bryan"]?.first ?: viewModel.getNpcFavorLevel("bryan")
    val isPrimateFleetCompleted = petState.completedQuestIds.contains("quest_monkey_madness_2") || petState.completedQuestIds.contains("quest_monkey_madness_2_part2")

    PofFarmTab(
        pofState = pofState,
        farmingXp = skillXpMap[OsrsSkill.FARMING] ?: 0L,
        hunterXp = skillXpMap[OsrsSkill.HUNTER] ?: 0L,
        constructionXp = skillXpMap[OsrsSkill.CONSTRUCTION] ?: 0L,
        coinsGp = petState.coinsGp,
        inventoryItems = inventoryItems,
        bankItems = bankItems,
        isAfkFarmingActive = isAfkFarmingActive,
        afkSeedCategory = afkSeedCategory,
        isQuest3Completed = petState.completedQuestIds.contains("quest_goblin_diplomacy"),
        bryanFavorLvl = bryanFavorLvl,
        isPrimateFleetCompleted = isPrimateFleetCompleted,
        onToggleAfkFarming = { category -> viewModel.toggleAfkFarming(category) },
        onDepositToCompostBin = { itemId, qty -> viewModel.addCropToCompostBin(itemId, qty) },
        onDepositAllCompostable = { viewModel.depositAllCompostableItemsToCompostBin() },
        onInspectItem = { itemId -> viewModel.inspectItemObtain(itemId) },
        onPlantSeed = { index, crop -> viewModel.plantSeedInPlot(index, crop) },
        onWaterPlot = { index -> viewModel.waterPlot(index) },
        onCompostPlot = { index -> viewModel.compostPlot(index) },
        onHarvestPlot = { index -> viewModel.harvestCropPlot(index) },
        onClearPlot = { index -> viewModel.clearPlot(index) },
        onBuySeed = { item, qty -> viewModel.buyShopItem(item) },
        onRequestContract = { difficulty -> viewModel.requestFarmingContract(difficulty) },
        onClaimContractReward = { viewModel.claimContractReward() },
        onOpenSeedPouch = { viewModel.openSeedPouch() },
        onBuyLivestock = { type -> viewModel.buyHusbandryLivestock(type) },
        onRenameLivestock = { id, name -> viewModel.renameHusbandryLivestock(id, name) },
        onSellLivestock = { id -> viewModel.sellOrDismissLivestock(id) },
        onFeedTrough = { itemId, qty -> viewModel.feedHusbandryTrough(itemId, qty) },
        onDepositAllCrops = { viewModel.depositAllAvailableCropsToTrough() },
        onCraftTroughSlosh = { id1, id2 -> viewModel.craftTroughSlosh(id1, id2) },
        onWithdrawHusbandryChest = { viewModel.withdrawHusbandryChestRewards() },
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PofFarmTab(
    pofState: PlayerOwnedFarmState,
    farmingXp: Long,
    hunterXp: Long = 0L,
    constructionXp: Long = 0L,
    coinsGp: Long,
    inventoryItems: List<InventoryItem>,
    bankItems: List<InventoryItem> = emptyList(),
    isAfkFarmingActive: Boolean,
    afkSeedCategory: SeedCategory,
    isQuest3Completed: Boolean = false,
    bryanFavorLvl: Int = 0,
    isPrimateFleetCompleted: Boolean = false,
    onToggleAfkFarming: (SeedCategory?) -> Unit,
    onDepositToCompostBin: (itemId: String, qty: Int) -> Unit,
    onDepositAllCompostable: (() -> Unit)? = null,
    onInspectItem: ((String) -> Unit)? = null,
    onPlantSeed: (plotIndex: Int, cropType: FarmCropType) -> Unit,
    onWaterPlot: (plotIndex: Int) -> Unit,
    onCompostPlot: (plotIndex: Int) -> Unit,
    onHarvestPlot: (plotIndex: Int) -> Unit,
    onClearPlot: (plotIndex: Int) -> Unit,
    onBuySeed: (seedItem: InventoryItem, qty: Int) -> Unit,
    onRequestContract: (ContractDifficulty) -> Unit = {},
    onClaimContractReward: () -> Unit = {},
    onOpenSeedPouch: () -> Unit = {},
    onBuyLivestock: (LivestockType) -> Unit = {},
    onRenameLivestock: (instanceId: String, newName: String) -> Unit = { _, _ -> },
    onSellLivestock: (instanceId: String) -> Unit = {},
    onFeedTrough: (itemId: String, qty: Int) -> Unit = { _, _ -> },
    onDepositAllCrops: () -> Unit = {},
    onCraftTroughSlosh: (itemId1: String, itemId2: String) -> Unit = { _, _ -> },
    onWithdrawHusbandryChest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val farmingLevel = OsrsXpCalculator.getLevelForXp(farmingXp)
    val hunterLevel = OsrsXpCalculator.getLevelForXp(hunterXp)
    val constructionLevel = OsrsXpCalculator.getLevelForXp(constructionXp)
    val totalDoubleCropChance = (bryanFavorLvl + if (isPrimateFleetCompleted) 25 else 0).coerceIn(0, 100)
    var selectedPlotIndexForPlanting by remember { mutableStateOf<Int?>(null) }
    var showSeedMerchantDialog by remember { mutableStateOf(false) }
    var showCompostDepositDialog by remember { mutableStateOf(false) }
    var showAfkCategoryDialog by remember { mutableStateOf(false) }
    var showFeedTroughDialog by remember { mutableStateOf(false) }
    var showCraftSloshDialog by remember { mutableStateOf(false) }
    var showBonusBreakdownDialog by remember { mutableStateOf(false) }
    var animalToRename by remember { mutableStateOf<FarmAnimalInstance?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live countdown ticker
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTimeMs = System.currentTimeMillis()
        }
    }

    val botanicalFloraPercent = remember(farmingLevel) {
        if (farmingLevel <= 1) 0 else (((farmingLevel - 1).toFloat() / 98f) * 100f).toInt().coerceIn(1, 100)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
    ) {
        // Living dynamic botanical background that flourishes as farming level rises (0% at level 1)
        FarmingDynamicBotanicalBackground(
            farmingLevel = farmingLevel,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- POF HEADER COMPACT RECTANGLE CARD ---
            WoodPlankPanel(
                modifier = Modifier.fillMaxWidth(),
                accentIcon = "🚜",
                borderColor = OsrsGold,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                    // Row 1: Title, Level & Bonus Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🚜", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "Player Owned Farm",
                                    color = OsrsTextYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "🌾 Lv. $farmingLevel Farming",
                                    color = Color(0xFF81C784),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = Color(0xFF2E7D32).copy(alpha = 0.45f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF81C784)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .clickable { showBonusBreakdownDialog = true }
                                    .testTag("badge_double_crop_chance")
                            ) {
                                Text(
                                    text = "+${totalDoubleCropChance}% Double Crops ⓘ",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            if (botanicalFloraPercent > 0) {
                                Surface(
                                    color = Color(0xFF1B5E20).copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFA5D6A7))
                                ) {
                                    Text(
                                        text = "🌿 $botanicalFloraPercent%",
                                        color = Color(0xFFA5D6A7),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Row 2: Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showSeedMerchantDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .testTag("button_seed_shop")
                        ) {
                            Text("🏪 Seeds", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (isAfkFarmingActive) {
                                    onToggleAfkFarming(null)
                                } else {
                                    showAfkCategoryDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAfkFarmingActive) Color(0xFF2E6B38) else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(28.dp)
                                .testTag("button_afk_farm")
                        ) {
                            Text(
                                if (isAfkFarmingActive) "⚡ AFK (${afkSeedCategory.icon})" else "⚡ AFK OFF",
                                color = OsrsTextWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

        // --- COMPOST BIN SECTION CARD ---
        WoodPlankPanel(
            modifier = Modifier.fillMaxWidth(),
            borderColor = Color(0xFF8D6E63),
            accentIcon = "🟤",
            contentPadding = PaddingValues(8.dp)
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
                    Text("🟤", fontSize = 22.sp)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Compost Bin",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Surface(
                                color = if (pofState.compostBucketsCount > 0) Color(0xFF4E342E) else Color(0xFFB71C1C),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${pofState.compostBucketsCount} Buckets",
                                    color = OsrsTextWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Converts crops & produce from Bag + Bank into compost buckets (1 per plot).",
                            color = OsrsParchment,
                            fontSize = 9.5.sp
                        )
                    }
                }

                Button(
                    onClick = { showCompostDepositDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("button_add_compost")
                ) {
                    Text("➕ Make Compost", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- MAIN FARM PLOTS (PLOTS #1 - #4: HERBS & VEGETABLES ONLY) ---
        Column(modifier = Modifier.padding(start = 2.dp)) {
            Text(
                text = "🌱 Allotment & Herb Patches (Plots #1 - #4)",
                color = OsrsTextYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "Dedicated for Herbs & Vegetables only",
                color = Color(0xFF81C784),
                fontSize = 10.sp
            )
        }

        val mainPlots = pofState.plots.take(4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mainPlots.getOrNull(0)?.let { plot ->
                    Box(modifier = Modifier.weight(1f)) {
                        FarmPlotCard(
                            plot = plot,
                            currentTimeMs = currentTimeMs,
                            farmingLevel = farmingLevel,
                            isAfkFarmingActive = isAfkFarmingActive,
                            afkSeedCategory = afkSeedCategory,
                            onPlantClick = { selectedPlotIndexForPlanting = plot.plotIndex },
                            onWaterClick = { onWaterPlot(plot.plotIndex) },
                            onCompostClick = { onCompostPlot(plot.plotIndex) },
                            onHarvestClick = { onHarvestPlot(plot.plotIndex) },
                            onClearClick = { onClearPlot(plot.plotIndex) }
                        )
                    }
                }
                mainPlots.getOrNull(1)?.let { plot ->
                    Box(modifier = Modifier.weight(1f)) {
                        FarmPlotCard(
                            plot = plot,
                            currentTimeMs = currentTimeMs,
                            farmingLevel = farmingLevel,
                            isAfkFarmingActive = isAfkFarmingActive,
                            afkSeedCategory = afkSeedCategory,
                            onPlantClick = { selectedPlotIndexForPlanting = plot.plotIndex },
                            onWaterClick = { onWaterPlot(plot.plotIndex) },
                            onCompostClick = { onCompostPlot(plot.plotIndex) },
                            onHarvestClick = { onHarvestPlot(plot.plotIndex) },
                            onClearClick = { onClearPlot(plot.plotIndex) }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mainPlots.getOrNull(2)?.let { plot ->
                    Box(modifier = Modifier.weight(1f)) {
                        FarmPlotCard(
                            plot = plot,
                            currentTimeMs = currentTimeMs,
                            farmingLevel = farmingLevel,
                            isAfkFarmingActive = isAfkFarmingActive,
                            afkSeedCategory = afkSeedCategory,
                            onPlantClick = { selectedPlotIndexForPlanting = plot.plotIndex },
                            onWaterClick = { onWaterPlot(plot.plotIndex) },
                            onCompostClick = { onCompostPlot(plot.plotIndex) },
                            onHarvestClick = { onHarvestPlot(plot.plotIndex) },
                            onClearClick = { onClearPlot(plot.plotIndex) }
                        )
                    }
                }
                mainPlots.getOrNull(3)?.let { plot ->
                    Box(modifier = Modifier.weight(1f)) {
                        FarmPlotCard(
                            plot = plot,
                            currentTimeMs = currentTimeMs,
                            farmingLevel = farmingLevel,
                            isAfkFarmingActive = isAfkFarmingActive,
                            afkSeedCategory = afkSeedCategory,
                            onPlantClick = { selectedPlotIndexForPlanting = plot.plotIndex },
                            onWaterClick = { onWaterPlot(plot.plotIndex) },
                            onCompostClick = { onCompostPlot(plot.plotIndex) },
                            onHarvestClick = { onHarvestPlot(plot.plotIndex) },
                            onClearClick = { onClearPlot(plot.plotIndex) }
                        )
                    }
                }
            }
        }

        // --- TREE ORCHARD PATCHES (UNLOCKED AT LEVEL 25 AGRICULTURE - PLOTS #9 & #10) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (farmingLevel >= 25) Color(0xFF1E2E1B) else Color(0xFF1F1A15)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (farmingLevel >= 25) Color(0xFF81C784) else Color.DarkGray
            )
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌳", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Tree Orchard Patches (Plots #9 - #12)",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (farmingLevel >= 25) "Level 25 Unlocked • Trees & Fruit Trees Only (4 Patches)" else "Requires Level 25 Agriculture to unlock",
                                color = if (farmingLevel >= 25) Color(0xFF81C784) else Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (farmingLevel < 25) {
                        Surface(
                            color = Color(0xFF3E2723),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "🔒 Lvl 25 Req",
                                color = OsrsGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (farmingLevel >= 25) {
                    HorizontalDivider(color = Color(0xFF388E3C).copy(alpha = 0.5f))

                    Text(
                        text = "🌳 Tree & Fruit Tree seeds take hours to grow, yielding bountiful timber bark and fresh fruit alongside massive Agriculture XP drops!",
                        color = OsrsParchment,
                        fontSize = 10.5.sp
                    )

                    val treePlots = listOfNotNull(
                        pofState.plots.getOrNull(8),
                        pofState.plots.getOrNull(9),
                        pofState.plots.getOrNull(10),
                        pofState.plots.getOrNull(11)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        treePlots.chunked(2).forEach { rowPlots ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPlots.forEach { plot ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        FarmPlotCard(
                                            plot = plot,
                                            currentTimeMs = currentTimeMs,
                                            farmingLevel = farmingLevel,
                                            isAfkFarmingActive = isAfkFarmingActive,
                                            afkSeedCategory = afkSeedCategory,
                                            onPlantClick = { selectedPlotIndexForPlanting = plot.plotIndex },
                                            onWaterClick = { onWaterPlot(plot.plotIndex) },
                                            onCompostClick = { onCompostPlot(plot.plotIndex) },
                                            onHarvestClick = { onHarvestPlot(plot.plotIndex) },
                                            onClearClick = { onClearPlot(plot.plotIndex) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFF2C1E18),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Tree Orchard Patches (Plots #9 & #10)",
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Requires Level 25 Agriculture to plant tree and fruit tree seeds!",
                                    color = Color.Gray,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        // --- ANIMAL HUSBANDRY SECTION (UNLOCKED AT LEVEL 65 AGRICULTURE, 40 BEAST TAMING, 50 HUT-KEEPING) ---
        val isHusbandryUnlocked = farmingLevel >= 65 && hunterLevel >= 40 && constructionLevel >= 50
        val maxLivestockCap = calculateMaxLivestockCapacity(farmingLevel)
        val husbandry = pofState.husbandryState

        Card(
            modifier = Modifier.fillMaxWidth().testTag("animal_husbandry_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (isHusbandryUnlocked) Color(0xFF241C14) else Color(0xFF1E1712)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isHusbandryUnlocked) Color(0xFFD7A15C) else Color(0xFF4E3629)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏡", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "Animal Husbandry",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isHusbandryUnlocked) {
                                    "Barn & Pen Active • Cap: ${husbandry.animals.size}/$maxLivestockCap animals"
                                } else {
                                    "Requires 65 Agriculture, 40 Beast Taming & 50 Hut-Keeping"
                                },
                                color = if (isHusbandryUnlocked) Color(0xFF81C784) else Color.LightGray,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    if (!isHusbandryUnlocked) {
                        Surface(
                            color = Color(0xFF3E2723),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "🔒 Requirements",
                                color = OsrsGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFF2E6B38),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${husbandry.animals.size}/$maxLivestockCap Animals",
                                color = OsrsTextWhite,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (!isHusbandryUnlocked) {
                    // Requirements Checklist
                    HorizontalDivider(color = Color(0xFF4E3629))
                    Text(
                        text = "Complete the skill master requirements to establish your farm barn & pasture:",
                        color = OsrsParchment,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Agriculture
                        val agriDone = farmingLevel >= 65
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (agriDone) Color(0xFF1B381E) else Color(0xFF2A1C14)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (agriDone) Color(0xFF81C784) else Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (agriDone) "✅" else "🌾", fontSize = 14.sp)
                                Text("65 Agriculture", color = if (agriDone) Color(0xFF81C784) else Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                Text("Lv. $farmingLevel / 65", color = Color.Gray, fontSize = 8.5.sp)
                            }
                        }

                        // Beast Taming
                        val beastDone = hunterLevel >= 40
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (beastDone) Color(0xFF1B381E) else Color(0xFF2A1C14)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (beastDone) Color(0xFF81C784) else Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (beastDone) "✅" else "🦅", fontSize = 14.sp)
                                Text("40 Beast Taming", color = if (beastDone) Color(0xFF81C784) else Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                Text("Lv. $hunterLevel / 40", color = Color.Gray, fontSize = 8.5.sp)
                            }
                        }

                        // Hut-Keeping
                        val hutDone = constructionLevel >= 50
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (hutDone) Color(0xFF1B381E) else Color(0xFF2A1C14)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (hutDone) Color(0xFF81C784) else Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (hutDone) "✅" else "🔨", fontSize = 14.sp)
                                Text("50 Hut-Keeping", color = if (hutDone) Color(0xFF81C784) else Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                Text("Lv. $constructionLevel / 50", color = Color.Gray, fontSize = 8.5.sp)
                            }
                        }
                    }
                } else {
                    // --- UNLOCKED ANIMAL HUSBANDRY VIEW ---
                    HorizontalDivider(color = Color(0xFF6D4C41).copy(alpha = 0.6f))

                    // 1. GENERATED PEN BACKGROUND WITH ACTIVE LIVESTOCK
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color(0xFF8D6E63), RoundedCornerShape(8.dp))
                    ) {
                        // Generated Background Image
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_animal_husbandry_pen),
                            contentDescription = "Animal Husbandry Livestock Pen",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark Gradient Tint for Legibility
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                        )

                        // Top info badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7A15C))
                            ) {
                                Text(
                                    text = "🌾 Livestock Pen • ${husbandry.animals.size}/$maxLivestockCap Animals",
                                    color = OsrsTextYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                color = if (husbandry.troughFoodPct > 100) Color(0xFF1B5E20).copy(alpha = 0.85f) else Color(0xFFB71C1C).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = if (husbandry.troughFoodPct > 0) "🟢 Producing" else "🔴 Trough Empty",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Floating Animals inside Pen
                        if (husbandry.animals.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🌾 Pen is empty! Visit The Barn below to purchase livestock.",
                                        color = OsrsParchment,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        } else {
                            // Row of active animals in pen
                            Row(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                husbandry.animals.take(5).forEach { animal ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                animalToRename = animal
                                                renameInputText = animal.customName
                                            }
                                            .padding(2.dp)
                                    ) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.65f),
                                            shape = CircleShape,
                                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD7A15C))
                                        ) {
                                            Text(
                                                text = animal.type.emoji,
                                                fontSize = 26.sp,
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        }
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.8f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = animal.customName,
                                                color = OsrsTextYellow,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Pen Bar: Trough Level & Actions
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌾 Feed Trough: ${husbandry.troughFoodPct}% / ${AnimalHusbandryState.MAX_TROUGH_CAPACITY}%",
                                    color = if (husbandry.troughFoodPct > 200) Color(0xFF81C784) else if (husbandry.troughFoodPct > 50) OsrsGold else Color(0xFFEF5350),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Consumes 1% per production tick",
                                    color = Color.LightGray,
                                    fontSize = 9.5.sp
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (husbandry.troughFoodPct.toFloat() / AnimalHusbandryState.MAX_TROUGH_CAPACITY.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (husbandry.troughFoodPct > 200) Color(0xFF4CAF50) else if (husbandry.troughFoodPct > 50) Color(0xFFFFA000) else Color(0xFFE53935),
                                trackColor = Color(0xFF37474F)
                            )
                        }
                    }

                    // Trough Feeding Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { showFeedTroughDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("feed_trough_button")
                        ) {
                            Text("🌾 Fill Trough with Crops", color = OsrsTextWhite, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }

                        val sloshInInv = inventoryItems.find { it.id == "item_trough_slosh" }?.quantity ?: 0
                        Button(
                            onClick = {
                                if (sloshInInv > 0) {
                                    onFeedTrough("item_trough_slosh", 1)
                                } else {
                                    showCraftSloshDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (sloshInInv > 0) Color(0xFF2E7D32) else Color(0xFF4E342E)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("feed_slosh_button")
                        ) {
                            Text(
                                text = if (sloshInInv > 0) "🍲 Feed Slosh ($sloshInInv left)" else "🍲 Mash Trough Slosh",
                                color = OsrsTextYellow,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 2. THE BARN (BUY LIVESTOCK & MANAGE HERD)
                    Text(
                        text = "🏡 The Barn (Livestock Market)",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )

                    // Capacity indicator: Starts at 5, +1 every 10 levels, max 10 at Lv 99
                    Text(
                        text = "Pen Capacity: ${husbandry.animals.size} / $maxLivestockCap (Starts at 5, increases every 10 levels to 10 at Lv 99).",
                        color = OsrsParchment,
                        fontSize = 10.5.sp
                    )

                    // Animals for sale list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LivestockType.entries.forEach { type ->
                            val hasLevel = farmingLevel >= type.reqFarmingLevel
                            val hasCoins = coinsGp >= type.buyCostGp
                            val hasSpace = husbandry.animals.size < maxLivestockCap
                            val canBuy = hasLevel && hasCoins && hasSpace

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (hasLevel) OsrsLeatherMedium else Color(0xFF1E1712)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (hasLevel) Color(0xFF8D6E63) else Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(type.emoji, fontSize = 22.sp)
                                        Column {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(type.speciesName, color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                                Surface(
                                                    color = if (hasLevel) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                                                    shape = RoundedCornerShape(3.dp)
                                                ) {
                                                    Text(
                                                        text = "Lv. ${type.reqFarmingLevel}",
                                                        color = Color.White,
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Produces: ${type.produceEmoji} ${type.produceItemName} + 💩 Compost",
                                                color = Color(0xFF81C784),
                                                fontSize = 9.5.sp
                                            )
                                            Text(
                                                text = type.description,
                                                color = Color.Gray,
                                                fontSize = 8.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onBuyLivestock(type) },
                                        enabled = canBuy,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF388E3C),
                                            disabledContainerColor = Color(0xFF3E2723)
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp).testTag("buy_${type.id}_button")
                                    ) {
                                        Text(
                                            text = "${type.buyCostGp} GP",
                                            color = if (canBuy) OsrsGold else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. YOUR HERD ROSTER (RENAME & MANAGE)
                    if (husbandry.animals.isNotEmpty()) {
                        HorizontalDivider(color = Color(0xFF6D4C41).copy(alpha = 0.6f))
                        Text(
                            text = "🐾 Farm Animals in Pen (Tap to Rename)",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            husbandry.animals.forEach { animal ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2219)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5D4037)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(animal.type.emoji, fontSize = 18.sp)
                                            Column {
                                                Text(
                                                    text = animal.customName,
                                                    color = OsrsTextYellow,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "${animal.type.speciesName} • Produces ${animal.type.produceItemName}",
                                                    color = Color.LightGray,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    animalToRename = animal
                                                    renameInputText = animal.customName
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("✏️", fontSize = 12.sp)
                                            }

                                            IconButton(
                                                onClick = { onSellLivestock(animal.instanceId) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("❌", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. TROUGH SLOSH MASH RECIPE STATION
                    HorizontalDivider(color = Color(0xFF6D4C41).copy(alpha = 0.6f))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F291C)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
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
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🍲", fontSize = 20.sp)
                                    Column {
                                        Text("Trough Slosh Mash Pot", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        Text("Hearty Livestock Feed (+150% Trough Hunger)", color = Color(0xFF81C784), fontSize = 9.5.sp)
                                    }
                                }

                                Surface(
                                    color = if (isQuest3Completed) Color(0xFF2E6B38) else Color(0xFF3E2723),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isQuest3Completed) "✅ Unlocked" else "🔒 Quest #3 Req",
                                        color = if (isQuest3Completed) Color.White else OsrsGold,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!isQuest3Completed) {
                                Text(
                                    text = "🔒 Recipe Locked: Complete Quest #3 'Wildland Chieftain Reconciliation' to learn the secret of brewing Trough Slosh!",
                                    color = OsrsParchment,
                                    fontSize = 10.sp
                                )
                            } else {
                                Text(
                                    text = "Combine ANY two items from your backpack to brew hearty Trough Slosh for your livestock trough.",
                                    color = OsrsParchment,
                                    fontSize = 10.sp
                                )

                                Button(
                                    onClick = { showCraftSloshDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(34.dp).testTag("brew_trough_slosh_button")
                                ) {
                                    Text("🍲 Brew Trough Slosh (Combine Any 2 Items)", color = OsrsTextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 5. BARN COLLECTION CHEST (UNDERNEATH)
                    HorizontalDivider(color = Color(0xFF6D4C41).copy(alpha = 0.6f))
                    val totalProduceCount = husbandry.storedProduce.values.sum()
                    val totalCompostCount = husbandry.storedCompost
                    val hasMaterialsToCollect = totalProduceCount > 0 || totalCompostCount > 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2117)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD7A15C)),
                        modifier = Modifier.fillMaxWidth().testTag("barn_collection_chest_card")
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
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("📦", fontSize = 22.sp)
                                    Column {
                                        Text("Barn Collection Chest", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Check & withdraw materials produced by your livestock", color = OsrsParchment, fontSize = 9.5.sp)
                                    }
                                }

                                Surface(
                                    color = if (hasMaterialsToCollect) Color(0xFF2E6B38) else Color(0xFF37474F),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (hasMaterialsToCollect) "📦 $totalProduceCount items + $totalCompostCount compost" else "Empty",
                                        color = OsrsTextWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!hasMaterialsToCollect) {
                                Text(
                                    text = "The chest is currently empty. While the pen trough has food, animals will steadily deposit their harvested materials and rich compost here.",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            } else {
                                // List of stored items
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    husbandry.storedProduce.forEach { (itemId, qty) ->
                                        if (qty > 0) {
                                            val def = DefaultItems.getItemById(itemId)
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(6.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8D6E63))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(def.iconEmoji, fontSize = 14.sp)
                                                    Text("${def.name}: x$qty", color = OsrsTextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    if (totalCompostCount > 0) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8D6E63))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("💩", fontSize = 14.sp)
                                                Text("Compost: x$totalCompostCount", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = onWithdrawHusbandryChest,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7A15C)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("withdraw_chest_button")
                                ) {
                                    Text("📦 Withdraw All Materials to Backpack", color = Color.Black, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }


        // --- CONSTRUCTION FARM PATCHES (UNLOCKED AT LEVEL 50 & 75 CONSTRUCTION) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (constructionLevel >= 50) Color(0xFF2E231B) else Color(0xFF1F1A15)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (constructionLevel >= 50) OsrsGold else Color.DarkGray
            )
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪵", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Construction Farm Patches",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Build POH & Estate Farm Patches with Construction",
                                color = OsrsParchment,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF3E2723),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Lvl 50 & 75 Con",
                            color = OsrsGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(color = OsrsGold.copy(alpha = 0.3f))

                // Patch #1 (Lvl 50 Construction - Plot #7 / index 6)
                if (constructionLevel >= 50) {
                    Text(
                        text = "🪴 Construction Farm Patch #1 (Plot #7) - Unlocked at Level 50 Construction",
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                    pofState.plots.getOrNull(6)?.let { plot ->
                        FarmPlotCard(
                            plot = plot,
                            currentTimeMs = currentTimeMs,
                            farmingLevel = farmingLevel,
                            isAfkFarmingActive = isAfkFarmingActive,
                            afkSeedCategory = afkSeedCategory,
                            onPlantClick = { selectedPlotIndexForPlanting = plot.plotIndex },
                            onWaterClick = { onWaterPlot(plot.plotIndex) },
                            onCompostClick = { onCompostPlot(plot.plotIndex) },
                            onHarvestClick = { onHarvestPlot(plot.plotIndex) },
                            onClearClick = { onClearPlot(plot.plotIndex) }
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFF2C1E18),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Construction Farm Patch #1 (Plot #7)",
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Requires Level 50 Construction to build this estate farm patch!",
                                    color = Color.Gray,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }

                // Patch #2 (Lvl 75 Construction - Plot #8 / index 7)
                if (constructionLevel >= 75) {
                    Text(
                        text = "🏡 Construction Farm Patch #2 (Plot #8) - Unlocked at Level 75 Construction",
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                    pofState.plots.getOrNull(7)?.let { plot ->
                        FarmPlotCard(
                            plot = plot,
                            currentTimeMs = currentTimeMs,
                            farmingLevel = farmingLevel,
                            isAfkFarmingActive = isAfkFarmingActive,
                            afkSeedCategory = afkSeedCategory,
                            onPlantClick = { selectedPlotIndexForPlanting = plot.plotIndex },
                            onWaterClick = { onWaterPlot(plot.plotIndex) },
                            onCompostClick = { onCompostPlot(plot.plotIndex) },
                            onHarvestClick = { onHarvestPlot(plot.plotIndex) },
                            onClearClick = { onClearPlot(plot.plotIndex) }
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFF2C1E18),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Construction Farm Patch #2 (Plot #8)",
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Requires Level 75 Construction to build this master estate farm patch!",
                                    color = Color.Gray,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // --- SEED PLANTING SELECTION DIALOG (GROUPED BY CATEGORY) ---
    selectedPlotIndexForPlanting?.let { plotIdx ->
        val allowedCategories = com.example.data.models.getAllowedSeedCategoriesForPlot(plotIdx)
        var selectedCategoryFilter by remember(plotIdx) {
            mutableStateOf(allowedCategories.first())
        }

        val plotTypeDescription = when (plotIdx) {
            in 0..3 -> "Allotment & Herb Patch (Plots #1 - #4: Herbs & Vegetables only)"
            in 8..11 -> "Tree Orchard Patch (Plots #9 - #12: Trees & Fruit Trees only)"
            4, 5 -> "Farming Guild Patch (Plots #5 - #6: All Crops & Trees)"
            6, 7 -> "Construction Estate Patch (Plots #7 - #8: All Crops & Trees)"
            else -> "Select seed type: Vegetable, Herb, Tree, or Fruit Tree"
        }

        AlertDialog(
            onDismissRequest = { selectedPlotIndexForPlanting = null },
            containerColor = OsrsLeatherDark,
            title = {
                Column {
                    Text(
                        text = if (plotIdx in 8..11) "🌳 Choose Seed for Plot #${plotIdx + 1}" else "🌱 Choose Seed for Plot #${plotIdx + 1}",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = plotTypeDescription,
                        color = Color(0xFF81C784),
                        fontSize = 10.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Category Selection Chips (filtered for allowed categories)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allowedCategories.forEach { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) Color(0xFF2E6B38) else OsrsLeatherMedium,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OsrsGold else Color.Gray),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategoryFilter = cat }
                            ) {
                                Text(
                                    text = "${cat.icon} ${cat.displayName}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OsrsTextYellow else OsrsParchment,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                        }
                    }

                    val availableSeeds = FarmCropType.entries.filter {
                        com.example.data.models.isCropAllowedInPlot(plotIdx, it) &&
                        it.category == selectedCategoryFilter
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    ) {
                        items(availableSeeds) { crop ->
                            val invSeedCount = inventoryItems.find { it.id == crop.seedId }?.quantity ?: 0
                            val bankSeedCount = bankItems.find { it.id == crop.seedId }?.quantity ?: 0
                            val totalSeedCount = invSeedCount + bankSeedCount
                            val meetsLevel = farmingLevel >= crop.reqFarmingLevel

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (totalSeedCount > 0 && meetsLevel) Color(0xFF23351C) else Color(0xFF1F1A15)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (totalSeedCount > 0 && meetsLevel) Color(0xFF4CAF50) else Color.DarkGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = totalSeedCount > 0 && meetsLevel) {
                                        onPlantSeed(plotIdx, crop)
                                        selectedPlotIndexForPlanting = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(crop.seedEmoji, fontSize = 22.sp)
                                        Column {
                                            Text(
                                                text = crop.displayName,
                                                color = if (meetsLevel) OsrsTextWhite else Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Req Lvl ${crop.reqFarmingLevel} | Grow: ${com.example.data.models.formatGrowthDuration(crop.growthTimeSeconds)} | Yield: x${crop.produceQty} ${crop.produceEmoji}",
                                                color = OsrsParchment,
                                                fontSize = 9.5.sp
                                            )
                                            val seedGrowthChance = com.example.data.models.calculateCropGrowthChance(crop, farmingLevel)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "+${crop.farmingXp} Agriculture XP",
                                                    color = Color(0xFF81C784),
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "🌱 $seedGrowthChance% Grow Chance",
                                                    color = if (seedGrowthChance >= 85) Color(0xFFA5D6A7) else Color(0xFFFFF59D),
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        color = if (totalSeedCount > 0) Color(0xFF2E6B38) else Color.DarkGray,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (totalSeedCount > 0) {
                                                if (bankSeedCount > 0 && invSeedCount > 0) "Bag: $invSeedCount | Bank: $bankSeedCount"
                                                else if (invSeedCount > 0) "Bag: $invSeedCount"
                                                else "Bank: $bankSeedCount"
                                            } else "0 Owned",
                                            color = OsrsTextWhite,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPlotIndexForPlanting = null }) {
                    Text("Close", color = OsrsTextYellow)
                }
            }
        )
    }

    // --- SEED MERCHANT DIALOG ---
    if (showSeedMerchantDialog) {
        AlertDialog(
            onDismissRequest = { showSeedMerchantDialog = false },
            containerColor = OsrsLeatherDark,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏪 Farming Seed Merchant", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("🪙 ${coinsGp} GP", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
                ) {
                    items(FarmCropType.entries) { crop ->
                        val defaultSeedItem = DefaultItems.ALL.find { it.id == crop.seedId } ?: return@items
                        val meetsLevel = farmingLevel >= crop.reqFarmingLevel

                        Card(
                            colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3828)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(crop.seedEmoji, fontSize = 20.sp)
                                    Column {
                                        Text(crop.seedName, color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        Text("Cost: ${defaultSeedItem.costGp} GP | Lvl ${crop.reqFarmingLevel}", color = OsrsGold, fontSize = 10.sp)
                                    }
                                }

                                Button(
                                    onClick = { onBuySeed(defaultSeedItem, 1) },
                                    enabled = coinsGp >= defaultSeedItem.costGp && meetsLevel,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B38)),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Buy 1x", color = OsrsTextWhite, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSeedMerchantDialog = false }) {
                    Text("Close", color = OsrsTextYellow)
                }
            }
        )
    }

    // --- AFK SEED CATEGORY SELECTION DIALOG ---
    if (showAfkCategoryDialog) {
        Dialog(
            onDismissRequest = { showAfkCategoryDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = OsrsLeatherDark),
                border = androidx.compose.foundation.BorderStroke(2.dp, OsrsGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🚜", fontSize = 20.sp)
                            Text(
                                "Select AFK Farming Category",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(
                            onClick = { showAfkCategoryDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("✕", color = OsrsParchment, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Helper Info Box
                    Surface(
                        color = Color(0xFF1E1711),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3828)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("ℹ️", fontSize = 14.sp)
                            Text(
                                text = "The Farm Helper auto-plants empty patches using seeds & compost from your Bag (5 GP fee per patch).",
                                color = OsrsParchment,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    // Categories List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SeedCategory.entries.forEach { category ->
                            val matchingSeedsCount = inventoryItems
                                .filter { inv ->
                                    val crop = FarmCropType.entries.find { it.seedId == inv.id }
                                    crop != null && (category == SeedCategory.ALL || crop.category == category)
                                }
                                .sumOf { it.quantity }

                            val isCurrentlySelected = isAfkFarmingActive && afkSeedCategory == category

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrentlySelected) Color(0xFF2E3E26) else OsrsLeatherMedium
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrentlySelected) Color(0xFF81C784) else if (matchingSeedsCount > 0) OsrsGold else Color(0xFF5D4037)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onToggleAfkFarming(category)
                                        showAfkCategoryDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFF1E1711),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3828)),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(category.icon, fontSize = 20.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = category.displayName,
                                                color = if (isCurrentlySelected) Color(0xFF81C784) else OsrsTextYellow,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (isCurrentlySelected) {
                                                Surface(
                                                    color = Color(0xFF1B5E20),
                                                    shape = RoundedCornerShape(3.dp)
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        color = Color(0xFFC8E6C9),
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = when (category) {
                                                SeedCategory.VEGETABLE -> "Potatoes, Onions, Sweetcorn, Watermelons..."
                                                SeedCategory.HERB -> "Sunleaf, Mystic Sage, Vervain..."
                                                SeedCategory.TREE -> "Oak, Willow, Maple, Yew, Magic, Redwood, Spirit..."
                                                SeedCategory.FRUIT_TREE -> "Cherry, Peach, Apricot, Apple, Palm, Coconut..."
                                                SeedCategory.ALL -> "Any available seeds in your inventory"
                                            },
                                            color = OsrsParchment.copy(alpha = 0.85f),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        color = if (matchingSeedsCount > 0) Color(0xFF2E6B38) else Color(0xFF37474F),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (matchingSeedsCount > 0) Color(0xFF81C784) else Color.Transparent
                                        )
                                    ) {
                                        Text(
                                            text = if (matchingSeedsCount > 0) "🌱 $matchingSeedsCount" else "0 Seeds",
                                            color = OsrsTextWhite,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Close Button
                    Button(
                        onClick = { showAfkCategoryDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsLeatherMedium),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5D4037)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        Text("Close", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- COMPOST BIN DEPOSIT DIALOG (INVENTORY + BANK ACCESS) ---
    if (showCompostDepositDialog) {
        val compostableItems = remember(inventoryItems, bankItems) {
            val map = linkedMapOf<String, Triple<InventoryItem, Int, Int>>() // id -> (item, invQty, bankQty)
            for (item in inventoryItems) {
                if (item.quantity > 0 && isCompostableItem(item)) {
                    val existing = map[item.id]
                    val bQty = existing?.third ?: 0
                    map[item.id] = Triple(item, item.quantity, bQty)
                }
            }
            for (item in bankItems) {
                if (item.quantity > 0 && isCompostableItem(item)) {
                    val existing = map[item.id]
                    val iQty = existing?.second ?: 0
                    val base = existing?.first ?: item
                    map[item.id] = Triple(base, iQty, item.quantity)
                }
            }
            map.values.toList().sortedByDescending { it.second + it.third }
        }

        val totalCompostableAvailable = remember(compostableItems) {
            compostableItems.sumOf { it.second + it.third }
        }

        Dialog(
            onDismissRequest = { showCompostDepositDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = OsrsLeatherDark),
                border = androidx.compose.foundation.BorderStroke(2.dp, OsrsGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .wrapContentHeight()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🟤", fontSize = 20.sp)
                            Text(
                                "Compost Bin Deposit",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = Color(0xFF1B382B),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                            ) {
                                Text(
                                    text = "🪣 ${pofState.compostBucketsCount} Buckets",
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            IconButton(
                                onClick = { showCompostDepositDialog = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("✕", color = OsrsParchment, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Explanation Banner
                    Surface(
                        color = Color(0xFF1E1711),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3828)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Decompose spare crops, herbs, and produce into compost buckets for +2 crop yield on every patch harvest.",
                            color = OsrsParchment,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    if (compostableItems.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 12.dp)
                        ) {
                            Text("🌾", fontSize = 36.sp)
                            Text(
                                text = "No Compostable Produce Found",
                                color = OsrsTextYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "You don't have any crops, vegetables, herbs, or logs in your Bag or Bank.\nHarvest crops from your plots or collect herbs to fill your Compost Bin!",
                                color = OsrsParchment.copy(alpha = 0.85f),
                                fontSize = 10.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    } else {
                        // Deposit All Button
                        if (onDepositAllCompostable != null && totalCompostableAvailable > 0) {
                            Button(
                                onClick = {
                                    onDepositAllCompostable()
                                    showCompostDepositDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B38)),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = "✨ Deposit All Produce from Bag + Bank ($totalCompostableAvailable items)",
                                    color = OsrsTextYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp)
                        ) {
                            items(compostableItems) { (item, invQty, bankQty) ->
                                val totalQty = invQty + bankQty
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5D4037)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Row 1: Item Icon + Name + Location Badges
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Color(0xFF1E1711),
                                                shape = RoundedCornerShape(6.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3828)),
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(item.iconEmoji, fontSize = 20.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = item.name,
                                                color = OsrsTextWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = Color(0xFF1A237E).copy(alpha = 0.7f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "🏦 $totalQty",
                                                        color = Color(0xFF90CAF9),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Row 2: Deposit Action Buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Color(0xFF5D4037),
                                                shape = RoundedCornerShape(4.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8D6E63)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(30.dp)
                                                    .clickable { onDepositToCompostBin(item.id, 1) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("Deposit 1", color = OsrsTextYellow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            if (totalQty >= 10) {
                                                Surface(
                                                    color = Color(0xFF4E342E),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6D4C41)),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(30.dp)
                                                        .clickable { onDepositToCompostBin(item.id, 10) }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text("Deposit 10", color = OsrsTextYellow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Surface(
                                                color = Color(0xFF2E6B38),
                                                shape = RoundedCornerShape(4.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .height(30.dp)
                                                    .clickable { onDepositToCompostBin(item.id, totalQty) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("Deposit All ($totalQty)", color = Color(0xFFFFD700), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Dismiss Button
                    Button(
                        onClick = { showCompostDepositDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsLeatherMedium),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5D4037)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        Text("Close", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- ANIMAL HUSBANDRY MODALS & DIALOGS ---

    // 1. Rename Animal Dialog
    if (animalToRename != null) {
        val target = animalToRename!!
        Dialog(onDismissRequest = { animalToRename = null }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = OsrsLeatherDark),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD7A15C)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("✏️ Name Your ${target.type.speciesName}", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Give a custom name to this animal residing in your farm pen:", color = OsrsParchment, fontSize = 11.sp)

                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it.take(24) },
                        label = { Text("Animal Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OsrsGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { animalToRename = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.LightGray)
                        }

                        Button(
                            onClick = {
                                if (renameInputText.trim().isNotEmpty()) {
                                    onRenameLivestock(target.instanceId, renameInputText.trim())
                                }
                                animalToRename = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B38)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Name", color = OsrsTextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. Feed Trough Dialog (Compact & Space-Efficient)
    if (showFeedTroughDialog) {
        val foodCandidates = (inventoryItems + bankItems)
            .filter { it.quantity > 0 && (it.category == ItemCategory.FOOD || it.restoreHunger > 0 || it.id == "item_trough_slosh") }
            .distinctBy { it.id }

        Dialog(onDismissRequest = { showFeedTroughDialog = false }) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = OsrsLeatherDark),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8D6E63)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Bar with Compact Close Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌾", fontSize = 16.sp)
                            Column {
                                Text("Feed Livestock Trough", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Max capacity: ${AnimalHusbandryState.MAX_TROUGH_CAPACITY}%", color = Color(0xFF81C784), fontSize = 9.sp)
                            }
                        }
                        IconButton(
                            onClick = { showFeedTroughDialog = false },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Text("✕", color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (foodCandidates.isEmpty()) {
                        Surface(
                            color = Color(0xFF231A14),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Text(
                                "No suitable crops or feed found in backpack/bank!",
                                color = Color.LightGray,
                                fontSize = 10.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        // Quick Max-Feed Action
                        Button(
                            onClick = {
                                onDepositAllCrops()
                                showFeedTroughDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(5.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .testTag("deposit_all_crops_trough")
                        ) {
                            Text("⚡ Deposit All Available Crops (Quick Fill)", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Compact Crop List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(foodCandidates) { item ->
                                val gain = if (item.id == "item_trough_slosh") 150 else if (item.restoreHunger > 0) maxOf(35, item.restoreHunger) else 35
                                Surface(
                                    color = OsrsLeatherMedium,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5D4037)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left Item Info
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(item.iconEmoji, fontSize = 16.sp)
                                            Column {
                                                Text(
                                                    item.name,
                                                    color = OsrsTextWhite,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "x${item.quantity}  •  +$gain%/ea",
                                                    color = Color(0xFF81C784),
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }

                                        // Right Compact Action Chips
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Color(0xFF4E342E),
                                                shape = RoundedCornerShape(4.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8D6E63).copy(alpha = 0.6f)),
                                                modifier = Modifier
                                                    .height(24.dp)
                                                    .clickable { onFeedTrough(item.id, 1) }
                                                    .testTag("deposit_1_${item.id}")
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.padding(horizontal = 6.dp)
                                                ) {
                                                    Text("+1", fontSize = 9.5.sp, color = OsrsTextWhite, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            if (item.quantity >= 5) {
                                                Surface(
                                                    color = Color(0xFF2E6B38),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f)),
                                                    modifier = Modifier
                                                        .height(24.dp)
                                                        .clickable { onFeedTrough(item.id, 5) }
                                                        .testTag("deposit_5_${item.id}")
                                                ) {
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier.padding(horizontal = 6.dp)
                                                    ) {
                                                        Text("+5", fontSize = 9.5.sp, color = Color(0xFFC8E6C9), fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                            }

                                            if (item.quantity > 1) {
                                                Surface(
                                                    color = Color(0xFF1B5E20),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                                                    modifier = Modifier
                                                        .height(24.dp)
                                                        .clickable { onFeedTrough(item.id, item.quantity) }
                                                        .testTag("deposit_all_${item.id}")
                                                ) {
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier.padding(horizontal = 6.dp)
                                                    ) {
                                                        Text("All (${item.quantity})", fontSize = 9.sp, color = OsrsTextYellow, fontWeight = FontWeight.Bold)
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
        }
    }

    // 3. Craft Trough Slosh Dialog
    if (showCraftSloshDialog) {
        val availableInvItems = inventoryItems.filter { it.quantity > 0 }
        var selectedItem1 by remember { mutableStateOf<InventoryItem?>(availableInvItems.firstOrNull()) }
        var selectedItem2 by remember { mutableStateOf<InventoryItem?>(availableInvItems.getOrNull(1) ?: availableInvItems.firstOrNull()) }

        Dialog(onDismissRequest = { showCraftSloshDialog = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = OsrsLeatherDark),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🍲 Mash Trough Slosh", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Combine any two items from your backpack to brew hearty Trough Slosh (+150% Trough Feed):", color = OsrsParchment, fontSize = 10.5.sp)

                    if (availableInvItems.isEmpty()) {
                        Text("⚠️ You need at least 2 items in your backpack to brew Trough Slosh!", color = Color(0xFFEF5350), fontSize = 11.sp)
                    } else {
                        // Item 1 Selector
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Ingredient 1: ${selectedItem1?.name ?: "None"} (x${selectedItem1?.quantity ?: 0})", color = OsrsTextWhite, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                availableInvItems.take(4).forEach { item ->
                                    Surface(
                                        color = if (selectedItem1?.id == item.id) Color(0xFF2E6B38) else Color(0xFF3E2723),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { selectedItem1 = item }
                                    ) {
                                        Text("${item.iconEmoji} ${item.name.take(8)}", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                        }

                        // Item 2 Selector
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Ingredient 2: ${selectedItem2?.name ?: "None"} (x${selectedItem2?.quantity ?: 0})", color = OsrsTextWhite, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                availableInvItems.take(4).forEach { item ->
                                    Surface(
                                        color = if (selectedItem2?.id == item.id) Color(0xFF2E6B38) else Color(0xFF3E2723),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { selectedItem2 = item }
                                    ) {
                                        Text("${item.iconEmoji} ${item.name.take(8)}", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                        }

                        val canBrew = selectedItem1 != null && selectedItem2 != null &&
                            (selectedItem1?.id != selectedItem2?.id || (selectedItem1?.quantity ?: 0) >= 2)

                        Button(
                            onClick = {
                                val item1 = selectedItem1
                                val item2 = selectedItem2
                                if (canBrew && item1 != null && item2 != null) {
                                    onCraftTroughSlosh(item1.id, item2.id)
                                    showCraftSloshDialog = false
                                }
                            },
                            enabled = canBrew,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("🍲 Brew 1x Trough Slosh (+150% Feed)", color = OsrsTextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showCraftSloshDialog = false },
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Text("Cancel", color = Color.LightGray)
                    }
                }
            }
        }
    }

    if (showBonusBreakdownDialog) {
        BonusBreakdownDialog(
            title = "Double Crop & Barn Drop Bonus",
            categoryName = "Player Owned Farm & Livestock",
            iconEmoji = "🌾",
            sources = listOf(
                BonusSourceDetail(
                    title = "Farmer Bryan's Favor (Lv. $bryanFavorLvl)",
                    description = "Grants +1% chance per favor level to harvest double crops from farm patches across your estate (Up to +50%).",
                    bonusPercent = bryanFavorLvl,
                    emoji = "👨‍🌾",
                    isUnlocked = true
                ),
                BonusSourceDetail(
                    title = "Quest: Siege of the Primate Fleet",
                    description = "Grandmaster Quest Perk: Grants a permanent +25% bonus chance to harvest double crops from plots and obtain double produce drops from livestock in your barn pen!",
                    bonusPercent = 25,
                    emoji = "🦍",
                    isUnlocked = isPrimateFleetCompleted,
                    unlockRequirement = "Complete Grandmaster Quest 'Siege of the Primate Fleet'"
                )
            ),
            note = "When triggered, harvested crops and barn animal produce are doubled! Both manual and AFK harvesting benefit from this perk.",
            onDismiss = { showBonusBreakdownDialog = false }
        )
    }

}

@Composable
fun FarmPlotCard(
    plot: FarmPlotState,
    currentTimeMs: Long,
    farmingLevel: Int,
    isAfkFarmingActive: Boolean = false,
    afkSeedCategory: SeedCategory = SeedCategory.ALL,
    onPlantClick: () -> Unit,
    onWaterClick: () -> Unit,
    onCompostClick: () -> Unit,
    onHarvestClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val isReady = remember(plot, currentTimeMs) { plot.isReadyToHarvest(currentTimeMs) }
    val remainingSec = remember(plot, currentTimeMs) { plot.remainingSeconds(currentTimeMs) }
    val rawProgress = remember(plot, currentTimeMs) { plot.progressFraction(currentTimeMs) }
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "crop_progress"
    )
    val growthChance = remember(plot, farmingLevel) { plot.growthChancePercent(farmingLevel) }
    val stage = remember(plot, currentTimeMs) { plot.getGrowthStage(currentTimeMs) }
    val stageVisualEmoji = remember(plot, currentTimeMs) { plot.getStageVisualEmoji(currentTimeMs) }

    // Pulsing animation for ready crops & AFK active indicator
    val infiniteTransition = rememberInfiniteTransition(label = "crop_anim")
    val readyPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ready_pulse"
    )
    val readyGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ready_glow"
    )

    val estimatedQty = remember(plot.cropType, farmingLevel, plot.isComposted) {
        if (plot.cropType != null) {
            val c = plot.cropType
            val bonus = if (plot.isComposted) 2 else 0
            if (c.category == SeedCategory.HERB) {
                (6 + ((farmingLevel - 1) * 8 / 98)).coerceIn(6, 14) + bonus
            } else if (c.category == SeedCategory.VEGETABLE) {
                (3 + (farmingLevel / 10)).coerceIn(3, 15) + bonus
            } else {
                c.produceQty + bonus
            }
        } else 0
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (plot.cropType != null) {
                if (isReady) Color(0xFF1B3D1E).copy(alpha = 0.95f) else OsrsLeatherMedium.copy(alpha = 0.92f)
            } else {
                if (isAfkFarmingActive) Color(0xFF1D2619).copy(alpha = 0.92f) else Color(0xFF1B140D).copy(alpha = 0.90f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isReady) Color(0xFF4CAF50).copy(alpha = readyGlowAlpha)
            else if (isAfkFarmingActive) Color(0xFF66BB6A)
            else OsrsGold
        ),
        modifier = Modifier.fillMaxWidth().heightIn(min = 205.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Plot Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Plot #${plot.plotIndex + 1}",
                        color = OsrsGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                    if (isAfkFarmingActive) {
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "⚡ AFK",
                                color = OsrsTextWhite,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                            )
                        }
                    }
                }

                if (plot.cropType != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (plot.isWatered) Text("💦", fontSize = 10.sp)
                        if (plot.isComposted) Text("🟤", fontSize = 10.sp)
                    }
                }
            }

            // Crop Content or Empty AFK Re-seeding State
            if (plot.cropType != null) {
                val crop = plot.cropType
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(
                        text = stageVisualEmoji,
                        fontSize = 28.sp,
                        modifier = Modifier.scale(if (isReady) readyPulseScale else 1.0f)
                    )
                    Text(
                        text = crop.displayName,
                        color = OsrsTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center
                    )

                    // Growth Stage Badge with Stage Name (e.g. Sown, Sprout, Vegetative, Flowering, Ripening, Ready)
                    Surface(
                        color = when {
                            isReady -> Color(0xFF1B5E20)
                            growthChance >= 85 -> Color(0xFF1B5E20)
                            growthChance >= 70 -> Color(0xFF33691E)
                            else -> Color(0xFFBF360C)
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = stage.stageName,
                            color = if (isReady) Color(0xFFDCE775) else if (growthChance >= 85) Color(0xFFA5D6A7) else Color(0xFFFFCC80),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (isReady) {
                        Text(
                            text = if (isAfkFarmingActive) "✨ Auto-Harvesting..." else "✨ READY TO HARVEST!",
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Ready in ${com.example.data.models.formatGrowthDuration(remainingSec)} (${(animatedProgress * 100).toInt()}%)",
                            color = OsrsParchment,
                            fontSize = 9.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = Color.DarkGray,
                        )
                    }
                }

                // Plot Actions
                if (isReady) {
                    Button(
                        onClick = onHarvestClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B38)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("button_harvest_plot_${plot.plotIndex}")
                    ) {
                        Text("🌾 Harvest ($estimatedQty x ${crop.produceEmoji})", color = OsrsTextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!plot.isWatered) {
                            Button(
                                onClick = onWaterClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f).height(28.dp)
                            ) {
                                Text("💦 Water", color = Color.White, fontSize = 9.sp)
                            }
                        }
                        if (!plot.isComposted) {
                            Button(
                                onClick = onCompostClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f).height(28.dp)
                            ) {
                                Text("🟤 Compost", color = Color.White, fontSize = 9.sp)
                            }
                        }
                        Button(
                            onClick = onClearClick,
                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("🗑️", color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            } else {
                // Empty Plot
                val emptyPlotLabel = when (plot.plotIndex) {
                    in 0..3 -> "Herbs & Veggies"
                    in 8..11 -> "Trees & Fruit Trees"
                    4, 5 -> "Guild Patch"
                    6, 7 -> "Estate Patch"
                    else -> "Soil Prepared"
                }
                val plantButtonLabel = when (plot.plotIndex) {
                    in 0..3 -> "🌱 Plant Herb/Veg"
                    in 8..11 -> "🌳 Plant Tree"
                    else -> "🌱 Plant Seed"
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Text(
                        text = if (plot.plotIndex in 8..11) "🌲" else if (isAfkFarmingActive) "🔄" else "🪴",
                        fontSize = 24.sp
                    )
                    Text(
                        text = if (isAfkFarmingActive) "Auto-Planting Next Seed..." else emptyPlotLabel,
                        color = if (isAfkFarmingActive) Color(0xFFDCE775) else Color(0xFF81C784),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        color = if (isAfkFarmingActive) Color(0xFF1B381E) else Color(0xFF1E3A20),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = if (isAfkFarmingActive)
                                "🚜 Sowing ${afkSeedCategory.displayName}..."
                            else
                                "🌱 Soil: $growthChance% Grow Chance",
                            color = Color(0xFFA5D6A7),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Button(
                    onClick = onPlantClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAfkFarmingActive) Color(0xFF33691E) else Color(0xFF2E6B38)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(32.dp).testTag("button_plant_plot_${plot.plotIndex}")
                ) {
                    Text(
                        if (isAfkFarmingActive) "⚡ Auto-Planting..." else plantButtonLabel,
                        color = OsrsTextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Procedural botanical background that dynamically fills with lush flora
 * proportional to the player's Farming level (0% at level 1, 100% at level 99).
 */
@Composable
fun FarmingDynamicBotanicalBackground(
    farmingLevel: Int,
    modifier: Modifier = Modifier
) {
    if (farmingLevel <= 1) {
        // At level 1 farming there won't be any plants visible
        return
    }

    val fillProgress = remember(farmingLevel) {
        ((farmingLevel - 1).toFloat() / 98f).coerceIn(0f, 1f)
    }

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val totalHeight = size.height
        if (totalWidth <= 0f || totalHeight <= 0f) return@Canvas

        // 72 deterministic botanical nodes distributed evenly across the screen
        val totalNodes = 72
        val activeNodes = (fillProgress * totalNodes).toInt().coerceIn(1, totalNodes)

        val greenSprout = Color(0x384CAF50)
        val greenLeaf = Color(0x4081C784)
        val greenDark = Color(0x482E7D32)
        val vineColor = Color(0x351B5E20)
        val goldWheat = Color(0x40FDD835)
        val flowerPink = Color(0x45E91E63)
        val flowerPurple = Color(0x45BA68C8)
        val flowerGold = Color(0x45FFA726)

        // Draw background botanical elements
        for (i in 0 until activeNodes) {
            // Pseudo-random deterministic placement based on node index
            val xRatio = (((i * 47) % 95) + 2.5f) / 100f
            val yRatio = (((i * 83) % 95) + 2.5f) / 100f
            val posX = xRatio * totalWidth
            val posY = yRatio * totalHeight
            val nodeType = i % 6

            when (nodeType) {
                0 -> {
                    // Two-leaf sprout shoot
                    val shootHeight = 16f + (i % 8) * 2f
                    drawLine(
                        color = greenDark,
                        start = Offset(posX, posY),
                        end = Offset(posX, posY - shootHeight),
                        strokeWidth = 2f
                    )
                    // Left leaf
                    drawOval(
                        color = greenSprout,
                        topLeft = Offset(posX - 8f, posY - shootHeight - 4f),
                        size = Size(8f, 5f)
                    )
                    // Right leaf
                    drawOval(
                        color = greenLeaf,
                        topLeft = Offset(posX, posY - shootHeight - 4f),
                        size = Size(8f, 5f)
                    )
                }
                1 -> {
                    // Curved climbing vine
                    val path = Path().apply {
                        moveTo(posX, posY)
                        quadraticBezierTo(posX + 10f, posY - 15f, posX - 4f, posY - 30f)
                    }
                    drawPath(
                        path = path,
                        color = vineColor,
                        style = Stroke(width = 2.5f)
                    )
                    drawCircle(
                        color = greenLeaf,
                        radius = 4.5f,
                        center = Offset(posX + 6f, posY - 12f)
                    )
                    drawCircle(
                        color = greenSprout,
                        radius = 4f,
                        center = Offset(posX - 4f, posY - 30f)
                    )
                }
                2 -> {
                    // Golden wheat blade
                    drawLine(
                        color = goldWheat,
                        start = Offset(posX, posY),
                        end = Offset(posX + 6f, posY - 22f),
                        strokeWidth = 2f
                    )
                    for (k in 0..3) {
                        val segY = posY - 8f - (k * 4f)
                        val segX = posX + (k * 1.5f)
                        drawOval(
                            color = goldWheat,
                            topLeft = Offset(segX - 4f, segY - 2f),
                            size = Size(8f, 3f)
                        )
                    }
                }
                3 -> {
                    // Four-leaf clover / herb cluster
                    val r = 5.5f
                    drawCircle(color = greenLeaf, radius = r, center = Offset(posX - 4f, posY))
                    drawCircle(color = greenSprout, radius = r, center = Offset(posX + 4f, posY))
                    drawCircle(color = greenDark, radius = r, center = Offset(posX, posY - 4f))
                    drawCircle(color = greenLeaf, radius = r, center = Offset(posX, posY + 4f))
                }
                4 -> {
                    // Wildflower blossom
                    val petalColor = when (i % 3) {
                        0 -> flowerPink
                        1 -> flowerPurple
                        else -> flowerGold
                    }
                    val petalRadius = 4.5f
                    drawCircle(color = petalColor, radius = petalRadius, center = Offset(posX - 4f, posY - 4f))
                    drawCircle(color = petalColor, radius = petalRadius, center = Offset(posX + 4f, posY - 4f))
                    drawCircle(color = petalColor, radius = petalRadius, center = Offset(posX - 4f, posY + 4f))
                    drawCircle(color = petalColor, radius = petalRadius, center = Offset(posX + 4f, posY + 4f))
                    drawCircle(color = flowerGold, radius = 3.5f, center = Offset(posX, posY))
                }
                5 -> {
                    // Dense foliage canopy leaf
                    drawOval(
                        color = greenLeaf,
                        topLeft = Offset(posX - 10f, posY - 6f),
                        size = Size(20f, 10f)
                    )
                    drawLine(
                        color = greenDark,
                        start = Offset(posX - 9f, posY),
                        end = Offset(posX + 9f, posY),
                        strokeWidth = 1.5f
                    )
                }
            }
        }
    }
}
