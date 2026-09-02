package com.example.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BossData
import com.example.data.models.BossMonster
import com.example.data.models.DropTableItem
import com.example.data.models.HunterCreature
import com.example.data.models.HunterData
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.data.models.SlayerData
import com.example.data.models.SlayerMonster
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.PetViewModel

@Composable
fun SlideableLootRow(
    title: String,
    titleColor: Color,
    drops: List<DropTableItem>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = titleColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(drops) { drop ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E1914),
                    border = BorderStroke(1.dp, Color(0xFF4A3B2C))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(drop.iconEmoji, fontSize = 11.sp)
                        Text(
                            text = "${drop.itemName} (${drop.chancePercent}%)",
                            fontSize = 10.5.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OsrsSlayerHunterTab(viewModel: PetViewModel) {
    val activeBossBattle by viewModel.activeBossBattle.collectAsStateWithLifecycle()

    if (activeBossBattle != null) {
        BossBattleStageView(viewModel = viewModel, modifier = Modifier.fillMaxSize())
        return
    }

    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Slayer, 1 = Bosses, 2 = Hunter
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()

    val isAfkSlayer by viewModel.isAfkSlayerActive.collectAsStateWithLifecycle()
    val isAfkBoss by viewModel.isAfkBossActive.collectAsStateWithLifecycle()
    val isAfkHunter by viewModel.isAfkHunterActive.collectAsStateWithLifecycle()

    val activeSlayerMonster by viewModel.selectedSlayerMonster.collectAsStateWithLifecycle()
    val activeBossMonster by viewModel.selectedBossMonster.collectAsStateWithLifecycle()
    val activeHunterCreature by viewModel.selectedHunterCreature.collectAsStateWithLifecycle()

    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val bankItems by viewModel.bankItems.collectAsStateWithLifecycle()
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val selectedCombatStyle by viewModel.selectedCombatStyle.collectAsStateWithLifecycle()
    val isDefensiveMode by viewModel.isDefensiveCombatMode.collectAsStateWithLifecycle()

    val npcFavorMap by viewModel.npcFavorMap.collectAsStateWithLifecycle()
    val theronFavorLvl = npcFavorMap["theron"]?.first ?: viewModel.getNpcFavorLevel("theron")
    val kaelFavorLvl = npcFavorMap["kael"]?.first ?: viewModel.getNpcFavorLevel("kael")
    val petCombatLvl = viewModel.calculatePetCombatLevel()

    val slayerXp = skillXpMap[OsrsSkill.SLAYER] ?: 0L
    val slayerLevel = OsrsXpCalculator.getLevelForXp(slayerXp)
    val slayerNextLevelXp = OsrsXpCalculator.getXpForLevel(slayerLevel + 1)
    val slayerCurrentLevelXp = OsrsXpCalculator.getXpForLevel(slayerLevel)
    val slayerProgress = if (slayerLevel >= 99) 1f else ((slayerXp - slayerCurrentLevelXp).toFloat() / (slayerNextLevelXp - slayerCurrentLevelXp).toFloat()).coerceIn(0f, 1f)

    val hunterXp = skillXpMap[OsrsSkill.HUNTER] ?: 0L
    val hunterLevel = OsrsXpCalculator.getLevelForXp(hunterXp)
    val hunterNextLevelXp = OsrsXpCalculator.getXpForLevel(hunterLevel + 1)
    val hunterCurrentLevelXp = OsrsXpCalculator.getXpForLevel(hunterLevel)
    val hunterProgress = if (hunterLevel >= 99) 1f else ((hunterXp - hunterCurrentLevelXp).toFloat() / (hunterNextLevelXp - hunterCurrentLevelXp).toFloat()).coerceIn(0f, 1f)

    var showTheronBonusBreakdown by remember { mutableStateOf(false) }
    var showKaelBonusBreakdown by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(12.dp)
        ) {
        // Top Sub-Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2C241B))
                .border(1.dp, Color(0xFF8B7355), RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedSubTab = 0 }
                    .background(if (selectedSubTab == 0) Color(0xFF4A3B2C) else Color.Transparent)
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💀 Bounty Hunter ($slayerLevel)",
                    color = if (selectedSubTab == 0) Color(0xFFFFD700) else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedSubTab = 1 }
                    .background(if (selectedSubTab == 1) Color(0xFF4A3B2C) else Color.Transparent)
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👑 Bosses ($petCombatLvl)",
                    color = if (selectedSubTab == 1) Color(0xFFFFD700) else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedSubTab = 2 }
                    .background(if (selectedSubTab == 2) Color(0xFF4A3B2C) else Color.Transparent)
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🐾 Beast Tracking ($hunterLevel)",
                    color = if (selectedSubTab == 2) Color(0xFFFFD700) else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedSubTab == 0) {
            // SLAYER SUB-TAB
            Surface(
                color = Color(0xFF3E1E14),
                border = BorderStroke(1.dp, Color(0xFFFF7043)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { showTheronBonusBreakdown = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("💀", fontSize = 13.sp)
                        Text("Theron Favor Perk:", color = Color(0xFFFFAB91), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("+${theronFavorLvl}% Double Task Reward ⓘ", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            // Combat Style Selector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C241B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF8B7355), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚔️ Combat Training Style:", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = if (isDefensiveMode) "Defensive (50% Def XP)" else "⚡ Full XP Mode",
                            color = if (isDefensiveMode) Color(0xFF81C784) else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        com.example.data.models.CombatStyle.entries.filter { it != com.example.data.models.CombatStyle.DEFENCE }.forEach { style ->
                            val isSelected = selectedCombatStyle == style
                            Button(
                                onClick = { viewModel.setSelectedCombatStyle(style) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF8B4513) else Color(0xFF1E1712)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF4A3828), RoundedCornerShape(6.dp))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(style.iconEmoji, fontSize = 14.sp)
                                    Text(style.displayName, fontSize = 8.5.sp, color = if (isSelected) Color(0xFFFFD700) else Color.LightGray, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Defensive Mode Stance Toggle
                    Button(
                        onClick = { viewModel.toggleDefensiveCombatMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDefensiveMode) Color(0xFF1B4D2E) else Color(0xFF382418)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isDefensiveMode) Color(0xFF81C784) else Color(0xFF8B7355), RoundedCornerShape(6.dp)),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isDefensiveMode) "Defensive Stance ACTIVE (50% XP to Defence)" else "Enable Defensive Stance (+50% Defence XP)",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slayer Progress Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C241B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF8B7355), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Row 1: Title, XP & Outfit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("💀 Slayer Bounty Progress", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Level $slayerLevel • ${"%,d".format(slayerXp)} XP", color = Color.LightGray, fontSize = 10.5.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { slayerProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFF9800),
                        trackColor = Color(0xFF42382C)
                    )
                    if (isAfkSlayer) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🔄 Active Task: Fighting ${activeSlayerMonster.name} in background!",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SlayerData.MONSTERS) { monster ->
                    val isUnlockedLevel = slayerLevel >= monster.reqSlayerLevel
                    val isUnlockedQuest = monster.reqQuestId == null || petState.completedQuestIds.contains(monster.reqQuestId)
                    val isUnlocked = isUnlockedLevel && isUnlockedQuest
                    val isThisAfk = isAfkSlayer && activeSlayerMonster.id == monster.id

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isThisAfk) Color(0xFF384A2A) else if (isUnlocked) Color(0xFF2E261D) else Color(0xFF211E1A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isThisAfk) 2.dp else 1.dp,
                                color = if (isThisAfk) Color(0xFF4CAF50) else if (isUnlocked) Color(0xFF8B7355) else Color(0xFF4A4238),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(monster.iconSymbol, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = monster.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isUnlocked) Color(0xFFFFD700) else Color.Gray
                                        )
                                        Text(
                                            text = "Req Lvl: ${monster.reqSlayerLevel} | HP: ${monster.maxHp} | XP: +${monster.xpReward}",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                if (!isUnlocked) {
                                    if (!isUnlockedLevel) {
                                        Text("🔒 Lvl ${monster.reqSlayerLevel}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("📜 Quest: ${monster.reqQuestName ?: "Required"}", color = Color(0xFFFF9800), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(monster.description, fontSize = 12.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(6.dp))
                            // Slideable Loot Row
                            SlideableLootRow(
                                title = "Loot Drops: ",
                                titleColor = Color(0xFFFFB74D),
                                drops = monster.drops
                            )

                            if (isUnlocked) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    CooldownActionButton(
                                        onClick = { viewModel.fightSlayerMonster(monster) },
                                        cooldownMs = 1200L,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFCD853F)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("⚔️ Fight Once", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { viewModel.toggleAfkSlayer(monster) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isThisAfk) Color(0xFF2E7D32) else Color(0xFF4A3B2C)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (isThisAfk) "⏹️ Stop AFK" else "🔄 AFK Task",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedSubTab == 1) {
            val maxPetHp = viewModel.getPetMaxHealth()
            val currentPetHp = petState.health.coerceAtMost(maxPetHp)
            val petHpRatio = if (maxPetHp > 0) currentPetHp.toFloat() / maxPetHp.toFloat() else 1f

            // BOSSES SUB-TAB
            // Pet Health Status Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C18)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFF6B6B), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("❤️ ${petState.customName} Health", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("$currentPetHp / $maxPetHp HP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { petHpRatio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (petHpRatio > 0.4f) Color(0xFF70E000) else Color(0xFFFF3333),
                            trackColor = Color(0xFF3E1C1A)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.feedPetLowestFood() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🍗 Feed Food", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Boss Lair Overview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C241B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑 Boss Lairs (Combat Lvl $petCombatLvl)", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val gearPower = viewModel.equippedItems.collectAsState().value.values.sumOf { it.combatPowerBonus }
                        Text("🛡️ Gear Power: +$gearPower", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Face fearsome bosses in strategic turn-based combat! Bosses execute unique move rotations and phases. Victory awards high-tier rare loot and trophies.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(BossData.BOSSES) { boss ->
                    val isUnlockedLevel = petCombatLvl >= boss.reqCombatLevel
                    val isUnlockedSlayer = boss.reqSlayerLevel == 0 || slayerLevel >= boss.reqSlayerLevel
                    val isUnlockedQuest = boss.reqQuestId == null || petState.completedQuestIds.contains(boss.reqQuestId)
                    val isUnlockedSkill = boss.reqSkill == null || OsrsXpCalculator.getLevelForXp(skillXpMap[boss.reqSkill] ?: 0L) >= boss.reqSkillLevel

                    val isFullyUnlocked = isUnlockedLevel && isUnlockedSlayer && isUnlockedQuest && isUnlockedSkill
                    val killTimeSec = viewModel.calculateBossKillTime(boss)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFullyUnlocked) Color(0xFF2E261D) else Color(0xFF211E1A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isFullyUnlocked) Color(0xFFFFD700) else Color(0xFF4A4238),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(boss.iconSymbol, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = boss.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isFullyUnlocked) Color(0xFFFFD700) else Color.Gray
                                        )
                                        Text(
                                            text = "Req Cbt: ${boss.reqCombatLevel} | HP: ${boss.maxHp} | Slayer Req: ${if (boss.reqSlayerLevel > 0) boss.reqSlayerLevel else "None"}",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                if (!isFullyUnlocked) {
                                    Surface(
                                        color = Color(0xFF4D1A1A),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                !isUnlockedLevel -> "🔒 Cbt ${boss.reqCombatLevel}"
                                                !isUnlockedSlayer -> "🔒 Slayer ${boss.reqSlayerLevel}"
                                                !isUnlockedQuest -> "🔒 Quest Needed"
                                                !isUnlockedSkill -> "🔒 ${boss.reqSkill?.displayName} ${boss.reqSkillLevel}"
                                                else -> "🔒 Locked"
                                            },
                                            color = Color(0xFFFF6B6B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            if (boss.reqQuestName != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📜 Quest Requirement: ${boss.reqQuestName}",
                                    fontSize = 10.5.sp,
                                    color = if (isUnlockedQuest) Color(0xFFA5D6A7) else Color(0xFFFF8A80),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(boss.description, fontSize = 12.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(6.dp))
                            // Slideable Boss Loot Table
                            SlideableLootRow(
                                title = "Boss Drops: ",
                                titleColor = Color(0xFFFFD700),
                                drops = boss.drops
                            )

                            if (isFullyUnlocked) {
                                Spacer(modifier = Modifier.height(8.dp))
                                CooldownActionButton(
                                    onClick = { viewModel.startInteractiveBossBattle(boss) },
                                    cooldownMs = 500L,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8860B)),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⚔️ Enter Boss Lair (Tactical Battle)", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedSubTab == 2) {
            // HUNTER SUB-TAB
            Surface(
                color = Color(0xFF1B3820),
                border = BorderStroke(1.dp, Color(0xFF81C784)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { showKaelBonusBreakdown = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("🐾", fontSize = 13.sp)
                        Text("Kael Favor Perk:", color = Color(0xFFA5D6A7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("+${kaelFavorLvl}% Double Task Reward ⓘ", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C241B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF8B7355), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Row 1: Title, XP & Outfit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🐾 Hunter Tracking Progress", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Level $hunterLevel • ${"%,d".format(hunterXp)} XP", color = Color.LightGray, fontSize = 10.5.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { hunterProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF8BC34A),
                        trackColor = Color(0xFF42382C)
                    )
                    if (isAfkHunter) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🔄 Active Hunter: Trapping ${activeHunterCreature.name} in background!",
                            color = Color(0xFF8BC34A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(HunterData.CREATURES) { creature ->
                    val isUnlockedLevel = hunterLevel >= creature.reqHunterLevel
                    val isUnlockedQuest = creature.reqQuestId == null || petState.completedQuestIds.contains(creature.reqQuestId)
                    val isUnlocked = isUnlockedLevel && isUnlockedQuest
                    val isThisAfk = isAfkHunter && activeHunterCreature.id == creature.id
                    val invTrapCount = inventoryItems.find { it.id == creature.requiredTrapItemId }?.quantity ?: 0
                    val bankTrapCount = bankItems.find { it.id == creature.requiredTrapItemId }?.quantity ?: 0
                    val totalTrapCount = invTrapCount + bankTrapCount

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isThisAfk) Color(0xFF2E3D2A) else if (isUnlocked) Color(0xFF2E261D) else Color(0xFF211E1A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isThisAfk) 2.dp else 1.dp,
                                color = if (isThisAfk) Color(0xFF8BC34A) else if (isUnlocked) Color(0xFF8B7355) else Color(0xFF4A4238),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(creature.iconSymbol, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = creature.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isUnlocked) Color(0xFFFFD700) else Color.Gray
                                        )
                                        Text(
                                            text = "Req Lvl: ${creature.reqHunterLevel} | XP: +${creature.xpReward}",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                if (!isUnlocked) {
                                    if (!isUnlockedLevel) {
                                        Text("🔒 Lvl ${creature.reqHunterLevel}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("📜 Quest: ${creature.reqQuestName ?: "Required"}", color = Color(0xFFFF9800), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            // Trap Requirement Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (totalTrapCount > 0) Color(0xFF1B3D1E) else Color(0xFF4D1A1A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🪤 Trap Needed: ${creature.requiredTrapName}",
                                        fontSize = 11.sp,
                                        color = if (totalTrapCount > 0) Color(0xFFA5D6A7) else Color(0xFFEF9A9A),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (totalTrapCount > 0) "Available: $totalTrapCount ⚡ (Inv: $invTrapCount | Bank: $bankTrapCount)" else "0 TRAPS AVAILABLE ❌",
                                        fontSize = 10.sp,
                                        color = if (totalTrapCount > 0) Color(0xFFFFD700) else Color(0xFFFF5252),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(creature.description, fontSize = 12.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(6.dp))
                            // Slideable Hunter Loot Row
                            SlideableLootRow(
                                title = "Trapped Items: ",
                                titleColor = Color(0xFF8BC34A),
                                drops = creature.drops
                            )

                            if (isUnlocked) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    CooldownActionButton(
                                        onClick = { viewModel.huntCreature(creature) },
                                        cooldownMs = 1200L,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF81C784)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🐾 Trap Once", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { viewModel.toggleAfkHunter(creature) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isThisAfk) Color(0xFF1B5E20) else Color(0xFF4A3B2C)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (isThisAfk) "⏹️ Stop AFK" else "🔄 AFK Trapping",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Close Column
        }

        // Floating NPC Companions: Kael on Beast Tracking (2), Eric on Bounty/Boss (0, 1)
        if (selectedSubTab == 2) {
            KaelNpcCompanion(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            EricNpcBadge(
                viewModel = viewModel,
                currentSubTab = selectedSubTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showTheronBonusBreakdown) {
            BonusBreakdownDialog(
                title = "Double Task Reward Chance",
                categoryName = "Slayer & Bounty Hunting",
                iconEmoji = "💀",
                sources = listOf(
                    BonusSourceDetail(
                        title = "Theron the Slayer Master's Favor (Lv. $theronFavorLvl)",
                        description = "Grants +1% chance per favor level to receive DOUBLE task rewards (2x Supply Boxes, 2x XP, 2x GP, 2x Favor XP) upon completing a Slayer task (Up to +50%).",
                        bonusPercent = theronFavorLvl,
                        emoji = "💀",
                        isUnlocked = true
                    )
                ),
                note = "When triggered upon task completion, your Slayer bounty rewards and supply crates are doubled! 🎁🎁",
                onDismiss = { showTheronBonusBreakdown = false }
            )
        }

        if (showKaelBonusBreakdown) {
            BonusBreakdownDialog(
                title = "Double Task Reward Chance",
                categoryName = "Hunter & Beast Tracking",
                iconEmoji = "🐾",
                sources = listOf(
                    BonusSourceDetail(
                        title = "Kael the Hunter Master's Favor (Lv. $kaelFavorLvl)",
                        description = "Grants +1% chance per favor level to receive DOUBLE task rewards (2x Supply Boxes, 2x XP, 2x GP, 2x Favor XP) upon completing a Hunter task (Up to +50%).",
                        bonusPercent = kaelFavorLvl,
                        emoji = "🐾",
                        isUnlocked = true
                    )
                ),
                note = "When triggered upon task completion, your Hunter tracking rewards and supply crates are doubled! 🎁🎁",
                onDismiss = { showKaelBonusBreakdown = false }
            )
        }
    }
}
