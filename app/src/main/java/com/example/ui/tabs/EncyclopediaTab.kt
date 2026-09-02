package com.example.ui.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

enum class SkillGroupCategory(val label: String, val icon: String, val skills: List<OsrsSkill>?) {
    ALL("All Skills", "🌟", null),
    SKILL_OUTFITS("Skill outfits", "🥋", null),
    ARTISAN(
        "Artisan",
        "⚒️",
        listOf(
            OsrsSkill.COOKING,
            OsrsSkill.SMITHING,
            OsrsSkill.FLETCHING,
            OsrsSkill.HERBLORE,
            OsrsSkill.CONSTRUCTION,
            OsrsSkill.RUNECRAFT,
            OsrsSkill.FIREMAKING
        )
    ),
    COMBAT(
        "Combat",
        "⚔️",
        listOf(
            OsrsSkill.ATTACK,
            OsrsSkill.DEFENCE,
            OsrsSkill.RANGED,
            OsrsSkill.MAGIC,
                        OsrsSkill.HITPOINTS,
            OsrsSkill.SLAYER
        )
    ),
    EXPLORATION(
        "Exploration",
        "🧭",
        listOf(
            OsrsSkill.ADVENTURING,
            OsrsSkill.SAILING,
            OsrsSkill.THIEVING,
            OsrsSkill.AGILITY
        )
    )
}

enum class HerbloreSubViewMode {
    CATALOG,
    BOTANICAL_ALMANAC
}

enum class EquipmentSlotFilter(val label: String, val icon: String, val slot: EquipmentSlot?) {
    ALL("All Gear", "🛡️", null),
    HEAD("Head / Helm", "👑", EquipmentSlot.HEAD),
    TORSO("Torso / Body", "🥋", EquipmentSlot.BODY),
    LEGS("Legs / Greaves", "👖", EquipmentSlot.LEGS),
    FEET("Feet / Boots", "🥾", EquipmentSlot.BOOTS),
    WEAPON("Weapon", "⚔️", EquipmentSlot.WEAPON),
    SHIELD("Shield / Offhand", "🛡️", EquipmentSlot.SHIELD),
    NECKLACE("Necklace / Amulet", "📿", EquipmentSlot.AMULET),
    RING("Ring", "💍", EquipmentSlot.RING),
    CAPE("Cape", "🦸", EquipmentSlot.CAPE),
    GLOVES("Gloves / Hands", "🧤", EquipmentSlot.GLOVES),
    AMMO("Ammo / Quiver", "🏹", EquipmentSlot.AMMO)
}

enum class FoodTypeFilter(val label: String, val icon: String) {
    ALL("All Food", "🍲"),
    RAW("Raw Catch & Meat", "🥩"),
    COOKED("Cooked Meals", "🍖"),
    RECIPES("Cauldron & Stews", "🥧")
}

enum class MaterialTypeFilter(val label: String, val icon: String) {
    ALL("All Materials", "📦"),
    ORES("Ores & Minerals", "⛏️"),
    BARS("Refined Bars", "⚒️"),
    EQUIPMENT("Forged Gear", "🛡️"),
    LOGS("Logs & Timbers", "🪵"),
    PROJECTILES("Bows & Arrows", "🏹"),
    RUNES("Runes & Essences", "🔮"),
    POTIONS("Potions & Herbs", "🧪"),
    SEEDS("Seeds & Crops", "🌱")
}

enum class EncyclopediaFilter {
    ALL,
    OWNED,
    UNOWNED,
    EQUIPMENT,
    CONSUMABLE
}

enum class EncyclopediaSort(val label: String) {
    NAME_ASC("Name (A-Z)"),
    VALUE_DESC("Value (High to Low)"),
    OWNED_DESC("Owned (Most to Least)"),
    LEVEL_REQ("Level Req")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EncyclopediaTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val equippedItems by viewModel.equippedItems.collectAsStateWithLifecycle()
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()

    // Master list of all items in the game
    val masterItemList = remember(inventoryItems, bankItems, equippedItems) {
        EncyclopediaDatabase.getAllEncyclopediaItems(inventoryItems, bankItems, equippedItems)
    }

    // Ownership counts map
    val ownershipMap = remember(inventoryItems, bankItems, equippedItems, petState.unlockedOutfitIds) {
        val map = mutableMapOf<String, Triple<Int, Int, Boolean>>() // invCount, bankCount, isEquipped
        inventoryItems.forEach { item ->
            val prev = map[item.id] ?: Triple(0, 0, false)
            map[item.id] = Triple(prev.first + item.quantity, prev.second, prev.third)
        }
        bankItems.forEach { item ->
            val prev = map[item.id] ?: Triple(0, 0, false)
            map[item.id] = Triple(prev.first, prev.second + item.quantity, prev.third)
        }
        equippedItems.values.forEach { item ->
            val prev = map[item.id] ?: Triple(0, 0, false)
            map[item.id] = Triple(prev.first, prev.second, true)
        }
        petState.unlockedOutfitIds.forEach { outfitId ->
            val prev = map[outfitId] ?: Triple(0, 0, false)
            map[outfitId] = Triple(prev.first + 1, prev.second, true)
        }
        map
    }

    // Navigation & Skill Selection State
    var selectedGroupCategory by remember { mutableStateOf(SkillGroupCategory.ALL) }
    var selectedSkill by remember { mutableStateOf<OsrsSkill?>(null) } // null = All Skills / Master View
    var herbloreSubView by remember { mutableStateOf(HerbloreSubViewMode.CATALOG) }

    // Contextual Sub-filters
    var selectedGearSlot by remember { mutableStateOf(EquipmentSlotFilter.ALL) }
    var selectedFoodType by remember { mutableStateOf(FoodTypeFilter.ALL) }
    var selectedMaterialType by remember { mutableStateOf(MaterialTypeFilter.ALL) }
    var selectedCategory by remember { mutableStateOf(EncyclopediaCategory.ALL) }

    // Search, Sorting, and Ownership Filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(EncyclopediaFilter.ALL) }
    var selectedSort by remember { mutableStateOf(EncyclopediaSort.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Inspection Modal Item
    var inspectingItem by remember { mutableStateOf<EncyclopediaItem?>(null) }

    val focusManager = LocalFocusManager.current

    val outfitItemIds = remember {
        SkillOutfitData.ALL_PIECE_IDS
    }

    // Determine candidate skills for the chips bar based on selectedGroupCategory
    val visibleSkills: List<OsrsSkill> = remember(selectedGroupCategory) {
        selectedGroupCategory.skills ?: OsrsSkill.entries
    }

    // Filtered items computation
    val filteredItems = remember(
        masterItemList,
        selectedGroupCategory,
        selectedSkill,
        selectedCategory,
        selectedGearSlot,
        selectedFoodType,
        selectedMaterialType,
        searchQuery,
        selectedFilter,
        selectedSort,
        ownershipMap
    ) {
        masterItemList.asSequence().filter { item ->
            // 1. Skill Section / Skill Outfits Filter
            if (selectedGroupCategory == SkillGroupCategory.SKILL_OUTFITS) {
                if (!outfitItemIds.contains(item.id)) {
                    return@filter false
                }
                if (selectedSkill != null && item.effectiveSkill != selectedSkill && item.reqSkill != selectedSkill) {
                    return@filter false
                }
            } else if (selectedSkill != null) {
                if (item.effectiveSkill != selectedSkill) {
                    return@filter false
                }
            } else if (selectedCategory != EncyclopediaCategory.ALL) {
                if (item.category != selectedCategory) {
                    return@filter false
                }
            }

            // 2. Sub-filters depending on active skill
            when (selectedSkill) {
                OsrsSkill.ATTACK, OsrsSkill.DEFENCE, OsrsSkill.RANGED -> {
                    val targetSlot = selectedGearSlot.slot
                    if (targetSlot != null) {
                        val slotMatches = item.equipmentSlot == targetSlot
                        val id = item.id.lowercase()
                        val looseSlotMatch = when (targetSlot) {
                            EquipmentSlot.HEAD -> id.contains("helm") || id.contains("hat") || id.contains("hood") || id.contains("crown")
                            EquipmentSlot.BODY -> id.contains("platebody") || id.contains("body") || id.contains("top") || id.contains("robe") || id.contains("torso")
                            EquipmentSlot.LEGS -> id.contains("platelegs") || id.contains("legs") || id.contains("skirt") || id.contains("chaps")
                            EquipmentSlot.BOOTS -> id.contains("boots") || id.contains("shoes") || id.contains("greaves")
                            EquipmentSlot.WEAPON -> id.contains("sword") || id.contains("bow") || id.contains("staff") || id.contains("dagger") || id.contains("axe") || id.contains("whip") || id.contains("scimitar")
                            EquipmentSlot.SHIELD -> id.contains("shield") || id.contains("defender") || id.contains("buckler") || id.contains("kiteshield")
                            EquipmentSlot.AMULET -> id.contains("amulet") || id.contains("necklace") || id.contains("pendant")
                            EquipmentSlot.RING -> id.contains("ring") || id.contains("band")
                            EquipmentSlot.CAPE -> id.contains("cape") || id.contains("cloak")
                            EquipmentSlot.GLOVES -> id.contains("gloves") || id.contains("gauntlets") || id.contains("bracers")
                            EquipmentSlot.AMMO -> id.contains("arrow") || id.contains("bolt") || id.contains("dart") || id.contains("quiver")
                            else -> false
                        }
                        if (!slotMatches && !looseSlotMatch) return@filter false
                    }
                }
                OsrsSkill.COOKING, OsrsSkill.FISHING -> {
                    val id = item.id.lowercase()
                    val name = item.name.lowercase()
                    when (selectedFoodType) {
                        FoodTypeFilter.ALL -> true
                        FoodTypeFilter.RAW -> id.contains("raw") || name.contains("raw") || id.contains("grain") || id.contains("flour") || id.contains("egg")
                        FoodTypeFilter.COOKED -> (id.contains("cooked") || name.contains("cooked") || id.contains("meat") || id.contains("fish") || id.contains("bread") || id.contains("lobster") || id.contains("shark")) && !id.contains("raw")
                        FoodTypeFilter.RECIPES -> id.contains("stew") || id.contains("pie") || id.contains("cake") || id.contains("pizza") || id.contains("soup") || id.contains("curry") || id.contains("tonic") || id.contains("elixir") || id.contains("chowder") || id.contains("nectar")
                    }
                }
                OsrsSkill.SMITHING -> {
                    val id = item.id.lowercase()
                    when (selectedMaterialType) {
                        MaterialTypeFilter.ALL -> true
                        MaterialTypeFilter.ORES -> id.contains("ore") || id.contains("gem") || id.contains("uncut") || id.contains("coal")
                        MaterialTypeFilter.BARS -> id.contains("bar") && !id.contains("bark")
                        MaterialTypeFilter.EQUIPMENT -> item.equipmentSlot != null || item.combatPower > 0 || item.defPower > 0
                        else -> true
                    }
                }
                OsrsSkill.WOODCUTTING, OsrsSkill.FLETCHING -> {
                    val id = item.id.lowercase()
                    when (selectedMaterialType) {
                        MaterialTypeFilter.ALL -> true
                        MaterialTypeFilter.LOGS -> id.contains("log") || id.contains("wood") || id.contains("timber") || id.contains("bark")
                        MaterialTypeFilter.PROJECTILES -> id.contains("arrow") || id.contains("bow") || id.contains("shaft") || id.contains("bolt") || id.contains("dart") || id.contains("fletch")
                        else -> true
                    }
                }
                OsrsSkill.MAGIC, OsrsSkill.RUNECRAFT -> {
                    val id = item.id.lowercase()
                    when (selectedMaterialType) {
                        MaterialTypeFilter.ALL -> true
                        MaterialTypeFilter.RUNES -> id.contains("rune") || id.contains("essence") || id.contains("talisman") || id.contains("tiara")
                        MaterialTypeFilter.EQUIPMENT -> item.equipmentSlot != null || id.contains("staff") || id.contains("wand") || id.contains("robe")
                        else -> true
                    }
                }
                OsrsSkill.FARMING -> {
                    val id = item.id.lowercase()
                    when (selectedMaterialType) {
                        MaterialTypeFilter.ALL -> true
                        MaterialTypeFilter.SEEDS -> id.contains("seed") || id.contains("sapling")
                        MaterialTypeFilter.POTIONS -> id.contains("herb") || id.contains("clean_") || id.contains("grimy")
                        else -> true
                    }
                }
                else -> { /* no additional specialized sub-filtering needed */ }
            }

            // 3. Search query filter
            if (searchQuery.isNotBlank()) {
                val query = searchQuery.trim().lowercase()
                val matchesName = item.name.lowercase().contains(query)
                val matchesDesc = item.description.lowercase().contains(query)
                val matchesObtain = item.allObtainMethods.any { it.lowercase().contains(query) }
                val matchesId = item.id.lowercase().contains(query)
                val matchesSkill = item.effectiveSkill.displayName.lowercase().contains(query) || item.effectiveSkill.name.lowercase().contains(query)
                if (!matchesName && !matchesDesc && !matchesObtain && !matchesId && !matchesSkill) {
                    return@filter false
                }
            }

            // 4. Quick Ownership / Category filter
            val counts = ownershipMap[item.id] ?: Triple(0, 0, false)
            val totalOwned = counts.first + counts.second + (if (counts.third) 1 else 0)
            when (selectedFilter) {
                EncyclopediaFilter.ALL -> true
                EncyclopediaFilter.OWNED -> totalOwned > 0
                EncyclopediaFilter.UNOWNED -> totalOwned == 0
                EncyclopediaFilter.EQUIPMENT -> item.equipmentSlot != null || item.category == EncyclopediaCategory.EQUIPMENT || item.combatPower > 0 || item.defPower > 0
                EncyclopediaFilter.CONSUMABLE -> item.healHp > 0 || item.restoreHunger > 0 || item.category == EncyclopediaCategory.FOOD || item.category == EncyclopediaCategory.HERBLORE
            }
        }.sortedWith(
            when (selectedSort) {
                EncyclopediaSort.NAME_ASC -> compareBy { it.name.lowercase() }
                EncyclopediaSort.VALUE_DESC -> compareByDescending { it.costGp }
                EncyclopediaSort.OWNED_DESC -> compareByDescending {
                    val c = ownershipMap[it.id] ?: Triple(0, 0, false)
                    c.first + c.second + (if (c.third) 1 else 0)
                }
                EncyclopediaSort.LEVEL_REQ -> compareBy { it.reqLevel }
            }
        ).toList()
    }

    // Overall & Skill Progress Statistics
    val totalDatabaseCount = masterItemList.size
    val totalDiscoveredCount = remember(masterItemList, ownershipMap) {
        masterItemList.count { item ->
            val c = ownershipMap[item.id] ?: Triple(0, 0, false)
            (c.first + c.second + if (c.third) 1 else 0) > 0
        }
    }
    val overallCompletionPercent = if (totalDatabaseCount > 0) (totalDiscoveredCount * 100 / totalDatabaseCount) else 0

    // Selected skill statistics
    val selectedSkillItems = remember(masterItemList, selectedSkill) {
        if (selectedSkill == null) masterItemList
        else masterItemList.filter { it.effectiveSkill == selectedSkill }
    }
    val selectedSkillDiscoveredCount = remember(selectedSkillItems, ownershipMap) {
        selectedSkillItems.count { item ->
            val c = ownershipMap[item.id] ?: Triple(0, 0, false)
            (c.first + c.second + if (c.third) 1 else 0) > 0
        }
    }
    val selectedSkillCompletionPercent = if (selectedSkillItems.isNotEmpty()) {
        (selectedSkillDiscoveredCount * 100 / selectedSkillItems.size)
    } else 0

    // Skill player level & XP
    val currentSkillXp = selectedSkill?.let { skillXpMap[it] ?: 0L } ?: 0L
    val currentSkillLevel = remember(currentSkillXp) {
        OsrsXpCalculator.getLevelForXp(currentSkillXp)
    }
    val nextLevelXp = remember(currentSkillLevel) {
        if (currentSkillLevel >= 99) OsrsXpCalculator.getXpForLevel(99)
        else OsrsXpCalculator.getXpForLevel(currentSkillLevel + 1)
    }
    val currentLevelBaseXp = remember(currentSkillLevel) {
        OsrsXpCalculator.getXpForLevel(currentSkillLevel)
    }
    val skillXpProgress = remember(currentSkillXp, currentLevelBaseXp, nextLevelXp, currentSkillLevel) {
        if (currentSkillLevel >= 99) 1f
        else {
            val range = (nextLevelXp - currentLevelBaseXp).coerceAtLeast(1L)
            val current = (currentSkillXp - currentLevelBaseXp).coerceAtLeast(0L)
            (current.toFloat() / range.toFloat()).coerceIn(0f, 1f)
        }
    }

    var showHeaderProgressExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("encyclopedia_tab_root"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // === COMPACT MASTER HEADER BAR ===
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = OsrsLeatherMedium,
            border = BorderStroke(1.dp, OsrsGoldBright.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
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
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showHeaderProgressExpanded = !showHeaderProgressExpanded }
                    ) {
                        Text(
                            text = if (selectedSkill != null) selectedSkill!!.iconSymbol else "📖",
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (selectedSkill != null) "${selectedSkill!!.displayName} Almanac" else "Item Encyclopedia",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OsrsGoldBright
                        )
                        if (selectedSkill != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = selectedSkill!!.accentColor.copy(alpha = 0.3f),
                                border = BorderStroke(0.5.dp, selectedSkill!!.accentColor)
                            ) {
                                Text(
                                    text = "Lv.$currentSkillLevel",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OsrsTextYellow,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Progress Pill (Clickable)
                    Surface(
                        onClick = { showHeaderProgressExpanded = !showHeaderProgressExpanded },
                        color = OsrsGoldBright.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, OsrsGoldBright.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (selectedSkill != null) "$selectedSkillDiscoveredCount/${selectedSkillItems.size} ($selectedSkillCompletionPercent%)" else "$totalDiscoveredCount/$totalDatabaseCount ($overallCompletionPercent%)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OsrsTextYellow,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { if (selectedSkill != null) skillXpProgress else (overallCompletionPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = selectedSkill?.accentColor ?: OsrsGoldBright,
                    trackColor = Color(0xFF1B3324)
                )

                if (showHeaderProgressExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedGroupCategory == SkillGroupCategory.SKILL_OUTFITS) {
                                if (selectedSkill != null) "${selectedSkill!!.displayName} Skilling Set Outfits (${filteredItems.size} pieces)" else "Displaying all Adventuring Skilling Outfits (${filteredItems.size} prizes)"
                            } else if (selectedSkill != null) {
                                "${selectedSkill!!.description} (${currentSkillXp.toInt()} XP)"
                            } else {
                                "Cataloguing ${totalDatabaseCount} Runescape items across all skills"
                            },
                            fontSize = 8.5.sp,
                            color = OsrsParchment.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // === SKILL GROUP CATEGORY CHIPS ===
        val groupScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(groupScrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SkillGroupCategory.entries.forEach { group ->
                val isSelected = selectedGroupCategory == group
                Surface(
                    onClick = {
                        selectedGroupCategory = group
                        val groupSkills = group.skills
                        if (groupSkills != null && selectedSkill != null && !groupSkills.contains(selectedSkill)) {
                            selectedSkill = groupSkills.firstOrNull()
                        }
                    },
                    shape = RoundedCornerShape(5.dp),
                    color = if (isSelected) OsrsGoldBright else OsrsLeatherMedium,
                    border = BorderStroke(1.dp, if (isSelected) OsrsGoldBright else OsrsRedFrame.copy(alpha = 0.7f)),
                    modifier = Modifier.height(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(group.icon, fontSize = 9.5.sp)
                        Text(
                            text = group.label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OsrsTextDark else OsrsParchment
                        )
                    }
                }
            }
        }

        // === INDIVIDUAL SKILL SELECTOR CHIPS ===
        val skillScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(skillScrollState),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // "All Items" master chip
            val isAllSelected = selectedSkill == null
            val allCount = if (selectedGroupCategory == SkillGroupCategory.SKILL_OUTFITS) outfitItemIds.size else totalDatabaseCount
            Surface(
                onClick = { selectedSkill = null },
                shape = RoundedCornerShape(5.dp),
                color = if (isAllSelected) Color(0xFF2B4C33) else OsrsLeatherMedium,
                border = BorderStroke(1.dp, if (isAllSelected) OsrsGoldBright else OsrsRedFrame.copy(alpha = 0.6f)),
                modifier = Modifier
                    .height(26.dp)
                    .testTag("encyclopedia_skill_all")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(if (selectedGroupCategory == SkillGroupCategory.SKILL_OUTFITS) "🥋" else "📚", fontSize = 10.sp)
                    Text(
                        text = "All ($allCount)",
                        fontSize = 9.5.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAllSelected) OsrsGoldBright else OsrsParchment
                    )
                }
            }

            // Each Skill Chip
            visibleSkills.forEach { skill ->
                val isSelected = selectedSkill == skill
                val skillItemsCount = remember(masterItemList, skill, selectedGroupCategory) {
                    if (selectedGroupCategory == SkillGroupCategory.SKILL_OUTFITS) {
                        masterItemList.count { outfitItemIds.contains(it.id) && (it.effectiveSkill == skill || it.reqSkill == skill) }
                    } else {
                        masterItemList.count { it.effectiveSkill == skill }
                    }
                }
                val skillDiscovered = remember(masterItemList, skill, ownershipMap, selectedGroupCategory) {
                    masterItemList.count { item ->
                        if (selectedGroupCategory == SkillGroupCategory.SKILL_OUTFITS) {
                            if (!outfitItemIds.contains(item.id) || (item.effectiveSkill != skill && item.reqSkill != skill)) return@count false
                        } else {
                            if (item.effectiveSkill != skill) return@count false
                        }
                        val c = ownershipMap[item.id] ?: Triple(0, 0, false)
                        (c.first + c.second + if (c.third) 1 else 0) > 0
                    }
                }
                val skillLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[skill] ?: 0L)

                Surface(
                    onClick = {
                        selectedSkill = skill
                        if (skill == OsrsSkill.HERBLORE) {
                            herbloreSubView = HerbloreSubViewMode.CATALOG
                        }
                    },
                    shape = RoundedCornerShape(5.dp),
                    color = if (isSelected) Color(0xFF38291B) else OsrsLeatherMedium,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) skill.accentColor else OsrsRedFrame.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .height(26.dp)
                        .testTag("encyclopedia_skill_${skill.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(skill.iconSymbol, fontSize = 10.sp)
                        Text(
                            text = "${skill.displayName} ($skillDiscovered/$skillItemsCount)",
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OsrsGoldBright else OsrsParchment
                        )
                    }
                }
            }
        }

        // Special Botanical Almanac / Catalog toggle for Herblore
        if (selectedSkill == OsrsSkill.HERBLORE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = { herbloreSubView = HerbloreSubViewMode.CATALOG },
                    shape = RoundedCornerShape(4.dp),
                    color = if (herbloreSubView == HerbloreSubViewMode.CATALOG) OsrsGoldBright else OsrsLeatherMedium,
                    border = BorderStroke(0.5.dp, if (herbloreSubView == HerbloreSubViewMode.CATALOG) OsrsGoldBright else OsrsRedFrame),
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧪", fontSize = 9.sp, modifier = Modifier.padding(end = 2.dp))
                        Text(
                            text = "Catalog",
                            fontSize = 9.sp,
                            fontWeight = if (herbloreSubView == HerbloreSubViewMode.CATALOG) FontWeight.Bold else FontWeight.Normal,
                            color = if (herbloreSubView == HerbloreSubViewMode.CATALOG) OsrsTextDark else OsrsParchment
                        )
                    }
                }

                Surface(
                    onClick = { herbloreSubView = HerbloreSubViewMode.BOTANICAL_ALMANAC },
                    shape = RoundedCornerShape(4.dp),
                    color = if (herbloreSubView == HerbloreSubViewMode.BOTANICAL_ALMANAC) OsrsGoldBright else OsrsLeatherMedium,
                    border = BorderStroke(0.5.dp, if (herbloreSubView == HerbloreSubViewMode.BOTANICAL_ALMANAC) OsrsGoldBright else OsrsRedFrame),
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌱", fontSize = 9.sp, modifier = Modifier.padding(end = 2.dp))
                        Text(
                            text = "Botanical Chain",
                            fontSize = 9.sp,
                            fontWeight = if (herbloreSubView == HerbloreSubViewMode.BOTANICAL_ALMANAC) FontWeight.Bold else FontWeight.Normal,
                            color = if (herbloreSubView == HerbloreSubViewMode.BOTANICAL_ALMANAC) OsrsTextDark else OsrsParchment
                        )
                    }
                }
            }
        }

        // === CONTEXTUAL SUB-FILTERS (Based on active skill or master mode) ===
        if (selectedSkill == null) {
            // Master Category Sub-Tabs (Horizontal Scroll)
            val subTabScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(subTabScrollState)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EncyclopediaCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val count = remember(masterItemList, cat) {
                        if (cat == EncyclopediaCategory.ALL) masterItemList.size
                        else masterItemList.count { it.category == cat }
                    }

                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF234B34) else OsrsLeatherMedium,
                        border = BorderStroke(1.dp, if (isSelected) OsrsGoldBright else OsrsRedFrame.copy(alpha = 0.6f)),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(cat.iconEmoji, fontSize = 10.sp)
                            Text(
                                text = cat.displayName,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) OsrsGoldBright else OsrsParchment
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) OsrsGoldBright else Color(0xFF1B281B)
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OsrsTextDark else OsrsTextYellow,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Skill-specific contextual filters
            when (selectedSkill) {
                OsrsSkill.ATTACK, OsrsSkill.DEFENCE, OsrsSkill.RANGED -> {
                    val gearScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(gearScrollState)
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EquipmentSlotFilter.entries.forEach { slotFilter ->
                            val isSelected = selectedGearSlot == slotFilter
                            Surface(
                                onClick = { selectedGearSlot = slotFilter },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFF382E1E) else OsrsLeatherMedium,
                                border = BorderStroke(1.dp, if (isSelected) OsrsGoldBright else OsrsRedFrame.copy(alpha = 0.6f)),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(slotFilter.icon, fontSize = 10.sp)
                                    Text(
                                        text = slotFilter.label,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) OsrsGoldBright else OsrsParchment
                                    )
                                }
                            }
                        }
                    }
                }
                OsrsSkill.COOKING, OsrsSkill.FISHING -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FoodTypeFilter.entries.forEach { foodFilter ->
                            val isSelected = selectedFoodType == foodFilter
                            Surface(
                                onClick = { selectedFoodType = foodFilter },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFF422818) else OsrsLeatherMedium,
                                border = BorderStroke(1.dp, if (isSelected) OsrsGoldBright else OsrsRedFrame.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(foodFilter.icon, fontSize = 10.sp, modifier = Modifier.padding(end = 2.dp))
                                    Text(
                                        text = foodFilter.label,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) OsrsGoldBright else OsrsParchment,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
                else -> { /* no additional sub-filters */ }
            }
        }

        // === SEARCH BAR & SORT ROW ===
        if (selectedSkill != OsrsSkill.HERBLORE || herbloreSubView == HerbloreSubViewMode.CATALOG) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("encyclopedia_search_input"),
                    placeholder = {
                        Text(
                            "Search name, obtain, monster...",
                            fontSize = 10.sp,
                            color = OsrsParchment.copy(alpha = 0.5f),
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = OsrsGoldBright,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = OsrsParchment,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OsrsLeatherMedium,
                        unfocusedContainerColor = OsrsLeatherMedium,
                        focusedBorderColor = OsrsGoldBright,
                        unfocusedBorderColor = OsrsRedFrame,
                        focusedTextColor = OsrsTextWhite,
                        unfocusedTextColor = OsrsParchment
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = OsrsTextWhite),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                // Sort Dropdown Button
                Box {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("encyclopedia_sort_button"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = OsrsLeatherMedium,
                            contentColor = OsrsTextYellow
                        ),
                        border = BorderStroke(1.dp, OsrsRedFrame),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Sort",
                            tint = OsrsTextYellow,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = selectedSort.label,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(OsrsLeatherMedium)
                    ) {
                        EncyclopediaSort.entries.forEach { sortOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sortOption.label,
                                        fontSize = 10.5.sp,
                                        color = if (selectedSort == sortOption) OsrsGoldBright else OsrsParchment
                                    )
                                },
                                onClick = {
                                    selectedSort = sortOption
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Quick Filter Chips (All, Owned, Unowned, Gear, Food/Potions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EncyclopediaFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val label = when (filter) {
                        EncyclopediaFilter.ALL -> "All"
                        EncyclopediaFilter.OWNED -> "✅ Owned"
                        EncyclopediaFilter.UNOWNED -> "❌ Unowned"
                        EncyclopediaFilter.EQUIPMENT -> "🛡️ Gear"
                        EncyclopediaFilter.CONSUMABLE -> "🍲 Consumables"
                    }

                    Surface(
                        onClick = { selectedFilter = filter },
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) Color(0xFF1E5236) else OsrsLeatherMedium.copy(alpha = 0.7f),
                        border = BorderStroke(
                            0.5.dp,
                            if (isSelected) OsrsGoldBright else OsrsRedFrame.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(22.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) OsrsGoldBright else OsrsParchment.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // === CONTENT RENDERER ===
        if (selectedSkill == OsrsSkill.HERBLORE && herbloreSubView == HerbloreSubViewMode.BOTANICAL_ALMANAC) {
            // Botanical Seed-to-Potion Lifecycle Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("encyclopedia_botanical_list"),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(HerbloreData.BOTANICAL_CHAINS, key = { it.seedId }) { chain ->
                    BotanicalChainCard(
                        chain = chain,
                        ownershipMap = ownershipMap,
                        onInspectItem = { itemId ->
                            val encItem = EncyclopediaDatabase.getEncyclopediaItem(
                                itemId = itemId,
                                inventoryItems = inventoryItems,
                                bankItems = bankItems,
                                equippedItems = equippedItems
                            )
                            inspectingItem = encItem
                        }
                    )
                }
            }
        } else {
            // Standard Skill Item Grid / Cards
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No items matched your query.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OsrsParchment
                        )
                        Text(
                            text = "Try clearing search terms or selecting a different skill.",
                            fontSize = 10.sp,
                            color = OsrsParchment.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("encyclopedia_items_list"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val ownership = ownershipMap[item.id] ?: Triple(0, 0, false)
                        val totalOwned = ownership.first + ownership.second + (if (ownership.third) 1 else 0)

                        EncyclopediaItemCard(
                            item = item,
                            invCount = ownership.first,
                            bankCount = ownership.second,
                            isEquipped = ownership.third,
                            totalOwned = totalOwned,
                            onClick = { inspectingItem = item }
                        )
                    }
                }
            }
        }
    }

    // === ITEM DETAIL INSPECTION DIALOG ===
    inspectingItem?.let { item ->
        val ownership = ownershipMap[item.id] ?: Triple(0, 0, false)
        EncyclopediaItemDetailDialog(
            item = item,
            invCount = ownership.first,
            bankCount = ownership.second,
            isEquipped = ownership.third,
            onDismiss = { inspectingItem = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BotanicalChainCard(
    chain: HerbBotanicalChain,
    ownershipMap: Map<String, Triple<Int, Int, Boolean>>,
    onInspectItem: (String) -> Unit
) {
    val seedCounts = ownershipMap[chain.seedId] ?: Triple(0, 0, false)
    val totalSeeds = seedCounts.first + seedCounts.second

    val herbCounts = ownershipMap[chain.herbId] ?: Triple(0, 0, false)
    val totalHerbs = herbCounts.first + herbCounts.second

    val crushedCounts = ownershipMap[chain.crushedHerbId] ?: Triple(0, 0, false)
    val totalCrushed = crushedCounts.first + crushedCounts.second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("botanical_card_${chain.seedId}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF142416)),
        border = BorderStroke(1.dp, Color(0xFF2E5B32))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(chain.herbEmoji, fontSize = 18.sp)
                    Column {
                        Text(
                            text = "${chain.herbName} Botanical Chain",
                            color = Color(0xFFA5D6A7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Farming Req: Lv. ${chain.reqFarmingLevel} • Herblore Req: Lv. ${chain.reqHerbloreLevel}",
                            color = OsrsParchment,
                            fontSize = 9.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E3822),
                    border = BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Text(
                        text = "Tap item for obtain lore",
                        color = OsrsGold,
                        fontSize = 8.5.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Lifecycle Visual Pipeline: Seed -> Clean Herb -> Crushed Herb
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Herb Seed
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F1C11),
                    border = BorderStroke(1.dp, if (totalSeeds > 0) Color(0xFF81C784) else Color(0xFF283B2A)),
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = { onInspectItem(chain.seedId) },
                            onLongClick = { onInspectItem(chain.seedId) }
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(chain.seedEmoji, fontSize = 16.sp)
                        Text(chain.seedName, color = OsrsParchment, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(if (totalSeeds > 0) "Owned: $totalSeeds" else "Unowned", color = if (totalSeeds > 0) Color(0xFFA5D6A7) else Color(0xFFE57373), fontSize = 8.sp)
                    }
                }

                Text("➔", color = OsrsGold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 2.dp))

                // 2. Fresh Normal Herb
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F1C11),
                    border = BorderStroke(1.dp, if (totalHerbs > 0) Color(0xFF81C784) else Color(0xFF283B2A)),
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = { onInspectItem(chain.herbId) },
                            onLongClick = { onInspectItem(chain.herbId) }
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(chain.herbEmoji, fontSize = 16.sp)
                        Text(chain.herbName, color = OsrsParchment, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(if (totalHerbs > 0) "Owned: $totalHerbs" else "Unowned", color = if (totalHerbs > 0) Color(0xFFA5D6A7) else Color(0xFFE57373), fontSize = 8.sp)
                    }
                }

                Text("➔", color = OsrsGold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 2.dp))

                // 3. Crushed Herb (Pestle & Mortar)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F1C11),
                    border = BorderStroke(1.dp, if (totalCrushed > 0) Color(0xFF81C784) else Color(0xFF283B2A)),
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = { onInspectItem(chain.crushedHerbId) },
                            onLongClick = { onInspectItem(chain.crushedHerbId) }
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🥣", fontSize = 16.sp)
                        Text(chain.crushedHerbName, color = OsrsParchment, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(if (totalCrushed > 0) "Owned: $totalCrushed" else "Unowned", color = if (totalCrushed > 0) Color(0xFFA5D6A7) else Color(0xFFE57373), fontSize = 8.sp)
                    }
                }
            }

            // Resulting Potions made from this herb
            if (chain.potionRecipes.isNotEmpty()) {
                Text(
                    text = "Brews into ${chain.potionRecipes.size} Potions:",
                    color = Color(0xFFA5D6A7),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                chain.potionRecipes.forEach { pot ->
                    val potCounts = ownershipMap[pot.outputPotionId] ?: Triple(0, 0, false)
                    val totalPots = potCounts.first + potCounts.second

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0D170E),
                        border = BorderStroke(0.5.dp, Color(0xFF254228)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onInspectItem(pot.outputPotionId) },
                                onLongClick = { onInspectItem(pot.outputPotionId) }
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(pot.iconEmoji, fontSize = 13.sp)
                                Column {
                                    Text(
                                        text = "${pot.name} (Req. Herblore ${pot.reqLevel})",
                                        color = Color(0xFFA5D6A7),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Recipe: 1x ${chain.crushedHerbName} + 1x ${pot.secondaryItemName}",
                                        color = OsrsParchment,
                                        fontSize = 8.5.sp
                                    )
                                }
                            }

                            Text(
                                text = if (totalPots > 0) "Owned: $totalPots" else "Unowned",
                                color = if (totalPots > 0) OsrsGold else Color(0xFFE57373),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EncyclopediaItemCard(
    item: EncyclopediaItem,
    invCount: Int,
    bankCount: Int,
    isEquipped: Boolean,
    totalOwned: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onClick
            )
            .testTag("encyclopedia_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (totalOwned > 0) OsrsGoldBright.copy(alpha = 0.35f) else OsrsRedFrame.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Icon in Styled Slot
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0C1811))
                    .border(
                        1.dp,
                        if (totalOwned > 0) OsrsGoldBright.copy(alpha = 0.7f) else Color(0xFF1E3827),
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.iconEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalOwned > 0) OsrsGoldBright else OsrsParchment,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Skill Badge
                    Surface(
                        color = item.effectiveSkill.accentColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, item.effectiveSkill.accentColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(item.effectiveSkill.iconSymbol, fontSize = 8.sp)
                            Text(
                                text = item.effectiveSkill.displayName,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = OsrsGoldBright
                            )
                        }
                    }
                }

                // Short Description
                Text(
                    text = item.description,
                    fontSize = 10.sp,
                    color = OsrsParchment.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Obtainment Highlight
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Obtain: ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = OsrsTextOrange
                    )
                    Text(
                        text = item.primaryObtainMethod,
                        fontSize = 9.sp,
                        color = OsrsTextYellow.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column (Ownership status & stats)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (totalOwned > 0) {
                    Surface(
                        color = Color(0xFF00FF9D).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, OsrsGoldBright.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "x$totalOwned Owned",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OsrsGoldBright,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFF261818),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF5C2626))
                    ) {
                        Text(
                            text = "Unobtained",
                            fontSize = 9.sp,
                            color = Color(0xFFE57373),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (item.costGp > 0) {
                    Text(
                        text = "${item.costGp} GP",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OsrsTextYellow
                    )
                } else if (item.combatPower > 0 || item.defPower > 0) {
                    Text(
                        text = "+${item.combatPower} Atk / +${item.defPower} Def",
                        fontSize = 8.sp,
                        color = OsrsParchmentLight
                    )
                }
            }
        }
    }
}

@Composable
fun EncyclopediaItemDetailDialog(
    item: EncyclopediaItem,
    invCount: Int,
    bankCount: Int,
    isEquipped: Boolean,
    onDismiss: () -> Unit
) {
    val totalOwned = invCount + bankCount + (if (isEquipped) 1 else 0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("encyclopedia_detail_dialog"),
            colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, OsrsGoldBright)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with Large Icon, Name, and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF09120D))
                            .border(1.5.dp, OsrsGoldBright, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.iconEmoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OsrsGoldBright
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = item.effectiveSkill.accentColor.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, item.effectiveSkill.accentColor.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(item.effectiveSkill.iconSymbol, fontSize = 9.sp)
                                    Text(
                                        text = item.effectiveSkill.displayName,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OsrsGoldBright
                                    )
                                }
                            }
                            if (item.costGp > 0) {
                                Text(
                                    text = "🪙 ${item.costGp} GP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OsrsTextYellow
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = OsrsParchment
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = OsrsRedFrame, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Description Box
                Text(
                    text = "Description & Lore",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OsrsTextOrange
                )
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = OsrsParchment,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                // Stats Section (if applicable)
                if (item.combatPower > 0 || item.defPower > 0 || item.healHp > 0 || item.restoreHunger > 0 || item.highAlchGp > 0) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0D1C13),
                        border = BorderStroke(0.5.dp, OsrsRedFrame)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            if (item.combatPower > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚔️ Combat", fontSize = 9.sp, color = OsrsParchment.copy(alpha = 0.7f))
                                    Text("+${item.combatPower}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OsrsGoldBright)
                                }
                            }
                            if (item.defPower > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🛡️ Defence", fontSize = 9.sp, color = OsrsParchment.copy(alpha = 0.7f))
                                    Text("+${item.defPower}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81D4FA))
                                }
                            }
                            if (item.healHp > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("❤️ Heals HP", fontSize = 9.sp, color = OsrsParchment.copy(alpha = 0.7f))
                                    Text("+${item.healHp}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
                                }
                            }
                            if (item.restoreHunger > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🍖 Hunger", fontSize = 9.sp, color = OsrsParchment.copy(alpha = 0.7f))
                                    Text("+${item.restoreHunger}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OsrsTextOrange)
                                }
                            }
                            if (item.highAlchGp > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✨ High Alch", fontSize = 9.sp, color = OsrsParchment.copy(alpha = 0.7f))
                                    Text("${item.highAlchGp} GP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OsrsTextYellow)
                                }
                            }
                        }
                    }
                }

                // === HOW TO OBTAIN GUIDE (Crucial Section) ===
                Text(
                    text = "📍 Ways to Obtain",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OsrsGoldBright
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F2218),
                    border = BorderStroke(1.dp, OsrsGoldBright.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item.allObtainMethods.forEach { method ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OsrsGoldBright
                                )
                                Text(
                                    text = method,
                                    fontSize = 10.sp,
                                    color = OsrsTextWhite,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // === PLAYER OWNERSHIP STATUS ===
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0B1710),
                    border = BorderStroke(0.5.dp, OsrsRedFrame)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Your Inventory Status",
                                fontSize = 9.sp,
                                color = OsrsParchment.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (totalOwned > 0) "Total Owned: $totalOwned items" else "Not in your possession",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalOwned > 0) OsrsGoldBright else Color(0xFFE57373)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🎒 Inv: $invCount", fontSize = 9.sp, color = OsrsParchment)
                            Text("🏦 Bank: $bankCount", fontSize = 9.sp, color = OsrsParchment)
                            if (isEquipped) {
                                Text("🛡️ Equipped", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OsrsTextYellow)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OsrsGoldBright)
                ) {
                    Text(
                        text = "Close Almanac",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OsrsTextDark
                    )
                }
            }
        }
    }
}
