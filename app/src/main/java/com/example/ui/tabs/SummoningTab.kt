@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.models.*
import com.example.ui.components.BonusBreakdownDialog
import com.example.ui.components.BonusSourceDetail
import com.example.ui.components.dashedBorder
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SummoningTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val activeSummon by viewModel.activeSummon.collectAsStateWithLifecycle()
    val activeGolem by viewModel.activeGolem.collectAsStateWithLifecycle()
    val npcFavorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    val sedriFavorLvl = npcFavorMap["sedri"]?.first ?: viewModel.getNpcFavorLevel("sedri")

    val summoningXp = skillXpMap[OsrsSkill.FIREMAKING] ?: 0L
    val summoningLevel = OsrsXpCalculator.getLevelForXp(summoningXp)

    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showGolemTaskDialog by remember { mutableStateOf(false) }
    var showBonusBreakdownDialog by remember { mutableStateOf(false) }

    fun getItemCount(itemId: String): Int {
        val invQty = inventoryItems.find { it.id == itemId }?.quantity ?: 0
        val bankQty = bankItems.find { it.id == itemId }?.quantity ?: 0
        return invQty + bankQty
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Summoning Header Banner ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = OsrsLeatherMedium,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Title, Level & Bonus Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Summoning Guild",
                                color = OsrsTextYellow,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Level $summoningLevel • ${String.format("%,d", summoningXp)} XP",
                                color = OsrsTextOrange,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFF6A1B9A).copy(alpha = 0.45f),
                            border = BorderStroke(0.8.dp, Color(0xFFBA68C8)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clickable { showBonusBreakdownDialog = true }
                                .testTag("badge_extra_charms_chance")
                        ) {
                            Text(
                                text = "+${sedriFavorLvl}% Extra Charms ⓘ",
                                color = Color(0xFFE1BEE7),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = OsrsLeatherDark,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGoldBright)
                        ) {
                            Text(
                                text = "Lvl ${summoningLevel + 1} 🎯",
                                color = OsrsGoldBright,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                val progress = OsrsXpCalculator.getXpProgressToNextLevel(summoningXp)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF52B788),
                    trackColor = OsrsLeatherDark,
                )
            }
        }

        // --- Sedri's Dual Favors Banner (Rune-Making & Spirit Summoning) ---
        SedriDualFavorsBanner(viewModel = viewModel)

        // --- Active Golem Worker Banner (if active) ---
        val currentGolem = activeGolem
        if (currentGolem != null) {
            ActiveGolemBanner(
                golem = currentGolem,
                onOpenTaskDialog = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showGolemTaskDialog = true
                },
                onClaimRewards = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.claimGolemRewards()
                },
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.dismissActiveGolem()
                }
            )
        }

        // --- Active Companion Banner ---
        val currentSummon = activeSummon
        if (currentSummon != null && currentSummon.remainingSeconds > 0) {
            val minutes = currentSummon.remainingSeconds / 60
            val seconds = currentSummon.remainingSeconds % 60
            val formattedTime = String.format("%02d:%02d", minutes, seconds)
            val progressFraction = (currentSummon.remainingSeconds.toFloat() / currentSummon.durationSeconds.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E3326),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF52B788))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentSummon.iconEmoji, fontSize = 28.sp)
                            Column {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentSummon.animalName,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = Color(0xFF2D6A4F),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE SPIRIT",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Benefit: ${currentSummon.benefitText}",
                                    color = Color(0xFFB7E4C7),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formattedTime,
                                color = Color(0xFFFFE082),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.dismissActiveSummon()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(24.dp).testTag("dismiss_companion_btn")
                            ) {
                                Text("Dismiss", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF52B788),
                        trackColor = Color(0xFF1B4332)
                    )
                }
            }
        }

        // --- Sub-Tabs Row ---
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = OsrsLeatherMedium,
            contentColor = OsrsTextYellow,
            edgePadding = 4.dp,
            modifier = Modifier.clip(RoundedCornerShape(6.dp))
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedSubTab = 0
                },
                text = { Text("🗺️ Elemental Shrines (AFK)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedSubTab = 1
                },
                text = { Text("🌿 Druid Altar (AFK)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedSubTab = 2
                },
                text = { Text("🐾 Familiars (${SummoningData.ALL_ANIMALS.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 3,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedSubTab = 3
                },
                text = { Text("🗿 Golems (AFK Workers)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 4,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedSubTab = 4
                },
                text = { Text("🪨 My Effigies", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedSubTab) {
            0 -> {
                ElementalShrinesPanel(
                    viewModel = viewModel,
                    getItemCount = ::getItemCount
                )
            }
            1 -> {
                DruidAltarPanel(viewModel = viewModel, getItemCount = ::getItemCount, playerLevel = summoningLevel)
            }
            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SummoningData.ALL_ANIMALS, key = { it.id }) { animal ->
                        SummonableAnimalCard(
                            animal = animal,
                            playerLevel = summoningLevel,
                            getItemCount = ::getItemCount,
                            isActive = activeSummon?.animalId == animal.id,
                            onSummon = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.summonAnimal(animal)
                            },
                            onActivateTotem = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.activateTotem(animal.id)
                            },
                            onInspectItem = { itemId ->
                                viewModel.inspectItemObtain(itemId)
                            }
                        )
                    }
                }
            }
            3 -> {
                GolemsListPanel(
                    playerLevel = summoningLevel,
                    getItemCount = ::getItemCount,
                    activeGolem = activeGolem,
                    onCraftTotem = { tier ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.craftGolemTotem(tier)
                    },
                    onActivateTotem = { totemId ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.activateGolemTotem(totemId)
                    },
                    onOpenTaskDialog = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showGolemTaskDialog = true
                    },
                    onInspectItem = { itemId ->
                        viewModel.inspectItemObtain(itemId)
                    }
                )
            }
            4 -> {
                MyEffigiesPanel(getItemCount = ::getItemCount)
            }
        }
    }

    if (showGolemTaskDialog) {
        GolemTaskSelectionDialog(
            tasks = SummoningData.GOLEM_TASKS,
            currentSelectedId = activeGolem?.assignedActivityId,
            onSelectTask = { task ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.assignGolemToActivity(task)
                showGolemTaskDialog = false
            },
            onDismiss = { showGolemTaskDialog = false }
        )
    }

    if (showBonusBreakdownDialog) {
        BonusBreakdownDialog(
            title = "Extra Charm Infusion Chance",
            categoryName = "Summoning Guild & Shrines",
            iconEmoji = "🔮",
            sources = listOf(
                BonusSourceDetail(
                    title = "Sedri the Rift Inscriber's Favor (Lv. $sedriFavorLvl)",
                    description = "Grants +1% chance per favor level to receive extra charms, runes, and elemental essence when crafting pouches or harvesting shrines (Up to +50%).",
                    bonusPercent = sedriFavorLvl,
                    emoji = "🔮",
                    isUnlocked = true
                )
            ),
            note = "When triggered, bonus charms and summoning essence are awarded without extra cost!",
            onDismiss = { showBonusBreakdownDialog = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DruidAltarPanel(
    viewModel: PetViewModel,
    getItemCount: (String) -> Int,
    playerLevel: Int = 1
) {
    val haptic = LocalHapticFeedback.current

    val isAfkActive by viewModel.isAfkDruidAltarActive.collectAsStateWithLifecycle()
    val selectedEffigyId by viewModel.selectedDruidEffigyId.collectAsStateWithLifecycle()
    val progress by viewModel.druidAltarProgress.collectAsStateWithLifecycle()
    val craftedCount by viewModel.druidAltarCraftedCount.collectAsStateWithLifecycle()

    val currentRecipe = SummoningData.getEffigyRecipe(selectedEffigyId)
    val runeStock = getItemCount(currentRecipe.runeId)
    val logStock = getItemCount(currentRecipe.logId)
    val nailStock = getItemCount(currentRecipe.nailId)
    val effigyStock = getItemCount(currentRecipe.effigyId)

    val isLevelMet = playerLevel >= currentRecipe.levelReq
    val hasMaterials = runeStock >= currentRecipe.runeCount && logStock >= currentRecipe.logCount && nailStock >= currentRecipe.nailCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- Header Banner ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OsrsLeatherMedium,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌿", fontSize = 24.sp)
                            Text(
                                text = "Druid Altar (Effigy Forging)",
                                color = OsrsTextYellow,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Each Spirit Effigy requires its own specific recipe combining Runes + Timber Logs + Metal Nails at the Druid Altar. Craft manually or start an AFK ritual to forge effigies over time!",
                        color = OsrsTextWhite,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- Active AFK Ritual Status Card ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isAfkActive) Color(0xFF1B382B) else OsrsLeatherMedium,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isAfkActive) OsrsGoldBright else OsrsLeatherDark)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAfkActive) "🌿 DRUID ALTAR RITUAL ACTIVE" else "⏸️ DRUID ALTAR IDLE",
                                color = if (isAfkActive) OsrsGoldBright else OsrsTextYellow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isAfkActive) {
                                Text(
                                    text = "Forging: ${currentRecipe.effigyName} ${currentRecipe.emoji} • Session Output: $craftedCount",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.toggleAfkDruidAltar()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAfkActive) Color(0xFFC62828) else OsrsRedFrame
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("toggle_druid_altar_afk_btn")
                        ) {
                            Text(
                                text = if (isAfkActive) "🛑 Stop Altar Ritual" else "🌿 Start AFK Ritual",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isAfkActive) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = OsrsGoldBright,
                            trackColor = OsrsLeatherDark
                        )
                    }
                }
            }
        }

        // --- Effigy Selector Carousel ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OsrsLeatherMedium,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OsrsLeatherDark)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Effigy Recipe:", color = OsrsTextYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummoningData.ALL_DRUID_EFFIGY_RECIPES.forEach { recipe ->
                            val isSelected = recipe.effigyId == selectedEffigyId
                            val isUnlocked = playerLevel >= recipe.levelReq
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.setSelectedDruidEffigyId(recipe.effigyId)
                                        },
                                        onLongClick = {
                                            viewModel.inspectItemObtain(recipe.effigyId)
                                        }
                                    ),
                                color = if (isSelected) Color(0xFF2E4D3B) else if (!isUnlocked) Color(0xFF242424) else OsrsLeatherDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) OsrsGoldBright else if (isUnlocked) OsrsLeatherMedium else Color(0xFF3E3E3E)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(recipe.emoji, fontSize = 20.sp)
                                    Text(
                                        text = recipe.effigyName.replace(" Effigy", ""),
                                        fontSize = 11.sp,
                                        color = if (isSelected) OsrsTextYellow else if (isUnlocked) Color.White else Color.Gray,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "Lvl ${recipe.levelReq}",
                                        fontSize = 9.sp,
                                        color = if (isUnlocked) Color(0xFFA5D6A7) else OsrsTextOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Active Selected Effigy Recipe Card ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OsrsLeatherMedium,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentRecipe.emoji, fontSize = 32.sp)
                            Column {
                                Text(
                                    text = currentRecipe.effigyName,
                                    color = OsrsTextYellow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Requires Lvl ${currentRecipe.levelReq} Summoning • +${currentRecipe.xpReward} XP",
                                    color = if (isLevelMet) Color(0xFFA5D6A7) else OsrsTextOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.craftDruidAltarEffigy(isAfk = false)
                            },
                            enabled = hasMaterials && isLevelMet,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OsrsRedFrame,
                                disabledContainerColor = Color(0xFF3E3E3E)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("craft_single_effigy_btn")
                        ) {
                            Text("Forge 1 Now 🔨", fontSize = 11.sp, color = if (hasMaterials && isLevelMet) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = currentRecipe.description,
                        color = OsrsTextWhite,
                        fontSize = 11.sp
                    )

                    HorizontalDivider(color = OsrsLeatherDark, thickness = 1.dp)

                    Text(
                        text = "Required Ingredients for ${currentRecipe.effigyName}:",
                        color = OsrsGoldBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Recipe Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RequirementBadge(
                            label = "${currentRecipe.runeCount}x ${currentRecipe.runeName}",
                            count = runeStock,
                            required = currentRecipe.runeCount,
                            onLongClick = { viewModel.inspectItemObtain(currentRecipe.runeId) }
                        )
                        RequirementBadge(
                            label = "${currentRecipe.logCount}x ${currentRecipe.logName}",
                            count = logStock,
                            required = currentRecipe.logCount,
                            onLongClick = { viewModel.inspectItemObtain(currentRecipe.logId) }
                        )
                        RequirementBadge(
                            label = "${currentRecipe.nailCount}x ${currentRecipe.nailName}",
                            count = nailStock,
                            required = currentRecipe.nailCount,
                            onLongClick = { viewModel.inspectItemObtain(currentRecipe.nailId) }
                        )
                    }

                    Text(
                        text = "Effigies in Inventory/Bank: $effigyStock",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // --- Recipe Catalog / Reference ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OsrsLeatherMedium,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OsrsLeatherDark)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📖 All 15 Effigy Recipes Reference",
                        color = OsrsTextYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    SummoningData.ALL_DRUID_EFFIGY_RECIPES.forEach { recipe ->
                        val isSelected = recipe.effigyId == selectedEffigyId
                        val isUnlocked = playerLevel >= recipe.levelReq
                        val rCount = getItemCount(recipe.runeId)
                        val lCount = getItemCount(recipe.logId)
                        val nCount = getItemCount(recipe.nailId)
                        val canMake = rCount >= recipe.runeCount && lCount >= recipe.logCount && nCount >= recipe.nailCount && isUnlocked

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setSelectedDruidEffigyId(recipe.effigyId)
                                },
                            color = if (isSelected) Color(0xFF233B2E) else OsrsLeatherDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) OsrsGoldBright else Color.Transparent
                            )
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
                                    Text(recipe.emoji, fontSize = 20.sp)
                                    Column {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = recipe.effigyName,
                                                color = if (isSelected) OsrsGoldBright else OsrsTextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Lvl ${recipe.levelReq}",
                                                color = if (isUnlocked) Color(0xFFA5D6A7) else OsrsTextOrange,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Text(
                                            text = "${recipe.runeCount}x ${recipe.runeName} + ${recipe.logCount}x ${recipe.logName} + ${recipe.nailCount}x ${recipe.nailName}",
                                            color = Color.LightGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Surface(
                                    color = if (canMake) Color(0xFF2E7D32) else Color(0xFF424242),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (canMake) "READY" else "LOCKED/NEED MATS",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RequirementBadge(label: String, count: Int, required: Int, onLongClick: (() -> Unit)? = null) {
    val isMet = count >= required
    Surface(
        color = if (isMet) Color(0xFF1B382B) else Color(0xFF3E2323),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isMet) Color(0xFF81C784) else Color(0xFFE57373)),
        modifier = if (onLongClick != null) Modifier.combinedClickable(onClick = { onLongClick() }, onLongClick = onLongClick) else Modifier
    ) {
        Text(
            text = "$label (${count}/$required)",
            fontSize = 10.sp,
            color = if (isMet) Color(0xFFA5D6A7) else Color(0xFFFFCDD2),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SummonableAnimalCard(
    animal: SummonableAnimal,
    playerLevel: Int,
    getItemCount: (String) -> Int,
    isActive: Boolean,
    onSummon: () -> Unit,
    onActivateTotem: (() -> Unit)? = null,
    onInspectItem: ((String) -> Unit)? = null
) {
    val isLevelMet = playerLevel >= animal.levelRequired
    val hasEffigies = animal.requiredEffigies.all { (effigyId, count) -> getItemCount(effigyId) >= count }
    val canSummon = isLevelMet && hasEffigies
    val totemCount = getItemCount("item_totem_${animal.id}")

    val borderColor = when {
        isActive -> Color(0xFF52B788)
        !isLevelMet -> Color(0xFF4A4A4A)
        hasEffigies -> OsrsGold
        else -> OsrsLeatherDark
    }

    val cardBg = when {
        isActive -> Color(0xFF1E3326)
        !isLevelMet -> Color(0xFF1B1B1B)
        else -> OsrsLeatherMedium
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBg,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(animal.iconEmoji, fontSize = 24.sp)
                    Column {
                        Text(
                            text = animal.name,
                            color = if (isLevelMet) OsrsTextYellow else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = animal.description,
                            color = OsrsTextWhite,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = if (isLevelMet) Color(0xFF2D6A4F) else Color(0xFF424242),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Lvl ${animal.levelRequired}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = OsrsLeatherDark,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGoldBright)
                ) {
                    Text(
                        text = "✨ ${animal.benefitText}",
                        color = OsrsGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "⏱️ ${animal.durationSeconds / 60} mins",
                    color = OsrsTextOrange,
                    fontSize = 11.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cost:", color = OsrsTextOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    animal.requiredEffigies.forEach { (effigyId, reqCount) ->
                        val currentCount = getItemCount(effigyId)
                        val effigyItem = DefaultItems.ALL_SHOP_ITEMS.find { it.id == effigyId }
                        Surface(
                            color = if (currentCount >= reqCount) Color(0xFF1B382B) else Color(0xFF3E2323),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (currentCount >= reqCount) Color(0xFF81C784) else Color(0xFFE57373)),
                            modifier = if (onInspectItem != null) Modifier.combinedClickable(
                                onClick = { onInspectItem(effigyId) },
                                onLongClick = { onInspectItem(effigyId) }
                            ) else Modifier
                        ) {
                            Text(
                                text = "${effigyItem?.iconEmoji ?: "🗿"} $currentCount/$reqCount",
                                fontSize = 10.sp,
                                color = if (currentCount >= reqCount) Color(0xFFA5D6A7) else Color(0xFFFFCDD2),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Button to craft Totem
                    Button(
                        onClick = onSummon,
                        enabled = canSummon,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(0xFF2E7D32) else OsrsRedFrame,
                            disabledContainerColor = Color(0xFF3E3E3E)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("summon_${animal.id}_btn")
                    ) {
                        Text(
                            text = when {
                                !isLevelMet -> "Locked"
                                !hasEffigies -> "Need Effigy"
                                else -> "🗿 Craft Totem"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLevelMet && hasEffigies) OsrsTextYellow else Color.LightGray
                        )
                    }

                    // Button to activate Totem if user has 1 or more in inventory
                    if (totemCount > 0 && onActivateTotem != null) {
                        Button(
                            onClick = onActivateTotem,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B5E20)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("activate_totem_${animal.id}_btn")
                        ) {
                            Text(
                                text = "✨ USE (x$totemCount)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfuseEffigiesPanel(
    getItemCount: (String) -> Int,
    onInfuse: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OsrsLeatherMedium,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🔮 Instant Infuse Runes into Spirit Effigies", color = OsrsTextYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Directly channel 5 runes into an instant Spirit Effigy without timber or nails, or visit the Druid Altar tab for the dedicated recipes with timber & nails!", color = OsrsTextWhite, fontSize = 11.sp)
                }
            }
        }

        items(SummoningData.ALL_DRUID_EFFIGY_RECIPES, key = { it.effigyId }) { recipe ->
            val runeItem = DefaultItems.ALL_SHOP_ITEMS.find { it.id == recipe.runeId }
            val effigyItem = DefaultItems.ALL_SHOP_ITEMS.find { it.id == recipe.effigyId }

            val runeCount = getItemCount(recipe.runeId)
            val effigyCount = getItemCount(recipe.effigyId)
            val canInfuse = runeCount >= 5

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OsrsLeatherMedium,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (canInfuse) OsrsGold else OsrsLeatherDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(recipe.emoji, fontSize = 24.sp)
                        Column {
                            Text(
                                text = "${recipe.effigyName} (${runeItem?.name ?: recipe.runeName})",
                                color = OsrsTextYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Runes: $runeCount/5 • Owned: $effigyCount",
                                color = if (canInfuse) Color(0xFFA5D6A7) else OsrsTextOrange,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Druid Altar Recipe: 1x ${recipe.runeName} + 1x ${recipe.logName} + 5x ${recipe.nailName}",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Button(
                        onClick = { onInfuse(recipe.runeId) },
                        enabled = canInfuse,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OsrsRedFrame,
                            disabledContainerColor = Color(0xFF3E3E3E)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("infuse_${recipe.runeId}_btn")
                    ) {
                        Text(
                            text = if (canInfuse) "Infuse 🔮" else "Need 5",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canInfuse) OsrsTextYellow else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyEffigiesPanel(
    getItemCount: (String) -> Int,
    modifier: Modifier = Modifier
) {
    val effigies = SummoningData.EFFIGY_ITEMS

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OsrsLeatherDark,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "My Effigy Collection 🗿",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = OsrsGoldBright
                    )
                    Text(
                        text = "Effigies are used to invoke Spirit Companions and construct Golem Totems. Forge them from Runes at the Druid Altar.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        items(effigies.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (effigy in pair) {
                    val count = getItemCount(effigy.id)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("effigy_card_${effigy.id}"),
                        color = if (count > 0) Color(0xFF1E281E) else OsrsLeatherDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (count > 0) Color(0xFF4CAF50) else OsrsGold
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = effigy.iconEmoji,
                                fontSize = 24.sp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = effigy.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (count > 0) OsrsTextYellow else Color.Gray,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Owned: $count",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (count > 0) Color(0xFF81C784) else Color.DarkGray
                                )
                            }
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ActiveGolemBanner(
    golem: ActiveGolemState,
    onOpenTaskDialog: () -> Unit,
    onClaimRewards: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tier = SummoningData.getGolemTier(golem.tierId)
    val task = golem.assignedActivityId?.let { SummoningData.getGolemTask(it) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (golem.isCompleted) Color(0xFF2C2411) else if (golem.isWorking) Color(0xFF1E2A38) else Color(0xFF2D2518),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (golem.isCompleted) OsrsGoldBright else if (golem.isWorking) Color(0xFF64B5F6) else Color(0xFFFFB74D)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(tier.iconEmoji, fontSize = 28.sp)
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = golem.golemName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = if (golem.isCompleted) OsrsGold else if (golem.isWorking) Color(0xFF1976D2) else Color(0xFFE65100),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (golem.isCompleted) "COMPLETED" else if (golem.isWorking) "WORKING" else "PAUSED (AWAITING TASK)",
                                    color = if (golem.isCompleted) Color.Black else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (golem.isWorking && task != null) {
                            Text(
                                text = "${task.emoji} Working: ${task.name}",
                                color = Color(0xFFBBDEFB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (golem.isAwaitingActivity) {
                            Text(
                                text = "⏸️ Timer paused until you assign an activity.",
                                color = Color(0xFFFFCC80),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (golem.isCompleted) {
                            Text(
                                text = "🎉 All tasks complete! Claim your rewards.",
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (golem.isWorking) {
                    val minutes = golem.remainingSeconds / 60
                    val seconds = golem.remainingSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        color = Color(0xFF90CAF9),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (golem.isWorking) {
                val progress = (golem.remainingSeconds.toFloat() / golem.durationSeconds.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF64B5F6),
                    trackColor = Color(0xFF0D47A1)
                )

                // Stats ticker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ Actions: ${golem.completedActions}",
                        color = Color(0xFFE0E0E0),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "+${String.format("%,d", golem.accumulatedXp)} XP  •  +${String.format("%,d", golem.accumulatedGp)} GP",
                        color = OsrsGoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Accumulated loot preview
                if (golem.accumulatedLoot.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        golem.accumulatedLoot.forEach { loot ->
                            Surface(
                                color = Color(0xFF102027),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF37474F))
                            ) {
                                Text(
                                    text = "${loot.iconEmoji} ${loot.name} x${loot.quantity}",
                                    color = Color(0xFFCFD8DC),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (golem.isCompleted || golem.completedActions > 0 || golem.accumulatedLoot.isNotEmpty()) {
                    Button(
                        onClick = onClaimRewards,
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsGold),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("claim_golem_rewards_btn")
                    ) {
                        Text(
                            text = if (golem.isCompleted) "🎁 Claim All Rewards" else "🎁 Claim Current Loot",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (golem.isAwaitingActivity) {
                    Button(
                        onClick = onOpenTaskDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("assign_golem_task_btn")
                    ) {
                        Text(
                            text = "⚡ Assign AFK Task",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else if (golem.isWorking) {
                    Button(
                        onClick = onOpenTaskDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("change_golem_task_btn")
                    ) {
                        Text(
                            text = "🔄 Change Task",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsRedFrame),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(36.dp).testTag("dismiss_golem_btn")
                ) {
                    Text("Dismiss", color = Color(0xFFEF9A9A), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun GolemsListPanel(
    playerLevel: Int,
    getItemCount: (String) -> Int,
    activeGolem: ActiveGolemState?,
    onCraftTotem: (GolemTier) -> Unit,
    onActivateTotem: (String) -> Unit,
    onOpenTaskDialog: () -> Unit,
    onInspectItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hero Explanation Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2A2318),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🗿", fontSize = 28.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Autonomous Golem Workers",
                            color = OsrsTextYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Summon Golems to perform a 2nd concurrent AFK activity! When activated, the timer won't start until you assign an activity. They gather items, XP, and GP autonomously.",
                            color = OsrsTextWhite,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        items(SummoningData.GOLEM_TIERS, key = { it.id }) { tier ->
            GolemTierCard(
                tier = tier,
                playerLevel = playerLevel,
                getItemCount = getItemCount,
                isActive = activeGolem?.tierId == tier.id,
                isAwaitingTask = activeGolem?.tierId == tier.id && activeGolem.isAwaitingActivity,
                onCraft = { onCraftTotem(tier) },
                onActivate = { onActivateTotem(tier.totemItemId) },
                onAssignTask = onOpenTaskDialog,
                onInspectItem = onInspectItem
            )
        }
    }
}

@Composable
fun GolemTierCard(
    tier: GolemTier,
    playerLevel: Int,
    getItemCount: (String) -> Int,
    isActive: Boolean,
    isAwaitingTask: Boolean,
    onCraft: () -> Unit,
    onActivate: () -> Unit,
    onAssignTask: () -> Unit,
    onInspectItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = playerLevel >= tier.reqLevel
    val totemCount = getItemCount(tier.totemItemId)
    val hasIngredients = tier.requiredEffigies.all { getItemCount(it.effigyItemId) >= it.quantity }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isActive) Color(0xFF1A2B3C) else if (isUnlocked) OsrsLeatherMedium else Color(0xFF24201A),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isActive) Color(0xFF64B5F6) else if (isUnlocked) OsrsGold else Color.DarkGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tier.iconEmoji, fontSize = 28.sp)
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tier.name,
                                color = if (isUnlocked) OsrsTextYellow else Color.Gray,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isActive) {
                                Surface(
                                    color = Color(0xFF1976D2),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isAwaitingTask) "ACTIVE (PAUSED)" else "ACTIVE WORKER",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Lvl ${tier.reqLevel} Summoning • ⏱️ ${tier.durationMinutes} min AFK Session",
                            color = if (isUnlocked) OsrsTextOrange else Color.DarkGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    color = if (totemCount > 0) Color(0xFF1B382B) else OsrsLeatherDark,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (totemCount > 0) Color(0xFF52B788) else Color.DarkGray)
                ) {
                    Text(
                        text = "Totems: $totemCount",
                        color = if (totemCount > 0) Color(0xFFFFE082) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = tier.description,
                color = if (isUnlocked) OsrsTextWhite else Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Recipe / Ingredients Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OsrsLeatherDark, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Crafting Materials:",
                    color = OsrsTextOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tier.requiredEffigies.forEach { req ->
                        val owned = getItemCount(req.effigyItemId)
                        val met = owned >= req.quantity
                        val effigyItem = SummoningData.EFFIGY_ITEMS.find { it.id == req.effigyItemId }

                        Surface(
                            modifier = Modifier
                                .clickable { onInspectItem(req.effigyItemId) }
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (met) Color(0xFF1B382B) else Color(0xFF332020),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, if (met) Color(0xFF52B788) else Color(0xFFE57373))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(effigyItem?.iconEmoji ?: "🪨", fontSize = 12.sp)
                                Text(
                                    text = "${effigyItem?.name ?: req.effigyItemId}: $owned/${req.quantity}",
                                    color = if (met) Color(0xFFB7E4C7) else Color(0xFFFFCDD2),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Buttons
            if (!isUnlocked) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF3E2723),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "🔒 Requires Level ${tier.reqLevel} Summoning",
                        color = Color(0xFFFFCCBC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCraft,
                        enabled = hasIngredients,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OsrsRedFrame,
                            disabledContainerColor = Color(0xFF3E3E3E)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("craft_${tier.id}_btn")
                    ) {
                        Text(
                            text = "Craft Totem 🗿",
                            color = if (hasIngredients) OsrsTextYellow else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    if (isActive && isAwaitingTask) {
                        Button(
                            onClick = onAssignTask,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("assign_${tier.id}_btn")
                        ) {
                            Text(
                                text = "⚡ Assign Task",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = onActivate,
                            enabled = totemCount > 0 && !isActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                disabledContainerColor = Color(0xFF3E3E3E)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("activate_${tier.id}_btn")
                        ) {
                            Text(
                                text = if (isActive) "Active Worker" else if (totemCount > 0) "Activate Totem ⚡" else "Need Totem",
                                color = if (totemCount > 0 && !isActive) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GolemTaskSelectionDialog(
    tasks: List<GolemTaskOption> = SummoningData.GOLEM_TASKS,
    currentSelectedId: String? = null,
    onSelectTask: (GolemTaskOption) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            color = OsrsLeatherDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, OsrsGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⚡ Assign Golem Activity",
                            color = OsrsTextYellow,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose an AFK task for your Golem to work on.",
                            color = OsrsTextOrange,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Text("✕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = OsrsGold, thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        val isSelected = task.id == currentSelectedId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTask(task) },
                            color = if (isSelected) Color(0xFF1B382B) else OsrsLeatherMedium,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF52B788) else OsrsLeatherDark
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(task.emoji, fontSize = 28.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = task.name,
                                            color = if (isSelected) Color(0xFFB7E4C7) else OsrsTextYellow,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = OsrsLeatherDark,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = task.skill.displayName,
                                                color = OsrsTextOrange,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = task.description,
                                        color = OsrsTextWhite,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Base Rate: 1 action per 4s • Yields ${task.skill.displayName} XP, GP & Resources",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 10.sp
                                    )
                                }

                                Button(
                                    onClick = { onSelectTask(task) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF2E7D32) else OsrsRedFrame
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("select_task_${task.id}_btn")
                                ) {
                                    Text(
                                        text = if (isSelected) "Active ✓" else "Assign",
                                        color = OsrsTextYellow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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

data class ElementalShrineMapNode(
    val runeInfo: RunecraftRuneInfo,
    val shrineName: String,
    val posXRatio: Float,
    val posYRatio: Float,
    val themeColor: Color,
    val lore: String
)

val ELEMENTAL_SHRINES_NODES: List<ElementalShrineMapNode> = listOf(
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_air" } ?: RunecraftData.CRAFTABLE_RUNES[0],
        shrineName = "Air Shrine",
        posXRatio = 0.12f,
        posYRatio = 0.22f,
        themeColor = Color(0xFFECEFF1),
        lore = "Perched upon gusty cliffs where atmospheric currents condense into pure swift winds."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_mind" } ?: RunecraftData.CRAFTABLE_RUNES[1],
        shrineName = "Mind Shrine",
        posXRatio = 0.28f,
        posYRatio = 0.16f,
        themeColor = Color(0xFFFFE082),
        lore = "A serene monastery sanctuary vibrating with monastic meditation and mental clarity."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_water" } ?: RunecraftData.CRAFTABLE_RUNES[2],
        shrineName = "Water Shrine",
        posXRatio = 0.16f,
        posYRatio = 0.44f,
        themeColor = Color(0xFF64B5F6),
        lore = "Submerged coral grotto where oceanic tides infuse raw essence into liquid magic."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_earth" } ?: RunecraftData.CRAFTABLE_RUNES[3],
        shrineName = "Earth Shrine",
        posXRatio = 0.34f,
        posYRatio = 0.36f,
        themeColor = Color(0xFFA1887F),
        lore = "Subterranean cavern where deep tectonic plates bind stone into steadfast physical runes."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_fire" } ?: RunecraftData.CRAFTABLE_RUNES[4],
        shrineName = "Fire Shrine",
        posXRatio = 0.50f,
        posYRatio = 0.20f,
        themeColor = Color(0xFFFF7043),
        lore = "Blazing volcanic caldera radiating incandescent elemental heat and volcanic magma."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_body" } ?: RunecraftData.CRAFTABLE_RUNES[5],
        shrineName = "Body Shrine",
        posXRatio = 0.40f,
        posYRatio = 0.54f,
        themeColor = Color(0xFFB0BEC5),
        lore = "Ancient obsidian monolith bolstering spiritual endurance and structural vitality."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_cosmic" } ?: RunecraftData.CRAFTABLE_RUNES[6],
        shrineName = "Cosmic Shrine",
        posXRatio = 0.64f,
        posYRatio = 0.32f,
        themeColor = Color(0xFFBA68C8),
        lore = "Floating starlight plateau aligned with celestial constellations and planar geometries."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_chaos" } ?: RunecraftData.CRAFTABLE_RUNES[7],
        shrineName = "Chaos Shrine",
        posXRatio = 0.78f,
        posYRatio = 0.18f,
        themeColor = Color(0xFFE57373),
        lore = "Volatile obsidian chasm crackling with wild chaotic currents and unstable surges."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_astral" } ?: RunecraftData.CRAFTABLE_RUNES[8],
        shrineName = "Astral Shrine",
        posXRatio = 0.86f,
        posYRatio = 0.38f,
        themeColor = Color(0xFF81D4FA),
        lore = "Lunar haven surrounded by glowing auroras where moon mages manipulate time and fate."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_nature" } ?: RunecraftData.CRAFTABLE_RUNES[9],
        shrineName = "Nature Shrine",
        posXRatio = 0.58f,
        posYRatio = 0.62f,
        themeColor = Color(0xFF81C784),
        lore = "Verdant heartwood shrine nestled in giant mossy roots teeming with boundless growth."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_law" } ?: RunecraftData.CRAFTABLE_RUNES[10],
        shrineName = "Law Shrine",
        posXRatio = 0.72f,
        posYRatio = 0.54f,
        themeColor = Color(0xFFFFD54F),
        lore = "Sacred white marble temple commanding spatial justice and instant teleportation."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_death" } ?: RunecraftData.CRAFTABLE_RUNES[11],
        shrineName = "Death Shrine",
        posXRatio = 0.82f,
        posYRatio = 0.68f,
        themeColor = Color(0xFF9E9E9E),
        lore = "Shadowed ruins maintaining the cosmic threshold between mortality and oblivion."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_blood" } ?: RunecraftData.CRAFTABLE_RUNES[12],
        shrineName = "Blood Shrine",
        posXRatio = 0.90f,
        posYRatio = 0.82f,
        themeColor = Color(0xFFEF5350),
        lore = "Deep sanguine catacomb pulsing with ancient vampyric vigor and primal life essence."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_soul" } ?: RunecraftData.CRAFTABLE_RUNES[13],
        shrineName = "Soul Shrine",
        posXRatio = 0.52f,
        posYRatio = 0.84f,
        themeColor = Color(0xFF4DD0E1),
        lore = "Ethereal spirit nexus bridging the material world with the celestial spirit plane."
    ),
    ElementalShrineMapNode(
        runeInfo = RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == "item_rune_wrath" } ?: RunecraftData.CRAFTABLE_RUNES[14],
        shrineName = "Wrath Shrine",
        posXRatio = 0.28f,
        posYRatio = 0.78f,
        themeColor = Color(0xFFFFB300),
        lore = "Furious draconic peaks where primordial thunder and wrathful tempest fury culminate."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ElementalShrinesPanel(
    viewModel: PetViewModel,
    getItemCount: (String) -> Int
) {
    val haptic = LocalHapticFeedback.current
    val isAfkRunecrafting by viewModel.isAfkRunecraftingActive.collectAsStateWithLifecycle()
    val targetRuneId by viewModel.afkRunecraftTargetRuneId.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()

    val rcXp = skillXpMap[OsrsSkill.RUNECRAFT] ?: 0L
    val rcLevel = OsrsXpCalculator.getLevelForXp(rcXp)

    val runeEssenceStock = getItemCount("item_rune_essence")
    val pureEssenceStock = getItemCount("item_pure_essence")
    val totalEssence = runeEssenceStock + pureEssenceStock

    val currentTargetShrine = ELEMENTAL_SHRINES_NODES.find { it.runeInfo.runeItemId == targetRuneId }
        ?: ELEMENTAL_SHRINES_NODES.first()

    var inspectedShrine by remember { mutableStateOf<ElementalShrineMapNode?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- Top Header Card ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OsrsLeatherMedium,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Title, Level & Essence Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔮", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Elemental Shrines Map",
                                    color = OsrsTextYellow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Runecrafting Level $rcLevel • ${String.format("%,d", rcXp)} XP",
                                    color = OsrsTextOrange,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            color = if (totalEssence > 0) Color(0xFF1B4332) else Color(0xFF4A1E1E),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (totalEssence > 0) Color(0xFF52B788) else Color(0xFFE57373))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✨", fontSize = 12.sp)
                                Text(
                                    text = "Essence: $totalEssence",
                                    color = if (totalEssence > 0) Color(0xFFB7E4C7) else Color(0xFFFFCDD2),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Channels raw Rune Essence or Pure Essence into elemental & magical runes at ancient world shrines (1 Essence ➔ 1 Rune per action). Tap a shrine pin or card to toggle AFK crafting.",
                        color = OsrsTextWhite,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- Interactive Elemental Shrines Map ---
        item {
            ElementalShrinesMapCard(
                userRcLevel = rcLevel,
                selectedRuneId = targetRuneId,
                isAfkActive = isAfkRunecrafting,
                onSelectShrine = { shrine ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (rcLevel >= shrine.runeInfo.reqLevel) {
                        viewModel.toggleAfkRunecrafting(shrine.runeInfo.runeItemId)
                    } else {
                        inspectedShrine = shrine
                    }
                },
                onLongPressShrine = { shrine ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    inspectedShrine = shrine
                }
            )
        }

        // --- Active AFK Conversion Control Bar ---
        item {
            val multiplier = RunecraftData.getMultiplier(rcLevel, currentTargetShrine.runeInfo.runeItemId)
            val isTargetLevelMet = rcLevel >= currentTargetShrine.runeInfo.reqLevel

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isAfkRunecrafting) Color(0xFF1B382B) else OsrsLeatherMedium,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isAfkRunecrafting) OsrsGoldBright else OsrsLeatherDark)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Shrine info & details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentTargetShrine.runeInfo.iconEmoji, fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentTargetShrine.shrineName,
                                        color = if (isAfkRunecrafting) OsrsGoldBright else OsrsTextYellow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        color = if (isAfkRunecrafting) Color(0xFF2E7D32) else OsrsLeatherDark,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (isAfkRunecrafting) "ACTIVE" else "TARGET",
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${currentTargetShrine.runeInfo.runeName} • +${(currentTargetShrine.runeInfo.xpPerEssence * 10).toInt()} Runecraft XP",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Row 2: Full width Action Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleAfkRunecrafting(currentTargetShrine.runeInfo.runeItemId)
                        },
                        enabled = isTargetLevelMet || isAfkRunecrafting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAfkRunecrafting) Color(0xFFC62828) else Color(0xFF2E7D32),
                            disabledContainerColor = Color(0xFF3E3E3E)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("toggle_elemental_shrine_afk_btn")
                    ) {
                        Text(
                            text = if (isAfkRunecrafting) "🛑 Stop AFK Shrine Conversion" else if (!isTargetLevelMet) "🔒 Requires Runecrafting Level ${currentTargetShrine.runeInfo.reqLevel}" else "⚡ Start AFK Shrine Conversion",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isAfkRunecrafting) {
                        val infiniteTransition = rememberInfiniteTransition(label = "afk_shrine_pulse")
                        val animProgress by infiniteTransition.animateFloat(
                            initialValue = 0.1f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "shrine_progress"
                        )
                        LinearProgressIndicator(
                            progress = { animProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = OsrsGoldBright,
                            trackColor = OsrsLeatherDark
                        )
                    }
                }
            }
        }

        // --- All 15 Shrines Carousel / Grid ---
        item {
            Text(
                text = "✨ All Elemental Shrines (${ELEMENTAL_SHRINES_NODES.size})",
                color = OsrsTextYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(ELEMENTAL_SHRINES_NODES, key = { it.runeInfo.runeItemId }) { shrine ->
            val isUnlocked = rcLevel >= shrine.runeInfo.reqLevel
            val isCurrentTarget = shrine.runeInfo.runeItemId == targetRuneId
            val shrineMultiplier = RunecraftData.getMultiplier(rcLevel, shrine.runeInfo.runeItemId)
            val currentRuneStock = getItemCount(shrine.runeInfo.runeItemId)
            val isDotted = !isUnlocked

            val itemBorderColor = when {
                isCurrentTarget -> OsrsGoldBright
                isUnlocked -> Color(0xFF388E3C)
                else -> Color(0xFFFFD54F)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDotted) Modifier.dashedBorder(
                            width = 1.dp,
                            color = itemBorderColor,
                            shape = RoundedCornerShape(8.dp),
                            dashLength = 4.dp,
                            gapLength = 4.dp
                        ) else Modifier
                    )
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            inspectedShrine = shrine
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            inspectedShrine = shrine
                        }
                    ),
                color = if (isCurrentTarget) Color(0xFF1E3326) else if (!isUnlocked) Color(0xFF201A15) else OsrsLeatherMedium,
                shape = RoundedCornerShape(8.dp),
                border = if (!isDotted) androidx.compose.foundation.BorderStroke(
                    1.dp,
                    itemBorderColor
                ) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = shrine.themeColor.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, shrine.themeColor),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(shrine.runeInfo.iconEmoji, fontSize = 20.sp)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = shrine.shrineName,
                                    color = if (isUnlocked) Color.White else Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isUnlocked) "Lv. ${shrine.runeInfo.reqLevel}" else "🔒 Lv. ${shrine.runeInfo.reqLevel}",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (isUnlocked) "+${(shrine.runeInfo.xpPerEssence * 10).toInt()} XP/action • Owned: $currentRuneStock"
                                else "Requires Level ${shrine.runeInfo.reqLevel} Runecrafting",
                                color = if (isUnlocked) Color(0xFFB7E4C7) else Color(0xFFEF9A9A),
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                inspectedShrine = shrine
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("ℹ️", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.toggleAfkRunecrafting(shrine.runeInfo.runeItemId)
                            },
                            enabled = isUnlocked || (isCurrentTarget && isAfkRunecrafting),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentTarget && isAfkRunecrafting) Color(0xFFC62828)
                                else if (isCurrentTarget) Color(0xFF2E7D32)
                                else OsrsRedFrame,
                                disabledContainerColor = Color(0xFF424242)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (isCurrentTarget && isAfkRunecrafting) "Stop 🛑" else if (isCurrentTarget) "Active ✓" else "Craft ⚡",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (inspectedShrine != null) {
        val shrine = inspectedShrine!!
        ShrineDetailsDialog(
            shrine = shrine,
            userRcLevel = rcLevel,
            runeStock = getItemCount(shrine.runeInfo.runeItemId),
            essenceStock = totalEssence,
            isCurrentTarget = shrine.runeInfo.runeItemId == targetRuneId,
            isAfkActive = isAfkRunecrafting,
            onSelectAndStart = {
                viewModel.toggleAfkRunecrafting(shrine.runeInfo.runeItemId)
                inspectedShrine = null
            },
            onDismiss = { inspectedShrine = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ElementalShrinesMapCard(
    userRcLevel: Int,
    selectedRuneId: String,
    isAfkActive: Boolean,
    onSelectShrine: (ElementalShrineMapNode) -> Unit,
    onLongPressShrine: (ElementalShrineMapNode) -> Unit
) {
    val shrines = ELEMENTAL_SHRINES_NODES

    val infiniteTransition = rememberInfiniteTransition(label = "shrine_map_ping")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shrine_pulse_alpha"
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141824)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, OsrsGold),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("elemental_shrines_map_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🗺️", fontSize = 14.sp)
                    Text(
                        "World Elemental Shrines",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("↔️ Scrollable", color = Color(0xFFB0BEC5), fontSize = 9.sp)
                    Text("•", color = Color.Gray, fontSize = 9.sp)
                    Text(
                        "Hold pin for info",
                        color = Color(0xFF81D4FA),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Scrollable Map Canvas
            val horizontalScrollState = rememberScrollState()
            val mapWidthDp = 820.dp
            val mapHeightDp = 580.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color(0xFF3F51B5), RoundedCornerShape(6.dp))
                    .horizontalScroll(horizontalScrollState)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = mapWidthDp, height = mapHeightDp)
                        .background(Color(0xFF0A0E1A))
                ) {
                    // Generated Thematic World Map Background
                    Image(
                        painter = painterResource(id = R.drawable.img_summoning_map_bg),
                        contentDescription = "Elemental Shrines World Map Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.85f)
                    )

                    // Procedural Cosmic / Ley-Line Map Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Subtle outer vignette to frame the parchment map edges nicely
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color(0x990A0E1A)),
                                center = Offset(w * 0.5f, h * 0.5f),
                                radius = w * 0.65f
                            )
                        )

                        // Starlight grid lines
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val gridColor = Color(0xFF64B5F6).copy(alpha = 0.22f)

                        var gx = 60f
                        while (gx < w) {
                            drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), pathEffect = dashEffect)
                            gx += 70f
                        }
                        var gy = 60f
                        while (gy < h) {
                            drawLine(gridColor, Offset(0f, gy), Offset(w, gy), pathEffect = dashEffect)
                            gy += 70f
                        }

                        // Connecting Ley Lines Path
                        val path = Path()
                        shrines.forEachIndexed { index, shrine ->
                            val px = shrine.posXRatio * w
                            val py = shrine.posYRatio * h
                            if (index == 0) {
                                path.moveTo(px, py)
                            } else {
                                path.lineTo(px, py)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF64B5F6).copy(alpha = 0.45f),
                            style = Stroke(width = 3.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f))
                        )

                        // Ambient energy nodes / glows on shrines
                        shrines.forEach { shrine ->
                            val px = shrine.posXRatio * w
                            val py = shrine.posYRatio * h
                            val isUnlocked = userRcLevel >= shrine.runeInfo.reqLevel
                            val isTarget = shrine.runeInfo.runeItemId == selectedRuneId

                            val glowColor = if (isTarget) OsrsGoldBright else if (isUnlocked) shrine.themeColor else Color.DarkGray
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(glowColor.copy(alpha = if (isTarget) 0.5f * pulseAlpha else 0.25f), Color.Transparent),
                                    center = Offset(px, py),
                                    radius = if (isTarget) 55f else 38f
                                ),
                                center = Offset(px, py),
                                radius = if (isTarget) 55f else 38f
                            )
                        }
                    }

                    // Watermark
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            "🧭 Primordial Ley Lines & Shrines",
                            color = Color(0xFF5C6BC0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Render Interactive Shrine Pins with fixed Dp calculations
                    shrines.forEach { shrine ->
                        val isUnlocked = userRcLevel >= shrine.runeInfo.reqLevel
                        val isTarget = shrine.runeInfo.runeItemId == selectedRuneId
                        val isDotted = !isUnlocked

                        val pinBgColor = when {
                            isTarget -> Color(0xFF1B5E20)
                            isUnlocked -> Color(0xFF2E7D32)
                            else -> Color(0xFF2C2210)
                        }

                        val pinBorderColor = when {
                            isTarget -> OsrsGoldBright
                            isUnlocked -> Color(0xFF81C784)
                            else -> Color(0xFFFFD54F)
                        }

                        val pinXDp = (mapWidthDp * shrine.posXRatio - 45.dp).coerceIn(4.dp, mapWidthDp - 115.dp)
                        val pinYDp = (mapHeightDp * shrine.posYRatio - 22.dp).coerceIn(4.dp, mapHeightDp - 50.dp)

                        Box(
                            modifier = Modifier
                                .offset(x = pinXDp, y = pinYDp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(pinBgColor.copy(alpha = if (isTarget) pulseAlpha else 0.92f))
                                .then(
                                    if (isDotted) Modifier.dashedBorder(
                                        width = if (isTarget) 2.dp else 1.dp,
                                        color = pinBorderColor,
                                        shape = RoundedCornerShape(8.dp),
                                        dashLength = 3.dp,
                                        gapLength = 3.dp
                                    ) else Modifier.border(if (isTarget) 2.dp else 1.dp, pinBorderColor, RoundedCornerShape(8.dp))
                                )
                                .combinedClickable(
                                    onClick = { onSelectShrine(shrine) },
                                    onLongClick = { onLongPressShrine(shrine) }
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("shrine_pin_${shrine.runeInfo.runeItemId}")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(shrine.runeInfo.iconEmoji, fontSize = 14.sp)
                                Column {
                                    Text(
                                        text = shrine.shrineName,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isUnlocked) "Lv. ${shrine.runeInfo.reqLevel}" else "🔒 Lv.${shrine.runeInfo.reqLevel}",
                                        color = if (isUnlocked) Color(0xFFA5D6A7) else Color(0xFFFFCDD2),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (isTarget && isAfkActive) {
                                    Text("⚡", fontSize = 10.sp)
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
fun ShrineDetailsDialog(
    shrine: ElementalShrineMapNode,
    userRcLevel: Int,
    runeStock: Int,
    essenceStock: Int,
    isCurrentTarget: Boolean,
    isAfkActive: Boolean,
    onSelectAndStart: () -> Unit,
    onDismiss: () -> Unit
) {
    val isUnlocked = userRcLevel >= shrine.runeInfo.reqLevel
    val multiplier = RunecraftData.getMultiplier(userRcLevel, shrine.runeInfo.runeItemId)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsLeatherDark,
            border = androidx.compose.foundation.BorderStroke(2.dp, shrine.themeColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("shrine_details_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = shrine.themeColor.copy(alpha = 0.25f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, shrine.themeColor),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(shrine.runeInfo.iconEmoji, fontSize = 24.sp)
                            }
                        }
                        Column {
                            Text(
                                text = shrine.shrineName,
                                color = OsrsTextYellow,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Elemental Rune Altar",
                                color = OsrsTextOrange,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Text("✖", color = Color.White, fontSize = 14.sp)
                    }
                }

                // Lore
                Surface(
                    color = OsrsLeatherMedium,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsLeatherDark)
                ) {
                    Text(
                        text = shrine.lore,
                        color = Color(0xFFECEFF1),
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Requirements & Stats
                Surface(
                    color = OsrsLeatherMedium,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFC62828))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Level Requirement:", color = OsrsTextWhite, fontSize = 12.sp)
                            Text(
                                text = "Lv. ${shrine.runeInfo.reqLevel} Runecrafting",
                                color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Your Level:", color = OsrsTextWhite, fontSize = 12.sp)
                            Text(
                                text = "Lv. $userRcLevel",
                                color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Conversion Ratio:", color = OsrsTextWhite, fontSize = 12.sp)
                            Text(
                                text = "1 Essence ➔ 1 ${shrine.runeInfo.runeName}",
                                color = OsrsGoldBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("XP Yield:", color = OsrsTextWhite, fontSize = 12.sp)
                            Text(
                                text = "+${(shrine.runeInfo.xpPerEssence * 10).toInt()} RC XP per action",
                                color = Color(0xFF64B5F6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stock Owned:", color = OsrsTextWhite, fontSize = 12.sp)
                            Text(
                                text = "$runeStock ${shrine.runeInfo.runeName}s",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Essence Available:", color = OsrsTextWhite, fontSize = 12.sp)
                            Text(
                                text = "$essenceStock total essence",
                                color = if (essenceStock > 0) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Action Button
                Button(
                    onClick = onSelectAndStart,
                    enabled = isUnlocked || (isCurrentTarget && isAfkActive),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentTarget && isAfkActive) Color(0xFFC62828) else Color(0xFF2E7D32),
                        disabledContainerColor = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("shrine_dialog_action_btn")
                ) {
                    Text(
                        text = if (isCurrentTarget && isAfkActive) "🛑 Stop AFK Conversion"
                        else if (!isUnlocked) "🔒 Level ${shrine.runeInfo.reqLevel} Required"
                        else "⚡ Set Target & Start AFK Conversion",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Compact Dual Favors Banner for Sedri on the Summoning Tab.
 * Displays both Rune-Making and Spirit Summoning favors distinctly with progress and rewards.
 */
@Composable
fun SedriDualFavorsBanner(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val contractsMap by viewModel.contractsMap.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val runeContract = contractsMap[com.example.data.models.OsrsSkill.RUNECRAFT]
    val summonContract = contractsMap[com.example.data.models.OsrsSkill.FIREMAKING]

    if (runeContract == null && summonContract == null) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1E140C),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6E4D25))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔮", fontSize = 16.sp)
                    Column {
                        Text(
                            text = "Sedri's Dual Favors",
                            color = Color(0xFFFFD700),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rune-Making & Spirit Summoning Tasks",
                            color = Color(0xFFCE93D8),
                            fontSize = 9.5.sp
                        )
                    }
                }
            }

            // Favor Items
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (runeContract != null) {
                    SedriFavorItemCard(
                        categoryLabel = "RUNE-MAKING",
                        categoryColor = Color(0xFFBA68C8),
                        contract = runeContract,
                        onClaim = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.claimSkillContract(com.example.data.models.OsrsSkill.RUNECRAFT)
                        }
                    )
                }

                if (summonContract != null) {
                    SedriFavorItemCard(
                        categoryLabel = "SPIRIT SUMMONING",
                        categoryColor = Color(0xFF52B788),
                        contract = summonContract,
                        onClaim = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.claimSkillContract(com.example.data.models.OsrsSkill.FIREMAKING)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SedriFavorItemCard(
    categoryLabel: String,
    categoryColor: Color,
    contract: com.example.data.models.SkillContract,
    onClaim: () -> Unit
) {
    val isCompleted = contract.currentQty >= contract.targetQty
    val progress = if (contract.targetQty > 0) {
        (contract.currentQty.toFloat() / contract.targetQty.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isCompleted) Color(0xFF1B2E1E) else Color(0xFF281C12),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isCompleted) Color(0xFF4CAF50) else Color(0xFF4E3725)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = categoryColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, categoryColor)
                    ) {
                        Text(
                            text = categoryLabel,
                            color = categoryColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                    Text(contract.iconSymbol.ifBlank { "⭐" }, fontSize = 11.sp)
                    Text(
                        text = contract.taskTitle,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = if (isCompleted) Color(0xFF2E7D32) else Color(0xFF382315)
                ) {
                    Text(
                        text = if (isCompleted) "✅ Done" else "${contract.currentQty}/${contract.targetQty}",
                        color = if (isCompleted) Color.White else Color(0xFFFFD54F),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Slim progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isCompleted) Color(0xFF4CAF50) else categoryColor,
                trackColor = Color(0xFF140D08)
            )

            // Rewards & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤝 +${contract.rewardFavorXp} Favor XP • 🪙 +${contract.rewardGp} GP",
                    color = Color(0xFFFFCC80),
                    fontSize = 8.5.sp
                )
                if (isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.clickable { onClaim() }
                    ) {
                        Text(
                            text = "🎁 Claim Reward",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
