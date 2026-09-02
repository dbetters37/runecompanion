package com.example.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@Composable
fun OsrsQuestTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val activeQuestState by viewModel.activeQuestExpedition.collectAsStateWithLifecycle()
    val savedQuestProgressMap by viewModel.savedQuestProgressMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()

    val totalPetLevel = remember(skillXpMap) {
        skillXpMap.values.sumOf { OsrsXpCalculator.getLevelForXp(it) }
    }
    val petCombatLevel = remember(skillXpMap) {
        CombatManager.calculateCombatLevel(skillXpMap)
    }

    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0 = All, 1 = Available, 2 = Ready, 3 = High Value, 4 = Completed
    var selectedRewardItemForDetail by remember { mutableStateOf<InventoryItem?>(null) }

    val allQuests = remember { OsrsQuestData.QUESTS.sortedBy { it.name } }
    val completedSet = petState.completedQuestIds

    val availableQuests = remember(allQuests, completedSet) {
        allQuests.filter { !completedSet.contains(it.id) }
    }

    val readyQuests = remember(allQuests, completedSet, skillXpMap, petCombatLevel, inventoryItems, bankItems) {
        allQuests.filter { quest ->
            if (completedSet.contains(quest.id)) return@filter false

            val skillMet = quest.reqSkill == null || (skillXpMap[quest.reqSkill]?.let { OsrsXpCalculator.getLevelForXp(it) } ?: 1) >= quest.reqSkillLevel
            if (!skillMet) return@filter false

            if (petCombatLevel < quest.recCombatLevel) return@filter false

            val preReqsMet = quest.reqQuestIds.all { completedSet.contains(it) }
            if (!preReqsMet) return@filter false

            val itemsMet = quest.requiredItems.all { req ->
                val invCount = inventoryItems.find { it.id == req.itemId }?.quantity ?: 0
                val bankCount = bankItems.find { it.id == req.itemId }?.quantity ?: 0
                (invCount + bankCount) >= req.requiredQty
            }
            if (!itemsMet) return@filter false

            true
        }
    }

    val highValueQuests = remember(allQuests) {
        allQuests.filter { it.isHighValueReward }
    }

    val completedQuests = remember(allQuests, completedSet) {
        allQuests.filter { completedSet.contains(it.id) }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showQuestSettingsDialog by remember { mutableStateOf(false) }
    var showQuestRelationshipsDialog by remember { mutableStateOf(false) }

    val filteredQuests = remember(selectedFilterIndex, searchQuery, allQuests, availableQuests, readyQuests, highValueQuests, completedQuests) {
        val baseList = when (selectedFilterIndex) {
            1 -> availableQuests
            2 -> readyQuests
            3 -> highValueQuests
            4 -> completedQuests
            else -> allQuests
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.difficulty.displayName.contains(searchQuery, ignoreCase = true) ||
                it.difficulty.rarityLabel.contains(searchQuery, ignoreCase = true) ||
                (it.rewardValueLabel?.contains(searchQuery, ignoreCase = true) == true) ||
                (it.reqSkill?.displayName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    if (showQuestSettingsDialog) {
        QuestSettingsDialog(
            completedQuests = completedQuests,
            onMarkIncomplete = { questId -> viewModel.markQuestIncomplete(questId) },
            onMarkAllIncomplete = { viewModel.markAllQuestsIncomplete() },
            onDismiss = { showQuestSettingsDialog = false }
        )
    }

    if (showQuestRelationshipsDialog) {
        QuestRelationshipDialog(
            viewModel = viewModel,
            onDismiss = { showQuestRelationshipsDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(OsrsLeatherDark)
            .border(1.5.dp, OsrsGold, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Compact Header Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Title, QP Badge & Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📜", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "Quest Journal",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Completed: ${completedQuests.size} / ${allQuests.size} Quests",
                                color = OsrsParchment,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // QP Badge
                        Surface(
                            color = OsrsLeatherMedium,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text("⭐", fontSize = 10.sp)
                                Text(
                                    text = "${petState.questPoints} QP",
                                    color = OsrsTextYellow,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Settings Button
                        Surface(
                            color = OsrsLeatherMedium,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showQuestSettingsDialog = true }
                                .testTag("osrs_quest_settings_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚙️", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Row 2: Action Row with Quest Tree
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expeditions for QP, favor & rare rewards",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )

                    Surface(
                        color = Color(0xFF382300),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showQuestRelationshipsDialog = true }
                            .testTag("osrs_quest_relationships_tree_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🌳", fontSize = 11.sp)
                            Text(
                                text = "Quest Tree",
                                color = OsrsGoldBright,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Quest Expedition Card
        activeQuestState?.let { expedition ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A20)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(petState.petType.iconSymbol, fontSize = 18.sp)
                            Text(
                                text = "🏃 Questing: ${expedition.quest.name}",
                                color = Color(0xFF70E000),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { viewModel.pauseQuestExpedition() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("💾 Pause", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.cancelQuestExpedition(expedition.quest.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("❌ Abandon", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { expedition.progressFraction },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF70E000),
                            trackColor = Color(0xFF122814)
                        )

                        Text(
                            text = "⏱️ ${com.example.data.models.formatQuestDuration(expedition.remainingSeconds)} (${(expedition.progressFraction * 100).toInt()}%)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Paused Quests Banner (if no active quest running)
        if (activeQuestState == null) {
            val pausedProgresses = savedQuestProgressMap.values.filter { saved ->
                !completedSet.contains(saved.questId) && saved.isPaused
            }
            if (pausedProgresses.isNotEmpty()) {
                pausedProgresses.forEach { saved ->
                    val pausedQuest = allQuests.find { it.id == saved.questId }
                    if (pausedQuest != null) {
                        val progressFraction = 1f - (saved.remainingSeconds.toFloat() / saved.totalDurationSeconds.toFloat())
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2216)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("💾", fontSize = 16.sp)
                                        Text(
                                            text = "Paused Quest: ${pausedQuest.name}",
                                            color = OsrsTextYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = { viewModel.startQuestExpedition(pausedQuest) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("▶️ Resume", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.cancelQuestExpedition(saved.questId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("🗑️ Reset", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = OsrsGold,
                                        trackColor = Color(0xFF1E1510)
                                    )

                                    Text(
                                        text = "⏱️ ${com.example.data.models.formatQuestDuration(saved.remainingSeconds)} (${(progressFraction * 100).toInt()}%)",
                                        color = OsrsParchment,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search Bar Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("🔍 Search Shaman quests & expeditions...", color = Color.Gray, fontSize = 12.sp) },
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Text("❌", fontSize = 11.sp)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = OsrsTextYellow, fontSize = 12.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1510),
                unfocusedContainerColor = Color(0xFF1E1510),
                focusedBorderColor = OsrsGold,
                unfocusedBorderColor = Color(0xFF4A3828)
            ),
            shape = RoundedCornerShape(6.dp)
        )

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val filterTabs = listOf(
                "All (${allQuests.size})",
                "Avail (${availableQuests.size})",
                "Ready (${readyQuests.size})",
                "💎 High Value (${highValueQuests.size})",
                "Done (${completedQuests.size})"
            )
            filterTabs.forEachIndexed { idx, label ->
                val isSelected = selectedFilterIndex == idx
                Button(
                    onClick = { selectedFilterIndex = idx },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) OsrsRedFrame else Color(0xFF2C2018)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OsrsTextYellow else Color.Gray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Quest List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredQuests) { quest ->
                val isCompleted = completedSet.contains(quest.id)
                val isCurrentActive = activeQuestState?.quest?.id == quest.id
                val savedProgress = savedQuestProgressMap[quest.id]
                val hasSavedProgress = savedProgress != null && !isCompleted
                val calculatedDuration = savedProgress?.totalDurationSeconds ?: viewModel.calculateEffectiveQuestDuration(quest, petCombatLevel)

                // Requirement evaluation
                val currentSkillLevel = quest.reqSkill?.let { skillXpMap[it]?.let { xp -> OsrsXpCalculator.getLevelForXp(xp) } ?: 1 } ?: 1
                val hasSkillReq = quest.reqSkill == null || currentSkillLevel >= quest.reqSkillLevel
                val hasCombatReq = petCombatLevel >= quest.recCombatLevel

                // Prerequisite Quests evaluation
                val preReqQuests = quest.reqQuestIds.mapNotNull { preId -> allQuests.find { it.id == preId } }
                val missingPreReqs = preReqQuests.filter { !completedSet.contains(it.id) }
                val hasPreReqs = missingPreReqs.isEmpty()

                // Item requirements evaluation
                val missingItems = quest.requiredItems.filter { req ->
                    val normReqId = com.example.data.models.DefaultItems.normalizeItemId(req.itemId)
                    val invCount = inventoryItems.filter { it.id == req.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity }
                    val bankCount = bankItems.filter { it.id == req.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity }
                    (invCount + bankCount) < req.requiredQty
                }
                val hasAllItems = missingItems.isEmpty()

                val isReadyToStart = !isCompleted && hasSkillReq && hasCombatReq && hasPreReqs && (hasAllItems || hasSavedProgress)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isCompleted -> Color(0xFF1C2B1B)
                            isCurrentActive -> Color(0xFF1E3A20)
                            isReadyToStart -> Color(0xFF2E2413)
                            else -> Color(0xFF271D15)
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = when {
                            isCompleted -> Color(0xFF4CAF50)
                            isCurrentActive -> Color(0xFF70E000)
                            isReadyToStart -> OsrsGold
                            else -> Color(0xFF5C4535)
                        }
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Quest Title Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(quest.iconEmoji, fontSize = 20.sp)
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = quest.name,
                                            color = when {
                                                isCompleted -> Color(0xFF70E000)
                                                isReadyToStart || isCurrentActive -> Color(0xFFFFD700)
                                                else -> Color(0xFFFF5555)
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (isReadyToStart) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                                                    .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("READY", color = Color(0xFF70E000), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Visual Difficulty & Rarity Tag
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(quest.difficulty.colorHex).copy(alpha = 0.25f))
                                                .border(1.dp, Color(quest.difficulty.colorHex), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${quest.difficulty.badgeIcon} ${quest.difficulty.rarityLabel} (${quest.difficulty.displayName})",
                                                color = Color(quest.difficulty.colorHex),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // High-Value / Important Reward Badge
                                        quest.rewardValueLabel?.let { label ->
                                            val badgeColor = when (quest.difficulty) {
                                                QuestDifficulty.GRANDMASTER -> Color(0xFFE91E63)
                                                QuestDifficulty.MASTER -> Color(0xFF9C27B0)
                                                else -> Color(0xFFFFD700)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(badgeColor.copy(alpha = 0.25f))
                                                    .border(1.dp, badgeColor, RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = badgeColor,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }

                                        Text(
                                            text = "⏱️ ${com.example.data.models.formatQuestDuration(calculatedDuration)}",
                                            color = OsrsParchment,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            if (isCompleted) {
                                Text("✅ Done", color = Color(0xFF70E000), fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                            }
                        }

                        Text(quest.description, color = OsrsParchment, fontSize = 10.sp, maxLines = 2)

                        // Requirements & Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text("📋 Requirements:", color = OsrsGold, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)

                                // Required Combat
                                Text(
                                    text = "• Rec Combat Lvl ${quest.recCombatLevel} ${if (hasCombatReq) "✓" else "✗"}",
                                    color = if (hasCombatReq) Color(0xFF81C784) else Color(0xFFFF6B6B),
                                    fontSize = 9.5.sp
                                )

                                // Required Skill
                                quest.reqSkill?.let { skill ->
                                    Text(
                                        text = "• ${skill.displayName} Lvl ${quest.reqSkillLevel} ${if (hasSkillReq) "✓" else "✗"}",
                                        color = if (hasSkillReq) Color(0xFF81C784) else Color(0xFFFF6B6B),
                                        fontSize = 9.5.sp
                                    )
                                }

                                // Required Items
                                if (quest.requiredItems.isNotEmpty()) {
                                    Text(
                                        text = "• Required Items Submission (${if (hasAllItems) "Ready ✓" else "Missing Items ✗"}):",
                                        color = if (hasAllItems) Color(0xFF81C784) else Color(0xFFFF8A80),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    quest.requiredItems.forEach { req ->
                                        val normReqId = com.example.data.models.DefaultItems.normalizeItemId(req.itemId)
                                        val invCount = inventoryItems.filter { it.id == req.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity }
                                        val bankCount = bankItems.filter { it.id == req.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity }
                                        val totalCount = invCount + bankCount
                                        val hasThisItem = totalCount >= req.requiredQty
                                        Text(
                                            text = "    ${req.itemEmoji} ${req.itemName}: $totalCount/${req.requiredQty} ${if (hasThisItem) "✓" else "✗"}",
                                            color = if (hasThisItem) Color(0xFF81C784) else Color(0xFFFF8A80),
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                // Rewards Section
                                Text("🎁 Rewards:", color = OsrsGold, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp, bottom = 2.dp)
                                ) {
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            color = Color(0xFF382D1E),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "⭐ +${quest.questPoints} QP",
                                                color = OsrsTextYellow,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (quest.rewardGp > 0) {
                                            Surface(
                                                color = Color(0xFF382D1E),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "🪙 +${quest.rewardGp} GP",
                                                    color = Color(0xFFFFD700),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (quest.rewardItemName != null || quest.rewardItemId != null) {
                                            Surface(
                                                color = Color(0xFF1E3320),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF70E000)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "🗡️ Item: ${quest.rewardItemEmoji ?: "🎁"} ${quest.rewardItemName ?: quest.rewardItemId}",
                                                    color = Color(0xFF70E000),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        quest.rewardXpMap.forEach { (skill, xp) ->
                                            Surface(
                                                color = Color(0xFF1F2838),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64B5F6)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "🧠 +$xp ${skill.displayName} XP",
                                                    color = Color(0xFF90CAF9),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        quest.unlockedFeatures.forEach { feature ->
                                            Surface(
                                                color = Color(0xFF0D332F),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26A69A)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = feature,
                                                    color = Color(0xFF80CBC4),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (hasSavedProgress) {
                                val pct = ((1f - savedProgress!!.remainingSeconds.toFloat() / savedProgress.totalDurationSeconds.toFloat()) * 100).toInt()
                                Surface(
                                    color = Color(0xFF382B1B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "💾 Saved Progress: $pct% (${com.example.data.models.formatQuestDuration(savedProgress.remainingSeconds)} left)",
                                        color = OsrsTextYellow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (isCompleted) {
                                Button(
                                    onClick = { viewModel.showQuestCompletionPopup(quest) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("view_quest_scroll_${quest.id}_button")
                                ) {
                                    Text("📜 View Scroll", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            if (isCurrentActive) {
                                                viewModel.pauseQuestExpedition()
                                            } else {
                                                viewModel.startQuestExpedition(quest)
                                            }
                                        },
                                        enabled = (isReadyToStart && activeQuestState == null) || isCurrentActive,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = when {
                                                isCurrentActive -> Color(0xFFD97706)
                                                hasSavedProgress -> Color(0xFF2E7D32)
                                                isReadyToStart -> OsrsRedFrame
                                                else -> Color.DarkGray
                                            }
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("start_quest_${quest.id}_button")
                                    ) {
                                        Text(
                                            text = when {
                                                isCurrentActive -> "⏸️ Pause"
                                                hasSavedProgress && activeQuestState == null -> "▶️ Resume"
                                                activeQuestState != null -> "Pet Busy"
                                                !hasSkillReq -> "Need Skill"
                                                !hasCombatReq -> "Need Combat"
                                                !hasPreReqs -> "Need Prereqs"
                                                !hasAllItems -> "Need Items"
                                                else -> "🚀 Send Pet"
                                            },
                                            color = if ((isReadyToStart && activeQuestState == null) || isCurrentActive) OsrsTextYellow else Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (hasSavedProgress && !isCurrentActive) {
                                        Button(
                                            onClick = { viewModel.cancelQuestExpedition(quest.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("🗑️", color = Color.White, fontSize = 10.sp)
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

    selectedRewardItemForDetail?.let { item ->
        UniqueItemDetailDialog(
            item = item,
            onDismissRequest = { selectedRewardItemForDetail = null }
        )
    }
}

@Composable
fun QuestSettingsDialog(
    completedQuests: List<OsrsQuest>,
    onMarkIncomplete: (String) -> Unit,
    onMarkAllIncomplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var dialogSearchQuery by remember { mutableStateOf("") }

    val filteredCompleted = remember(completedQuests, dialogSearchQuery) {
        if (dialogSearchQuery.isBlank()) {
            completedQuests
        } else {
            completedQuests.filter {
                it.name.contains(dialogSearchQuery, ignoreCase = true) ||
                it.description.contains(dialogSearchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⚙️", fontSize = 20.sp)
                    Text(
                        text = "Completed Quests Settings",
                        color = OsrsTextYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("❌", fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Mark completed quests as incomplete to test and redo their expeditions.",
                    color = OsrsParchment,
                    fontSize = 11.sp
                )

                if (completedQuests.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dialogSearchQuery,
                            onValueChange = { dialogSearchQuery = it },
                            placeholder = { Text("Search completed...", color = Color.Gray, fontSize = 11.sp) },
                            textStyle = TextStyle(color = OsrsParchment, fontSize = 11.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OsrsGold,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedContainerColor = OsrsLeatherDark,
                                unfocusedContainerColor = OsrsLeatherDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { onMarkAllIncomplete() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("reset_all_quests_button")
                        ) {
                            Text("🔄 Reset All", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        color = Color(0xFF1E140C),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (filteredCompleted.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching completed quests found.",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filteredCompleted, key = { it.id }) { quest ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5C4028)),
                                        shape = RoundedCornerShape(4.dp),
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
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(quest.iconEmoji, fontSize = 18.sp)
                                                Column {
                                                    Text(
                                                        text = quest.name,
                                                        color = OsrsTextYellow,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(3.dp))
                                                                .background(Color(quest.difficulty.colorHex).copy(alpha = 0.2f))
                                                                .border(1.dp, Color(quest.difficulty.colorHex), RoundedCornerShape(3.dp))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                quest.difficulty.displayName,
                                                                color = Color(quest.difficulty.colorHex),
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Text("⭐ +${quest.questPoints} QP", color = Color(0xFFFFD700), fontSize = 9.5.sp)
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = { onMarkIncomplete(quest.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                                shape = RoundedCornerShape(4.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier
                                                    .height(30.dp)
                                                    .testTag("mark_incomplete_${quest.id}_button")
                                            ) {
                                                Text("🔄 Mark Incomplete", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📜", fontSize = 32.sp)
                            Text(
                                text = "No completed quests yet!",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Send your pet on quest expeditions to complete them.",
                                color = OsrsParchment,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OsrsGold),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("close_quest_settings_button")
            ) {
                Text("Done", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        containerColor = OsrsLeatherDark,
        shape = RoundedCornerShape(8.dp)
    )


}
