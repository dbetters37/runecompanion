package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@Composable
fun TrainerLeagueTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val activeExpedition by viewModel.activeQuestExpedition.collectAsStateWithLifecycle()
    val savedQuestProgressMap by viewModel.savedQuestProgressMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()

    val petCombatLevel = remember(skillXpMap) {
        CombatManager.calculateCombatLevel(skillXpMap)
    }

    var selectedRegion by remember { mutableStateOf(TrainerRegion.KANTO) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedChapterId by remember { mutableStateOf<String?>(null) } // null = All
    var inspectedTotemForUnlocks by remember { mutableStateOf<TotemInfo?>(null) }
    var showAreaAtlasDialog by remember { mutableStateOf(false) }

    val completedSet = petState.completedQuestIds.toSet()
    val inventoryItemIds = inventoryItems.map { it.id }.toSet()
    val bankItemIds = bankItems.map { it.id }.toSet()

    val reqPrevChampion = selectedRegion.requiredPrevChampionQuestId
    val isSelectedRegionUnlocked = reqPrevChampion == null ||
            TrainerLeagueData.isQuestCompleted(reqPrevChampion, completedSet, inventoryItemIds, bankItemIds)

    val activeChapters = remember(selectedRegion) {
        TrainerLeagueData.getChaptersForRegion(selectedRegion)
    }
    val activeQuests = remember(selectedRegion) {
        TrainerLeagueData.getQuestsForRegion(selectedRegion)
    }
    val activeBadges = remember(selectedRegion) {
        TrainerLeagueData.getBadgesForRegion(selectedRegion)
    }

    val unlockedBadgesCount = activeBadges.count { badge ->
        inventoryItemIds.contains(badge.itemId) || bankItemIds.contains(badge.itemId) || TrainerLeagueData.isQuestCompleted(badge.gymQuestId, completedSet, inventoryItemIds, bankItemIds)
    }

    val championQuestId = when (selectedRegion) {
        TrainerRegion.KANTO -> "tl_kanto_30_champion"
        TrainerRegion.JOHTO -> "tl_johto_16_e4_lance"
        TrainerRegion.HOENN -> "tl_hoenn_16_champion"
        TrainerRegion.SINNOH -> "tl_sinnoh_16_champion"
    }

    val hasLeagueTrophy = TrainerLeagueData.isQuestCompleted(championQuestId, completedSet, inventoryItemIds, bankItemIds) ||
            (selectedRegion == TrainerRegion.JOHTO && TrainerLeagueData.isQuestCompleted("tl_johto_17_champion_red", completedSet, inventoryItemIds, bankItemIds))

    val filteredQuests = remember(searchQuery, selectedChapterId, activeQuests) {
        activeQuests.filter { quest ->
            val matchesSearch = searchQuery.isBlank() ||
                    quest.name.contains(searchQuery, ignoreCase = true) ||
                    quest.description.contains(searchQuery, ignoreCase = true)

            val matchesChapter = selectedChapterId == null || quest.chapterId == selectedChapterId

            matchesSearch && matchesChapter
        }
    }

    if (showAreaAtlasDialog) {
        ShamanPathAreaAtlasDialog(
            viewModel = viewModel,
            onDismiss = { showAreaAtlasDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(OsrsLeatherDark)
            .border(2.dp, OsrsGold, RoundedCornerShape(10.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- REGION SELECTOR TABS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrainerRegion.values().forEach { region ->
                val isUnlocked = region.requiredPrevChampionQuestId == null ||
                        completedSet.contains(region.requiredPrevChampionQuestId)
                val isSelected = selectedRegion == region

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedRegion = region
                        selectedChapterId = null
                        if (!isUnlocked) {
                            viewModel.addChatMessage("👀 Previewing locked ${region.displayName}: Defeat ${region.requiredPrevRegionName} Champion to embark on expeditions.")
                        }
                    },
                    label = {
                        Text(
                            text = if (isUnlocked) "${region.emoji} ${region.displayName}" else "${region.emoji} ${region.displayName} 🔒",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isUnlocked) OsrsGold else Color(0xFF8D4F1E),
                        selectedLabelColor = Color.Black,
                        containerColor = if (isUnlocked) OsrsLeatherMedium else Color(0xFF1E140C),
                        labelColor = if (isUnlocked) Color.White else Color.Gray
                    )
                )
            }
        }

        // --- LOCKED REALM PREVIEW BANNER ---
        if (!isSelectedRegionUnlocked) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF381616),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE57373)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔒", fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Locked Realm: ${selectedRegion.displayName} (Preview Mode)",
                            color = Color(0xFFFFCDD2),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Defeat ${selectedRegion.requiredPrevRegionName} Champion to unlock expeditions here. You can inspect upcoming territories, chapters, and rewards!",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // --- HEADER BANNER & BADGE CASE ---
        var isBadgesExpanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(selectedRegion.emoji, fontSize = 22.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "🪶 Shaman Path",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${selectedRegion.displayName} • ${selectedRegion.subtitle}",
                                color = OsrsParchment.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Area Atlas Dialog Button
                        Surface(
                            color = Color(0xFF382300),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showAreaAtlasDialog = true }
                                .testTag("shaman_path_area_atlas_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text("🗺️", fontSize = 11.sp)
                                Text(
                                    text = "Atlas",
                                    color = OsrsGoldBright,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Toggle Badges Button
                        Surface(
                            color = if (isBadgesExpanded) Color(0xFF3E2723) else Color(0xFF241810),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isBadgesExpanded) OsrsGold else Color(0xFF5C473A)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isBadgesExpanded = !isBadgesExpanded }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🏆 $unlockedBadgesCount/${activeBadges.size}",
                                    color = OsrsGold,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBadgesExpanded) "▲" else "▼",
                                    color = OsrsGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (isBadgesExpanded) {
                    HorizontalDivider(color = Color(0xFF4A3B32), thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

                    // --- BADGES & LEAGUE TROPHY DISPLAY CASE ---
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆 ${selectedRegion.regionName} Spirit Obelisks (Tap to inspect unlocks):",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (hasLeagueTrophy) {
                                Text(
                                    text = "👑 HALL OF FAME",
                                    color = OsrsGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (hasLeagueTrophy) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF382300),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("👑", fontSize = 16.sp)
                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Text(
                                            text = "${selectedRegion.regionName.uppercase()} LEAGUE CHAMPION",
                                            color = OsrsGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Hall of Fame Honoree • Defeated League Champion & Crowned Trophy",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (badge in activeBadges) {
                                val isUnlocked = inventoryItemIds.contains(badge.itemId) ||
                                        completedSet.contains(badge.gymQuestId)

                                BadgeSlot(
                                    emoji = badge.emoji,
                                    name = badge.name,
                                    isUnlocked = isUnlocked,
                                    onInspect = { inspectedTotemForUnlocks = badge }
                                )
                            }

                            // League Trophy Badge
                            BadgeSlot(
                                emoji = "👑",
                                name = "${selectedRegion.regionName} Trophy",
                                isUnlocked = hasLeagueTrophy,
                                onInspect = {
                                    inspectedTotemForUnlocks = TotemInfo(
                                        itemId = "trophy_${selectedRegion.id}",
                                        name = "${selectedRegion.regionName} League Trophy",
                                        emoji = "👑",
                                        gymQuestId = championQuestId
                                    )
                                }
                            )
                        }
                    }
                } else {
                    // Mini Badges Strip when collapsed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (badge in activeBadges) {
                            val isUnlocked = inventoryItemIds.contains(badge.itemId) ||
                                    completedSet.contains(badge.gymQuestId)

                            Text(
                                text = if (isUnlocked) badge.emoji else "🔒",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isUnlocked) Color(0xFF3E2723) else Color(0xFF1E140C))
                                    .clickable { inspectedTotemForUnlocks = badge }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- POPUP DIALOG FOR TOTEM UNLOCKS INSPECTION ---
        inspectedTotemForUnlocks?.let { totem ->
            val isUnlocked = inventoryItemIds.contains(totem.itemId) || completedSet.contains(totem.gymQuestId)
            val gymQuest = TrainerLeagueData.getQuestById(totem.gymQuestId)
            TotemUnlocksDialog(
                totem = totem,
                isUnlocked = isUnlocked,
                gymQuest = gymQuest,
                region = selectedRegion,
                onDismiss = { inspectedTotemForUnlocks = null }
            )
        }

        // --- ACTIVE EXPEDITION CARD ---
        activeExpedition?.let { exp ->
            val progressRatio = if (exp.totalDurationSeconds > 0) {
                ((exp.totalDurationSeconds - exp.remainingSeconds).toFloat() / exp.totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
            } else 1f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_expedition_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142E18)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4E9A55))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
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
                            Text(exp.quest.iconEmoji, fontSize = 18.sp)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = "🏃 Expedition: ${exp.quest.name}",
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${petState.customName} • ⏱️ ${formatQuestDuration(exp.remainingSeconds)} remaining",
                                    color = Color(0xFFFFD700),
                                    fontSize = 9.5.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { viewModel.pauseQuestExpedition() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Pause", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.cancelQuestExpedition(exp.quest.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Cancel", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Compact Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFF0D2111)
                        )
                        Text(
                            text = "${(progressRatio * 100).toInt()}%",
                            color = Color(0xFFA5D6A7),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- CHAPTER FILTER CHIPS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedChapterId == null,
                onClick = { selectedChapterId = null },
                label = { Text("All ${selectedRegion.regionName} (${activeQuests.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OsrsGold,
                    selectedLabelColor = Color.Black,
                    containerColor = OsrsLeatherMedium,
                    labelColor = Color.White
                )
            )

            activeChapters.forEach { chapter ->
                val questCount = activeQuests.count { it.chapterId == chapter.id }
                FilterChip(
                    selected = selectedChapterId == chapter.id,
                    onClick = {
                        selectedChapterId = if (selectedChapterId == chapter.id) null else chapter.id
                    },
                    label = { Text("${chapter.emoji} ${chapter.title.split(":").first()} ($questCount)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OsrsGold,
                        selectedLabelColor = Color.Black,
                        containerColor = OsrsLeatherMedium,
                        labelColor = Color.White
                    )
                )
            }
        }

        // --- SEARCH INPUT ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trainer_league_search_input"),
            placeholder = { Text("Search trails, realms, or Spirit Guardians...", color = Color.Gray, fontSize = 11.5.sp) },
            leadingIcon = { Text("🔍", fontSize = 13.sp) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Text("✕", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OsrsGold,
                unfocusedBorderColor = Color(0xFF4A3B32),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E140C),
                unfocusedContainerColor = Color(0xFF1E140C)
            )
        )

        // --- ROUTE & QUEST LIST ---
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(filteredQuests) { index, quest ->
                val isCompleted = TrainerLeagueData.isQuestCompleted(quest.id, completedSet, inventoryItemIds, bankItemIds)
                val isActive = activeExpedition?.quest?.id == quest.id
                val activeRemainingSeconds = if (isActive) activeExpedition?.remainingSeconds else null
                val savedProgress = savedQuestProgressMap[quest.id]
                val isPaused = savedProgress != null && savedProgress.isPaused

                // Prerequisite check: Must complete previous quest in sequence
                val preReqsMet = quest.reqQuestIds.all { TrainerLeagueData.isQuestCompleted(it, completedSet, inventoryItemIds, bankItemIds) }

                KantoRouteCard(
                    sequenceNumber = index + 1,
                    quest = quest,
                    petCombatLevel = petCombatLevel,
                    isCompleted = isCompleted,
                    isActive = isActive,
                    activeRemainingSeconds = activeRemainingSeconds,
                    isPaused = isPaused,
                    preReqsMet = preReqsMet,
                    isRegionUnlocked = isSelectedRegionUnlocked,
                    skillXpMap = skillXpMap,
                    inventoryItems = inventoryItems,
                    bankItems = bankItems,
                    viewModel = viewModel,
                    onStartExpedition = {
                        if (!isSelectedRegionUnlocked) {
                            viewModel.addChatMessage("🔒 Defeat ${selectedRegion.requiredPrevRegionName} Champion first to embark on expeditions in ${selectedRegion.displayName}!")
                        } else {
                            viewModel.startQuestExpedition(quest)
                        }
                    },
                    onCompleteQuest = {
                        viewModel.markQuestCompleted(quest.id)
                    },
                    onResetProgress = {
                        viewModel.markQuestIncomplete(quest.id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BadgeSlot(
    emoji: String,
    name: String,
    isUnlocked: Boolean,
    onInspect: () -> Unit = {}
) {
    val cleanName = remember(name) {
        name.replace(" Badge", "").replace(" Totem", "").replace(" Obelisk", "")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isUnlocked) Color(0xFF3E2723) else Color(0xFF1E140C),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) OsrsGold else Color.Gray.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .width(66.dp)
            .combinedClickable(
                onClick = onInspect,
                onLongClick = onInspect
            )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = cleanName,
                color = if (isUnlocked) OsrsGold else Color.Gray,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TotemUnlocksDialog(
    totem: TotemInfo,
    isUnlocked: Boolean,
    gymQuest: OsrsQuest?,
    region: TrainerRegion,
    onDismiss: () -> Unit
) {
    // Collect unlocks associated with this totem
    val cleanId = totem.itemId.removePrefix("item_totem_").removePrefix("item_badge_")

    val matchingForests = remember(totem) {
        val cleanName = totem.name.replace(" Totem", "").replace(" Obelisk", "")
        AdventuringStoryData.GROVE_FOREST_AREAS.filter { forest ->
            forest.reqTotemId == totem.itemId ||
            forest.reqTotemId == cleanId ||
            forest.reqTotemId == "item_totem_$cleanId" ||
            forest.reqTotemName?.contains(cleanName, ignoreCase = true) == true ||
            (totem.name.contains("Woodland", true) && forest.id == "grove_deep_sylvan") ||
            (totem.name.contains("Mist", true) && forest.id == "grove_elder_hollow") ||
            (totem.name.contains("Sacred", true) && (forest.id == "grove_lunar_glade" || forest.id == "grove_celestial_redwood")) ||
            (totem.name.contains("Ember", true) && forest.id == "grove_mystic_whisper") ||
            (totem.name.contains("Celestial", true) && (forest.id == "grove_primal_overgrowth" || forest.id == "grove_shadow_grove")) ||
            (totem.name.contains("Astral", true) && (forest.id == "grove_wyrmwood_reach" || forest.id == "grove_yggdrasil_roots")) ||
            (totem.name.contains("Sovereign", true) && forest.id == "grove_astral_sanctum")
        }
    }

    val matchingPools = remember(totem) {
        val cleanName = totem.name.replace(" Totem", "").replace(" Obelisk", "")
        AdventuringStoryData.SPIRIT_POOL_AREAS.filter { pool ->
            pool.reqTotemId == totem.itemId ||
            pool.reqTotemId == cleanId ||
            pool.reqTotemId == "item_totem_$cleanId" ||
            pool.reqTotemName?.contains(cleanName, ignoreCase = true) == true ||
            (totem.name.contains("Woodland", true) && pool.id == "pool_whispering_creek") ||
            (totem.name.contains("Mist", true) && pool.id == "pool_lunar_marsh") ||
            (totem.name.contains("Ancient", true) && pool.id == "pool_azure_springs") ||
            (totem.name.contains("Sacred", true) && pool.id == "pool_coral_reef") ||
            (totem.name.contains("Ember", true) && (pool.id == "pool_magma_caldera" || pool.id == "pool_sunken_grotto")) ||
            (totem.name.contains("Celestial", true) && pool.id == "pool_deep_trench") ||
            (totem.name.contains("Astral", true) && (pool.id == "pool_celestial_abyss" || pool.id == "pool_spirit_oasis")) ||
            (totem.name.contains("Sovereign", true) && pool.id == "pool_primordial_ocean")
        }
    }

    val matchingQuarries = remember(totem) {
        val cleanName = totem.name.replace(" Totem", "").replace(" Obelisk", "")
        AdventuringStoryData.GEMOLOGY_AREAS.filter { quarry ->
            quarry.reqTotemId == totem.itemId ||
            quarry.reqTotemId == cleanId ||
            quarry.reqTotemId == "item_totem_$cleanId" ||
            quarry.reqTotemName?.contains(cleanName, ignoreCase = true) == true ||
            (totem.name.contains("Woodland", true) && quarry.id == "quarry_surface") ||
            (totem.name.contains("Ancient", true) && quarry.id == "quarry_iron_ridge") ||
            (totem.name.contains("Ember", true) && (quarry.id == "quarry_volcanic_magma" || quarry.id == "quarry_mithril_veins")) ||
            (totem.name.contains("Astral", true) && quarry.id == "quarry_celestial_nexus") ||
            (totem.name.contains("Sovereign", true) && quarry.id == "quarry_adamant_chasm")
        }
    }

    val matchingTrickeryDistricts = remember(totem) {
        val cleanName = totem.name.replace(" Totem", "").replace(" Obelisk", "")
        TRICKERY_DISTRICTS.filter { district ->
            district.reqTotemId == totem.itemId ||
            district.reqTotemId == cleanId ||
            district.reqTotemId == "item_totem_$cleanId" ||
            district.reqTotemId == "item_badge_$cleanId" ||
            district.reqTotemName?.contains(cleanName, ignoreCase = true) == true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsLeatherDark,
            border = androidx.compose.foundation.BorderStroke(2.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUnlocked) Color(0xFF3E2723) else Color(0xFF1E140C),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked) OsrsGold else Color.Gray),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(totem.emoji, fontSize = 22.sp)
                            }
                        }
                        Column {
                            Text(
                                text = totem.name,
                                color = OsrsGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${region.displayName} Realm Obelisk",
                                color = OsrsParchment.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isUnlocked) Color(0xFF1B5E20) else Color(0xFF3E2723),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked) Color(0xFF81C784) else Color(0xFFFF8A80))
                    ) {
                        Text(
                            text = if (isUnlocked) "🟢 Unlocked" else "🔒 Locked",
                            color = if (isUnlocked) Color(0xFFA5D6A7) else Color(0xFFFFAB91),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Divider(color = Color(0xFF5C473A), thickness = 1.dp)

                // How to obtain / Gym Quest
                gymQuest?.let { quest ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2C1E14),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5C473A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "🏆 Attainment Trial:",
                                color = OsrsTextYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${quest.iconEmoji} ${quest.name}",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Requirement: Combat Level ${quest.recCombatLevel} • ${quest.reqSkill?.displayName ?: "Adventure"} Lv. ${quest.reqSkillLevel}",
                                color = OsrsParchment.copy(alpha = 0.8f),
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }

                // UNLOCKS SECTION
                Text(
                    text = "🌟 Territory & Feature Unlocks:",
                    color = OsrsGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                if (matchingForests.isEmpty() && matchingPools.isEmpty() && matchingQuarries.isEmpty() && matchingTrickeryDistricts.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF241810),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✨ Grants sacred regional blessings, unlocks elite Shaman Path trials, and empowers spirit summoning resonance.",
                            color = Color(0xFFE0E0E0),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Trickery / Rogue Districts Unlocks
                if (matchingTrickeryDistricts.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🗡️ Trickery Territory Unlocked:",
                            color = Color(0xFFCE93D8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        matchingTrickeryDistricts.forEach { district ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF23122B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFAB47BC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(district.emoji, fontSize = 16.sp)
                                    Column {
                                        Text(
                                            text = district.name,
                                            color = Color(0xFFE1BEE7),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Thieving Lv. ${district.reqLevel} • ${district.specialPerkDesc ?: "${district.npcs.size} Targets"}",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // The Grove Unlocks
                if (matchingForests.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🌲 The Grove Territories Unlocked:",
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        matchingForests.forEach { forest ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF142416),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(forest.emoji, fontSize = 16.sp)
                                    Column {
                                        Text(
                                            text = forest.name,
                                            color = Color(0xFFA5D6A7),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Woodcutting Lv. ${forest.reqLevel} • ${forest.choppableTrees.size} Tree Species",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Spirit Pool Unlocks
                if (matchingPools.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🎣 Spirit Pool Fishing Areas Unlocked:",
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        matchingPools.forEach { pool ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10202E),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1976D2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(pool.emoji, fontSize = 16.sp)
                                    Column {
                                        Text(
                                            text = pool.name,
                                            color = Color(0xFFBBDEFB),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Fishing Lv. ${pool.reqLevel} • Spirit Fishes",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Mining Quarry Unlocks
                if (matchingQuarries.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "⛏️ Mining Quarries Unlocked:",
                            color = Color(0xFFFFCC80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        matchingQuarries.forEach { quarry ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2C2216),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF57C00)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(quarry.emoji, fontSize = 16.sp)
                                    Column {
                                        Text(
                                            text = quarry.name,
                                            color = Color(0xFFFFE0B2),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mining Lv. ${quarry.reqLevel} • Ore Veins",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dismiss button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OsrsGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KantoRouteCard(
    sequenceNumber: Int,
    quest: OsrsQuest,
    petCombatLevel: Int,
    isCompleted: Boolean,
    isActive: Boolean,
    activeRemainingSeconds: Int? = null,
    isPaused: Boolean = false,
    preReqsMet: Boolean,
    isRegionUnlocked: Boolean = true,
    skillXpMap: Map<OsrsSkill, Long> = emptyMap(),
    inventoryItems: List<InventoryItem> = emptyList(),
    bankItems: List<InventoryItem> = emptyList(),
    viewModel: PetViewModel? = null,
    onStartExpedition: () -> Unit,
    onCompleteQuest: () -> Unit = {},
    onResetProgress: () -> Unit
) {
    val estimatedSeconds = viewModel?.calculateEffectiveQuestDuration(quest, petCombatLevel) ?: quest.calculateDurationSeconds(petCombatLevel)
    var isExpanded by remember { mutableStateOf(false) }
    val hasCombatReq = petCombatLevel >= quest.recCombatLevel

    val playerSkillLevel = remember(quest.reqSkill, skillXpMap) {
        if (quest.reqSkill != null) {
            OsrsXpCalculator.getLevelForXp(skillXpMap[quest.reqSkill] ?: 0L)
        } else 1
    }
    val hasSkillReq = quest.reqSkill == null || playerSkillLevel >= quest.reqSkillLevel

    val missingItems = remember(quest.requiredItems, inventoryItems, bankItems) {
        quest.requiredItems.filter { req ->
            val normReqId = DefaultItems.normalizeItemId(req.itemId)
            val invCount = inventoryItems.filter { it.id == req.itemId || DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity.toLong() }
            val bankCount = bankItems.filter { it.id == req.itemId || DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity.toLong() }
            (invCount + bankCount) < req.requiredQty.toLong()
        }
    }
    val hasRequiredItems = missingItems.isEmpty()

    val canStart = isRegionUnlocked && preReqsMet && hasCombatReq && hasSkillReq && hasRequiredItems

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isCompleted -> Color(0xFF1B2E1C)
                    isActive -> Color(0xFF2C1E14)
                    !preReqsMet || !hasCombatReq || !hasSkillReq || !hasRequiredItems || !isRegionUnlocked -> Color(0xFF1F1714)
                    else -> OsrsLeatherMedium
                }
            )
            .border(
                1.dp,
                when {
                    isCompleted -> Color(0xFF4CAF50)
                    isActive -> Color(0xFFFFD700)
                    !hasCombatReq || !hasSkillReq || !hasRequiredItems -> Color(0xFF8B3A3A)
                    !preReqsMet || !isRegionUnlocked -> Color(0xFF3E2F28)
                    else -> Color(0xFF5C473A)
                },
                RoundedCornerShape(8.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // --- HEADER ROW: Sequence, Emoji, Title & Expand Icon ---
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2C2018))
                            .border(1.dp, Color(0xFF5C473A), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#$sequenceNumber",
                            color = OsrsGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(quest.iconEmoji, fontSize = 20.sp)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = quest.name,
                            color = if (isCompleted) Color(0xFF81C784) else OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        color = OsrsGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- METADATA BADGES (FlowRow with crisp tags & combat req) ---
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Combat Requirement Tag (prominent, readable)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (hasCombatReq) Color(0xFF1B381E) else Color(0xFF3E1B1B))
                        .border(1.dp, if (hasCombatReq) Color(0xFF4CAF50) else Color(0xFFE53935), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (hasCombatReq) "⚔️ Combat Lv ${quest.recCombatLevel} ✓" else "⚔️ Need Combat Lv ${quest.recCombatLevel} (You: $petCombatLevel)",
                        color = if (hasCombatReq) Color(0xFF81C784) else Color(0xFFFF8A80),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Difficulty Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(quest.difficulty.colorHex).copy(alpha = 0.25f))
                        .border(1.dp, Color(quest.difficulty.colorHex), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = quest.difficulty.displayName,
                        color = Color(quest.difficulty.colorHex),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Estimated Duration
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E2D3D).copy(alpha = 0.6f))
                        .border(1.dp, Color(0xFF42A5F5), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⏱️ ${formatQuestDuration(estimatedSeconds)}",
                        color = Color(0xFF90CAF9),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Skill Req Tag if present
                if (quest.reqSkill != null && quest.reqSkillLevel > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (hasSkillReq) Color(0xFF2E1C38) else Color(0xFF3E1B1B))
                            .border(1.dp, if (hasSkillReq) Color(0xFFAB47BC) else Color(0xFFE53935), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (hasSkillReq) "✨ ${quest.reqSkill.displayName} Lv ${quest.reqSkillLevel} ✓" else "✨ Need ${quest.reqSkill.displayName} Lv ${quest.reqSkillLevel} (You: $playerSkillLevel)",
                            color = if (hasSkillReq) Color(0xFFE1BEE7) else Color(0xFFFF8A80),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Missing Items Tag if present
                if (missingItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF3E1B1B))
                            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🎒 Need: ${missingItems.joinToString { "${it.itemName} x${it.requiredQty}" }}",
                            color = Color(0xFFFF8A80),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reward GP Tag
                if (quest.rewardGp > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF332A15))
                            .border(1.dp, OsrsGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🪙 +${quest.rewardGp} GP",
                            color = OsrsGold,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Expanded Details Section
            if (isExpanded) {
                Divider(color = Color(0xFF4A3B32), thickness = 1.dp)

                Text(
                    text = quest.description,
                    color = Color(0xFFE0E0E0),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )

                if (quest.requiredItems.isNotEmpty()) {
                    Text("🎒 Required Supplies:", color = OsrsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quest.requiredItems.forEach { req ->
                            val normReqId = DefaultItems.normalizeItemId(req.itemId)
                            val invCount = inventoryItems.filter { it.id == req.itemId || DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity.toLong() }
                            val bankCount = bankItems.filter { it.id == req.itemId || DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity.toLong() }
                            val totalHave = invCount + bankCount
                            val hasEnough = totalHave >= req.requiredQty.toLong()
                            RewardChip(
                                text = "${req.itemName} ($totalHave/${req.requiredQty})",
                                bgColor = if (hasEnough) Color(0xFF1E3823) else Color(0xFF3E1B1B),
                                borderColor = if (hasEnough) Color(0xFF66BB6A) else Color(0xFFE53935),
                                textColor = if (hasEnough) Color(0xFFA5D6A7) else Color(0xFFFF8A80)
                            )
                        }
                    }
                }

                Text("🎁 Expedition Rewards:", color = OsrsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RewardChip(
                        text = "🪙 +${quest.rewardGp} GP",
                        bgColor = Color(0xFF332A15),
                        borderColor = OsrsGold,
                        textColor = OsrsGold
                    )

                    quest.rewardItemName?.let { itemName ->
                        val emoji = quest.rewardItemEmoji ?: "🎁"
                        RewardChip(
                            text = "$emoji $itemName",
                            bgColor = Color(0xFF1E3823),
                            borderColor = Color(0xFF66BB6A),
                            textColor = Color(0xFFA5D6A7)
                        )
                    }

                    quest.rewardXpMap.forEach { (skill, xp) ->
                        RewardChip(
                            text = "+${xp} ${skill.displayName} XP",
                            bgColor = Color(0xFF2E1C38),
                            borderColor = Color(0xFFAB47BC),
                            textColor = Color(0xFFE1BEE7)
                        )
                    }

                    quest.unlockedFeatures.forEach { feature ->
                        RewardChip(
                            text = feature,
                            bgColor = Color(0xFF0D332F),
                            borderColor = Color(0xFF26A69A),
                            textColor = Color(0xFF80CBC4)
                        )
                    }
                }
            }

            // --- ACTION BUTTON ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCompleted) {
                    Button(
                        onClick = { },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF1B5E20),
                            disabledContentColor = Color(0xFF81C784)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp).testTag("quest_done_${quest.id}")
                    ) {
                        Text("✓ Completed", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isActive) {
                    val remaining = activeRemainingSeconds ?: 0
                    if (remaining > 0) {
                        Button(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFF3E2F28),
                                disabledContentColor = OsrsGold
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(30.dp).testTag("kanto_quest_in_progress_${quest.id}")
                        ) {
                            Text("⏳ In Progress (${formatQuestDuration(remaining)})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onCompleteQuest,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100), contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(30.dp).testTag("claim_kanto_quest_${quest.id}")
                        ) {
                            Text("⚡ Claim Rewards", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isPaused) {
                    Button(
                        onClick = onStartExpedition,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20), contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp).testTag("resume_kanto_quest_${quest.id}")
                    ) {
                        Text("▶️ Resume Expedition", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (!isRegionUnlocked) {
                    Button(
                        onClick = { },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF3E1B1B),
                            disabledContentColor = Color(0xFFFF8A80)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp).testTag("region_locked_${quest.id}")
                    ) {
                        Text("🔒 Region Locked", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (!preReqsMet) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF332924)
                    ) {
                        Text(
                            text = "🔒 Locked (Prev Quest Req)",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                } else if (!hasCombatReq) {
                    Button(
                        onClick = { },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF3E1B1B),
                            disabledContentColor = Color(0xFFFF8A80)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp).testTag("combat_locked_${quest.id}")
                    ) {
                        Text("⚔️ Need Combat Lv ${quest.recCombatLevel}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (!hasSkillReq) {
                    Button(
                        onClick = { },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF3E1B1B),
                            disabledContentColor = Color(0xFFFF8A80)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp).testTag("skill_locked_${quest.id}")
                    ) {
                        Text("🔒 Need ${quest.reqSkill?.displayName} Lv ${quest.reqSkillLevel}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (!hasRequiredItems) {
                    val firstMissing = missingItems.first()
                    Button(
                        onClick = { },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF3E1B1B),
                            disabledContentColor = Color(0xFFFF8A80)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp).testTag("item_locked_${quest.id}")
                    ) {
                        Text("🎒 Need ${firstMissing.itemName} x${firstMissing.requiredQty}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStartExpedition,
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsGold, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp).testTag("start_kanto_quest_${quest.id}")
                    ) {
                        Text("🚀 Send Expedition", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RewardChip(
    text: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
