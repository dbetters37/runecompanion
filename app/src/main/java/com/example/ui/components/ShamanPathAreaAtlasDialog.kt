package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
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

data class ShamanTerritoryArea(
    val id: String,
    val name: String,
    val emoji: String,
    val region: TrainerRegion,
    val chapterTitle: String,
    val description: String,
    val areaType: String, // "Chapter Trail", "Sacred Grove", "Spirit Pool", "Gem Quarry", "Trickery District"
    val reqLevel: Int = 1,
    val reqTotemId: String? = null,
    val reqTotemName: String? = null,
    val reqTotemEmoji: String? = null,
    val reqQuestId: String? = null,
    val reqQuestName: String? = null,
    val reqRegionChampionId: String? = null,
    val unlockedPerks: List<String> = emptyList()
)

object ShamanAreaAtlasData {

    fun getAllTerritories(): List<ShamanTerritoryArea> {
        val list = mutableListOf<ShamanTerritoryArea>()

        // 1. Chapters & Trails across all regions
        for (region in TrainerRegion.values()) {
            val chapters = TrainerLeagueData.getChaptersForRegion(region)
            val quests = TrainerLeagueData.getQuestsForRegion(region)

            for (ch in chapters) {
                val chQuests = quests.filter { it.chapterId == ch.id }
                val firstQuest = chQuests.firstOrNull()
                val bossQuest = chQuests.lastOrNull()
                val keyUnlocks = chQuests.flatMap { it.unlockedFeatures }.take(3)

                list.add(
                    ShamanTerritoryArea(
                        id = "territory_${ch.id}",
                        name = ch.title.split(":").getOrElse(1) { ch.title }.trim(),
                        emoji = ch.emoji,
                        region = region,
                        chapterTitle = ch.title.split(":").first().trim(),
                        description = ch.description,
                        areaType = "Chapter Realm Trail",
                        reqLevel = firstQuest?.recCombatLevel ?: 1,
                        reqQuestId = firstQuest?.reqQuestIds?.firstOrNull(),
                        reqQuestName = firstQuest?.name,
                        reqRegionChampionId = region.requiredPrevChampionQuestId,
                        unlockedPerks = keyUnlocks.ifEmpty { listOf("${chQuests.size} Sacred Trail Quests & Trials") }
                    )
                )
            }
        }

        // 2. Sacred Groves
        for (grove in AdventuringStoryData.GROVE_FOREST_AREAS) {
            val treesDesc = grove.choppableTrees.joinToString(", ") { "${it.emoji} ${it.name}" }
            val perks = mutableListOf<String>()
            if (grove.specialPerkDesc != null) perks.add("🌟 ${grove.specialPerkDesc}")
            perks.add("🌲 Trees: $treesDesc")

            list.add(
                ShamanTerritoryArea(
                    id = "grove_${grove.id}",
                    name = grove.name,
                    emoji = grove.emoji,
                    region = TrainerRegion.KANTO,
                    chapterTitle = "Sacred Forest Grove",
                    description = grove.description,
                    areaType = "Sacred Grove",
                    reqLevel = grove.reqLevel,
                    reqTotemId = grove.reqTotemId,
                    reqTotemName = grove.reqTotemName,
                    reqTotemEmoji = grove.reqTotemEmoji,
                    reqQuestId = grove.reqQuestId,
                    reqQuestName = grove.reqQuestName,
                    unlockedPerks = perks
                )
            )
        }

        // 3. Spirit Pools
        for (pool in AdventuringStoryData.SPIRIT_POOL_AREAS) {
            val fishDesc = pool.catchableFish.joinToString(", ") { "${it.emoji} ${it.name}" }
            val perks = mutableListOf<String>()
            if (pool.specialPerkDesc != null) perks.add("🌊 ${pool.specialPerkDesc}")
            perks.add("🐟 Fish: $fishDesc")

            list.add(
                ShamanTerritoryArea(
                    id = "pool_${pool.id}",
                    name = pool.name,
                    emoji = pool.emoji,
                    region = TrainerRegion.JOHTO,
                    chapterTitle = "Mystic Spirit Pool",
                    description = pool.description,
                    areaType = "Spirit Pool",
                    reqLevel = pool.reqLevel,
                    reqTotemId = pool.reqTotemId,
                    reqTotemName = pool.reqTotemName,
                    reqTotemEmoji = pool.reqTotemEmoji,
                    reqQuestId = pool.reqQuestId,
                    reqQuestName = pool.reqQuestName,
                    unlockedPerks = perks
                )
            )
        }

        // 4. Gemology & Mining Quarries
        for (quarry in AdventuringStoryData.GEMOLOGY_AREAS) {
            val oreDesc = quarry.minerals.joinToString(", ") { "${it.emoji} ${it.name}" }
            val perks = mutableListOf<String>()
            if (quarry.specialPerkDesc != null) perks.add("⛏️ ${quarry.specialPerkDesc}")
            perks.add("💎 Mineral Veins: $oreDesc")

            list.add(
                ShamanTerritoryArea(
                    id = "quarry_${quarry.id}",
                    name = quarry.name,
                    emoji = quarry.emoji,
                    region = TrainerRegion.HOENN,
                    chapterTitle = "Gem & Mineral Quarry",
                    description = quarry.description,
                    areaType = "Gem Quarry",
                    reqLevel = quarry.reqLevel,
                    reqTotemId = quarry.reqTotemId,
                    reqTotemName = quarry.reqTotemName,
                    reqTotemEmoji = quarry.reqTotemEmoji,
                    reqQuestId = quarry.reqQuestId,
                    reqQuestName = quarry.reqQuestName,
                    unlockedPerks = perks
                )
            )
        }

        return list
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShamanPathAreaAtlasDialog(
    viewModel: PetViewModel,
    onDismiss: () -> Unit
) {
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val completedSet = petState.completedQuestIds.toSet()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val inventoryItemIds = remember(inventoryItems) { inventoryItems.map { it.id }.toSet() }
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()

    val allAreas = remember { ShamanAreaAtlasData.getAllTerritories() }

    var selectedRegionFilter by remember { mutableStateOf<TrainerRegion?>(null) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "CHAPTERS", "GROVES", "POOLS", "QUARRIES"
    var showOnlyLocked by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAreaId by remember { mutableStateOf<String?>(allAreas.firstOrNull()?.id) }

    fun checkAreaUnlocked(area: ShamanTerritoryArea): Boolean {
        // Check Region Lock
        if (area.reqRegionChampionId != null && !completedSet.contains(area.reqRegionChampionId)) {
            return false
        }
        // Check Companion / Totem Lock
        if (area.reqTotemId != null) {
            if (!viewModel.isTotemUnlocked(area.reqTotemId)) {
                return false
            }
        } else if (area.reqTotemName != null) {
            val clean = area.reqTotemName.lowercase()
            val hasTotem = inventoryItemIds.any { it.contains(clean) } ||
                    completedSet.any { it.contains(clean) } ||
                    inventoryItems.any { it.name.contains(area.reqTotemName, ignoreCase = true) }
            if (!hasTotem) return false
        }
        // Check Quest Lock
        if (area.reqQuestId != null && !completedSet.contains(area.reqQuestId)) {
            return false
        }
        return true
    }

    val filteredAreas = remember(
        allAreas,
        selectedRegionFilter,
        selectedTypeFilter,
        showOnlyLocked,
        searchQuery,
        completedSet,
        inventoryItemIds
    ) {
        allAreas.filter { area ->
            val matchesRegion = selectedRegionFilter == null || area.region == selectedRegionFilter
            val matchesType = when (selectedTypeFilter) {
                "CHAPTERS" -> area.areaType == "Chapter Realm Trail"
                "GROVES" -> area.areaType == "Sacred Grove"
                "POOLS" -> area.areaType == "Spirit Pool"
                "QUARRIES" -> area.areaType == "Gem Quarry"
                else -> true
            }
            val isUnlocked = checkAreaUnlocked(area)
            val matchesLock = if (showOnlyLocked) !isUnlocked else true
            val matchesSearch = searchQuery.isBlank() ||
                    area.name.contains(searchQuery, ignoreCase = true) ||
                    area.chapterTitle.contains(searchQuery, ignoreCase = true) ||
                    area.description.contains(searchQuery, ignoreCase = true) ||
                    area.unlockedPerks.any { it.contains(searchQuery, ignoreCase = true) }

            matchesRegion && matchesType && matchesLock && matchesSearch
        }
    }

    val totalCount = allAreas.size
    val unlockedCount = allAreas.count { checkAreaUnlocked(it) }
    val lockedCount = totalCount - unlockedCount

    val selectedArea = remember(selectedAreaId, allAreas) {
        allAreas.find { it.id == selectedAreaId } ?: allAreas.firstOrNull()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsLeatherDark,
            border = BorderStroke(2.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- TOP HEADER ---
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E1C12),
                            border = BorderStroke(1.dp, OsrsGold),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🗺️", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Shaman Path Territory & Area Atlas",
                                    color = OsrsTextYellow,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF382300),
                                    border = BorderStroke(1.dp, OsrsGold)
                                ) {
                                    Text(
                                        text = "🔒 $lockedCount Locked • 🟢 $unlockedCount Unlocked",
                                        color = OsrsGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Preview all territories, sacred groves, pools, and requirements",
                                color = OsrsParchment.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("❌", fontSize = 14.sp)
                    }
                }

                // --- REGION FILTER CHIPS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedRegionFilter == null,
                        onClick = { selectedRegionFilter = null },
                        label = { Text("All Realms (${allAreas.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OsrsGold,
                            selectedLabelColor = Color.Black,
                            containerColor = OsrsLeatherMedium,
                            labelColor = Color.White
                        )
                    )

                    TrainerRegion.values().forEach { region ->
                        val isRegionUnlocked = region.requiredPrevChampionQuestId == null || completedSet.contains(region.requiredPrevChampionQuestId)
                        val isSelected = selectedRegionFilter == region

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRegionFilter = region },
                            label = {
                                Text(
                                    text = if (isRegionUnlocked) "${region.emoji} ${region.displayName}" else "${region.emoji} ${region.displayName} 🔒",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OsrsGold,
                                selectedLabelColor = Color.Black,
                                containerColor = if (isRegionUnlocked) OsrsLeatherMedium else Color(0xFF1E140C),
                                labelColor = if (isRegionUnlocked) Color.White else Color.Gray
                            )
                        )
                    }
                }

                // --- CATEGORY & LOCK FILTER CHIPS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "ALL" to "🌐 All Types",
                        "CHAPTERS" to "🪶 Realm Trails",
                        "GROVES" to "🌲 Sacred Groves",
                        "POOLS" to "🌊 Spirit Pools",
                        "QUARRIES" to "⛏️ Gem Quarries"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedTypeFilter == key,
                            onClick = { selectedTypeFilter = key },
                            label = { Text(label, fontSize = 10.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1A1F1C),
                                labelColor = Color.White
                            )
                        )
                    }

                    // Show Only Locked Toggle
                    FilterChip(
                        selected = showOnlyLocked,
                        onClick = { showOnlyLocked = !showOnlyLocked },
                        label = { Text(if (showOnlyLocked) "🔒 Locked Only" else "👁️ Show All", fontSize = 10.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFC62828),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF2B1914),
                            labelColor = Color(0xFFFFAB91)
                        )
                    )
                }

                // --- SEARCH BAR ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("area_atlas_search_input"),
                    placeholder = { Text("Search areas, trees, fish, minerals, or requirements...", color = Color.Gray, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OsrsGold,
                        unfocusedBorderColor = Color(0xFF4A3B32),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // --- AREA LIST ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF130E0A))
                        .border(1.dp, Color(0xFF3E2D20), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredAreas) { area ->
                        val isUnlocked = checkAreaUnlocked(area)
                        val isSelected = selectedAreaId == area.id

                        val requiresTotem = area.reqTotemId != null || area.reqTotemName != null
                        val hasTotem = if (area.reqTotemId != null) {
                            viewModel.isTotemUnlocked(area.reqTotemId)
                        } else if (area.reqTotemName != null) {
                            val clean = area.reqTotemName.lowercase()
                            inventoryItemIds.any { it.contains(clean) } || completedSet.any { it.contains(clean) } || inventoryItems.any { it.name.contains(area.reqTotemName, ignoreCase = true) }
                        } else true

                        val hasLevel = when (area.areaType) {
                            "Sacred Grove" -> OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.WOODCUTTING] ?: 0L) >= area.reqLevel
                            "Spirit Pool" -> OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.FISHING] ?: 0L) >= area.reqLevel
                            "Gem Quarry" -> OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.SMITHING] ?: 0L) >= area.reqLevel
                            "Trickery District" -> OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.THIEVING] ?: 0L) >= area.reqLevel
                            else -> true
                        }

                        val isObeliskLocked = requiresTotem && !hasTotem
                        val isLevelLocked = !hasLevel && hasTotem
                        val isDotted = isObeliskLocked || isLevelLocked
                        val areaItemAlpha = if (isObeliskLocked) 0.30f else 1.0f

                        val borderColor = when {
                            isSelected -> OsrsGold
                            isUnlocked -> Color(0xFF4CAF50)
                            isObeliskLocked || isLevelLocked -> Color(0xFFFFD54F)
                            else -> Color(0xFF6D4C41)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isSelected -> Color(0xFF382300)
                                isUnlocked -> Color(0xFF162B1A)
                                isObeliskLocked || isLevelLocked -> Color(0xFF2E260D)
                                else -> Color(0xFF1F140D)
                            },
                            border = if (!isDotted) BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                borderColor
                            ) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .alpha(areaItemAlpha)
                                .then(
                                    if (isDotted) Modifier.dashedBorder(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(6.dp),
                                        dashLength = 4.dp,
                                        gapLength = 4.dp
                                    ) else Modifier
                                )
                                .clickable { selectedAreaId = area.id }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
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
                                        Text(area.emoji, fontSize = 16.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = area.name,
                                                    color = if (isUnlocked) Color(0xFF81C784) else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = Color(0xFF2C1D14)
                                                ) {
                                                    Text(
                                                        text = area.areaType,
                                                        color = OsrsGold,
                                                        fontSize = 8.5.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${area.region.displayName} • ${area.chapterTitle}",
                                                color = OsrsParchment.copy(alpha = 0.75f),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isUnlocked) Color(0xFF1B5E20) else Color(0xFF3E2723),
                                        border = BorderStroke(1.dp, if (isUnlocked) Color(0xFF81C784) else Color(0xFFFF8A80))
                                    ) {
                                        Text(
                                            text = if (isUnlocked) "🟢 Unlocked" else "🔒 Locked Area",
                                            color = if (isUnlocked) Color(0xFFA5D6A7) else Color(0xFFFFAB91),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = area.description,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 9.5.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Unlock Requirements Line
                                if (!isUnlocked) {
                                    val reqText = when {
                                        area.reqRegionChampionId != null && !completedSet.contains(area.reqRegionChampionId) ->
                                            "🔒 Requires Defeating ${area.region.requiredPrevRegionName} Champion"
                                        area.reqTotemName != null ->
                                            "🔒 Requires ${area.reqTotemEmoji ?: "🗿"} ${area.reqTotemName}"
                                        area.reqQuestName != null ->
                                            "🔒 Requires Quest: ${area.reqQuestName}"
                                        else -> "🔒 Requires Lv. ${area.reqLevel}"
                                    }
                                    Text(
                                        text = reqText,
                                        color = Color(0xFFFFCC80),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (area.unlockedPerks.isNotEmpty()) {
                                    Text(
                                        text = area.unlockedPerks.joinToString(" • "),
                                        color = OsrsGold,
                                        fontSize = 8.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // --- BOTTOM INSPECTOR CARD ---
                selectedArea?.let { area ->
                    val isUnlocked = checkAreaUnlocked(area)

                    val requiresTotem = area.reqTotemId != null || area.reqTotemName != null
                    val hasTotem = if (area.reqTotemId != null) {
                        viewModel.isTotemUnlocked(area.reqTotemId)
                    } else if (area.reqTotemName != null) {
                        val clean = area.reqTotemName.lowercase()
                        inventoryItemIds.any { it.contains(clean) } || completedSet.any { it.contains(clean) } || inventoryItems.any { it.name.contains(area.reqTotemName, ignoreCase = true) }
                    } else true

                    val hasLevel = when (area.areaType) {
                        "Sacred Grove" -> OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.WOODCUTTING] ?: 0L) >= area.reqLevel
                        "Spirit Pool" -> OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.FISHING] ?: 0L) >= area.reqLevel
                        "Gem Quarry" -> OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.SMITHING] ?: 0L) >= area.reqLevel
                        "Trickery District" -> OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.THIEVING] ?: 0L) >= area.reqLevel
                        else -> true
                    }

                    val isObeliskLocked = requiresTotem && !hasTotem
                    val isLevelLocked = !hasLevel && hasTotem
                    val isDotted = isObeliskLocked || isLevelLocked
                    val inspectorAlpha = if (isObeliskLocked) 0.30f else 1.0f

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF231810),
                        border = if (!isDotted) BorderStroke(1.5.dp, OsrsGold) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(inspectorAlpha)
                            .then(
                                if (isDotted) Modifier.dashedBorder(
                                    width = 1.5.dp,
                                    color = Color(0xFFFFD54F),
                                    shape = RoundedCornerShape(8.dp),
                                    dashLength = 4.dp,
                                    gapLength = 4.dp
                                ) else Modifier
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                    Text(area.emoji, fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = area.name,
                                            color = OsrsTextYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = "${area.region.emoji} ${area.region.displayName} • ${area.areaType}",
                                            color = OsrsParchment.copy(alpha = 0.85f),
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isUnlocked) Color(0xFF1B5E20) else Color(0xFF3E2723),
                                    border = BorderStroke(1.dp, if (isUnlocked) Color(0xFF81C784) else Color(0xFFFF8A80))
                                ) {
                                    Text(
                                        text = if (isUnlocked) "🟢 Traversed" else "🔒 Locked Preview",
                                        color = if (isUnlocked) Color(0xFFA5D6A7) else Color(0xFFFFAB91),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = area.description,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp
                            )

                            // REQUIREMENTS & REWARDS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📋 Unlock Requirements:",
                                        color = Color(0xFFFFB74D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    )
                                    if (area.reqRegionChampionId != null) {
                                        Text("• Defeat ${area.region.requiredPrevRegionName} Champion", color = if (completedSet.contains(area.reqRegionChampionId)) Color(0xFF81C784) else Color.White, fontSize = 9.sp)
                                    }
                                    if (area.reqTotemName != null) {
                                        val obeliskMet = if (area.reqTotemId != null) viewModel.isTotemUnlocked(area.reqTotemId) else hasTotem
                                        Text(
                                            text = "• ${area.reqTotemEmoji ?: "🗿"} ${area.reqTotemName}",
                                            color = if (obeliskMet) Color(0xFF81C784) else Color.White,
                                            fontSize = 9.sp
                                        )
                                    }
                                    if (area.reqQuestName != null) {
                                        Text("• Complete ${area.reqQuestName}", color = if (area.reqQuestId != null && completedSet.contains(area.reqQuestId)) Color(0xFF81C784) else Color.White, fontSize = 9.sp)
                                    }
                                    if (area.reqLevel > 1) {
                                        Text("• Skill / Combat Lv. ${area.reqLevel}", color = if (hasLevel) Color(0xFF81C784) else Color.White, fontSize = 9.sp)
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🎁 Unlocked Content & Features:",
                                        color = OsrsGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    )
                                    area.unlockedPerks.forEach { perk ->
                                        Text("• $perk", color = Color(0xFFFFF59D), fontSize = 9.sp)
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
