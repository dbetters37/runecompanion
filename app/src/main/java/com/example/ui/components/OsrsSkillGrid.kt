package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.EquipmentSlot
import com.example.data.models.InventoryItem
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.data.models.CombatManager
import com.example.ui.components.weatheredStoneBorder
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OsrsSkillGrid(
    skillXpMap: Map<OsrsSkill, Long>,
    onSkillClick: (OsrsSkill) -> Unit,
    modifier: Modifier = Modifier,
    completedQuestIds: List<String> = emptyList(),
    inventoryItems: List<InventoryItem> = emptyList(),
    bankItems: List<InventoryItem> = emptyList(),
    equippedItems: Map<EquipmentSlot, InventoryItem> = emptyMap(),
    onOpenTrainerLeague: (() -> Unit)? = null
) {
    var skillForItemsDialog by remember { mutableStateOf<OsrsSkill?>(null) }

    skillForItemsDialog?.let { targetSkill ->
        SkillItemsDialog(
            skill = targetSkill,
            inventoryItems = inventoryItems,
            bankItems = bankItems,
            equippedItems = equippedItems,
            onDismissRequest = { skillForItemsDialog = null }
        )
    }

    val totalLevel = skillXpMap.values.sumOf { OsrsXpCalculator.getLevelForXp(it) }
    val totalXp = skillXpMap.values.sum()

    val combatBreakdown = remember(skillXpMap) { CombatManager.getCombatBreakdown(skillXpMap) }
    val petCombatLevel = combatBreakdown.combatLevel
    val att = combatBreakdown.attackLevel
    val def = combatBreakdown.defenceLevel
    val hp = combatBreakdown.hitpointsLevel
    val rng = combatBreakdown.rangedLevel
    val mag = combatBreakdown.magicLevel

    val baseStat = combatBreakdown.baseStat
    val meleeStat = combatBreakdown.meleeStat
    val rangeStat = combatBreakdown.rangeStat
    val mageStat = combatBreakdown.mageStat
    val maxOffense = combatBreakdown.maxOffense

    val completedSet = remember(completedQuestIds) { completedQuestIds.toSet() }
    val inventoryIds = remember(inventoryItems) { inventoryItems.map { it.id }.toSet() }

    val totemItems = listOf(
        Triple("item_totem_woodland", "Woodland Totem", "🪵"),
        Triple("item_totem_mist_fen", "Mist Fen Totem", "🐺"),
        Triple("item_totem_ancient_crag", "Ancient Crag Totem", "🗿"),
        Triple("item_totem_sacred_grove", "Sacred Grove Totem", "🌳"),
        Triple("item_totem_ember_spirit", "Ember Spirit Totem", "🔥"),
        Triple("item_totem_celestial_canopy", "Celestial Canopy Totem", "🌌"),
        Triple("item_totem_astral_bloom", "Astral Bloom Totem", "🌺"),
        Triple("item_totem_sovereign_wild", "Sovereign Wild Totem", "🦌")
    )

    val unlockedBadgesCount = totemItems.count { (itemId, _, _) ->
        inventoryIds.contains(itemId) ||
                inventoryIds.contains(itemId.replace("totem_", "badge_")) ||
                completedSet.contains("tl_kanto_4_pewter_gym") && (itemId == "item_totem_woodland" || itemId == "item_badge_boulder") ||
                completedSet.contains("tl_kanto_7_cerulean_gym") && (itemId == "item_totem_mist_fen" || itemId == "item_badge_cascade") ||
                completedSet.contains("tl_kanto_11_vermilion_gym") && (itemId == "item_totem_ancient_crag" || itemId == "item_badge_thunder") ||
                completedSet.contains("tl_kanto_14_celadon_gym") && (itemId == "item_totem_sacred_grove" || itemId == "item_badge_rainbow") ||
                completedSet.contains("tl_kanto_18_fuchsia_gym") && (itemId == "item_totem_ember_spirit" || itemId == "item_badge_soul") ||
                completedSet.contains("tl_kanto_20_saffron_gym") && (itemId == "item_totem_celestial_canopy" || itemId == "item_badge_marsh") ||
                completedSet.contains("tl_kanto_23_cinnabar_gym") && (itemId == "item_totem_astral_bloom" || itemId == "item_badge_volcano") ||
                completedSet.contains("tl_kanto_24_viridian_gym") && (itemId == "item_totem_sovereign_wild" || itemId == "item_badge_earth")
    }

    val hasLeagueTrophy = inventoryIds.contains("item_badge_league") || completedSet.contains("tl_kanto_30_champion")

    var showCombatCalcDialog by remember { mutableStateOf(false) }

    if (showCombatCalcDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCombatCalcDialog = false },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = { showCombatCalcDialog = false },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                ) {
                    Text("Got It 👍", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🛡️", fontSize = 22.sp)
                    Column {
                        Text("Pet Combat Level Calculation", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Level $petCombatLevel Breakdown", color = Color(0xFF00B4D8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Combat level is calculated using standard Old School RuneScape formula principles based on your pet's 7 combat stats:",
                        color = Color.White,
                        fontSize = 11.5.sp
                    )

                    Surface(
                        color = Color(0xFF141E28),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3E50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📐 Formula:", color = Color(0xFF00FF9D), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("• Base = 0.25 × (Def + HP)", color = Color.LightGray, fontSize = 10.5.sp)
                            Text("• Melee = 0.325 × (Hand Combat + Strength)", color = Color.LightGray, fontSize = 10.5.sp)
                            Text("• Blowdarts = 0.325 × ⌊1.5 × Blowdarts⌋", color = Color.LightGray, fontSize = 10.5.sp)
                            Text("• Incantations = 0.325 × ⌊1.5 × Incantations⌋", color = Color.LightGray, fontSize = 10.5.sp)
                            Text("• Combat Lv = ⌊Base + Max(Melee, Blowdarts, Incantations)⌋", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Text("📊 Your Pet's Current Combat Stats:", color = Color(0xFF00FF9D), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("🛡️ Warding: Level $def", color = Color.LightGray, fontSize = 10.5.sp)
                        Text("❤️ Life Energy: Level $hp", color = Color.LightGray, fontSize = 10.5.sp)
                        Text("⚔️ Hand Combat: Level $att", color = Color.LightGray, fontSize = 10.5.sp)
                        Text("🏹 Blowdarts: Level $rng", color = Color.LightGray, fontSize = 10.5.sp)
                        Text("🪄 Incantations: Level $mag", color = Color.LightGray, fontSize = 10.5.sp)
                    }

                    androidx.compose.material3.HorizontalDivider(color = Color(0xFF2C3E50))

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("• Base Vitality / Warding: ${"%.2f".format(baseStat)}", color = Color(0xFF00B4D8), fontSize = 10.5.sp)
                        Text("• Melee Rating: ${"%.2f".format(meleeStat)}", color = Color.LightGray, fontSize = 10.5.sp)
                        Text("• Blowdarts Rating: ${"%.2f".format(rangeStat)}", color = Color.LightGray, fontSize = 10.5.sp)
                        Text("• Incantations Rating: ${"%.2f".format(mageStat)}", color = Color.LightGray, fontSize = 10.5.sp)
                        Text("• Highest Offense: ${"%.2f".format(maxOffense)}", color = Color(0xFF00FF9D), fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                        Text("• Total Calculated: ${"%.2f".format(baseStat + maxOffense)} ➔ Level $petCombatLevel", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            },
            containerColor = Color(0xFF0D1B2A)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(OsrsLeatherMedium)
            .weatheredStoneBorder(cornerRadius = 10.dp)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Skill Grid header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚔️ SKILL PROGRESS (${OsrsSkill.entries.size - 1} SKILLS)",
                    color = OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = "💡 Hold down on a skill to track its items!",
                    color = Color(0xFF00B4D8),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "Total XP: ${String.format("%,d", totalXp)}",
                color = OsrsGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- FOREST REALM TOTEMS SUMMARY CARD ---
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2C1E14),
            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onOpenTrainerLeague != null) Modifier.clickable { onOpenTrainerLeague() } else Modifier)
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌲", fontSize = 14.sp)
                        Text(
                            text = "FOREST REALM TOTEMS",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = OsrsGold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold)
                    ) {
                        Text(
                            text = "$unlockedBadgesCount / 8 Totems${if (hasLeagueTrophy) " 👑" else ""}",
                            color = OsrsGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    totemItems.forEach { (itemId, name, emoji) ->
                        val isUnlocked = inventoryIds.contains(itemId) ||
                                inventoryIds.contains(itemId.replace("totem_", "badge_")) ||
                                completedSet.contains("tl_kanto_4_pewter_gym") && (itemId == "item_totem_woodland" || itemId == "item_badge_boulder") ||
                                completedSet.contains("tl_kanto_7_cerulean_gym") && (itemId == "item_totem_mist_fen" || itemId == "item_badge_cascade") ||
                                completedSet.contains("tl_kanto_11_vermilion_gym") && (itemId == "item_totem_ancient_crag" || itemId == "item_badge_thunder") ||
                                completedSet.contains("tl_kanto_14_celadon_gym") && (itemId == "item_totem_sacred_grove" || itemId == "item_badge_rainbow") ||
                                completedSet.contains("tl_kanto_18_fuchsia_gym") && (itemId == "item_totem_ember_spirit" || itemId == "item_badge_soul") ||
                                completedSet.contains("tl_kanto_20_saffron_gym") && (itemId == "item_totem_celestial_canopy" || itemId == "item_badge_marsh") ||
                                completedSet.contains("tl_kanto_23_cinnabar_gym") && (itemId == "item_totem_astral_bloom" || itemId == "item_badge_volcano") ||
                                completedSet.contains("tl_kanto_24_viridian_gym") && (itemId == "item_totem_sovereign_wild" || itemId == "item_badge_earth")

                        Text(
                            text = emoji,
                            fontSize = if (isUnlocked) 18.sp else 14.sp,
                            color = if (isUnlocked) Color.Unspecified else Color.Gray.copy(alpha = 0.4f)
                        )
                    }

                    Text(
                        text = "👑",
                        fontSize = if (hasLeagueTrophy) 18.sp else 14.sp,
                        color = if (hasLeagueTrophy) Color.Unspecified else Color.Gray.copy(alpha = 0.4f)
                    )
                }
            }
        }

        // 3-Column OSRS Grid with Combat skills descending vertically in the first column
        val displaySkills = remember {
            listOf(
                OsrsSkill.ATTACK, OsrsSkill.HITPOINTS, OsrsSkill.SMITHING,
                OsrsSkill.DEFENCE, OsrsSkill.AGILITY, OsrsSkill.FISHING,
                OsrsSkill.RANGED, OsrsSkill.HERBLORE, OsrsSkill.COOKING,
                OsrsSkill.THIEVING, OsrsSkill.FIREMAKING,
                OsrsSkill.MAGIC, OsrsSkill.FLETCHING, OsrsSkill.WOODCUTTING,
                OsrsSkill.RUNECRAFT, OsrsSkill.SLAYER, OsrsSkill.FARMING,
                OsrsSkill.CONSTRUCTION, OsrsSkill.HUNTER, OsrsSkill.DIVINATION,
                OsrsSkill.SAILING, OsrsSkill.ADVENTURING
            )
        }
        val skillChunks = remember(displaySkills) { displaySkills.chunked(3) }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            skillChunks.forEachIndexed { index, rowSkills ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowSkills.forEach { skill ->
                        val xp = skillXpMap[skill] ?: 0L
                        val level = OsrsXpCalculator.getLevelForXp(xp)
                        val progress = OsrsXpCalculator.getXpProgressToNextLevel(xp)

                        Box(modifier = Modifier.weight(1f)) {
                            SkillTile(
                                skill = skill,
                                level = level,
                                progress = progress,
                                onClick = { onSkillClick(skill) },
                                onLongClick = { skillForItemsDialog = skill }
                            )
                        }
                    }
                    if (rowSkills.size == 1) {
                        // Place Spacer in middle, and Total Level Tile directly in Column 3 (under Navigation)!
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.weight(1f)) {
                            TotalLevelTile(totalLevel = totalLevel)
                        }
                    } else if (rowSkills.size == 2) {
                        Box(modifier = Modifier.weight(1f)) {
                            TotalLevelTile(totalLevel = totalLevel)
                        }
                    }
                }
            }
            if (skillChunks.lastOrNull()?.size == 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Spacer(modifier = Modifier.weight(2f))
                    Box(modifier = Modifier.weight(1f)) {
                        TotalLevelTile(totalLevel = totalLevel)
                    }
                }
            }
        }

        // --- COMPACT PET COMBAT LEVEL AT BOTTOM ---
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF1B2430),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B4D8)),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { showCombatCalcDialog = true },
                    onLongClick = { showCombatCalcDialog = true }
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🛡️", fontSize = 13.sp)
                        Text(
                            text = "Pet Combat Level",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                    Text(
                        text = "Factored from Hand Combat, Strength, Warding, Life Energy, Blowdarts & Incantations",
                        color = Color.LightGray,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00B4D8).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B4D8))
                ) {
                    Text(
                        text = "Level $petCombatLevel",
                        color = Color(0xFF00B4D8),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SkillTile(
    skill: OsrsSkill,
    level: Int,
    progress: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // Skills that have real-life / app activities synced
    val isSyncedSkill = remember(skill) {
        when (skill) {
            OsrsSkill.WOODCUTTING,
            OsrsSkill.AGILITY,
            OsrsSkill.THIEVING,
            OsrsSkill.HERBLORE,
            OsrsSkill.ATTACK,
            OsrsSkill.CONSTRUCTION,
            OsrsSkill.MAGIC,
            OsrsSkill.RUNECRAFT,
                        OsrsSkill.FARMING,
            OsrsSkill.FISHING,
            OsrsSkill.COOKING -> true
            else -> false
        }
    }

    val tileBgBrush = remember(skill) {
        androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(
                skill.accentColor.copy(alpha = 0.35f),
                Color(0xFF231C16),
                skill.accentColor.copy(alpha = 0.18f)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(tileBgBrush)
            .border(1.dp, skill.accentColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
            .testTag("skill_tile_${skill.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        // Background Progress Fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .align(Alignment.CenterStart)
                .background(skill.accentColor.copy(alpha = 0.30f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = skill.iconSymbol,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(end = 3.dp)
                )
                Text(
                    text = skill.displayName,
                    color = OsrsParchment,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isSyncedSkill) {
                    Text(
                        text = " ∞",
                        color = Color(0xFF70E000),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                text = "Lvl $level",
                color = OsrsTextYellow,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

@Composable
fun TotalLevelTile(totalLevel: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF381C10))
            .border(1.dp, OsrsGold, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total Level",
                color = OsrsGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$totalLevel",
                color = OsrsTextYellow,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
