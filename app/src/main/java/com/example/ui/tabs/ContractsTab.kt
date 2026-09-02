package com.example.ui.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.DefaultItems
import com.example.data.models.EquipmentData
import com.example.data.models.InventoryItem
import com.example.data.models.NpcData
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.data.models.ShamanVillagerNpc
import com.example.data.models.SkillContract
import com.example.data.models.SkillContractData
import com.example.data.models.SkillOutfitData
import com.example.data.models.SkillOutfitPiece
import com.example.data.models.SkillOutfitSet
import com.example.ui.theme.OsrsGold
import com.example.ui.theme.OsrsTextYellow
import com.example.viewmodel.PetViewModel

fun getFavorDestination(skill: OsrsSkill): Pair<Int, String> {
    return when (skill) {
        OsrsSkill.WOODCUTTING -> Pair(21, "The Grove")
        OsrsSkill.FISHING -> Pair(20, "Shaman Pool & Fishing")
        OsrsSkill.SMITHING -> Pair(14, "Smithing & Forging")
        OsrsSkill.FLETCHING -> Pair(13, "Fletching Tab")
        OsrsSkill.COOKING -> Pair(4, "Cauldron")
        OsrsSkill.FIREMAKING -> Pair(5, "POH Campfire")
        OsrsSkill.HERBLORE -> Pair(15, "Herblore Tab")
        OsrsSkill.RUNECRAFT -> Pair(12, "Magic & Runecrafting")
        OsrsSkill.CONSTRUCTION -> Pair(5, "POH Estate")
        OsrsSkill.FARMING -> Pair(9, "POF Farm")
        OsrsSkill.HUNTER -> Pair(6, "Hunter Grounds")
        OsrsSkill.SLAYER -> Pair(6, "Slayer Grounds")
        OsrsSkill.AGILITY -> Pair(18, "Navigation & Rooftops")
        OsrsSkill.THIEVING -> Pair(16, "Trickery & Thieving")
        OsrsSkill.ADVENTURING -> Pair(19, "Adventuring")
        OsrsSkill.SAILING -> Pair(18, "Navigation Tab")
        OsrsSkill.DIVINATION -> Pair(22, "Divination Conflux")
        OsrsSkill.MAGIC -> Pair(12, "Magic Spellbook")
        else -> Pair(1, "Skills Tab")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContractsTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: ((Int, String) -> Unit)? = null
) {
    val contractsMap by viewModel.contractsMap.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val equippedItems by viewModel.equippedItems.collectAsStateWithLifecycle()
    val petState by viewModel.petState.collectAsStateWithLifecycle()

    val selectedCategoryFilter by viewModel.selectedContractCategory.collectAsStateWithLifecycle()
    val npcFavorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    var skillToOpenReward by remember { mutableStateOf<OsrsSkill?>(null) }
    var selectedVillagerInfo by remember { mutableStateOf<ShamanVillagerNpc?>(null) }
    var selectedNpcForRewards by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var showVillagerRoster by remember { mutableStateOf(false) }
    var showOutfitsDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var openQuantity by remember { mutableIntStateOf(1) }

    // Calculate total owned contract / favor rewards
    val ownedContractRewards = remember(inventoryItems, bankItems) {
        val allItems = (inventoryItems + bankItems).distinctBy { it.id }
        SkillContractData.CONTRACT_SUPPORTED_SKILLS.mapNotNull { skill ->
            val prefix = skill.name.lowercase()
            val rewardItemId = "item_contract_reward_$prefix"
            val total = allItems.find { it.id == rewardItemId }?.quantity ?: 0
            if (total > 0) skill to total else null
        }
    }

    // Total outfits collected count
    val totalOutfitPieces = SkillOutfitData.ALL_PIECES.size
    val ownedOutfitPiecesCount = remember(petState.unlockedOutfitIds) {
        val unlockedIds = petState.unlockedOutfitIds.toSet()
        SkillOutfitData.ALL_PIECES.count { unlockedIds.contains(it.id) }
    }

    val categories = listOf("All", "In Progress", "Gathering", "Artisan", "Support & Adventure")

    val inProgressCount = remember(contractsMap) {
        SkillContractData.CONTRACT_SUPPORTED_SKILLS.count { skill ->
            val contract = contractsMap[skill]
            contract != null && contract.currentQty > 0
        }
    }

    val readyCount = remember(contractsMap) {
        SkillContractData.CONTRACT_SUPPORTED_SKILLS.count { skill ->
            val contract = contractsMap[skill]
            contract != null && contract.currentQty >= contract.targetQty
        }
    }

    val filteredSkills = remember(selectedCategoryFilter, contractsMap, searchQuery) {
        val list = SkillContractData.CONTRACT_SUPPORTED_SKILLS.filter { skill ->
            val contract = contractsMap[skill]
            val isReady = contract != null && contract.currentQty >= contract.targetQty
            val isInProgress = contract != null && contract.currentQty > 0
            val matchesCategory = when (selectedCategoryFilter) {
                "In Progress", "Started" -> isInProgress
                "Done", "Ready to Turn In" -> isReady
                "Gathering" -> skill in listOf(
                    OsrsSkill.WOODCUTTING, OsrsSkill.FISHING,
                    OsrsSkill.FARMING, OsrsSkill.HUNTER, OsrsSkill.DIVINATION
                )
                "Artisan" -> skill in listOf(
                    OsrsSkill.SMITHING, OsrsSkill.FLETCHING,
                    OsrsSkill.COOKING, OsrsSkill.FIREMAKING, OsrsSkill.HERBLORE,
                    OsrsSkill.RUNECRAFT, OsrsSkill.CONSTRUCTION
                )
                "Support & Adventure" -> skill in listOf(
                    OsrsSkill.SLAYER, OsrsSkill.AGILITY, OsrsSkill.THIEVING,
                    OsrsSkill.ADVENTURING, OsrsSkill.SAILING
                )
                else -> true
            }
            if (!matchesCategory) return@filter false

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                val skillMatch = skill.displayName.lowercase().contains(q)
                val (guildNpc, _) = SkillContractData.getGuildInfo(skill)
                val npcMatch = guildNpc.name.lowercase().contains(q)
                val contractMatch = contract != null && (
                    contract.taskTitle.lowercase().contains(q) ||
                    contract.npcName.lowercase().contains(q) ||
                    (contract.targetEntityId?.lowercase()?.contains(q) == true)
                )
                skillMatch || npcMatch || contractMatch
            } else {
                true
            }
        }
        // Always sort completed / ready-to-turn-in favors automatically to the top of the list!
        list.sortedWith(
            compareByDescending<OsrsSkill> { skill ->
                val contract = contractsMap[skill]
                contract != null && contract.currentQty >= contract.targetQty
            }.thenByDescending { skill ->
                val contract = contractsMap[skill]
                contract != null && contract.currentQty > 0
            }.thenBy { it.displayName }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF16120E))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Banner: Shaman Tribe Favors
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contracts_header_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2218)),
                    border = BorderStroke(1.dp, Color(0xFF8B6508)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4A3520),
                                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🤝", fontSize = 22.sp)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Shaman Tribe Favors",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Help village members with their specific craft disciplines to earn Favor Boxes, GP, and skilling outfits!",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFD7CCC8),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Villagers Directory Toggle Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E1610))
                                .border(1.dp, Color(0xFF4E3825), RoundedCornerShape(8.dp))
                                .clickable { showVillagerRoster = !showVillagerRoster }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🛖", fontSize = 14.sp)
                                Text(
                                    text = "Shaman Tribe Villagers (${NpcData.VILLAGERS.size})",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OsrsGold
                                )
                            }
                            Text(
                                text = if (showVillagerRoster) "▲ Hide Villagers" else "▼ View Villager Disciplines",
                                fontSize = 11.sp,
                                color = Color(0xFFFFCC80)
                            )
                        }

                        // Expanded Villager Roster List
                        AnimatedVisibility(visible = showVillagerRoster) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                NpcData.VILLAGERS.forEach { villager ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF221A12),
                                        border = BorderStroke(1.dp, Color(0xFF5D4037)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedVillagerInfo = villager }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(villager.avatarEmoji, fontSize = 20.sp)
                                            Column(modifier = Modifier.weight(1f)) {
                                                val vFavorLvl = viewModel.getNpcFavorLevel(villager.id)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = villager.name,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFFFD700),
                                                            fontSize = 12.sp
                                                        )
                                                        Text(
                                                            text = "• ${villager.role}",
                                                            color = Color.LightGray,
                                                            fontSize = 10.5.sp
                                                        )
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFF18120D),
                                                        border = BorderStroke(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                                                    ) {
                                                        Text(
                                                            text = "🤝 Favor Lv.$vFavorLvl",
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFFFD700),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "Favors: ${villager.favorTypeLabels.joinToString(" • ")}",
                                                    color = Color(0xFFA5D6A7),
                                                    fontSize = 9.5.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Outfit Collection Stats Row (Interactive: tap or hold to inspect full outfit completion)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E1610))
                                .border(1.dp, Color(0xFF4E3825), RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = { showOutfitsDialog = true },
                                    onLongClick = { showOutfitsDialog = true }
                                )
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .testTag("skilling_outfits_collected_row"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🎽", fontSize = 15.sp)
                                Column {
                                    Text(
                                        text = "Skilling Outfits Collected",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.LightGray
                                    )
                                    Text(
                                        text = "Hold or tap to view outfit completion",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFFCC80)
                                    )
                                }
                            }
                            Text(
                                text = "$ownedOutfitPiecesCount / $totalOutfitPieces (+${ownedOutfitPiecesCount * 5}% XP)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }
                    }
                }
            }

            // Owned Favor Reward Boxes Section (if any owned)
            if (ownedContractRewards.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contracts_owned_rewards_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF221A12)),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🎁", fontSize = 16.sp)
                                    Text(
                                        text = "Unopened Favor Reward Boxes",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81C784)
                                    )
                                }
                                Text(
                                    text = "${ownedContractRewards.sumOf { it.second }} Boxes Total",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(ownedContractRewards) { (skill, count) ->
                                    val (npc, _) = SkillContractData.getGuildInfo(skill)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF2E2419),
                                        border = BorderStroke(1.dp, Color(0xFF81C784)),
                                        modifier = Modifier.clickable {
                                            skillToOpenReward = skill
                                            openQuantity = count.coerceAtMost(1)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(npc.avatarEmoji, fontSize = 14.sp)
                                            Column {
                                                Text(
                                                    text = "${skill.displayName} ($count)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Tap to open",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF81C784)
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

            // Favors Search Bar (Right above Category Filters)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1610),
                    border = BorderStroke(1.dp, Color(0xFF5A4432))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔍", fontSize = 14.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("favors_search_bar_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFFFFD700),
                                fontSize = 12.5.sp
                            ),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search favors, skills, tasks, or villagers...",
                                        color = Color(0xFF8D6E63),
                                        fontSize = 12.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { searchQuery = "" }
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        val badge = when (cat) {
                            "In Progress" -> if (inProgressCount > 0) " ($inProgressCount)" else ""
                            "Done", "Ready to Turn In" -> if (readyCount > 0) " ($readyCount)" else ""
                            else -> ""
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF8B6508) else Color(0xFF2C241B),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF4A3B2C)),
                            modifier = Modifier.clickable { viewModel.setContractCategoryFilter(cat) }
                        ) {
                            Text(
                                text = "$cat$badge",
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Tribe Favor Cards
            val allSkillLevelsMap = skillXpMap.mapValues { OsrsXpCalculator.getLevelForXp(it.value) }
            items(filteredSkills, key = { it.name }) { skill ->
                val contract = contractsMap[skill] ?: SkillContractData.generateContractForSkill(
                    skill,
                    OsrsXpCalculator.getLevelForXp(skillXpMap[skill] ?: 0L),
                    emptyList(),
                    allSkillLevels = allSkillLevelsMap
                )
                val skillXp = skillXpMap[skill] ?: 0L
                val skillLevel = OsrsXpCalculator.getLevelForXp(skillXp)
                val isCompleted = contract.currentQty >= contract.targetQty
                val progress = if (contract.targetQty > 0) {
                    (contract.currentQty.toFloat() / contract.targetQty.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contract_card_${skill.name.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) Color(0xFF1B2E1E) else Color(0xFF241C15)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isCompleted) Color(0xFF4CAF50) else Color(0xFF5A4432)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Card Header: Villager Info & Discipline
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
                                    color = Color(0xFF3B2D20),
                                    border = BorderStroke(1.dp, if (isCompleted) Color(0xFF4CAF50) else Color(0xFF8B7355)),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(contract.npcEmoji, fontSize = 20.sp)
                                    }
                                }
                                Column {
                                    Text(
                                        text = "${contract.npcName}'s Favor",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) Color(0xFF81C784) else Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = "${contract.npcRole} • ${skill.displayName}",
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            val npcFavorLevel = viewModel.getNpcFavorLevel(contract.npcId)
                            // Favor Level Badge (Clickable to view all 50 level rewards)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF18120D),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.7f)),
                                modifier = Modifier.clickable {
                                    selectedNpcForRewards = Triple(contract.npcId, contract.npcName, contract.npcEmoji)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text("🤝", fontSize = 10.sp)
                                    Text(
                                        text = "Favor Lvl $npcFavorLevel",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }

                        // NPC Quote / Greeting
                        if (contract.npcLoreQuote.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1B140E),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "\"${contract.npcLoreQuote}\"",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFFE0B2),
                                    style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Task Description & Progress
                        val currentSkillXp = skillXpMap[skill] ?: 0L
                        val currentSkillLevel = OsrsXpCalculator.getLevelForXp(currentSkillXp)
                        val fishingLevel = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.FISHING] ?: 0L)
                        val isLevelAppropriate = com.example.data.models.SkillContractData.isFavorLevelAppropriate(contract, currentSkillLevel, fishingLevel)
                        val reqLevel = com.example.data.models.SkillContractData.getRequiredLevelForFavorTarget(contract.targetEntityId, contract.taskTitle)
                        val reqFishingLevel = if (contract.skill == OsrsSkill.COOKING) com.example.data.models.SkillContractData.getRequiredFishingLevelForCookingTarget(contract.targetEntityId, contract.taskTitle) else 1

                        Text(
                            text = "Favor: ${contract.taskTitle}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        if (contract.skill == OsrsSkill.SAILING && contract.targetEntityId.startsWith("parcel:")) {
                            val parcelInfo = viewModel.getActiveBarnabyParcelInfo()
                            if (parcelInfo != null) {
                                val sVillager = com.example.data.models.NpcData.findNpcById(parcelInfo.sourceNpcId)
                                val dVillager = com.example.data.models.NpcData.findNpcById(parcelInfo.destNpcId)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0D253A),
                                    border = BorderStroke(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "📦 Route: ${sVillager?.avatarEmoji ?: "👤"} ${parcelInfo.sourceNpcName} ➔ ${dVillager?.avatarEmoji ?: "👤"} ${parcelInfo.destNpcName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF80D8FF)
                                        )
                                        val statusText = when {
                                            isCompleted -> "✅ Parcel delivered to ${parcelInfo.destNpcName}! Ready to complete."
                                            parcelInfo.hasCollectedParcel -> "🎒 Parcel in Backpack: Deliver to ${parcelInfo.destNpcName} (${dVillager?.hutLocation ?: "Hut"})"
                                            else -> "📍 Step 1: Collect parcel from ${parcelInfo.sourceNpcName} (${sVillager?.hutLocation ?: "Hut"})"
                                        }
                                        Text(
                                            text = statusText,
                                            fontSize = 10.sp,
                                            color = if (isCompleted) Color(0xFF81C784) else Color(0xFFFFD54F),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        if (!isLevelAppropriate) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF4A1010),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.assignNewSkillContract(skill) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val errorMsg = if (contract.skill == OsrsSkill.COOKING && fishingLevel < reqFishingLevel) {
                                        "⚠️ Fishing Lv $fishingLevel too low for raw fish (Req: Lv.$reqFishingLevel)!"
                                    } else {
                                        "⚠️ Lvl $currentSkillLevel too low (Req: Lv.$reqLevel)!"
                                    }
                                    Text(errorMsg, color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Assign New Favor 🔄", color = Color(0xFFFFD700), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress: ${contract.currentQty} / ${contract.targetQty}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isCompleted) Color(0xFF81C784) else Color(0xFFFFD54F)
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFFB300),
                            trackColor = Color(0xFF382C22)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rewards Info Box (Featuring Favor XP)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF18120D))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF2E7D32).copy(alpha = 0.35f),
                                    border = BorderStroke(0.5.dp, Color(0xFF81C784))
                                ) {
                                    Text(
                                        text = "🤝 +${contract.rewardFavorXp} Favor XP",
                                        fontSize = 10.sp,
                                        color = Color(0xFF81C784),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "+${contract.rewardXp} ${skill.displayName} XP • +${contract.rewardGp} GP",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF332619)
                            ) {
                                Text(
                                    text = "1x 📦 Box",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons
                        val (destTab, destName) = getFavorDestination(skill)
                        if (isCompleted) {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { viewModel.claimSkillContract(skill) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("claim_contract_btn_${skill.name.lowercase()}")
                                ) {
                                    Text(
                                        text = "🎉 Complete Favor for ${contract.npcName}!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onNavigateToTab?.invoke(destTab, destName) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD54F)),
                                    border = BorderStroke(1.dp, Color(0xFF5D4037)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("📍", fontSize = 11.sp)
                                        Text("Go to $destName", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onNavigateToTab?.invoke(destTab, destName) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3420), contentColor = Color(0xFFFFD700)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("goto_favor_btn_${skill.name.lowercase()}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("📍", fontSize = 11.5.sp)
                                            Text(
                                                text = "Go to $destName",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFD700),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.assignNewSkillContract(skill) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D)),
                                        border = BorderStroke(1.dp, Color(0xFF6B533E)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("reroll_contract_btn_${skill.name.lowercase()}")
                                    ) {
                                        Text("🔄 Reroll", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Villager Details Dialog
    selectedVillagerInfo?.let { villager ->
        AlertDialog(
            onDismissRequest = { selectedVillagerInfo = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(villager.avatarEmoji, fontSize = 24.sp)
                    Column {
                        Text(
                            text = "${villager.name} - ${villager.title}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "${villager.role} • ${villager.hutLocation}",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = villager.description,
                        fontSize = 12.sp,
                        color = Color(0xFFD7CCC8),
                        lineHeight = 16.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1B140E),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${villager.greeting}\"",
                            fontSize = 11.sp,
                            color = Color(0xFFFFE0B2),
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Divider(color = Color(0xFF4A3B2C))

                    Text(
                        text = "📜 Specialization Disciplines (Up to 3 Favors):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OsrsGold
                    )

                    villager.favorTypeLabels.forEachIndexed { idx, label ->
                        val skill = villager.favoredActivities.getOrNull(idx)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("•", color = Color(0xFF81C784), fontSize = 14.sp)
                            Text(
                                text = "$label (${skill?.displayName ?: "Skill"})",
                                fontSize = 11.5.sp,
                                color = Color.White
                            )
                        }
                    }

                    com.example.ui.components.NpcActiveFavorsCompactSection(
                        npcId = villager.id,
                        npcName = villager.name,
                        viewModel = viewModel
                    )

                    com.example.ui.components.NpcParcelActionButtons(
                        npcId = villager.id,
                        npcName = villager.name,
                        viewModel = viewModel
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val v = villager
                            selectedVillagerInfo = null
                            selectedNpcForRewards = Triple(v.id, v.name, v.avatarEmoji)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("🎁 50 Level Rewards", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { selectedVillagerInfo = null },
                        modifier = Modifier.weight(0.7f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3520)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Close", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            containerColor = Color(0xFF231A12)
        )
    }

    // Selected NPC 50 Milestone Rewards Dialog

    // Skilling Outfits Completed Progress Dialog
    if (showOutfitsDialog) {
        val unlockedOutfitIds = petState.unlockedOutfitIds.toSet()
        var inspectingPiece by remember { mutableStateOf<SkillOutfitPiece?>(null) }

        AlertDialog(
            onDismissRequest = { 
                if (inspectingPiece != null) inspectingPiece = null else showOutfitsDialog = false 
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎽", fontSize = 22.sp)
                    Column {
                        Text(
                            text = "Skilling Outfits Progress",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "$ownedOutfitPiecesCount / $totalOutfitPieces Unlocked (+${ownedOutfitPiecesCount * 5}% XP Passive)",
                            fontSize = 11.sp,
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Passive XP Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1B2E1E),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✨", fontSize = 14.sp)
                            Text(
                                text = "Permanent Passive Buffs: Skill outfits never take up storage space and do not need to be equipped. Tap any piece to inspect or unlock!",
                                fontSize = 10.5.sp,
                                color = Color(0xFFA5D6A7),
                                lineHeight = 13.5.sp
                            )
                        }
                    }

                    // Inspecting Piece Details Dialog Subview
                    if (inspectingPiece != null) {
                        val piece = inspectingPiece!!
                        val isOwned = unlockedOutfitIds.contains(piece.id)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF2E2014),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(piece.iconEmoji, fontSize = 28.sp)
                                Text(
                                    text = piece.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "${piece.slotName} • +${piece.bonusXpPercent}% ${piece.skill.displayName} XP Passive",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = piece.description,
                                    fontSize = 10.5.sp,
                                    color = Color.LightGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isOwned) {
                                    Surface(
                                        color = Color(0xFF1B5E20),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "✅ UNLOCKED & ACTIVE (+5% XP)",
                                            color = Color(0xFF00FF9D),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            viewModel.unlockSkillOutfitPiece(piece.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Unlock Permanently (${piece.costGp} GP)",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { inspectingPiece = null },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Back to All Outfits", fontSize = 11.sp, color = Color.LightGray)
                                }
                            }
                        }
                    } else {
                        // Vertically stacked compact list of all skill outfits
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(SkillOutfitData.ALL_SETS, key = { it.skill.name }) { set ->
                                val skillName = set.skill.displayName
                                val pieces = set.pieces
                                val ownedCount = pieces.count { unlockedOutfitIds.contains(it.id) }
                                val totalCount = pieces.size
                                val isFullSet = ownedCount == totalCount && totalCount > 0
                                val passiveBonus = ownedCount * 5

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isFullSet) Color(0xFF263321) else Color(0xFF1E1712),
                                    border = BorderStroke(1.dp, if (isFullSet) Color(0xFF4CAF50) else Color(0xFF4A3B2C))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Set Title & Completion Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Text(set.iconEmoji, fontSize = 14.sp)
                                                Text(
                                                    text = "${set.setName} Set ($skillName)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isFullSet) Color(0xFFFFD700) else Color.White
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (isFullSet) {
                                                    Surface(
                                                        color = Color(0xFF2E7D32),
                                                        shape = RoundedCornerShape(3.dp)
                                                    ) {
                                                        Text(
                                                            text = "COMPLETE",
                                                            color = Color.White,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "$ownedCount / $totalCount (+$passiveBonus% XP)",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (ownedCount > 0) Color(0xFF81C784) else Color.Gray
                                                )
                                            }
                                        }

                                        // Compact Row of Pieces
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            pieces.forEach { piece ->
                                                val isOwned = unlockedOutfitIds.contains(piece.id)
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(28.dp)
                                                        .clickable {
                                                            inspectingPiece = piece
                                                        },
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isOwned) Color(0xFF2E5A36) else Color(0xFF2A2018),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isOwned) Color(0xFF81C784) else Color(0xFF3E3125)
                                                    )
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = piece.iconEmoji,
                                                            fontSize = 13.sp,
                                                            color = if (isOwned) Color.White else Color.Gray.copy(alpha = 0.4f)
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
            },
            confirmButton = {
                Button(
                    onClick = { showOutfitsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3520)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Close", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF231A12)
        )
    }

    selectedNpcForRewards?.let { info: Triple<String, String, String> ->
        val npcId = info.first
        val npcName = info.second
        val npcEmoji = info.third
        val favorLevel = viewModel.getNpcFavorLevel(npcId)
        val favorXp = viewModel.getNpcFavorXp(npcId)
        val reqXp = viewModel.getRequiredXpForFavorLevel(favorLevel)

        com.example.ui.components.NpcFavorRewardsDialog(
            npcId = npcId,
            npcName = npcName,
            npcEmoji = npcEmoji,
            currentLevel = favorLevel,
            currentXp = favorXp,
            reqXp = reqXp,
            onDismiss = { selectedNpcForRewards = null },
            onOfferTribute = {
                viewModel.addNpcFavorXp(npcId, 50L, npcName, "Tribute Offering")
            }
        )
    }

    // Open Reward Dialog with Multi-Quantity selection
    skillToOpenReward?.let { skill ->
        val prefix = skill.name.lowercase()
        val rewardItemId = "item_contract_reward_$prefix"
        val totalOwned = (inventoryItems + bankItems).distinctBy { it.id }.find { it.id == rewardItemId }?.quantity ?: 0
        val (npc, _) = SkillContractData.getGuildInfo(skill)

        AlertDialog(
            onDismissRequest = { skillToOpenReward = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(npc.avatarEmoji, fontSize = 22.sp)
                    Text(
                        text = "Open ${npc.name}'s ${skill.displayName} Boxes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "You own $totalOwned x ${npc.name}'s Favor Reward box(es).",
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Text(
                        text = "Each opened box grants:\n• 💰 3,500 - 12,000 Gold\n• 📦 2-4 varieties of ${skill.displayName} materials\n• 🌟 1% chance for an unowned piece of the ${skill.displayName} Skilling Outfit (+5% XP bonus)!",
                        fontSize = 11.5.sp,
                        color = Color(0xFFD7CCC8),
                        lineHeight = 16.sp
                    )

                    Divider(color = Color(0xFF4A3B2C))

                    Text(
                        text = "Choose quantity to open: $openQuantity",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )

                    // Quantity buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 5, 10, totalOwned).distinct().forEach { qty ->
                            if (qty in 1..totalOwned) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (openQuantity == qty) Color(0xFF8B6508) else Color(0xFF2C241B),
                                    border = BorderStroke(1.dp, if (openQuantity == qty) Color(0xFFFFD700) else Color(0xFF4A3B2C)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { openQuantity = qty }
                                ) {
                                    Text(
                                        text = if (qty == totalOwned && totalOwned > 10) "Max ($qty)" else "$qty",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (openQuantity == qty) Color(0xFFFFD700) else Color.LightGray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (totalOwned > 1) {
                        Slider(
                            value = openQuantity.toFloat(),
                            onValueChange = { openQuantity = it.toInt().coerceIn(1, totalOwned) },
                            valueRange = 1f..totalOwned.toFloat(),
                            steps = if (totalOwned > 2) totalOwned - 2 else 0,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFD700),
                                activeTrackColor = Color(0xFF8B6508),
                                inactiveTrackColor = Color(0xFF3E2723)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = openQuantity
                        skillToOpenReward = null
                        viewModel.openContractRewards(skill, count)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("🎁 Open $openQuantity Box(es)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { skillToOpenReward = null }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF231A12)
        )
    }
}
