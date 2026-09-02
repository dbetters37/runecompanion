package com.example.ui.components

import kotlinx.coroutines.launch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.PetViewModel
import kotlin.math.roundToInt

/**
 * Compact Active Favors Display for NPC Dialogue Dialogs & Roster Cards.
 */
@Composable
fun NpcActiveFavorsCompactSection(
    npcId: String,
    npcName: String,
    viewModel: PetViewModel,
    modifier: Modifier = Modifier,
    allowedSkillCategories: List<String>? = null
) {
    val allContracts by viewModel.contractsMap.collectAsStateWithLifecycle()
    val normId = viewModel.normalizeNpcId(npcId)
    val haptic = LocalHapticFeedback.current

    val npcContracts = remember(allContracts, npcId, npcName, allowedSkillCategories) {
        val direct = allContracts.values.filter { contract ->
            val normContract = viewModel.normalizeNpcId(contract.npcId)
            val matchesNpc = normContract == normId || contract.npcName.equals(npcName, ignoreCase = true)
            val matchesSkill = allowedSkillCategories == null || allowedSkillCategories.any { cat ->
                contract.skill.name.equals(cat, ignoreCase = true) ||
                contract.skill.displayName.equals(cat, ignoreCase = true) ||
                (cat.equals("Summoning", ignoreCase = true) && (contract.skill == com.example.data.models.OsrsSkill.FIREMAKING || contract.skill.displayName.equals("Summoning", ignoreCase = true))) ||
                (cat.equals("Runecrafting", ignoreCase = true) && (contract.skill == com.example.data.models.OsrsSkill.RUNECRAFT || contract.skill.displayName.equals("Runemaking", ignoreCase = true))) ||
                (cat.equals("Runemaking", ignoreCase = true) && (contract.skill == com.example.data.models.OsrsSkill.RUNECRAFT || contract.skill.displayName.equals("Runemaking", ignoreCase = true))) ||
                (cat.equals("Rune-making", ignoreCase = true) && (contract.skill == com.example.data.models.OsrsSkill.RUNECRAFT || contract.skill.displayName.equals("Runemaking", ignoreCase = true)))
            }
            matchesNpc && matchesSkill
        }
        if (direct.isNotEmpty()) {
            direct
        } else {
            val villager = com.example.data.models.NpcData.VILLAGERS.find {
                viewModel.normalizeNpcId(it.id) == normId || it.name.equals(npcName, ignoreCase = true)
            }
            val skills = villager?.favoredActivities ?: com.example.data.models.NpcData.findNpcById(normId)?.favoredActivities ?: emptyList()
            val filteredSkills = if (allowedSkillCategories != null) {
                skills.filter { skill ->
                    allowedSkillCategories.any { cat ->
                        skill.name.equals(cat, ignoreCase = true) || skill.displayName.equals(cat, ignoreCase = true)
                    }
                }
            } else {
                skills
            }
            filteredSkills.mapNotNull { allContracts[it] }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E140C)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF6E4D25)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("📜", fontSize = 11.sp)
                    Text(
                        text = if (npcContracts.isNotEmpty()) "Current Favors (${npcContracts.size})" else "Current Favors",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (npcContracts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF150E08),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No active favor tasks assigned currently.",
                        color = Color(0xFFBCAAA4),
                        fontSize = 9.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            } else {
                npcContracts.forEach { contract ->
                    val isCompleted = contract.currentQty >= contract.targetQty
                    val progress = if (contract.targetQty > 0) {
                        (contract.currentQty.toFloat() / contract.targetQty.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCompleted) Color(0xFF1B2E1E) else Color(0xFF281C12),
                        border = BorderStroke(0.5.dp, if (isCompleted) Color(0xFF4CAF50) else Color(0xFF4E3725)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(contract.iconSymbol.ifBlank { "⭐" }, fontSize = 11.sp)
                                    Text(
                                        text = contract.taskTitle,
                                        color = Color.White,
                                        fontSize = 10.5.sp,
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
                                color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFFB300),
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
                                        modifier = Modifier.clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.claimSkillContract(contract.skill)
                                        }
                                    ) {
                                        Text(
                                            text = "🎁 Claim",
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
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

data class NpcFavorReward(
    val level: Int,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val perkTag: String
)

/**
 * Returns the 50 milestone favor rewards (capped at Max Level 50).
 */
fun getNpcFavorRewardsList(npcId: String, npcName: String): List<NpcFavorReward> {
    val norm = npcId.removePrefix("npc_").lowercase()
    val (activityName, perkDesc, iconEmoji, tag) = when (norm) {
        "arlg", "arig" -> Quad("Forging & Mining", "chance to obtain double ore from rocks and extra forged materials", "⚒️", "Double Ore / Material")
        "finbar" -> Quad("Shaman Pool", "chance to catch double fish from the Shaman Pool", "🐟", "Extra Fish")
        "bram" -> Quad("Timber & Harvesting", "chance to harvest extra timber, logs, and grove crops from The Grove and farmlands", "🪓", "Extra Harvest")
        "ren" -> Quad("Trickery", "chance to obtain extra loot and stolen goods from Trickery", "🥷", "Extra Loot")
        "elnya", "elenya" -> Quad("Incantations & Magic", "chance to channel extra spirit runes and double spell yields", "🧙‍♀️", "Double Spell Yield")
        "theron" -> Quad("Bounty Hunter", "chance to receive double task rewards (2x Supply Boxes, 2x XP, 2x GP, 2x Favor XP) upon completing Slayer contracts", "💀", "Double Task Reward")
        "kael" -> Quad("Beast Tracking", "chance to receive double task rewards (2x Supply Boxes, 2x XP, 2x GP, 2x Favor XP) upon completing Hunter contracts", "🐾", "Double Task Reward")
        "ember" -> Quad("Cooking Fire", "chance to cook double food dishes at the Cooking Fire", "🔥", "Extra Cooked Food")
        "bryan" -> Quad("Farming", "chance to harvest extra crops and produce from farm patches", "🌱", "Extra Crop Harvest")
        "nia" -> Quad("Whittling & Construction", "chance to craft extra fletched shafts, bows, and materials", "🛠️", "Extra Crafted Goods")
        "sedri" -> Quad("Divination & Runecrafting", "chance to craft extra cosmic runes and energy wisps", "🔮", "Extra Runes & Wisps")
        "orla", "zahur" -> Quad("Herblore", "chance to brew extra potions and harvest extra herbs", "🧪", "Extra Potions")
        "barnaby" -> Quad("Quests & Shaman Path Expeditions", "reduction to all Quest and Shaman Path expedition durations (-1% per Favor Level, up to -50%)", "⛵", "-1% Expedition Time")
        "eric" -> Quad("Dungeon Delving", "chance to find extra relics and dungeon materials", "🗺️", "Extra Relics")
        "grace" -> Quad("Agility", "chance to obtain extra agility marks and expedition supplies", "👟", "Extra Supplies")
        else -> Quad("Wisdom & Gathering", "chance to obtain extra materials when training with $npcName", "⭐", "Extra Material")
    }

    val list = mutableListOf<NpcFavorReward>()
    for (lvl in 1..50) {
        list.add(
            NpcFavorReward(
                level = lvl,
                title = if (norm == "barnaby") "Favor Level $lvl: -${lvl}% Expedition Time" else "Favor Level $lvl: +${lvl}% Bonus",
                description = if (norm == "barnaby") "-${lvl}% reduction to all Quest and Shaman Path expedition durations." else "+${lvl}% $perkDesc when training $activityName.",
                iconEmoji = iconEmoji,
                perkTag = if (norm == "theron" || norm == "kael") "+${lvl}% Double Reward" else if (norm == "barnaby") "-${lvl}% Expedition Time" else "+${lvl}% Extra Material"
            )
        )
    }
    return list
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * M3 Dialog displaying all 50 Favor Levels, their perks, and unlock status.
 */
@Composable
fun NpcFavorRewardsDialog(
    npcId: String,
    npcName: String,
    npcEmoji: String,
    currentLevel: Int,
    currentXp: Long,
    reqXp: Long,
    onDismiss: () -> Unit,
    onOfferTribute: () -> Unit
) {
    val rewards = remember(npcId, npcName) { getNpcFavorRewardsList(npcId, npcName) }
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Unlocked, 2: Locked
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val filteredRewards = remember(rewards, selectedFilter, currentLevel) {
        when (selectedFilter) {
            1 -> rewards.filter { it.level <= currentLevel }
            2 -> rewards.filter { it.level > currentLevel }
            else -> rewards
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E140C)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Compact Header
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
                            color = Color(0xFF382315),
                            border = BorderStroke(1.dp, Color(0xFFFFD700)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(npcEmoji, fontSize = 20.sp)
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$npcName's Favor",
                                    color = Color(0xFFFFD700),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (currentLevel >= 50) Color(0xFF2E7D32) else Color(0xFFE65100)
                                ) {
                                    Text(
                                        text = "Lvl $currentLevel/50",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (currentLevel >= 50) "🏆 Max Level 50 Mastered!" else "XP: $currentXp / $reqXp (${(currentLevel * 100) / 50}%)",
                                color = Color(0xFFFFA726),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Slim Progress Bar
                LinearProgressIndicator(
                    progress = { (currentLevel / 50f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFFFD700),
                    trackColor = Color(0xFF382315)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter tabs + Jump to current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("All (50)", "Unlocked ($currentLevel)", "Locked (${50 - currentLevel})").forEachIndexed { index, label ->
                            val isSel = selectedFilter == index
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFFFFD700) else Color(0xFF2D1B10),
                                border = BorderStroke(0.5.dp, if (isSel) Color(0xFFFFD700) else Color(0xFF4A3416)),
                                modifier = Modifier.clickable { selectedFilter = index }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.Black else Color(0xFFD7CCC8),
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (selectedFilter == 0 && currentLevel in 1..49) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF4A3416),
                            border = BorderStroke(0.5.dp, Color(0xFFFFB74D)),
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    val targetIdx = (currentLevel - 1).coerceIn(0, rewards.size - 1)
                                    listState.animateScrollToItem(targetIdx)
                                }
                            }
                        ) {
                            Text(
                                text = "🎯 Jump to Current",
                                color = Color(0xFFFFE082),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Compact Rewards List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredRewards, key = { it.level }) { reward ->
                        val isUnlocked = reward.level <= currentLevel
                        val isNext = reward.level == currentLevel + 1

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isUnlocked -> Color(0xFF1B2E1C)
                                isNext -> Color(0xFF382810)
                                else -> Color(0xFF241810)
                            },
                            border = BorderStroke(
                                width = if (isNext) 1.dp else 0.5.dp,
                                color = when {
                                    isUnlocked -> Color(0xFF4CAF50).copy(alpha = 0.8f)
                                    isNext -> Color(0xFFFFD700)
                                    else -> Color(0xFF4A3416)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Level & Emoji Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isUnlocked) Color(0xFF2E7D32) else if (isNext) Color(0xFF8B6508) else Color(0xFF3E2723),
                                    modifier = Modifier.size(width = 44.dp, height = 30.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(reward.iconEmoji, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${reward.level}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Info Column
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = reward.title,
                                            color = if (isUnlocked) Color(0xFF81C784) else if (isNext) Color(0xFFFFE082) else Color(0xFFCCCCCC),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (reward.perkTag.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFF120C08)
                                            ) {
                                                Text(
                                                    text = reward.perkTag,
                                                    color = if (isUnlocked) Color(0xFFA5D6A7) else Color(0xFFFFB74D),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = reward.description,
                                        color = if (isUnlocked) Color(0xFFE8F5E9) else Color(0xFF9E9E9E),
                                        fontSize = 9.5.sp,
                                        lineHeight = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Status Pill
                                Surface(
                                    color = if (isUnlocked) Color(0xFF2E7D32) else if (isNext) Color(0xFFE65100) else Color(0xFF332619),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isUnlocked) "✅ Unlocked" else if (isNext) "🎯 Next" else "🔒",
                                        color = Color.White,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOfferTribute,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("🌟 Offer Tribute (+50 XP)", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.7f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Close", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
/**
 * Unified Draggable, Scaled NPC Companion Badge (10% smaller, position memory, 50-level favor rewards).
 */
@Composable
fun GenericNpcCompanionBadge(
    npcId: String,
    npcName: String,
    npcTitle: String,
    npcRole: String,
    avatarEmoji: String,
    dialogues: List<String>,
    viewModel: PetViewModel,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFFFFD700),
    secondaryBadgeText: String? = null,
    allowedSkillCategories: List<String>? = null
) {
    val isNpcEnabled by viewModel.isNpcCompanionsEnabled.collectAsStateWithLifecycle()
    val isMinimized = viewModel.isNpcSessionMinimized(npcId)

    if (!isNpcEnabled || isMinimized) return

    val haptic = LocalHapticFeedback.current
    var showDialogueDialog by remember { mutableStateOf(false) }
    var showRewardsDialog by remember { mutableStateOf(false) }
    var currentDialogueIndex by remember { mutableIntStateOf(0) }
    var showMiniBubble by remember { mutableStateOf(true) }
    var isDragging by remember { mutableStateOf(false) }

    val favorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    val favorPair = favorMap[npcId] ?: Pair(1, 0L)
    val favorLevel = favorPair.first.coerceIn(1, 50)
    val favorXp = favorPair.second
    val reqXp = viewModel.getRequiredXpForFavorLevel(favorLevel)

    val currentDialogue = dialogues[currentDialogueIndex % dialogues.size]

    // Floating pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "npc_bounce_$npcId")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -3.0f,
        targetValue = 3.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Position memory
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableFloatStateOf(1080f) }
    var containerHeightPx by remember { mutableFloatStateOf(1920f) }

    val savedPos = remember(npcId) { viewModel.getNpcPosition(npcId, 0.82f, 0.70f) }
    var normalizedX by remember(npcId) { mutableFloatStateOf(savedPos.first) }
    var normalizedY by remember(npcId) { mutableFloatStateOf(savedPos.second) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("npc_companion_$npcId")
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(widthPx, heightPx) {
            if (widthPx > 100f && heightPx > 100f) {
                containerWidthPx = widthPx
                containerHeightPx = heightPx
            }
        }

        val badgeWidthDp = 225.dp
        val badgeHeightDp = 100.dp
        val badgeWidthPx = with(density) { badgeWidthDp.toPx() }
        val badgeHeightPx = with(density) { badgeHeightDp.toPx() }

        val safeMaxX = (containerWidthPx - badgeWidthPx).coerceAtLeast(10f)
        val safeMaxY = (containerHeightPx - badgeHeightPx).coerceAtLeast(10f)

        val currentXPx = (normalizedX * safeMaxX).coerceIn(0f, safeMaxX)
        val currentYPx = ((normalizedY * safeMaxY) + bounceOffset).coerceIn(0f, safeMaxY)

        Box(
            modifier = Modifier
                .offset { IntOffset(currentXPx.roundToInt(), currentYPx.roundToInt()) }
                .width(badgeWidthDp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top control bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .background(Color(0xDD1B120C), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF6E4D25), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDragEnd = {
                                        isDragging = false
                                        viewModel.saveNpcPosition(npcId, normalizedX, normalizedY)
                                    },
                                    onDragCancel = { isDragging = false },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val newXPx = (normalizedX * safeMaxX) + dragAmount.x
                                        val newYPx = (normalizedY * safeMaxY) + dragAmount.y
                                        normalizedX = (newXPx / safeMaxX).coerceIn(0.01f, 0.99f)
                                        normalizedY = (newYPx / safeMaxY).coerceIn(0.01f, 0.99f)
                                    }
                                )
                            }
                            .background(if (isDragging) Color(0xFFFF9800) else Color(0xFF4A3416), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("✋ Drag", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // 4 Corner Snaps
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .background(Color(0xFF332211), RoundedCornerShape(3.dp))
                            .clickable {
                                normalizedX = 0.04f; normalizedY = 0.08f
                                viewModel.saveNpcPosition(npcId, 0.04f, 0.08f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↖", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .background(Color(0xFF332211), RoundedCornerShape(3.dp))
                            .clickable {
                                normalizedX = 0.96f; normalizedY = 0.08f
                                viewModel.saveNpcPosition(npcId, 0.96f, 0.08f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↗", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .background(Color(0xFF332211), RoundedCornerShape(3.dp))
                            .clickable {
                                normalizedX = 0.04f; normalizedY = 0.88f
                                viewModel.saveNpcPosition(npcId, 0.04f, 0.88f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↙", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .background(Color(0xFF332211), RoundedCornerShape(3.dp))
                            .clickable {
                                normalizedX = 0.96f; normalizedY = 0.88f
                                viewModel.saveNpcPosition(npcId, 0.96f, 0.88f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↘", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Hide Button
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8B1E1E), RoundedCornerShape(3.dp))
                            .clickable { viewModel.minimizeNpcForSession(npcId) }
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("━ Hide", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Speech Bubble & Avatar
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showMiniBubble) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF26190E)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF8B6B38)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showDialogueDialog = true
                                }
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$npcName (${favorLevel}/50)",
                                        color = accentColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("💬 Tap", color = Color(0xFFFFA726), fontSize = 8.sp)
                                }
                                Text(
                                    text = currentDialogue,
                                    color = Color(0xFFEDE0D4),
                                    fontSize = 9.sp,
                                    lineHeight = 11.5.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    // Avatar Circle (47dp scaled down 10%)
                    Box(
                        modifier = Modifier
                            .size(47.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(accentColor, Color(0xFF3E2723), Color(0xFF1B0F07))
                                )
                            )
                            .border(1.8.dp, accentColor, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDialogueDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarEmoji, fontSize = 23.sp)

                        // Favor badge overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(Color(0xFF1B5E20), CircleShape)
                                .border(0.5.dp, Color.White, CircleShape)
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = "$favorLevel",
                                color = Color.White,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Main NPC Dialogue Dialog
    if (showDialogueDialog) {
        Dialog(onDismissRequest = { showDialogueDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF26190E)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF3E2723), CircleShape)
                                    .border(1.5.dp, accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatarEmoji, fontSize = 24.sp)
                            }
                            Column {
                                Text(
                                    text = npcName,
                                    color = accentColor,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$npcTitle • $npcRole",
                                    color = Color(0xFFD7CCC8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = { showDialogueDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    // Compact Formatted Favor Level Panel (Single View Rewards Button)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C12)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF8B6508)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🤝 Favor Lv.$favorLevel/50", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (favorLevel >= 50) {
                                        Text("🏆", fontSize = 11.sp)
                                    }
                                }
                                FilledTonalButton(
                                    onClick = { showRewardsDialog = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF5D3A1A),
                                        contentColor = Color(0xFFFFD54F)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("🎁 View Rewards", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (favorLevel >= 50) 1f
                                    else (favorXp.toFloat() / reqXp.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFFFFD700),
                                trackColor = Color(0xFF1E140C)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (favorLevel >= 50) "Max Level 50 Mastered!" else "$favorXp / $reqXp XP",
                                    color = Color(0xFFFFF9C4),
                                    fontSize = 9.5.sp
                                )
                                if (favorLevel < 50) {
                                    Text(
                                        text = "${((favorXp.toFloat() / reqXp.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f) * 100).toInt()}%",
                                        color = Color(0xFFFFCC80),
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Compact Active Favors List
                    NpcActiveFavorsCompactSection(
                        npcId = npcId,
                        npcName = npcName,
                        viewModel = viewModel,
                        allowedSkillCategories = allowedSkillCategories
                    )

                    // Dialogue Quote Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1108)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF5A3E20)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "\"$currentDialogue\"",
                                color = Color(0xFFFFF8E1),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Actions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Captain Barnaby Parcel Dispatch Deliveries
                        NpcParcelActionButtons(
                            npcId = npcId,
                            npcName = npcName,
                            viewModel = viewModel
                        )

                        Button(
                            onClick = {
                                currentDialogueIndex++
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💭 Ask for another tip / story", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.addNpcFavorXp(npcId, 50L, npcName, "Offer Tribute")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🌟 Chat & Offer Tribute (+50 XP)", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 50-Level Rewards Dialog
    if (showRewardsDialog) {
        NpcFavorRewardsDialog(
            npcId = npcId,
            npcName = npcName,
            npcEmoji = avatarEmoji,
            currentLevel = favorLevel,
            currentXp = favorXp,
            reqXp = reqXp,
            onDismiss = { showRewardsDialog = false },
            onOfferTribute = {
                viewModel.addNpcFavorXp(npcId, 50L, npcName, "Offer Tribute")
            }
        )
    }
}

// ==========================================
// SPECIALIZED NPC COMPANION COMPOSABLES
// ==========================================

/** Captain Barnaby on Navigation Tab */
@Composable
fun CaptainBarnabyNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "barnaby",
        npcName = "Captain Barnaby",
        npcTitle = "River Voyager",
        npcRole = "Nautical Navigator",
        avatarEmoji = "⛵",
        accentColor = Color(0xFF4FC3F7),
        dialogues = listOf(
            "Ahoy matey! Barnaby here. Keep your sails trimmed and check the wind currents on the navigation map! ⛵",
            "Long voyages bring back ancient sunken chests and rare nautical relics! Pack plenty of timber repair kits! ⚓",
            "Chart the uncharted river tributaries! Every new island discovered expands our shaman trade routes! 🧭",
            "Rough sea storms ahead? Adjust your rudder and steady your spirit! Great captains are forged in rough waters! 🌊",
            "The coastal winds are favorable today! Ready your flagship for deep exploration! 🗺️"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Bram on Harvesting Tab (The Grove) */
@Composable
fun BramNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "bram",
        npcName = "Bram",
        npcTitle = "Grove Woodtender",
        npcRole = "Harvesting Master",
        avatarEmoji = "🪓",
        accentColor = Color(0xFF81C784),
        dialogues = listOf(
            "Greetings! Bram here. The sacred grove whispers when the harvest is bountiful! 🪓🌾",
            "I oversee both the timber fallen in the Whispering Grove and the crop harvests across the soil! 🌲",
            "Knotwood and Elder Magic trees yield rare amber resins. Keep your woodcutting axe razor-sharp! 🪓",
            "Tending the grove saplings and farmland patches ensures our supplies never run dry! 🪵🌱",
            "Listen closely to the tree bark—knocking three times reveals where hollow nests hide golden birds' nests! 🌳",
            "A hearty swing from the hips splits even the thickest teak logs in a single stroke! 🪚"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Nia on House Tab (POH) */
@Composable
fun NiaNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "nia",
        npcName = "Nia",
        npcTitle = "Hearth Builder",
        npcRole = "Carpentry Warden",
        avatarEmoji = "🔨",
        accentColor = Color(0xFFFFB74D),
        dialogues = listOf(
            "Welcome to the homestead! Nia here. A warm hearth makes any house a sacred tribal home! 🔨",
            "Upgrading your mahogany workbenches unlocks exquisite furniture and ornate room portal chambers! 🏡",
            "Keep an eye on your plank stores. High-tier teak and mahogany build the finest dining tables! 🪑",
            "Need more room? Expand the house layout grid and construct specialized meditation chapels! 📐",
            "Nothing beats coming back to a cozy roaring fire after a long wilderness adventure! 🪵"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Ember on Storage Tab -> Fire Subtab ONLY */
@Composable
fun EmberNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "ember",
        npcName = "Ember",
        npcTitle = "Pyre Keeper",
        npcRole = "Sacred Flame Watcher",
        avatarEmoji = "🔥",
        accentColor = Color(0xFFFF7043),
        dialogues = listOf(
            "Feed the flame! Ember here. The pyre fire never sleeps as long as we keep feeding it dry logs! 🔥",
            "Burning seasoned maple and yew logs keeps our campfires roaring with bonus cooking exp! 🍲",
            "Sprinkle sacred incense herbs into the embers for restorative spiritual smoke buffs! 🏮",
            "A steady flame roasts meat to juicy perfection without scorching the savory seasonings! 🥩",
            "Fire clears away decay and sparks new life. Keep your tinderbox close at all times! ✨"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Sedri on Summoning & Divination Tabs */
@Composable
fun SedriNpcCompanion(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier,
    allowedSkillCategories: List<String>? = null
) {
    GenericNpcCompanionBadge(
        npcId = "sedri",
        npcName = "Sedri",
        npcTitle = "Rift Inscriber & Summoner",
        npcRole = "Rift Shaman & Summoner",
        avatarEmoji = "🔮",
        accentColor = Color(0xFFBA68C8),
        dialogues = listOf(
            "The cosmic rifts and spirit leylines hum with immense power! I have favors for both rune-making and spirit summoning! 🔮🐺",
            "Inscribe ancient runes at the elemental shrines or weave memory effigies into guardian totems! ✨",
            "Pure essence aligns with ancestral runes. Meditate at the cosmic altar or bind a spirit familiar! 🌀",
            "Each memory effigy and rune carved strengthens our tribe's spiritual bond with ancient guardians! 🗿",
            "Harness the radiant pulse—shape raw runes and summon loyal animal spirits to aid our journey! 🌌"
        ),
        viewModel = viewModel,
        modifier = modifier,
        allowedSkillCategories = allowedSkillCategories ?: listOf("Runecraft", "Runemaking", "Runecrafting", "Rune-making", "Summoning", "Firemaking")
    )
}

/** Ren on Trickery Tab (Thieving) */
@Composable
fun RenNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "ren",
        npcName = "Ren",
        npcTitle = "Silent Crow",
        npcRole = "Tribe Shadow Scout",
        avatarEmoji = "🥷",
        accentColor = Color(0xFFB0BEC5),
        dialogues = listOf(
            "Quiet now... Ren here. Move like the wind through the tree canopies! 🥷",
            "Watch the guard's patrol rhythm. Strike the pocket when their eyes drift toward the market stall! 🗝️",
            "A feather-light touch and swift footwork keep you undetected in the wealthiest treasuries! 🪙",
            "Keep lockpicks oiled and coated in graphite. Rusty tumblers make too much noise! 🎭",
            "The shadows are our allies. Always plan your escape route before lifting a coin pouch! 🌙"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Arig on Forging Tab (Smithing) */
@Composable
fun ArigNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "arig",
        npcName = "Arig",
        npcTitle = "Anvil Warden",
        npcRole = "Master Smith",
        avatarEmoji = "⚒️",
        accentColor = Color(0xFFFF8A65),
        dialogues = listOf(
            "Strike while the iron is glowing hot! Arig here. The anvil sings when the rhythm is right! ⚒️",
            "Folding rune and dragon metal bars requires tremendous heat and steady hammer blows! 🗡️",
            "Tempering breastplates and helmets increases defence power for dungeon raiding parties! 🛡️",
            "Quench the heated steel in volcanic oil to harden the blade against brittle fractures! 🌋",
            "A master smith respects the ore. Smelt pure coal into high-grade steel ingots! ⚙️"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Farmer Bryan on POF Farm Tab */
@Composable
fun FarmerBryanNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "bryan",
        npcName = "Farmer Bryan",
        npcTitle = "Cropwarden",
        npcRole = "Agriculture Master",
        avatarEmoji = "🌱",
        accentColor = Color(0xFFAED581),
        dialogues = listOf(
            "Fine day for farming! Bryan here. Rich soil and compost are the secrets to bumper crops! 🌱",
            "Keep the livestock troughs full of feed and mash! Happy animals produce top-quality wool, milk, and eggs! 🐮",
            "Prune your fruit trees regularly and protect your allotments from garden pests! 🌾",
            "Breeding prized sheep and cows unlocks rare genetic traits and golden agricultural produce! 🚜",
            "Compost everything! Organic scrap turns into ultra-compost for sacred herb patches! 🌿"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Orla on Herbalism Tab (Herblore) */
@Composable
fun OrlaNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "orla",
        npcName = "Orla",
        npcTitle = "Star Weaver & Herbalist",
        npcRole = "Master Alchemist",
        avatarEmoji = "🌿",
        accentColor = Color(0xFF80CBC4),
        dialogues = listOf(
            "Blessings of the flora! Orla here. Every wild herb carries potent healing and spirit blessings! 🌿",
            "Clean your grimy herbs with care before mixing them into pure distilled water vials! 🧪",
            "Combine ranarr weeds with snape grass to concoct legendary restorative prayer potions! 🍃",
            "Secondary ingredients like crushed dragon horn and bird nests empower elixirs with super boosts! 🍵",
            "Alchemy is an art of patience. Let the cauldron simmer at a gentle spiritual heat! 🍵"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Kael on Beast Tracking Subtab ONLY */
@Composable
fun KaelNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "kael",
        npcName = "Kael",
        npcTitle = "Beast Tracker",
        npcRole = "Tribe Trapper",
        avatarEmoji = "🐾",
        accentColor = Color(0xFFFFB300),
        dialogues = listOf(
            "Hush and watch the tracks! Kael here. Chinchompas and spotted kebbits are right around the ridge! 🐾",
            "Check the windward direction! Animals can scent human presence from fifty paces away! 🪤",
            "Set box traps and pitfall snares near watering holes. Bait them with spicy seeds and wild berries! 🦅",
            "A well-placed trap snaps instantly. Keep your hunting camouflage gear fresh and clean! 🏹",
            "Rare mutant beasts leave radiant golden pawprints. Follow them for exotic trophies! ✨"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/** Elder Elnya on Incantations Tab (Magic) */
@Composable
fun ElderElnyaNpcCompanion(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    GenericNpcCompanionBadge(
        npcId = "npc_elnya",
        npcName = "Elder Elnya",
        npcTitle = "Voice of the Spirits",
        npcRole = "High Shaman Elder",
        avatarEmoji = "🧙‍♀️",
        accentColor = Color(0xFFCE93D8),
        dialogues = listOf(
            "Peace be upon your spirit, child. The ancient runes channel cosmic energies of eternity! ✨",
            "Cast your elemental bolts and blasts to awaken the dormant ley line pathways! ⚡",
            "Transmute ores and enchant mystic jewelry at the Sacred Totem Shrine to empower tribal wards! 🔮",
            "Alchemy, Divination, and Incantations form the sacred trifecta of high shamanic sorcery! 🌌",
            "May the ancestral spirits guide your wand and bless your magical endeavors! 🧙‍♀️"
        ),
        viewModel = viewModel,
        modifier = modifier
    )
}

/**
 * Reusable dynamic Parcel Delivery chat options for Captain Barnaby's favors.
 * Shows the option to receive a parcel on the source NPC, and the option to deliver on the destination NPC.
 */
@Composable
fun NpcParcelActionButtons(
    npcId: String,
    npcName: String,
    viewModel: PetViewModel
) {
    val activeContracts by viewModel.contractsMap.collectAsStateWithLifecycle()
    val invItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val parcelInfo = remember(activeContracts, invItems) { viewModel.getActiveBarnabyParcelInfo() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    if (parcelInfo != null) {
        val normCurrent = viewModel.normalizeNpcId(npcId)
        val normSource = viewModel.normalizeNpcId(parcelInfo.sourceNpcId)
        val normDest = viewModel.normalizeNpcId(parcelInfo.destNpcId)
        val isSource = normCurrent == normSource || npcName.equals(parcelInfo.sourceNpcName, ignoreCase = true)
        val isDest = normCurrent == normDest || npcName.equals(parcelInfo.destNpcName, ignoreCase = true)
        val isBarnaby = normCurrent == "barnaby" || npcName.contains("Barnaby", ignoreCase = true)

        if (isSource && !parcelInfo.hasCollectedParcel && !parcelInfo.isDelivered) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.collectBarnabyParcel(npcId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📦 Take Barnaby's Parcel (For ${parcelInfo.destNpcName})",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (isSource && parcelInfo.hasCollectedParcel && !parcelInfo.isDelivered) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF3E2723),
                border = BorderStroke(0.5.dp, Color(0xFFFFB74D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📦", fontSize = 14.sp)
                    Text(
                        text = "You have collected the parcel! Deliver it safely to ${parcelInfo.destNpcName}.",
                        color = Color(0xFFFFE0B2),
                        fontSize = 11.sp
                    )
                }
            }
        } else if (isDest && parcelInfo.hasCollectedParcel && !parcelInfo.isDelivered) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.deliverBarnabyParcel(npcId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📦 Deliver Barnaby's Parcel (From ${parcelInfo.sourceNpcName})",
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (isDest && !parcelInfo.hasCollectedParcel && !parcelInfo.isDelivered) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2E1C0C),
                border = BorderStroke(0.5.dp, Color(0xFF8D6E63)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📦", fontSize = 14.sp)
                    Text(
                        text = "${parcelInfo.destNpcName} is awaiting a parcel from ${parcelInfo.sourceNpcName}.",
                        color = Color(0xFFD7CCC8),
                        fontSize = 11.sp
                    )
                }
            }
        } else if (isBarnaby && !parcelInfo.isDelivered) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0D47A1).copy(alpha = 0.35f),
                border = BorderStroke(1.dp, Color(0xFF42A5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⛵", fontSize = 16.sp)
                    Text(
                        text = if (!parcelInfo.hasCollectedParcel) {
                            "Courier Dispatch: Collect parcel from ${parcelInfo.sourceNpcName} and deliver it to ${parcelInfo.destNpcName}!"
                        } else {
                            "Courier Dispatch: Take the sealed parcel to ${parcelInfo.destNpcName}!"
                        },
                        color = Color(0xFFE3F2FD),
                        fontSize = 11.sp
                    )
                }
            }
        } else if (isBarnaby && parcelInfo.isDelivered) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.claimSkillContract(com.example.data.models.OsrsSkill.SAILING)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎉 Complete Parcel Favor for Captain Barnaby!",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
