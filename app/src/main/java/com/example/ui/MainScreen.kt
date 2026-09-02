package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.data.models.SwipeSensitivity
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.weatheredStoneBorder
import kotlin.math.cos
import kotlin.math.sin
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.DefaultItems
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

private val MAIN_NAV_ITEMS = listOf(
    BottomNavItemData("🐾", "Companion", 0, "Companion Tab", "nav_companion"),
    BottomNavItemData("🗺️", "Adventuring", 19, "Adventuring Tab", "nav_adventuring"),
    BottomNavItemData("⚔️", "Skills", 1, "Skills Tab", "nav_skills"),
    BottomNavItemData("🗃️", "Storage", 4, "Storage Tab", "nav_storage"),
    BottomNavItemData("✨", "Incantations", 12, "Incantations Tab", "nav_incantations"),
    BottomNavItemData("📜", "Quests", 8, "Quests Tab", "nav_quests"),
    BottomNavItemData("🪶", "Shaman Path", 10, "Shaman Path Tab", "nav_shaman_path"),
    BottomNavItemData("⚒️", "Forging", 14, "Forging Tab", "nav_forging"),
    BottomNavItemData("🌊", "Shaman Pool", 20, "Shaman Pool Tab", "nav_shaman_pool"),
    BottomNavItemData("🌲", "The Grove", 21, "The Grove Tab", "nav_the_grove"),
    BottomNavItemData("🥷", "Trickery", 16, "Trickery Tab", "nav_trickery"),
    BottomNavItemData("✨", "Summoning", 11, "Summoning Tab", "nav_summoning"),
    BottomNavItemData("💀", "Bounty Hunter", 6, "Bounty Hunter Tab", "nav_bounty_hunter"),
    BottomNavItemData("🛡️", "Equipment", 7, "Equipment Tab", "nav_equipment"),
    BottomNavItemData("🎯", "Whittling", 13, "Whittling Tab", "nav_whittling"),
    BottomNavItemData("🌿", "Herbalism", 15, "Herbalism Tab", "nav_herbalism"),
    BottomNavItemData("⛵", "Navigation", 18, "Navigation Tab", "nav_navigation"),
    BottomNavItemData("📱", "Divination", 22, "Divination Tab", "nav_divination"),
    BottomNavItemData("🤝", "Favors", 24, "Tribe Favors Tab", "nav_favors"),
    BottomNavItemData("📖", "Encyclopedia", 23, "Encyclopedia Tab", "nav_encyclopedia"),
    BottomNavItemData("🏡", "House", 5, "POH House Tab", "nav_house"),
    BottomNavItemData("🚜", "POF Farm", 9, "Player Owned Farm Tab", "nav_farm"),
    BottomNavItemData("📈", "XP Log", 3, "XP Log Tab", "nav_xp_log")
)

private fun getNavVisualPosition(navIndex: Int): Int {
    val pos = MAIN_NAV_ITEMS.indexOfFirst { it.navIndex == navIndex }
    return if (pos >= 0) pos else navIndex
}

@Composable
fun MainScreen(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val petMoodState by viewModel.petMoodState.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val equippedItems by viewModel.equippedItems.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val levelUpEvent by viewModel.levelUpEvent.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val stepCount by viewModel.stepCounterManager.stepCount.collectAsStateWithLifecycle()
    val swipeSensitivity by viewModel.swipeTabSensitivity.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var dragConsumed by remember { mutableStateOf(false) }
    val thresholdPx = remember(swipeSensitivity, density) {
        with(density) { swipeSensitivity.thresholdDp.dp.toPx() }
    }

    val pohState by viewModel.pohHouseState.collectAsStateWithLifecycle()
    val isAfkCampfireActive by viewModel.isAfkCampfireActive.collectAsStateWithLifecycle()
    val isAfkCookingActive by viewModel.isAfkCookingActive.collectAsStateWithLifecycle()
    val isAfkFishingActive by viewModel.isAfkFishingActive.collectAsStateWithLifecycle()
    val isAfkMiningActive by viewModel.isAfkMiningActive.collectAsStateWithLifecycle()
    val isAfkSmeltingActive by viewModel.isAfkSmeltingActive.collectAsStateWithLifecycle()
    val isAfkSawmillActive by viewModel.isAfkSawmillActive.collectAsStateWithLifecycle()
    val isAfkWoodcuttingActive by viewModel.isAfkWoodcuttingActive.collectAsStateWithLifecycle()
    val isAfkNailCraftingActive by viewModel.isAfkNailCraftingActive.collectAsStateWithLifecycle()
    val isAfkStickCraftingActive by viewModel.isAfkStickCraftingActive.collectAsStateWithLifecycle()
    val isAfkArrowtipCraftingActive by viewModel.isAfkArrowtipCraftingActive.collectAsStateWithLifecycle()
    val isAfkFletchingActive by viewModel.isAfkFletchingActive.collectAsStateWithLifecycle()
    val isAfkBoneBuryingActive by viewModel.isAfkBoneBuryingActive.collectAsStateWithLifecycle()
    val isAfkSailingActive by viewModel.isAfkSailingActive.collectAsStateWithLifecycle()
    val isAfkRunecraftingActive by viewModel.isAfkRunecraftingActive.collectAsStateWithLifecycle()
    val isAfkThievingActive by viewModel.isAfkThievingActive.collectAsStateWithLifecycle()
    val isAfkSepulchreActive by viewModel.isAfkSepulchreActive.collectAsStateWithLifecycle()
    val isAfkDruidAltarActive by viewModel.isAfkDruidAltarActive.collectAsStateWithLifecycle()
    val afkRunecraftTargetRuneId by viewModel.afkRunecraftTargetRuneId.collectAsStateWithLifecycle()
    val isAfkTrapCraftingActive by viewModel.isAfkTrapCraftingActive.collectAsStateWithLifecycle()
    val isAfkSlayerActive by viewModel.isAfkSlayerActive.collectAsStateWithLifecycle()
    val isAfkHunterActive by viewModel.isAfkHunterActive.collectAsStateWithLifecycle()
    val isAfkBossActive by viewModel.isAfkBossActive.collectAsStateWithLifecycle()
    val isAfkFarmingActive by viewModel.isAfkFarmingActive.collectAsStateWithLifecycle()
    val activeQuestExpedition by viewModel.activeQuestExpedition.collectAsStateWithLifecycle()
    val selectedCraftingTrapId by viewModel.selectedCraftingTrapId.collectAsStateWithLifecycle()
    val selectedFishId by viewModel.selectedFishId.collectAsStateWithLifecycle()
    val selectedFoodId by viewModel.selectedFoodId.collectAsStateWithLifecycle()
    val cookingQueue by viewModel.cookingQueue.collectAsStateWithLifecycle()
    val selectedOreId by viewModel.selectedOreId.collectAsStateWithLifecycle()
    val selectedBarId by viewModel.selectedBarId.collectAsStateWithLifecycle()
    val selectedTreeId by viewModel.selectedTreeId.collectAsStateWithLifecycle()
    val skillAppListeners by viewModel.skillAppListeners.collectAsStateWithLifecycle()
    val completedQuestPopup by viewModel.completedQuestPopup.collectAsStateWithLifecycle()
    val fmColor1 by viewModel.fmColor1.collectAsStateWithLifecycle()
    val fmColor2 by viewModel.fmColor2.collectAsStateWithLifecycle()
    val selectedFmAnimations by viewModel.selectedFmAnimations.collectAsStateWithLifecycle()
    val foodBagEatHighestFirst by viewModel.foodBagEatHighestFirst.collectAsStateWithLifecycle()
    val cauldronFoodName by viewModel.cauldronFoodName.collectAsStateWithLifecycle()
    val cauldronFoodEmoji by viewModel.cauldronFoodEmoji.collectAsStateWithLifecycle()
    val cauldronUncookedCount by viewModel.cauldronUncookedCount.collectAsStateWithLifecycle()
    val cauldronCookedCount by viewModel.cauldronCookedCount.collectAsStateWithLifecycle()
    val cauldronProgress by viewModel.cauldronProgress.collectAsStateWithLifecycle()
    val isCauldronAfkActive by viewModel.isCauldronAfkActive.collectAsStateWithLifecycle()
    val selectedCauldronRecipe by viewModel.selectedCauldronRecipe.collectAsStateWithLifecycle()
    val activeCookingBuffs by viewModel.activeCookingBuffs.collectAsStateWithLifecycle()
    val favoriteItemIds by viewModel.favoriteItemIds.collectAsStateWithLifecycle()
    val recentAfkHistory by viewModel.recentAfkHistory.collectAsStateWithLifecycle()
    val inspectedObtainItem by viewModel.inspectedObtainItem.collectAsStateWithLifecycle()
    val offlineGainsReport by viewModel.offlineGainsReport.collectAsStateWithLifecycle()
    val preferredQueuedFoodId by viewModel.preferredQueuedFoodId.collectAsStateWithLifecycle()
    val lastUsedTotemId by viewModel.lastUsedTotemId.collectAsStateWithLifecycle()
    val activeSummon by viewModel.activeSummon.collectAsStateWithLifecycle()
    val contractsMap by viewModel.contractsMap.collectAsStateWithLifecycle()
    val contractsNoticeDismissedDay by viewModel.contractsNoticeDismissedDay.collectAsStateWithLifecycle()
    val lastContractRewardResult by viewModel.lastContractRewardResult.collectAsStateWithLifecycle()
    val floorClearReward by viewModel.floorClearRewardEvent.collectAsStateWithLifecycle()
    val adventuringXp = skillXpMap[com.example.data.models.OsrsSkill.ADVENTURING] ?: 0L
    val adventuringLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(adventuringXp)
    val cookingXp = skillXpMap[com.example.data.models.OsrsSkill.COOKING] ?: 0L
    val cookingLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(cookingXp)
    val maxBuffSlots = viewModel.getMaxCookingBuffSlots()
    var selectedBottomNavIndex by remember { mutableIntStateOf(0) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START, androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    viewModel.onAppForegrounded()
                }
                androidx.lifecycle.Lifecycle.Event.ON_STOP, androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    viewModel.onAppBackgrounded()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onAppBackgrounded()
        }
    }

    fun navigateWithSailingXp(index: Int, tabName: String) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (index == 4 && (tabName.contains("Cauldron", ignoreCase = true) || tabName.contains("Cooking", ignoreCase = true))) {
            viewModel.setStorageSubTab(1)
        }
        if (selectedBottomNavIndex != index) {
            selectedBottomNavIndex = index
            viewModel.addXpAndNotify(
                skill = OsrsSkill.SAILING,
                amount = 35L,
                gpReward = 10L,
                logTitle = "Seafaring Exploration",
                logDesc = "Navigated across Gielinor to $tabName! Gained +35 Sailing XP ⛵!"
            )
        }
    }
    var selectedSkillForDetail by remember { mutableStateOf<OsrsSkill?>(null) }
    var isPetSelectorOpen by remember { mutableStateOf(false) }
    var isTaskXpEditorOpen by remember { mutableStateOf(false) }
    var isMasterControlPanelOpen by remember { mutableStateOf(false) }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    var isFoodSelectorOpen by remember { mutableStateOf(false) }
    var isBattleDialogOpen by remember { mutableStateOf(false) }

    // Intercept back button so back button does not close out of the app
    BackHandler(enabled = true) {
        when {
            offlineGainsReport != null -> viewModel.dismissOfflineGainsReport()
            completedQuestPopup != null -> viewModel.dismissQuestCompletionPopup()
            isSettingsDialogOpen -> isSettingsDialogOpen = false
            isBattleDialogOpen -> isBattleDialogOpen = false
            isFoodSelectorOpen -> isFoodSelectorOpen = false
            isTaskXpEditorOpen -> isTaskXpEditorOpen = false
            isMasterControlPanelOpen -> isMasterControlPanelOpen = false
            isPetSelectorOpen -> isPetSelectorOpen = false
            selectedSkillForDetail != null -> selectedSkillForDetail = null
            selectedBottomNavIndex != 0 -> selectedBottomNavIndex = 0
            else -> {
                // On home tab with no overlays: keep app open
            }
        }
    }

    val activeAfkName = when {
        isAfkCampfireActive -> "🔥 Campfire (Summoning)"
        isAfkCookingActive -> "🍳 Kitchen Range (Cooking)"
        isAfkFishingActive -> "🎣 POH Pond (Fishing)"
        isAfkMiningActive -> "⛏️ POH Quarry (Gemology)"
        isAfkSmeltingActive -> "🔥 Smelting Furnace (Forging)"
        isAfkSawmillActive -> "🪚 Sawmill Planks (Hut-Keeping)"
        isAfkWoodcuttingActive -> "🌳 Woodcutting Grove (Harvesting)"
        isAfkNailCraftingActive -> "🔨 Nail Anvil (Forging)"
        isAfkTrapCraftingActive -> "🪤 Trap Crafting (Whittling)"
        isAfkFletchingActive -> "🏹 Arrow Fletching (Whittling)"
        isAfkBoneBuryingActive -> "🦴 Bone Burying (Magic)"
        isAfkRunecraftingActive -> "🔮 Runemaking Altar (Runemaking)"
        isAfkThievingActive -> "🕵️ Pickpocketing (Trickery)"
        isAfkSepulchreActive -> "🗿 Shamanic Catacombs (Trickery)"
        isAfkDruidAltarActive -> "🌿 Druid Altar (Summoning)"
        isAfkSailingActive -> "⛵ Ocean Rowing (Navigation)"
        else -> null
    }

    val currentActivityText = remember(
        activeQuestExpedition,
        activeAfkName,
        isAfkSlayerActive,
        isAfkHunterActive,
        isAfkBossActive,
        isAfkFarmingActive
    ) {
        val exp = activeQuestExpedition
        if (exp != null && !exp.isPaused) {
            val isKanto = exp.quest.chapterId != null || exp.quest.id.startsWith("tl_kanto_")
            val timeStr = com.example.data.models.formatQuestDuration(exp.remainingSeconds)
            if (isKanto) "🧢 Kanto: ${exp.quest.name} ($timeStr)"
            else "📜 Quest: ${exp.quest.name} ($timeStr)"
        } else if (isAfkSlayerActive) {
            "⚔️ AFK Bounty Hunter Task"
        } else if (isAfkHunterActive) {
            "🐾 AFK Beast Tracking"
        } else if (isAfkBossActive) {
            "☠️ AFK Boss Fight"
        } else if (isAfkFarmingActive) {
            "🌾 AFK Agriculture Patch"
        } else if (activeAfkName != null) {
            activeAfkName
        } else {
            "💤 Idle"
        }
    }

    val selectedNavAnimations by viewModel.selectedNavAnimations.collectAsState()
    val navColor1 by viewModel.navColor1.collectAsState()
    val navColor2 by viewModel.navColor2.collectAsState()

    val totalLevel = remember(skillXpMap) {
        skillXpMap.values.sumOf { OsrsXpCalculator.getLevelForXp(it) }
    }

    val allCookedFoods = remember(inventoryItems, bankItems) {
        val baseCookedList = (com.example.data.models.CookingRecipes.ALL_COOKED_FOOD_ITEMS + 
            com.example.data.models.DefaultItems.ALL_SHOP_ITEMS.filter { it.isCookedReadyToEatFood })
            .distinctBy { it.id }

        val playerItems = (inventoryItems + bankItems).distinctBy { it.id }
        val allFoodIds = (baseCookedList.map { it.id } + playerItems.filter { it.isCookedReadyToEatFood }.map { it.id }).distinct()

        allFoodIds.mapNotNull { foodId ->
            val template = baseCookedList.find { it.id == foodId }
                ?: playerItems.find { it.id == foodId }
                ?: com.example.data.models.DefaultItems.getItemById(foodId)

            val normId = com.example.data.models.DefaultItems.normalizeItemId(foodId)
            val totalStock = playerItems.filter {
                it.id == foodId || it.id == normId ||
                com.example.data.models.DefaultItems.normalizeItemId(it.id) == normId ||
                it.name.equals(template.name, ignoreCase = true)
            }.sumOf { it.quantity }

            template.copy(quantity = totalStock)
        }.sortedWith(
            compareByDescending<com.example.data.models.InventoryItem> { it.quantity > 0 }
                .thenByDescending { it.healHp }
                .thenBy { it.name }
        )
    }

    val queuedFoodItem = remember(allCookedFoods, preferredQueuedFoodId, foodBagEatHighestFirst) {
        val inStock = allCookedFoods.filter { it.quantity > 0 }
        if (preferredQueuedFoodId != null) {
            val prefNorm = com.example.data.models.DefaultItems.normalizeItemId(preferredQueuedFoodId!!)
            val prefFood = allCookedFoods.find { it.id == preferredQueuedFoodId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == prefNorm }
            if (prefFood != null && prefFood.quantity > 0) {
                prefFood
            } else {
                // Preferred food has reached 0 quantity - fallback to available food in stock!
                if (foodBagEatHighestFirst) inStock.maxByOrNull { it.healHp } ?: prefFood ?: allCookedFoods.firstOrNull()
                else inStock.minByOrNull { it.healHp } ?: prefFood ?: allCookedFoods.firstOrNull()
            }
        } else {
            if (inStock.isNotEmpty()) {
                if (foodBagEatHighestFirst) inStock.maxByOrNull { it.healHp } else inStock.minByOrNull { it.healHp }
            } else {
                allCookedFoods.firstOrNull()
            }
        }
    }

    val completedContractsCount = remember(contractsMap) {
        com.example.data.models.SkillContractData.CONTRACT_SUPPORTED_SKILLS.count { skill ->
            val contract = contractsMap[skill]
            contract != null && contract.currentQty >= contract.targetQty
        }
    }

    val todayDateStr = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    val showContractsDailyNotice = completedContractsCount > 0 && contractsNoticeDismissedDay != todayDateStr

    val lastUsedTotemAnimal = remember(lastUsedTotemId) {
        com.example.data.models.SummoningData.ALL_ANIMALS.find { it.id == lastUsedTotemId }
            ?: com.example.data.models.SummoningData.ALL_ANIMALS.first()
    }

    val lastTotemCount = remember(inventoryItems, bankItems, lastUsedTotemAnimal) {
        val totemId = "item_totem_${lastUsedTotemAnimal.id}"
        (inventoryItems.find { it.id == totemId }?.quantity ?: 0) +
        (bankItems.find { it.id == totemId }?.quantity ?: 0)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OsrsLeatherDark,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                OsrsHeader(
                    petState = petState,
                    totalLevel = totalLevel,
                    petMoodState = petMoodState,
                    currentActivityText = currentActivityText,
                    onOpenSettings = { isSettingsDialogOpen = true }
                )
            }
        },
        bottomBar = {
            val lazyListState = rememberLazyListState()
            val navItems = MAIN_NAV_ITEMS

            LaunchedEffect(selectedBottomNavIndex) {
                val targetIndex = MAIN_NAV_ITEMS.indexOfFirst { it.navIndex == selectedBottomNavIndex }
                if (targetIndex >= 0) {
                    lazyListState.animateScrollToItem(targetIndex)
                }
            }

            val scrollOffsetPx = remember {
                derivedStateOf {
                    lazyListState.firstVisibleItemIndex * 240f + lazyListState.firstVisibleItemScrollOffset
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(Color(0xFF14100C))
                    .drawBehind {
                        val w = size.width
                        val h = size.height
                        val currentScroll = scrollOffsetPx.value

                        // 1. Carved Rock Track Channel Background
                        drawRect(color = Color(0xFF0F0C09))

                        // Top and Bottom Granite Mechanism Rails
                        drawRect(
                            color = Color(0xFF2B221B),
                            topLeft = Offset(0f, 0f),
                            size = Size(w, 5f)
                        )
                        drawRect(
                            color = Color(0xFF2B221B),
                            topLeft = Offset(0f, h - 5f),
                            size = Size(w, 5f)
                        )

                        // Steel/Bronze Rock Track Grooves (slotted guide rail)
                        drawLine(
                            color = Color(0xFF4D3F31),
                            start = Offset(0f, 6f),
                            end = Offset(w, 6f),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = Color(0xFF4D3F31),
                            start = Offset(0f, h - 6f),
                            end = Offset(w, h - 6f),
                            strokeWidth = 2f
                        )

                        // Sliding Rock Mechanism Cog Teeth along rails moving with scroll
                        val toothSpacing = 22f
                        val toothOffset = (currentScroll % toothSpacing)
                        val toothCount = (w / toothSpacing).toInt() + 2

                        for (i in -1..toothCount) {
                            val toothX = i * toothSpacing - toothOffset
                            drawRect(
                                color = Color(0xFF6B5643),
                                topLeft = Offset(toothX, 1f),
                                size = Size(8f, 4f)
                            )
                            drawRect(
                                color = Color(0xFF6B5643),
                                topLeft = Offset(toothX, h - 5f),
                                size = Size(8f, 4f)
                            )
                        }

                        // Rotating Mechanical Stone Gears on left & right ends
                        val gearRadius = 14f
                        val leftGearCenter = Offset(12f, h / 2f)
                        val rightGearCenter = Offset(w - 12f, h / 2f)
                        val gearAngleRad = (currentScroll * 0.04f)

                        listOf(leftGearCenter, rightGearCenter).forEachIndexed { idx, gearCenter ->
                            val dir = if (idx == 0) 1f else -1f
                            drawCircle(
                                color = Color(0xFF3B2E24),
                                radius = gearRadius,
                                center = gearCenter
                            )
                            drawCircle(
                                color = Color(0xFFFFD700).copy(alpha = 0.6f),
                                radius = gearRadius,
                                center = gearCenter,
                                style = Stroke(width = 1.5f)
                            )
                            val spokes = 6
                            for (s in 0 until spokes) {
                                val angle = gearAngleRad * dir + s * (2f * Math.PI.toFloat() / spokes)
                                val px = gearCenter.x + cos(angle) * (gearRadius - 2f)
                                val py = gearCenter.y + sin(angle) * (gearRadius - 2f)
                                drawCircle(
                                    color = Color(0xFFFFB703),
                                    radius = 2.5f,
                                    center = Offset(px, py)
                                )
                            }
                            drawCircle(
                                color = Color(0xFF140F0C),
                                radius = 4f,
                                center = gearCenter
                            )
                        }

                        // Top ancient stone pedestal border
                        drawLine(
                            color = Color(0xFF695847),
                            start = Offset(0f, 0f),
                            end = Offset(w, 0f),
                            strokeWidth = 3f
                        )
                    }
            ) {
                LazyRow(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(navItems, key = { it.testTag }) { item ->
                        BottomNavItem(
                            icon = item.icon,
                            label = item.label,
                            isSelected = selectedBottomNavIndex == item.navIndex,
                            onClick = { navigateWithSailingXp(item.navIndex, item.tabName) },
                            testTag = item.testTag
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(selectedBottomNavIndex, swipeSensitivity) {
                    if (swipeSensitivity == SwipeSensitivity.OFF) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            dragConsumed = false
                        },
                        onDragEnd = {
                            totalDragX = 0f
                            dragConsumed = false
                        },
                        onDragCancel = {
                            totalDragX = 0f
                            dragConsumed = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!dragConsumed) {
                                totalDragX += dragAmount
                                if (totalDragX < -thresholdPx) {
                                    // Swiped Left -> Advance to next tab
                                    dragConsumed = true
                                    val currentPos = MAIN_NAV_ITEMS.indexOfFirst { it.navIndex == selectedBottomNavIndex }
                                    if (currentPos >= 0) {
                                        val nextPos = (currentPos + 1) % MAIN_NAV_ITEMS.size
                                        val nextItem = MAIN_NAV_ITEMS[nextPos]
                                        navigateWithSailingXp(nextItem.navIndex, nextItem.tabName)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                } else if (totalDragX > thresholdPx) {
                                    // Swiped Right -> Go to previous tab
                                    dragConsumed = true
                                    val currentPos = MAIN_NAV_ITEMS.indexOfFirst { it.navIndex == selectedBottomNavIndex }
                                    if (currentPos >= 0) {
                                        val prevPos = (currentPos - 1 + MAIN_NAV_ITEMS.size) % MAIN_NAV_ITEMS.size
                                        val prevItem = MAIN_NAV_ITEMS[prevPos]
                                        navigateWithSailingXp(prevItem.navIndex, prevItem.tabName)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            com.example.ui.components.NatureBackgroundParticles(
                selectedNavAnimations = selectedNavAnimations,
                navColor1 = navColor1,
                navColor2 = navColor2,
                activeNavIndex = selectedBottomNavIndex,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Main Content depending on selected tab
                val fmXp = skillXpMap[OsrsSkill.FIREMAKING] ?: 0L
                val fmLevel = OsrsXpCalculator.getLevelForXp(fmXp)

            TapFireOverlay(
                firemakingLevel = fmLevel,
                fmColor1 = fmColor1,
                fmColor2 = fmColor2,
                navColor1 = navColor1,
                navColor2 = navColor2,
                selectedFmAnimations = selectedFmAnimations,
                onTapFire = null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = selectedBottomNavIndex,
                    transitionSpec = {
                        val initialPos = getNavVisualPosition(initialState)
                        val targetPos = getNavVisualPosition(targetState)
                        if (targetPos > initialPos) {
                            (slideInHorizontally { width -> width / 3 } + fadeIn() + scaleIn(initialScale = 0.96f))
                                .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut() + scaleOut(targetScale = 0.96f))
                        } else {
                            (slideInHorizontally { width -> -width / 3 } + fadeIn() + scaleIn(initialScale = 0.96f))
                                .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut() + scaleOut(targetScale = 0.96f))
                        }.using(
                            SizeTransform(clip = false)
                        )
                    },
                    label = "tab_switch_60fps_transition",
                    modifier = Modifier.fillMaxSize()
                ) { targetNavIndex ->
                    when (targetNavIndex) {
                    0 -> {
                        // Companion Pet View
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 12.dp)
                        ) {
                            // Once a day, if you have at least one complete contract, show a notification on the companions tab
                            if (showContractsDailyNotice) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setContractCategoryFilter("Done")
                                            selectedBottomNavIndex = 24 // Contracts Tab
                                        }
                                        .testTag("contracts_companion_daily_banner"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C12)),
                                    border = BorderStroke(1.5.dp, com.example.ui.theme.OsrsGold)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("🤝", fontSize = 22.sp)
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Tribe Favors",
                                                        color = com.example.ui.theme.OsrsTextYellow,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Surface(
                                                        color = Color(0xFF1B5E20),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "$completedContractsCount Done",
                                                            color = Color(0xFFA5D6A7),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "You have $completedContractsCount favor${if (completedContractsCount > 1) "s" else ""} ready to claim!",
                                                    color = Color.LightGray,
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.setContractCategoryFilter("Done")
                                                    selectedBottomNavIndex = 24
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.OsrsRedFrame),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("View", color = com.example.ui.theme.OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            IconButton(
                                                onClick = { viewModel.dismissContractsDailyNotice() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Dismiss for today",
                                                    tint = Color.LightGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            PetDisplayView(
                                petState = petState,
                                totalLevel = totalLevel,
                                maxPetHp = viewModel.getPetMaxHealth(),
                                pohState = pohState,
                                petMoodState = petMoodState,
                                currentActivityText = currentActivityText,
                                recentAfkHistory = recentAfkHistory,
                                activeAfkName = activeAfkName,
                                onQuickFeed = { viewModel.feedPetLowestFood() },
                                onPetTouch = {
                                    viewModel.addXpAndNotify(
                                        skill = petState.petType.primarySkill,
                                        amount = 50L,
                                        gpReward = 10L,
                                        logTitle = "Petted ${petState.customName}",
                                        logDesc = "Shared affection with companion!"
                                    )
                                },
                                onOpenPetSelector = { isPetSelectorOpen = true },
                                onOpenPoh = { selectedBottomNavIndex = 5 },
                                onSendChatMessage = { msg ->
                                    viewModel.sendPetChatMessage(msg) {}
                                },
                                onToggleMute = { viewModel.toggleMute() },
                                onResetPetXp = { viewModel.resetCurrentPetXp() },
                                onOpenMasterControlPanel = { isMasterControlPanelOpen = true },
                                onBoostMood = { viewModel.boostPetMood(15, "Pet Interaction") },
                                onEvolvePet = { viewModel.evolveCurrentPet() },
                                onOpenDailySpiritQuests = { viewModel.setShowDailySpiritQuestsDialog(true) },
                                onStartAfkActivity = { activityId -> viewModel.startAfkActivityById(activityId) },
                                onStopAllAfk = { viewModel.stopAllAfkStations() },
                                queuedFoodItem = queuedFoodItem,
                                allCookedFoods = allCookedFoods,
                                lastUsedTotem = lastUsedTotemAnimal,
                                totemStockCount = lastTotemCount,
                                activeSummon = activeSummon,
                                allTotems = com.example.data.models.SummoningData.ALL_ANIMALS,
                                getTotemCount = { animalId -> viewModel.getTotemItemCount(animalId) },
                                onFeedQueuedFood = { item -> viewModel.feedQueuedFood(item ?: queuedFoodItem) },
                                onSelectQueuedFood = { item -> viewModel.setPreferredQueuedFood(item?.id) },
                                onUseTotem = { totemId -> viewModel.activateTotem(totemId) },
                                onSelectTotem = { totemId -> viewModel.setLastUsedTotem(totemId) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // OSRS Scrolling Chatbox at bottom of Companion tab
                            OsrsChatbox(messages = chatMessages)
                        }
                    }

                    1 -> {
                        // Authentic OSRS Skill Tab Grid
                        OsrsSkillGrid(
                            skillXpMap = skillXpMap,
                            onSkillClick = { skill -> selectedSkillForDetail = skill },
                            completedQuestIds = petState.completedQuestIds,
                            inventoryItems = inventoryItems,
                            bankItems = bankItems,
                            equippedItems = equippedItems,
                            onOpenTrainerLeague = { navigateWithSailingXp(10, "Shaman Path Tab") },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    3 -> {
                        // Latest XP Summary & Activity Log
                        XpActivityLogTab(
                            activityLogs = activityLogs,
                            activeAfkName = activeAfkName,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    4 -> {
                        // Inventory, Bank Vault & Grand Exchange Shop
                        OsrsInventoryTab(
                            inventoryItems = inventoryItems,
                            bankItems = bankItems,
                            coinsGp = petState.coinsGp,
                            onFeedPet = { item -> viewModel.feedPet(item) },
                            onPlayPet = { item -> viewModel.playWithPet(item) },
                            onSellItem = { item, qty -> viewModel.sellInventoryItem(item, qty) },
                            onBuyItem = { shopItem -> viewModel.buyShopItem(shopItem) },
                            onBuyCustomGeOffer = { name, price, qty -> viewModel.buyCustomGeOffer(name, price, qty) },
                            onEquipItem = { item -> viewModel.equipItem(item) },
                            onDepositToBank = { item, qty -> viewModel.depositToBank(item.id, qty) },
                            onWithdrawFromBank = { item, qty -> viewModel.withdrawFromBank(item.id, qty) },
                            onDepositAllToBank = { viewModel.depositAllInventoryToBank() },
                            onOpenSeedPouch = { item -> viewModel.openSeedPouch(item.id) },
                            onOpenContractReward = { skill, qty -> viewModel.openContractRewards(skill, qty) },
                            foodBagEatHighestFirst = foodBagEatHighestFirst,
                            onToggleFoodBagEatOrder = { viewModel.toggleFoodBagEatOrder() },
                            onFeedFromFoodBag = { item -> viewModel.feedPetFromFoodBag(item) },
                            cauldronFoodName = cauldronFoodName,
                            cauldronFoodEmoji = cauldronFoodEmoji,
                            cauldronUncookedCount = cauldronUncookedCount,
                            cauldronCookedCount = cauldronCookedCount,
                            cauldronProgress = cauldronProgress,
                            isCauldronAfkActive = isCauldronAfkActive,
                            activeCauldronRecipe = selectedCauldronRecipe,
                            onSelectCauldronRecipe = { recipe -> viewModel.selectCauldronRecipe(recipe) },
                            onCookRecipeFromBankAndInventory = { recipe -> viewModel.cookRecipeFromBankAndInventory(recipe) },
                            adventuringLevel = adventuringLvl,
                            cookingLevel = cookingLvl,
                            cookingXp = cookingXp,
                            cookingQueue = cookingQueue,
                            isAfkCookingActive = isAfkCookingActive,
                            onToggleAfkCooking = { viewModel.toggleAfkCooking() },
                            onAddToCookingQueue = { rawId -> viewModel.addToCookingQueue(rawId) },
                            onRemoveFromCookingQueue = { rawId -> viewModel.removeFromCookingQueue(rawId) },
                            onMoveCookingQueueItem = { rawId, dir -> viewModel.moveCookingQueueItem(rawId, dir) },
                            onClearCookingQueue = { viewModel.clearCookingQueue() },
                            onAutoPopulateCookingQueue = { viewModel.autoPopulateCookingQueue() },
                            onCookFood = { rawId, qty -> viewModel.cookRawFoodAtRange(targetFoodId = rawId, isAfk = false, quantity = qty) },
                            activeCookingBuffs = activeCookingBuffs,
                            maxBuffSlots = maxBuffSlots,
                            favoriteItemIds = favoriteItemIds,
                            onToggleFavoriteItem = { itemId -> viewModel.toggleFavoriteItem(itemId) },
                            onAddFoodToCauldron = { item, qty -> viewModel.addFoodToCauldron(item, qty) },
                            onClaimCauldronCookedFood = { viewModel.claimCauldronCookedFood() },
                            onToggleCauldronAfk = { viewModel.toggleCauldronAfk() },
                            onOfferPouchItem = { item, qty -> viewModel.offerResourceToSpirits(item, qty) },
                            onBatchOfferPouchCategory = { cat -> viewModel.batchOfferMysticalCategory(cat) },
                            onBatchOfferPouchAll = { viewModel.batchOfferAllMystical() },
                            onTransmutePouchItem = { item, qty -> viewModel.transmuteResourceInPouch(item, qty) },
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    5 -> {
                        // Poh House Companion Tab (with Nia NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            PohHouseTab(
                                pohState = pohState,
                                petState = petState,
                                inventoryItems = inventoryItems,
                                bankItems = bankItems,
                                constructionXp = skillXpMap[OsrsSkill.CONSTRUCTION] ?: 0L,
                                coinsGp = petState.coinsGp,
                                unlockedPets = petState.unlockedPets,
                                isAfkCampfireActive = isAfkCampfireActive,
                                isAfkCookingActive = isAfkCookingActive,
                                isAfkFishingActive = isAfkFishingActive,
                                isAfkMiningActive = isAfkMiningActive,
                                isAfkSmeltingActive = isAfkSmeltingActive,
                                isAfkSawmillActive = isAfkSawmillActive,
                                isAfkWoodcuttingActive = isAfkWoodcuttingActive,
                                isAfkNailCraftingActive = isAfkNailCraftingActive,
                                isAfkStickCraftingActive = isAfkStickCraftingActive,
                                isAfkArrowtipCraftingActive = isAfkArrowtipCraftingActive,
                                isAfkFletchingActive = isAfkFletchingActive,
                                isAfkBoneBuryingActive = isAfkBoneBuryingActive,
                                isAfkSailingActive = isAfkSailingActive,
                                isAfkRunecraftingActive = isAfkRunecraftingActive,
                                selectedRuneId = afkRunecraftTargetRuneId,
                                isAfkTrapCraftingActive = isAfkTrapCraftingActive,
                                selectedCraftingTrapId = selectedCraftingTrapId,
                                selectedFishId = selectedFishId,
                                selectedFoodId = selectedFoodId,
                                selectedOreId = selectedOreId,
                                selectedBarId = selectedBarId,
                                selectedTreeId = selectedTreeId,
                                onToggleAfkCampfire = { viewModel.toggleAfkCampfire() },
                                onToggleAfkCooking = { viewModel.toggleAfkCooking() },
                                onToggleAfkFishing = { viewModel.toggleAfkFishing() },
                                onToggleAfkMining = { viewModel.toggleAfkMining() },
                                onToggleAfkSmelting = { viewModel.toggleAfkSmelting() },
                                onToggleAfkSawmill = { viewModel.toggleAfkSawmill() },
                                onToggleAfkWoodcutting = { viewModel.toggleAfkWoodcutting() },
                                onToggleAfkNailCrafting = { viewModel.toggleAfkNailCrafting() },
                                onToggleAfkStickCrafting = { viewModel.toggleAfkStickCrafting() },
                                onToggleAfkArrowtipCrafting = { viewModel.toggleAfkArrowtipCrafting() },
                                onToggleAfkFletching = { viewModel.toggleAfkFletching() },
                                onToggleAfkBoneBurying = { viewModel.toggleAfkBoneBurying() },
                                onToggleAfkSailing = { viewModel.toggleAfkSailing() },
                                onToggleAfkRunecrafting = { runeId -> viewModel.toggleAfkRunecrafting(runeId) },
                                onToggleAfkTrapCrafting = { viewModel.toggleAfkTrapCrafting() },
                                onSelectCraftingTrapId = { trapId -> viewModel.setSelectedCraftingTrapId(trapId) },
                                onSelectFishId = { fishId -> viewModel.setSelectedFishId(fishId) },
                                onSelectFoodId = { foodId -> viewModel.setSelectedFoodId(foodId) },
                                onSelectOreId = { oreId -> viewModel.setSelectedOreId(oreId) },
                                onSelectBarId = { barId -> viewModel.setSelectedBarId(barId) },
                                onSelectTreeId = { treeId -> viewModel.setSelectedTreeId(treeId) },
                                onBurnLogsAtCampfire = { viewModel.burnLogsAtCampfire() },
                                onCookAtRange = { viewModel.cookRawFoodAtRange() },
                                onCookSpecificFood = { foodId -> viewModel.cookRawFoodAtRange(targetFoodId = foodId) },
                                onFishAtPohPond = { viewModel.fishAtPohPond() },
                                onFishSpecificFish = { fishId -> viewModel.fishAtPohPond(targetFishId = fishId) },
                                onMineAtQuarry = { viewModel.mineAtPohQuarry() },
                                onMineSpecificOre = { oreId -> viewModel.mineAtPohQuarry(targetOreId = oreId) },
                                onSmeltAtFurnace = { viewModel.smeltOresAtFurnace() },
                                onSmeltSpecificBar = { barId -> viewModel.smeltOresAtFurnace(targetBarId = barId) },
                                onConvertLogsAtSawmill = { viewModel.convertLogsToPlanksAtSawmill() },
                                onChopTrees = { viewModel.chopTrees() },
                                onChopSpecificTree = { treeId -> viewModel.chopTrees(targetTreeId = treeId) },
                                onCraftNailsAtAnvil = { viewModel.craftBarsToNailsAtAnvil() },
                                onCraftSticks = { viewModel.craftLogsToSticks() },
                                onCraftArrowtips = { viewModel.craftBarsToArrowtips() },
                                onFletchArrows = { viewModel.fletchSticksToArrows() },
                                onCraftTrap = { trapId -> viewModel.craftHunterTrap(trapId) },
                                onForgeArmor = { armorId -> viewModel.forgeArmorAtAnvil(armorId) },
                                onForgeEquipment = { equipId -> viewModel.forgeEquipmentAtAnvil(equipId) },
                                onBuryBones = { viewModel.buryBonesFromInventory() },
                                onBuildRoom = { roomType, pos -> viewModel.buildRoomInPoh(roomType, pos) },
                                onDemolishRoom = { room -> viewModel.demolishRoomInPoh(room) },
                                onBuildFurniture = { room, furn -> viewModel.buildFurnitureInPoh(room, furn) },
                                onDestroyFurniture = { room, furnId -> viewModel.destroyFurnitureInPoh(room, furnId) },
                                onBuyGeMaterial = { mat, qty -> viewModel.buyGeMaterial(mat, qty) },
                                onExpandGrid = { cost -> viewModel.expandPohGridForGp(cost) },
                                onUpdateWallsAndFloor = { room, wn, we, ws, ww, fl -> viewModel.updateRoomWallsAndFloor(room, wn, we, ws, ww, fl) },
                                modifier = Modifier.fillMaxSize()
                            )

                            NiaNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    6 -> {
                        // Slayer & Hunter Tab
                        OsrsSlayerHunterTab(viewModel = viewModel)
                    }

                    7 -> {
                        // Equipment Worn Items Tab
                        OsrsEquipmentTab(viewModel = viewModel)
                    }

                    8 -> {
                        // Quests Tab
                        OsrsQuestTab(viewModel = viewModel)
                    }

                    9 -> {
                        // Player Owned Farm (POF) Tab (with Farmer Bryan NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            PofFarmTab(viewModel = viewModel)
                            FarmerBryanNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    10 -> {
                        // Trainer League Tab
                        TrainerLeagueTab(viewModel = viewModel)
                    }

                    11 -> {
                        // Summoning Tab (with Sedri NPC Companion - listing summoning and runemaking/runecrafting favors)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.SummoningTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            SedriNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize(),
                                allowedSkillCategories = listOf("Summoning", "Runecrafting", "Runemaking")
                            )
                        }
                    }

                    12 -> {
                        // Magic & Runecrafting Tab (with Elder Elnya NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.MagicTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            ElderElnyaNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    13 -> {
                        // Fletching / Whittling Tab (with Bram NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.FletchingTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            BramNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    14 -> {
                        // Smithing / Forging Tab (with Arig NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.SmithingTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            ArigNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    15 -> {
                        // Herblore / Herbalism Tab (with Orla NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.HerbloreTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            OrlaNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    16 -> {
                        // Thieving / Trickery Tab (with Ren NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.ThievingTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            RenNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    18 -> {
                        // Navigation Tab (with Captain Barnaby NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.NavigationTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            CaptainBarnabyNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    19 -> {
                        // Adventuring Tab
                        com.example.ui.tabs.AdventuringTab(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    20 -> {
                        // Shaman Pool Tab
                        com.example.ui.tabs.ShamanPoolTab(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    21 -> {
                        // The Grove Tab (with Bram NPC Companion)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.TheGroveTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            BramNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    22 -> {
                        // Divination Tab (with Sedri NPC Companion - listing only divination favors)
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.example.ui.tabs.DivinationTab(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            SedriNpcCompanion(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize(),
                                allowedSkillCategories = listOf("Divination")
                            )
                        }
                    }

                    23 -> {
                        // Encyclopedia Tab
                        com.example.ui.tabs.EncyclopediaTab(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    24 -> {
                        // Contracts Tab
                        com.example.ui.tabs.ContractsTab(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize(),
                            onNavigateToTab = { targetTab, targetName ->
                                navigateWithSailingXp(targetTab, targetName)
                            }
                        )
                    }
                }
            }
        }
    }

    // Floating Level Up Top Notification Banner (Overlays on top without pushing content)
    val currentLevelUpEvent = levelUpEvent
    if (currentLevelUpEvent != null) {
        com.example.ui.components.LevelUpNotificationBanner(
            event = currentLevelUpEvent,
            onDismiss = { viewModel.dismissLevelUpEvent() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .align(Alignment.TopCenter)
        )
    }


    // Skill Detail Dialog
    selectedSkillForDetail?.let { skill ->
        val currentXp = skillXpMap[skill] ?: 0L
        OsrsSkillDetailDialog(
            skill = skill,
            currentXp = currentXp,
            onDismiss = { selectedSkillForDetail = null },
            onTrainNow = {
                viewModel.addXpAndNotify(
                    skill = skill,
                    amount = 300L,
                    gpReward = 50L,
                    logTitle = "Trained ${skill.displayName}",
                    logDesc = skill.realLifeAction
                )
            },
            onResetXp = { viewModel.resetSkillXp(skill) }
        )
    }

    // Pet Selector Overlay
    if (isPetSelectorOpen) {
        PetSelectorDialog(
            currentPetState = petState,
            onSelectPetType = { newType -> viewModel.switchPetType(newType) },
            onAdoptPet = { newType -> viewModel.unlockAndAdoptPet(newType) },
            onRenamePet = { newName -> viewModel.updatePetName(newName) },
            onDismiss = { isPetSelectorOpen = false }
        )
    }

    // Settings Dialog Overlay
    if (isSettingsDialogOpen) {
        com.example.ui.components.SettingsDialog(
            viewModel = viewModel,
            onDismiss = { isSettingsDialogOpen = false },
            onOpenMasterControlPanel = { isMasterControlPanelOpen = true },
            onOpenTaskXpEditor = { isTaskXpEditorOpen = true },
            onResetAllData = { viewModel.resetCurrentPetXp() }
        )
    }

    // Master Control Panel Dialog
    if (isMasterControlPanelOpen) {
        com.example.ui.components.MasterControlPanelDialog(
            onDismiss = { isMasterControlPanelOpen = false },
            onTriggerTask = { config, xp, gp ->
                viewModel.addXpAndNotify(
                    skill = config.defaultSkill,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "Control Panel: ${config.taskName}",
                    logDesc = "Triggered ${config.taskName} from Master Control Panel!"
                )
            },
            onResetAllData = {
                viewModel.resetCurrentPetXp()
                isMasterControlPanelOpen = false
            }
        )
    }

    // Task XP Editor Dialog Overlay
    if (isTaskXpEditorOpen) {
        val standardTasks = listOf(
            StandardTaskXp("Swiped Notification", "Clear App Notifications", OsrsSkill.WOODCUTTING, viewModel.getTaskXp("Swiped Notification", 175L), "Clear phone notification clutter"),
            StandardTaskXp("Real Life Walk", "Walk 10 Steps", OsrsSkill.AGILITY, viewModel.getTaskXp("Real Life Walk", 15L), "Physical movement and steps"),
            StandardTaskXp("Pickpocket Practice", "Pickpocket Dexterity", OsrsSkill.THIEVING, viewModel.getTaskXp("Pickpocket Practice", 450L), "Stealth dexterity practice"),
            StandardTaskXp("Drank Water", "Hydration Block", OsrsSkill.HERBLORE, viewModel.getTaskXp("Drank Water", 250L), "Drink 8oz water"),
            StandardTaskXp("Workout Session", "Exercise Set", OsrsSkill.ATTACK, viewModel.getTaskXp("Workout Session", 600L), "Physical pushups/rep session"),
            StandardTaskXp("Cleaned Room", "House Organization", OsrsSkill.CONSTRUCTION, viewModel.getTaskXp("Cleaned Room", 800L), "Organize living space"),
            StandardTaskXp("Focus Reading Block", "30 Min Study", OsrsSkill.MAGIC, viewModel.getTaskXp("Focus Reading Block", 1200L), "Reading & mental focus")
        )

        TaskXpEditorDialog(
            taskXpList = standardTasks,
            customQuests = quests,
            onUpdateTaskXp = { id, newXp -> viewModel.updateTaskXp(id, newXp) },
            onUpdateQuestXp = { id, newXp -> viewModel.updateQuestXp(id, newXp) },
            onFitnessSyncSteps = { steps -> viewModel.addSimulatedSteps(steps) },
            onDismiss = { isTaskXpEditorOpen = false }
        )
    }

    // Food Inventory Selection Dialog Overlay
    if (isFoodSelectorOpen) {
        FeedFoodSelectionDialog(
            inventoryItems = inventoryItems,
            bankItems = bankItems,
            favoriteItemIds = favoriteItemIds,
            onToggleFavorite = { itemId -> viewModel.toggleFavoriteItem(itemId) },
            foodBagEatHighestFirst = foodBagEatHighestFirst,
            onToggleFoodBagEatOrder = { viewModel.toggleFoodBagEatOrder() },
            onFeedFromFoodBag = { item -> viewModel.feedPetFromFoodBag(item) },
            onSelectFoodToFeed = { food -> viewModel.feedPet(food) },
            onGetStarterBread = { viewModel.claimStarterBread() },
            onDismiss = { isFoodSelectorOpen = false }
        )
    }

    // Pet Monster Battle Dialog Overlay
    if (isBattleDialogOpen) {
        PetBattleDialog(
            petState = petState,
            maxPetHp = viewModel.getPetMaxHealth(),
            onWinBattle = { location, skill -> viewModel.onWinPetBattle(location, skill) },
            onDismiss = { isBattleDialogOpen = false }
        )
    }

    // Offline AFK Gains Summary Dialog Overlay (Triggered upon reopening after > 1 min of AFK)
    offlineGainsReport?.let { report ->
        com.example.ui.components.OfflineGainsDialog(
            report = report,
            onDismiss = { viewModel.dismissOfflineGainsReport() }
        )
    }

    // Contract Reward Box Open Result Dialog Overlay (Opens anywhere in the app)
    lastContractRewardResult?.let { result ->
        com.example.ui.components.ContractRewardPopupDialog(
            result = result,
            onDismiss = { viewModel.dismissContractRewardResult() }
        )
    }

    // Adventuring Floor Clear 99 Skilling Set Prize Dialog Overlay
    floorClearReward?.let { reward ->
        com.example.ui.components.FloorClearPrizeDialog(
            reward = reward,
            onClaim = { viewModel.dismissFloorClearReward() }
        )
    }

    // Quest Complete Dialog Overlay
    completedQuestPopup?.let { quest ->
        com.example.ui.components.QuestCompleteDialog(
            quest = quest,
            onDismiss = { viewModel.dismissQuestCompletionPopup() }
        )
    }

    // Universal Item Obtainment Inspector Dialog Overlay (Triggered by holding items across tabs)
    inspectedObtainItem?.let { item ->
        val invCount = inventoryItems.filter { it.id == item.id }.sumOf { it.quantity }
        val bankCount = bankItems.filter { it.id == item.id }.sumOf { it.quantity }
        val isEquipped = equippedItems.values.any { it.id == item.id }

        com.example.ui.tabs.EncyclopediaItemDetailDialog(
            item = item,
            invCount = invCount,
            bankCount = bankCount,
            isEquipped = isEquipped,
            onDismiss = { viewModel.clearInspectedObtainItem() }
        )
    }
        }
    }
}

private data class BottomNavItemData(
    val icon: String,
    val label: String,
    val navIndex: Int,
    val tabName: String,
    val testTag: String
)

@Composable
private fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val haptic = LocalHapticFeedback.current

    // Smooth tab scale & bounce spring physics on selection
    val itemScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "item_scale"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "tab_glow_anim")
    val activeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val stoneBg = if (isSelected) Color(0xFF3B3024) else Color(0xFF221C17)
    val stoneBorderColor = if (isSelected) Color(0xFFFFD700) else Color(0xFF635343)
    val crackColor = if (isSelected) Color(0xFFFFD700) else Color(0xFF140F0C)
    val crackHighlight = if (isSelected) Color(0xFF9E8548) else Color(0xFF453A2F)

    Box(
        modifier = Modifier
            .width(84.dp)
            .height(54.dp)
            .padding(vertical = 2.dp, horizontal = 2.dp)
            .scale(itemScale)
            .clip(RoundedCornerShape(6.dp))
            .background(stoneBg)
            .drawBehind {
                val w = size.width
                val h = size.height

                // 1. Draw Outer Stone Border Frame
                drawRoundRect(
                    color = if (isSelected) Color(0xFFFFD700).copy(alpha = activeGlowAlpha) else stoneBorderColor,
                    size = size,
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx())
                )

                // 2. Draw Cracked Ancient Stone Fissures along the borders
                val path1 = Path().apply {
                    // Top-left corner crack splitting into stone border
                    moveTo(0f, h * 0.35f)
                    lineTo(w * 0.18f, h * 0.22f)
                    lineTo(w * 0.12f, 0f)
                    // Branch
                    moveTo(w * 0.18f, h * 0.22f)
                    lineTo(w * 0.3f, h * 0.3f)
                }

                val path2 = Path().apply {
                    // Top-right crack splitting outer border
                    moveTo(w * 0.75f, 0f)
                    lineTo(w * 0.82f, h * 0.25f)
                    lineTo(w, h * 0.4f)
                }

                val path3 = Path().apply {
                    // Bottom-right corner deep fissure
                    moveTo(w, h * 0.72f)
                    lineTo(w * 0.82f, h * 0.82f)
                    lineTo(w * 0.65f, h)
                    // Branch into center
                    moveTo(w * 0.82f, h * 0.82f)
                    lineTo(w * 0.7f, h * 0.68f)
                }

                val path4 = Path().apply {
                    // Bottom-left crack
                    moveTo(w * 0.28f, h)
                    lineTo(w * 0.15f, h * 0.78f)
                    lineTo(0f, h * 0.65f)
                }

                // Render Fissures & Crack Highlights
                listOf(path1, path2, path3, path4).forEach { crackPath ->
                    // Dark inner fissure shadow
                    drawPath(
                        path = crackPath,
                        color = Color(0xFF0D0A08),
                        style = Stroke(width = 2.5f)
                    )
                    // Beveled stone crack edge highlight
                    drawPath(
                        path = crackPath,
                        color = crackHighlight,
                        style = Stroke(width = 1.0f)
                    )
                }

                // Glowing Ancient Shaman Energy inside cracks if tab is selected
                if (isSelected) {
                    listOf(path1, path3).forEach { crackPath ->
                        drawPath(
                            path = crackPath,
                            color = crackColor.copy(alpha = 0.85f * activeGlowAlpha),
                            style = Stroke(width = 1.2f)
                        )
                    }
                }
            }
            .clickable {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}
                onClick()
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            Text(text = icon, fontSize = 16.sp, modifier = Modifier.scale(iconScale))
            Text(
                text = label,
                color = if (isSelected) Color(0xFFFFD700) else OsrsParchment,
                fontSize = 9.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
