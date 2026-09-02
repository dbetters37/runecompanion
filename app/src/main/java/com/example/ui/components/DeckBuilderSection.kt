package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

enum class DeckBuilderTab {
    MY_DECK,
    DECKS_LIST,
    CARD_LIBRARY
}

enum class CardSortOption(val displayName: String) {
    REQ_LEVEL("Req Level"),
    ENERGY_LOW("Cost: Low → High"),
    ENERGY_HIGH("Cost: High → Low"),
    DAMAGE("Damage: High"),
    SHIELD("Shield: High"),
    NAME("Name: A-Z")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckBuilderSection(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val customDeckCardIds by viewModel.customDeckCardIds.collectAsState()
    val savedDeckLoadouts by viewModel.savedDeckLoadouts.collectAsState()
    val activeDeckLoadoutId by viewModel.activeDeckLoadoutId.collectAsState()
    val adventuringCombatStance by viewModel.adventuringCombatStance.collectAsState()
    val skillXpMap by viewModel.skillXpMap.collectAsState()

    val allCards = remember { DefaultCombatCards.ALL_CARDS }
    val haptic = LocalHapticFeedback.current

    var activeTab by remember { mutableStateOf(DeckBuilderTab.MY_DECK) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSkillFilter by remember { mutableStateOf<OsrsSkill?>(null) }
    var selectedStanceFilter by remember { mutableStateOf<String>("ALL") }
    var selectedEnergyFilter by remember { mutableStateOf<Int?>(null) }
    var selectedEffectFilter by remember { mutableStateOf<String>("ALL") }
    var showOnlyUnlocked by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(CardSortOption.REQ_LEVEL) }

    var showSaveDeckDialog by remember { mutableStateOf(false) }
    var showPresetsDialog by remember { mutableStateOf(false) }
    var selectedDeckForOptions by remember { mutableStateOf<SavedDeckLoadout?>(null) }
    var deckToRename by remember { mutableStateOf<SavedDeckLoadout?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<SavedDeckLoadout?>(null) }
    var enlargedCard by remember { mutableStateOf<CombatCard?>(null) }
    var showAnalyticsExpanded by remember { mutableStateOf(false) }

    // Active deck card objects
    val activeDeckCards = remember(customDeckCardIds, allCards, adventuringCombatStance) {
        if (customDeckCardIds.isEmpty()) {
            DefaultCombatCards.getDefaultDeckForStance(adventuringCombatStance, skillXpMap)
        } else {
            allCards.filter { customDeckCardIds.contains(it.id) }
        }
    }

    // Active loadout reference
    val currentLoadout = remember(savedDeckLoadouts, activeDeckLoadoutId) {
        savedDeckLoadouts.find { it.id == activeDeckLoadoutId }
            ?: ArchetypeDeckPresets.PRESETS.find { it.id == activeDeckLoadoutId }
    }

    // Deck Analytics Calculations
    val deckSize = activeDeckCards.size
    val totalDamage = activeDeckCards.sumOf { it.baseDamage }
    val totalShield = activeDeckCards.sumOf { it.baseShield }
    val totalHeal = activeDeckCards.sumOf { it.baseHeal }
    val avgEnergyCost = if (deckSize > 0) activeDeckCards.map { it.energyCost }.average() else 0.0
    val count0Cost = activeDeckCards.count { it.energyCost == 0 }
    val count1Cost = activeDeckCards.count { it.energyCost == 1 }
    val count2Cost = activeDeckCards.count { it.energyCost == 2 }
    val count3PlusCost = activeDeckCards.count { it.energyCost >= 3 }

    // Collapsible Top Controls state to give maximum screen room to card lists
    var showTopHeaderExpanded by remember { mutableStateOf(false) }

    // Filtered Cards for Library
    val filteredLibraryCards = remember(
        allCards, searchQuery, selectedSkillFilter, selectedStanceFilter,
        selectedEnergyFilter, selectedEffectFilter, showOnlyUnlocked, sortOption, skillXpMap
    ) {
        val list = allCards.filter { card ->
            val matchesSearch = if (searchQuery.isNotBlank()) {
                card.title.contains(searchQuery, ignoreCase = true) ||
                card.description.contains(searchQuery, ignoreCase = true) ||
                card.skill.displayName.contains(searchQuery, ignoreCase = true) ||
                card.skill.name.contains(searchQuery, ignoreCase = true)
            } else true

            val matchesSkill = if (selectedSkillFilter != null) {
                card.skill == selectedSkillFilter
            } else true

            val matchesStance = when (selectedStanceFilter) {
                "ALL" -> true
                else -> card.stance == selectedStanceFilter || card.stance == "ALL"
            }

            val matchesEnergy = if (selectedEnergyFilter != null) {
                if (selectedEnergyFilter == 3) card.energyCost >= 3 else card.energyCost == selectedEnergyFilter
            } else true

            val matchesEffect = when (selectedEffectFilter) {
                "DAMAGE" -> card.baseDamage > 0
                "SHIELD" -> card.baseShield > 0
                "HEAL" -> card.baseHeal > 0
                "BUFF" -> card.nextAttackBuff > 0
                else -> true
            }

            val playerLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[card.skill] ?: 0L)
            val isUnlocked = playerLvl >= card.reqLevel
            val matchesUnlocked = if (showOnlyUnlocked) isUnlocked else true

            matchesSearch && matchesSkill && matchesStance && matchesEnergy && matchesEffect && matchesUnlocked
        }

        when (sortOption) {
            CardSortOption.REQ_LEVEL -> list.sortedWith(compareBy<CombatCard> { it.reqLevel }.thenBy { it.energyCost })
            CardSortOption.ENERGY_LOW -> list.sortedWith(compareBy<CombatCard> { it.energyCost }.thenBy { it.reqLevel })
            CardSortOption.ENERGY_HIGH -> list.sortedWith(compareByDescending<CombatCard> { it.energyCost }.thenBy { it.reqLevel })
            CardSortOption.DAMAGE -> list.sortedByDescending { it.baseDamage }
            CardSortOption.SHIELD -> list.sortedByDescending { it.baseShield }
            CardSortOption.NAME -> list.sortedBy { it.title }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1722))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // --- 1. COMPACT / COLLAPSIBLE TOP PROFILE & ANALYTICS BAR ---
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = OsrsDarkPanel,
            border = BorderStroke(1.dp, Color(0xFF37474F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Compact Single-Line Active Deck Bar with quick actions and expand toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .weight(1f)
                            .combinedClickable(
                                onClick = { showTopHeaderExpanded = !showTopHeaderExpanded },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (currentLoadout != null) {
                                        selectedDeckForOptions = currentLoadout
                                    }
                                }
                            )
                    ) {
                        Text(currentLoadout?.iconEmoji ?: "🎴", fontSize = 16.sp)
                        Text(
                            currentLoadout?.name ?: "Custom Battle Deck",
                            color = OsrsGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Deck count badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when {
                                deckSize < 5 -> Color(0xFFC62828)
                                deckSize in 8..15 -> Color(0xFF2E7D32)
                                else -> Color(0xFFE65100)
                            }
                        ) {
                            Text(
                                "$deckSize Cards",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPresetsDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OsrsTextYellow),
                            border = BorderStroke(1.dp, OsrsGold.copy(alpha = 0.6f)),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("✨ Presets", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showSaveDeckDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("💾 Save", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        IconButton(
                            onClick = { showTopHeaderExpanded = !showTopHeaderExpanded },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                if (showTopHeaderExpanded) "▲" else "⚙️",
                                color = OsrsGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Compact Quick Stats Line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⚡ ${String.format("%.1f", avgEnergyCost)} avg | 💥 $totalDamage dmg | 🛡️ $totalShield blk | 💚 $totalHeal heal",
                        color = Color.LightGray,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (showTopHeaderExpanded) {
                        Text(
                            "Tap ▲ to collapse",
                            color = OsrsGold.copy(alpha = 0.8f),
                            fontSize = 8.5.sp
                        )
                    }
                }

                // Expanded Section: Deck Slots Carousel & Energy Curve (hidden by default)
                if (showTopHeaderExpanded) {
                    HorizontalDivider(color = Color(0xFF37474F), thickness = 0.5.dp)

                    Text("Saved Deck Slots:", color = OsrsTextYellow, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1B5E20),
                                border = BorderStroke(1.dp, Color(0xFF81C784)),
                                modifier = Modifier.clickable { showSaveDeckDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "New Deck", tint = Color.White, modifier = Modifier.size(10.dp))
                                    Text("New", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        items(savedDeckLoadouts, key = { it.id }) { loadout ->
                            val isEquipped = activeDeckLoadoutId == loadout.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isEquipped) Color(0xFF2E3D52) else Color(0xFF141D28),
                                border = BorderStroke(
                                    width = if (isEquipped) 1.5.dp else 1.dp,
                                    color = if (isEquipped) OsrsGold else Color(0xFF37474F)
                                ),
                                modifier = Modifier.combinedClickable(
                                    onClick = { viewModel.loadDeckLoadout(loadout.id) },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedDeckForOptions = loadout
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(loadout.iconEmoji, fontSize = 10.sp)
                                    Text(
                                        loadout.name,
                                        color = if (isEquipped) OsrsGold else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = if (isEquipped) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text("(${loadout.cardIds.size})", color = Color.LightGray, fontSize = 8.sp)

                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Deck Options",
                                        tint = OsrsGold.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable { selectedDeckForOptions = loadout }
                                    )
                                }
                            }
                        }
                    }

                    // Energy Curve Distribution
                    Text("Energy Distribution:", color = Color.LightGray, fontSize = 9.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EnergyBar("0⚡", count0Cost, deckSize, Color(0xFF2E7D32), Modifier.weight(1f))
                        EnergyBar("1⚡", count1Cost, deckSize, Color(0xFF1565C0), Modifier.weight(1f))
                        EnergyBar("2⚡", count2Cost, deckSize, Color(0xFFE65100), Modifier.weight(1f))
                        EnergyBar("3+⚡", count3PlusCost, deckSize, Color(0xFF8E24AA), Modifier.weight(1f))
                    }
                }
            }
        }

        // --- 2. TAB CONTROLS (My Deck vs Decks vs Grimoire) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TabButton(
                label = "🎴 In Deck ($deckSize)",
                isSelected = activeTab == DeckBuilderTab.MY_DECK,
                badgeColor = if (deckSize in 5..15) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.weight(1f),
                onClick = { activeTab = DeckBuilderTab.MY_DECK }
            )

            TabButton(
                label = "📁 Decks (${savedDeckLoadouts.size})",
                isSelected = activeTab == DeckBuilderTab.DECKS_LIST,
                badgeColor = OsrsGold,
                modifier = Modifier.weight(1f),
                onClick = { activeTab = DeckBuilderTab.DECKS_LIST }
            )

            TabButton(
                label = "📚 Grimoire (${allCards.size})",
                isSelected = activeTab == DeckBuilderTab.CARD_LIBRARY,
                badgeColor = Color(0xFF1565C0),
                modifier = Modifier.weight(1f),
                onClick = { activeTab = DeckBuilderTab.CARD_LIBRARY }
            )
        }

        // --- 4. MAIN WORKSPACE BASED ON SELECTED TAB ---
        when (activeTab) {
            DeckBuilderTab.MY_DECK -> {
                // Quick Deck Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tap card to remove (✕) • Long-press for details",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { viewModel.fillRecommendedDeckForStance(adventuringCombatStance) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("✨ Auto-Fill", color = OsrsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { viewModel.clearCustomDeck() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🧹 Clear", color = Color(0xFFEF5350), fontSize = 10.sp)
                        }
                    }
                }

                if (activeDeckCards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎴", fontSize = 36.sp)
                            Text(
                                "Your deck is currently empty!\nSwitch to 'Card Grimoire' tab or tap 'Auto-Fill' to equip cards.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.fillRecommendedDeckForStance(adventuringCombatStance) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("✨ Fill Recommended $adventuringCombatStance Deck", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(activeDeckCards, key = { it.id }) { card ->
                            val playerLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[card.skill] ?: 0L)
                            val isUnlocked = playerLvl >= card.reqLevel

                            DeckCardRow(
                                card = card,
                                isUnlocked = isUnlocked,
                                isInDeck = true,
                                onAction = { viewModel.toggleCustomDeckCard(card.id) },
                                onLongPress = { enlargedCard = card }
                            )
                        }
                    }
                }
            }

            DeckBuilderTab.DECKS_LIST -> {
                // Compact Action Header with New Deck & Presets buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Saved Decks & Presets",
                        color = OsrsGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { showSaveDeckDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("➕ New Deck", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showPresetsDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OsrsTextYellow),
                            border = BorderStroke(1.dp, OsrsGold),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("✨ Presets", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Decks List
                val allDecksToDisplay = remember(savedDeckLoadouts) {
                    val custom = savedDeckLoadouts
                    val presets = ArchetypeDeckPresets.PRESETS
                    (custom + presets).distinctBy { it.id }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allDecksToDisplay, key = { it.id }) { loadout ->
                        val isEquipped = activeDeckLoadoutId == loadout.id
                        DeckLoadoutCardItem(
                            loadout = loadout,
                            isEquipped = isEquipped,
                            allCards = allCards,
                            onEquip = { viewModel.loadDeckLoadout(loadout.id) },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedDeckForOptions = loadout
                            },
                            onOptionsClick = {
                                selectedDeckForOptions = loadout
                            }
                        )
                    }
                }
            }

            DeckBuilderTab.CARD_LIBRARY -> {
                // Search Input & Filters Row
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("🔍 Search cards, skills, or effects...", fontSize = 11.sp, color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OsrsGold,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141C24),
                        unfocusedContainerColor = Color(0xFF141C24)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                )

                // Stance & Effect Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Stance Filters
                    item {
                        FilterPill("🌐 All Stances", selectedStanceFilter == "ALL") { selectedStanceFilter = "ALL" }
                    }
                    item {
                        FilterPill("⚔️ Melee", selectedStanceFilter == "MELEE") { selectedStanceFilter = "MELEE" }
                    }
                    item {
                        FilterPill("🏹 Ranged", selectedStanceFilter == "RANGED") { selectedStanceFilter = "RANGED" }
                    }
                    item {
                        FilterPill("🪄 Magic", selectedStanceFilter == "MAGIC") { selectedStanceFilter = "MAGIC" }
                    }

                    // Energy Filters
                    item {
                        FilterPill(
                            label = if (selectedEnergyFilter == null) "⚡ Any Cost" else "⚡ ${if (selectedEnergyFilter == 3) "3+" else selectedEnergyFilter.toString()}",
                            isSelected = selectedEnergyFilter != null,
                            accentColor = Color(0xFFFFB74D)
                        ) {
                            selectedEnergyFilter = when (selectedEnergyFilter) {
                                null -> 0
                                0 -> 1
                                1 -> 2
                                2 -> 3
                                else -> null
                            }
                        }
                    }

                    // Effect Filters
                    item {
                        FilterPill("💥 Damage", selectedEffectFilter == "DAMAGE", Color(0xFFFF7043)) {
                            selectedEffectFilter = if (selectedEffectFilter == "DAMAGE") "ALL" else "DAMAGE"
                        }
                    }
                    item {
                        FilterPill("🛡️ Shield", selectedEffectFilter == "SHIELD", Color(0xFF64B5F6)) {
                            selectedEffectFilter = if (selectedEffectFilter == "SHIELD") "ALL" else "SHIELD"
                        }
                    }
                    item {
                        FilterPill("💚 Heal", selectedEffectFilter == "HEAL", Color(0xFF81C784)) {
                            selectedEffectFilter = if (selectedEffectFilter == "HEAL") "ALL" else "HEAL"
                        }
                    }
                    item {
                        FilterPill("⚡ Buff", selectedEffectFilter == "BUFF", Color(0xFFBA68C8)) {
                            selectedEffectFilter = if (selectedEffectFilter == "BUFF") "ALL" else "BUFF"
                        }
                    }

                    // Unlocked Only Toggle
                    item {
                        FilterPill("✓ Unlocked Only", showOnlyUnlocked, Color(0xFF81C784)) {
                            showOnlyUnlocked = !showOnlyUnlocked
                        }
                    }
                }

                // Skill Tabs Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterPill(
                            label = "All Skills",
                            isSelected = selectedSkillFilter == null,
                            onClick = { selectedSkillFilter = null }
                        )
                    }

                    val availableSkills = OsrsSkill.entries.filter { skill ->
                        allCards.any { it.skill == skill }
                    }

                    items(availableSkills, key = { it.name }) { skill ->
                        val playerLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[skill] ?: 0L)
                        val isSelected = selectedSkillFilter == skill
                        FilterPill(
                            label = "${skill.iconSymbol} ${skill.displayName} ($playerLvl)",
                            isSelected = isSelected,
                            accentColor = skill.accentColor,
                            onClick = { selectedSkillFilter = if (selectedSkillFilter == skill) null else skill }
                        )
                    }
                }

                // Subheader & Sort
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Showing ${filteredLibraryCards.size} cards",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Sort:", color = Color.Gray, fontSize = 10.sp)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF263238),
                            modifier = Modifier.clickable {
                                val nextIndex = (sortOption.ordinal + 1) % CardSortOption.entries.size
                                sortOption = CardSortOption.entries[nextIndex]
                            }
                        ) {
                            Text(
                                "${sortOption.displayName} ⬍",
                                color = OsrsGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        TextButton(
                            onClick = {
                                filteredLibraryCards.forEach { card ->
                                    val isSelected = customDeckCardIds.contains(card.id)
                                    if (!isSelected) {
                                        viewModel.toggleCustomDeckCard(card.id)
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("+ Add Filtered", color = OsrsGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Cards List
                if (filteredLibraryCards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No cards match your active search/filter criteria.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredLibraryCards, key = { it.id }) { card ->
                            val isSelected = if (customDeckCardIds.isEmpty()) {
                                activeDeckCards.any { it.id == card.id }
                            } else {
                                customDeckCardIds.contains(card.id)
                            }
                            val playerLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[card.skill] ?: 0L)
                            val isUnlocked = playerLvl >= card.reqLevel

                            DeckCardRow(
                                card = card,
                                isUnlocked = isUnlocked,
                                isInDeck = isSelected,
                                onAction = { viewModel.toggleCustomDeckCard(card.id) },
                                onLongPress = { enlargedCard = card }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- SAVE / EDIT DECK DIALOG ---
    if (showSaveDeckDialog) {
        SaveDeckModal(
            currentDeckCardCount = customDeckCardIds.size,
            initialName = currentLoadout?.name ?: "My Battle Deck",
            initialStance = currentLoadout?.stance ?: adventuringCombatStance,
            initialEmoji = currentLoadout?.iconEmoji ?: "🎴",
            onSave = { name, stance, emoji ->
                viewModel.saveCurrentDeckAsLoadout(name, emoji, stance)
                showSaveDeckDialog = false
            },
            onDismiss = { showSaveDeckDialog = false }
        )
    }

    // --- ARCHETYPES PRESETS DIALOG ---
    if (showPresetsDialog) {
        ArchetypePresetsModal(
            onEquipPreset = { presetId ->
                viewModel.loadDeckLoadout(presetId)
                showPresetsDialog = false
            },
            onSelectDeckOptions = { presetDeck ->
                selectedDeckForOptions = presetDeck
            },
            onDismiss = { showPresetsDialog = false }
        )
    }

    // --- DECK OPTIONS MODAL (LONG PRESS OR MENU TRIGGER) ---
    selectedDeckForOptions?.let { loadout ->
        DeckOptionsModal(
            loadout = loadout,
            isEquipped = activeDeckLoadoutId == loadout.id,
            onEquip = {
                viewModel.loadDeckLoadout(loadout.id)
            },
            onCopy = {
                viewModel.duplicateDeckLoadout(loadout.id)
            },
            onRename = {
                deckToRename = loadout
            },
            onDelete = {
                showDeleteConfirmDialog = loadout
            },
            onDismiss = { selectedDeckForOptions = null }
        )
    }

    // --- RENAME DECK MODAL ---
    deckToRename?.let { loadout ->
        RenameDeckModal(
            loadout = loadout,
            onRename = { newName ->
                viewModel.renameDeckLoadout(loadout.id, newName)
                deckToRename = null
            },
            onDismiss = { deckToRename = null }
        )
    }

    // --- DELETE CONFIRM DIALOG ---
    showDeleteConfirmDialog?.let { loadout ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Deck Profile?", color = OsrsGold) },
            text = { Text("Are you sure you want to delete '${loadout.name}' ${loadout.iconEmoji}?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDeckLoadout(loadout.id)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = OsrsDarkPanel
        )
    }

    // --- ENLARGED CARD PREVIEW DIALOG ---
    enlargedCard?.let { card ->
        val playerLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[card.skill] ?: 0L)
        val isUnlocked = playerLvl >= card.reqLevel
        EnlargedCardDetailDialog(
            card = card,
            isPlayable = isUnlocked,
            onDismiss = { enlargedCard = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeckCardRow(
    card: CombatCard,
    isUnlocked: Boolean,
    isInDeck: Boolean,
    onAction: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                isInDeck -> Color(0xFF1B3A24)
                !isUnlocked -> Color(0xFF181A1D)
                else -> Color(0xFF1F2933)
            }
        ),
        border = BorderStroke(
            width = if (isInDeck) 1.5.dp else 1.dp,
            color = when {
                isInDeck -> OsrsGold
                !isUnlocked -> Color(0xFF2E3440)
                else -> Color(0xFF37474F)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onAction,
                onLongClick = onLongPress
            )
            .testTag("deck_card_item_${card.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(card.iconEmoji, fontSize = 22.sp)

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            card.title,
                            color = if (isUnlocked) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Energy Cost Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (card.energyCost == 0) Color(0xFF2E7D32) else Color(0xFFD84315)
                        ) {
                            Text(
                                "⚡${card.energyCost}",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        if (card.stance != "ALL") {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF37474F)
                            ) {
                                Text(
                                    card.stance,
                                    color = Color.LightGray,
                                    fontSize = 7.sp,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "${card.skill.iconSymbol} ${card.skill.displayName}",
                            color = card.skill.accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (isUnlocked) {
                            Text("✓ Lv.${card.reqLevel}", color = Color(0xFF81C784), fontSize = 8.sp)
                        } else {
                            Text("🔒 Req Lv.${card.reqLevel}", color = Color(0xFFEF5350), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }

                        if (card.baseDamage > 0) Text("💥${card.baseDamage}", color = Color(0xFFFFB74D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        if (card.baseShield > 0) Text("🛡️${card.baseShield}", color = Color(0xFF64B5F6), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        if (card.baseHeal > 0) Text("💚${card.baseHeal}", color = Color(0xFF81C784), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        if (card.nextAttackBuff > 0) Text("⚡+${card.nextAttackBuff}", color = Color(0xFFBA68C8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        card.description,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Add / Remove Toggle Button
            Surface(
                shape = CircleShape,
                color = if (isInDeck) Color(0xFF2E7D32) else Color(0xFF263238),
                border = BorderStroke(1.dp, if (isInDeck) OsrsGold else Color.Gray.copy(alpha = 0.5f)),
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onAction() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isInDeck) "✓" else "+",
                        color = if (isInDeck) OsrsGold else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color.LightGray, fontSize = 8.sp)
            Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EnergyBar(label: String, count: Int, total: Int, color: Color, modifier: Modifier = Modifier) {
    val pct = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text("$count", color = Color.White, fontSize = 8.sp)
        }
        LinearProgressIndicator(
            progress = { pct },
            color = color,
            trackColor = Color(0xFF263238),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF2E3D52) else Color(0xFF141D28),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) OsrsGold else Color(0xFF263342)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (isSelected) OsrsGold else OsrsParchment,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    accentColor: Color = OsrsGold,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0xFF1E262C),
        border = BorderStroke(
            1.dp,
            if (isSelected) accentColor else Color.Gray.copy(alpha = 0.4f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) accentColor else Color.LightGray,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SaveDeckModal(
    currentDeckCardCount: Int,
    initialName: String,
    initialStance: String,
    initialEmoji: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var stance by remember { mutableStateOf(initialStance) }
    var selectedEmoji by remember { mutableStateOf(initialEmoji) }

    val emojis = listOf("🎴", "⚔️", "🏹", "🪄", "🛡️", "🐉", "💀", "🌿", "💎", "⚡", "👑", "🔥", "❄️", "🗡️", "🩸", "🌟")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsDarkPanel,
            border = BorderStroke(2.dp, OsrsGold),
            modifier = Modifier.fillMaxWidth()
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
                    Text("💾 Save Deck Profile", color = OsrsGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text("Profile Name:", color = Color.LightGray, fontSize = 11.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OsrsGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141C24),
                        unfocusedContainerColor = Color(0xFF141C24)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Icon Avatar:", color = Color.LightGray, fontSize = 11.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emojis) { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) OsrsGold.copy(alpha = 0.3f) else Color(0xFF141D28),
                            border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color.Gray.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { selectedEmoji = emoji }
                        ) {
                            Text(emoji, fontSize = 18.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }

                Text("Combat Stance:", color = Color.LightGray, fontSize = 11.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("ALL" to "🌐 All", "MELEE" to "⚔️ Melee", "RANGED" to "🏹 Ranged", "MAGIC" to "🪄 Magic").forEach { (st, lbl) ->
                        val isSelected = stance == st
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) Color(0xFF2E3D52) else Color(0xFF141D28),
                            border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color(0xFF37474F)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { stance = st }
                        ) {
                            Text(
                                lbl,
                                color = if (isSelected) OsrsGold else Color.White,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = { onSave(name, stance, selectedEmoji) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text("✓ Save Deck ($currentDeckCardCount Cards)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArchetypePresetsModal(
    onEquipPreset: (String) -> Unit,
    onSelectDeckOptions: (SavedDeckLoadout) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = remember { ArchetypeDeckPresets.PRESETS }
    val haptic = LocalHapticFeedback.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsDarkPanel,
            border = BorderStroke(2.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✨ Archetype Deck Presets", color = OsrsGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    "Select a combat archetype or tap ⋮ for options:",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF37474F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onEquipPreset(preset.id) },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSelectDeckOptions(preset)
                                        onDismiss()
                                    }
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
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
                                        Text(preset.iconEmoji, fontSize = 20.sp)
                                        Column {
                                            Text(preset.name, color = OsrsGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("${preset.cardIds.size} Cards • Stance: ${preset.stance}", color = Color.LightGray, fontSize = 9.sp)
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = { onEquipPreset(preset.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Equip", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        IconButton(
                                            onClick = {
                                                onSelectDeckOptions(preset)
                                                onDismiss()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                tint = OsrsGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Text(preset.description, color = Color.LightGray.copy(alpha = 0.85f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckOptionsModal(
    loadout: SavedDeckLoadout,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    onCopy: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsDarkPanel,
            border = BorderStroke(2.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(loadout.iconEmoji, fontSize = 24.sp)
                        Column {
                            Text(
                                loadout.name,
                                color = OsrsGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${loadout.cardIds.size} Cards • ${loadout.stance} Stance${if (loadout.isPreset) " • Preset" else ""}",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color(0xFF37474F), thickness = 0.5.dp)

                // 1. Equip Option (if not active)
                if (!isEquipped) {
                    DeckOptionRow(
                        icon = "🎴",
                        title = "Equip Deck",
                        subtitle = "Set as active combat deck for adventures",
                        accentColor = Color(0xFF2E7D32),
                        onClick = {
                            onEquip()
                            onDismiss()
                        }
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⭐", fontSize = 14.sp)
                            Text("Currently Equipped as Active Deck", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. Copy Option
                DeckOptionRow(
                    icon = "📋",
                    title = "Copy Deck (Duplicate)",
                    subtitle = "Create an editable clone of this loadout",
                    accentColor = Color(0xFF1976D2),
                    onClick = {
                        onCopy()
                        onDismiss()
                    }
                )

                // 3. Rename Option
                DeckOptionRow(
                    icon = "✏️",
                    title = "Rename Deck",
                    subtitle = "Change profile name and display title",
                    accentColor = Color(0xFFF57C00),
                    onClick = {
                        onRename()
                        onDismiss()
                    }
                )

                // 4. Delete Option
                if (!loadout.isPreset) {
                    DeckOptionRow(
                        icon = "🗑️",
                        title = "Delete Deck",
                        subtitle = "Permanently remove this custom deck profile",
                        accentColor = Color(0xFFD32F2F),
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    )
                } else {
                    DeckOptionRow(
                        icon = "🔒",
                        title = "Preset Deck Protected",
                        subtitle = "Built-in archetype presets cannot be deleted (use Copy instead)",
                        accentColor = Color.Gray,
                        enabled = false,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun DeckOptionRow(
    icon: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) Color(0xFF1E293B) else Color(0xFF141A24),
        border = BorderStroke(1.dp, if (enabled) accentColor.copy(alpha = 0.5f) else Color(0xFF263238)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = if (enabled) OsrsGold else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    color = if (enabled) Color.LightGray else Color.DarkGray,
                    fontSize = 9.5.sp
                )
            }
            if (enabled) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun RenameDeckModal(
    loadout: SavedDeckLoadout,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(loadout.name) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsDarkPanel,
            border = BorderStroke(2.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✏️ Rename Deck", color = OsrsGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    "Enter a new name for '${loadout.name}' ${loadout.iconEmoji}:",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Deck Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OsrsGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141C24),
                        unfocusedContainerColor = Color(0xFF141C24)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("Cancel", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onRename(newName.trim())
                            }
                        },
                        enabled = newName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("✓ Save", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeckLoadoutCardItem(
    loadout: SavedDeckLoadout,
    isEquipped: Boolean,
    allCards: List<CombatCard>,
    onEquip: () -> Unit,
    onLongPress: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) Color(0xFF1E2D40) else Color(0xFF141C26)
        ),
        border = BorderStroke(
            width = if (isEquipped) 1.5.dp else 1.dp,
            color = if (isEquipped) OsrsGold else Color(0xFF2C394B)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEquip,
                onLongClick = onLongPress
            )
            .testTag("deck_loadout_${loadout.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(loadout.iconEmoji, fontSize = 20.sp)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                loadout.name,
                                color = if (isEquipped) OsrsGold else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isEquipped) {
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = Color(0xFF1B5E20)
                                ) {
                                    Text(
                                        "EQUIPPED",
                                        color = Color.White,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            if (loadout.isPreset) {
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = Color(0xFF0D47A1)
                                ) {
                                    Text(
                                        "PRESET",
                                        color = Color.White,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            "${loadout.cardIds.size} Cards • Stance: ${loadout.stance}",
                            color = Color.LightGray,
                            fontSize = 9.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isEquipped) {
                        Button(
                            onClick = onEquip,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Equip", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onOptionsClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Deck Options",
                            tint = OsrsGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (loadout.description.isNotBlank()) {
                Text(
                    loadout.description,
                    color = Color.LightGray.copy(alpha = 0.85f),
                    fontSize = 9.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Cards Preview Chips
            val previewCards = remember(loadout.cardIds, allCards) {
                allCards.filter { loadout.cardIds.contains(it.id) }.take(5)
            }
            if (previewCards.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    previewCards.forEach { card ->
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = Color(0xFF0D1520),
                            border = BorderStroke(0.5.dp, Color(0xFF37474F))
                        ) {
                            Text(
                                card.title,
                                color = Color.LightGray,
                                fontSize = 8.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (loadout.cardIds.size > 5) {
                        Text(
                            "+${loadout.cardIds.size - 5} more",
                            color = OsrsGold,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
